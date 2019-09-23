package server;

import container.others.Feedback;
import container.profile.Profile;
import lombok.extern.slf4j.Slf4j;

import java.awt.image.BufferedImage;
import java.util.HashMap;

@Slf4j
public class Server {
    private static Server server;
    int capacitySize = 100;
    int maxPerGroup = 4;
    HashMap<Integer ,ServerHandler> serverHandlers = new HashMap<Integer, ServerHandler>();

    private Server() {
        server = this;
    }
    public static Server getServer() {
        return server==null?(server=new Server()):(server);
    }
    public int createChannel() {
        for(int port = 3000; port<3000+ capacitySize; port+=port*maxPerGroup*2) {
            if(!serverHandlers.containsKey(port)) {
                ServerHandler serverHandler = new ServerHandler(port);
                serverHandlers.put(port,serverHandler);
                log.info("Server created at {}", port);
                return port;
            }
        }
        log.error("Unable to create server. Server FULL");
        return -1;
    }
    public void removeChannel(int port) {
        serverHandlers.get(port).stop();
        serverHandlers.remove(port);
        log.info("ServerHandler with port {} removed", port);
    }

    boolean auth (String email, String password) {
        //email/username
        return false;
    }

    boolean Register (Profile profile) {
        //register info
        return false;
    }

    Profile[] searchUser(String searchParameters) {
        return null;
    }

    Profile getUserDetails() {
        return null;
    }

    boolean sendChat(String groupID, String data) {
        return false;
    }

    boolean sendImage(String groupID, BufferedImage image) {
        return false;
    }

    boolean sendFeedback(Feedback feedback) {
        return false;
    }

    int createVideoCall() {
        return -1;
    }


    boolean inviteClientToVideo(String email) {
        return false;
    }

    boolean getNotification() {
        return false;
    }

    boolean getNewMessages() {
        return false;
    }

    boolean sendFriendRequest(String clientEmail) {
        return false;
    }

    boolean setStatus(String status) {
        return false;
    }

    Profile[] getFriendSuggestions() {
        return null;
    }

    int createGroup() {
        return -1;
    }

    boolean joinGroup(int groupID) {
        return false;
    }

    boolean inviteFriends(String clientEmail) {
        return inviteClient(clientEmail);
    }

    boolean inviteClient(String clientEmail) {
        return false;
    }

    public static void main(String[] args) {
        Server server = new Server();
    }
}
