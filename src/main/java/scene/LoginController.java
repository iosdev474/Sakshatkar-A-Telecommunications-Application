package scene;

import com.google.gson.Gson;
import com.jfoenix.controls.*;
import container.message.UploadFile;
import container.others.Login;
import container.profile.Profile;
import container.profile.ProfilePicture;
import container.profile.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import lombok.extern.slf4j.Slf4j;
import main.Configuration;
import main.DAO;
import main.Utility;
import net.Packet;
import net.manager.NetManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

@Slf4j
public class LoginController extends SceneManager  implements Initializable {

    @FXML
    private JFXTextField userNameTextField;
    @FXML
    private JFXPasswordField passwordTextField;
    @FXML
    private JFXButton loginButton;
    @FXML
    private JFXButton registerButton;
    @FXML
    private Text error;
    @FXML
    private Text userText;
    @FXML
    private Text passText;
    @FXML
    private Text projectText;
    @FXML
    private JFXTextField registerNameTextField;
    @FXML
    private JFXTextField registerUsernameTextField;
    @FXML
    private JFXPasswordField registerPasswordField;
    @FXML
    private JFXTextField registerEmailTextField;
    @FXML
    private JFXTextField registerCompanyTextField;
    @FXML
    private JFXTextField registerContactEmailTextField;
    @FXML
    private JFXTextField registerContactPhoneTextField;
    @FXML
    private JFXTextField portTextField;
    @FXML
    private JFXTextArea haTextArea;
    @FXML
    private JFXButton profilePictureButton;
    @FXML
    private JFXSpinner loadingSpinner;
    @FXML
    private Pane registerPane;
    @FXML
    private Pane loginPane;

    private boolean login;
    private String username;
    private boolean register;
    private boolean valid = false;
    private String profilePictureStr = "";

    @FXML
    void loginButton(ActionEvent event) {
        if (valid) {
            log.info("{}::{}",username,userNameTextField.getText());
            if (!username.equals(userNameTextField.getText())){
                valid=false;
                error.setText("Username changed Login again");
                error.setFill(Paint.valueOf("yellow"));
                return;
            }
            Configuration.getConfig().loggedIn=true;
            changeScene(event, DAO.homePage);
        }
        if (!login) {
            setLoginPaneVisiblity(true);
            login = true;
            setRegisterVisiblity(false);
            return;
        }
        if (userNameTextField.getText().isEmpty()) {
            log.warn("Username empty");
            error.setVisible(true);
            error.setFill(Paint.valueOf("red"));
            error.setText("Username empty");
            return;
        }
        if (passwordTextField.getText().isEmpty()) {
            log.warn("Password empty");
            error.setVisible(true);
            error.setFill(Paint.valueOf("red"));
            error.setText("Password empty");
            return;
        }
        new Thread(() -> {
            loadingSpinner.setVisible(true);
            Configuration config = Configuration.getConfig();
            try {
                Gson gson = new Gson();
                Packet requestPacket = new Packet();
                requestPacket.call = "Server.auth";
                requestPacket.data = gson.toJson(new Login(userNameTextField.getText(), Utility.getSha256(passwordTextField.getText())));
                String request = gson.toJson(requestPacket);
                String res = NetManager.getNetManager().send(request, config.serverIP, config.serverPort);
                log.debug("Request json: {}", res);
                Packet responsePacket = gson.fromJson(res, Packet.class);
                User response = gson.fromJson(responsePacket.data, User.class);
                log.info("Profile: {}", response);
                if (responsePacket.call.equals("Return.Success")) {
                    config.myProfile = response;
                    error.setVisible(true);
                    error.setFill(Paint.valueOf("green"));
                    error.setText("Successful Authentication");
                    valid = true;
                    username = config.myProfile.getUsername();
                } else {
                    error.setVisible(true);
                    error.setFill(Paint.valueOf("red"));
                    error.setText("Invalid Credentials");
                }
            } catch (IOException e) {
                log.error("Error unable to connect to server {}:{}", config.serverIP, config.serverPort);
                error.setVisible(true);
                error.setFill(Paint.valueOf("yellow"));
                error.setText("Unable to connect to server");
            }
            loadingSpinner.setVisible(false);
        }).start();
    }

