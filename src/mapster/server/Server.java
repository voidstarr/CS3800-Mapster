package mapster.server;

import mapster.messages.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//create a server that listens for connections
public class Server {
    //Standard info for server
    private Socket socket;
    private ServerSocket server;
    private int port;
    //Streams for reading and writing
    private InputStream inputStream;
    private OutputStream outputStream;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private int clientListeningPort;
    //File processing
    private HashMap<String, ArrayList<ResultMessage.Result>> map;

    //***************main methods ***************************************
    //Initialize streams that reads from the port
    private void initializeVariable() {
        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            out = new ObjectOutputStream(outputStream);
            in = new ObjectInputStream(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getCMDLineArguments() {
        String cmdLine = "Connect to port: " + port;
        return cmdLine;
    }

    private void initializeSockets(int port) {
        try {
            this.port = port;
            server = new ServerSocket(port);
            System.out.println("Server Started");
        } catch (IOException e) {
            System.out.println("Could not listen on port: " + port);
            System.exit(1);
        }
    }
    //***************main methods ***************************************

    //***************File methods ***************************************
    private void readFile_and_buildMap() {
        try {
            File file = new File("src/mapster/server/key.txt");
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                //read info
                String[] split = line.split(",");
                String keyword = split[0];
                String name = split[1];
                String ip = split[2];
                int port = Integer.parseInt(split[3]);
                ResultMessage.Result result = new ResultMessage.Result(ip, port, name);
                //add to map
                if (map.containsKey(keyword)) {
                    map.get(keyword).add(result);
                } else {
                    ArrayList<ResultMessage.Result> list = new ArrayList<>();
                    list.add(result);
                    map.put(keyword, list);
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void exportFile() {
        try {
            File file = new File("src/mapster/server/keyword.txt");
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            HashMap<String, Integer> asdf = new HashMap<>();
            for (Map.Entry<String, ArrayList<ResultMessage.Result>> entry : map.entrySet()) {
                for (ResultMessage.Result info : entry.getValue()) {
                    String line = entry.getKey() + "," + info.getFileName() + "," + info.getIpAddress() + "," + info.getPort();
                    if (!asdf.containsKey(line)) {
                        asdf.put(line, 1324);
                        bw.write(line);
                        bw.newLine();
                    }
                }
            }
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ArrayList<ResultMessage.Result> searchFilesbyKey(String key) {
        ArrayList<ResultMessage.Result> list = map.get(key);
        return list;
    }

    private ResultMessage.Result searchFilebyName(String name) {
        for (ArrayList<ResultMessage.Result> fileList : map.values()) {
            for (ResultMessage.Result result : fileList) {
                if (result.getFileName().equals(name)) {
                    return result;
                }
            }
        }
        return null;
    }

    private void printList(ArrayList<ResultMessage.Result> list) {
        for (ResultMessage.Result result : list) {
            System.out.println(result.toString());
        }
    }

    //***************File methods ***************************************
    private void commandLoop() {
        try {
            //Accept a connection
            socket = server.accept();
            initializeVariable();
            //Get the command line arguments
            String cmdLine = getCMDLineArguments();
            while (true) {
                //Get the response from the client
                Object message = in.readObject();
                if (message instanceof JoinMessage) {
                    JoinMessage command = (JoinMessage) message;
                    clientListeningPort = command.getPort();
                    System.out.printf("Received JoinMessage client port: %d.%n", command.getPort());
                } else if (message instanceof LeaveMessage) {
                    LeaveMessage command = (LeaveMessage) message;
                    System.out.printf("Received LeaveMessage %s%n", command);
                    break;
                } else if (message instanceof PublishMessage) {
                    PublishMessage command = (PublishMessage) message;
                    System.out.printf("Received PublishMessage %s%n", command);
                    ResultMessage.Result received = new ResultMessage.Result(socket.getRemoteSocketAddress().toString(), clientListeningPort, command.getFileName());
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
                    ArrayList<ResultMessage.Result> list = searchFilesbyKey(command.getKeyword());
                    out.writeObject(new ResultMessage(list));
                } else {
                    System.out.printf("Unable to determine type of message.%n%s", message.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeSockets() {
        try {
            in.close();
            out.close();
            socket.close();
            server.close();
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //constructor
    //input: port #
    public Server(int port) {
        initializeSockets(port);
        //initialize the map
        map = new HashMap<>();
        readFile_and_buildMap();
    }

    //Public methods Section
    public void startServer() {
        commandLoop();
    }

    public void closeServer() {
        closeSockets();
        exportFile();
    }

    //End of Public methods Section
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Server server = new Server(5050);
        server.startServer();
        server.closeServer();
    }
}
