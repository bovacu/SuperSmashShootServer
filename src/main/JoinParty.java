package main;

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

public class JoinParty extends ServerAction {

    private Connection conn;
    private String host;
    private int partyId;
    private String usr;

    public JoinParty(Socket socket, DataInputStream input, DataOutputStream output, Connection conn, String usr, String host) {
        super(socket, input, output);
        this.conn = conn;
        this.host = host;
        this.usr = usr;
        this.partyId = this.userNameToPartyId();
    }

    @Override
    public void run() {

        ResultSet rs, rs2 = null;
        PreparedStatement stmt, stmt2 = null;

        try {
            stmt = this.conn.prepareStatement("SELECT P.UserName " +
                    "From Players P " +
                    "Where Online = 1 AND Party = " + this.partyId + " AND P.UserName = '" + this.host + "'");
            rs = stmt.executeQuery();

            stmt2 = this.conn.prepareStatement("SELECT COUNT(*) " +
                    "From Players P " +
                    "Where Online = 1 AND Party = " + this.partyId + " AND P.UserName = '" + this.host + "'");
            rs2 = stmt2.executeQuery();

            rs2.next();
            if(rs.next() && rs2.getInt(1) < 4) {
                rs2.close();
                stmt2.close();

                String query = "update Players set Party = ? where UserName = ?";
                PreparedStatement preparedStmt = this.conn.prepareStatement(query);
                preparedStmt.setInt   (1, this.partyId);
                preparedStmt.setString(2, this.usr);
                preparedStmt.executeUpdate();
                preparedStmt.close();
                rs.close();
                stmt.close();

                query = "DELETE FROM PartyRequest WHERE UserName = ? AND FriendName = ?";
                stmt = this.conn.prepareStatement(query);
                stmt.setString(1, this.host);
                stmt.setString(2, this.usr);
                stmt.executeUpdate();
                stmt.close();

                this.output.writeBytes("JOIN OK" + "\r\n");
                this.output.writeBytes(this.host + "\r\n");
                this.output.writeBytes(this.partyId + "\r\n");

                stmt = this.conn.prepareStatement("SELECT P.UserName " +
                        "From Players P " +
                        "Where Online = 1 AND Party = " + this.partyId + "");
                rs = stmt.executeQuery();

                while(rs.next()) {
                    if(!rs.getString(1).equals(this.usr)) {
                        this.output.writeBytes(rs.getString(1) + "\r\n");

                        for(ClientSpeaker c : ServerManager.currentPlayers){
                            if(c.getName().equals(rs.getString(1))){
                                List<String> toSend = new ArrayList<>();
                                toSend.add("UPDATE PARTY");
                                toSend.add(this.usr);
                                c.writeInstantAction(toSend);
                            }
                        }
                    }
                }

                this.output.writeBytes("END" + "\r\n");
                this.output.flush();
                rs.close();
                stmt.close();



            }else{
                this.output.writeBytes("PARTY FULL" + "\r\n");
                this.output.flush();
            }

            rs.close();
            stmt.close();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private int userNameToPartyId(){
        int id = 0;

        for(int i = 0; i < this.host.length(); i++){
            id += this.host.charAt(i) * (i + 1);
        }

        return id;
    }
}
