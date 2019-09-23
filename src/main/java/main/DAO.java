package main;

import container.message.Message;
import container.others.Group;
import container.others.VideoCallConfig;
import container.profile.User;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import scene.*;

import java.awt.*;

/**
 * DAO - DataAccessObject
 * <p>
 * Used to store public data used throughout the software
 * and some constants used in software
 */
@Slf4j
public class DAO {
    public static final String configurationLocation = "Config.ser";
    public volatile static boolean status = false;
    public static final String projectName = "Sakshatkar";
    public static final String sceneLocation = "src/main/java/scene/";
    public static final String landingPage = sceneLocation + "LoginPage.fxml";
    public static final String homePage = sceneLocation + "HomePage.fxml";
    public static final String videoHomePage = sceneLocation + "VideoHome.fxml";
    public static final String videoPage = sceneLocation + "Video.fxml";
    public static final String feedbackPage = sceneLocation + "Feedback.fxml";
    public static final String popupPage = sceneLocation + "Popup.fxml";
    public static int projectWidth = 600;
    public static int projectHeight = 400;
    public static int videoWidth = 512;//480;//320;
    public static int videoHeight = 512;//360;//240;
    public static int defaultPort = 1234;
    public static User selectedUser = null;
    public static Group selectedGroup = null;
    public static VideoCallConfig currentVideoCallConfig = null;
    public static String typeOfCall = null;
    public static final int HTTPServerPort = 8080;
    public static final String HTTPServerPath = System.getProperty("user.dir");
    public static String defaultImage = "C:\\Users\\iOSDev474\\IdeaProjects\\Sakshatkar\\default.png";
    public static String testJPGImage = "C:\\Users\\iOSDev474\\IdeaProjects\\Sakshatkar\\src\\main\\resources\\User_Avatar-512.jpg";
    public static String testPNGImage = "C:\\Users\\iOSDev474\\IdeaProjects\\Sakshatkar\\src\\main\\resources\\User_Avatar-512.png";
    public static String emptyJPGImage= "C:\\Users\\iOSDev474\\IdeaProjects\\Sakshatkar\\src\\main\\resources\\User_Avatar-000.jpg";
    public static boolean notification = false;
    public static SceneManager sceneManager;
    public static LoginController loginController;
    public static HomePageController homePageController;
    public static VideoHomePageController videoHomePageController;
    public static VideoController videoController;
    public static FeedbackController feedbackController;
    public static boolean webcamLocked = false;


    public static void notification() {
        log.info("Someone trigerred notification");
        notification = true;
        Toolkit.getDefaultToolkit().beep();
        if (homePageController != null) {
            homePageController.refreshButton(null);
        }
    }

    public static void chatNotify(int groupID, Message message) {
        if (selectedGroup.getGID() == groupID) {
            try{
                homePageController.userChatListView.getItems().add(message);
            } catch (Exception e){
                log.error("Unable to write chat to home page");
            }
            try{
                videoController.messageListView.getItems().add(message);
            } catch (Exception e){
                log.error("Unable to write chat to video page");
            }
        }
    }
}