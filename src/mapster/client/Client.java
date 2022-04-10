package mapster.client;

import mapster.messages.DownloadMessage;
import mapster.messages.JoinMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class Client {

    static boolean running = true;

    static Scanner keyboardInput;

    static String serverAddress;
    static int serverPort;
    static int clientPort;

    static ObjectOutputStream serverOutputStream;
    static ObjectInputStream serverInputStream;

    static ServerSocketChannel listeningSocketChannel;
    static SocketChannel incomingClientConnection;

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        initializeVariables();
        getCmdLineArguments(args);
        listenToNetwork();
        serviceLoop();
    }

    private static void listenToNetwork() throws IOException {
        listeningSocketChannel = ServerSocketChannel.open();
        listeningSocketChannel.configureBlocking(false);
        listeningSocketChannel.bind(new InetSocketAddress(clientPort));
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

    private static void serviceLoop() throws IOException, ClassNotFoundException {
        while (running) {
            if (keyboardInput.hasNextLine()) {
                commandService(keyboardInput.nextLine());
            }
            if((incomingClientConnection = listeningSocketChannel.accept()) != null) {
                fileService();
            }
        }
    }

    private static void fileService() throws IOException, ClassNotFoundException {
        System.out.printf("Received connection from %s%n", incomingClientConnection.getRemoteAddress().toString());
        ObjectInputStream in = new ObjectInputStream(incomingClientConnection.socket().getInputStream());
        Object received = in.readObject();
        if(received instanceof DownloadMessage) {

        } else {
            in.close();
            incomingClientConnection.close();
        }
        //if download request received
        //then Issue connection for the request
        //handleFileRequest()
    }

    private static void handleFileRequest() {
        //Receive the file name from the peer
        //Send the file size to the peer
        //Send the whole file to the peer (in multiple messages)
    }

    private static void commandService(String command) throws IOException {
        String[] cmd = command.split(" ");
        switch (cmd[0]) {
            case "join":
                handleJoin();
                break;
            case "publish":
                handlePublish();
                break;
            case "search":
                handleSearch();
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
            default:
                System.out.println("Invalid command.");
                break;
        }
    }

    private static void handleQuit() {
        handleLeave();
    }

    private static void handleLeave() {
        // ?
    }

    private static void handleDownload() {
        //Establish a connection to a peer
        //Send the file name to the peer
        //Receive the file size from the peer
        //Receive the whole file from the peer (in multiple messages)
        //Put the keyword and file name in keyword.txt
        //Publish this file to the server
    }

    private static void handleSearch() {
        //Send keyword to server
        //Receive the number of results N from server
        //Receive N pairs of IP addresses and port from server
    }

    private static void handleJoin() {
        try {
            //Establish a TCP connection to server
            Socket socket = new Socket(serverAddress, serverPort);
            serverOutputStream = new ObjectOutputStream(socket.getOutputStream());
            serverInputStream = new ObjectInputStream(socket.getInputStream());
            //Send listening port to server
            serverOutputStream.writeObject(new JoinMessage(clientPort));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handlePublish() {
        //while Not the end of keyword file
        //Read in a keyword and file name pair
        //Send this pair to server
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