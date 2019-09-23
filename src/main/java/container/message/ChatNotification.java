package container.message;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatNotification {
    private int groupID;
    private Message message;

    public ChatNotification(int groupID, Message message){
        this.groupID=groupID;
        this.message=message;
    }
}
