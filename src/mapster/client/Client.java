package mapster.client;

import java.util.Scanner;

public class Client {

    static boolean running = true;
    static Scanner keyboardInput;

    public static void main(String[] args) {
        initializeVariables();
        serviceLoop();
    }

    private static void initializeVariables() {
        keyboardInput = new Scanner(System.in);
    }

    private static void serviceLoop() {
        while (running) {
            if (keyboardInput.hasNextLine()) {
                commandService(keyboardInput.nextLine());
            }
            // if download request received
            // fileService();
        }
    }

    private static void fileService() {
        //if download request received
        //then Issue connection for the request
        //handleFileRequest()
    }

    private static void handleFileRequest() {
        //Receive the file name from the peer
        //Send the file size to the peer
        //Send the whole file to the peer (in multiple messages)
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
        //Establish a TCP connection to server
        //Send listening port to server
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