package mg.didavid.firsttry.Models;

public class ModeleChatroom {
    String other_user_id, other_user_name, room_id, last_message, last_message_timestamp;

    public ModeleChatroom() {
    }

    public ModeleChatroom(String other_user_id, String other_user_name, String room_id, String last_message, String last_message_timestamp) {
        this.other_user_id = other_user_id;
        this.other_user_name = other_user_name;
        this.room_id = room_id;
        this.last_message = last_message;
        this.last_message_timestamp = last_message_timestamp;
    }

    public String getOther_user_id() {
        return other_user_id;
    }

    public void setOther_user_id(String other_user_id) {
        this.other_user_id = other_user_id;
    }

    public String getOther_user_name() {
        return other_user_name;
    }

    public void setOther_user_name(String other_user_name) {
        this.other_user_name = other_user_name;
    }

    public String getRoom_id() {
        return room_id;
    }

    public void setRoom_id(String room_id) {
        this.room_id = room_id;
    }

    public String getLast_message() {
        return last_message;
    }

    public void setLast_message(String last_message) {
        this.last_message = last_message;
    }

    public String getLast_message_timestamp() {
        return last_message_timestamp;
    }

    public void setLast_message_timestamp(String last_message_timestamp) {
        this.last_message_timestamp = last_message_timestamp;
    }
}
