package main;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientSpeaker extends Thread {

    private final String REQUESTS[] = {"CLOSE", "FRIEND LIST", "CONNECT", "REGISTER", "FRIEND REQUEST", "PARTY REQUEST",
    "SEND PARTY REQUEST"};

    private Socket socketOfSpeaker, socketOfListener;
    private DataInputStream inputOfSpeaker, inputOfListener;
    private DataOutputStream outputOfSpeaker, outputOfListener;
    private Connection connection;
    private String userName;
    private JLabel label;

    ClientSpeaker(Socket socketOfSpeaker, Socket socketOfListener,  String url, JLabel label){
        this.socketOfSpeaker = socketOfSpeaker;
        this.socketOfListener = socketOfListener;
        this.label = label;

        try {
            this.connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            this.inputOfSpeaker = new DataInputStream(socketOfSpeaker.getInputStream());
            this.outputOfSpeaker = new DataOutputStream(socketOfSpeaker.getOutputStream());

            this.inputOfListener = new DataInputStream(socketOfListener.getInputStream());
            this.outputOfListener = new DataOutputStream(socketOfListener.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while(true){
            try {
                String requests = this.inputOfSpeaker.readLine();
                System.out.println(requests);
                if(requests != null){
                    if(requests.equals(this.REQUESTS[0])) {
                        this.outputOfSpeaker.writeBytes("CLOSE OK" + "\r\n");
                        ServerManager.numberOfPLayers--;
                        System.out.println(ServerManager.numberOfPLayers);
                        this.label.setText("Number of players: " + ServerManager.numberOfPLayers);
                        this.label.revalidate();
                        this.label.repaint();
                        ServerManager.currentPlayers.remove(this);
                        break;
                    }

                    else if(requests.equals(this.REQUESTS[1])){
                        this.outputOfSpeaker.writeBytes("SENDING FRIEND LIST" + "\r\n");
                        Friends f = new Friends(this.socketOfSpeaker, this.inputOfSpeaker, this.outputOfSpeaker, this.connection, this.userName);
                        f.run();
                    }

                    else if(requests.equals(this.REQUESTS[2])){
                        String usr = this.inputOfSpeaker.readLine();
                        String psw = this.inputOfSpeaker.readLine();
                        this.userName = usr;
                        super.setName(this.userName);
                        Connections c = new Connections(this.socketOfSpeaker, this.inputOfSpeaker, this.outputOfSpeaker, this.connection, usr, psw);
                        c.run();
                    }

                    else if(requests.equals(this.REQUESTS[3])){
                        String usr = this.inputOfSpeaker.readLine();
                        String psw = this.inputOfSpeaker.readLine();
                        Registration r = new Registration(this.socketOfSpeaker, this.inputOfSpeaker, this.outputOfSpeaker, this.connection, usr, psw);
                        r.run();
                    }

                    else if(requests.equals(this.REQUESTS[4])){
                        this.outputOfSpeaker.writeBytes("SENDING REQUEST LIST" + "\r\n");
                        FriendRequests fr = new FriendRequests(this.socketOfSpeaker, this.inputOfSpeaker, this.outputOfSpeaker, this.connection, this.userName);
                        fr.run();
                    }

                    else if(requests.equals(this.REQUESTS[5])){
                        this.outputOfSpeaker.writeBytes("SENDING PARTY REQUESTS" + "\r\n");
                        PartyRequests pr = new PartyRequests(this.socketOfSpeaker, this.inputOfSpeaker, this.outputOfSpeaker, this.connection, this.userName);
                        pr.run();
                    }

                    else if(requests.equals(this.REQUESTS[6])){
                        String partyRequestReceiver = this.inputOfSpeaker.readLine();
                        for(ClientSpeaker c : ServerManager.currentPlayers)
                            if(c.getName().equals(partyRequestReceiver)) {
                                List<String> toSend = new ArrayList<>();
                                toSend.add("PARTY REQUEST SENT");
                                toSend.add(this.userName);
                                c.writeInstantAction(c, toSend);
                                break;
                            }
                    }

                    else{
                        this.outputOfSpeaker.writeBytes("NAN" + "\r\n");
                        this.outputOfSpeaker.flush();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    this.inputOfSpeaker.close();
                    this.outputOfSpeaker.close();
                    this.socketOfSpeaker.close();
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
            this.socketOfSpeaker.close();
            this.inputOfSpeaker.close();
            this.outputOfSpeaker.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeInstantAction(ClientSpeaker c, List<String> commands){
        for(String s : commands){
            try {
                c.outputOfListener.writeBytes(s + "\r\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            c.outputOfListener.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
