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
            String query = "update Players set Online = ?, Party = -1 where UserName = ?";
            PreparedStatement preparedStmt = con.prepareStatement(query);
            preparedStmt.setInt   (1, 0);
            preparedStmt.setString(2, this.usr);
            preparedStmt.executeUpdate();
            preparedStmt.close();

            String removeParties = "DELETE FROM PartyRequest WHERE UserName = ?";
            PreparedStatement preparedStmt2 = con.prepareStatement(removeParties);
            preparedStmt2.setString(1, this.usr);
            preparedStmt2.executeUpdate();
            preparedStmt2.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
