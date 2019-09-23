package scene;

import com.google.gson.Gson;
import com.jfoenix.controls.JFXButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import lombok.extern.slf4j.Slf4j;
import main.Configuration;
import net.Packet;
import net.manager.NetManager;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class Popup implements Initializable {

    @FXML
    private JFXButton set;

    int i=0;
    String[] positions = {"Online", "Available", "Meeting", "Busy", "Offline"};


    @FXML
    void back(ActionEvent event) {
        i--;
        update();
    }

    @FXML
    void forward(ActionEvent event) {
        i++;
        update();
    }

    private void update() {
        set.setText(positions[i]);
    }

    @FXML
    void set(ActionEvent event) {
        new Thread(() -> {
            Configuration config = Configuration.getConfig();
            try {
                Gson gson = new Gson();
                Packet requestPacket = new Packet();
                requestPacket.call = "Database.updateUser";
                config.myProfile.setStatus(set.getText());
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
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        switch (Configuration.getConfig().myProfile.getStatus()){
            case "Online":
                i=0;
                break;
            case "Available":
                i=1;
                break;
            case "Meeting":
                i=2;
                break;
            case "Busy":
                i=3;
                break;
            case "Offline":
                i=4;
                break;
            default:
                i=0;
                break;
        }
    }
}
