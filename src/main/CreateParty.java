package main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CreateParty extends ServerAction {

    private Connection conn;
    private String usr;

    public CreateParty(Socket socket, DataInputStream input, DataOutputStream output, Connection conn, String usr) {
        super(socket, input, output);
        this.conn = conn;
        this.usr = usr;
    }

    @Override
    public void run() {
        try {
            String query = "update Players set Party = ? where UserName = ?";
            PreparedStatement preparedStmt = this.conn.prepareStatement(query);
            preparedStmt.setInt   (1, this.userNameToPartyId());
            preparedStmt.setString(2, this.usr);
            preparedStmt.executeUpdate();
            preparedStmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int userNameToPartyId(){
        int id = 0;

        for(int i = 0; i < this.usr.length(); i++){
            id += this.usr.charAt(i) * (i + 1);
        }

        return id;
    }
}
