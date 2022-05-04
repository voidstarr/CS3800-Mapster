package mapster.client;

import mapster.messages.DownloadMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FileService extends Thread {

    ServerSocketChannel listeningSocket;
    ConcurrentLinkedQueue<String> messagesFromMainThread;
    boolean running = true;

    public FileService(int clientPort, ConcurrentLinkedQueue<String> messagesFromMainThread) throws IOException {
        listeningSocket = ServerSocketChannel.open();
        listeningSocket.configureBlocking(false);
        listeningSocket.socket().bind(new InetSocketAddress(clientPort));
        this.messagesFromMainThread = messagesFromMainThread;
    }

    @Override
    public void run() {
        SocketChannel incomingClientConnection;
        while (running) {
            String messageFromMainThread = messagesFromMainThread.poll();
            if (messageFromMainThread != null) {
                running = !messageFromMainThread.equals("stop");
            }
            try {
                if ((incomingClientConnection = listeningSocket.accept()) != null) {
                    System.out.printf("Received connection from %s%n", incomingClientConnection.getRemoteAddress().toString());
                    ObjectOutputStream out = new ObjectOutputStream(incomingClientConnection.socket().getOutputStream());
                    out.flush();
                    ObjectInputStream in = new ObjectInputStream(incomingClientConnection.socket().getInputStream());
                    Object received = in.readObject();
                    if (received instanceof DownloadMessage) {
                        new FileRequestThread((DownloadMessage) received, incomingClientConnection, in, out).start();
                    } else {
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
