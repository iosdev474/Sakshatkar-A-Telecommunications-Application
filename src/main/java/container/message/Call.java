package container.message;

import lombok.Getter;
import lombok.Setter;

/**
 * <code>Container</code> used to maintain a single <code>Call</code>>.
 */
@Getter
@Setter
public class Call {
    private String callerUsername;
    private String calleeUsername;
    private String date;
    private String typeOfCall;

    /**
     * <code>Parameterised constructor</code>
     *
     * @param callerUsername Caller's username
     * @param calleeUsername Callee's username
     * @param date           Date on which call is placed
     * @param typeOfCall     Type of call like audio/video
     */
    public Call(String callerUsername, String calleeUsername, String date, String typeOfCall) {
        this.callerUsername = callerUsername;
        this.calleeUsername = calleeUsername;
        this.date = date;
        this.typeOfCall = typeOfCall;
    }

    @Override
    public String toString(){
        return date+":"+typeOfCall+":"+calleeUsername;
    }
}