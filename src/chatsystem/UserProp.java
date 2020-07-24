

package chatsystem;


public class UserProp {
    
    private String nick;
    private RoomProp room;
    
    
    public UserProp(String nick, RoomProp room) {
        this.nick = nick;
        this.room = room;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }
    

    public RoomProp getRoom() {
        return room;
    }

    public void setRoom(RoomProp room) {
        this.room = room;
    }
    
    
    

}
