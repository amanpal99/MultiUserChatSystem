/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package apendix;

import java.sql.*;
import com.mysql.jdbc.Driver;

/**
 *
 * @author amanpal
 */
public class ConnectionClass {
    static ConnectionClass connObj;
    public static Connection conn;
    private ConnectionClass()
    {
        try
        { 
            //Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://192.168.43.97:3306/intranet", "root", "amanpal");
        }
        catch(Exception e)
        {
            System.out.println("SQL setting connection Error : "+e);
        }
        
    }
    public static ConnectionClass getConnObj()
    {
        if(connObj == null)
        {
            synchronized(ConnectionClass.class)
            {
                 if(connObj == null)
                 {
                     connObj = new ConnectionClass();
                 }
            }
        }
        
        return connObj;
    }
    
}