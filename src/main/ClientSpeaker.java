package main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.*;

public class ClientSpeaker implements Runnable {

    private final String REQUESTS[] = {"CLOSE", "FRIEND LIST", "CONNECT", "REGISTER"};

    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;
    private boolean close;
    private Connection connection;
    private String userName;

    public ClientSpeaker(Socket socket, String url){
        this.socket = socket;
        this.close = false;

        try {
            this.connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            this.input = new DataInputStream(socket.getInputStream());
            this.output = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while(true){
            try {
                String requests = this.input.readLine();
                System.out.println(requests);
                if(requests != null){
                    if(requests.equals(this.REQUESTS[0])) {
                        this.output.writeBytes("CLOSE OK" + "\r\n");
                        break;
                    }

                    else if(requests.equals(this.REQUESTS[1])){
                        this.output.writeBytes("SENDING FRIEND LIST" + "\r\n");
                        Friends f = new Friends(this.socket, this.input, this.output, this.connection, this.userName);
                        f.run();
                    }

                    else if(requests.equals(this.REQUESTS[2])){
                        String usr = this.input.readLine();
                        String psw = this.input.readLine();
                        this.userName = usr;
                        Connections c = new Connections(this.socket, this.input, this.output, this.connection, usr, psw);
                        c.run();
                    }

                    else if(requests.equals(this.REQUESTS[3])){
                        String usr = this.input.readLine();
                        String psw = this.input.readLine();
                        Registration r = new Registration(this.socket, this.input, this.output, this.connection, usr, psw);
                        r.run();
                    }

                    else{
                        this.output.writeBytes("NAN" + "\r\n");
                        this.output.flush();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    this.input.close();
                    this.output.close();
                    this.socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                break;
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        try {
            this.socket.close();
            this.input.close();
            this.output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
