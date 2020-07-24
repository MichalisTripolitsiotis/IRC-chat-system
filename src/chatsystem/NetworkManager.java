

package chatsystem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;


public class NetworkManager {
    
    private Socket socket;
    private BufferedReader input;
    private BufferedWriter output;
    private static NetworkManager instance;
    private ChatWindow interface2;
    
    public static NetworkManager getInstance() {
        if (instance == null) {
        	instance = new NetworkManager();
        }
        return instance;
    }
    
    
    public void listenServer() {
    	initializeHeartBeat();
        try {
            while (true) {
                String packet = toReceive();
                if (packet.startsWith("Success confirmed:")) {
                    //Success confirmed:
                } else if (packet.startsWith("Disconnection confirmed:")) {
                    JOptionPane.showMessageDialog(interface2, packet.substring(4), "Disconnected", JOptionPane.ERROR_MESSAGE);
                  
                } else if (packet.startsWith("Error confirmed:")) {
                    JOptionPane.showMessageDialog(interface2, packet.substring(4), "Error", JOptionPane.WARNING_MESSAGE);
                } else if (packet.startsWith("ROOM")) {
                    String[] p = packet.split("[ ]");
                    interface2.setTitle("Room: " + p[1] + "@" + socket.getInetAddress().getHostAddress());
                } else if (packet.startsWith("LIST")) {
                	interface2.clearList();
                    int count = packet.split("[ ]").length;
                    for (int i = 1; i < count; i++) {
                        UserProp u = new UserProp(packet.split("[ ]")[i], null);
                        interface2.addUser(u);
                    }
                    
                } else if(!packet.isEmpty()) {
                	interface2.addMessage(packet);
                }
            }
        } catch (NullPointerException ex) {
            JOptionPane.showMessageDialog(interface2, "The connection to the server has been lost "," Disconnected", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }
    
    public void setServer(String IP, int port) {
        try {
            socket = new Socket(IP, port);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new BufferedWriter(new PrintWriter(socket.getOutputStream()));
        } catch (IOException ex) {
            Logger.getLogger(NetworkManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void submit(String var) {
        if (!var.isEmpty()) {
            try {
            	output.write(var + "\n");
            	output.flush();
            } catch (IOException ex) {
                Logger.getLogger(NetworkManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public String toReceive() {
        String s = "";
        try {
            s = input.readLine();
        } catch (IOException ex) {
            
        }
        return s;
    }
    
    public void setinterface2(ChatWindow inter_face) {
        this.interface2 = inter_face;
    }

    private void initializeHeartBeat() {
        new Thread(new Runnable() {
                @Override
                public void run() {
                    long heartbeat = System.currentTimeMillis();
                    while (true) {
                        if (System.currentTimeMillis() - heartbeat >= 4000) {
                        	submit("BEAT " + System.currentTimeMillis());
                            heartbeat = System.currentTimeMillis();              
                        }
                    }
                }
            }).start();
    }
}
