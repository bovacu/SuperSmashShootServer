package main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StatsList extends ServerAction {

    private Connection connection;
    private String usr;

    public StatsList(Socket socket, DataInputStream input, DataOutputStream output, Connection connection, String usr) {
        super(socket, input, output);
        this.connection = connection;
        this.usr = usr;
    }

    @Override
    public void run() {
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = this.connection.prepareStatement("SELECT S.* " +
                    "From Stats S " +
                    "Where UserName='" + this.usr + "'");
            rs = stmt.executeQuery();

            if(rs.next()) {
                this.output.writeBytes("SENDING STATS LIST" + "\r\n");
                output.writeBytes(rs.getString(1) + "\r\n");
                output.writeBytes(rs.getString(2) + "\r\n");
                output.writeBytes(rs.getString(3) + "\r\n");
                output.writeBytes(rs.getString(4) + "\r\n");
                output.writeBytes(rs.getString(5) + "\r\n");
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
