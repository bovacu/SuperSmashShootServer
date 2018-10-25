package main;

import java.io.*;
import java.util.Properties;

public class Main {

    public static void main(String args[]){
        ServerManager serverManager = new ServerManager();
        serverManager.runServer();
    }

    public static String getProperties(String key) {
        String c = null;
        try{
            Properties p = new Properties();
            p.load(new FileInputStream("connection.properties"));
            c = p.getProperty(key);
        }catch(IOException e){
            e.printStackTrace();
        }
        return c;
    }
}
