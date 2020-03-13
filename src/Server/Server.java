/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 *
 * @author amanpal
 */
public class Server extends Thread{
    private final int serverPort;
    private ArrayList<ServerWorker> workerList = new ArrayList<>();
    
    public Server(int serverPort) {
        this.serverPort = serverPort;
    }
    
    public List<ServerWorker> getWorkerList()
    {
        return workerList;
    }
    
    @Override
    public void run()
    {
        try {
            ServerSocket serverSocket = new ServerSocket(serverPort);
            System.out.println("Server Running.....");
            while(true)
            {
                System.out.println("Waiting for client....");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected :: "+clientSocket);
                ServerWorker worker = new ServerWorker(this, clientSocket);
                workerList.add(worker);
                worker.start();
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeWorker(ServerWorker worker) 
    {
        workerList.remove(worker);
    }
    
}
