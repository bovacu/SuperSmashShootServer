package main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Disconnections extends ServerAction {

    private Connection con;
    private String usr;

    public Disconnections(Socket socket, DataInputStream is, DataOutputStream os, Connection con, String usr){
        super(socket, is, os);
        this.con = con;
        this.usr = usr;
    }

    @Override
    public void run() {
        try {
            String query = "update Players set Online = ? where UserName = ?";
            PreparedStatement preparedStmt = con.prepareStatement(query);
            preparedStmt.setInt   (1, 0);
            preparedStmt.setString(2, this.usr);
            preparedStmt.executeUpdate();
            preparedStmt.close();

            System.out.println(this.usr);
            super.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally{
            super.close();
        }
    }
}
