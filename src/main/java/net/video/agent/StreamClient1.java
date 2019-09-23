package net.video.agent;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.net.InetSocketAddress;


import lombok.extern.slf4j.Slf4j;
import main.DAO;
import net.video.agent.ui.SingleVideoDisplayWindow;
import net.video.handler.StreamFrameListener;
@Slf4j
public class StreamClient1 {
    /**
     * @author kerr
     * */
    private final static Dimension dimension = new Dimension(DAO.videoWidth,DAO.videoHeight);
    private final static SingleVideoDisplayWindow displayWindow = new SingleVideoDisplayWindow("Stream example",dimension);
    public static void main(String[] args) {
        //setup the videoWindow
        displayWindow.setVisible(true);

        //setup the connection
        log.info("setup dimension :{}",dimension);
        StreamClientAgent clientAgent = new StreamClientAgent(new StreamFrameListenerIMPL(),dimension);
        clientAgent.connect(new InetSocketAddress("localhost", 20000));
    }


    protected static class StreamFrameListenerIMPL implements StreamFrameListener {
        private volatile long count = 0;
        @Override
        public void onFrameReceived(BufferedImage image) {
            log.info("frame received :{}",count++);
            displayWindow.updateImage(image);
        }

    }


}
