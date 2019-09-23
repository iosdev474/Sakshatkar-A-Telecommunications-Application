package container.profile;

import container.message.Message;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * <code>Container</code> used to hold a <code>Profile</code> of a <code>User</code>
 */
@Getter
@Setter
public class Profile extends User {
    private String password;
    private List<Message> messages;

    public Profile() {
        messages = new ArrayList<Message>();
    }

    /**
     * <code>Parameterised constructor</code>
     *
     * @param password Hashed password of <code>User</code>
     * @param messages Messages of <code>User</code>
     */
    public Profile(String password, List<Message> messages, User user) {
        this();
        this.password = password;
        this.messages = messages;
        setUsername(user.getUsername());
        setEmail(user.getEmail());
        setName(user.getName());
        setStatus(user.getStatus());
        setPhoto(user.getPhoto());
        setCompany(user.getCompany());
        setContactEmail(user.getContactEmail());
        setContactPhoneNumber(user.getContactPhoneNumber());
        setFriends(user.getFriends());
        setIp(user.getIp());
        setPort(user.getPort());
        setLastSeen(user.getLastSeen());
        setHa(user.getHa());
        setCv(user.getCv());
    }



}
