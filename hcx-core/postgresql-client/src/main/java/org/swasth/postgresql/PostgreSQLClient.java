package org.swasth.postgresql;

import org.swasth.common.exception.ClientException;

import java.sql.*;

public class PostgreSQLClient implements IDatabaseService {

    private String url;
    private String user;
    private String password;
    private String tableName;

    public PostgreSQLClient(String url, String user, String password, String tableName) {
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
        } catch (Exception e) {
            throw new ClientException("Error connecting to the PostgreSQL server: " + e.getMessage());
        }
        return conn;
    }

    public void insert(String mid, String payload) throws ClientException, SQLException {
        Connection connection = getConnection();
        try {
            connection.setAutoCommit(false);
            String query = "INSERT INTO " + tableName +" VALUES (?,?);";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, mid);
            pstmt.setString(2, payload);
            pstmt.executeUpdate();
            System.out.println("Insert operation completed successfully.");
            connection.commit();
        } catch (Exception e) {
            throw new ClientException("Error while performing insert operation: " + e.getMessage());
        } finally {
            connection.close();
        }
    }

    public boolean isHealthy() {
        try {
            Connection conn = getConnection();
            conn.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
