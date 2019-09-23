package scene;

import com.google.gson.Gson;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXTextArea;
import container.others.Feedback;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import lombok.extern.slf4j.Slf4j;
import main.Configuration;
import main.DAO;
import net.Packet;
import net.manager.NetManager;

import java.io.IOException;

@Slf4j
public class FeedbackController extends SceneManager {

    @FXML
    private JFXSlider starSlider;

    @FXML
    private JFXTextArea feedbackTextArea;

    @FXML
    private JFXButton submitButton;

    @FXML
    void submitButton(ActionEvent event) {
        if(feedbackTextArea.getText().isEmpty())
            return;
        Feedback feedback = new Feedback(feedbackTextArea.getText(),(int)starSlider.getValue());
        Configuration config = Configuration.getConfig();
        try {
            Gson gson = new Gson();
            Packet requestPacket = new Packet();
            requestPacket.call = "Database.feedback";
            requestPacket.data = gson.toJson(feedback);
            String request = gson.toJson(requestPacket);
            String res = NetManager.getNetManager().send(request, config.serverIP, config.serverPort);
            log.debug("Response json: {}", res);
        } catch (IOException e) {
            log.error("Error unable to join group {}:{}", config.serverIP, config.serverPort);
        }
        log.info("Feedback recorded");
        changeScene(event, DAO.homePage);
    }

}
