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
    private DataOutputStream outputForGuest;

    public SendPartyRequest(Socket socket, DataInputStream input, DataOutputStream output, DataOutputStream output2, Connection connection, String usr, String frd) {
        super(socket, input, output);
        this.connection = connection;
        this.usr = usr;
        this.frd = frd;
        this.outputForGuest = output2;
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

            if(result > 0) {
                output.writeBytes("INVITATION SENT" + "\r\n");
                this.outputForGuest.writeBytes("INVITATION RECEIVED" + "\r\n");
                this.outputForGuest.writeBytes(this.usr + "\r\n");
                this.outputForGuest.flush();
            } else
                output.writeBytes("INVITATION ERROR" + "\r\n");

            output.flush();

        } catch(UcanaccessSQLException e){
            try {
                output.writeBytes("INVITATION REPEATED" + "\r\n");
                output.flush();

            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
}
