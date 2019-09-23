package container.message;

import lombok.Getter;
import lombok.Setter;

/**
 * <code>Container</code> used to hold a single <code>Notification</code>
 */
@Getter
@Setter
public class Notification {
    private long time;
    private String message;

    /**
     * <code>Parameterised constructor</code>
     *
     * @param time    Time in UTC when notification is generated
     * @param message Notification message content
     */
    public Notification(long time, String message) {
        this.time = time == -1 ? System.currentTimeMillis() : time;
        this.message = message;
    }

    public Notification(){}

    @Override
    public String toString(){
        return "["+time+"] "+message;
    }
}
