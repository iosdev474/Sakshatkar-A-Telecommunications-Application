package container.others;

import container.profile.User;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * <code>Container</code> used to hold a list of <code>User</code>
 */
@Getter
@Setter
public class SearchResult {
    private List<User> users;

    /**
     * <code>Default constructor</code>
     */
    public SearchResult() {
        users = new ArrayList<User>();
    }

    /**
     * <code>Parameterised constructor</code>
     *
     * @param users List of <code>User</code>
     */
    public SearchResult(List<User> users) {
        this();
        this.users = users;
    }
}
