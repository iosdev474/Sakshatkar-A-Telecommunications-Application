package net.video.agent;

import com.github.sarxos.webcam.Webcam;
import lombok.extern.slf4j.Slf4j;
import main.Utility;
import net.video.VideoManager;
import net.video.channel.StreamServerChannelPipelineFactory;
import net.video.handler.H264StreamEncoder;
import net.video.handler.StreamServerListener;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.SocketAddress;
import java.util.concurrent.*;

@Slf4j
public class StreamServerAgent implements IStreamServerAgent {
    protected final Webcam webcam;
    protected final Dimension dimension;
    protected final ChannelGroup channelGroup = new DefaultChannelGroup();
    protected final ServerBootstrap serverBootstrap;
    //I just move the stream encoder out of the channel pipeline for the performance
    protected final H264StreamEncoder h264StreamEncoder;
    protected volatile boolean isStreaming;
    protected ScheduledExecutorService timeWorker;
    protected ExecutorService encodeWorker;
    protected int FPS = 25;
    protected ScheduledFuture<?> imageGrabTaskFuture;
    private VideoManager videoManager;

    public StreamServerAgent(Webcam webcam, Dimension dimension) {
        super();
        this.webcam = webcam;
        this.dimension = dimension;
        //this.h264StreamEncoder = new H264StreamEncoder(dimension,false);
        this.serverBootstrap = new ServerBootstrap();
        this.serverBootstrap.setFactory(new NioServerSocketChannelFactory(
                Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool()));
        this.serverBootstrap.setPipelineFactory(new StreamServerChannelPipelineFactory(
                new StreamServerListenerIMPL(),
                dimension));
        this.timeWorker = new ScheduledThreadPoolExecutor(1);
        this.encodeWorker = Executors.newSingleThreadExecutor();
        this.h264StreamEncoder = new H264StreamEncoder(dimension, false);

    }

    public StreamServerAgent(Dimension dimension, VideoManager videoManager) {
        this(null, dimension);
        this.videoManager = videoManager;
    }


    public int getFPS() {
        return FPS;
    }

    public void setFPS(int fPS) {
        FPS = fPS;
    }

    @Override
    public void start(SocketAddress streamAddress) {
        log.info("Server started :{}", streamAddress);
        Channel channel = serverBootstrap.bind(streamAddress);
        channelGroup.add(channel);
    }

    @Override
    public void stop() {
        log.info("server is stoping");
        channelGroup.close();
        timeWorker.shutdown();
        encodeWorker.shutdown();
        serverBootstrap.releaseExternalResources();
    }


    private class StreamServerListenerIMPL implements StreamServerListener {

        @Override
        public void onClientConnectedIn(Channel channel) {
            log.info("Client Joined {}", isStreaming);
            channelGroup.add(channel);
            if (!isStreaming) {

				Runnable imageGrabTask = new ImageGrabTask();
				ScheduledFuture<?> imageGrabFuture =
						timeWorker.scheduleWithFixedDelay(imageGrabTask,
						0,
						1000/FPS,
						TimeUnit.MILLISECONDS);
				imageGrabTaskFuture = imageGrabFuture;

                Thread thread = new Thread(new ImageGrabTask());
                thread.start();
                isStreaming = true;
            }
            log.info("current connected clients :{}", channelGroup.size());
        }

        @Override
        public void onClientDisconnected(Channel channel) {
            channelGroup.remove(channel);
            int size = channelGroup.size();
            log.info("current connected clients :{}", size);
            if (size == 1) {
                //cancel the task
                imageGrabTaskFuture.cancel(false);
                webcam.close();
                isStreaming = false;
            }
        }

        @Override
        public void onException(Channel channel, Throwable t) {
            channelGroup.remove(channel);
            channel.close();
            int size = channelGroup.size();
            log.info("current connected clients :{}", size);
            if (size == 1) {
                //cancel the task
                imageGrabTaskFuture.cancel(false);
                if (webcam != null)
                    webcam.close();
                isStreaming = false;

            }

        }

        protected volatile long frameCount = 0;

        private class ImageGrabTask implements Runnable {

            @Override
            public void run() {
//			while(true){
                BufferedImage bufferedImage = webcam != null ? webcam.getImage() : Utility.joinBufferedImage(videoManager.images);
				if(bufferedImage==null) {
					log.info("No image to encode");
					return;
            	}
                log.info("image grabed {} {},count :{}",bufferedImage.getWidth(),bufferedImage.getHeight(), frameCount++);
				/**
                 * using this when the h264 encoder is added to the pipeline
                 * */
                //channelGroup.write(bufferedImage);
                /**
                 * using this when the h264 encoder is inside this class
                 * */
                encodeWorker.execute(new EncodeTask(bufferedImage));
//			}
            }

        }

        private class EncodeTask implements Runnable {
            private final BufferedImage image;

            public EncodeTask(BufferedImage image) {
                super();
                this.image = image;
            }

            @Override
            public void run() {
                try {
//                    log.error("IMAGE::::::{}", image);
                    Object msg = h264StreamEncoder.encode(image);
//                    log.error("MSG::::::{}", msg);
                    if (msg != null) {
//                        log.info("SEND IMAGE");
                        channelGroup.write(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }


    }


}
