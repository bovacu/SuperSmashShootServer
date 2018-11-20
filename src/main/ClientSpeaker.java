package main;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientSpeaker extends Thread {

    public enum CharacterType{SOLDIER, CLOWN, PIRATE, KNIGHT}

    private Socket socketOfSpeaker, socketOfListener;
    private DataInputStream inputOfSpeaker, inputOfListener;
    private DataOutputStream outputOfSpeaker, outputOfListener;
    private Connection connection;
    private String userName;
    private JLabel label;
    private JFrame frame;

    private DatagramSocket udpSpeaker, udpListener;
    private DatagramPacket udpPacketSpeaker, udpPacketListener;
    private byte messageReceived[], messageSent[];

    private boolean readyForFightF;
    private boolean playingMatchF;

    private List<ClientSpeaker> playersToSendInfo;

    private int clientPort;
    private String clientAddress;

    private CharacterType character;

    private int skin;

    ClientSpeaker(Socket socketOfSpeaker, Socket socketOfListener,  DatagramSocket udpSpeaker, String url, JLabel label, JFrame frame){
        this.socketOfSpeaker = socketOfSpeaker;
        this.socketOfListener = socketOfListener;
        this.label = label;
        this.frame = frame;
        this.readyForFightF = false;
        this.playingMatchF = false;
        this.playersToSendInfo = new ArrayList<>();

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

            this.messageReceived = new byte[256];
            this.messageSent = new byte[256];

            this.udpSpeaker = udpSpeaker;
            this.udpListener = new DatagramSocket();

            this.udpPacketSpeaker = new DatagramPacket(this.messageReceived, this.messageReceived.length);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this.frame, e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        boolean loop = true;

        while(loop){
            try {

                String requests;
                if(!this.playingMatchF){
                    requests = this.inputOfSpeaker.readLine();
                }else {
                    requests = "SEND PLAYER DATA PACKAGE";
                }

                if(requests.split(":").length > 1)
                    requests = requests.split(":")[0];

                if(requests != null){

                    switch (requests){
                        case "CLOSE" : {
                            ServerManager.numberOfPLayers--;
                            this.label.setText("Number of players: " + ServerManager.numberOfPLayers);
                            this.label.revalidate();
                            this.label.repaint();
                            ServerManager.currentPlayers.remove(this);
                            DisconnectFromGame d = new DisconnectFromGame(this.socketOfSpeaker, this.inputOfSpeaker, this.outputOfSpeaker, this.connection, this.userName);
                            d.run();
                            this.outputOfSpeaker.writeBytes("CLOSE OK" + "\r\n");
                            this.outputOfListener.writeBytes("CLOSE OK" + "\r\n");
                            this.outputOfSpeaker.flush();
                            this.outputOfListener.flush();

                            break;
                        }

                        case "FRIEND LIST" : {
                            this.outputOfSpeaker.writeBytes("SENDING FRIEND LIST" + "\r\n");
                            FriendList f = new FriendList(this.socketOfSpeaker, this.inputOfSpeaker, this.outputOfSpeaker, this.connection, this.userName);
                            f.run();

                            break;
                        }

                        case "CONNECT" : {
                            String usr = this.inputOfSpeaker.readLine();
                            String psw = this.inputOfSpeaker.readLine();
                            this.clientAddress = this.inputOfSpeaker.readLine();
                            this.clientPort = Integer.valueOf(this.inputOfSpeaker.readLine());
                            this.udpPacketListener = new DatagramPacket(this.messageSent, this.messageSent.length, InetAddress.getByName(this.clientAddress), this.clientPort);
                            this.userName = usr;
                            super.setName(this.userName);
                            ConnectToGame c = new ConnectToGame(this.socketOfSpeaker, this.inputOfSpeaker, this.outputOfSpeaker, this.connection, usr, psw);
                            c.run();

                            break;
                        }

                        case "REGISTER" : {
                            String usr = this.inputOfSpeaker.readLine();
                            String psw = this.inputOfSpeaker.readLine();
                            Registration r = new Registration(this.socketOfSpeaker, this.inputOfSpeaker, this.outputOfSpeaker, this.connection, usr, psw);
                            r.run();

                            break;
                        }

                        case "FRIEND REQUEST" : {
                            this.outputOfSpeaker.writeBytes("SENDING REQUEST LIST" + "\r\n");
                            FriendRequestList fr = new FriendRequestList(this.socketOfSpeaker, this.inputOfSpeaker, this.outputOfSpeaker, this.connection, this.userName);
                            fr.run();

                            break;
                        }

                        case "PARTY REQUEST" : {
                            this.outputOfSpeaker.writeBytes("SENDING PARTY REQUESTS" + "\r\n");
                            PartyRequestList pr = new PartyRequestList(this.socketOfSpeaker, this.inputOfSpeaker, this.outputOfSpeaker, this.connection, this.userName);
                            pr.run();

                            break;
                        }

                        case "SEND PARTY REQUEST" : {
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

                        case "SEND MESSAGE" : {
                            String infoSent = this.inputOfSpeaker.readLine();
                            String colors[] = {"[GREEN]", "[ORANGE]", "[PINK]"};
                            int colorCounter = 0;
                            String name;
                            while(!(name = this.inputOfSpeaker.readLine()).equals("END")) {
                                for (ClientSpeaker c : ServerManager.currentPlayers) {
                                    if (c.getName().equals(name)) {
                                        List<String> toSend = new ArrayList<>();
                                        toSend.add("RECEIVE MESSAGE");
                                        toSend.add(colors[colorCounter] + this.userName + "[]");
                                        toSend.add(infoSent);
                                        c.writeInstantAction(toSend);
                                        colorCounter++;
                                    }
                                }
                            }
                            this.outputOfSpeaker.writeBytes("MESSAGE SENT" + "\r\n");
                            this.outputOfSpeaker.flush();

                            break;
                        }

                        case "SEND PARTY INVITATION" : {
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

                            break;
                        }

                        case "CREATE PARTY" : {
                            CreateParty c = new CreateParty(this.socketOfSpeaker, this.inputOfSpeaker, this.outputOfSpeaker, this.connection, this.userName);
                            c.run();
                            this.outputOfSpeaker.writeBytes("PARTY CREATED" + "\r\n");
                            this.outputOfSpeaker.flush();

                            break;
                        }

                        case "JOIN PARTY" : {
                            String host = this.inputOfSpeaker.readLine();
                            JoinParty c = new JoinParty(this.socketOfSpeaker, this.inputOfSpeaker, this.outputOfSpeaker, this.connection, this.userName, host);
                            c.run();

                            break;
                        }

                        case "ADD FRIEND" : {
                            String friend = this.inputOfSpeaker.readLine();
                            AddFriend af = new AddFriend(this.socketOfSpeaker, this.inputOfSpeaker, this.outputOfSpeaker, this.connection, this.userName, friend);
                            af.run();

                            break;
                        }

                        case "ACCEPT FRIEND" : {
                            String friend = this.inputOfSpeaker.readLine();
                            AcceptFriend af = new AcceptFriend(this.socketOfSpeaker, this.inputOfSpeaker, this.outputOfSpeaker, this.connection, this.userName, friend);
                            af.run();

                            break;
                        }

                        case "LEAVE PARTY" : {
                            List<String> partyMembers = new ArrayList<>();

                            String friend;

                            while(!(friend = this.inputOfSpeaker.readLine()).equals("END"))
                                partyMembers.add(friend);

                            LeaveParty lp = new LeaveParty(this.socketOfSpeaker, this.inputOfSpeaker, this.outputOfSpeaker, this.connection, this.userName, partyMembers);
                            lp.run();

                            break;
                        }

                        case "STATS LIST" : {
                            StatsList sl = new StatsList(this.socketOfSpeaker, this.inputOfSpeaker, this.outputOfSpeaker, this.connection, this.userName);
                            sl.run();

                            break;
                        }

                        case "REMOVE FRIEND" : {
                            String frd = this.inputOfSpeaker.readLine();
                            RemoveFriend sl = new RemoveFriend(this.socketOfSpeaker, this.inputOfSpeaker, this.outputOfSpeaker, this.connection, this.userName, frd);
                            sl.run();

                            break;
                        }

                        case "LOAD CHARACTER SELECTOR" : {
                            this.loadCharacterSelector();

                            break;
                        }

                        case "LOAD MAP SELECTOR" : {
                            this.loadMapSelector();

                            break;
                        }

                        case "START FIGHT" : {
                            this.startFight();

                            break;
                        }

                        case "SEND PLAYER DATA PACKAGE" : {
                            this.sendDataPackage();

                            break;
                        }

                        default : {
                            System.err.println("nan error on server");
                            this.outputOfSpeaker.writeBytes("NAN" + "\r\n");
                            this.outputOfSpeaker.flush();
                        }
                    }

                    if(requests.equals("CLOSE"))
                        break;
                }

                /*------------------------------- CERRAR LAS CONEXIONES -----------------------------*/
            } catch (SocketException e){
                e.printStackTrace();
                ServerManager.numberOfPLayers--;
                this.label.setText("Number of players: " + ServerManager.numberOfPLayers);
                this.label.revalidate();
                this.label.repaint();
                ServerManager.currentPlayers.remove(this);
                DisconnectFromGame d = new DisconnectFromGame(this.socketOfSpeaker, this.inputOfSpeaker, this.outputOfSpeaker, this.connection, this.userName);
                d.run();

                break;
            }catch (IOException e) {
                e.printStackTrace();
                ServerManager.numberOfPLayers--;
                this.label.setText("Number of players: " + ServerManager.numberOfPLayers);
                this.label.revalidate();
                this.label.repaint();
                ServerManager.currentPlayers.remove(this);
                DisconnectFromGame d = new DisconnectFromGame(this.socketOfSpeaker, this.inputOfSpeaker, this.outputOfSpeaker, this.connection, this.userName);
                d.run();
                try {
                    this.inputOfSpeaker.close();
                    this.outputOfSpeaker.close();
                    this.socketOfSpeaker.close();
                    this.inputOfListener.close();
                    this.outputOfListener.close();
                    this.socketOfListener.close();
                    this.connection.close();
                } catch (IOException e1) {
                    JOptionPane.showMessageDialog(this.frame, e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
                    e1.printStackTrace();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
                break;
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
            e.printStackTrace();
        }
    }

    void writeInstantAction(List<String> commands){
        for(String s : commands){
            try {
                this.outputOfListener.writeBytes(s + "\r\n");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this.frame, e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }

        try {
            this.outputOfListener.flush();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this.frame, e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void setPlayingMatchF(boolean playingMatchF){
        this.playingMatchF = playingMatchF;
    }

    private void loadCharacterSelector() throws IOException {
        List<String> partyMembers = new ArrayList<>();

        String friend;

        while(!(friend = this.inputOfSpeaker.readLine()).equals("END"))
            partyMembers.add(friend);

        for(ClientSpeaker c : ServerManager.currentPlayers){
            for(String name : partyMembers){
                if(c.getName().equals(name)){
                    List<String> toSend = new ArrayList<>();
                    toSend.add("LOAD CHARACTER SELECTOR");
                    c.writeInstantAction(toSend);
                }
            }
        }

        this.outputOfSpeaker.writeBytes("OK" + "\r\n");
        this.outputOfSpeaker.flush();
    }

    private void loadMapSelector() throws IOException {
        this.readyForFightF = true;

        this.character = CharacterType.valueOf(this.inputOfSpeaker.readLine());
        this.skin = Integer.valueOf(this.inputOfSpeaker.readLine());
        System.out.println("player " + this.userName + ", character: " + this.character + ", skin: " + this.skin);

        if(this.inputOfSpeaker.readLine().equals("NOT HOST"))
            this.playingMatchF = true;

        boolean goToMapSelector = true;

        List<String> partyMembers = new ArrayList<>();
        String friend;
        while(!(friend = this.inputOfSpeaker.readLine()).equals("END")) {
            partyMembers.add(friend);
        }

        for(ClientSpeaker c : ServerManager.currentPlayers){
            for(String name : partyMembers){
                if(c.getName().equals(name)){
                    this.playersToSendInfo.add(c);
                }
            }
        }

        for(ClientSpeaker c : ServerManager.currentPlayers){
            for(String name : partyMembers){
                if(c.getName().equals(name)){
                    if(!c.readyForFightF){
                        goToMapSelector = false;
                        break;
                    }
                }
            }
        }

        if(goToMapSelector){
            List<String> toSend = new ArrayList<>();
            toSend.add("LOAD MAP SELECTOR");
            this.writeInstantAction(toSend);
            for(ClientSpeaker c : ServerManager.currentPlayers){
                for(String name : partyMembers){
                    if(c.getName().equals(name)){
                        c.writeInstantAction(toSend);
                    }
                }
            }
        }

        this.outputOfSpeaker.writeBytes("OK" + "\r\n");
        this.outputOfSpeaker.flush();
    }

    private void startFight() throws IOException {
        String map = this.inputOfSpeaker.readLine();
        this.outputOfSpeaker.writeBytes("START FIGHT" + "\r\n");
        this.outputOfSpeaker.flush();

        String playersAndSkins = "USER:" + this.userName + ":" + this.character + ":" + this.skin;

        for(ClientSpeaker c : this.playersToSendInfo){
            playersAndSkins += "USER:" + c.userName + ":" + c.character + ":" + c.skin;
        }

        List<String> toSend =  new ArrayList<>();
        toSend.add("START FIGHT");
        toSend.add(map);
        toSend.add(String.valueOf(this.playersToSendInfo.size()));
        toSend.add(playersAndSkins);
        this.playingMatchF = true;

        this.writeInstantAction(toSend);

        for(ClientSpeaker c : this.playersToSendInfo) {
            c.setPlayingMatchF(true);
            c.writeInstantAction(toSend);
        }
    }

    private void sendDataPackage() throws IOException {
        List<ClientSpeaker> toRemove = new ArrayList<>();

        /*--------------------- UDP VERSION ---------------------**/
        this.udpSpeaker.receive(this.udpPacketSpeaker);

        for(ClientSpeaker c : this.playersToSendInfo) {
            try{
                c.udpPacketListener.setData(this.udpPacketSpeaker.getData(), 0, this.udpPacketSpeaker.getLength());
                c.udpListener.send(c.udpPacketListener);
            }catch (SocketException e){
                toRemove.add(c);
            }
        }

        this.playersToSendInfo.removeAll(toRemove);
    }
}
