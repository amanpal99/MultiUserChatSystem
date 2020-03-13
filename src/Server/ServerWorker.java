/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import apendix.ConnectionClass;
import java.net.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author amanpal
 */
public class ServerWorker extends Thread{
    private final Socket clientSocket;
    private String login = null;
    private final Server server;
    private OutputStream outputStream;
    private HashSet<String> topicSet = new HashSet<>();
    private ConnectionClass dbObject = ConnectionClass.getConnObj();
    
    public ServerWorker(Server server, Socket clientSocket)
    {
        this.server = server;
        this.clientSocket = clientSocket;
    }
    
    @Override
    public void run()
    {
        try {
            handleClientSocket();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
    private void handleClientSocket() throws IOException, InterruptedException
    {
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while((line = reader.readLine()) != null)
        {
            String[] tokens = StringUtils.split(line);
            if(tokens != null && tokens.length > 0)
            {
                String cmd = tokens[0];
                if("exit".equalsIgnoreCase(cmd))
                {
                    handleLoginPageExit();
                    break;
                }
                else if ("logoff".equals(cmd) || "quit".equalsIgnoreCase(cmd)) 
                {
                    handleLogoff();
                    break;
                } 
                else if ("login".equalsIgnoreCase(cmd)) 
                {
                    handleLogin(outputStream, tokens);
                } 
                else if ("msg".equalsIgnoreCase(cmd))
                {
                    String[] msgTokens = StringUtils.split(line, null, 3);
                    handleMessage(msgTokens);
                }
                else if ("join".equalsIgnoreCase(cmd))
                {
                    handleLeave(tokens);
                }
                else if ("leave".equalsIgnoreCase(cmd))
                {
                    handleJoin(tokens);
                }
                else if("file".equalsIgnoreCase(cmd))
                {
                    handleFile(tokens);
                }
                else if("sentFile".equalsIgnoreCase(cmd))
                {
                    handleFSent(tokens);
                }
                else 
                {
                    String msg = "unknown " + cmd + "\n";
                    outputStream.write(msg.getBytes());
                }
            }
        }
        //clientSocket.close();
    }
    
    private void handleLoginPageExit() throws IOException
    {
        server.removeWorker(this);
        clientSocket.close();
    }
    
    private void handleLogoff() throws IOException 
    {
        // setting is_active 0
        try
        {
            PreparedStatement pSt = dbObject.conn.prepareStatement("update user set is_active = ? where username = ?");
            pSt.setString(1, "0");
            pSt.setString(2, login);

            //result set
            int done = pSt.executeUpdate();
            if(done == 1)
            {
                server.removeWorker(this);
                List<ServerWorker> workerList = server.getWorkerList();

                // send other online users current user's status
                String onlineMsg = "offline " + this.login + "\n";
                System.out.println(onlineMsg);
                for(ServerWorker worker : workerList) {
                    if (!this.login.equals(worker.getLogin())) {
                        worker.send(onlineMsg);
                    }
                }
            }
        }
        catch(SQLException ex)
        {
            ex.printStackTrace();
        }
        clientSocket.close();
    }
    
    public String getLogin()
    {
        return this.login;
    }
    
    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException {
        if (tokens.length == 3) {
            String login = tokens[1];
            String password = tokens[2];

            // mysql queries
            //getConnection to database
            dbObject = ConnectionClass.getConnObj();
            
            try 
            {
                PreparedStatement pSt = dbObject.conn.prepareStatement("select * from user where username = ? AND password = ?");
                pSt.setString(1, login);
                pSt.setString(2, password);
                
                //result set
                ResultSet done = pSt.executeQuery();
                
                if(done.next())
                {
                    if(done.getString("is_active").equalsIgnoreCase("1"))
                    {
                        JOptionPane.showMessageDialog(null, "Your ID is already Open somewhere!");
                        String msg = "error login\n";
                        outputStream.write(msg.getBytes());
                        System.err.println("Login failed by "+login);
                        return;
                    }
                    else
                    {
                        PreparedStatement pSt2 = dbObject.conn.prepareStatement("update user set is_active = ? where username = ? AND password = ?");
                        pSt2.setString(1, "1");
                        pSt2.setString(2, login);
                        pSt2.setString(3, password);
                        pSt2.executeUpdate();
                    }
                    String msg = "ok login\n";
                    outputStream.write(msg.getBytes());
                    this.login = login;
                    System.out.println("User logged in succesfully: " + login);

                    List<ServerWorker> workerList = server.getWorkerList();

                    // send current user all other online logins
                    for(ServerWorker worker : workerList) {
                        if (worker.getLogin() != null) {
                            if (!login.equals(worker.getLogin())) {
                                String msg2 = "online " + worker.getLogin() + "\n";
                                send(msg2);
                            }
                        }
                    }

                    // send other online users current user's status
                    String onlineMsg = "online " + login + "\n";
                    for(ServerWorker worker : workerList) {
                        if (!login.equals(worker.getLogin())) {
                            worker.send(onlineMsg);
                        }
                    }
                }
                else
                {
                    String msg = "error login\n";
                    outputStream.write(msg.getBytes());
                    System.err.println("Login failed by "+login);
                }
            } 
            catch (SQLException ex) 
            {
                JOptionPane.showMessageDialog(null, "Error While Login!");
                ex.printStackTrace();
            }
        }
    }
    
    private void send(String msg) throws IOException {
        if (login != null) {
            outputStream.write(msg.getBytes());
        }
    }

    private void handleMessage(String[] tokens) throws IOException {
        String sendTo = tokens[1];
        String body = tokens[2];
        
        boolean isTopic = sendTo.charAt(0) == '#';
        
        List<ServerWorker> workerList = server.getWorkerList();
        for(ServerWorker worker : workerList)
        {
            if(isTopic)
            {
                if(worker.isMemberOfTopic(sendTo) && !(worker.getLogin().equals(this.getLogin()))) // remember to make an exit of topic
                {
                    String outMsg = "msg " + sendTo + ":" + login + " " + body + "\n";
                    worker.outputStream.write(outMsg.getBytes());
                }
            }
            else if(sendTo.equalsIgnoreCase(worker.getLogin())) // during project remember to all "user not online"
            {
                String outMsg = "msg " + login + " " + body + "\n";
                worker.outputStream.write(outMsg.getBytes());
            }
        }
    }

    public boolean isMemberOfTopic(String topic)
    {
       return topicSet.contains(topic);
    }
    
    private void handleJoin(String[] tokens) {
        if(tokens.length > 1)
        {
            String topic = tokens[1];
            topicSet.add(topic);
        }
    }

    private void handleLeave(String[] tokens) {
        if(tokens.length > 1)
            if(isMemberOfTopic(tokens[1]))
            {
                topicSet.remove(tokens[1]);
            }
    }

    private void handleFile(String[] tokens) throws IOException {
        String sendTo = tokens[1];
        String IP = tokens[2];
        List<ServerWorker> workerList = server.getWorkerList();
        for(ServerWorker worker : workerList)
        {
            if(sendTo.equalsIgnoreCase(worker.getLogin())) // during project remember to all "user not online"
            {
                String outMsg = "file " + sendTo + " " + IP +"\n";
                worker.outputStream.write(outMsg.getBytes());
            }
        }
    }

    private void handleFSent(String[] tokens) throws IOException {
        String sendTo = tokens[1];
        String FileName = tokens[2];
        List<ServerWorker> workerList = server.getWorkerList();
        for(ServerWorker worker : workerList)
        {
            if(sendTo.equalsIgnoreCase(worker.getLogin())) // during project remember to all "user not online"
            {
                String outMsg = "sentFile " + login +" "+ FileName + "\n";
                worker.outputStream.write(outMsg.getBytes());
            }
        }
    }
}
