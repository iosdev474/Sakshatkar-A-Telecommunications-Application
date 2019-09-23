package net.manager;

import com.google.gson.Gson;
import container.message.*;
import container.others.*;
import container.profile.Profile;
import container.profile.ProfilePicture;
import container.profile.User;
import database.manager.DatabaseManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import main.Configuration;
import main.DAO;
import net.Packet;
import net.video.VideoManager;
import org.apache.commons.lang3.RandomStringUtils;
import server.Server;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.List;

/**
 * <code>NetManager</code> manages all networking
 * Here Client/Server sockets are utilised and managed here only
 * and an NetManager is provided to all classes as a interface
 * which can be used to utilize to send data and handle on receive data
 * <p>
 * It is a singleton class so only a single object is used throughout
 * the software
 * <p>
 * It manages multiple clients with threads automatically
 * <p>
 * Act as a abstract layer handling multiple threads, sockets, server-socket, etc
 * and is implemented similar to working of HTTP calls
 *
 * <h3>Heart of our software :3</h3>
 */
@Slf4j
public class NetManager implements INetManager, Runnable {
    private ServerSocket serverSocket;                                      // single server socket used to recieve data from outside
    private int port;                                                       // port number for server socket
    private static NetManager netManager;                                   // singleton object so private
    private static List<Thread> myThreads = new ArrayList<Thread>();             //manage threads automatically
    @Getter
    private Map<Integer, VideoManager> videoManagers = new HashMap<Integer, VideoManager>();    //video manager used for video server

