package net.notification;

public class NotificationHandler {

    private static NotificationHandler notificationHandler;

    private NotificationHandler(){

    }

    public static NotificationHandler getNotificationHandler(){
        if(notificationHandler == null){
            notificationHandler = new NotificationHandler();
        }
        return notificationHandler;
    }
}
