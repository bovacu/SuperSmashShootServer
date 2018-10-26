package main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Friends extends ServerAction {

    private Connection con;
    private String usr;

    public Friends(Socket socket, DataInputStream is, DataOutputStream os, Connection con, String usr){
        super(socket, is, os);
        this.con = con;
        this.usr = usr;
    }

    @Override
    public void run() {
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement("SELECT F.FriendName " +
                    "From Friends F " +
                    "Where UserName='" + this.usr + "' AND Accepted=1");
            rs = stmt.executeQuery();
            while(rs.next()) {
                output.writeBytes(rs.getString(1) + "\r\n");
            }

            stmt = con.prepareStatement("SELECT F.UserName " +
                    "From Friends F " +
                    "Where FriendName='" + this.usr + "' AND Accepted=1");
            rs = stmt.executeQuery();
            while(rs.next()) {
                output.writeBytes(rs.getString(1) + "\r\n");
            }
            output.writeBytes("END" + "\r\n");
            output.flush();

            rs.close();
            stmt.close();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        } finally{
            try {
                rs.close();
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
