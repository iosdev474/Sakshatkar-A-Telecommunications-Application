package net.video;

import container.others.State;
import container.others.VideoInitPacket;
import container.profile.User;
import javafx.util.Pair;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import main.Configuration;
import main.DAO;
import net.video.agent.StreamServerAgent;
import org.apache.commons.lang3.RandomStringUtils;
import sun.security.krb5.Config;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class VideoManager implements Runnable {

    public int port;
    @Getter
    private String password;
    StreamServerAgent serverAgent;
    ServerSocket serverSocket;
    Map<Integer, User> userMap = new HashMap<Integer, User>();
    public Map<Integer, State> images = new HashMap<Integer, State>();
    public Map<Integer, Pair<byte[], Integer>> audios = new HashMap<>();
    int id = 1;

    enum STATE {
        RUNNING,
        NOT_RUNNING
    }

    volatile STATE status = STATE.NOT_RUNNING;

    public VideoManager(int port) {
        this.port = port;
        password = RandomStringUtils.randomAlphabetic(10);
        Dimension dimension = new Dimension(DAO.videoWidth, DAO.videoHeight);
        serverAgent = new StreamServerAgent(dimension, this);
        try {
            serverSocket = new ServerSocket(port - 1);
            log.info("Starting a TCP server at {}", port - 1);
        } catch (IOException e) {
            log.error("Unable to create TCP server");
            return;
        }
//        Thread tt = new Thread(() -> {
//            while(status == STATE.RUNNING) {
//                try {
//                    log.info("Combine {}",images.size());
//                    combinedBufferedImage = ;
//                } catch (NoSuchElementException e) {
//                    log.info("no images");
//                }
//            }
//        });
//        tt.start();
//        log.info("Combine image thread started");
        start();
        log.info("VM Started");
    }

    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }

    void startUDPServer(int port) {
        int pp = this.port;
        Thread thread = new Thread(() -> {
            log.info("Starting udp server at {}", port);
            try {
                DatagramSocket datagramSocket = new DatagramSocket(port);
                byte[] receive = new byte[65535];
                DatagramPacket recievePacket = null;
                while (status == STATE.RUNNING) {
//                        log.info("Receive packet start");
                    recievePacket = new DatagramPacket(receive, receive.length);
                    datagramSocket.receive(recievePacket);
                    byte[] buff = recievePacket.getData();

                    ByteArrayInputStream bis = new ByteArrayInputStream(buff);
                    BufferedImage bImage = ImageIO.read(bis);

                    InetAddress ipaddress = recievePacket.getAddress();
                    log.debug("UDP SERVER: Recieve packet from ID={}  {}:{}", port - pp, ipaddress.toString(), recievePacket.getPort());
                    images.put(port - pp, new State(bImage));
                    receive = new byte[65535];
//                        log.info("Receive packet end");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
        log.info("UDP SERVER THREAD STARTED");
    }

    private AudioFormat getAudioFormat() {
        float sampleRate = 16000.0F;
        int sampleSizeBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = true;

        return new AudioFormat(sampleRate, sampleSizeBits, channels, signed, bigEndian);
    }

    void startAudioServer(int port) {
        final ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            log.error("Unable to start Audio Server");
            return;
        }
        AudioFormat format = getAudioFormat();
        int pp = this.port;
        Thread audioRecieveThread = new Thread(() -> {
            try {
                while (status == STATE.RUNNING) {
                    Socket socket = serverSocket.accept();
                    OutputStream outputStream = socket.getOutputStream();
                    InputStream inputStream = socket.getInputStream();
                    while (true) {
                        byte[] data = new byte[512];
                        int bytesRead = 0;
                        if ((bytesRead = inputStream.read(data)) != -1) {
//                            log.info("Bytes read {}",bytesRead);
                            audios.put(port, new Pair<>(data, bytesRead));
                        }
                        int mean = 0;
                        int[] combinedAudio = new int[512];
                        for (Map.Entry<Integer, Pair<byte[], Integer>> audioData : audios.entrySet()) {
                            if (audioData.getKey() == port)
                                continue;
                            else {
                                mean++;
                                for (int i = 0; i < combinedAudio.length && i < audioData.getValue().getValue(); i++) {
                                    combinedAudio[i] += audioData.getValue().getKey()[i];
                                }
                            }
                        }
                        byte[] finalAudio = new byte[512];
                        for (int i = 0; i < combinedAudio.length; i++) {
                            if(mean!=0)
                                finalAudio[i] = (byte) (combinedAudio[i] / mean);
                            else
                                ;
                            System.out.print(finalAudio[i]+" ");
                        }
                        log.info("Audio inserted for {}", port);
//                        log.info("{}",finalAudio[0]);
                        outputStream.write(finalAudio);
                        log.info("Send Final Audio to {}", port);
                        Thread.sleep(10);
                    }
                }
            } catch (IOException | InterruptedException e) {
                log.error("Client disconnected");
                audios.remove(port);
            }
        });
        audioRecieveThread.start();
        log.info("Audio Server STARTED");
    }

    public void start() {
        serverAgent.start(new InetSocketAddress(Configuration.getConfig().getServerIP(), port));
        status = STATE.RUNNING;
        new Thread(this).start();
    }

    public void stop() {
        serverAgent.stop();
        status = STATE.NOT_RUNNING;
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (status == STATE.RUNNING) {
            try {
                log.info("Waiting for client");
                Socket socket = serverSocket.accept();
                log.info("Client connected");
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                VideoInitPacket vInit = (VideoInitPacket) objectInputStream.readObject();
                if (vInit.getQuery().equals("new")) {
                    log.info("New user try id={} port={}", id, port + id);
                    userMap.put(id, vInit.getUser());
                    vInit.setId(id);
                    startUDPServer(port + id);
                    startAudioServer(port + id + 1);
                    vInit.setPort(port + id);
                    id += 2;
                } else if (vInit.getQuery().equals("get")) {
                    log.info("Old user try id={} port={}", id, port + id);
                    boolean flag = true;
                    for (Integer id : userMap.keySet()) {
                        if (userMap.get(id).getUsername().equals(vInit.getUser().getUsername())) {
                            vInit.setId(id);
                            flag = false;
                            break;
                        }
                    }
                    if (flag)
                        vInit.setPort(-1);
                }
                objectOutputStream.writeObject(vInit);
                objectOutputStream.flush();
                objectOutputStream.close();
                objectInputStream.close();
                socket.close();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
    /*




      BufferedImage bImage = ImageIO.read(new File("sample.jpg"));
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ImageIO.write(bImage, "jpg", bos );
      byte [] data = bos.toByteArray();
      ByteArrayInputStream bis = new ByteArrayInputStream(data);
      BufferedImage bImage2 = ImageIO.read(bis);
      ImageIO.write(bImage2, "jpg", new File("output.jpg") );
      System.out.println("image created");




            BufferedImage bufferedImage = webcam.getImage();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                ImageIO.write(bufferedImage, "jpg", byteArrayOutputStream);
            } catch (IOException e) {
                log.error("Unable to write buffered image to byte array");
            }
            OutputStream outputStream = null;
            try {
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                log.error("Unable to get output stream");
            }
            byte[] size = ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array();
            try {
                outputStream.write(size);
                outputStream.write(byteArrayOutputStream.toByteArray());
                outputStream.flush();
            } catch (IOException e) {
                log.error("Unable to write image");
            }
            log.info("Image sent to server");
*/
