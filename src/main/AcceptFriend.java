package main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AcceptFriend extends ServerAction {

    private Connection connection;
    private String usr, frd;

    public AcceptFriend(Socket socket, DataInputStream input, DataOutputStream output, Connection connection, String usr, String frd) {
        super(socket, input, output);
        this.connection = connection;
        this.usr = usr;
        this.frd = frd;
    }

    @Override
    public void run() {
        try{
            String query = "UPDATE Friends set Accepted = 1 where UserName = ? AND FriendName = ? AND Accepted = 0";
            PreparedStatement stmt = this.connection.prepareStatement(query);
            stmt.setString(1, this.frd);
            stmt.setString(2, this.usr);
            int result = stmt.executeUpdate();
            stmt.close();

            if(result == 1)
                super.output.writeBytes("ACCEPT FRIEND OK" + "\r\n");
            else
                super.output.writeBytes("ACCEPT FRIEND ERROR" + "\r\n");

            super.output.flush();
        } catch (SQLException | IOException e){
            e.printStackTrace();
        }
    }
}
