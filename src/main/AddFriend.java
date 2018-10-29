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
import java.util.ArrayList;
import java.util.List;

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
            if(!this.frd.equals(this.usr)){
                stmt = con.prepareStatement("SELECT P.* " +
                        "From Players P " +
                        "Where UserName='" + this.frd + "'");
                rs = stmt.executeQuery();
                System.out.println("entra aqui");
                if(rs.next()){
                    rs.close();
                    stmt.close();

                    stmt = con.prepareStatement("SELECT F.* " +
                            "From FriendList F " +
                            "Where UserName='" + this.frd + "' AND FriendName='" + this.usr + "'");
                    rs = stmt.executeQuery();
                    System.out.println("entra aqui");
                    if(!rs.next()) {
                        System.out.println("entra aqui");
                        this.addRow();
                        System.out.println("entra aqui");
                        output.writeBytes("FRIEND REQUEST SENT" + "\r\n");

                        for(ClientSpeaker c : ServerManager.currentPlayers){
                            if(c.getName().equals(this.frd)){
                                List<String> toSend = new ArrayList<>();
                                toSend.add("NEW FRIEND");
                                toSend.add(this.usr);
                                c.writeInstantAction(toSend);
                            }
                        }
                    }else{
                        output.writeBytes("ALREADY FRIEND" + "\r\n");
                    }
                    output.flush();
                }else{
                    output.writeBytes("NO PLAYER" + "\r\n");
                    output.flush();
                }
            }else{
                output.writeBytes("NO PLAYER" + "\r\n");
                output.flush();
            }
        }catch (UcanaccessSQLException e){
            try {
                e.printStackTrace();
                output.writeBytes("ALREADY FRIEND" + "\r\n");
                output.flush();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        } finally{

            try {
                if(rs != null){
                    rs.close();
                    stmt.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void addRow() throws SQLException {
        String query = "insert INTO FriendList([UserName], [FriendName], [Accepted]) VALUES(?, ?, ?)";
        PreparedStatement preparedStmt = con.prepareStatement(query);
        preparedStmt.setString(1, this.usr);
        preparedStmt.setString(2, this.frd);
        preparedStmt.setInt(3, 0);
        preparedStmt.executeUpdate();
        preparedStmt.close();
    }
}
