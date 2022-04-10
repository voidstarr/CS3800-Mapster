package mapster.server;
import java.net.*;
import java.io.*;

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
    public static void main(String[] args) {
        Server server = new Server(5000);
    }
}
