package container.others;

import lombok.Getter;
import lombok.Setter;

/**
 * <code>Container</code> used to hold a single <code>Group</code> information
 */
@Getter
@Setter
public class Group {
    private int gID;
    private String owner;

    /**
     * <code>Parameterised constructor</code>
     *
     * @param gID Group ID (Primary key in <code>database</code>)
     * @param owner <code>User</code>'s username who created this group
     */
    public Group(int gID, String owner){
        this.gID=gID;
        this.owner=owner;
    }

    public Group(){}

    @Override
    public String toString(){
        return gID+":"+owner;
    }
}
