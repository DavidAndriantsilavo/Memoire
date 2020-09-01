package mg.didavid.firsttry.Models;

public class Message {
    String sender_id, content, timestamp;

    public Message(String sender_id, String content, String timestamp) {
        this.sender_id = sender_id;
        this.content = content;
        this.timestamp = timestamp;
    }

    public Message() {
    }

    public String getSender_id() {
        return sender_id;
    }

    public void setSender_id(String sender_id) {
        this.sender_id = sender_id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
