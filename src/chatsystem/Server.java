package chatsystem;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    public static final int port = 30000;
    public static ArrayList<Rooms> roomList;
    public static boolean saveLogs = true;

    public static void main(String[] args) throws IOException {
        if (saveLogs) 
        	System.out.println("Saved logging enabled"); 
        else 
        	System.out.println("Saved logs disabled"); 
        
        //We create the socket to listen for incoming connections
        ServerSocket ss = new ServerSocket(port);
        
        Log.log("Server initialized in the port " + port);
        
        //Initialize the list of rooms, create the "Main" room, which is
        //the room by default and add it to the list of rooms
        roomList = new ArrayList<>();
        Rooms room = new Rooms("Main_Room");
        addRoom(room);
        
        //infinite loop that waits for connection requests and creates separate instances for each
        while (true) {
            Socket socket = ss.accept();
            Log.log("Connection established with " + socket.getInetAddress().getHostAddress());
            //We create a new instance by passing the socket and the main room
            new Thread(new Users(socket, room)).start();
            
        }
    }
    
    public static void addRoom(Rooms r) {
        if (getRoom(r.getRoom_name()) == null) {
        	roomList.add(r);
            Log.log("The named room has been created " + r.getRoom_name());
        }
    }
    
    public static void deleteRoom(Rooms r) {
        if (getRoom(r.getRoom_name()) != null && !r.getRoom_name().equalsIgnoreCase("Main_Room")) {
            r.moveRoom(roomList.get(0));
            roomList.remove(r);
            Log.log("The named room has been removed " + r.getRoom_name());
        }
    }
    
    public static Rooms getRoom(String roomName) {
        for (Rooms r : roomList) {
            if (r.getRoom_name().equalsIgnoreCase(roomName)) {
                return r;
            }
        }
        return null;
    }
    
    public static Rooms[] getRooms() {
        Rooms[] r = new Rooms[roomList.size()];
        for (int i = 0; i < roomList.size(); i++) {
            r[i] = roomList.get(i);
        }
        return r;
    }
    
    public static boolean existsRoom(Rooms r) {
        for (int i = 0; i < roomList.size(); i++) {
            if (roomList.get(i).getRoom_name().equalsIgnoreCase(r.getRoom_name())) {
                return true;
            }
        }
        return false;
    }

}
