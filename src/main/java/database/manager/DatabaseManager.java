package database.manager;

import LRUCache.LRUCache;
import com.google.gson.*;
import container.message.Call;
import container.message.ChatNotification;
import container.message.Message;
import container.message.Notification;
import container.others.Feedback;
import container.others.FriendRequest;
import container.others.Group;
import container.others.Login;
import container.profile.Profile;
import container.profile.User;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.Packet;
import net.manager.NetManager;
import org.python.antlr.ast.Str;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.*;

@Getter
@Setter
@Slf4j
public class DatabaseManager {

    private long timeToWait=500;
    private static DatabaseManager databaseManager;
    private LRUCache<String,User> profileLRUCache;
    private LRUCache<Integer,List<User>> groupLRUCache;
    private LRUCache<Integer,List<Message>> messageLRUCache;
    private LRUCache<String, List<Group>> userGroupsLRUCache;
    private LRUCache<String, List<String>> userCallLogLRUCache;
    private LRUCache<String, List<User>> userFriendsLRUCache;

    public static void main(String[] args) throws Exception {
      DatabaseManager databaseManager = new DatabaseManager();
//        Login login = new Login("kashyap","A");
//        Profile profile = new Profile();
//        profile.setUsername("arsh");
//        profile.setStatus("B");
//        profile.setPort(5);
//        profile.setPhoto("C");
//        profile.setName("D");
//        profile.setLastSeen(147);
//        profile.setEmail("E");
//        profile.setContactEmail("F");
//        profile.setCompany("G");
//        profile.setContactEmail("H");
//        profile.setIp("I");
//        System.out.println(databaseManager.register(profile));
//        Group group = new Group(12,"twin");
//        databaseManager.leavegroup(group);
        System.out.println(databaseManager.getUsersInGroup(12));
//        System.out.println(databaseManager.auth(login).name);
        //System.out.println(databaseManager.searchContactPhone("9").get(0).name);
    }

    private DatabaseManager(){
        profileLRUCache = new LRUCache<String, User>(25);
        groupLRUCache = new LRUCache<Integer, List<User>>(25);
        messageLRUCache = new LRUCache<Integer, List<Message>>(25);
        userGroupsLRUCache = new LRUCache<String, List<Group>>(25);
        userCallLogLRUCache = new LRUCache<String, List<String>>(25);
        userFriendsLRUCache = new LRUCache<String, List<User>>(25);
        log.debug("UserDatabaseManager created");
    }

    private boolean addNotification(Notification notification, User user) throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat formatterDate = new SimpleDateFormat("yyyy/MM/dd");
                Date date = new Date();
                String echoResponse = namePipe("addNotification","mutation {\n" +
                        "  insert_skype_notification(objects: {Time: \""+System.currentTimeMillis()+"\", date: \""+formatterDate.format(date)+"\", msg: \""+notification.getMessage()+"\", username: \""+user.getUsername()+"\"}) {\n" +
                        "    affected_rows\n" +
                        "  }\n" +
                        "}");

