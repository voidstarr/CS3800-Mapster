package mapster.server;
import java.net.*;
import java.io.*;

import mapster.messages.*;

//create a server that listens for connections
public class Server {
    private Socket socket;
    private ServerSocket server;
    private int port;
    private DataInputStream in;

    private void initializeVariable(){
        port = 0;
    }
    private String getCMDLineArguements(){
        return "";
    }
    private void initializeSockets(){
        try{
            server = new ServerSocket(port);
            socket = server.accept();
            in = new DataInputStream(socket.getInputStream());
        }catch(IOException e){
            System.out.println("Error: " + e);
        }
    }
    private void commandLoop(){
        String cmd = "";
        while(!cmd.equals("exit")){
            try{
                cmd = in.readUTF();
                System.out.println(cmd);
            }catch(IOException e){
                System.out.println("Error: " + e);
            }
        }
    }

    public Server(int port) {
        try{
            this.port = port;
            server = new ServerSocket(port);
            System.out.println("Server Started");

            socket = server.accept();
            System.out.println("Client Connected");

            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

            String line = "";
            while(!line.equals("exit")) {
                try{
                    line = in.readUTF();
                    System.out.println(line);
                }
                catch(IOException i){
                    System.out.println(i);
                }
            }
            System.out.println("Closing connection");

            //close connection
            socket.close();
            in.close();
        }
        catch(IOException e){
            System.out.println("Could not listen on port: " + port);
            System.exit(1);
        }
    }
    public static void main(String[] args) throws IOException, ClassNotFoundException{
        ServerSocket ss = new ServerSocket(5050);
        System.out.println("ServerSocket awaiting connections...");
        Socket socket = ss.accept(); // blocking call, this will wait until a connection is attempted on this port.
        System.out.println("Connection from " + socket + "!");

        // get the input stream from the connected socket
        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();
        // create a DataInputStream so we can read data from it.
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        // https://stackoverflow.com/questions/8088557/getinputstream-blocks
        objectOutputStream.flush(); // THIS IS IMPORTANT AFTER EVERY OPERATION WITH A ObjectOutputStream
        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

        // read the list of messages from the socket
        Object message = objectInputStream.readObject();
        System.out.println(message.toString());
        if(message instanceof JoinMessage)
        System.out.printf("Received JoinMessage client port: %d.", ((JoinMessage)message).getPort());
        ss.close();
        socket.close();
    }
}
