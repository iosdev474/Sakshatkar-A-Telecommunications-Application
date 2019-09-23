package main;

import container.message.Message;
import container.others.Group;
import container.profile.User;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.manager.NetManager;

import java.io.Serializable;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

/**
 * <code>Configuration</code> stored at <code>DAO.configurationLocation</code>
 * and is used to store <code>User</code>'s settings as well as connection settings
 * of server
 *
 * Also <code>Configuration</code> is a singleton class
 */
@Slf4j
@Getter
public class Configuration implements Serializable {
    private static Configuration config;

    @Setter public User myProfile = new User();
    public String serverIP;
    public int serverPort;
    public String localIP;
    public int localPort;
    public boolean loggedIn;
    public HashMap<Integer, Group> myGroups;
    public HashMap<Integer, List<Message>> messages;

    Configuration() {
        myProfile.setUsername("");
        myProfile.setEmail("");
        serverIP = "192.168.0.162";
        loggedIn = false;
        myGroups = new HashMap<>();
        messages = new HashMap<>();
        setIP();
        localPort= NetManager.getNetManager().getPort();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter Server port:");
        serverPort = scanner.nextInt();//todo undo DAO.defaultPort;
    }

    void setIP(){
        try {
            DatagramSocket socket = new DatagramSocket();
            socket.connect(InetAddress.getByName("8.8.8.8"), 9911);
            localIP = socket.getLocalAddress().getHostAddress();
            socket.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    /**
     * Used to Load configuration from local system
     * @return Configuration loaded from local system stored at <code>DAO.configurationLocation</code>
     */
    private static Configuration loadConfiguration() {
        return (Configuration) Utility.load(DAO.configurationLocation);
    }

    /**
     * Used to store configuration on local system
     * @return true when configuration file is stores successfully
     */
    public boolean saveConfiguration() {
        return Utility.save(DAO.configurationLocation, this);
    }

    /**
     * get singleton object of <code>Configuration</code>
     * @return singleton configuration object
     */
    public static Configuration getConfig() {
        if (config == null) {
            config = loadConfiguration();
            if (config == null) {
                log.warn("No Configuration Found. New Configuration created");
                config = new Configuration();
//        todo        config.saveConfiguration();
            } else {
                log.info("Configuration loaded");
            }
        }
        return config;
    }
}