                Packet packet = new Packet();
                packet.call = "User.notification";
                packet.data = "";
                Gson gson = new Gson();
                try {
                    NetManager.getNetManager().send(gson.toJson(packet), user.getIp(), user.getPort());
                }catch (IOException e){
                    log.error("Unable tp send notification to user {}",user.getUsername());
                }

            }
        }).start();
        return true;
    }

    public static DatabaseManager getDatabaseManager(){
        return databaseManager ==null? databaseManager =new DatabaseManager(): databaseManager;
    }

    public Profile getProfileDetails(){
        return null;
    }

    private void blockingFunction(Process p) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line="";
        log.debug("Waiting for python");
        while ((line=bufferedReader.readLine())!=null){
            System.out.println(line);
            if(line.equalsIgnoreCase("waiting for client"))
                break;
        }
        log.debug("Done with python");
        //Thread.sleep(timeToWait);
    }

    private String namePipe(String name,String query){
        log.debug(query);
        String echoResponse = null;


        try {
            // Connect to the pipe
            String path = System.getProperty("user.dir")+"\\src\\main\\java\\database\\manager\\python\\"+name+".py";
            log.debug("Path of python script: "+path);
            Process p = Runtime.getRuntime().exec("python "+path);

            blockingFunction(p);

            RandomAccessFile pipe = new RandomAccessFile("\\\\.\\pipe\\"+name, "rw");

            String echoText = query;
            // write to pipe
            pipe.write ( echoText.getBytes() );
            // read response
            echoResponse = pipe.readLine();
            log.debug("DB response {}", echoResponse);
            pipe.close();

        } catch (Exception e) {
            log.debug("name pipe error");
        }

        return echoResponse;
    }

    public User auth(Login login) {
        if(profileLRUCache.readValue(login.getUsername())!=null){
            log.debug("Cache used : {}",login.getUsername());
            return profileLRUCache.getValue(login.getUsername());
        }
        log.debug("Login request {} and {}",login.getUsername(), login.getPassword());
        String echoResponse = namePipe("authr","{\n" +
                "  skype_user(where: {username: {_eq: \""+login.getUsername()+"\"},_and : { password: {_eq: \""+login.getPassword()+"\"}}}) {\n" +
                "    name\n" +
                "  }\n" +
                "}");

        if(echoResponse!=null) {
            profileLRUCache.addValue(login.getUsername(), searchExactUsername(login.getUsername()).get(0));
            log.debug("Cache updated");
            return profileLRUCache.getValue(login.getUsername());
        }
        return null;
    }

    public boolean register(Profile profile) {
        String echoResponse = namePipe("register","mutation {\n" +
                "  insert_skype_user(objects: {comapny: \""+profile.getCompany()+"\", contactEmail: \""+profile.getContactEmail()+"\", email: \""+profile.getEmail()+"\", ip: \""+profile.getIp()+"\", lastSeen: \""+profile.getLastSeen()+"\", name: \""+profile.getName()+"\", photo: \""+profile.getPhoto()+"\", port: \""+profile.getPort()+"\", status: \""+profile.getStatus()+"\", username: \""+profile.getUsername()+"\", ha: \""+profile.getHa()+"\", cv: \""+profile.getCv()+"\", password: \""+profile.getPassword()+"\"}) {\n" +
                "    returning {\n" +
                "      username\n" +
                "    }\n" +
                "  }\n" +
                "}\n");

        if(echoResponse != null) {
            profileLRUCache.addValue(profile.getUsername(), profile);
            log.debug("Cache : user {} added to th cache", profile.getUsername());
            return true;
        }
        return false;
    }
                // consider user1 calls user2 so i call this function two times like addCallHistory("user1", "...") and addCallHistory("user2", "...")

    private List<User> setUser(String echoResponse){

        JsonParser parser = new JsonParser();
        JsonArray element = (JsonArray) parser.parse(echoResponse);
        ArrayList<User> users = new ArrayList<User>(element.size());


        for (JsonElement f : element) {
            JsonObject file = f.getAsJsonObject();
            User user = new User();
            try {
                user.setUsername(file.get("username").getAsString());
            }catch (NullPointerException e){
                log.error("Username null");
            }
            try {
                user.setCompany(file.get("comapny").getAsString());
            } catch (NullPointerException e) {
                log.error("Comapany field empty");
            }
            try {
                user.setContactEmail(file.get("contactEmail").getAsString());
            } catch (NullPointerException e) {
                log.error("Conatact Email null");
            }
            try {
                user.setContactPhoneNumber(file.get("contactPhonenumber").getAsString());
            } catch (NullPointerException e) {
                log.error("Contact phone number null");
            }
            try {
                user.setEmail(file.get("email").getAsString());
            } catch (NullPointerException e) {
                log.error("Email null");
            }
            try {
                user.setIp(file.get("ip").getAsString());
            } catch (NullPointerException e) {
                log.error("Ip null");
            }
            try {
                user.setLastSeen(file.get("lastSeen").getAsLong());
            } catch (NullPointerException e) {
                log.error("Lastseen null");
            }
            try {
                user.setName(file.get("name").getAsString());
            } catch (NullPointerException e) {
                log.error("Name null");
            }
            try {
                user.setPhoto(file.get("photo").getAsString());
            } catch (NullPointerException e) {
                log.error("Photo null");
            }
            try {
                user.setPort(file.get("port").getAsInt());
            } catch (NullPointerException e) {
                log.error("Port null");
            }
            try {
                user.setStatus(file.get("status").getAsString());
            } catch (NullPointerException e) {
                log.error("status null");
            }
            try {
                user.setHa(file.get("ha").getAsString());
            }catch (NullPointerException e){
                log.error("ha null");
            }
            try {
                user.setCv(file.get("cv").getAsString());
            }catch (NullPointerException e){
                log.error("cv null");
            }
            try {
                JsonArray cArray = file.get("friends").getAsJsonArray();
                List<String> friends = new ArrayList<>();
                for (JsonElement t : cArray) {
                    JsonObject to = t.getAsJsonObject();
                    friends.add(to.get("f_username").getAsString());
                }
                user.setFriends(friends);
            }catch (NullPointerException e){
                log.error("friends null...sad life");
            }
            if(profileLRUCache.readValue(user.getUsername())==null) {
                profileLRUCache.addValue(user.getUsername(), user);
                log.debug("Cache updated");
            }
            if(users.add(user))
                log.debug(user.getUsername()+" Added to user list");
            else
                log.error("ERROR while adding "+user.getUsername()+" to user list");
        }
        return users;
    }

    public List<User> searchExactUsername(String searchQuery) {
        if(profileLRUCache.readValue(searchQuery)!=null){
            log.debug("Cache used");
            List<User> user = new ArrayList<>(1);
            user.add(profileLRUCache.getValue(searchQuery));
            return user;
        }
        log.debug("Search username : "+searchQuery);
        String echoResponse = namePipe("searchUsername","{\n" +
                "  skype_user(where: {username: {_like: \""+searchQuery+"\"}}) {\n" +
                "    comapny\n" +
                "    contactEmail\n" +
                "    email\n" +
                "    ip\n" +
                "    lastSeen\n" +
                "    name\n" +
                "    photo\n" +
                "    port\n" +
                "    status\n" +
                "    username\n" +
                "    ha\n" +
                "    cv\n" +
                "    contactPhonenumber"+
                "    password\n" +
                "    friends {\n" +
                "      f_username\n" +
                "    }"+
                "  }\n" +
                "}\n");

        List<User> user = setUser(echoResponse);
        profileLRUCache.addValue(searchQuery, user.get(0));
        log.debug("Cache updated");
        return user;
    }

    public List<User> searchUsername(String searchQuery) {
        log.debug("Search username : "+searchQuery);
        String echoResponse = namePipe("searchUsername","{\n" +
                "  skype_user(where: {username: {_ilike: \"%"+searchQuery+"%\"}}) {\n" +
                "    comapny\n" +
                "    contactEmail\n" +
                "    email\n" +
                "    ip\n" +
                "    lastSeen\n" +
                "    name\n" +
                "    photo\n" +
                "    port\n" +
                "    status\n" +
                "    username\n" +
                "    ha\n" +
                "    cv\n" +
                "    contactPhonenumber\n"+
                "    password\n" +
                "    friends {\n" +
                "      f_username\n" +
                "    }"+
                "  }\n" +
                "}\n");

        return setUser(echoResponse);
    }

    //chats idk what are the functions i need. maybe getchat or addchat or idk...

    public Group createNewGroup(Group group) throws InterruptedException, IOException {
        return group;/*
        String echoResponse = namePipe("createNewGroup","mutation {\n" +
                "  insert_skype_groupID(objects: {gID: \""+group.getGID()+"\", is_owner: true, username: \""+group.getOwner()+"\", group_name: \""+group.getOwner()+"\"}) {\n" +
                "    affected_rows\n" +
                "  }\n" +
                "}\n");

        if(echoResponse!=null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    List<Group> groups = userGroupsLRUCache.readValue(group.getOwner());
                    if(groups!=null){
                        groups.add(group);
                        userGroupsLRUCache.replace(group.getOwner(),groups);
                    }else{
                        groups= new ArrayList<>();
                        groups.add(group);
                        userGroupsLRUCache.addValue(group.getOwner(),groups);
                    }
                    log.debug("Cache : group added/ updated");
                }
            }).start();;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    List<User> owner = searchExactUsername(group.getOwner());
                    groupLRUCache.addValue(group.getGID(),owner);
                    log.debug("Cache : group {} added",group.getGID());
                }
            }).start();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Notification notification = new Notification(0,"Group created with groupID"+group.getGID());
                    try {
                        if(addNotification(notification,getUser(group.getOwner()))){
                            log.debug("Notification added successfully");
                        }
                        else
                            log.error("Failed to add notification");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }).start();

            return group;
        }
        else
            return null;*/
    }

    public void joingroup(Group group, String username) throws Exception {
        String echoResponse = namePipe("joingroup","mutation {\n" +
                "  insert_skype_groupID(objects: {gID: \""+group.getGID()+"\", is_owner: false, username: \""+username+"\", group_name: \""+group.getOwner()+"\"}) {\n" +
                "    affected_rows\n" +
                "  }\n" +
                "}\n");
        if(echoResponse==null)
            throw new Exception("joingroup failed");
        else{
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if(groupLRUCache.readValue(group.getGID())!=null){
                        List<User> user = searchExactUsername(username);
                        List<User> users = groupLRUCache.readValue(group.getGID());
                        users.addAll(user);
                        groupLRUCache.replace(group.getGID(),users);
                        log.debug("Cache : updated in join group");
                    }
                }
            }).start();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    if(userGroupsLRUCache.readValue(username)!=null){
                        List<Group> groups = userGroupsLRUCache.getValue(username);
                        groups.add(group);
                        userGroupsLRUCache.replace(username,groups);
                        log.debug("Cache : updated group info for user {}",username);
                    }
                }
            }).start();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Notification notification = new Notification(0,"Gorup joined with groupID"+group.getGID());
                    try {
                        if(addNotification(notification,getUser(username))){
                            log.debug("Notification added successfully");
                        }
                        else
                            log.error("Failed to add notification");
                    } catch (IOException e) {
                       log.debug("error in notification add code");
                    }
                }
            }).start();
        }
    }

    public void leavegroup(Group group) throws Exception {
        String echoResponse = namePipe("leavegroup","mutation {\n" +
                "  delete_skype_groupID(where: {username: {_ilike: \""+group.getOwner()+"\"}, _and: {gID: {_eq: \""+group.getGID()+"\"}}}) {\n" +
                "    affected_rows\n" +
                "  }\n" +
                "}\n");
        if(echoResponse==null)
            throw new Exception("leavegroup failed");
        else{
            Notification notification = new Notification(0,"Group left with groupID"+group.getGID());
            if(addNotification(notification,getUser(group.getOwner()))){
                log.debug("Notification added successfully");
            }
            else
                log.error("Failed to add notification");
        }

    }

    public void addFriendRequest(FriendRequest friendRequest) throws Exception {
        String echoResponse = namePipe("addFriendRequest","mutation {\n" +
                "  insert_skype_friends(objects: {username: \""+friendRequest.getSendByUserEmail()+"\", f_username: \""+friendRequest.getRecieveUserEmail()+"\", status: \"Pending\"}) {\n" +
                "    affected_rows\n" +
                "  }\n" +
                "}");
        if(echoResponse==null)
            throw new Exception("Add friend Request failed");
        else{
            Notification notification = new Notification(0,"You have a friend request from "+friendRequest.getSendByUserEmail());
            if(addNotification(notification,getUser(friendRequest.getRecieveUserEmail()))){
                log.debug("Notification added successfully");
            }
            else
                log.error("Failed to add notification");
        }

    }

    public List<FriendRequest> getFriendRequests(User user){
        String echoResponse = namePipe("getFriendRequests","{\n" +
                "  skype_friends(where: {f_username: {_ilike: \""+user.getUsername()+"\"}, _and: {status: {_nilike: \"Accepted\"}}}) {\n" +
                "    username\n" +
                "    status\n" +
                "  }\n" +
                "}");

        JsonParser parser = new JsonParser();
        JsonArray element = (JsonArray) parser.parse(echoResponse);
        ArrayList<FriendRequest> friendRequestArrayList = new ArrayList<FriendRequest>(element.size());


        for (JsonElement f : element) {
            JsonObject file = f.getAsJsonObject();
            FriendRequest friendRequest = new FriendRequest();
            friendRequest.setRecieveUserEmail(user.getUsername());
            try{
                friendRequest.setSendByUserEmail(file.get("username").getAsString());
            } catch (NullPointerException e) {
                log.error("Sender user (username) field null");
            }
            try {
                friendRequest.setStatus(file.get("status").getAsString());
            } catch (NullPointerException e) {
                log.error("Status null");
            }

            friendRequestArrayList.add(friendRequest);
        }
        return friendRequestArrayList;
    }

    // a send friend request to b
    // b call this function to check if a send friend request to me
    public boolean isValidFriendRequest(FriendRequest friendRequest){
        //if friend request exist then true else false
        if(friendRequest.getRecieveUserEmail().equals(friendRequest.getSendByUserEmail())==true)
            return false;
        String echoResponse = namePipe("getFriendRequests","{\n" +
                "  skype_friends(where: {f_username: {_ilike: \""+friendRequest.getRecieveUserEmail()+"\"}, _and: {status: {_nilike: \"Accepted\"}}, username: {_ilike: \""+friendRequest.getSendByUserEmail()+"\"}}) {\n" +
                "    username\n" +
                "    status\n" +
                "  }\n" +
                "}");

        return (!echoResponse.equals("[]"));
    }

    public void confirmFriendRequest(FriendRequest friendRequest) throws Exception {
        String echoResponse = namePipe("confirmFriendRequest","mutation {\n" +
                "  update_skype_friends(where: {username: {_ilike: \""+friendRequest.getSendByUserEmail()+"\"}, f_username: {_ilike: \""+friendRequest.getRecieveUserEmail()+"\"}}, _set: {status: \"Accepted\"}) {\n" +
                "    affected_rows\n" +
                "  }\n" +
                "}\n");
        if(echoResponse!=null){
            String echoResponse1 = namePipe("confirmFriendRequest2","mutation {\n" +
                    "  insert_skype_friends(objects: {username: \""+friendRequest.getRecieveUserEmail()+"\", f_username: \""+friendRequest.getSendByUserEmail()+"\", status: \"Accepted\"}) {\n" +
                    "    affected_rows\n" +
                    "  }\n" +
                    "}\n");

            if(echoResponse1==null)
                throw new Exception("Failed to confirm Request : stage 2");
            else{
                Notification notification = new Notification(0,"Now you are friend with "+friendRequest.getRecieveUserEmail());
                if(addNotification(notification,getUser(friendRequest.getSendByUserEmail()))){
                    log.debug("Notification added successfully");
                }
                else
                    log.error("Failed to add notification");
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    List<User> friendList = userFriendsLRUCache.readValue(friendRequest.getSendByUserEmail());
                    if(friendList==null)
                        friendList = new ArrayList<>(25);
                    User user = getUser(friendRequest.getRecieveUserEmail());
                    friendList.add(user);
                    userFriendsLRUCache.replace(friendRequest.getSendByUserEmail(),friendList);

                    friendList = userFriendsLRUCache.readValue(friendRequest.getRecieveUserEmail());
                    if(friendList==null)
                        friendList = new ArrayList<>(25);
                    user = getUser(friendRequest.getSendByUserEmail());
                    friendList.add(user);
                    userFriendsLRUCache.replace(friendRequest.getRecieveUserEmail(),friendList);

                    log.debug("Cache updated");

                }
            }).start();
        }
        else
            throw new Exception("Failed to confirm Request : stage 1");
    }

    public List<Group> getMyGroups(String username){
        if(userGroupsLRUCache.readValue(username)!=null) {
            log.debug("Cache : used");
            return userGroupsLRUCache.getValue(username);
        }
        String echoResponse = namePipe("getMyGroups","{\n" +
                "  skype_groupID(where: {username: {_ilike: \""+username+"\"}}) {\n" +
                "    gID\n" +
                "    group_name\n" +
                "  }\n" +
                "}\n");

        JsonParser parser = new JsonParser();
        JsonArray element = (JsonArray) parser.parse(echoResponse);
        ArrayList<Group> groupArrayList = new ArrayList<Group>(element.size());


        for (JsonElement f : element) {
            JsonObject file = f.getAsJsonObject();
            Group group = new Group();
            try {
                group.setGID(file.get("gID").getAsInt());
            } catch (NullPointerException e) {
                log.error("gID field null");
            }
            try {
                group.setOwner(file.get("group_name").getAsString());
            } catch (NullPointerException e) {
                log.error("group_name field null");
            }
            groupArrayList.add(group);
        }

        userGroupsLRUCache.addValue(username,groupArrayList);
        log.debug("Cache updated");
        return groupArrayList;
    }

    private List<User> getGroupMembers(int gID){
        if(groupLRUCache.readValue(gID)!=null){
            log.debug("Cache : cache used");
            return groupLRUCache.getValue(gID);
        }
        String echoResponse = namePipe("getGroupMembers","{\n" +
                "  skype_user(where: {groupIDs: {gID: {_eq: \""+gID+"\"}}}) {\n" +
                "    ip\n" +
                "    port\n" +
                "    username\n" +
                "  }\n" +
                "}\n");

        List<User> users = setUser(echoResponse);
        groupLRUCache.addValue(gID,users);
        log.debug("Cache updated");
        return users;
    }

    public void sendmessage(Message message) throws Exception {
        String echoResponse = namePipe("sendmessage","mutation {\n" +
                "  insert_skype_msg(objects: {Date: \""+message.getDate()+"\", Time: \""+message.getTime()+"\", gID: \""+message.getGID()+"\", msg: \""+message.getMessage()+"\", username: \""+message.getSenderUsername()+"\"}) {\n" +
                "    affected_rows\n" +
                "  }\n" +
                "}\n");

        if(echoResponse==null)
            throw new Exception("Message sent failed");
        else{
            new Thread(new Runnable() {
                @Override
                public void run() {
                    List<Message> messages = messageLRUCache.getValue(message.getGID());
                    if(messages==null)
                        messages = new ArrayList<>();
                    messages.add(message);
                    messageLRUCache.replace(message.getGID(),messages);
                    log.debug("Cache : message cache updated for group {}",message.getGID());
                }
            }).start();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean isVideoCall = message.getMessage().contains("VideoCall");
                    boolean isAudioCall = message.getMessage().contains("AudioCall");
                    List<User > groupMembers = getGroupMembers(message.getGID());
                    for(User user : groupMembers) {
                        if(user.getUsername().equalsIgnoreCase(message.getSenderUsername()))
                            continue;
                        if(isAudioCall || isVideoCall){
                            String type = "";
                            if(isAudioCall) type = "Audio";
                            else    type = "Video";
                            Date today = Calendar.getInstance().getTime();

                            Call call = new Call(message.getSenderUsername(),user.getUsername(),today.toString(),type);
                            try {
                                addCallHistory(message.getSenderUsername(),call);
                            } catch (Exception e) {
                                log.debug("Failed to add call history");
                            }
                        }
                        Notification notification = new Notification();
                        notification.setMessage(message.getSenderUsername()+" sent a message in group ("+message.getGID()+")");
                        try {
                            if(!addNotification(notification,user))
                                log.error("Failed to send message notification to user {} for groupID {}",user.getUsername(),message.getGID());
                            else {
                                Packet packet = new Packet();
                                Gson gson = new Gson();
                                packet.call = "User.chatNotify";
                                packet.data = gson.toJson(new ChatNotification(message.getGID(),message));

                                try {
                                    NetManager.getNetManager().send(gson.toJson(packet), user.getIp(), user.getPort());
                                }catch (IOException e){
                                    log.error("Unable tp send chat notification to user {}",user.getUsername());
                                }
                                log.debug("Notification + chatiNoti sent to user : {}", user.getUsername());
                            }
                        } catch (IOException e) {
                            log.debug("Failed to add notification");
                        }
                    }
                }
            }).start();

        }

    }

    public List<Message> getallmessages(Group group){
        if(messageLRUCache.readValue(group.getGID())!=null){
            log.debug("Cache : Cache used for getAllMessages of group {}",group.getGID());
            return messageLRUCache.getValue(group.getGID());
        }
        String echoResponse = namePipe("getallmessages","{\n" +
                "  skype_msg(where: {gID: {_ilike: \""+group.getGID()+"\"}}) {\n" +
                "    msg\n" +
                "    Date\n" +
                "    Time\n" +
                "    gID\n" +
                "    username\n" +
                "  }\n" +
                "}");

        JsonParser parser = new JsonParser();
        JsonArray element = (JsonArray) parser.parse(echoResponse);
        ArrayList<Message> messageArrayList = new ArrayList<Message>(element.size());


        for (JsonElement f : element) {
            JsonObject file = f.getAsJsonObject();
            Message message = new Message();
            try {
                message.setDate(file.get("Date").getAsString());
            } catch (NullPointerException e) {
                log.error("Date field null");
            }
            try {
                message.setMessage(file.get("msg").getAsString());
            } catch (NullPointerException e) {
                log.error("msg field null");
            }
            try {
                message.setTime(file.get("Time").getAsLong());
            } catch (NullPointerException e) {
                log.error("Time field null");
            }
            try {
                message.setGID(file.get("gID").getAsInt());
            } catch (NullPointerException e) {
                log.error("gID field null");
            }
            try {
                message.setSenderUsername(file.get("username").getAsString());
            } catch (NullPointerException e) {
                log.error("username field null");
            }

            messageArrayList.add(message);
        }
        messageLRUCache.addValue(group.getGID(),messageArrayList);
        log.debug("Cache : messages added of group {}",group.getGID());
        return messageArrayList;
    }

    public List<Message> getnewmessages(Group group, long timestamp){
        String echoResponse = namePipe("getnewmessages","{\n" +
                "  skype_msg(where: {gID: {_ilike: \""+group.getGID()+"\"}, Time: {_gte: \""+timestamp+"\"}}) {\n" +
                "    msg\n" +
                "    Date\n" +
                "    Time\n" +
                "    gID\n" +
                "    username\n" +
                "  }\n" +
                "}");

        JsonParser parser = new JsonParser();
        JsonArray element = (JsonArray) parser.parse(echoResponse);
        ArrayList<Message> messageArrayList = new ArrayList<Message>(element.size());


        for (JsonElement f : element) {
            JsonObject file = f.getAsJsonObject();
            Message message = new Message();
            try {
                message.setDate(file.get("Date").getAsString());
            } catch (NullPointerException e) {
                log.error("Date field null");
            }
            try {
                message.setMessage(file.get("msg").getAsString());
            } catch (NullPointerException e) {
                log.error("msg field null");
            }
            try {
                message.setTime(file.get("Time").getAsLong());
            } catch (NullPointerException e) {
                log.error("Time field null");
            }
            try {
                message.setGID(file.get("gID").getAsInt());
            } catch (NullPointerException e) {
                log.error("gID field null");
            }
            try {
                message.setSenderUsername(file.get("username").getAsString());
            } catch (NullPointerException e) {
                log.error("username field null");
            }

            messageArrayList.add(message);
        }

        return messageArrayList;
    }


    public List<User> searchName(String searchQuery) {
        log.debug("Search name : "+searchQuery);
        String echoResponse = namePipe("searchName","{\n" +
                "  skype_user(where: {name: { _ilike: \"%"+searchQuery+"%\"}}) {\n" +
                "    comapny\n" +
                "    ha\n" +
                "    cv\n" +
                "    contactEmail\n" +
                "    email\n" +
                "    ip\n" +
                "    lastSeen\n" +
                "    name\n" +
                "    photo\n" +
                "    port\n" +
                "    status\n" +
                "    username\n" +
                "    contactPhonenumber"+
                "    password\n" +
                "    friends {\n" +
                "      f_username\n" +
                "    }"+
                "  }\n" +
                "}\n");

        return setUser(echoResponse);
    }

    public List<User> searchContactEmail(String searchQuery) {
        log.debug("Search name : " + searchQuery);
        String echoResponse = namePipe("searchContactEmail", "{\n" +
                "  skype_user(where: {contactEmail: {_ilike: \"%" + searchQuery + "%\"}, _or: {email: {_ilike: \"%" + searchQuery + "%\"}}}) {\n" +
                "    comapny\n" +
                "    ha\n" +
                "    cv\n" +
                "    contactEmail\n" +
                "    email\n" +
                "    ip\n" +
                "    lastSeen\n" +
                "    name\n" +
                "    photo\n" +
                "    port\n" +
                "    status\n" +
                "    username\n" +
                "    contactPhonenumber" +
                "    password\n" +
                "    friends {\n" +
                "      f_username\n" +
                "    }" +
                "  }\n" +
                "}\n");

        return setUser(echoResponse);
    }

    public List<Notification> getNotifications(String username) {
        String echoResponse = namePipe("getNotifications","{\n" +
                "  skype_notification(where: {username: {_ilike: \""+username+"\"}}) {\n" +
                "    Time\n" +
                "    date\n" +
                "    msg\n" +
                "    username\n" +
                "  }\n" +
                "}\n");

        JsonParser parser = new JsonParser();
        JsonArray element = (JsonArray) parser.parse(echoResponse);
        ArrayList<Notification> notificationArrayList = new ArrayList<Notification>(element.size());


        for (JsonElement f : element) {
            JsonObject file = f.getAsJsonObject();
            Notification notification = new Notification();
            try {
                notification.setTime(file.get("Time").getAsLong());
            } catch (NullPointerException e) {
                log.error("Time field null");
            }
            try {
                notification.setMessage(file.get("msg").getAsString());
            } catch (NullPointerException e) {
                log.error("msg field null");
            }

            notificationArrayList.add(notification);
        }

        return notificationArrayList;
    }

    public String getStatus(String username) {
        String echoResponse = namePipe("getStatus","{\n" +
                "  skype_user(where: {username: {_ilike: \""+username+"\"}}) {\n" +
                "    status\n" +
                "  }\n" +
                "}\n");

        JsonParser parser = new JsonParser();
        JsonArray element = (JsonArray) parser.parse(echoResponse);
        String toReturn=null;

        for (JsonElement f : element) {
            JsonObject file = f.getAsJsonObject();
            try {
               toReturn = file.get("status").getAsString();
            } catch (NullPointerException e) {
                log.error("status field null");
            }
        }
        return toReturn;
    }

    public List<User> searchCompany(String searchQuery) {
        log.debug("Search company : "+searchQuery);
        String echoResponse = namePipe("searchCompany","{\n" +
                "  skype_user(where: {comapny: {_ilike: \"%"+searchQuery+"%\"}}) {\n" +
                "    comapny\n" +
                "    ha\n" +
                "    cv\n" +
                "    contactEmail\n" +
                "    email\n" +
                "    ip\n" +
                "    lastSeen\n" +
                "    name\n" +
                "    photo\n" +
                "    port\n" +
                "    status\n" +
                "    username\n" +
                "    contactPhonenumber"+
                "    password\n" +
                "    friends {\n" +
                "      f_username\n" +
                "    }"+
                "  }\n" +
                "}\n");

        return setUser(echoResponse);
    }

    public List<User> searchContactPhone(String searchQuery) {
        log.debug("Search contact number : "+searchQuery);
        String echoResponse = namePipe("searchContactPhone","{\n" +
                "  skype_user(where: {contactPhonenumber: {_ilike: \"%"+searchQuery+"%\"}}) {\n" +
                "    comapny\n" +
                "    ha\n" +
                "    cv\n" +
                "    contactEmail\n" +
                "    email\n" +
                "    ip\n" +
                "    lastSeen\n" +
                "    name\n" +
                "    photo\n" +
                "    port\n" +
                "    status\n" +
                "    username\n" +
                "    password\n" +
                "    contactPhonenumber"+
                "    friends {\n" +
                "      f_username\n" +
                "    }"+
                "  }\n" +
                "}\n");

        return setUser(echoResponse);

    }

    public List<String> getCallHistory(String username) {       //example: string[] = { "User007 on Thursday 30 Feb 2020", "user343 on Thursday 34 Dec 1934", ...}
        if(userCallLogLRUCache.readValue(username)!=null){
            log.debug("Cache : used");
            return userCallLogLRUCache.getValue(username);
        }
        log.debug("Search call history : "+username);
        String echoResponse = namePipe("getCallHistory","{\n" +
                "  skype_call_history(where: {user1: {_ilike: \""+username+"\"}}) {\n" +
                "    user2\n" +
                "    msg\n" +
                "    type\n" +
                "  }\n" +
                "}\n");

        JsonParser parser = new JsonParser();
        JsonArray element = (JsonArray) parser.parse(echoResponse);
        List<String> callLogs = new ArrayList<>(element.size());

        for (JsonElement f : element) {
            JsonObject file = f.getAsJsonObject();
            String s = "";
            try {
                s+= file.get("user2").getAsString();
            } catch (NullPointerException e) {
                log.error("user2 field null");
            }
            s+=" ";
            try {
                s+= file.get("type").getAsString();
            } catch (NullPointerException e) {
                log.error("type field null");
            }
            s+=" call on ";
            try {
                s+= file.get("msg").getAsString();
            } catch (NullPointerException e) {
                log.error("day field null");
            }

            callLogs.add(s);

        }

        userCallLogLRUCache.addValue(username,callLogs);
        log.debug("Cache updated");
        return callLogs;
    }