    @FXML
    void registerButton(ActionEvent event) {
        if (register && username.equals(registerUsernameTextField.getText())) {
            error.setText("Already registered");
            return;
        }
        if (login) {
            login = false;
            setRegisterVisiblity(true);
            setLoginPaneVisiblity(false);
            return;
        }
        if (profilePictureStr.equals("")) {
            error.setFill(Paint.valueOf("red"));
            error.setText("Select profile Picture");
            error.setVisible(true);
            log.error("Select profile Picture");
            return;
        }
        if(haTextArea.getText().isEmpty()){
            error.setFill(Paint.valueOf("red"));
            error.setText("Honors? I understand but...");
            error.setVisible(true);
            log.error("Select profile Picture");
            return;
        }
        if (cv==null) {
            error.setFill(Paint.valueOf("red"));
            error.setText("Upload a CV");
            error.setVisible(true);
            log.error("Upload a CV");
            return;
        }
        if(registerNameTextField.getText().isEmpty() || registerUsernameTextField.getText().isEmpty() || registerCompanyTextField.getText().isEmpty() || registerCompanyTextField.getText().isEmpty() || registerContactEmailTextField.getText().isEmpty() || registerContactPhoneTextField.getText().isEmpty() || registerEmailTextField.getText().isEmpty()  || registerPasswordField.getText().isEmpty()){
            error.setFill(Paint.valueOf("red"));
            error.setText("Complete your profile");
            error.setVisible(true);
            return;
        }
        new Thread(() -> {
            loadingSpinner.setVisible(true);
            User user = new User();
            user.setUsername(registerUsernameTextField.getText());
            user.setEmail(registerEmailTextField.getText());
            user.setName(registerNameTextField.getText());
            user.setStatus("Online");
            user.setHa(haTextArea.getText());
            user.setCv(cv);
            user.setPhoto(profilePictureStr);
            user.setCompany(registerCompanyTextField.getText());
            user.setContactEmail(registerContactEmailTextField.getText());
            user.setContactPhoneNumber(registerContactPhoneTextField.getText());
            user.setFriends(new ArrayList<String>());
            user.setIp(Configuration.getConfig().localIP);
            user.setPort(Configuration.getConfig().localPort);
            Profile profile = new Profile(Utility.getSha256(registerPasswordField.getText()), new ArrayList<>(), user);
            Configuration config = Configuration.getConfig();
            try {
                Gson gson = new Gson();
                Packet requestPacket = new Packet();
                requestPacket.call = "Server.register";
                requestPacket.data = gson.toJson(profile);
                String request = gson.toJson(requestPacket);
                String res = NetManager.getNetManager().send(request, config.serverIP, config.serverPort);
                log.debug("Response json: {}", res);
                Packet responsePacket = gson.fromJson(res, Packet.class);
                boolean response = gson.fromJson(responsePacket.data, Boolean.class);
                log.info("Account created: {}", response);
                if (response) {
                    log.info("Account created successfully");
                    config.setMyProfile(user);
                    error.setFill(Paint.valueOf("green"));
                    error.setText("Registered Successfully");
                    error.setVisible(true);
                    setRegisterVisiblity(false);
                    setLoginPaneVisiblity(true);
                    userNameTextField.setText(registerUsernameTextField.getText());
                    passwordTextField.setText(registerPasswordField.getText());
                    register=true;
                    username=registerUsernameTextField.getText();
                } else {
                    error.setFill(Paint.valueOf("red"));
                    error.setText("Unable to create account");
                    error.setVisible(true);
                    log.error("Unable to create account");
                }
            } catch (IOException e) {
                error.setFill(Paint.valueOf("yellow"));
                error.setText("Unable to connect to server");
                error.setVisible(true);
                log.error("Error unable to connect to server {}:{}", config.serverIP, config.serverPort);
            }
            loadingSpinner.setVisible(false);
        }).start();
    }

