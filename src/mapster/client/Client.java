package mapster.client;

import mapster.messages.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

public class Client {

    static boolean running = true;

    static Scanner keyboardInput;

    static String serverAddress;
    static Socket serverSocket;
    static int serverPort;
    static int clientPort;

    static ObjectOutputStream serverOutputStream;
    static ObjectInputStream serverInputStream;

    static FileService fileServiceThread;

    static ConcurrentLinkedQueue<String> messagesToFileService = new ConcurrentLinkedQueue<>();

    public static void main(String[] args) throws IOException {
        initializeVariables();
        getCmdLineArguments(args);
        fileServiceThread = new FileService(clientPort, messagesToFileService);
        fileServiceThread.start();
        serviceLoop();
    }

    public static void getCmdLineArguments(String[] args) {
        if (args.length != 3) {
            printUsage();
            System.exit(-1);
        }
        serverAddress = args[0];
        serverPort = Integer.parseInt(args[1]);
        clientPort = Integer.parseInt(args[2]);
    }

    public static void printUsage() {
        System.out.println("Usage: ./client <server addr> <server port> <client port>");
        System.out.println("\t<server addr> Specify IP address or name of server to connect to.");
        System.out.println("\t<server port> Specify port of server to connect to.");
        System.out.println("\t<client port> Specify port that the client will listen to for file downloading.");
    }

    private static void initializeVariables() {
        keyboardInput = new Scanner(System.in);
    }

    private static void serviceLoop() {
        while (running) {
            if (keyboardInput.hasNextLine()) {
                commandService(keyboardInput.nextLine());
            }
        }
    }

    private static void commandService(String command) {
        String[] cmd = command.split(" ");
        switch (cmd[0]) {
            case "join":
                handleJoin();
                break;
            case "publish":
                handlePublish();
                break;
            case "search":
                handleSearch(cmd);
                break;
            case "download":
                handleDownload();
                break;
            case "leave":
                handleLeave();
                break;
            case "quit":
                handleQuit();
                break;
            case "help":
                printHelp();
                break;
            default:
                System.out.println("Invalid command.");
                break;
        }
    }

    private static void handleQuit() {
        handleLeave();
    }

    private static void handleLeave() {
        try {
            serverOutputStream.writeObject(new LeaveMessage());
            serverOutputStream.flush();
            serverOutputStream.close();
            serverInputStream.close();
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("Something went wrong when trying to disconnect from the server.");
        }
    }

    private static void handleDownload() {
        //Establish a connection to a peer
        //Send the file name to the peer
        //Receive the file size from the peer
        //Receive the whole file from the peer (in multiple messages)
        //Put the keyword and file name in keyword.txt
        //Publish this file to the server
    }

    private static void handleSearch(String[] keywords) {
        try {
            serverOutputStream.writeObject(new SearchMessage(String.join(" ", Arrays.copyOfRange(keywords, 1, keywords.length))));
            ResultMessage resultMessage = (ResultMessage) serverInputStream.readObject();
            // TODO: still not done yet
            for (ResultMessage.Result r : resultMessage.getResults()) {
                System.out.println(r);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Something went wrong when trying to disconnect from the server.");
        }
        //Send keyword to server
        //Receive the number of results N from server
        //Receive N pairs of IP addresses and port from server
    }

    private static void handleJoin() {
        try {
            //Establish a TCP connection to server
            serverSocket = new Socket(serverAddress, serverPort);
            serverOutputStream = new ObjectOutputStream(serverSocket.getOutputStream());
            serverInputStream = new ObjectInputStream(serverSocket.getInputStream());
            //Send listening port to server
            serverOutputStream.writeObject(new JoinMessage(clientPort));
            serverOutputStream.flush();
        } catch (IOException e) {
//            e.printStackTrace();
            System.out.println("Unable to connect to the server. Check Server IP and port.");
        }
    }

    private static void handlePublish() {
        try (Stream<String> stream = Files.lines(Paths.get("./client_shared_folder/client_keywords.txt"))) {
            stream.forEach(str -> {
                String[] in = str.split(","); // keyword,filename
                try {
                    serverOutputStream.writeObject(new PublishMessage(in[0], in[1]));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printHelp() {
        System.out.println("Command\tFormat\tFunction");
        System.out.println("join\tjoin\testablish a connection with and send <client port> to the server");
        System.out.println("publish\tpublish keyword file\tsend the server publish messages containing its shared file information");
        System.out.println("search\tsearch keyword\tquery the server with a keyword");
        System.out.println("download\tdownload file IP Port\task the client to get file from its peers indicated by (IP, Port) pair");
        System.out.println("leave\tleave\task the client to disconnect from the server");
        System.out.println("quit\tquit\task the client to quit");
    }
}