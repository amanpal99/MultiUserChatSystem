/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package File_Transfer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.StringTokenizer;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import Client.ChatClient;
/**
 *
 * @author amanpal
 */
public class Receiver {
    private ChatClient client;
    private String location;
    private String user;
    //private final ChatClient client;
    public Receiver(String user, String IP, ChatClient client) {
        this.user = user;
        this.client = client;
        location = "C:\\intraMessagingApp\\"+user+"\\received";
        makeDir(location);
        setIP(IP);
        
    }
    
    //making dir
    private void makeDir(String name)
    {
      boolean dirFlag = false;

        // create File object
        File stockDir = new File(name);

        try {
           dirFlag = stockDir.mkdir();
        } catch (SecurityException Se) {
        System.out.println("Error while creating directory in Java:" + Se);
        }

        if (dirFlag)
           System.out.println("Directory created successfully");
        else
           System.out.println("Directory was not created successfully");

    }

    private void setIP(String IP) {
        int add[] = new int[4];
            int counter=0;
            StringTokenizer st = new StringTokenizer(IP,".");
            while(st.hasMoreTokens())
            {
                try
                {
                    if(counter>=4)
                        new Integer("g");
                    add[counter++]=new Integer(st.nextToken());
                }
                catch(NumberFormatException nfe)
                {
                    JOptionPane.showMessageDialog(null,"IP address has only no.s seperated by dots (.)nIt is of the form xxx.xxx.xxx.xxx nwhere xxx is a no. less than 256","Integer Data Expected",0);
                    return;
                }
            }
            if(  !(counter == 4) || add[0] > 255 || add[1] > 255 || add[2] > 255 || add[3] > 255 )
            {
                JOptionPane.showMessageDialog(null, "Incorrect IP Address nIt is of the form xxx.xxx.xxx.xxx nwhere xxx is a no. less than 256","Invalid IP", 0);
                return;
            }
            new Thread(new Runnable()
            {
                public void run()
                {
                    recieveFile(IP);
                }
            }).start();
    }
    
    private void recieveFile(String IP)
    {
        byte b[] = new byte[100000];
        // array to retrieve data from server and send to client

        String sizeName[] = new String[2];
        // stores size and name of file as recieved from character streams

        double done=0,length;
        // done is used to count the percentage

        int read=0,i=0;
        // read counts the bytes read (within 4 bytes integer range) in WHILE loop

        // constructing streams
        BufferedReader br=null;
        // to read String and long data via Socket

        PrintWriter pw=null;
        // to write String and long data via Socket

        BufferedInputStream bis=null;
        //to write file contents (byte stream) via Socket

        BufferedOutputStream bos=null;
        //to read byte data via Socket

        FileOutputStream fos=null;
        // to read actual file using byte stream

        Socket s=null;
        // this will serve a local port for a client

        // now allocating memory to objects and starting main logic

        try
        {
            s = new Socket(IP,4000);
            System.out.println("Connected to server 4000!");
            br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            pw = new PrintWriter(s.getOutputStream());
            StringTokenizer st = new StringTokenizer(br.readLine(),"/");
            while(st.hasMoreTokens())
                sizeName[i++]=st.nextToken();
            pw.println("Recieved");
            pw.flush();
            length = new Double(sizeName[1]);
            bis = new BufferedInputStream  (s.getInputStream());
            bos = new BufferedOutputStream (s.getOutputStream());
            fos = new FileOutputStream(location+"\\"+sizeName[0]);

            while(true)
            {
                done+=read;
                if(done>=length)
                    break;
                read=bis.read(b);
                ClientSwingWorker csw = new ClientSwingWorker (done,length,read,b,fos);
                csw.execute();
                while(!(csw.isDone())) {}
            }
            fos.flush();

            JOptionPane.showMessageDialog(null, "Recieved 100%");
            
            double time = new Double(br.readLine());
            String speedString = br.readLine();
            bis.close();
            bos.close();
            fos.close();
            pw.close();
            br.close();
            s.close();
            
            client.sentFile(user, sizeName[0]);
            JOptionPane.showMessageDialog(null,"Time taken is "+time+"nSpeed is "+speedString+" MBPS","File Sent (Client)",3);
        }
        catch(Exception e)
        {
            if (e instanceof FileNotFoundException)
            {
                JOptionPane.showMessageDialog(null,"Failed in saving file,nLocation : "+location+" required administrative rights to save or it was an invalid pathnSelect some other location for downloaded filesnThe Program will now Exit and default location would be reset","Error ["+location+"]",0);
                try
                {
                    FileWriter fw = new FileWriter("D:\\fss\\log.bin");
                    fw.close();
                }
                catch(IOException ee)
                {
                }
                System.exit(1);
            }
            e.printStackTrace();
        }
    }
}

class ClientSwingWorker extends SwingWorker
{
    final double done,size;
    byte b[] = new byte[100000];
    final int read;
    FileOutputStream fos;
    ClientSwingWorker(double done,double size,int read,byte b[],FileOutputStream fos)
    {
        this.done = done;
        this.size = size;
        this.read = read;
        this.b    = b;
        this.fos  = fos;
    }
    protected Void doInBackground() throws Exception
    {
        fos.write(b,0,read);
        return null;
    }

    protected void done()
    {
        final double temp=(done/size)*100;
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                String tString = new Double(temp).toString();
                int index=tString.indexOf(".");
                int breakPoint= (index+3)>tString.length()?tString.length():(index+3);
                tString=tString.substring(0,breakPoint);
            }
        });
    }
}
