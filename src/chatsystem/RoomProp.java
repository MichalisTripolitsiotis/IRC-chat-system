

package chatsystem;
import java.util.ArrayList;

public class RoomProp {
    
    private final ArrayList<UserProp> users;
    private String roomName;
   
    
    public RoomProp(String roomName) {
    	users = new ArrayList<>();
        this.roomName = roomName;
    }


    public void addUser(UserProp usr) {
    	users.add(usr);
    }
    
    public void deleteUser(UserProp usr) {
    	users.remove(usr);
    }
    
    public int getUsers() {
        return users.size();
    }

    public String getRoom_name() {
        return roomName;
    }

    public void setRoom_name(String roomName) {
        this.roomName = roomName;
    }
    
    

}
