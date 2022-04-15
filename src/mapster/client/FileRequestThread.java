package mapster.client;

import mapster.messages.DownloadMessage;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class FileRequestThread extends Thread {

    Socket socket;
    ObjectInputStream in;
    ObjectOutputStream out;
    DownloadMessage receivedMessage;

    public FileRequestThread(Socket socket) {
        this.socket = socket;
    }

    public FileRequestThread(DownloadMessage receivedMessage, ObjectInputStream in, ObjectOutputStream out) {
        this.in = in;
        this.out = out;
        this.receivedMessage = receivedMessage;
    }

    @Override
    public void run() {

    }

    private static void handleFileRequest() {
        //Receive the file name from the peer
        //Send the file size to the peer
        //Send the whole file to the peer (in multiple messages)
    }
}
