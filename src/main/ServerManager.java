package main;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerManager extends JFrame {

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

    static int numberOfPLayers = 0;
    static List<ClientSpeaker> currentPlayers;

    private ExecutorService pool;
    private Connection connection;
    private final String url="jdbc:ucanaccess://C:/Users/vazqu/IdeaProjects/SuperSmashShootServer/DataBase.accdb";
    private ServerSocket server;

    private boolean loop;
    private JLabel count;

    ServerManager(){

        this.count = new JLabel("Number of players: 0");
        this.createWindow(this.count);
        ServerManager.currentPlayers = new ArrayList<>();
        try {
            this.server = new ServerSocket(this.PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.pool = Executors.newFixedThreadPool(this.MAX_THREADS);
        this.loop = true;
    }

    void runServer(){
        while(this.loop){
            Socket socketOfSpeaker, socketOfListener;

            try{
                socketOfSpeaker = this.server.accept();
                socketOfListener = this.server.accept();
                numberOfPLayers++;
                this.count.setText("Number of players: " + numberOfPLayers);
                this.count.revalidate();
                this.count.repaint();
                super.revalidate();
                super.repaint();
                ClientSpeaker cp = new ClientSpeaker(socketOfSpeaker, socketOfListener, url, this.count);
                ServerManager.currentPlayers.add(cp);
                this.pool.execute(cp);

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

    private void createWindow(JLabel label){
        super.setVisible(true);
        super.setSize(500, 150);
        super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        super.setLayout(new FlowLayout());
        try {
            //create the font to use. Specify the size!
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, new File("fonts\\Undeveloped.ttf")).deriveFont(40f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            //register the font
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("fonts\\Undeveloped.ttf")));
            JLabel serverRunning = new JLabel("(Server Running)");
            serverRunning.setHorizontalAlignment(JLabel.CENTER);
            serverRunning.setVerticalAlignment(JLabel.CENTER);
            serverRunning.setFont(customFont);

            Font customFont2 = Font.createFont(Font.TRUETYPE_FONT, new File("fonts\\neon_pixel-7.ttf")).deriveFont(40f);
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("fonts\\neon_pixel-7.ttf")));
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setVerticalAlignment(JLabel.BOTTOM);
            label.setFont(customFont2);

            super.add(serverRunning);
            super.add(label);

            super.revalidate();
            super.repaint();

            GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
            Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
            int x = (int) rect.getMaxX() - super.getWidth();
            int y = 0;
            super.setLocation(x, y);

        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
        }

    }
}
