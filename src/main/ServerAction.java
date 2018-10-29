package main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public abstract class ServerAction {

    protected DataInputStream input;
    protected DataOutputStream output;
    protected Socket socket;

    public ServerAction(Socket socket, DataInputStream input, DataOutputStream output){
        this.socket = socket;
        this.input = input;
        this.output = output;
    }

    public void close(){
        try {
            this.input.close();
            this.output.close();
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public abstract void run();
}
