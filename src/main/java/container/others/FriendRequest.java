package container.others;

import lombok.Getter;
import lombok.Setter;

/**
 * <code>Container</code> used to hold a <code>Friend Request</code>
 */
@Getter
@Setter
public class FriendRequest {
    private String sendByUserEmail;
    private String recieveUserEmail;
    private String status;

    /**
     * <code>Parameterised Constructor</code>
     *
     * @param sender   <code>User</code>'s username which generated <code>FriendRequest</code>
     * @param reciever <code>User</code>'s username which was invited to be friend of <code>sender</cod
     * @param status Status of friend request ("Accepted","Pending")
     */
    public FriendRequest(String sender, String reciever, String status) {
        sendByUserEmail = sender;
        recieveUserEmail = reciever;
        this.status = status;
    }
    public FriendRequest(){

    }
}