    private String uploadPhoto(BufferedImage profileBufferedImage) {
        Configuration config = Configuration.getConfig();
        try {
            Gson gson = new Gson();
            Packet requestPacket = new Packet();
            requestPacket.call = "Server.uploadImage";
            requestPacket.data = gson.toJson(new ProfilePicture(profileBufferedImage, "png"));
            String request = gson.toJson(requestPacket);
            String res = NetManager.getNetManager().send(request, config.serverIP, config.serverPort);
            log.debug("Request json: {}", res);
            Packet responsePacket = gson.fromJson(res, Packet.class);
            String response = gson.fromJson(responsePacket.data, String.class);
            log.info("Response Upload Image: {}", response);
            if (responsePacket.call.equals("Return.Success")) {
                return "http://" + config.getServerIP() + ":" + DAO.HTTPServerPort + "/" + response;
            }
            error.setVisible(true);
            error.setFill(Paint.valueOf("green"));
            error.setText("Profile picture uploaded successfully");
        } catch (IOException e) {
            error.setVisible(true);
            error.setFill(Paint.valueOf("yellow"));
            error.setText("Unable to connect to server");
            log.error("Error unable to connect to server {}:{}", config.serverIP, config.serverPort);
        }
        return "";
    }

    void setLoginPaneVisiblity(boolean visiblity) {
        loginPane.setVisible(visiblity);
        registerPane.setVisible(!visiblity);
    }

    void setRegisterVisiblity(boolean visiblity) {
        loginPane.setVisible(!visiblity);
        registerPane.setVisible(visiblity);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setLoginPaneVisiblity(true);
        setRegisterVisiblity(false);
        projectText.setVisible(true);
        projectText.setText(DAO.projectName);
        login = true;
    }
    volatile String cv = null;
    @FXML
    void uploadButton(ActionEvent event) {
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter("Choose your Resume","*.pdf");
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(imageFilter);
        File ha = fc.showOpenDialog(null);
        if (ha == null) {
            log.info("Empty profile picture");
            return;
        }
        log.info("Selected file: {}", ha.getName());
        Configuration config = Configuration.getConfig();
        try {
            Gson gson = new Gson();
            Packet requestPacket = new Packet();
            requestPacket.call = "Server.uploadFile";
            requestPacket.data = gson.toJson(new UploadFile(readFileToByteArray(ha)), UploadFile.class);
            String request = gson.toJson(requestPacket);
            String res = NetManager.getNetManager().send(request, config.serverIP, config.serverPort);
            log.debug("Request json: {}", res);
            Packet responsePacket = gson.fromJson(res, Packet.class);
            String response = gson.fromJson(responsePacket.data, String.class);
            log.info("Response Upload File: {}", response);
            if (responsePacket.call.equals("Return.Success")) {
                cv = "http://" + config.getServerIP() + ":" + DAO.HTTPServerPort + "/" + response;
                error.setVisible(true);
                error.setFill(Paint.valueOf("green"));
                error.setText("Resume uploaded successfully");
            } else {
                error.setVisible(true);
                error.setFill(Paint.valueOf("red"));
                error.setText("Unable to upload Try again");
            }
        } catch (IOException e) {
            error.setVisible(true);
            error.setFill(Paint.valueOf("yellow"));
            error.setText("Unable to connect to server");
            log.error("Error unable to connect to server {}:{}", config.serverIP, config.serverPort);
        }
    }

    private static byte[] readFileToByteArray(File file){
        FileInputStream fis = null;
        byte[] bArray = new byte[(int) file.length()];
        try{
            fis = new FileInputStream(file);
            fis.read(bArray);
            fis.close();

        }catch(IOException ioExp){
            ioExp.printStackTrace();
        }
        return bArray;
    }

    @FXML
    void profilePictureButton(ActionEvent event) {
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter("Choose your Profile Picture", "*.png");
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(imageFilter);
        File profilePicture = fc.showOpenDialog(null);
        if (profilePicture == null) {
            log.info("Empty profile picture");
            return;
        }
        log.info("Selected file: {}", profilePicture.getName());
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(profilePicture);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (bufferedImage != null)
            profilePictureStr = uploadPhoto(bufferedImage);
        log.info("Profile Picture = {}", profilePictureStr);
        error.setVisible(true);
        error.setFill(Paint.valueOf("green"));
        error.setText("Profile picture uploaded successfully");
    }
}
