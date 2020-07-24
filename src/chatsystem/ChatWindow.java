package chatsystem;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.text.DefaultCaret;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JList;
import javax.swing.GroupLayout;
import javax.swing.LayoutStyle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;

public class ChatWindow extends JFrame {

    NetworkManager net;
    DefaultListModel<String> mlu;
    
    private JTextArea messageArea;
    private JButton btSend;
    private JButton btChooser;
    private JButton btSendFile;
    private JTextField fieldMsg;
    private JTextField fieldFile;
    private JList jList1;
    private JScrollPane jScrollPane2;
    private JScrollPane jScrollPane3;
    private GroupLayout groupLayout;
    
    public ChatWindow() {
        net = NetworkManager.getInstance();
        net.setServer(readIP(), 30000);
        net.setinterface2(this);
        net.submit("Username: " + inputNick());
        mlu = new DefaultListModel<>();
        initComponents();
        setComponentsExtras();
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                net.listenServer();
            }
        }).start();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                net.submit("EXIT");
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        
        //message typing area
        fieldMsg = new JTextField();
        fieldFile = new JTextField();
        //send button
        btSend = new JButton();
        btChooser = new JButton();
        btSendFile = new JButton();
        //list of messages from messageArea
        jScrollPane2 = new JScrollPane();
        //all messages from the user
        messageArea = new JTextArea();
        //list with username
        jScrollPane3 = new JScrollPane();
        //vertical bar with usernames
        jList1 = new JList();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        fieldMsg.addKeyListener(new KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                fieldMsgKeyPressed(evt);
            }
        });

        btSend.setText("Send");
        btSend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	btSubmitActionPerformed(evt);
            }
        });
        
        btChooser.setText("Choose File");
        btChooser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	btSubmitActionPerformed(evt);
            }
        });
        
        btSendFile.setText("Send File");
        btSendFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	btSubmitActionPerformed(evt);
            }
        });
        
        
        
        messageArea.setEditable(false);
        messageArea.setColumns(20);
        messageArea.setLineWrap(true);
        //vertical bar
        messageArea.setRows(40);
        messageArea.setToolTipText("");
        messageArea.setWrapStyleWord(true);
        messageArea.setBackground(Color.DARK_GRAY);
        
        jScrollPane2.setViewportView(messageArea);
       messageArea.setForeground(Color.WHITE);
        
        jList1.setModel(mlu);
        jList1.setFixedCellHeight(20);
        jList1.setBackground(Color.white);
        
        jScrollPane3.setViewportView(jList1);
         
        
        fieldMsg.setBackground(Color.white);
        fieldFile.setBackground(Color.white);
        btSend.setBackground(Color.DARK_GRAY);
        btSend.setForeground(Color.WHITE);
        btChooser.setBackground(Color.DARK_GRAY);
        btChooser.setForeground(Color.WHITE);
        btSendFile.setBackground(Color.DARK_GRAY);
        btSendFile.setForeground(Color.WHITE);
        
                
        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(fieldMsg)
                        // width of messages area
                    .addComponent(jScrollPane2, GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        //                                  width of list with names
                    .addComponent(jScrollPane3, GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                    .addComponent(btSend, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(fieldFile)
                    .addComponent(btChooser)
                    .addComponent(btSendFile)
                .addContainerGap())
                
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        // height of the whole chat system
                    .addComponent(jScrollPane2, GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
                    .addComponent(jScrollPane3))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(fieldMsg, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(btSend))
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                     .addComponent(fieldFile)    
                    .addComponent(btChooser)
                    .addComponent(btSendFile))
                .addContainerGap())
        );

        pack();
    }

    private void btSubmitActionPerformed(java.awt.event.ActionEvent evt) {
        //We send the message to the server
        net.submit(fieldMsg.getText());
        //We clean the text field
        fieldMsg.setText("");
    }

    private void fieldMsgKeyPressed(java.awt.event.KeyEvent evt) {
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
        	btSubmitActionPerformed(null);
        }
    }

    public static void main(String args[]) {
       
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ChatWindow().setVisible(true);
            }
        });
    }


    public void addUser(UserProp u) {
        mlu.addElement(u.getNick());
    }
    
    public void addMessage(String s) {
    	messageArea.append(s + "\n");
    }
    
    public void clearList() {
        mlu.clear();
    }

    private void setComponentsExtras() {
        DefaultCaret caret = (DefaultCaret)messageArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        jList1.revalidate();
        jList1.setFixedCellHeight(20);
        setLocationRelativeTo(null);
        fieldMsg.requestFocus();
    }

    private String readIP() {
        return JOptionPane.showInputDialog(null, "Enter the server IP", "127.0.0.1");
    }

    private String inputNick() {
        return JOptionPane.showInputDialog(null, "Enter your username", "Username");
    }
}

