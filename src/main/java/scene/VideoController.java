package scene;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamException;
import com.google.gson.Gson;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXNodesList;
import com.jfoenix.controls.JFXTextField;
import container.message.Message;
import container.message.UploadFile;
import container.others.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import lombok.extern.slf4j.Slf4j;
import main.Configuration;
import main.DAO;
import main.Utility;
import net.Packet;
import net.manager.NetManager;
import net.video.agent.StreamClientAgent;
import net.video.handler.StreamFrameListener;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.ResourceBundle;

@Slf4j
public class VideoController extends SceneManager implements Initializable {
    @FXML
    private ImageView videoImageView;
    @FXML
    public JFXListView messageListView;
    @FXML
    private JFXButton micButton;
    @FXML
    private JFXButton endButton;
    @FXML
    private JFXButton videoButton;
    @FXML
    private JFXButton presentButton;
    @FXML
    private JFXButton settingsButton;
    @FXML
    private JFXTextField messageTextField;
    @FXML
    private JFXButton fileButton;
    @FXML
    private JFXNodesList nodeList;

    boolean chatVisible = true;
    StreamClientAgent clientAgent;
    VideoManager videoManager;

    @FXML
    void endButton(ActionEvent event) {
        clientAgent.stop();
        videoManager.stopVM();
        changeScene(event, DAO.feedbackPage);
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
    void fileButton(ActionEvent event) {

        File pickedFile;

        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Choose a file to send");
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(filter);
        pickedFile = fc.showOpenDialog(null);
        if (pickedFile == null) {
            log.info("No file selected");
            return;
        }
        log.info("Selected file: {}", pickedFile.getName());


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
            log.info("Response Upload File: {}", response);
            if (responsePacket.call.equals("Return.Success")) {
                sendChat("http://" + config.getServerIP() + ":" + DAO.HTTPServerPort + "/" + response);
            }
        } catch (IOException e) {
            log.error("Error unable to connect to server {}:{}", config.serverIP, config.serverPort);
        }
    }

    @FXML
    void messageTextField(KeyEvent event) {
        if (event.getCode() != KeyCode.ENTER)
            return;
        if (messageTextField.getText().isEmpty())
            return;
        sendChat(messageTextField.getText());
    }

