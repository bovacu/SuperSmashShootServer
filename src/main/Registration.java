package main;

import net.ucanaccess.jdbc.UcanaccessSQLException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Registration extends ServerAction {
    private Connection con;
    private String usr, psw;

    public Registration(Socket socket, DataInputStream is, DataOutputStream os, Connection con, String usr, String psw){
        super(socket, is, os);
        this.usr = usr;
        this.psw = psw;
        this.con = con;
    }

    @Override
    public void run() {
        String party = "NaN";
        try {
            String query = "insert INTO Players([UserName], [Password], [Online], [Party]) VALUES(?, ?, ?, ?)";
            PreparedStatement preparedStmt = con.prepareStatement(query);
            preparedStmt.setString(1, this.usr);
            preparedStmt.setString(2, this.psw);
            preparedStmt.setInt(3, 0);
            preparedStmt.setInt(4, -1);
            int result = preparedStmt.executeUpdate();
            preparedStmt.close();

            if(result > 0) {
                query = "insert INTO Stats([UserName], [Matchs], [Wins], [Looses], [DoneDamage], [ReceivedDamage]) VALUES(?, 0, 0, 0, 0, 0)";
                preparedStmt = con.prepareStatement(query);
                preparedStmt.setString(1, this.usr);
                preparedStmt.executeUpdate();
                preparedStmt.close();
                output.writeBytes("REGISTER OK" + "\r\n");
                output.writeBytes(this.usr + "\r\n");
            }else {
                System.out.println("Repetido el valor a registrar");
                output.writeBytes("REGISTER REPEATED" + "\r\n");
            }

            output.flush();

        } catch(UcanaccessSQLException e){
            e.printStackTrace();
            try {
                System.err.println("error durante el registro");
                output.writeBytes("REGISTER REPEATED" + "\r\n");
                output.flush();

            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
}
