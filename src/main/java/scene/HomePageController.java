package scene;

import container.message.*;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import com.google.gson.Gson;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextField;
import container.others.*;
import container.profile.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import com.jfoenix.controls.*;
import container.message.Message;
import container.message.Notification;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import main.Configuration;
import main.DAO;
import net.Packet;
import net.manager.NetManager;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;

@Slf4j
public class HomePageController extends SceneManager implements Initializable {

    @FXML
    private JFXTextField searchTextField;
    @FXML
    private JFXTextField connectTextField;
    @FXML
    private Text nameText;
    @FXML
    private JFXButton settingsButton;
    @FXML
    private JFXButton chatButton;
    @FXML
    private JFXButton callButton;
    @FXML
    private JFXButton contactButton;
    @FXML
    private JFXButton notificationButton;
    @FXML
    private JFXButton newGroupOkButton;
    @FXML
    private JFXButton newGroupCancelButton;
    @FXML
    private JFXButton getSuggestionButton;
    @FXML
    private Pane chatPane;
    @FXML
    private JFXButton addChatButton;
    @FXML
    private JFXListView chatListView;
    @FXML
    public JFXListView userChatListView;
    @FXML
    private Pane callPane;
    @FXML
    private JFXListView callListView;
    @FXML
    private Pane contactPane;
    @FXML
    private JFXListView contactListView;
    @FXML
    private Pane notificationPane;
    @FXML
    private JFXButton refreshButton;
    @FXML
    public JFXListView notificationListView;
    @FXML
    private JFXListView searchResultListView;
    @FXML
    private JFXListView newGroupInviteListView;
    @FXML
    private Text userNameText;
    @FXML
    private JFXButton addFriendButton;
    @FXML
    private JFXButton videoCallButton;
    @FXML
    private JFXButton audioCallButton;
    @FXML
    private JFXButton inviteFriend;
    @FXML
    private JFXTextField messageTextField;
    @FXML
    private JFXTextField groupNameTextField;
    @FXML
    private Pane userPane;
    @FXML
    private Pane profilePane;
    @FXML
    private Pane newGroupPane;
    @FXML
    private Circle userImage;
    @FXML
    private Text userName;
    @FXML
    private Text contactEmail;
    @FXML
    private Text company;
    @FXML
    private Text status;
    @FXML
    private Text name;
    @FXML
    private Text email;
    @FXML
    private Text contactPhone;
    @FXML
    private Text userLastSeen;
    @FXML
    private Text resumeText;
    @FXML
    private JFXTextArea haTextArea;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        nameText.setText(Configuration.getConfig().myProfile.getName());
        notificationPane.setVisible(false);
        callPane.setVisible(false);
        contactPane.setVisible(false);
        chatPane.setVisible(true);
        userPane.setVisible(false);
        profilePane.setVisible(false);
        DAO.selectedUser = null;
        chatListView.getItems().clear();
        newGroupPane.setVisible(false);
        log.info("Panes initialised");
        new Thread(() -> {
            running = true;
            while (running) {
                if (DAO.notification) {
                    refreshButton(null);
                    DAO.notification = false;
                }
            }
        }).start();
        new Thread(() -> {
            Configuration config = Configuration.getConfig();
            try {
                Gson gson = new Gson();
                Packet requestPacket = new Packet();
                requestPacket.call = "Database.updateUser";
                config.myProfile.setStatus("Online");
                config.myProfile.setLastSeen(System.currentTimeMillis());
                config.myProfile.setIp(config.localIP);
                config.myProfile.setPort(config.localPort);
                requestPacket.data = gson.toJson(config.myProfile);
                String request = gson.toJson(requestPacket);
                String res = NetManager.getNetManager().send(request, config.serverIP, config.serverPort);
                log.debug("Request json: {}", res);
            } catch (IOException e) {
                log.error("Error unable to send status {}:{}", config.serverIP, config.serverPort);
            }
        }).start();
        loadGroups();
        loadUser(Configuration.getConfig().myProfile);
    }

    @FXML
    public void copy(MouseEvent event) {
        StringSelection data = new StringSelection(resumeText.getText());
        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
        cb.setContents(data, data);
    }

    File pickedFile;

    void pickFile() {
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Choose a file to send", "*");
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(filter);
        pickedFile = fc.showOpenDialog(null);
        if (pickedFile == null) {
            log.info("No file selected");
            return;
        }
        log.info("Selected file: {}", pickedFile.getName());
    }

    @FXML
    void fileButton(ActionEvent event) {

        pickFile();

        if (pickedFile == null) {
            log.error("No file selected");
            return;
        }


        Configuration config = Configuration.getConfig();
        try {
            Gson gson = new Gson();
            Packet requestPacket = new Packet();
            requestPacket.call = "Server.uploadFile";
            requestPacket.data = gson.toJson(new UploadFile(readFileToByteArray(pickedFile)));
            String request = gson.toJson(requestPacket);
            String res = NetManager.getNetManager().send(request, config.serverIP, config.serverPort);
            log.debug("Request json: {}", res);
            Packet responsePacket = gson.fromJson(res, Packet.class);
            String response = gson.fromJson(responsePacket.data, String.class);
            log.info("Response Upload Image: {}", response);
            if (responsePacket.call.equals("Return.Success")) {
                sendMessage(DAO.selectedGroup, "http://" + config.getServerIP() + ":" + DAO.HTTPServerPort + "/" + response);
            }
        } catch (IOException e) {
            log.error("Error unable to connect to server {}:{}", config.serverIP, config.serverPort);
        }
    }

    private static byte[] readFileToByteArray(File file) {
        FileInputStream fis = null;
        byte[] bArray = new byte[(int) file.length()];
        try {
            fis = new FileInputStream(file);
            fis.read(bArray);
            fis.close();

        } catch (IOException ioExp) {
            ioExp.printStackTrace();
        }
        return bArray;
    }

    void setAllPane(boolean visiblity) {
        notificationPane.setVisible(visiblity);
        callPane.setVisible(visiblity);
        contactPane.setVisible(visiblity);
        chatPane.setVisible(visiblity);
        searchResultListView.setVisible(visiblity);
    }

    @FXML
    void getSuggestionButton(ActionEvent event) {
        contactListView.getItems().clear();

        Configuration config = Configuration.getConfig();
        try {
            Gson gson = new Gson();
            Packet requestPacket = new Packet();
            requestPacket.call = "Server.getSuggestions";
            requestPacket.data = gson.toJson(config.myProfile.getUsername());
            String request = gson.toJson(requestPacket);
            String res = NetManager.getNetManager().send(request, config.serverIP, config.serverPort);
            log.debug("Request json: {}", res);
            Packet responsePacket = gson.fromJson(res, Packet.class);
            SearchResult result = gson.fromJson(responsePacket.data, SearchResult.class);
            contactListView.getItems().clear();
            for (User user : result.getUsers()) {
                contactListView.getItems().add(user);
            }
            contactListView.setOnMouseClicked(e -> {
                DAO.selectedUser = (User) contactListView.getSelectionModel().getSelectedItem();
                if (DAO.selectedUser == null)
                    return;
                profilePane.setVisible(true);
                userPane.setVisible(false);
                loadUser(DAO.selectedUser);
            });
        } catch (IOException e) {
            log.error("Error unable to load groups {}:{}", config.serverIP, config.serverPort);
        }
    }

    @FXML
    void settingsButton(ActionEvent event) {
        Configuration.getConfig().loggedIn = false;
        Configuration.getConfig().saveConfiguration();
        changeScene(event, DAO.landingPage);
    }

    void loadGroups() {
        Configuration config = Configuration.getConfig();
        for (Map.Entry<Integer, Group> group : config.myGroups.entrySet()) {
            chatListView.getItems().add(group.getValue());
        }
        new Thread(() -> {
            try {
                Gson gson = new Gson();
                Packet requestPacket = new Packet();
                requestPacket.call = "Database.getMyGroups";
                requestPacket.data = gson.toJson(config.myProfile.getUsername());
                String request = gson.toJson(requestPacket);
                String res = NetManager.getNetManager().send(request, config.serverIP, config.serverPort);
                log.debug("Request json: {}", res);
                Packet responsePacket = gson.fromJson(res, Packet.class);
                GroupList groupList = gson.fromJson(responsePacket.data, GroupList.class);
                chatListView.getItems().clear();
                for (Group group : groupList.getMygroups()) {
                    chatListView.getItems().add(group);
                    config.myGroups.put(group.getGID(), group);
                }
                chatListView.setOnMouseClicked(e -> {
                    DAO.selectedGroup = (Group) chatListView.getSelectionModel().getSelectedItem();
                    profilePane.setVisible(false);
                    userPane.setVisible(true);
                    loadChat(DAO.selectedGroup);
                });

            } catch (IOException e) {
                log.error("Error unable to load groups {}:{}", config.serverIP, config.serverPort);
            }
        }).start();
    }

    void loadChat(Group group) {
        Configuration config = Configuration.getConfig();
        if (group == null)
            return;
        userNameText.setText(group.getOwner());
        if(config.messages.get(group.getGID())==null){
            List<Message> temp = new ArrayList<>();
            config.messages.put(group.getGID(),temp);
        }
        for (Message message : config.messages.get(group.getGID())) {
            userChatListView.getItems().add(message);
        }
        new Thread(() -> {
            try {
                Gson gson = new Gson();
                Packet requestPacket = new Packet();
                requestPacket.call = "Database.getChat";
                requestPacket.data = gson.toJson(group, Group.class);
                String request = gson.toJson(requestPacket);
                String res = NetManager.getNetManager().send(request, config.serverIP, config.serverPort);
                log.debug("Request json: {}", res);
                Packet responsePacket = gson.fromJson(res, Packet.class);
                MessageList messageList = gson.fromJson(responsePacket.data, MessageList.class);
                userChatListView.getItems().clear();
                for (Message message : messageList.getChats()) {
                    if (!config.messages.get(group.getGID()).contains(message))
                        config.messages.get(group.getGID()).add(message);
                    userChatListView.getItems().add(message);
                }
                userChatListView.setOnMouseClicked(e -> {
                    StringSelection data = new StringSelection(((Message) (userChatListView.getSelectionModel().getSelectedItem())).getMessage());
                    Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
                    cb.setContents(data, data);
                    log.info("Text copied to clipBoard {}", data);
                });
            } catch (IOException e) {
                log.error("Error unable to load chat {}:{} {}", config.serverIP, config.serverPort, DAO.selectedGroup);
            }
        }).start();
    }


    @FXML
    void chatButton(ActionEvent event) {
        chatButton.setTextFill(Paint.valueOf("black"));
        callButton.setTextFill(Paint.valueOf("white"));
        contactButton.setTextFill(Paint.valueOf("white"));
        chatButton.setStyle("-fx-background-color:white");
        callButton.setStyle("-fx-background-color:#2C2E38");
        contactButton.setStyle("-fx-background-color:#2C2E38");
        setAllPane(false);
        chatPane.setVisible(true);
        chatListView.getItems().clear();
        loadGroups();
    }

    @FXML
    void callButton(ActionEvent event) {
        chatButton.setTextFill(Paint.valueOf("white"));
        callButton.setTextFill(Paint.valueOf("black"));
        contactButton.setTextFill(Paint.valueOf("white"));
        chatButton.setStyle("-fx-background-color:#2C2E38");
        callButton.setStyle("-fx-background-color:white");
        contactButton.setStyle("-fx-background-color:#2C2E38");
        setAllPane(false);
        callPane.setVisible(true);
        loadCall();
    }

    private void loadCall() {
        Configuration config = Configuration.getConfig();
        try {
            Gson gson = new Gson();
            Packet requestPacket = new Packet();
            requestPacket.call = "Server.getCallHistory";
            requestPacket.data = gson.toJson(config.myProfile.getUsername(), String.class);
            String request = gson.toJson(requestPacket);
            String res = NetManager.getNetManager().send(request, config.serverIP, config.serverPort);
            log.debug("Request json: {}", res);
            Packet responsePacket = gson.fromJson(res, Packet.class);
            CallHistory callHistory = gson.fromJson(responsePacket.data, CallHistory.class);
            callListView.getItems().clear();
            for (String calls : callHistory.getHistory()) {
                callListView.getItems().add(calls);
            }
        } catch (IOException e) {
            log.error("Error unable to fetch notifications {}:{}", config.serverIP, config.serverPort);
        }
    }

    @FXML
    void contactButton(ActionEvent event) {
        chatButton.setTextFill(Paint.valueOf("white"));
        callButton.setTextFill(Paint.valueOf("white"));
        contactButton.setTextFill(Paint.valueOf("black"));
        chatButton.setStyle("-fx-background-color:#2C2E38");
        callButton.setStyle("-fx-background-color:#2C2E38");
        contactButton.setStyle("-fx-background-color:white");
        setAllPane(false);
        contactPane.setVisible(true);
        loadFriends();
    }

    private void loadFriends() {
        Configuration config = Configuration.getConfig();
        try {
            Gson gson = new Gson();
            Packet requestPacket = new Packet();
            requestPacket.call = "Database.getMyFriends";
            requestPacket.data = gson.toJson(config.myProfile.getUsername());
            String request = gson.toJson(requestPacket);
            String res = NetManager.getNetManager().send(request, config.serverIP, config.serverPort);
            log.debug("Request json: {}", res);
            Packet responsePacket = gson.fromJson(res, Packet.class);
            SearchResult result = gson.fromJson(responsePacket.data, SearchResult.class);
            contactListView.getItems().clear();
            for (User user : result.getUsers()) {
                contactListView.getItems().add(user);
            }
            contactListView.setOnMouseClicked(e -> {
                DAO.selectedUser = (User) contactListView.getSelectionModel().getSelectedItem();
                if (DAO.selectedUser == null)
                    return;
                profilePane.setVisible(true);
                userPane.setVisible(false);
                loadUser(DAO.selectedUser);
            });

        } catch (IOException e) {
            log.error("Error unable to load groups {}:{}", config.serverIP, config.serverPort);
        }
    }

    boolean notificationVisible = false;

    @FXML
    void notificationButton(ActionEvent event) {
        notificationVisible = !notificationVisible;
        notificationPane.setVisible(notificationVisible);
    }

    @FXML
    void addChatButton(ActionEvent event) {
        newGroupPane.setVisible(true);
        newGroupInviteListView.getItems().clear();
        newGroupInviteListView.setVisible(true);
        for (String friend : Configuration.getConfig().myProfile.getFriends()) {
            JFXCheckBox checkBox = new JFXCheckBox();
            checkBox.setText(friend);
            checkBox.setVisible(true);
            log.info("Add {} {}", checkBox, friend);
            newGroupInviteListView.getItems().add(checkBox);
        }
    }

    @FXML
    void newGroupOkButton(ActionEvent event) {
        Group group = new Group(new Random().nextInt(100000) + 1000, groupNameTextField.getText());
        DAO.selectedGroup = newGroup(group);
        group = DAO.selectedGroup;
        joinGroup(group, Configuration.getConfig().myProfile);
        for (Object oFriend : newGroupInviteListView.getItems()) {
            JFXCheckBox temp = (JFXCheckBox) oFriend;
            if (temp.isSelected()) {
                User friend = new User();
                friend.setUsername(temp.getText());
                joinGroup(group, friend);
            }
        }
        loadChat(DAO.selectedGroup);
        log.info("Group created");
        newGroupPane.setVisible(false);
    }

    @FXML
    void newGroupCancelButton(ActionEvent event) {
        newGroupInviteListView.getItems().clear();
        newGroupPane.setVisible(false);
    }

    void joinGroup(Group group, User selectedUser) {
        Configuration config = Configuration.getConfig();
        try {
            Gson gson = new Gson();
            Packet requestPacket = new Packet();
            requestPacket.call = "Database.joinGroup";
            requestPacket.data = gson.toJson(new GroupInvite(group, selectedUser), GroupInvite.class);
            String request = gson.toJson(requestPacket);
            String res = NetManager.getNetManager().send(request, config.serverIP, config.serverPort);
            log.debug("Request json: {}", res);
            Packet responsePacket = gson.fromJson(res, Packet.class);
        } catch (IOException e) {
            log.error("Error unable to join group {}:{}", config.serverIP, config.serverPort);
        }
    }

    Group newGroup(Group group) {
        Configuration config = Configuration.getConfig();
        try {
            Gson gson = new Gson();
            Packet requestPacket = new Packet();
            requestPacket.call = "Database.newGroup";
            requestPacket.data = gson.toJson(group, Group.class);
            String request = gson.toJson(requestPacket);
            String res = NetManager.getNetManager().send(request, config.serverIP, config.serverPort);
            log.debug("Request json: {}", res);
            Packet responsePacket = gson.fromJson(res, Packet.class);
            group = gson.fromJson(responsePacket.data, Group.class);
        } catch (IOException e) {
            log.error("Error unable to create new Group {}:{} {}", config.serverIP, config.serverPort, DAO.selectedGroup);
            return null;
        }
        return group;
    }

    @FXML
    void addFriendToGroupButton(ActionEvent event) {

    }

    @FXML
    public void refreshButton(ActionEvent event) {
        notificationListView.getItems().clear();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Configuration config = Configuration.getConfig();
                try {
                    Gson gson = new Gson();
                    Packet requestPacket = new Packet();
                    requestPacket.call = "Database.getNotification";
                    requestPacket.data = gson.toJson(config.myProfile.getUsername(), String.class);
                    String request = gson.toJson(requestPacket);
                    String res = NetManager.getNetManager().send(request, config.serverIP, config.serverPort);
                    log.debug("Request json: {}", res);
                    Packet responsePacket = gson.fromJson(res, Packet.class);
                    NotificationList notificationList = gson.fromJson(responsePacket.data, NotificationList.class);
                    for (Notification notif : notificationList.getNotifications()) {
                        notificationListView.getItems().add(notif);
                    }
                } catch (IOException e) {
                    log.error("Error unable to fetch notifications {}:{}", config.serverIP, config.serverPort);
                }
            }
        }).start();

    }

    @FXML
    void videoCallButton(ActionEvent event) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (DAO.selectedGroup != null) {
                    if (createVideoCall()) {
                        DAO.currentVideoCallConfig.setVideoEnabled(true);
                        DAO.currentVideoCallConfig.setPresentEnabled(false);
                        DAO.currentVideoCallConfig.setAudioEnabled(true);
                        DAO.typeOfCall="VideoCall";
                        changeScene(event, DAO.videoHomePage);
                    } else {
                        log.error("Unable to create video call");
                    }
                }
            }
        }).start();
    }

    @FXML
    public void connectVideoCallButton(ActionEvent event) {
        if (connectTextField.getText().isEmpty()) {
            return;
        }
        String callURL = connectTextField.getText();
        String ip = null, type;
        int port = -1;
        String password = null;
        try {
            type = callURL.split(":")[0];
            ip = callURL.split(":")[1].substring(2);
            port = Integer.parseInt(callURL.split(":")[2]);
            password = callURL.split(":")[3];
        } catch (Exception e) {
            log.error("Unable to parse {} {} {}", ip, port, password);
            return;
        }
        if(type.toLowerCase().contains("video")){
            if (checkCall(ip, port, password)) {
                log.info("Call Verified");
                VideoCallConfig videoConfig = new VideoCallConfig(password, ip, port);
                log.info("Attempt to connect to {} {} with {}", ip, port, password);
                DAO.currentVideoCallConfig = videoConfig;
                DAO.currentVideoCallConfig.setVideoEnabled(true);
                DAO.currentVideoCallConfig.setPresentEnabled(false);
                DAO.currentVideoCallConfig.setAudioEnabled(true);
                DAO.typeOfCall = "VideoCall";
                changeScene(event, DAO.videoHomePage);
            } else {
                log.info("Call invalid");
            }
        } else if (type.toLowerCase().contains("audio")){
            if (checkCall(ip, port, password)) {
                log.info("Call Verified");
                VideoCallConfig videoConfig = new VideoCallConfig(password, ip, port);
                log.info("Attempt to connect to {} {} with {}", ip, port, password);
                DAO.currentVideoCallConfig = videoConfig;
                DAO.currentVideoCallConfig.setVideoEnabled(false);
                DAO.currentVideoCallConfig.setPresentEnabled(false);
                DAO.currentVideoCallConfig.setAudioEnabled(true);
                DAO.typeOfCall = "AudioCall";
                changeScene(event, DAO.videoHomePage);
            } else {
                log.info("Call invalid");
            }
        } else {
            log.info("Call invalid");
        }
    }

    private boolean checkCall(String ip, int port, String password) {
        VideoCallConfig videoCallConfig = new VideoCallConfig(password, ip, port);
        Configuration config = Configuration.getConfig();
        try {
            Gson gson = new Gson();
            Packet requestPacket = new Packet();
            requestPacket.call = "Server.isVideoServerRunning";
            requestPacket.data = gson.toJson(videoCallConfig);
            String request = gson.toJson(requestPacket);
            String res = NetManager.getNetManager().send(request, config.serverIP, config.serverPort);
            log.debug("Request json: {}", res);
            Packet responsePacket = gson.fromJson(res, Packet.class);
            return gson.fromJson(responsePacket.data, Boolean.class);
        } catch (IOException e) {
            log.error("Error unable to check call {}:{}", config.serverIP, config.serverPort);
        }
        return false;
    }

    private boolean createVideoCall() {
        Configuration config = Configuration.getConfig();
        try {
            Gson gson = new Gson();
            Packet requestPacket = new Packet();
            requestPacket.call = "Server.createVideoServer";
            requestPacket.data = "";
            String request = gson.toJson(requestPacket);
            String res = NetManager.getNetManager().send(request, config.serverIP, config.serverPort);
            log.debug("Request json: {}", res);
            Packet responsePacket = gson.fromJson(res, Packet.class);
            if (responsePacket.call.equals("Return.Fail")) {
                throw new IOException("Unable to create video call server");
            }
            VideoCallConfig callConfig = gson.fromJson(responsePacket.data, VideoCallConfig.class);
            log.info("Video Call config: {}", callConfig);
            DAO.currentVideoCallConfig = callConfig;
            DAO.currentVideoCallConfig.setVideoEnabled(true);
            DAO.currentVideoCallConfig.setPresentEnabled(false);
            DAO.currentVideoCallConfig.setAudioEnabled(true);
            DAO.currentVideoCallConfig.setIp(Configuration.getConfig().serverIP);
            sendMessage(DAO.selectedGroup, "VideoCall://" + Configuration.getConfig().serverIP + ":" + callConfig.getPort() + ":" + callConfig.getPassword());
        } catch (IOException e) {
            log.error("Error unable to create Video Call Server {}:{}", config.serverIP, config.serverPort);
            return false;
        }
        return true;
    }

    @FXML
    void addFriendButton(ActionEvent event) {
        if (DAO.selectedUser == null) {
            log.error("No user selected");
            return;
        }
        if (((JFXButton) event.getSource()).getText().equals("Friend"))
            return;
        if (((JFXButton) event.getSource()).getText().equals("Accept")) {
            if (DAO.selectedUser != null) {
                acceptFriendRequest(new FriendRequest(DAO.selectedUser.getUsername(), Configuration.getConfig().myProfile.getUsername(), "ACCEPT PLEASE"));
                ((JFXButton) event.getSource()).setText("Friend");
                ((JFXButton) event.getSource()).setDisable(true);
                Configuration.getConfig().myProfile.getFriends().add(DAO.selectedUser.getUsername());
            }
            return;
        }
        FriendRequest friendRequest = new FriendRequest(Configuration.getConfig().myProfile.getUsername(), DAO.selectedUser.getUsername(), "Pending");
        Configuration config = Configuration.getConfig();
        try {
            Gson gson = new Gson();
            Packet requestPacket = new Packet();
            requestPacket.call = "Server.addFriendRequest";
            requestPacket.data = gson.toJson(friendRequest, FriendRequest.class);
            String request = gson.toJson(requestPacket);
            String res = NetManager.getNetManager().send(request, config.serverIP, config.serverPort);
            log.debug("Request json: {}", res);
            Packet responsePacket = gson.fromJson(res, Packet.class);
            log.info("Friend invite: {}", responsePacket);
        } catch (IOException e) {
            log.error("Error unable to add friend {}:{}", config.serverIP, config.serverPort);
        }
    }

    void acceptFriendRequest(FriendRequest friendRequest) {
        Configuration config = Configuration.getConfig();
        try {
            Gson gson = new Gson();
            Packet requestPacket = new Packet();
            requestPacket.call = "Server.acceptFriendRequest";
            requestPacket.data = gson.toJson(friendRequest, FriendRequest.class);
            String request = gson.toJson(requestPacket);
            String res = NetManager.getNetManager().send(request, config.serverIP, config.serverPort);
            log.debug("Request json: {}", res);
            Packet responsePacket = gson.fromJson(res, Packet.class);
            log.info("Friend accept: {}", responsePacket);
        } catch (IOException e) {
            log.error("Error unable to accept friend request {}:{}", config.serverIP, config.serverPort);
        }
    }

    @FXML
    void audioCallButton(ActionEvent event) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (DAO.selectedGroup != null) {
                    if (createVideoCall()) {
                        DAO.currentVideoCallConfig.setVideoEnabled(false);
                        DAO.currentVideoCallConfig.setPresentEnabled(false);
                        DAO.currentVideoCallConfig.setAudioEnabled(true);
                        changeScene(event, DAO.videoHomePage);
                    } else {
                        log.error("Unable to create audio call");
                    }
                }
            }
        }).start();
    }

    @FXML
    void messageTextField(KeyEvent event) {
        if (event.getCode() != KeyCode.ENTER)
            return;
        if (messageTextField.getText().isEmpty()) {
            log.warn("Message text field is empty");
            return;
        }
        sendMessage(DAO.selectedGroup, messageTextField.getText());
    }

    void sendMessage(Group group, String chat) {
        Message message = new Message(group.getGID(), Configuration.getConfig().myProfile.getUsername(), System.currentTimeMillis(), new Date().toString(), chat);
        Configuration config = Configuration.getConfig();
        try {
            Gson gson = new Gson();
            Packet requestPacket = new Packet();
            requestPacket.call = "Server.sendMessage";
            requestPacket.data = gson.toJson(message, Message.class);
            String request = gson.toJson(requestPacket);
            String res = NetManager.getNetManager().send(request, config.serverIP, config.serverPort);
            log.debug("Request json: {}", res);
            Packet responsePacket = gson.fromJson(res, Packet.class);
            boolean result = gson.fromJson(responsePacket.data, Boolean.class);
            log.info("Chat Send Result: {}", result);
            if (result) {
                userChatListView.getItems().add(message);
                messageTextField.setText("");
            }
        } catch (IOException e) {
            log.error("Error unable to send message {}:{}", config.serverIP, config.serverPort);
        }
    }

    private void loadUser(User user) {
        if (user == null)
            return;
        if (checkFriend(user)) {
            addFriendButton.setText("Friend");
            addFriendButton.setDisable(true);
        } else {
            addFriendButton.setDisable(false);
            boolean isFriendRequest = checkFriendRequest(new FriendRequest(user.getUsername(), Configuration.getConfig().myProfile.getUsername(), ""));
            if (isFriendRequest) {
                addFriendButton.setText("Accept");
            } else {
                addFriendButton.setText("Add Friend");
            }
        }//todo AudioCall
        log.info("Load user {}", user);
        profilePane.setVisible(true);
        userPane.setVisible(false);
        newGroupPane.setVisible(false);
        notificationPane.setVisible(false);
        try {
            userImage.setFill(new ImagePattern(new Image(user.getPhoto())));
        } catch (Exception e){
            log.error("ip error to load image");
        }
        userNameText.setText(user.getName());
        chatListView.getItems().clear();
        userName.setText(user.getUsername());
        resumeText.setText(user.getCv());
        haTextArea.setText(user.getHa());
        haTextArea.setEditable(false);
        contactEmail.setText(user.getContactEmail());
        company.setText(user.getCompany());
        status.setText(user.getStatus());
        name.setText(user.getName());
        email.setText(user.getEmail());
        contactPhone.setText(user.getContactPhoneNumber());

        if (!user.getStatus().equalsIgnoreCase("online")) {
            userLastSeen.setVisible(true);
            userLastSeen.setText(new Date(user.getLastSeen()).toString());
        } else {
            userLastSeen.setVisible(false);
        }
    }

    @FXML
    public void status(MouseEvent event){
        Parent root1 = null;
        try {
            root1 = FXMLLoader.load(new File(DAO.popupPage).toURI().toURL());
//            root1 = FXMLLoader.load(getClass().getResource("notificationView.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Stage stage = new Stage();
        stage.setScene(new Scene(root1));
        stage.show();
    }

    private boolean checkFriend(User user) {
        if(user==null)
            return false;
        if(Configuration.getConfig().myProfile.getFriends()==null)
            return false;
        for (String friend : Configuration.getConfig().myProfile.getFriends()) {
            if (user.getUsername().equals(friend))
                return true;
        }
        return false;
    }

    private boolean checkFriendRequest(FriendRequest friendRequest) {
        Configuration config = Configuration.getConfig();
        try {
            Gson gson = new Gson();
            Packet requestPacket = new Packet();
            requestPacket.call = "Database.isValidFriendRequest";
            requestPacket.data = gson.toJson(friendRequest);
            String request = gson.toJson(requestPacket);
            String res = NetManager.getNetManager().send(request, config.serverIP, config.serverPort);
            log.debug("Request json: {}", res);
            Packet responsePacket = gson.fromJson(res, Packet.class);
            return gson.fromJson(responsePacket.data, Boolean.class);
        } catch (IOException e) {
            log.error("Error unable to check friend request {}:{}", config.serverIP, config.serverPort);
        }
        return false;
    }

    @FXML
    void search(KeyEvent event) {
        if (event.getCode() != KeyCode.ENTER)
            return;
        if (searchTextField.getText().isEmpty()) {
            log.warn("Search text field is empty");
            return;
        }
        Configuration config = Configuration.getConfig();
        try {
            Gson gson = new Gson();
            Packet requestPacket = new Packet();
            requestPacket.call = "Server.search";
            requestPacket.data = gson.toJson(searchTextField.getText());
            String request = gson.toJson(requestPacket);
            String res = NetManager.getNetManager().send(request, config.serverIP, config.serverPort);
            log.debug("Request json: {}", res);
            Packet responsePacket = gson.fromJson(res, Packet.class);
            SearchResult searchResult = gson.fromJson(responsePacket.data, SearchResult.class);
            log.info("Login: {}", searchResult);
            setAllPane(false);
            searchResultListView.setVisible(true);
            searchResultListView.getItems().clear();
            profilePane.setVisible(false);
            for (User user : searchResult.getUsers()) {
                searchResultListView.getItems().add(user);
            }
            searchResultListView.setOnMouseClicked(e -> {
                DAO.selectedUser = (User) searchResultListView.getSelectionModel().getSelectedItem();
                if (DAO.selectedUser == null)
                    return;
                loadUser(DAO.selectedUser);
                userPane.setVisible(false);
                profilePane.setVisible(true);
            });
        } catch (IOException e) {
            log.error("Error unable to connect to server {}:{}", config.serverIP, config.serverPort);
        }
    }


}
