package mapster.server;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

import mapster.messages.*;

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
    //File processing
    private ArrayList<Info> fileList;
    private HashMap<String, ArrayList<Info>> map;

    //***************main methods ***************************************
    //Initialize streams that reads from the port
    private void initializeVariable(){
        try{
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            in = new ObjectInputStream(inputStream);
            out = new ObjectOutputStream(outputStream);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    private String getCMDLineArguements(){
        String cmdLine = "Connect to port: " + port;
        return cmdLine;
    }
    private void initializeSockets(int port){
        try{
            this.port = port;
            server = new ServerSocket(port);
            System.out.println("Server Started");
        }
        catch(IOException e){
            System.out.println("Could not listen on port: " + port);
            System.exit(1);
        }
    }
    //***************main methods ***************************************

    //***************File methods ***************************************
    private void readFile_and_buildMap(){
        try{
            File file = new File("src/mapster/server/key.txt");
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while((line = br.readLine()) != null){
                //read info
                String[] split = line.split(",");
                String keyword = split[0];
                String name = split[1];
                String ip = split[2];
                int port = Integer.parseInt(split[3]);
                Info info = new Info(keyword,name, ip, port);
                //add to file list
                fileList.add(info);
                //add to map
                if(map.containsKey(keyword)){
                    map.get(keyword).add(info);
                }
                else{
                    ArrayList<Info> list = new ArrayList<Info>();
                    list.add(info);
                    map.put(keyword, list);
                }
            }
            br.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    private void exportFile(){
        try{
            File file = new File("src/mapster/server/keyword.txt");
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            for(Info info : fileList){
                String line = info.getKey() + "," + info.getName() + "," + info.getIp() + "," + info.getPort();
                bw.write(line);
                bw.newLine();
            }
            bw.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    private ArrayList<Info> searchFilesbyKey(String key){
        ArrayList<Info> list = map.get(key);
        return list;
    }
    private Info searchFilebyName(String name){
        for(Info info : fileList){
            if(info.getName().equals(name)){
                return info;
            }
        }
        return null;
    }
    private void printList(ArrayList<Info> list){
        for(Info info : list){
            System.out.println(info.toString());
        }
    }
    //***************File methods ***************************************
    private void commandLoop(){
        try{
            while(true){
                //Accept a connection
                socket = server.accept();
                initializeVariable();
                //Get the command line arguments
                String cmdLine = getCMDLineArguements();
                //Send the command line arguments to the client
                out.flush();
                out.writeObject(cmdLine);
                out.flush();
                //Get the response from the client
                Object message = in.readObject();
                System.out.println(message.toString());
                if(message instanceof JoinMessage){
                    JoinMessage command = (JoinMessage)message;
                    System.out.printf("Received JoinMessage client port: %d.", ((JoinMessage)message).getPort());
                }
                else if(message instanceof LeaveMessage){
                    LeaveMessage command = (LeaveMessage)message;
                    System.out.printf("Received LeaveMessage");
                    break;
                }
                else if(message instanceof PublishMessage){
                    PublishMessage command = (PublishMessage)message;
                    System.out.printf("Received PublishMessage");
                }
                else if(message instanceof SearchMessage){
                    SearchMessage command = (SearchMessage)message;
                    System.out.printf("Received SearchMessage");
                    ArrayList<Info> list = searchFilesbyKey(command.getKeyword());
                    printList(list);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    private void closeSockets(){
        try{
            in.close();
            out.close();
            socket.close();
            server.close();
            inputStream.close();
            outputStream.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    //constructor
    //input: port #
    public Server(int port) {
        initializeSockets(port);
        //initialize the map and list
        fileList = new ArrayList<Info>();
        map = new HashMap<String, ArrayList<Info>>();
        readFile_and_buildMap();
    }
    //Public methods Section
    public void startServer(){
        commandLoop();
    }
    public void closeServer(){
        closeSockets();
        exportFile();
    }
    //End of Public methods Section
    public static void main(String[] args) throws IOException, ClassNotFoundException{
        Server server = new Server(5050);
        server.startServer();
        server.closeServer();
    }
}
