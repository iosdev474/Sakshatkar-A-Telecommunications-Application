package container.others;

import lombok.Getter;
import lombok.Setter;

/**
 * <code>Container</code> used to hold <code>Login</code> request
 */
@Getter
@Setter
public class Login {
    private String username;
    private String password;

    /**
     * <code>Parameterised constructor</code>
     *
     * @param username
     * @param password
     */
    public Login(String username, String password) {
        this.username = username;
        this.password = password;
    }

}
