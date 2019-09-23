package container.others;

import container.message.Notification;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * <code>Container</code> used to hold a list of <code>Notification</code>
 */
@Getter
@Setter
public class NotificationList {
    private List<Notification> notifications;

    /**
     * <code>Default constructor</code>
     */
    public NotificationList() {
        notifications = new ArrayList<Notification>();
    }

    /**
     * <code>Parameterised constructor</code>
     *
     * @param notifications List of <code>Notification</code>
     */
    public NotificationList(List<Notification> notifications) {
        this();
        this.notifications = notifications;
    }
}
