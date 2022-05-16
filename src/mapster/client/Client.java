package mapster.client;

import mapster.messages.*;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

public class Client {

    public static String sharedFolderLocation = "./client_shared_folder/";
    public static String keywordsFile = "keywords.txt";

    static Scanner keyboardInput;

    static SSLContext sslctx = createContext();
    static SSLSocketFactory sslSocketFactory = sslctx.getSocketFactory();
    static SSLServerSocketFactory sslServerSocketFactory = sslctx.getServerSocketFactory();

    static String serverAddress;
    static SSLSocket serverSocket;
    static int serverPort;
    static int clientPort;

    static ObjectOutputStream serverOutputStream;
    static ObjectInputStream serverInputStream;

    static FileService fileServiceThread;
    static HashMap<String, ArrayList<String>> keywordMap = new HashMap<>(); // keyword,List of filenames
    static ConcurrentLinkedQueue<String> messagesToFileService = new ConcurrentLinkedQueue<>();

    public static void main(String[] args) throws IOException {
        initializeVariables();
        getCmdLineArguments(args);
        fileServiceThread = new FileService(clientPort, messagesToFileService);
        fileServiceThread.start();
        readKeywordFile();
        serviceLoop();
    }

    /*
    * Load keyword file from disk and store in map
    * */
    private static void readKeywordFile() {
        try (Stream<String> stream = Files.lines(Paths.get(sharedFolderLocation + keywordsFile))) {
            stream.forEach(str -> {
                String[] in = str.split(","); // keyword,filename
                if (keywordMap.containsKey(in[0])) {
                    keywordMap.get(in[0]).add(in[1]);
                } else {
                    ArrayList<String> list = new ArrayList<>();
                    list.add(in[1]);
                    keywordMap.put(in[0], list);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    * When supplied arguments, decipher what they mean.
    * */
    public static void getCmdLineArguments(String[] args) {
        if (args.length != 3 && args.length != 4) {
            printUsage();
            System.exit(-1);
        }
        serverAddress = args[0];
        serverPort = Integer.parseInt(args[1]);
        clientPort = Integer.parseInt(args[2]);
        if (args.length == 4)
            sharedFolderLocation = args[3];
    }

    /*
    * Don't know what to do? Ask for help.
    * */
    public static void printUsage() {
        System.out.println("Usage: ./client <server addr> <server port> <client port> <shared folder>");
        System.out.println("\t<server addr> Specify IP address or name of server to connect to.");
        System.out.println("\t<server port> Specify port of server to connect to.");
        System.out.println("\t<client port> Specify port that the client will listen to for file downloading.");
        System.out.println("\t<shared folder> Where files will be downloaded/uploaded from");
    }

    private static void initializeVariables() {
        keyboardInput = new Scanner(System.in);
    }

    /*
    * Read user input and handle it
    * */
    private static void serviceLoop() {
        while (true) {
            if (keyboardInput.hasNextLine()) {
                commandService(keyboardInput.nextLine());
            }
        }
    }

    /*
     * Decipher user input into commands
     * */
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
                handleDownload(cmd);
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

    /*
    * When we quit, tell other threads to stop
    * tell the server we're leaving
    * and halt the process
    * */
    private static void handleQuit() {
        messagesToFileService.add("stop");
        handleLeave();
        System.exit(0);
    }

    /*
    * Tell the server we're leaving
    * Close open sockets and streams
    * */
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

    /*
    * Download specified file from specified client
    * */
    private static void handleDownload(String[] cmd) {
        String fileName = cmd[1];
        String ipAddress = cmd[2];
        int port = Integer.parseInt(cmd[3]);
        try {
            //Establish a connection to a peer

            SSLSocket downloadSocket = (SSLSocket) sslSocketFactory.createSocket(ipAddress, port);
            downloadSocket.setUseClientMode(true);

            ObjectOutputStream downloadOutStream = new ObjectOutputStream(downloadSocket.getOutputStream());
            downloadOutStream.flush();
            ObjectInputStream downloadInStream = new ObjectInputStream(downloadSocket.getInputStream());
            //Send the file name to the peer
            downloadOutStream.writeObject(new DownloadMessage(fileName));

            //Receive the file from the peer
            DownloadResponseMessage downloadResponseMessage = (DownloadResponseMessage) downloadInStream.readObject();

            //Save file
            Files.write(Paths.get(sharedFolderLocation + downloadResponseMessage.getFileName()), downloadResponseMessage.getFileContent());

            //Put the keyword and file name in keyword.txt
            if (keywordMap.containsKey("")) {
                keywordMap.get("").add(downloadResponseMessage.getFileName());
            } else {
                ArrayList<String> list = new ArrayList<>();
                list.add(downloadResponseMessage.getFileName());
                keywordMap.put("", list);
            }

            //Publish this file to the server
            serverOutputStream.writeObject(new PublishMessage("", downloadResponseMessage.getFileName()));

            // close connection to the other client
            downloadOutStream.close();
            downloadInStream.close();
            downloadSocket.close();
            System.out.printf("Finished downloading %s from %s%n", downloadResponseMessage.getFileName(), ipAddress);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /*
    * Send search message to server and handle response
    * */
    private static void handleSearch(String[] keywords) {
        try {
            //Send keyword to server
            serverOutputStream.writeObject(new SearchMessage(String.join(" ", Arrays.copyOfRange(keywords, 1, keywords.length))));
            //Receive results
            ResultMessage resultMessage = (ResultMessage) serverInputStream.readObject();
            // TODO: still not done yet
            //Display results
            if (resultMessage.getResults() != null)
                for (ResultMessage.Result r : resultMessage.getResults()) {
                    System.out.println(r);
                }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Something went wrong when trying to disconnect from the server.");
        }
    }

    /*
    * Tell the server we've joined the network and send the port on which clients can connect to us
    * */
    private static void handleJoin() {
        try {
            serverSocket = (SSLSocket) sslSocketFactory.createSocket(serverAddress, serverPort);
            serverSocket.setUseClientMode(true);
            //Establish a TCP connection to server
            serverOutputStream = new ObjectOutputStream(serverSocket.getOutputStream());
            serverInputStream = new ObjectInputStream(serverSocket.getInputStream());
            //Send listening port to server
            serverOutputStream.writeObject(new JoinMessage(clientPort));
            serverOutputStream.flush();
        } catch (IOException e) {
            System.out.println("Unable to connect to the server. Check Server IP and port.");
            e.printStackTrace();
        }
    }

    /*
     * Push a list of local keyword:filename pairs to the server
     */
    private static void handlePublish() {
        try (Stream<String> stream = Files.lines(Paths.get(sharedFolderLocation + keywordsFile))) {
            stream.forEach(str -> {
                String[] in = str.split(","); // keyword,filename
                if (keywordMap.containsKey(in[0])) {
                    keywordMap.get(in[0]).add(in[1]);
                } else {
                    ArrayList<String> list = new ArrayList<>();
                    list.add(in[1]);
                    keywordMap.put(in[0], list);
                }
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

    /*
     * Don't know what to do? Ask for help.
     */
    private static void printHelp() {
        System.out.println("Command\tFormat\tFunction");
        System.out.println("join\tjoin\testablish a connection with and send <client port> to the server");
        System.out.println("publish\tpublish keyword file\tsend the server publish messages containing its shared file information");
        System.out.println("search\tsearch keyword\tquery the server with a keyword");
        System.out.println("download\tdownload file IP Port\task the client to get file from its peers indicated by (IP, Port) pair");
        System.out.println("leave\tleave\task the client to disconnect from the server");
        System.out.println("quit\tquit\task the client to quit");
    }

    /*
    * Create the SSLContext in order to create SSLSockets
    * */
    private static SSLContext createContext() {
        SSLContext ssl_ctx = null;
        try {
            final KeyStore key_store = KeyStore.getInstance("PKCS12");

            final KeyStore trust_store = KeyStore.getInstance("PKCS12");

            final char[] passphrase = "password".toCharArray();

            key_store.load(new FileInputStream("./keystore"), passphrase);
            trust_store.load(new FileInputStream("./truststore"), passphrase);

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(key_store, passphrase);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(trust_store);

            ssl_ctx = SSLContext.getInstance("TLSv1.3");
            ssl_ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException | IOException | KeyManagementException e) {
            e.printStackTrace();
        }
        return ssl_ctx;
    }

}