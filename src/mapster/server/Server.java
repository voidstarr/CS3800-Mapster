package mapster.server;

import mapster.messages.ResultMessage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//create a server that listens for connections
public class Server {
    //Standard info for server
    private ArrayList<Socket> clientSockets = new ArrayList<>();
    private ServerSocket server;
    private int port;
    //Streams for reading and writing

    private int clientListeningPort;
    //File processing
    private ConcurrentHashMap<String, ArrayList<ResultMessage.Result>> map = new ConcurrentHashMap<>();

    //***************main methods ***************************************
    //Initialize streams that reads from the port
    private void initializeVariable() {
        try {

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
            File file = new File("src/mapster/server/key.txt");
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
            while (true) {
                //Accept a connection
                Socket socket = server.accept();
                clientSockets.add(socket);
                ClientHandlerThread clientThread = new ClientHandlerThread(socket, map);
                clientThread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeSockets() {
        try {
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //constructor
    //input: port #
    public Server(int port) {
        //Get the command line arguments
        String cmdLine = getCMDLineArguments();
        initializeSockets(port);
        //initialize the map
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
