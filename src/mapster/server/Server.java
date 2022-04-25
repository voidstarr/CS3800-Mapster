package mapster.server;
import java.net.*;
import java.io.*;

import mapster.messages.*;

//create a server that listens for connections
public class Server {
    private Socket socket;
    private ServerSocket server;
    private int port;
    //Streams for reading and writing
    private InputStream inputStream;
    private OutputStream outputStream;
    private ObjectInputStream in;
    private ObjectOutputStream out;

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
    private void commandLoop(){
        try{
            while(true){
                //Accept a connection
                socket = server.accept();
                initializeVariable();
                //Get the command line arguments
                String cmdLine = getCMDLineArguements();
                //Send the command line arguments to the client
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
        commandLoop();
        closeSockets();
    }
    public void startServer(){
        commandLoop();
    }
    public static void main(String[] args) throws IOException, ClassNotFoundException{
        Server server = new Server(5050);
        server.startServer();
    }
}
