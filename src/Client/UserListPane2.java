/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import apendix.ConnectionClass;
import apendix.ConnectionClassLite;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import swing_gui.*;
/**
 *
 * @author amanpal
 */

class WindowEventHandler extends WindowAdapter {
    private final ChatClient client;
    private final String cmd;
    public WindowEventHandler(ChatClient client, String cmd)
    {
        this.client = client;
        this.cmd = cmd;
    }
    public void windowClosing(WindowEvent evt) {
        try {
            if(cmd.equalsIgnoreCase("exit"))
            {
                client.close();
            }
            else
            {
                client.disconnect();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
  }
}
//// above is later built class

public class UserListPane2 extends javax.swing.JFrame implements UserStatusListener, MessageListener, FileReceivedListener{

    private final ChatClient client;
    private Hashtable<String, Component> compoList;
    private JList<String> userListUI;
    private DefaultListModel<String> userListModel;
    private final String user;
    private boolean needInit = true;
    private LoginWindow lWindow;
    private boolean isClicked = false;
    private UserListPane2 myself;
    private ConnectionClass dbObject;
    /**
     * Creates new form UserListPane2
     * @param user
     * @param client
     */
    public UserListPane2(LoginWindow user, ChatClient client)
    {
        super(user.getUsername());
        this.myself = this;
        this.user = user.getUsername();
        this.client = client;
        this.client.addMessageListener(this);
        this.client.addUserStatusListener(this);
        this.lWindow = user;
        
        this.compoList = new Hashtable<>();
        userListModel = new DefaultListModel<>();
        userListUI = new JList<>(userListModel);
        //font and UI setting
        userListUI.setFont(new java.awt.Font("Segoe UI Semibold", 0, 28));
        userListUI.setAlignmentX(CENTER_ALIGNMENT);
        initComponents();
        needInit = false;
        // chatter select Event
        
        // disconnect before exit
        this.addWindowListener(new WindowEventHandler(client, "disconnect"));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         //offline list
        offlineList();
        refresh(usersList);
    }

    private void offlineList()
    {
        try {
            dbObject = ConnectionClass.getConnObj();
            PreparedStatement pSt = dbObject.conn.prepareStatement("select * from user");//where is_active = ?");
            //pSt.setString(1, "0");
            ResultSet set = pSt.executeQuery();
            while(set.next())
            {
                if(set.getString("username").equalsIgnoreCase(user))
                {
                    continue;
                }
                if(set.getString("is_active").equalsIgnoreCase("1"))
                {
                    Online userOnline = new Online();
                    userOnline.setName(set.getString("username"));
                    compoList.put(set.getString("username"), userOnline);
                    usersList.add(userOnline);
                    addClickListener(userOnline, set.getString("username"));
                }
                else
                {
                    offline userOffline = new offline();
                    userOffline.setName(set.getString("username"));
                    compoList.put(set.getString("username"), userOffline);
                    usersList.add(userOffline);
                    addClickListener(userOffline, set.getString("username"));
                }
                
                //userListModel.removeElement(login);
                refresh(usersList);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            this.dispose();
            System.exit(0);
        }
        
    }
    private void refresh(Component obj) {
        obj.revalidate();
        obj.repaint();
    }
    
    private void addClickListener(Component obj, String login)
    {
        obj.addMouseListener(new MouseAdapter() {
            @Override
             public void mouseClicked(MouseEvent e)
             {
                 if(e.getClickCount()>1 && isClicked == false)
                 {    
                    isClicked = true;
                    client.removeMessageListener(myself);
                    MessagePane messagePane = new MessagePane(client, login, user, lWindow, myself);
                    setVisible(false);
                    myself.dispose();
                    messagePane.setVisible(true);
                    
                 }
             }
        }); 
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane(userListUI);
        usersList = new javax.swing.JDesktopPane(){
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(574, 666));
        setResizable(false);
        setSize(new java.awt.Dimension(574, 666));

        jLabel1.setBackground(new java.awt.Color(255, 255, 255));
        jLabel1.setFont(new java.awt.Font("Segoe UI Semibold", 1, 48)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 102, 102));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Online Users");
        jLabel1.setFocusable(false);
        jLabel1.setPreferredSize(new java.awt.Dimension(2, 64));
        getContentPane().add(jLabel1, java.awt.BorderLayout.CENTER);

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setAlignmentY(10.0F);
        jScrollPane1.setFont(new java.awt.Font("Segoe UI Semibold", 0, 18)); // NOI18N
        jScrollPane1.setMaximumSize(new java.awt.Dimension(574, 600));
        jScrollPane1.setMinimumSize(new java.awt.Dimension(574, 600));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(574, 550));

        usersList.setBackground(new java.awt.Color(255, 255, 255));
        usersList.setMaximumSize(new java.awt.Dimension(574, 550));
        usersList.setMinimumSize(new java.awt.Dimension(574, 550));
        usersList.setLayout(new javax.swing.BoxLayout(usersList, javax.swing.BoxLayout.Y_AXIS));
        jScrollPane1.setViewportView(usersList);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.PAGE_END);

        jButton1.setBackground(new java.awt.Color(255, 51, 51));
        jButton1.setFont(new java.awt.Font("Segoe UI Semibold", 1, 13)); // NOI18N
        jButton1.setText("Log Out");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton1, java.awt.BorderLayout.LINE_END);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        this.dispose();
        try {
            client.disconnect();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        System.exit(0);
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(UserListPane2.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(UserListPane2.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(UserListPane2.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(UserListPane2.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    ChatClient client = new ChatClient("192.168.43.97", 8818);
                    new UserListPane2(new LoginWindow(),client).setVisible(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JDesktopPane usersList;
    // End of variables declaration//GEN-END:variables

    @Override
    public void online(String login) {
        if(compoList.containsKey(login))
        {
            usersList.remove(compoList.get(login));
            compoList.remove(login);
            Online userOnline = new Online();
            userOnline.setName(login);
            compoList.put(login, userOnline);
            usersList.add(userOnline);
            //userListModel.removeElement(login);
            System.out.println("offline to Online");
            addClickListener(userOnline, login);
            refresh(usersList);
            return;
        }
        Online userOnline = new Online();
        userOnline.setName(login);
        compoList.put(login, userOnline);
        usersList.add(userOnline);
        if(needInit == true)
            initComponents();
        //userListModel.addElement(login);
        addClickListener(userOnline, login);
        System.out.println("called-added");
        refresh(usersList);
    }

    @Override
    public void offline(String login) {
        
        if(compoList.containsKey(login) && compoList.get(login) != null)
        {
            usersList.remove(compoList.get(login));
            compoList.remove(login);
        }
        offline userOffline = new offline();
        userOffline.setName(login);
        usersList.add(userOffline);
        compoList.put(login, userOffline);
        //userListModel.removeElement(login);
        addClickListener(userOffline, login);
        System.out.println("Online to offline");
        refresh(usersList);
    }

    private String[] getIDs(String login)
    {
        String[] a = new String[2];
        try {
            dbObject = ConnectionClass.getConnObj();
            PreparedStatement pSt = dbObject.conn.prepareStatement("select * from user where `username` = ? OR `username` = ?");
            pSt.setString(1, this.user);        pSt.setString(2, login);
            ResultSet rs = pSt.executeQuery();
            if(rs.next())
            {
                //settin something
                
                if(rs.getString("username").equalsIgnoreCase(this.user))
                {
                    a[0] = rs.getString("id");
                    rs.next();
                    a[1] = rs.getString("id");
                }
                else
                {
                    a[1] = rs.getString("id");
                    rs.next();
                    a[0] = rs.getString("id");
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(MessagePane.class.getName()).log(Level.SEVERE, null, ex);
        }
        return a;
    }
    
    @Override
    public void onMessage(String fromLogin, String msgBody) 
    {
        String a[] = getIDs(fromLogin);
        try 
        {
            //getConnection to database
            ConnectionClassLite dbObjectLite = ConnectionClassLite.getConnObj(user);
            //date
            java.util.Date now = new java.util.Date();
            SimpleDateFormat fnow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String date = fnow.format(now);

            if(true)
            {
                // database query
                    PreparedStatement pSt2 = dbObjectLite.conn.prepareStatement("insert into messages(`sender_id`, `receiver_id`, `message_body`, `create_date`) values(?, ?, ?, ?)");
                    String text = msgBody;
                    if(msgBody.split(" ")[0].equalsIgnoreCase("###") && msgBody.split(" ")[1].contains("."))
                    {
                        text = msgBody;
                    }
                    pSt2.setString(1, a[1]); pSt2.setString(2, a[0]);
                    pSt2.setString(3, text);    pSt2.setString(4, date);
                    
                    int done = pSt2.executeUpdate();                  
            }
            else
            {
                //JOptionPane.showMessageDialog(null, "Error Storing Message");
            }
        } catch (SQLException ex) {
            Logger.getLogger(MessagePane.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void receivedFile(String from, String FileName) {
        String line = "### "+FileName;
        onMessage(from, FileName);
    }
}
