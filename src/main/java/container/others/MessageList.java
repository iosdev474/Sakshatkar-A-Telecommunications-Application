package container.others;

import container.message.Message;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * <code>Container</code> used to hold a list of <code>Message</code>
 */
@Getter
@Setter
public class MessageList {
    private List<Message> chats;

    /**
     * <code>Default constructor</code>
     */
    public MessageList() {
        chats = new ArrayList<Message>();
    }

    /**
     * <code>Parameterised constructor</code>
     *
     * @param chats
     */
    public MessageList(List<Message> chats) {
        this();
        this.chats = chats;
    }
}
