package main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LeaveParty extends ServerAction {

    private Connection connection;
    private String usr;
    private List<String> partyMembers;

    public LeaveParty(Socket socket, DataInputStream input, DataOutputStream output, Connection connection, String usr, List<String> partyMembers) {
        super(socket, input, output);
        this.connection = connection;
        this.usr = usr;
        this.partyMembers = partyMembers;
    }

    @Override
    public void run() {
        String query = "update Players set Party = -1 where UserName = ?";

        try {
            System.out.println(this.usr);
            PreparedStatement preparedStmt = this.connection.prepareStatement(query);
            preparedStmt.setString(1, this.usr);

            System.out.println(preparedStmt.executeUpdate());
            preparedStmt.close();

            this.output.writeBytes("PARTY LEFT" + "\r\n");
            this.output.flush();

            for(String friend : this.partyMembers){
                for(ClientSpeaker c : ServerManager.currentPlayers){
                    if(c.getName().equals(friend)){
                        List<String> toSend = new ArrayList<>();
                        toSend.add("MEMBER LEFT");
                        toSend.add(this.usr);
                        c.writeInstantAction(toSend);
                        break;
                    }
                }
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
}
