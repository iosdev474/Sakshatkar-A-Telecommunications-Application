package container.others;

import container.profile.User;
import lombok.Getter;
import lombok.Setter;


/**
 * <code>Container</code> used to hold a <code>GroupInvite</code>
 */
@Getter
@Setter
public class GroupInvite {
    private Group group;
    private User user;

    /**
     * <code>Parameterised constructor</code>
     *
     * @param group <code>Group</code> for which <code>User</code> is invited
     * @param user <code>User</code>'s username which is invited
     */
    public GroupInvite(Group group, User user){
        this.user = user;
        this.group = group;
    }
}
