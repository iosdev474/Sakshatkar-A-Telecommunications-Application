package container.profile;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * <code>Container</code> used to hold imformation of <code>User</code>
 */
@Getter
@Setter
public class User implements Serializable {
    private String username;
    private String email;
    private String name;
    private String status;
    private String photo;
    private String company;
    private String contactEmail;
    private String contactPhoneNumber;
    private List<String> friends;
    private String ip;
    private int port;
    private String ha;
    private String cv;
    private long lastSeen = System.currentTimeMillis();

    @Override
    public String toString(){
        return username;
    }


}
