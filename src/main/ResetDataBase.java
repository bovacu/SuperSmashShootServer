package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ResetDataBase {
    private Connection connection;

    public ResetDataBase(String url){
        try {
            this.connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void run(){
        try {
            String query = "update Players set Online = 0, Party = -1";
            PreparedStatement preparedStmt = this.connection.prepareStatement(query);
            preparedStmt.executeUpdate();
            preparedStmt.close();

            String removeParties = "DELETE FROM PartyRequest";
            PreparedStatement preparedStmt2 = this.connection.prepareStatement(removeParties);
            preparedStmt2.executeUpdate();
            preparedStmt2.close();

            this.connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
