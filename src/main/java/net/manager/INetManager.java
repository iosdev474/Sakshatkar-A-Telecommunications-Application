package net.manager;

import java.io.IOException;
import java.net.Socket;

public interface INetManager {
    void ping();
    int getPort();
    void setPort(int port);
    void onReceive(Socket clientSocket) throws IOException;
    String send(String data, String ip, int port) throws IOException;
    void stop();
}
