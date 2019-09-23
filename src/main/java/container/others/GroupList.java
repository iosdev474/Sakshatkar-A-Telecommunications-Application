package container.others;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * <code>Container</code> used to hold a list of <code>Group</code>
 */
@Getter
@Setter
public class GroupList {
    private List<Group> mygroups;

    /**
     * <code>Default constructor</code>
     */
    public GroupList() {
        mygroups = new ArrayList<Group>();
    }

    /**
     * <code>Parameterised constructor</code>
     *
     * @param mygroups List of <code>Group</code>
     */
    public GroupList(List<Group> mygroups) {
        this();
        this.mygroups = mygroups;
    }
}
