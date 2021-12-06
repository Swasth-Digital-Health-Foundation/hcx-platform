package org.swasth.postgresql;

import org.swasth.common.exception.ClientException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class PostgreSQLClient implements IDatabaseService {

    private String url;
    private String user;
    private String password;
    private String tableName;

    public PostgreSQLClient(String url, String user, String password, String tableName){
        this.url = url;
        this.user = user;
        this.password = password;
        this.tableName = tableName;
    }

    private Connection getConnection() throws ClientException {
        Connection conn;
        try {
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            throw new ClientException("Error connecting to the PostgreSQL server: " + e.getMessage());
        }
        return conn;
    }

    public void insert(String mid, String payload) throws ClientException {
        try {
            Connection connection = getConnection();
            connection.setAutoCommit(false);
            Statement stmt = connection.createStatement();
            String query = "INSERT INTO " + tableName + " VALUES " + "(" + "'" + mid + "'" + "," + "'" + payload + "'" + ");" ;
            stmt.executeUpdate(query);
            stmt.close();
            connection.commit();
            connection.close();
        } catch (Exception e) {
            throw new ClientException("Error while performing insert operation: " + e.getMessage());
        }
    }

}