    void sendChat(String chat) {
        Message message = new Message(DAO.selectedGroup.getGID(), Configuration.getConfig().myProfile.getUsername(), System.currentTimeMillis(), new Date().toString(), chat);
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
                messageListView.getItems().add(message);
                messageTextField.setText("");
            }
        } catch (IOException e) {
            log.error("Error unable to send message {}:{}", config.serverIP, config.serverPort);
        }
    }

    @FXML
    void micButton(ActionEvent event) {
        DAO.currentVideoCallConfig.setAudioEnabled(!DAO.currentVideoCallConfig.getAudioEnabled());
        synchronized (this) {
            DAO.currentVideoCallConfig.setAudioEnabled(!DAO.currentVideoCallConfig.getAudioEnabled());
            VideoManager.audioEnabled = !VideoManager.audioEnabled;
        }
        if (DAO.currentVideoCallConfig.getAudioEnabled()) {
            try {
                VideoManager.socket = new Socket(DAO.currentVideoCallConfig.getIp(), VideoManager.vInit.getPort() + 1);
                VideoManager.outputStream = VideoManager.socket.getOutputStream();
                VideoManager.inputStream = VideoManager.socket.getInputStream();
            } catch (IOException e) {
                log.error("Unable to connect to audio server");
            }

        } else {
            try {
                VideoManager.socket.close();
            } catch (IOException e) {
                log.error("Unable to close audio client socket");
            }
        }
        log.info("Microphone {}", DAO.currentVideoCallConfig.getAudioEnabled());
    }

    @FXML
    void presentButton(ActionEvent event) {
        DAO.currentVideoCallConfig.setPresentEnabled(true);
        DAO.currentVideoCallConfig.setVideoEnabled(false);
        VideoManager.presentEnabled = true;
        VideoManager.videoEnabled = false;
        log.info("Present {}", DAO.currentVideoCallConfig.getPresentEnabled());
    }

    @FXML
    void videoButton(ActionEvent event) {
        synchronized (this) {
            DAO.currentVideoCallConfig.setVideoEnabled(!DAO.currentVideoCallConfig.getVideoEnabled());
            VideoManager.videoEnabled = !VideoManager.videoEnabled;
            if (VideoManager.videoEnabled && VideoManager.presentEnabled) {
                VideoManager.presentEnabled = false;
                DAO.currentVideoCallConfig.setPresentEnabled(false);
            }
        }
        log.info("Video {}", DAO.currentVideoCallConfig.getVideoEnabled());
    }

    @FXML
    void settingsButton(ActionEvent event) {
        chatVisible = !chatVisible;
        setChatVisiblity(chatVisible);
    }

    void setChatVisiblity(boolean visiblity) {
        messageListView.setVisible(visiblity);
        fileButton.setVisible(visiblity);
        messageTextField.setVisible(visiblity);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initCameraNodeList();
        new Thread(() -> {
            Configuration config = Configuration.getConfig();
            try {
                Gson gson = new Gson();
                Packet requestPacket = new Packet();
                requestPacket.call = "Database.getChat";
                requestPacket.data = gson.toJson(DAO.selectedGroup, Group.class);
                String request = gson.toJson(requestPacket);
                String res = NetManager.getNetManager().send(request, config.serverIP, config.serverPort);
                log.debug("Request json: {}", res);
                Packet responsePacket = gson.fromJson(res, Packet.class);
                MessageList messageList = gson.fromJson(responsePacket.data, MessageList.class);
                messageListView.getItems().clear();
                for (Message message : messageList.getChats()) {
                    messageListView.getItems().add(message);
                }
            } catch (IOException e) {
                log.error("Error unable to load chat {}:{} {}", config.serverIP, config.serverPort, DAO.selectedGroup);
            }
        }).start();
        setChatVisiblity(chatVisible);
        Dimension dimension = new Dimension(DAO.videoWidth, DAO.videoHeight);
        log.info("Video setup dimension :{}", dimension);
        clientAgent = new StreamClientAgent(new StreamFrameListenerIMPL(videoImageView), dimension);
        clientAgent.connect(new InetSocketAddress(DAO.currentVideoCallConfig.getIp(), DAO.currentVideoCallConfig.getPort()));
        log.info("Connected to {} {}", DAO.currentVideoCallConfig.getIp(), DAO.currentVideoCallConfig.getPort());
        videoManager = new VideoManager(DAO.currentVideoCallConfig);
        videoManager.startVM();
        log.info("Started VM");
    }

    private void initCameraNodeList() {
        JFXButton btnMenu = new JFXButton("Menu");
        JFXButton btnOption1 = new JFXButton("Option 1");
        JFXButton btnOption2 = new JFXButton("Option 2");

        JFXButton btnCollapse = new JFXButton("<<");
        btnCollapse.setTooltip(new Tooltip("Collapse menu"));
        btnCollapse.setOnAction(e -> nodeList.animateList(false));

        nodeList.addAnimatedNode(btnMenu);
        nodeList.addAnimatedNode(btnOption1);
        nodeList.addAnimatedNode(btnOption2);
        nodeList.addAnimatedNode(btnCollapse);
    }

    protected static class StreamFrameListenerIMPL implements StreamFrameListener {
        private volatile long count = 0;
        ObjectProperty<javafx.scene.image.Image> imageObjectProperty;
        boolean start = false;
        ImageView imageView;

        public StreamFrameListenerIMPL(ImageView videoImageView) {
            imageView = videoImageView;
        }

        void start() {
            log.info("Image property set");
            ObjectProperty<javafx.scene.image.Image> imageProperty = new SimpleObjectProperty<Image>();
            imageView.imageProperty().bind(imageProperty);
            //this.imageObjectProperty=imageObjectProperty;
        }

        @Override
        public void onFrameReceived(BufferedImage image) {
            log.info("frame received :{}", count++);
            if (!start) {
                start = true;
//                start();
            }
            log.info("frame image :{}", image);
            imageView.setImage(SwingFXUtils.toFXImage(image, null));
            updateImage(imageView, image);
        }

        private void updateImage(ImageView videoImageView, BufferedImage image) {
            try {
                if (image != null) {
                    final Image img = SwingFXUtils.toFXImage(image, null);
                    //imageObjectProperty.set(img);
                    image.flush();
                    log.info("set {}", count);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}

@Slf4j
class VideoManager implements Runnable {

    public volatile static boolean state = false;
    DatagramSocket clientSocket = null;
    Webcam webcam;
    volatile static boolean videoEnabled;
    volatile static boolean audioEnabled;
    volatile static boolean presentEnabled;
    AudioFormat format = getAudioFormat();
    DataLine.Info speakerInfo = new DataLine.Info(SourceDataLine.class, format);
    SourceDataLine speaker;
    public static Socket socket;
    public static OutputStream outputStream;
    public static InputStream inputStream;
    public static VideoInitPacket vInit = null;


    DataLine.Info micInfo = new DataLine.Info(TargetDataLine.class, format);
    TargetDataLine mic;

    {
        try {
            mic = (TargetDataLine) AudioSystem.getLine(micInfo);
            mic.open(format);
            mic.start();
            speaker = (SourceDataLine) AudioSystem.getLine(speakerInfo);
            speaker.open(format);
            speaker.start();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }


    VideoManager(VideoCallConfig callConfig) {
        this.videoEnabled = callConfig.getVideoEnabled();
        this.audioEnabled = callConfig.getAudioEnabled();
        this.presentEnabled = callConfig.getPresentEnabled();
    }

    public void stopVM() {
        try {
            clientSocket.close();
            webcam.close();
        } catch (Exception e) {
            log.error("Unable to close connection and camera may be already closed");
        }
        state = false;
        log.error("STATEFALSE: {}",state);
        speaker.drain();
        speaker.close();
//        mic.drain();
        mic.close();
        log.info("VM Stopped ");
    }

    public void startVM() {
        state = true;
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        Socket socket = null;
        clientSocket = null;
        try {
            socket = new Socket(DAO.currentVideoCallConfig.getIp(), DAO.currentVideoCallConfig.getPort() - 1);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            vInit = new VideoInitPacket(-1, -1, null, "new");
            log.info("Query new user");
            objectOutputStream.writeObject(vInit);
            objectOutputStream.flush();
            vInit = (VideoInitPacket) objectInputStream.readObject();
            log.info("Got id={} and port={}", vInit.getId(), vInit.getPort());
            socket.close();
        } catch (IOException | ClassNotFoundException e) {
            log.error("Unable to connect to server {} {}", DAO.currentVideoCallConfig.getIp(), DAO.currentVideoCallConfig.getPort() - 1);
            return;
        }
        try {
            clientSocket = new DatagramSocket();//DAO.currentVideoCallConfig.ip,vInit.port);
        } catch (IOException e) {
            log.error("Unable to connect to UDP server at {} {}", DAO.currentVideoCallConfig.getIp(), vInit.getPort());
            return;
        }
        log.info("UDP Socket initialized");
        try {
            webcam = Webcam.getDefault();
        } catch (WebcamException e) {
            webcam = null;
            log.error("WEBCAM LOCKED");
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InetAddress IPAddress = null;
        try {
            IPAddress = InetAddress.getByName(DAO.currentVideoCallConfig.getIp());
        } catch (UnknownHostException e) {
            log.error("Unable to found {}", DAO.currentVideoCallConfig.getIp());
        }
        new Thread(() -> {
            while (state) {
                byte[] data = new byte[512];
                int bytesRead;
                try {
                    log.info("Audio receive started");
                    if(inputStream == null) {
                        log.error("input Stream is null");
                    } else {
                        if ((bytesRead = inputStream.read(data)) != -1) {
                            speaker.write(data, 0, bytesRead);
                            log.info("Audio receive {}", bytesRead);
                        }
                    }
                } catch (IOException e) {
                    log.error("Unable to read audio from input stream");
                }
                try {
                    if (audioEnabled) {
                        if (this.socket == null) {
                            VideoManager.socket = new Socket(DAO.currentVideoCallConfig.getIp(), VideoManager.vInit.getPort() + 1);
                            VideoManager.outputStream = VideoManager.socket.getOutputStream();
                            VideoManager.inputStream = VideoManager.socket.getInputStream();
                        }
                        byte tmpBuff[] = new byte[mic.getBufferSize() / 5];
                        int count = mic.read(tmpBuff, 0, tmpBuff.length);
                        if (count > 0) {
                            outputStream.write(tmpBuff, 0, count);
                            log.info("Audio Send {}", count);
                        }
                    }
                } catch (UnknownHostException e) {
                    log.error("Socket host unknown");
                } catch (IOException e) {
                    log.error("Unable to send to audio server");
                }
            }
        }).start();
        while (state) {
            BufferedImage bufferedImage = null;
            try {
                if (presentEnabled) {
                    bufferedImage = Utility.getDesktop();
                    if (bufferedImage == null)
                        bufferedImage = ImageIO.read(new File(DAO.testJPGImage));
                } else {
                    if (DAO.webcamLocked) {
                        bufferedImage = ImageIO.read(new File(DAO.testJPGImage));
                    } else {
                        if (videoEnabled) {
                            bufferedImage = webcam.getImage();
                            log.info("Video image captured");
                        } else {
                            bufferedImage = ImageIO.read(new File(DAO.testJPGImage));
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Video capture Error e {}", e.getMessage());
            }
            if (IPAddress != null) {
                try {
                    if (bufferedImage == null) {
                        log.error("Buffered image is null");
                    } else {

                        ImageIO.write(bufferedImage, "jpg", baos);
                        baos.flush();
                        byte[] buffer = baos.toByteArray();
                        log.info("Sending Buffer length:{} :: {} {}", buffer.length, IPAddress, vInit.getPort());
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, IPAddress, vInit.getPort());
                        clientSocket.send(packet);
                        baos = new ByteArrayOutputStream();
                    }
                } catch (IOException e) {
                    log.error("Unable to write image to UDP's output stream + e{}", e.getMessage());
                }
            }
            log.info("State: {}",state);
        }

    }

    private AudioFormat getAudioFormat() {
        float sampleRate = 16000.0F;
        int sampleSizeBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = true;
        return new AudioFormat(sampleRate, sampleSizeBits, channels, signed, bigEndian);
    }
}