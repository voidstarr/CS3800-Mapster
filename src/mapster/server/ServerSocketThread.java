package mapster.server;

import mapster.messages.ResultMessage;

import javax.net.ssl.SSLServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ServerSocketThread extends Thread {
    SSLServerSocket server;
    ConcurrentHashMap<String, ArrayList<ResultMessage.Result>> map;

    //Constructor
    public ServerSocketThread(SSLServerSocket server, ConcurrentHashMap<String, ArrayList<ResultMessage.Result>> map) {
        this.server = server;
        this.map = map;
    }

    @Override
    public void run() {
        super.run();
        try {
            while (true) {
                //Accept a connection
                Socket socket = server.accept();
                ClientHandlerThread clientThread = new ClientHandlerThread(socket, map);
                clientThread.start();
            }
        } catch (Exception e) {
            //e.printStackTrace();
            // silence exceptions on close
        }
    }
}
