package main;

import net.ucanaccess.jdbc.UcanaccessSQLException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AddFriend extends ServerAction {

    private Connection con;
    private String usr, frd;

    public AddFriend(Socket socket, DataInputStream is, DataOutputStream os, Connection con, String usr, String frd){
        super(socket, is, os);
        this.usr = usr;
        this.frd = frd;
        this.con = con;
    }

    @Override
    public void run() {
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement("SELECT P.UserName " +
                    "From Players P " +
                    "Where UserName='" + this.frd + "'");
            rs = stmt.executeQuery();
            if(rs.next() && !this.frd.equals(this.usr)) {
                this.addRow();
                output.writeBytes("OK");
            }else{
                output.writeBytes("NO PLAYER");
            }
            output.flush();

            rs.close();
            stmt.close();
            super.close();
        }catch (UcanaccessSQLException e){
            try {
                output.writeBytes("ALREADY FRIEND");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        } finally{
            super.close();

            try {
                rs.close();
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void addRow() throws SQLException {
        String query = "insert INTO Friends([UserName], [FriendName], [Accepted]) VALUES(?, ?, ?)";
        PreparedStatement preparedStmt = con.prepareStatement(query);
        preparedStmt.setString(1, this.usr);
        preparedStmt.setString(2, this.frd);
        preparedStmt.setInt(3, 0);
        preparedStmt.executeUpdate();
        preparedStmt.close();
    }
}
