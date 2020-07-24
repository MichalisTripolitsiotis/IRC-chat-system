package chatsystem;



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Users  implements Runnable {
    
    public String nick;
    private BufferedReader input;
    private BufferedWriter output;
    private long loginTime;
    public boolean connected, superUser, heartBeatOn;
    public String IP;
    private long ping;
    public Rooms room;
    private long lastBeat;
    
   
    
    public Users(String nick) {
        this.nick = nick;
    }
    
    public Users(Socket s, Rooms room) throws IOException {
        this.room = room;
        this.loginTime = System.currentTimeMillis();
        this.IP = s.getInetAddress().getHostAddress();
        this.ping = 0;
        this.superUser = false;
        this.heartBeatOn = true;
        input = new BufferedReader(new InputStreamReader(s.getInputStream()));
        output = new BufferedWriter(new PrintWriter(s.getOutputStream()));
    }

    @Override
    public void run() {
        //Wait to receive the client login message
        String login = toReceive();
        //Check that the login message is correct
        if (login.startsWith("NICKNAME")||login.startsWith("nickname")||login.startsWith("LIST")||login.startsWith("list")||login.startsWith("exit")||login.startsWith("EXIT")) {
            //Disconnect the client if there is an error
        	submit("Disconnection confirmed: Invalid packet received");
            Log.log("Invalid login: " + login);
            connected = false;
        } else {
            //Check that the size of the nick is not too long
            if (login.split("[ ]")[1].length() >= 12) {
                //Send an error and disconnect the user
            	submit("Disconnection confirmed: The chosen nick is too long, enter a nick of at most 12 characters");
                Log.log("A user has tried to enter with a nick too long. Nickname: " + login.split("[ ]")[1]);
            } else {
                //Connects the client if the user does not exist in the room
            	connected = !room.userExists(this);
            }
        }
        //If everything is correct, it connects to the room
        if (connected) {
            //we have the nick received from the client
            nick = login.split("[ ]")[1];
            
            //We connect the user to the room
            submit(room.enter(this));
            //We send the list of users of the room
            sendListUsers();
            
            if (heartBeatOn) {
                //Initialize the heartbeat to verify that the client maintains the connection
                lastBeat = System.currentTimeMillis();
                asyncBeatCheck();
            }
            //We send the name of the room
            submit("ROOM: " + room.getRoom_name());
            
            //Loop that will last until the user disconnects (EXIT or errors)
            do {
                //We are waiting to receive a message from the client
                String packet = toReceive();

                //If the package is not empty, we analyze it
                if (packet != null && !packet.isEmpty()) {
                	analizePacket(packet);
                }
                
            } while (connected);
            
            //We send the disconnection message to the user
            submit("Disconnection confirmed: ");
            
            //The client is no longer connected, we take him out of the room
            room.getOut(this);
        }
    }
    
    public void analizePacket(String s) {
        if (s.startsWith("EXIT")||s.startsWith("exit")) { //Chat output request
        	connected = false;
        } else if (s.startsWith("BEAT")||s.startsWith("beat")) { //Heartbeat automatic message
            String[] p;
            p = s.split("[ ]");
            if (p.length > 2) {
            	submit("Error confirmed: Wrong Command, check syntax again!");
                Log.log("Wrong type of message sent: " + s);
            } else {
                //We updated the last time we received a heartbeat
                lastBeat = System.currentTimeMillis();
                //We update the user's ping
                ping = System.currentTimeMillis() - Long.parseLong(p[1]);
            }
        } else if (s.startsWith("/NICKNAME ")||s.startsWith("/nickname")) { //Request to change nick
            String[] p;
            p = s.split("[ ]");
            if (p.length > 2) {
            	submit("Error confirmed: Wrong Command, check syntax again!");
                Log.log("Wrong type of message sent: " + s);
            } else {
                //We saved the old nick
                String oldname = nick;
                //We check that there is not a user with the new nick in the room
                Rooms[] roomArray = Server.getRooms();
                 for (Rooms room1 : roomArray) {
                if (room1.userExists(new Users(p[1]))) {
                	submit("Error confirmed: There is already a user called " + p[1] + " in the room: "+room1.getRoom_name());
                    nick = oldname;
                    break;
                } 
                else {
                    if (!(p[1].length() > 12)) {
                        //We change the nick by the indicated
                        nick = p[1];
                        //We send the list of users to all the users of the room (so that they receive the new nick in the list)
                        room1.updateListedUsers();
                        Log.log(oldname + " has changed its name to " + nick);
                        //We send a message to all the users of the room indicating the nick change
                        room1.spread(oldname + " has changed its name to " + nick);
                        //We send a message indicating that the operation has been carried out successfully
                        submit("Success confirmed:");
                    } else {
                    	submit("Error confirmed: The nick chosen is too long. Max 12 characters");
                    }
                }
            }
        }
            }
        else if (s.startsWith("/WHOIS ")||s.startsWith("/whois ")) { //Requesting information about a user
            String[] p;
            p = s.split("[ ]");
            if (p.length > 2) {
            	submit("Error confirmed: Wrong Command, check syntax again!");
                Log.log("Wrong type of message sent: " + s);
            } else {
                Rooms[] roomArray = Server.getRooms();
                 for (Rooms room1 : roomArray) {
                for(int i=0;i<room1.getUsers().size();i++){
               
                //We check that the user is in the room
                if (!room1.userExists(new Users(p[1]))) {
                	submit("There is no user with the name: " + p[1]+" in the room: "+room1.getRoom_name());
                        break;
                } else {
                    //We obtain the required user of the room
                    Users tmp = room1.getUser(p[1]);
                    //We send the user's data
                    submit("==========\nFirst name: " + tmp.getNick()+"\nRoom: "+room1.getRoom_name() + "\nIP: " + tmp.getIP() + "\nPing: " + tmp.getPing() + "ms\nEntry: " + new Date(tmp.getLoginTime()).toGMTString() + "======================");
                    Log.log("Request for WHOIS of " + nick + " on " + tmp.getNick());
                }
            }
                   }
            }
        } else if (s.startsWith("/HELP")||s.startsWith("/help")) { //Command listing request
            String[] p;
            p = s.split("[ ]");
            
            //We send the list with the available commands
            submit("========================= Commands ===================== - /WHOIS <usr>: Shows user information");
            submit("- /PRIVATE <usr> <msg>: Send a private message to a user in the room \\  /NICKNAME <new>: Change your username");
            submit("- /CREATE <name> [PW]: Create a new room and put it in it. Optional password. \\  /JOIN <name> [PW]: Switch to the specified room");
            submit("- /LIST: Lists the rooms available on the server \\  EXIT : Exits the chat \\ n =============== =======");
        }  else if (s.startsWith("/PRIVATE")||s.startsWith("/private")) { //Private message
            String[] p;
            p = s.split("[ ]");
            Rooms[] roomArray = Server.getRooms();
                 for (Rooms room1 : roomArray) {
            //We check that the user exists in the room
            if (!room1.userExists(new Users(p[1]))) {
            	submit("There is no user with the name " + p[1]+" in room: "+room1.getRoom_name());
            } else {
                //We obtain the user to whom we will send the message
                Users tmp = room1.getUser(p[1]);
                //We send our user, the destination user, and the content of the private message to the room
                //The room will be responsible for sending the private message only to the recipient thereof
                room1.sendPrivateMessage(this, tmp, s.substring(3+tmp.getNick().length()+1));
                Log.log("Private message from " + this.getNick() + " to " + tmp.getNick() + ": " + s.substring(3+tmp.getNick().length()));
            }
        }   
        } else if (s.startsWith("/ADMIN ")||s.startsWith("/admin ")) { //Request for administrator permissions
            String[] p;
            p = s.split("[ ]");
            
            //We check that the administrator password sent matches
             if (p.length > 0) {
                if (p[1].equals(room.getAdminPass())) {
                    EraseRoomAdmin();
                    superUser = true;
                    submit("You have obtained Admin privileges");//We change the user mode to superuser
                    Log.log("Admin Privileges granted to " + nick); //We send a message only to the user indicating that their permissions have been changed correctly
                } else {
                    
                    submit("Error confirmed: The password for Admin privileges is incorrect");
                      
                }
            }
        } else if (s.startsWith("/GIVE ")||s.startsWith("/give ")) { //Request for administrator permissions
            String[] p;
            p = s.split("[ ]");
            //We check that the administrator password sent matches
             if (p.length > 0) {
                if(!room.userExists(new Users(p[1]))) {
                	submit("Error confirmed: There is no user with the name " + p[1]);
                } else {
                    //We get the user
                    Users tmp = room.getUser(p[1]);
                    EraseRoomAdmin();
                    tmp.setSuperUser(true);
                    tmp.submit("You have obtained Admin privileges");//We change the user mode to superuser
                    Log.log("Admin Privileges granted to " + tmp.nick); //We send a message only to the user indicating that their permissions have been changed correctly
                } 
            }
        } else if (s.startsWith("/KICK ")||s.startsWith("/kick ")) { //Request to take someone out of the chat
            String[] p;
            p = s.split("[ ]");
            
            if (p.length > 2) {
            	submit("Error confirmed: Wrong Command, check syntax again!");
                Log.log("Wrong type of message sent: " + s);
            } else if (!superUser) { //We verify that the user who performs the action is superuser
            	submit("Error confirmed: You are not the Admin!!! Action denied");
                Log.log("Attempt to use KICK command without superuser: " + nick);
            } else {
                //We check that the indicated user is in the room
                if (!room.userExists(new Users(p[1]))) {
                	submit("Error confirmed: There is no user with the name " + p[1]);
                } else {
                    //We get the user
                    Users tmp = room.getUser(p[1]);
                    Rooms roomArray = Server.getRoom("Main_Room");
                    //We take the user out of the current room
                     room.getOut(tmp);
                    //We change the room in the user
                       roomArray.enter(tmp);
                       tmp.setRoom(roomArray); //set room live for the tmp user
                       tmp.submit("ROOM: " + roomArray.getRoom_name());
                       roomArray.updateListedUsers();
                    //We distribute a message indicating the kick
                    room.spread(nick + " has thrown out " + tmp.getNick() + " from chat");
                        //We send the name of the new room to the user
                        submit("ROOM: " + room.getRoom_name());
                        room.updateListedUsers();
                        //We update the user list for all users of the room
                    //We warn the user that he has been expelled
                    tmp.submit("Disconnection confirmed: You have been expelled from the chat by " + nick);
                    Log.log(nick + " has thrown out " + tmp.getNick());
               
                }
            }
        } else if (s.startsWith("/BAN ")||s.startsWith("/ban ")) { //Ban user request (permanent)
            String[] p;
            p = s.split("[ ]");
            
            if (p.length > 2) {
            	submit("Error confirmed: Wrong Command, check syntax again!");
                Log.log("Wrong type of message sent: " + s);
            } else if (!superUser) { //We verify that the user who performs the action is superuser
            	submit("Error confirmed: You are not the Admin!!! Action denied");
                Log.log("Attempt to use BAN command without superuser: " + nick);
            } else {
                //We check that the indicated user is in the room
                if (!room.userExists(new Users(p[1]))) {
                	submit("Error confirmed: There is no user with the name " + p[1]);
                } else {
                    //We get the user
                    Users tmp = room.getUser(p[1]);
                    Rooms roomArray = Server.getRoom("Main_Room");
                    //We take the user out of the current room
                     room.getOut(tmp);
                    //We change the room in the user
                       roomArray.enter(tmp);
                       tmp.setRoom(roomArray);// set live room for the tmp user;
                        //We add the user as banned from the room
                       room.addBan(tmp);
                       tmp.submit("ROOM: " + roomArray.getRoom_name());
                       roomArray.updateListedUsers();
                     submit("ROOM: " + room.getRoom_name());
                      //We update the list of users for all users of the room
                     room.updateListedUsers();
                    //We show a message in the room indicating the ban
                    room.spread(nick + " has banned " + tmp.getNick() + " from the room");
                    //We warn the user that he has been expelled
                    tmp.submit("Disconnection confirmed: You have been banned from the chat by " + nick);
                    Log.log(nick + " has banned " + tmp.getNick());
                }
            }
        } else if (s.startsWith("/UNBAN ")||s.startsWith("/unban ")) { //Remove the ban to a user
            String[] p;
            p = s.split("[ ]");
            
            if (p.length > 2) {
            	submit("Error confirmed: Wrong Command, check syntax again!");
                Log.log("Wrong type of message sent: " + s);
            } else if (!superUser) { //We verify that the user who performs the action is superuser
            	submit("Error confirmed: You are not the Admin!!! Action denied");
                Log.log("Attempt to use UNBAN command without superuser: " + nick);
            } else {
                //We verify that the indicated user is really in the banned list of the room
                if (!room.isBanned(new Users(p[1]))) {
                	submit("Error confirmed: The user " + p[1] + " is not banned");
                } else {
                    //We eliminate the user from the banned list of the room
                    room.removeBan(p[1]);
                    room.spread(nick + " has unbanned " + p[1] + " from the room");
                    Log.log(nick + " has removed the ban " + p[1]);
                }
            }
        } else if (s.startsWith("/CREATE ")||s.startsWith("/create ")) { //Request for the creation of a new room
            String[] p;
            p = s.split("[ ]");
            
            if (p.length > 4) {
            	submit("Error confirmed: Wrong Command, check syntax again!");
                Log.log("Wrong type of message sent: " + s);
            } else {
                //We check that the room does not exist
                if (!Server.existsRoom(new Rooms(p[1]))) {
                    Rooms roomArray = null;
                    //Room without password and admin_pass(1 parameter)
                    if (p.length == 2) {
                        superUser = true;
                        //We create the room
                        roomArray = new Rooms(p[1]);
                    } else if (p.length == 3) { //Room with password and not admin_pass (2 parameters)
                        //We create the room
                        roomArray = new Rooms(p[1], p[2]);
                        superUser = true;
                    }
                    else if (p.length == 4) { //Room with password and admin_pass (3 parameters)
                        //We create the room
                        roomArray = new Rooms(p[1], p[2],p[3]);
                        superUser = true;
                    }
                    if (roomArray != null) {
                        //We add the room to the list of rooms
                        Server.addRoom(roomArray);
                        //
                        room.getOut(this);
                        //We take the user out of the current room
                        roomArray.enter(this);
                        //We change the room in the user
                        room = roomArray;
                        //We send the name of the new room to the user
                        submit("ROOM: " + room.getRoom_name());
                        //We update the user list for all users of the room
                        room.updateListedUsers();
                    }
                } else {
                	submit("Error confirmed: There is already a room with that name");
                }
            }
        } else if (s.startsWith("/JOIN ")||s.startsWith("/join ")) { //Request to enter an existing room
            String[] p;
            p = s.split("[ ]");
            
            if (p.length > 3) {
            	submit("Error confirmed: Wrong Command, check syntax again!");
                Log.log("Wrong type of message sent: " + s);
            } else {
                //We check that the room exists
                if (Server.existsRoom(new Rooms(p[1]))) {
                    //We check that the user is not banned from the room
                    if (!Server.getRoom(p[1]).isBanned(this)) {
                        //Received 1 parameter (room name)
                        if (p.length == 2) {
                            //We get the room from the name
                            Rooms roomArray = Server.getRoom(p[1]);
                            //If you have a password, we indicate that you can not enter without specifying it
                            if (roomArray.hasPassword()) {
                            	submit("Error confirmed: This room requires a password");
                            } else { //If you do not have a password, we enter the room
                                //We take the user out of the current room
                                EraseRoomAdmin();
                                room.getOut(this);
                                //We put it in the new room
                                roomArray.enter(this);
                                //We change the room in the user
                                room = roomArray;
                                //We send the name of the room
                                submit("ROOM: " + room.getRoom_name());
                                //We update the list of users for all users of the room
                                room.updateListedUsers();
                            }
                        } else if (p.length == 3) { //Received 2 parameters (name room + password)
                            //We get the room from the name
                            Rooms roomArray = Server.getRoom(p[1]);
                            //We verify that the indicated password matches that of the room
                            if (!roomArray.getPassword().equalsIgnoreCase(p[2])) {
                            	submit("Error confirmed: Incorrect room password");
                            } else { //The password matches
                                //We take the user out of the current room
                                EraseRoomAdmin();
                                room.getOut(this);
                                //We put it in the new room
                                roomArray.enter(this);
                                //We change the room in the user
                                room = roomArray;
                                //We send the name of the room
                                submit("ROOM: " + room.getRoom_name());
                                //We update the list of users for all users of the room
                                room.updateListedUsers();
                            }
                        }
                    } else {
                    	submit("Error confirmed: You can not access the room "+ p [1] +" because you are banned in it");
                    }
                } else { //There is no room
                	submit("Error confirmed: There is no room called " + p[1]);
                }
            }
        } else if (s.startsWith("/DELETE ")||s.startsWith("/delete ")) { //Request to remove a room
            String[] p;
            p = s.split("[ ]");
            
            if (p.length > 2) {
            	submit("Error confirmed: Wrong Command, check syntax again!");
                Log.log("Wrong type of message sent: " + s);
            } else {
                //We verify that the user that performs this action has superuser permissions
                if (p.length == 2 && superUser) {
                    //We verify that you are not eliminating the main room
                    if (p[1].equalsIgnoreCase("Main_Room")) {
                    	submit("Error confirmed: The main room can not be eliminated!");
                    } else {
                        //We check that the room exists
                        if (Server.existsRoom(new Rooms(p[1]))) {
                            EraseRoomAdmin();
                            //We get the room from the name
                            Rooms roomArray = Server.getRoom(p[1]);
                            //We inform all the users of the room that the user has deleted the room before deleting it
                            roomArray.spread(nick + " has removed the room");
                            //Remove the room from the server room list (move the users to the Main room)
                            Server.deleteRoom(roomArray);
                        } else { //There is no room
                        	submit("Error confirmed: There is no room called " + p[1]);
                        }
                    }
                } else {
                	submit("Error confirmed: You are not the Admin!!! Action denied");
                }
            }
        } 
        else if (s.startsWith("/PERMIT ")||s.startsWith("/permit ")) { //Request to permit someone to enter the room by the creator or admin
            String[] p;
            p = s.split("[ ]");
            
            if (p.length > 2) {
            	submit("Error confirmed: Wrong Command, check syntax again!");
                Log.log("Wrong type of message sent: " + s);
            } else {
                //We verify that the user that performs this action has superuser permissions
                if (p.length == 2 && superUser) {
                    //We verify that you are not eliminating the main room
                        Rooms[] roomArray = Server.getRooms();
                 for (Rooms room1 : roomArray) {
               
                //We check that the user is in the room
                if (!room1.userExists(new Users(p[1]))) {
                	submit("There is no user with the name: " + p[1]+" in the room: "+room1.getRoom_name());
                        break;
                } else {
                    //We obtain the required user of the room
                    Users tmp = room1.getUser(p[1]);
                     room1.getOut(tmp);
                                //We put it in the new room
                                room.enter(tmp);
                                //We change the room in the user
                              tmp.setRoom(room);// set live room for the tmp user;
                                //We send the name of the room
                                submit("ROOM: " + room.getRoom_name());
                                //We update the list of users for all users of the room
                                room.updateListedUsers();
                                room.spread(nick + " has permited and transferred the " + tmp.getNick() + " in the room");
                    //We warn the user that he has been expelled
                    tmp.submit("Success confirmed: You have been permitted and transferred to room: "+ room.getRoom_name()+ " from admin of the chat user: " + nick);
                    Log.log(nick + " has permitted " + tmp.getNick());
                    break;
                } 
                 }
                }
                 else {
                	submit("Error confirmed: You are not the Admin!!! Action denied");
                }  
        } 
        }
        else if (s.startsWith("/LIST")||s.startsWith("/list")) { //Request for a list of available rooms
            //We obtain an array of strings with the names of the rooms
            Rooms[] roomArray = Server.getRooms();
            //We send the information of the rooms to the user who makes the request
            submit("===========================");
            submit("Rooms available: " + roomArray.length);
            for (Rooms room1 : roomArray) {
                submit(room1.getRoom_name() + " - Users: " + room1.getCountUsers() + ((room1.hasPassword())?" (with password)":""));
                }
            
            submit("===========================");
            for (Rooms room1 : roomArray) {
                submit(room1.getRoom_name() + " - Users: " + room1.getCountUsers() + ((room1.hasPassword())?" (with password)":""));
                for(int i=0;i<room1.getUsers().size();i++){
                submit(room1.getUsers().get(i).getNick());
                }
            }
            submit("===========================");
            
        } 
         else { //Received a normal text message
            if (s.length() < 140) {
                //We spread the message received to all users of the room
                room.spread(nick + ": " + s);
                Log.log("Received "+ nick +" message in the room "+ room.getRoom_name() +". Content: " + s);
            } else {
                Log.log("Received too long message from " + nick);
            }
        }
    }
    
    public void sendListUsers() {
        StringBuilder strb = new StringBuilder();
        strb.append("LIST ");
        for (Users usr : room.getUsers()) {
            strb.append(usr.getNick());
            strb.append(" ");
        }
        submit(strb.toString());
    }
    
    public void EraseRoomAdmin(){
        for (Users usr : room.getUsers()) {
            usr.setSuperUser(false);
        }
    }
    
    
    
    public void submit(String s) {
        try {
        	output.write(s + "\n");
        	output.flush();
        } catch (IOException ex) {
            Logger.getLogger(Users.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String toReceive() {
        String s = "";
        try {
            s = input.readLine();
        } catch (Exception ex) {}
        return s;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public long getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(long loginTime) {
        this.loginTime = loginTime;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public long getPing() {
        return ping;
    }

    public void setPing(long ping) {
        this.ping = ping;
    }

    public boolean isSuperUser() {
        return superUser;
    }

    public void setSuperUser(boolean superUser) {
        this.superUser = superUser;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    private void asyncBeatCheck() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //We continue checking that the user is connected while it is still connected
                while (connected) {
                    //We wait 10 second
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException ex) {}
                    //We check that no more than 20 seconds have passed since the last heartbeat was sent
                    if (System.currentTimeMillis() - lastBeat >= 20000) {
                        //We disconnect the user due to inactivity
                    	submit("Disconnection confirmed: Disconnected due to inactivity");
                    	//connected = false;
                        Log.log(nick + " has been disconnected due to inactivity");                
                    }
                }
            }
        }).start();
    }

    public Rooms getRoom() {
        return room;
    }

    public void setRoom(Rooms room) {
        this.room = room;
    }
    
}

