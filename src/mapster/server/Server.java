package mapster.server;

import mapster.messages.ResultMessage;

import javax.net.ssl.*;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

//create a server that listens for connections
public class Server {

    static SSLContext sslctx = createContext();
    static SSLServerSocketFactory sslServerSocketFactory = sslctx.getServerSocketFactory();

    //Standard info for server
    private SSLServerSocket server;
    private int port;


    private int clientListeningPort;
    //File processing
    private ConcurrentHashMap<String, ArrayList<ResultMessage.Result>> map = new ConcurrentHashMap<>();

    Scanner keyboardInput;

    //***************main methods ***************************************

    private String getCMDLineArguments() {
        String cmdLine = "Connect to port: " + port;
        return cmdLine;
    }

    //Initialize Server Socket 
    private void initializeSockets(int port) {
        try {
            this.port = port;
            server = (SSLServerSocket) sslServerSocketFactory.createServerSocket(port);
            server.setUseClientMode(false);
            System.out.println("Server Started");
        } catch (IOException e) {
            System.out.println("Could not listen on port: " + port);
            System.exit(1);
        }
    }
    //***************main methods ***************************************

    //***************File methods ***************************************
    
    //Legacy feature for saving files shared on the server.
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

    //Save the files on the server
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

    //Additional search file by name on the server feature that is not listed in the project description
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

    //print out the files on the server for debug purposes
    private void printList(ArrayList<ResultMessage.Result> list) {
        for (ResultMessage.Result result : list) {
            System.out.println(result.toString());
        }
    }

    //Main method to read keyboard input and process the command
    private void commandLoop() {
        while (true) {
            if (keyboardInput.hasNextLine()) {
                commandService(keyboardInput.nextLine());
            }
        }
    }

    //enable the server to perform "print" and "quit"
    private void commandService(String command) {
        String[] cmd = command.split(" ");
        switch (cmd[0]) {
            case "print":
                handlePrint();
                break;
            case "quit":
                closeServer();
            default:
                System.out.println("Invalid command.");
                break;
        }
    }

    //Print out all the files on the server
    private void handlePrint() {
        for (ArrayList<ResultMessage.Result> results : map.values()) {
            for (ResultMessage.Result result : results) {
                System.out.printf("%s\t%s:%d%n", result.getFileName(), result.getIpAddress(), result.getPort());
            }
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
        keyboardInput = new Scanner(System.in);
        new ServerSocketThread(server, map).start();
        commandLoop();
    }

    public void closeServer() {
        closeSockets();
        exportFile();
        System.exit(0);
    }

    //End of Public methods Section
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Server server = new Server(5050);
        server.startServer();
        server.closeServer();
    }

    //SSL
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
