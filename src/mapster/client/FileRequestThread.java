package mapster.client;

import mapster.messages.DownloadMessage;
import mapster.messages.DownloadResponseMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileRequestThread extends Thread {

    Socket socket;
    ObjectInputStream in;
    ObjectOutputStream out;
    DownloadMessage receivedMessage;

    public FileRequestThread(DownloadMessage receivedMessage, Socket socket, ObjectInputStream in, ObjectOutputStream out) throws IOException {
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.receivedMessage = receivedMessage;
    }

    @Override
    public void run() {
        try {
            // read in file to send
            byte[] content = Files.readAllBytes(Paths.get(Client.sharedFolderLocation + receivedMessage.getFileName()));
            // send file
            out.writeObject(new DownloadResponseMessage(receivedMessage.getFileName(), content));
            // clean up
            out.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
