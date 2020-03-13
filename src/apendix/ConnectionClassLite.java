/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package apendix;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.DatabaseMetaData;
import java.io.File;

/**
 *
 * @author amanpal
 */
public class ConnectionClassLite {
    private String url = "jdbc:sqlite:C://intraMessagingApp//" + "intrachat.db"; 
    static ConnectionClassLite connObj;
    public static Connection conn;
    private ConnectionClassLite()
    {
        try
        {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(url);  
            if (conn != null) 
            {  
                DatabaseMetaData meta = conn.getMetaData();  
                System.out.println("The driver name is " + meta.getDriverName());  
                System.out.println("A new database has been created.");  
            }
        }
        catch(Exception e)
        {
            System.out.println("SQLLite setting connection Error : "+e);
        }
        
    }
    private ConnectionClassLite(String login)
    {
        makeDir("C://intraMessagingApp");
        makeDir("C://intraMessagingApp/"+login);
        this.url = "jdbc:sqlite:C://intraMessagingApp//"+login+"//" + "intrachat.db"; 
        try
        {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(url);  
            if (conn != null) 
            {  
                DatabaseMetaData meta = conn.getMetaData();  
                System.out.println("The driver name is " + meta.getDriverName());  
                System.out.println("A new database has been created.");  
            }
        }
        catch(Exception e)
        {
            System.out.println("SQLLite setting connection Error : "+e);
        }
        
    }
    //normal
    public static ConnectionClassLite getConnObj(String login)
    {
        if(connObj == null)
        {
            synchronized(ConnectionClassLite.class)
            {
                 if(connObj == null)
                 {
                     connObj = new ConnectionClassLite(login);
                 }
            }
        }
        
        return connObj;
    }
    
    //normal
     public static ConnectionClassLite getConnObj()
    {
        if(connObj == null)
        {
            synchronized(ConnectionClassLite.class)
            {
                 if(connObj == null)
                 {
                     connObj = new ConnectionClassLite();
                 }
            }
        }
        
        return connObj;
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
}