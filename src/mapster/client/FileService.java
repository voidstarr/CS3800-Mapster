package mapster.client;

import mapster.messages.DownloadMessage;

import javax.net.ssl.SSLServerSocket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FileService extends Thread {

    SSLServerSocket listeningSocket;
    ConcurrentLinkedQueue<String> messagesFromMainThread;
    boolean running = true;

    public FileService(int clientPort, ConcurrentLinkedQueue<String> messagesFromMainThread) throws IOException {
        listeningSocket = (SSLServerSocket) Client.sslServerSocketFactory.createServerSocket(clientPort);

        this.messagesFromMainThread = messagesFromMainThread;
    }

    @Override
    public void run() {
        Socket incomingClientConnection;
        while (running) {
            // should we continue listening for incoming connections?
            String messageFromMainThread = messagesFromMainThread.poll();
            if (messageFromMainThread != null) {
                running = !messageFromMainThread.equals("stop");
            }
            try {
                // when we receive an incomming connection
                if ((incomingClientConnection = listeningSocket.accept()) != null) {
                    System.out.printf("Received connection from %s%n", incomingClientConnection.getRemoteSocketAddress().toString());
                    // create in/out streams
                    ObjectOutputStream out = new ObjectOutputStream(incomingClientConnection.getOutputStream());
                    out.flush();
                    ObjectInputStream in = new ObjectInputStream(incomingClientConnection.getInputStream());
                    Object received = in.readObject();
                    // did we receive the correct message?
                    if (received instanceof DownloadMessage) {
                        // start a new thread to handle it
                        new FileRequestThread((DownloadMessage) received, incomingClientConnection, in, out).start();
                    } else {
                        // otherwise, close
                        in.close();
                        out.close();
                        incomingClientConnection.close();
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
