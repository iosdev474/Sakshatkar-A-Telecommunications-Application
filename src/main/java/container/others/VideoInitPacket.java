package container.others;

import container.profile.User;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * <code>Container</code> used initiate/reconnect a video call
 */
@Getter
@Setter
public class VideoInitPacket implements Serializable {
    private int id;
    private int port;
    private User user;
    private String query;

    /**
     * <code>Parameterised constructor</code>
     *
     * @param id    id of <code>User</code>> on a video call server
     * @param port  port number where <code>User</code> has to send his data
     * @param user  <code>User</code> whom this packet belongs to
     * @param query Query used to define what this packet means like join/reconnect call
     */
    public VideoInitPacket(int id, int port, User user, String query) {
        this.id = id;
        this.port = port;
        this.user = user;
        this.query = query;
    }
}
