package mapster.server;

import mapster.messages.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandlerThread extends Thread {
    ConcurrentHashMap<String, ArrayList<ResultMessage.Result>> map;
    Socket clientSocket;
    ObjectOutputStream out;
    ObjectInputStream in;
    int clientListeningPort;

    public Socket getClientSocket() {
        return clientSocket;
    }

    public int getClientListeningPort() {
        return clientListeningPort;
    }

    //Constructor
    public ClientHandlerThread(Socket clientSocket, ConcurrentHashMap<String, ArrayList<ResultMessage.Result>> map) throws IOException {
        this.map = map;
        this.clientSocket = clientSocket;
        this.out = new ObjectOutputStream(clientSocket.getOutputStream());
        this.out.flush();
        this.in = new ObjectInputStream(clientSocket.getInputStream());
    }

    @Override
    public void run() {
        super.run();
        try {
            while (true) {
                //Get the response from the client
                Object message = in.readObject();
                System.out.println(message);
                //Perform different actions based on the Message type
                if (message instanceof JoinMessage) {
                    JoinMessage command = (JoinMessage) message;
                    clientListeningPort = command.getPort();
                    System.out.printf("Received JoinMessage client port: %d.%n", command.getPort());
                } else if (message instanceof LeaveMessage) {
                    LeaveMessage command = (LeaveMessage) message;
                    for (Map.Entry<String, ArrayList<ResultMessage.Result>> entry : map.entrySet()) {
                        entry.getValue().removeIf(result -> result.getIpAddress().equals(clientSocket.getInetAddress().getHostAddress()));
                        if (entry.getValue().size() == 0) map.remove(entry.getKey());
                    }
                    System.out.printf("Received LeaveMessage %s%n", command);
                    break;
                } else if (message instanceof PublishMessage) {
                    PublishMessage command = (PublishMessage) message;
                    System.out.printf("Received PublishMessage %s%n", command);
                    ResultMessage.Result received = new ResultMessage.Result(clientSocket.getInetAddress().getHostAddress(), clientListeningPort, command.getFileName());
                    if (map.get(command.getKeyword().toLowerCase()) != null) {
                        map.get(command.getKeyword().toLowerCase()).add(received);
                    } else {
                        ArrayList<ResultMessage.Result> list = new ArrayList<>();
                        list.add(received);
                        map.put(command.getKeyword(), list);
                    }
                } else if (message instanceof SearchMessage) {
                    SearchMessage command = (SearchMessage) message;
                    System.out.printf("Received SearchMessage %s%n", command);
                    ArrayList<ResultMessage.Result> list = searchFilesByKey(command.getKeyword());
                    out.writeObject(new ResultMessage(list));
                } else {
                    System.out.printf("Unable to determine type of message.%n%s", message.toString());
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    //Search the map for the keyword
    private ArrayList<ResultMessage.Result> searchFilesByKey(String key) {
        return map.get(key);
    }
}
