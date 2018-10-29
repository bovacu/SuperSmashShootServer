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

    private final String REQUESTS[] = { "CLOSE",                        //0
                                        "FRIEND LIST",                  //1
                                        "CONNECT",                      //2
                                        "REGISTER",                     //3
                                        "FRIEND REQUEST",               //4
                                        "PARTY REQUEST",                //5
                                        "SEND PARTY REQUEST",           //6
                                        "SEND MESSAGE",                 //7
                                        "SEND PARTY INVITATION",        //8
                                        "CREATE PARTY",                 //9
                                        "JOIN PARTY",                   //10
                                        "ADD FRIEND"                    //11
    };

    private Socket socketOfSpeaker, socketOfListener;
    private DataInputStream inputOfSpeaker, inputOfListener;
    private DataOutputStream outputOfSpeaker, outputOfListener;
    private Connection connection;
    private String userName;
    private JLabel label;
    private JFrame frame;

    ClientSpeaker(Socket socketOfSpeaker, Socket socketOfListener,  String url, JLabel label, JFrame frame){
        this.socketOfSpeaker = socketOfSpeaker;
        this.socketOfListener = socketOfListener;
        this.label = label;
        this.frame = frame;

        try {
            this.connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this.frame, e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
        }

        try {
            this.inputOfSpeaker = new DataInputStream(socketOfSpeaker.getInputStream());
            this.outputOfSpeaker = new DataOutputStream(socketOfSpeaker.getOutputStream());

            this.inputOfListener = new DataInputStream(socketOfListener.getInputStream());
            this.outputOfListener = new DataOutputStream(socketOfListener.getOutputStream());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this.frame, e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
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
                        ServerManager.numberOfPLayers--;
                        this.label.setText("Number of players: " + ServerManager.numberOfPLayers);
                        this.label.revalidate();
                        this.label.repaint();
                        ServerManager.currentPlayers.remove(this);
                        Disconnections d = new Disconnections(this.socketOfSpeaker, this.inputOfSpeaker, this.outputOfSpeaker, this.connection, this.userName);
                        d.run();
                        this.outputOfSpeaker.writeBytes("CLOSE OK" + "\r\n");
                        this.outputOfListener.writeBytes("CLOSE OK" + "\r\n");
                        this.outputOfSpeaker.flush();
                        this.outputOfListener.flush();
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
                                c.writeInstantAction(toSend);
                                break;
                            }
                    }

                    else if(requests.equals(this.REQUESTS[7])){
                        String infoSent = this.inputOfSpeaker.readLine();

                        String name;
                        while(!(name = this.inputOfSpeaker.readLine()).equals("END")) {
                            for (ClientSpeaker c : ServerManager.currentPlayers) {
                                if (c.getName().equals(name)) {
                                    List<String> toSend = new ArrayList<>();
                                    toSend.add("RECEIVE MESSAGE");
                                    toSend.add(this.userName);
                                    toSend.add(infoSent);
                                    c.writeInstantAction(toSend);
                                }
                            }
                        }
                        this.outputOfSpeaker.writeBytes("MESSAGE SENT" + "\r\n");
                        this.outputOfSpeaker.flush();
                    }

                    else if(requests.equals(this.REQUESTS[8])){
                        String host = this.inputOfSpeaker.readLine();
                        String guest = this.inputOfSpeaker.readLine();
                        boolean found = false;
                        for(ClientSpeaker c : ServerManager.currentPlayers)
                            if(c.getName().equals(guest)) {
                                found = true;
                                SendPartyRequest spr = new SendPartyRequest(this.socketOfSpeaker, this.inputOfSpeaker,
                                        this.outputOfSpeaker, c.outputOfListener, this.connection, host, guest);
                                spr.run();
                                break;
                            }

                            if(!found)
                                this.outputOfSpeaker.writeBytes("INVITATION OFFLINE" + "\r\n");
                    }

                    else if(requests.equals(this.REQUESTS[9])){
                        CreateParty c = new CreateParty(this.socketOfSpeaker, this.inputOfSpeaker, this.outputOfSpeaker, this.connection, this.userName);
                        c.run();
                        this.outputOfSpeaker.writeBytes("PARTY CREATED" + "\r\n");
                        this.outputOfSpeaker.flush();
                    }

                    else if(requests.equals(this.REQUESTS[10])){
                        String host = this.inputOfSpeaker.readLine();
                        JoinParty c = new JoinParty(this.socketOfSpeaker, this.inputOfSpeaker, this.outputOfSpeaker, this.connection, this.userName, host);
                        c.run();
                    }

                    else if(requests.equals(this.REQUESTS[11])){
                        String friend = this.inputOfSpeaker.readLine();
                        AddFriend af = new AddFriend(this.socketOfSpeaker, this.inputOfSpeaker, this.outputOfSpeaker, this.connection, this.userName, friend);
                        af.run();
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
                    this.inputOfListener.close();
                    this.outputOfListener.close();
                    this.socketOfListener.close();
                } catch (IOException e1) {
                    JOptionPane.showMessageDialog(this.frame, e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
                }
                break;
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                JOptionPane.showMessageDialog(this.frame, e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
            }

        }

        try {
            this.socketOfSpeaker.close();
            this.inputOfSpeaker.close();
            this.outputOfSpeaker.close();
            this.inputOfListener.close();
            this.outputOfListener.close();
            this.socketOfListener.close();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this.frame, e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void writeInstantAction(List<String> commands){
        for(String s : commands){
            try {
                this.outputOfListener.writeBytes(s + "\r\n");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this.frame, e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
            }
        }

        try {
            this.outputOfListener.flush();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this.frame, e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
        }
    }
}
