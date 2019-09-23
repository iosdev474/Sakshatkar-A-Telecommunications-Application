package container.message;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * <code>Container</code> used to hold a single <code>Message</code>>
 */
@Getter
@Setter
public class Message {
    private int gID;
    private String senderUsername;
    private long time;
    private String date;
    private String message;

    /**
     * <code>Parameterised constructor</code>
     *
     * @param gID            Group ID, primary key in database
     * @param senderUsername Message sender username
     * @param time           Time in UTC when message is created
     * @param date           Date when message is created
     * @param message        Message, can also contain url
     */
    public Message(int gID, String senderUsername, long time, String date, String message) {
        this.gID = gID;
        this.senderUsername = senderUsername;
        this.time = time == -1 ? System.currentTimeMillis() : time;
        this.date = date;
        this.message = message;
    }

    public Message(){}

    @Override
    public String toString(){
        return "["+date+"] ["+senderUsername+"]   "+message;
    }
}
