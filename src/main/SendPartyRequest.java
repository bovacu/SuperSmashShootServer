package main;

import net.ucanaccess.jdbc.UcanaccessSQLException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SendPartyRequest extends ServerAction{

    private Connection connection;
    private String usr;
    private String frd;

    public SendPartyRequest(Socket socket, DataInputStream input, DataOutputStream output, Connection connection, String usr, String frd) {
        super(socket, input, output);
        this.connection = connection;
        this.usr = usr;
        this.frd = frd;
    }

    @Override
    public void run() {
        try {
            String query = "insert INTO PartyRequest ([UserName], [FriendName]) VALUES(?, ?)";
            PreparedStatement preparedStmt = connection.prepareStatement(query);
            preparedStmt.setString(1, this.usr);
            preparedStmt.setString(2, this.frd);
            int result = preparedStmt.executeUpdate();
            preparedStmt.close();

            if(result > 0)
                output.writeBytes("OK" + "\r\n");
            else
                output.writeBytes("REPEATED" + "\r\n");

            output.flush();

            super.close();
        } catch(UcanaccessSQLException e){
            try {
                output.writeBytes("REPEATED" + "\r\n");
                output.flush();

            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (SQLException | IOException e) {
            super.close();
            e.printStackTrace();
        } finally{
            super.close();
        }
    }
}