    /**
     * Singleton object so to avoid creation of object manually
     */
    private NetManager() {
        //port = Configuration.getConfig().serverPort;
        //todo remove this gand
        System.out.println("Enter a port:");
        Scanner scanner = new Scanner(System.in);
        port = scanner.nextInt();
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            log.error("Unable to start server socket at port " + port);
            e.printStackTrace();
            System.exit(4);             //Error unable to create heart :p
        }
        log.info("Server Initialized");
        Thread thread = new Thread(this);
        myThreads.add(thread);
        thread.start();
    }

    /**
     * Start <code>NetManager</code>
     */
    public static void start() {
        getNetManager();
    }

    /**
     * getter method for <code>NetManager</code>>'s singleton object
     *
     * @return singleton object of <code>NetManager</code>
     */
    public static NetManager getNetManager() {
        return netManager == null ? (netManager = new NetManager()) : (netManager);
    }

    @Override
    public void ping() {
        throw new NullPointerException("NOT IMPLEMENTED");
    }

    @Override
    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public int getPort(){
        return port;
    }

    @Override
    // Handle data received from client
    public void onReceive(Socket clientSocket) {
        Thread thread = new Thread(this);
        myThreads.add(thread);
        thread.start();
        String httpResponse = "";
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String request = "";
            while (true) {
                int temp = reader.read();
                if (temp == (int) '\0') {
                    break;
                }
                request += (char) temp;
            }
            log.info("Request Json {}", request);
            Gson gson = new Gson();
            Packet requestPacket = gson.fromJson(request, Packet.class);
            Packet responsePacket = new Packet();


            if (requestPacket.call.equals("Server.createChannel")) {
                int response = Server.getServer().createChannel();
                if (response == -1) {
                    responsePacket.call = "Return.Fail";
                    responsePacket.data = "Unable to create server. Server full";
                } else {
                    responsePacket.call = "Return.Success";
                    responsePacket.data = gson.toJson(response);
                }
            } else if (requestPacket.call.equals("Server.removeChannel")) {
                Integer port = gson.fromJson(requestPacket.data, Integer.class);
                Server.getServer().removeChannel(port);
                responsePacket.call = "Return.Success";
                responsePacket.data = "Server Channel removed";
            } else if (requestPacket.call.equals("Server.auth")) {
                Login login = gson.fromJson(requestPacket.data, Login.class);
                User response = DatabaseManager.getDatabaseManager().auth(login);
                if (response == null) {
                    responsePacket.call = "Return.Fail";
                } else {
                    responsePacket.call = "Return.Success";
                }
                responsePacket.data = gson.toJson(response);
            } else if (requestPacket.call.equals("Server.register")) {
                Profile user = gson.fromJson(requestPacket.data, Profile.class);
                boolean response = DatabaseManager.getDatabaseManager().register(user);
                responsePacket.data = gson.toJson(response);
            } else if (requestPacket.call.equals("Server.search")) {
                String searchQuery = gson.fromJson(requestPacket.data, String.class);
                List<User> response = DatabaseManager.getDatabaseManager().search(searchQuery);
                responsePacket.call = "Return.Success";
                responsePacket.data = gson.toJson(new SearchResult(response));
            }

            /// Video Server

            else if (requestPacket.call.equals("Server.createVideoServer")) {
                VideoManager videoManager = new VideoManager(getEmptyPort());
                videoManagers.put(videoManager.port, videoManager);
                VideoCallConfig callConfig = new VideoCallConfig(videoManager.getPassword(),"",videoManager.port);
                responsePacket.call = "Return.Success";
                responsePacket.data = gson.toJson(callConfig);
            } else if (requestPacket.call.equals("Server.isVideoServerRunning")) {
                VideoCallConfig videoCallConfig = gson.fromJson(requestPacket.data, VideoCallConfig.class);
                VideoManager videoManager = videoManagers.get(videoCallConfig.getPort());
                if (videoManager == null)
                    responsePacket.data = gson.toJson(false, Boolean.class);
                else {
                    if (videoManager.checkPassword(videoCallConfig.getPassword()))
                        responsePacket.data = gson.toJson(true, Boolean.class);
                    else
                        responsePacket.data = gson.toJson(false, Boolean.class);
                }
                responsePacket.call = "Return.Success";
            }

            /// Group and Chat

            else if (requestPacket.call.equals("Database.getMyGroups")) {
                String username = gson.fromJson(requestPacket.data, String.class);
                List<Group> groups = DatabaseManager.getDatabaseManager().getMyGroups(username);
                responsePacket.call = "Return.Success";
                responsePacket.data = gson.toJson(new GroupList(groups), GroupList.class);
            } else if (requestPacket.call.equals("Database.getChat")) {
                Group group = gson.fromJson(requestPacket.data, Group.class);
                List<Message> messages = DatabaseManager.getDatabaseManager().getallmessages(group);
                responsePacket.call = "Return.Success";
                responsePacket.data = gson.toJson(new MessageList(messages), MessageList.class);
            } else if (requestPacket.call.equals("Server.sendMessage")) {
                try {
                    Message message = gson.fromJson(requestPacket.data, Message.class);
                    DatabaseManager.getDatabaseManager().sendmessage(message);
                    responsePacket.call = "Return.Success";
                    responsePacket.data = gson.toJson(true, Boolean.class);
                } catch (Exception e) {
                    responsePacket.call = "Return.Error";
                    log.error("Unable to send message");
                }
            } else if (requestPacket.call.equals("Database.newGroup")) {
                Group group = gson.fromJson(requestPacket.data, Group.class);
                try {
                    group = DatabaseManager.getDatabaseManager().createNewGroup(group);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                responsePacket.call = "Return.Success";
                responsePacket.data = gson.toJson(group, Group.class);
            } else if (requestPacket.call.equals("Database.joinGroup")) {
                try {
                    GroupInvite groupInvite = gson.fromJson(requestPacket.data, GroupInvite.class);
                    DatabaseManager.getDatabaseManager().joingroup(groupInvite.getGroup(), groupInvite.getUser().getUsername());
                    responsePacket.call = "Return.Success";
                    responsePacket.data = "";
                } catch (Exception e) {
                    responsePacket.call = "Return.Error";
                    log.error("Unable to join group");
                }
            } else if (requestPacket.call.equals("Server.addFriendRequest")) {
                try {
                    FriendRequest friendRequest = gson.fromJson(requestPacket.data, FriendRequest.class);
                    DatabaseManager.getDatabaseManager().addFriendRequest(friendRequest);
                    responsePacket.call = "Return.Success";
                    responsePacket.data = "";
                } catch (Exception e) {
                    log.error("Unable to add friend request");
                    responsePacket.call = "Return.Error";
                    responsePacket.data = "";
                }
            } else if (requestPacket.call.equals("Server.acceptFriendRequest")) {
                try {
                    FriendRequest friendRequest = gson.fromJson(requestPacket.data, FriendRequest.class);
                    DatabaseManager.getDatabaseManager().confirmFriendRequest(friendRequest);
                    responsePacket.call = "Return.Success";
                    responsePacket.data = "";
                } catch (Exception e) {
                    responsePacket.call = "Return.Error";
                    log.error("Unable to accept friend request");
                }
            } else if (requestPacket.call.equals("Database.getMyFriends")) {
                try {
                    String username = gson.fromJson(requestPacket.data, String.class);
                    SearchResult searchResult = new SearchResult(DatabaseManager.getDatabaseManager().getFriends(username));
                    responsePacket.call = "Return.Success";
                    responsePacket.data = gson.toJson(searchResult);
                } catch (Exception e) {
                    responsePacket.call = "Return.Error";
                    log.error("Unable to get my friend");
                }
            } else if (requestPacket.call.equals("Database.updateUser")) {
                try {
                    User profile = gson.fromJson(requestPacket.data, User.class);
                    DatabaseManager.getDatabaseManager().updateUser(profile);
                    responsePacket.call = "Return.Success";
                    responsePacket.data = "";
                } catch (Exception e) {
                    responsePacket.call = "Return.Error";
                    log.error("Unable to update profile");
                }
            } else if (requestPacket.call.equals("Database.isValidFriendRequest")) {
                try {
                    FriendRequest friendRequest = gson.fromJson(requestPacket.data, FriendRequest.class);
                    boolean result = DatabaseManager.getDatabaseManager().isValidFriendRequest(friendRequest);
                    responsePacket.call = "Return.Success";
                    responsePacket.data = gson.toJson(result);
                } catch (Exception e) {
                    responsePacket.call = "Return.Error";
                    log.error("Unable to get my friend");
                }
            } else if (requestPacket.call.equals("Database.getNotification")) {
                String username = gson.fromJson(requestPacket.data, String.class);
                NotificationList notificationList = new NotificationList();
                notificationList.setNotifications(DatabaseManager.getDatabaseManager().getNotifications(username));
                responsePacket.call = "Return.Success";
                responsePacket.data = gson.toJson(notificationList);
            } else if (requestPacket.call.equals("Database.feedback")) {
                try {
                    Feedback feedback = gson.fromJson(requestPacket.data, Feedback.class);
                    DatabaseManager.getDatabaseManager().addFeedback(feedback);
                    responsePacket.call = "Return.Success";
                    responsePacket.data = "";
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (requestPacket.call.equals("Server.uploadImage")) {
                ProfilePicture profilePicture = gson.fromJson(requestPacket.data, ProfilePicture.class);
                byte[] data = profilePicture.getProfileBufferedImageArray();
                ByteArrayInputStream bis = new ByteArrayInputStream(data);
                BufferedImage bImage2 = ImageIO.read(bis);
                String fileName = RandomStringUtils.randomAlphabetic(10);
                ImageIO.write(bImage2, "jpg", new File(System.getProperty("user.dir") + "\\" + fileName));
                responsePacket.call = "Return.Success";
                responsePacket.data = fileName.replace("\\", "/");
                log.info("Store image at {}", System.getProperty("user.dir") + "\\" + fileName);
            } else if (requestPacket.call.equals("Server.uploadFile")) {
                UploadFile uploadedFile = gson.fromJson(requestPacket.data, UploadFile.class);
                uploadedFile.getFile();
                String fileName = RandomStringUtils.randomAlphabetic(10);
                OutputStream out = new FileOutputStream(new File(DAO.HTTPServerPath + "\\" + fileName));
                out.write(uploadedFile.getFile());
                out.close();
                responsePacket.call = "Return.Success";
                responsePacket.data = fileName.replace("\\", "/");
                log.info("Store File at {}", System.getProperty("user.dir") + "\\" + fileName);
            } else if (requestPacket.call.equals("User.notification")) {
                DAO.notification();
            } else if (requestPacket.call.equals("User.chatNotify")) {
                ChatNotification chatNotification = gson.fromJson(requestPacket.data, ChatNotification.class);
                DAO.chatNotify(chatNotification.getGroupID(), chatNotification.getMessage());
            } else if (requestPacket.call.equals("Database.setStatus")) {
                try {
                    User user = gson.fromJson(requestPacket.data, User.class);
                    DatabaseManager.getDatabaseManager().setStatus(user.getUsername(), user.getStatus());
                    responsePacket.call = "Return.Success";
                    responsePacket.data = "";
                } catch (Exception e) {
                    log.error("Unable to set status for {}", requestPacket.data);
                }
            } else if (requestPacket.call.equals("Database.setLastSeen")) {
                try {
                    User user = gson.fromJson(requestPacket.data, User.class);
                    DatabaseManager.getDatabaseManager().setLastSeen(user.getUsername(), user.getLastSeen());
                    DatabaseManager.getDatabaseManager().setStatus(user.getUsername(), user.getStatus());
                    responsePacket.call = "Return.Success";
                    responsePacket.data = "";
                } catch (Exception e) {
                    log.error("Unable to set last seen for {}", requestPacket.data);
                }
            } else if (requestPacket.call.equals("Server.getSuggestions")) {
                String searchQuery = gson.fromJson(requestPacket.data, String.class);
                List<User> response = DatabaseManager.getDatabaseManager().getFriendsOfFriend(searchQuery, 15);
                responsePacket.call = "Return.Success";
                responsePacket.data = gson.toJson(new SearchResult(response));
            } else if (requestPacket.call.equals("Server.getCallHistory")) {
                String username = gson.fromJson(requestPacket.data, String.class);
                List<String> response = DatabaseManager.getDatabaseManager().getCallHistory(username);
                responsePacket.call = "Return.Success";
                responsePacket.data = gson.toJson(new CallHistory(response));
            } else if (requestPacket.call.equals("Server.addCallHistory")) {
                CallAddHistory callAddHistory = gson.fromJson(requestPacket.data, CallAddHistory.class);
                List<String> users = DatabaseManager.getDatabaseManager().getUsersInGroup(callAddHistory.getGID());
                for (String username : users) {
                    try {
                        DatabaseManager.getDatabaseManager().addCallHistory(username, callAddHistory.getCall());
                    } catch (Exception e) {
                        log.error("Unable to call history in {}", username);
                    }
                }
                responsePacket.call = "Return.Success";
                responsePacket.data = "";
            } else {
                log.error("NO CALL FOUND");
                responsePacket.call = "Return.Error";
                responsePacket.data = "No Call Found";
            }

            httpResponse += gson.toJson(responsePacket);
        } catch (IOException e) {
            log.error("Unable to read request");
            Packet responsePacket = new Packet();
            responsePacket.call = "Return.Error";
            responsePacket.data = "";
            httpResponse += new Gson().toJson(responsePacket);
        }
        try {
            log.info("Response Json " + httpResponse + "\r\n\r\n");
            httpResponse += '\0';
//            new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())).write(httpResponse);
            clientSocket.getOutputStream().write(httpResponse.getBytes("UTF-8"));           //send response
        } catch (IOException e) {
            log.error("Unable to send response");
        }
    }

    private int getEmptyPort() {
        Random random = new Random();
        return random.nextInt(2000) + 3000;
    }

    /**
     * Send requestJson to given IP and Port
     *
     * @param requestJson JsonData to send
     * @param ip          IP to where Data is to be send
     * @param port        Port number to where data is to be send
     * @return Response string (Json)
     * @throws IOException When server doesn't responds or when unable to send requestJson
     */
    public String send(String requestJson, String ip, int port) throws IOException {
        log.info("Send data at {} {} {}", ip, port, requestJson);
        Socket socket = new Socket(ip, port);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        writer.write(requestJson + '\0');
        writer.flush();
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String responseJson = "";
        while (true) {
            int temp = reader.read();
            if (temp == (int) '\0') {
                break;
            }
            responseJson += (char) temp;
        }
        return responseJson;
    }

    /**
     * To stop netManager
     * When not stopped it starts running as Demon thread
     * Don't stop when you need to run it as Deamon thread
     */
    public void stop() {
        log.info("Stop Netmanager");
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            log.info("Waiting for client");
            Socket clientSocket = serverSocket.accept();
            log.info("Client connected " + clientSocket.getInetAddress());
            onReceive(clientSocket);
        } catch (IOException e) {
            log.error("Unable to accept socket EXITING");
        }
    }

    public static void main(String[] args) {
        try {
            Runtime.getRuntime().exec("python -m http.server " + DAO.HTTPServerPort + " -d " + DAO.HTTPServerPath);
        } catch (IOException e) {
            log.error("Unable to start python server");
        }
        NetManager.getNetManager();
    }
}

/*

String httpResponse = null;
            try {
                httpResponse = "HTTP/1.1 200 OK\r\n\r\n" + onRecieve(clientSocket);
            } catch (IOException e) {
                httpResponse = "HTTP/1.1 500 Internal Server Error\r\n\r\n";
                log.error("Unable to read request");
//                e.printStackTrace();
            }
            try {
                clientSocket.getOutputStream().write(httpResponse.getBytes("UTF-8"));
            } catch (IOException e) {
                log.error("Unable to send response");
//                e.printStackTrace();
            }


 */

////// search torrent file by name tags extension author or hash sorting based on extension and availability