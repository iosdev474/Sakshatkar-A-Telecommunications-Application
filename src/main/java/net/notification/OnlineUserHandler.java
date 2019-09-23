package net.notification;

public class OnlineUserHandler {

    private static OnlineUserHandler onlineUserHandler;

    private OnlineUserHandler(){

    }

    public static OnlineUserHandler getOnlineUserHandler(){
        if(onlineUserHandler == null) {
            onlineUserHandler = new OnlineUserHandler();
        }
        return onlineUserHandler;
    }
}
