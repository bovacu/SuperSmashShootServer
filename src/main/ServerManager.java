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
            System.out.printf("No se puedo conectar a la BD");
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
            JOptionPane.showMessageDialog(this, e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
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
                ClientSpeaker cp = new ClientSpeaker(socketOfSpeaker, socketOfListener, url, this.count, this);
                ServerManager.currentPlayers.add(cp);
                this.pool.execute(cp);

            } catch (IOException e){
                JOptionPane.showMessageDialog(this, e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
            }
        }

        try {
            if(this.connection != null)
                this.connection.close();
            if(this.server != null)
                this.server.close();
        } catch (SQLException | IOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
        }

        this.pool.shutdown();
    }

    private void createWindow(JLabel label){
        super.setVisible(true);
        super.setSize(500, 150);
        super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        super.setLayout(new FlowLayout());

        Font customFont, customFont2;
        JLabel serverRunning = new JLabel("(Server Running)");
        serverRunning.setHorizontalAlignment(JLabel.CENTER);
        serverRunning.setVerticalAlignment(JLabel.CENTER);

        label.setHorizontalAlignment(JLabel.CENTER);
        label.setVerticalAlignment(JLabel.BOTTOM);

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

        try {
            customFont = Font.createFont(Font.TRUETYPE_FONT, new File("fonts/Undeveloped.ttf")).deriveFont(40f);
            ge.registerFont(customFont);
            serverRunning.setFont(customFont);

            customFont2 = Font.createFont(Font.TRUETYPE_FONT, new File("fonts/neon_pixel-7.ttf")).deriveFont(40f);
            ge.registerFont(customFont2);
            label.setFont(customFont2);

        } catch (IOException | FontFormatException e) {

            Font[] fonts = ge.getAllFonts();
            String fontName = "";

            for(Font f : fonts){
                System.out.println(f.getName());
                if(f.getName().equals("Undeveloped Regular")){
                    fontName = f.getFontName();
                    break;
                }else{
                    fontName = fonts[0].getFontName();
                }
            }

            customFont = new Font(fontName + ".ttf", Font.PLAIN, 40);
            serverRunning.setFont(customFont);


            for(Font f : fonts){
                if(f.getFontName().equals("Neon Pixel-7")){
                    fontName = f.getFontName();
                    break;
                }else{
                    fontName = fonts[0].getFontName();
                }
            }

            customFont2 = new Font(fontName + ".ttf", Font.PLAIN, 40);
            label.setFont(customFont2);

            JOptionPane.showMessageDialog(this, e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
        }

        super.add(serverRunning);
        super.add(label);

        super.revalidate();
        super.repaint();

        GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
        Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
        int x = (int) rect.getMaxX() - super.getWidth();
        int y = 0;
        super.setLocation(x, y);
    }
}
