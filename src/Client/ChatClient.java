/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import File_Transfer.Receiver;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author amanpal
 */
public class ChatClient {
    //private final InetAddress serverName2;
    private final String serverName;
    private final int serverPort;
    private Socket socket;
    private OutputStream serverOut;
    private InputStream serverIn;
    private BufferedReader bufferedIn;
    
    private ArrayList<UserStatusListener> userStatusListeners = new ArrayList<>(); 
    private ArrayList<MessageListener> messageListeners = new ArrayList<>();
     private ArrayList<FileReceivedListener> receivedListeners = new ArrayList<>();
    
    public ChatClient(String serverName, int serverPort) {
        this.serverName = serverName;
        this.serverPort = serverPort;
        
    }
    
    public static void main(String arg[]) throws IOException, InterruptedException
    {
        ChatClient client = new ChatClient("192.168.43.97", 8818);
        client.addUserStatusListener(new UserStatusListener() {

            @Override
            public void online(String login) {
                System.out.println("ONLINE: "+login);
            }

            @Override
            public void offline(String login) {
                System.out.println("OFFLINE: "+login);
            }
        });
        
        client.addMessageListener(new MessageListener() {
            @Override
            public void onMessage(String fromLogin, String msgBody) {
                System.out.println("You got a message from " + fromLogin + " ===> " + msgBody);
            }
        });
        
        if(!client.connect())
        {
            System.err.println("Connection to Server Failed!");
        }
        else
        {
            System.out.println("Connected to Server");
            if(client.login("aman", "aman"))
            {
                System.out.println("You are LoggedIn");
                client.msg("guest", "Hello World!");
            }
            else
            {
                System.err.println("LogIn Failed");
            }
        }
        //client.disconnect();
    }

    //mine
    public void disconnect() throws IOException {
        String cmd = "logoff\n";
        serverOut.write(cmd.getBytes());
    }
    //mine 
    public void close() throws IOException
    {
        String cmd = "exit\n";
        serverOut.write(cmd.getBytes());
    }
    
    public void sentFile(String sendTo, String FileName) throws IOException
    {
        String cmd = "sentFile " + sendTo + " " + FileName + "\n";
        serverOut.write(cmd.getBytes());
    }
    
    public void file(String sendTo, InetAddress IP) throws IOException
    {
        String cmd = "file " + sendTo + " " + IP.getHostAddress() +"\n";
        serverOut.write(cmd.getBytes());
    }
    
    public void msg(String sendTo, String msgBody) throws IOException {
        String cmd = "msg " + sendTo + " " + msgBody + "\n";
        serverOut.write(cmd.getBytes());
    }
    
    public boolean connect() {
        try {
            this.socket = new Socket(serverName, serverPort);
            this.serverOut = socket.getOutputStream();
            this.serverIn = socket.getInputStream();
            this.bufferedIn = new BufferedReader(new InputStreamReader(serverIn));
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean login(String login, String password) throws IOException, InterruptedException {
        String cmd = "login "+login+" "+password+"\n";
        serverOut.write(cmd.getBytes());
        String reponse = bufferedIn.readLine();
        System.out.println(reponse);
        
        if("ok login".equalsIgnoreCase(reponse))
        {
            startMessageReader();
            return true;
        }
        else
            return false;
    }

    private void startMessageReader() {
        Thread t = new Thread() {
        
            @Override
            public void run()
            {
                readMessageLoop();
            }
        };
        t.start();
    }
    
    private void readMessageLoop()
    {   
        try 
        {
            String line;
            while((line = bufferedIn.readLine()) != null)
            {
                 String[] tokens = StringUtils.split(line);
                if(tokens != null && tokens.length > 0)
                {
                    String cmd = tokens[0];
                    if("online".equalsIgnoreCase(cmd))
                    {
                        handleOnline(tokens);
                    }
                    else if("offline".equalsIgnoreCase(cmd))
                    {
                        handleOffline(tokens);
                    }
                    else if ("msg".equalsIgnoreCase(cmd)) 
                    {
                        String[] tokensMsg = StringUtils.split(line, null, 3);
                        handleMessage(tokensMsg);
                    }
                    else if("file".equalsIgnoreCase(cmd))
                    {
                        handleFile(tokens);
                    }
                    else if("sentFile".equalsIgnoreCase(cmd))
                    {
                        handleFsent(tokens);
                    }
                }
            }
        } 
        catch (Exception ex) 
        {
            ex.printStackTrace();
            try {
                this.socket.close();
            } catch (IOException ex1) {
                Logger.getLogger(ChatClient.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
    }

    private void handleFsent(String[] tokens) {
        String login = tokens[1];
        String FileName = tokens[2];
        for(FileReceivedListener listener : receivedListeners) {
            listener.receivedFile(login, FileName);
        }
    }
    
    private void handleFile(String[] tokens) {
       Receiver temp = new Receiver(tokens[1], tokens[2], this);
    }
    
    private void handleMessage(String[] tokensMsg) {
        String login = tokensMsg[1];
        String msgBody = tokensMsg[2];

        for(MessageListener listener : messageListeners) {
            listener.onMessage(login, msgBody);
        }
    }
    
    private void handleOnline(String[] tokens) {
        String login = tokens[1];
        for(UserStatusListener listener : userStatusListeners)
        {
            listener.online(login);
        }
    }

    private void handleOffline(String[] tokens) {
        String login = tokens[1];
        for(UserStatusListener listener : userStatusListeners)
        {
            listener.offline(login);
        }    
    }
    
    public void addUserStatusListener(UserStatusListener listener) {
        userStatusListeners.add(listener);
    }
    
     public void removeUserStatusListener(UserStatusListener listener) {
        userStatusListeners.remove(listener);
    }
    
    public void addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
    }

    public void removeMessageListener(MessageListener listener) {
        messageListeners.remove(listener);
    }
    
    public void addFileReceivedListener(FileReceivedListener listener) {
        receivedListeners.add(listener);
    }
    
     public void removeFileReceivedListener(FileReceivedListener listener) {
        receivedListeners.remove(listener);
    }
}
