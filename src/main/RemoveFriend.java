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

public class RemoveFriend extends ServerAction{

    private Connection connection;
    private String frd, usr;

    public RemoveFriend(Socket socket, DataInputStream input, DataOutputStream output, Connection connection, String usr, String frd) {
        super(socket, input, output);
        this.connection = connection;
        this.frd = frd;
        this.usr = usr;
    }

    @Override
    public void run() {
        try {
            String removeParties = "DELETE FROM Friends WHERE UserName = ? AND FriendName = ?";
            PreparedStatement preparedStmt = this.connection.prepareStatement(removeParties);
            preparedStmt.setString(1, this.usr);
            preparedStmt.setString(2, this.frd);

            if(preparedStmt.executeUpdate() == 0){
                preparedStmt.close();
                removeParties = "DELETE FROM Friends WHERE UserName = ? AND FriendName = ?";
                preparedStmt = this.connection.prepareStatement(removeParties);
                preparedStmt.setString(1, this.frd);
                preparedStmt.setString(2, this.usr);

                if(preparedStmt.executeUpdate() == 0){
                    this.output.writeBytes("FRIEND REMOVE ERROR" + "\r\n");
                    this.output.flush();
                }else{
                    this.output.writeBytes("FRIEND REMOVED" + "\r\n");

                    for(ClientSpeaker c : ServerManager.currentPlayers){
                        if(c.getName().equals(this.usr)){
                            List<String> toSend = new ArrayList<>();
                            toSend.add("REMOVED");
                            toSend.add(this.usr);
                            c.writeInstantAction(toSend);
                            break;
                        }
                    }

                    this.output.flush();
                }
            }else{
                this.output.writeBytes("FRIEND REMOVED" + "\r\n");

                for(ClientSpeaker c : ServerManager.currentPlayers){
                    if(c.getName().equals(this.frd)){
                        List<String> toSend = new ArrayList<>();
                        toSend.add("REMOVED");
                        toSend.add(this.frd);
                        c.writeInstantAction(toSend);
                        break;
                    }
                }

                this.output.flush();
            }

            preparedStmt.close();

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
}
