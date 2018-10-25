package main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Connections extends ServerAction {

    private String usr, psw;
    private Connection con;

    public Connections(Socket socket, DataInputStream is, DataOutputStream os, Connection con, String usr, String psw){
        super(socket, is, os);
        this.usr = usr;
        this.psw = psw;
        this.con = con;
    }

    @Override
    public void run() {
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement("SELECT P.UserName, Password " +
                    "From Players P " +
                    "Where UserName='" + this.usr + "' AND Password='" + this.psw + "'");
            rs = stmt.executeQuery();
            if(rs.next()) {
                String query = "update Players set Online = ? where UserName = ?";
                PreparedStatement preparedStmt = con.prepareStatement(query);
                preparedStmt.setInt   (1, 1);
                preparedStmt.setString(2, this.usr);
                preparedStmt.executeUpdate();
                preparedStmt.close();

                output.writeBytes("CONNECT OK" + "\r\n");
            } else {
                output.writeBytes("CONNECT ERROR" + "\r\n");
            }

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
