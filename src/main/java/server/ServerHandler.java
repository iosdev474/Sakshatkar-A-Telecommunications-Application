package server;

import com.github.sarxos.webcam.Webcam;
import lombok.extern.slf4j.Slf4j;
import main.Configuration;
import main.DAO;
import net.manager.NetManager;
import net.video.agent.StreamServerAgent;

import java.awt.*;
import java.net.InetSocketAddress;

@Slf4j
public class ServerHandler {
    StreamServerAgent serverAgent;
    int port;

    ServerHandler(int port){
        Webcam.setAutoOpenMode(true);
        Webcam webcam = Webcam.getDefault();
        Dimension dimension = new Dimension(DAO.videoWidth,DAO.videoHeight);
        webcam.setViewSize(dimension);

        serverAgent = new StreamServerAgent(webcam, dimension);
        serverAgent.start(new InetSocketAddress(Configuration.getConfig().getServerIP(), port));
        log.info("New Server handler at {}", port);
    }

    void stop(){
        serverAgent.stop();
    }
}
