package chatsystem;



import java.util.ArrayList;
import java.util.ConcurrentModificationException;

public class Rooms {
    private ArrayList<Users> users;
    private String roomName;
    private String password;
    private ArrayList<Users> ban;
    private String admin_pass="admin";
    
    public Rooms(String roomName) {
        this.roomName = roomName;
        this.password = "";
        this.users = new ArrayList<>();
        this.ban = new ArrayList<>();
    }
    
    public Rooms(String roomName, String password) {
        this.roomName = roomName;
        this.password = password;
        this.users = new ArrayList<>();
        this.ban = new ArrayList<>();
    }
    
    public Rooms(String roomName,String password,String admin_pass) {
        this.roomName = roomName;
        this.password = password;
        this.users = new ArrayList<>();
        this.ban = new ArrayList<>();
        this.admin_pass = admin_pass;
    }
    
    public String enter(Users u) {
        //Check that the user does not exist in the room
        if (!userExists(u)) {
            //Check that the user is not banned from the room
            if (!isBanned(u)) {
                //Connect the user
                u.setConnected(true);
                //We add the user to the user list of the room
                users.add(u);
                //We spread the user's notice message to all users of the room
                spread(u.getNick() + " entered the room " + this.roomName);
                //We send the updated user list to all users of the room
                updateListedUsers();
                Log.log(u.getNick() + " entered the room " + this.roomName);
                //We send the name of the room to the user
                u.submit("Room: " + this.roomName);
                return "Success confirmed:";
            } else {
                //We disconnect the user
                u.setConnected(false);
                return "Disconnection confirmed: You're banned from this room";
            }
        } else {
            //We disconnect the users
            u.setConnected(false);
            return " The user is already in the room";
        }
    }
    
    public String enter(Users u, String password) {
        //Check that the user does not exist in the room
        if (!userExists(u)) {
            //Check that the password is correct
            if (password.equals(this.password)) {
                //Check that the user is not banned from the room
                if (!isBanned(u)) {
                    //Connect the user
                    u.setConnected(true);
                    //We add the user to the user list of the room
                    users.add(u);
                    //We spread the user's notice message to all users of the room
                    spread(u.getNick() + " entered the room " + this.roomName);
                    //We send the updated user list to all users of the room
                    updateListedUsers();
                    Log.log(u.getNick() + " entered the room " + this.roomName);
                    //We send the name of the room to the user
                    u.submit("Room: " + this.roomName);
                    return "Success confirmed:";
                } else {
                    //We disconnect the user
                    u.setConnected(false);
                    return "Disconnection confirmed: You're banned from this room";
                }
            } else {
                //We send a password error to the user
                return "Error confirmed: The password of the room is incorrect";
            }
        } else {
            //We disconnect the user
            u.setConnected(false);
            return "Disconnection confirmed: The user is already in the room";
        }
    }
    
    public boolean hasPassword() {
        return !this.password.isEmpty();
    }
    
    public void getOut(Users u) {
        //If the user exists, we leave the room
        if (userExists(u)) {
            //We remove it from the list of users of the room
        	users.remove(u);
            //We spread the exit message to all members of the room
        	spread(u.getNick() + " to come out of the room " + this.roomName);
            //We send the updated list of users to all users of the room
            updateListedUsers();
            Log.log(u.getNick() + " to come out of the room " + this.roomName);
        }
    }
    
    public boolean userExists(Users u) {
        for (Users usr : users) {
            if (usr.getNick().equalsIgnoreCase(u.getNick())) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isBanned(Users u) {
        for (Users usr : ban) {
            if (usr.getNick().equals(u.getNick()) || usr.getIP().equals(u.getIP())) {
                return true;
            }
        }
        return false;
    }
    
    public void spread(String message) {
        for (Users usr : users) {
            usr.submit(message);
        }
    }
    
    public Users getUser(String nick) {
        for (Users usr : users) {
            if (usr.getNick().equalsIgnoreCase(nick)) {
                return usr;
            }
        }
        return null;
    }
    
    public void addBan(Users u) {
    	ban.add(u);
    }
    
    public void removeBan(String usr) {
        for (int i = 0; i < ban.size(); i++) {
            if (ban.get(i).getNick().equals(usr)) {
            	ban.remove(i);
                break;
            }
        }
    }
    
    //method to send private message to a specific user
    public void sendPrivateMessage(Users de, Users re, String message) {
        //Show the message to the sender
        de.submit("(private)" + de.getNick() + ": " + message);
        //Show the message to the recipient
        re.submit("(private)" + de.getNick() + ": " + message);
    }
    
    public void moveRoom(Rooms destination) {
        //Move all users from one room to another (for example when a room is deleted)
        try {
            //We visit the users of the room
            for (int i = users.size()-1; i >= 0; i--) {
                //We change the room where you should go
            	users.get(0).setRoom(destination);
                //Enter the destination room
            	destination.enter(users.get(0));
                //We disconnect the user
                getOut(users.get(0));
            }
            //We send the updated list of users to the whole room after finishing moving all the users
            destination.updateListedUsers();
        } catch (ConcurrentModificationException ex) {}
        finally {updateListedUsers();}
        
    }
   
   
    
    

    public String getRoom_name() {
        return roomName;
    }

    public void setRoom_name(String roomName) {
        this.roomName = roomName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public int getCountUsers() {
        return users.size();
    }

    public ArrayList<Users> getUsers() {
        return users;
    }

    public void updateListedUsers() {
        for (Users usr : users) {
            usr.sendListUsers();
        }
    }
    
     public String getAdminPass(){
        return admin_pass;
    }
    
    public void setAdminPass(String s){
        admin_pass=s;
    }

	
}
