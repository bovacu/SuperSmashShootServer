package main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerManager {

    static {
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
        } catch (ClassNotFoundException e) {
            System.out.println("No puedo cargar el driver JDBC de la BD");
        }
    }

    private final String COMMANDS[] = {"CONNECT", "DISCONNECT", "REGISTER", "FRIENDS", "ADD FRIEND", "REQUESTS", "PARTY"};
    private final int PORT = 6767;
    private final int MAX_THREADS = 50;

    private ExecutorService pool;
    private Connection connection;
    private final String url="jdbc:ucanaccess://D:/GamesProjects/Java/SuperSmashShootServer/DataBase.accdb";
    private ServerSocket server;

    private boolean loop;

    public ServerManager(){
        try {
            this.server = new ServerSocket(this.PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.pool = Executors.newFixedThreadPool(this.MAX_THREADS);
        this.loop = true;
    }

    public void runServer(){
        while(this.loop){

            Socket client;

            try{
                client = this.server.accept();
                this.pool.execute(new ClientSpeaker(client, url));

            } catch (IOException e){
                e.printStackTrace();
            }
        }

        try {
            if(this.connection != null)
                this.connection.close();
            if(this.server != null)
                this.server.close();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }

        this.pool.shutdown();
    }

    private void addTaskToRequestPool(Socket socket, DataInputStream is, String request){
        ServerAction serverAction = null;

        try {

            DataOutputStream os = new DataOutputStream(socket.getOutputStream());

            if(request.equals(this.COMMANDS[0])){
                serverAction = new Connections(socket, is, os, connection, is.readLine(), is.readLine());
            }else if(request.equals(this.COMMANDS[1])){
                serverAction = new Disconnections(socket, is, os,connection, is.readLine());
            }else if(request.equals(this.COMMANDS[2])){
                serverAction = new Registration(socket, is, os, connection, is.readLine(), is.readLine());
            }else if((request.equals(this.COMMANDS[3]))){
                serverAction = new Friends(socket, is, os, connection, is.readLine());
            }else if((request.equals(this.COMMANDS[4]))){
                serverAction = new AddFriend(socket, is, os, connection, is.readLine(), is.readLine());
            }else if((request.equals(this.COMMANDS[5]))){
                serverAction = new FriendRequests(socket, is, os, connection, is.readLine());
            }else if((request.equals(this.COMMANDS[6]))){
                serverAction = new SendPartyRequest(socket, is, os, connection, is.readLine(), is.readLine());
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }

    }
}
