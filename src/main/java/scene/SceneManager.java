package scene;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import main.DAO;

import java.io.File;
import java.io.IOException;

/**
 * contains all event handling functions required in every controller file
 */
@Slf4j
public abstract class SceneManager {

    public static volatile boolean running=false;

    public void changeScene(Event event, String scene){
        try {
            FXMLLoader loader = new FXMLLoader(new File(scene).toURI().toURL());
            Parent home_parent = loader.load();
            switch (scene){
                case DAO.homePage:
                    DAO.homePageController=loader.getController();
                    break;
                case DAO.landingPage:
                    DAO.loginController=loader.getController();
                    break;
                case DAO.feedbackPage:
                    DAO.feedbackController=loader.getController();;
                    break;
                case DAO.videoHomePage:
                    DAO.videoHomePageController=loader.getController();
                    break;
                case DAO.videoPage:
                    DAO.videoController=loader.getController();
                    break;
                default:
                    log.error("No scene found");
                    break;
            }

            Scene Home = new Scene(home_parent);
            running=false;
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(Home);
            window.show();
        } catch (IOException e) {
            log.error("Error while changing scene {}", DAO.sceneLocation+scene);
        }
    }


    @FXML
    private void home(ActionEvent event) {
        changeScene(event, DAO.homePage);
    }



}