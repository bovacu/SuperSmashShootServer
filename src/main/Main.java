package main;

import java.io.*;
import java.util.Properties;

public class Main {
    public static void main(String args[]){
        //D:\GamesProjects\Java\SuperSmashShootServer
        //C:/Users/vazqu/IdeaProjects/SuperSmashShootServer/
        String databaseUrl = "D:/GamesProjects/Java/SuperSmashShootServer/";
        if(args.length == 1)
            databaseUrl = args[0];

        ServerManager serverManager = new ServerManager(databaseUrl);
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
