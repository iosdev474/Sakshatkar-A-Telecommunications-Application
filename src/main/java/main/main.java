package main;

import com.google.gson.Gson;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;
import net.Packet;
import net.manager.NetManager;

import java.io.File;
import java.io.IOException;

/**
 * Main class - entry point of our application
 */
@Slf4j
public class main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        DAO.status = true;
        startSleepThread();
        Parent root;
        if(Configuration.getConfig().isLoggedIn() && Configuration.getConfig().myProfile!=null) {
            root = FXMLLoader.load(new File(DAO.homePage).toURI().toURL());
            log.info("Logged in");
        } else {
            root = FXMLLoader.load(new File(DAO.landingPage).toURI().toURL());
            log.info("Not Logged in");
        }
        primaryStage.setTitle(DAO.projectName);
        NetManager.start();
        primaryStage.setScene(new Scene(root, DAO.projectWidth, DAO.projectHeight));
        primaryStage.show();
    }

    private void startSleepThread() {
        return;
        /*
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(5000);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.info("sleep thread Started");
                long oldT = System.currentTimeMillis();
                long newT = System.currentTimeMillis();
                while(DAO.status){
                    oldT=newT;
                    newT=System.currentTimeMillis();
                    log.error("awa{}",(newT-oldT));
                    if(newT-oldT>100){
                        Away();
                        log.info("AWAY");
                    }
                }
            }
        }).start();*/
    }

    private void Away() {
        return;
        /*
        Configuration config = Configuration.getConfig();
        try {
            Gson gson = new Gson();
            Packet requestPacket = new Packet();
            requestPacket.call = "Database.updateUser";
            config.myProfile.setStatus("Away");
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
        System.exit(0);*/
    }

    @Override
    public void stop(){
// todo       Configuration.getConfig().saveConfiguration();
        DAO.status = false;

        new Thread(() -> {
            Configuration config = Configuration.getConfig();
            try {
                Gson gson = new Gson();
                Packet requestPacket = new Packet();
                requestPacket.call = "Database.updateUser";
                config.myProfile.setStatus("Offline");
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
            System.exit(0);
        }).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}