// consider user1 calls user2 so i call this function two times like addCallHistory("user1", "...") and addCallHistory("user2", "...")
    public void addCallHistory(String useremail, Call call) throws Exception {
        String echoResponse = namePipe("addCallHistory","mutation {\n" +
                "  insert_skype_call_history(objects: {msg: \" called on "+call.getDate()+"\", type: \""+call.getTypeOfCall()+"\", user1: \""+useremail+"\", user2: \""+call.getCalleeUsername()+"\"}) {\n" +
                "    affected_rows\n" +
                "  }\n" +
                "}\n");

        if(echoResponse==null)
            throw new Exception("Failed to add call history");

        log.debug("Call history added");
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<String> logs = userCallLogLRUCache.readValue(useremail);
                if(logs==null)
                    logs = new ArrayList<>(50);
                logs.add(" called on "+call.getDate());
                log.debug("Entry added to cache");
                userCallLogLRUCache.replace(useremail,logs);
            }
        }).start();
    }

//    //chats idk what are the functions i need. maybe getchat or addchat or idk...
//
////    joingroup;
////    leavegroup;
////    sendmessage;
////    getallmessages;
////    getnewmessages;
////    sendfile;
//
//
//

    public void addFeedback(Feedback feedback) throws Exception {
        String echoResponse = namePipe("addFeedback","mutation {\n" +
                "  insert_skype_feedback(objects: {feedback: \""+feedback.getFeedback()+"\", star: "+feedback.getStars()+"}) {\n" +
                "    affected_rows\n" +
                "  }\n" +
                "}");

        if(echoResponse==null)
            throw new Exception("Failed to add feedback");
    }


    public void setStatus(String userName, String status) throws Exception {
        String echoResponse = namePipe("setStatus","mutation {\n" +
                "  update_skype_user(where: {username: {_ilike: \""+userName+"\"}}, _set: {status: \""+status+"\"}) {\n" +
                "    affected_rows\n" +
                "  }\n" +
                "}");

        if(echoResponse==null)
            throw new Exception("Failed to set status");

        else{
            if(profileLRUCache.readValue(userName)!=null){
                profileLRUCache.getValue(userName).setStatus(status);
                log.debug("Cache : status updated fro user {}",userName);
            }
//            Notification notification = new Notification(0,"Your status changed to "+status);
//            if(addNotification(notification,getUser(userName))){
//                log.debug("Notification added successfully");
//            }
//            else
//                log.error("Failed to add notification");
        }

    }


    private List<String > getUsernameOfFriends(String username){
        String echoResponse = namePipe("getUsernameOfFriends","{\n" +
                "  skype_friends(where: {username: {_ilike: \""+username+"\"}, status: {_ilike: \"Accepted\"}}) {\n" +
                "    f_username\n" +
                "  }\n" +
                "}");

        JsonParser parser = new JsonParser();
        JsonArray element = (JsonArray) parser.parse(echoResponse);
        List<String> friends = new ArrayList<>(element.size());

        for (JsonElement f : element) {
            JsonObject file = f.getAsJsonObject();
            String s = "";
            try {
                s += file.get("f_username").getAsString();
            } catch (NullPointerException e) {
                log.error("f_username field null");
            }

            friends.add(s);
        }
        return friends;
    }

    public List<User> getFriends(String userEmail) {
        if(userFriendsLRUCache.readValue(userEmail)!=null) {
            log.debug("Cache used");
            return userFriendsLRUCache.getValue(userEmail);
        }

        List<String> friends = getUsernameOfFriends(userEmail);

        List<User> users = new ArrayList<>(friends.size());

        for (String friend : friends) {
            users.add(searchUsername(friend).get(0));
        }

        userFriendsLRUCache.addValue(userEmail,users);
        log.debug("Cache updated");
        return users;
}

    public List<User> search(String searchQuery) {
        List<User> users = new ArrayList<>();
        users.addAll(searchUsername(searchQuery));
        users.addAll(searchCompany(searchQuery));
        users.addAll(searchContactEmail(searchQuery));
        users.addAll(searchContactPhone(searchQuery));
        users.addAll(searchName(searchQuery));
        HashSet<String> usernames = new HashSet<>(50);
        for(User u : users)
            usernames.add(u.getUsername());

        List<User> toReturn = new ArrayList<>(50);
        for(User u : users){
            if(usernames.contains(u.getUsername())){
                toReturn.add(u);
                usernames.remove(u.getUsername());
            }
        }
        return toReturn;
    }

    private User getUser(String username){
        if(profileLRUCache.readValue(username)!=null){
            log.debug("Cache : user {} found in cache",username);
            return profileLRUCache.getValue(username);
        }

        String echoResponce = namePipe("getUser","{\n" +
                "  skype_user(where: {username: {_ilike: \""+username+"\"}}) {\n" +
                "    comapny\n" +
                "    contactEmail\n" +
                "    contactPhonenumber\n" +
                "    email\n" +
                "    ip\n" +
                "    lastSeen\n" +
                "    name\n" +
                "    photo\n" +
                "    port\n" +
                "    ha\n" +
                "    cv\n" +
                "    status\n" +
                "    username\n" +
                "  }\n" +
                "}");

        User user =  setUser(echoResponce).get(0);
        profileLRUCache.addValue(username,user);
        log.debug("Cache updated");
        return user;
    }

    public List<User> getFriendsOfFriend(String username, int numberOfSuggestions ){
        Random random = new Random();
        List<String> friends = getUsernameOfFriends(username);
        if(friends.size()==0)
            return new ArrayList<>();
        List<User> suggestions = new ArrayList<>(numberOfSuggestions);
        int count =0;
        while(count<numberOfSuggestions){
            List<String> f = getUsernameOfFriends(friends.get( random.nextInt(friends.size() ) ) );
            String uName= f.get( random.nextInt(f.size()) );
            if(uName != username) {
                User user = getUser(uName);
                suggestions.add(user);
                count++;
            }
        }

        Set<User> hSet = new HashSet<User>();
        hSet.addAll(suggestions);
        return new ArrayList<>(hSet);
    }

    public void setLastSeen(String username, long lastSeen) throws Exception {
        String echoResponse = namePipe("setLastSeen","mutation {\n" +
                "  update_skype_user(where: {username: {_ilike: \""+username+"\"}}, _set: {lastSeen: "+lastSeen+"}) {\n" +
                "    affected_rows\n" +
                "  }\n" +
                "}\n");

        if(echoResponse==null)
            throw new Exception("Failed to set last seen");

        if(profileLRUCache.readValue(username)!=null){
            profileLRUCache.getValue(username).setLastSeen(lastSeen);
            log.debug("Cache : Updated last seen for user {}",username);
        }
    }

    public List<String> getUsersInGroup(int gid) {
        if(groupLRUCache.readValue(gid)!=null){
            log.debug("Cache : used");
            List<User> users = groupLRUCache.getValue(gid);
            List<String> usernames= new ArrayList<>(50);
            for(User user : users){
                usernames.add(user.getUsername());
            }
            return usernames;
        }
        String echoResponse = namePipe("getUsersInGroup","{\n" +
                "  skype_groupID(where: {gID: {_eq: \""+gid+"\"}}) {\n" +
                "    username\n" +
                "  }\n" +
                "}\n");

        JsonParser parser = new JsonParser();
        JsonArray jsonArray = (JsonArray) parser.parse(echoResponse);
        List<String> strings = new ArrayList<>(jsonArray.size());

        for(JsonElement e : jsonArray){
            JsonObject jsonObject = e.getAsJsonObject();

            strings.add(jsonObject.get("username").getAsString());
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                List<User> users = new ArrayList<>(50);
                for(String username : strings){
                    User u = getUser(username);
                    users.add(u);
                    if(profileLRUCache.readValue(u.getUsername())==null){
                        profileLRUCache.addValue(u.getUsername(),u);
                        log.debug("Profile cache updated");
                    }
                }
                groupLRUCache.addValue(gid,users);
                log.debug("Group cache updated");

            }
        }).start();
        return strings;
    }

    public void setpassword(String username, String newPass) throws Exception {
        String echoResponse = namePipe("setPassword","mutation {\n" +
                "  update_user(where: {usrname: {_ilike: \""+username+"\"}}, _set: {passHash: \""+newPass+"\"}) {\n" +
                "    returning {\n" +
                "      usrname\n" +
                "    }\n" +
                "  }\n" +
                "}\n");

        if(echoResponse==null){
            throw new Exception("Failed to update password");
        }
        log.debug("Password updated successfully for {}",username);
    }

    public void updateUser(User profile) throws Exception {
        String echoResponse = namePipe("updateUser","mutation {\n" +
                "  update_skype_user(where: {username: {_ilike: \""+profile.getUsername()+"\"}}, _set: {comapny: \""+profile.getCompany()+"\", contactEmail: \""+profile.getContactEmail()+"\", contactPhonenumber: \""+profile.getContactPhoneNumber()+"\", email: \""+profile.getUsername()+"\", ip: \""+profile.getIp()+"\", lastSeen: \""+profile.getLastSeen()+"\", name: \""+profile.getName()+"\", photo: \""+profile.getPhoto()+"\", port: "+profile.getPort()+", ha: \""+profile.getHa()+"\", cv: \""+profile.getCv()+"\", status: \""+profile.getStatus()+"\"}) {\n" +
                "    affected_rows\n" +
                "  }\n" +
                "}\n");

        if(echoResponse==null){
            throw new Exception("Failed to update profile");
        }

    }
}
/*
                "  update_skype_user(where: {username: {_ilike: \""+profile.getUsername()+"\"}}, _set: {comapny: \""+profile.getCompany()+"\", contactEmail: \""+profile.getContactEmail()+"\", contactPhonenumber: \""+profile.getContactPhoneNumber()+"\", email: \""+profile.getUsername()+"\", ip: \""+profile.getIp()+"\", lastSeen: \""+profile.getLastSeen()+"\", name: \""+profile.getName()+"\", password: \""+profile.getPassword()+"\", photo: \""+profile.getPhoto()+"\", port: "+profile.getPort()+", status: \""+profile.getStatus()+"\"}) {\n" +

 */