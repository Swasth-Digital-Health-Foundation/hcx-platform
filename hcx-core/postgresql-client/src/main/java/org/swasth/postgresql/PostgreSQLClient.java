package org.swasth.postgresql;

import org.swasth.common.exception.ClientException;

import java.sql.*;

public class PostgreSQLClient implements IDatabaseService {

    private final String url;
    private final String user;
    private final String password;
    private Connection connection;
    private Statement statement;

    public PostgreSQLClient(String url, String user, String password) throws ClientException, SQLException {
        this.url = url;
        this.user = user;
        this.password = password;
        initializeConnection();
    }

    private void initializeConnection() throws ClientException {
        try {
            connection = DriverManager.getConnection(url, user, password);
            statement = connection.createStatement();
        } catch (SQLException e) {
            throw new ClientException("Error connecting to the PostgreSQL server: " + e.getMessage());
        }
    }

    public Connection getConnection() throws ClientException {
        try {
            if (connection == null || connection.isClosed()) {
                initializeConnection();
            }
        } catch (SQLException e) {
            throw new ClientException("Error connecting to the PostgreSQL server: " + e.getMessage());
        }
        return connection;
    }

    public void close() throws SQLException {
        if (statement != null) {
            statement.close();
        }
        if (connection != null) {
            connection.close();
        }
    }

    public boolean execute(String query) throws ClientException {
        try {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            boolean result = stmt.execute(query);
            stmt.close();
            return result;
        } catch (SQLException e) {
            throw new ClientException("Error while performing database operation: " + e.getMessage());
        }
    }

    public ResultSet executeQuery(String query) throws ClientException {
        try {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            ResultSet resultSet = stmt.executeQuery(query);
            stmt.close();
            return resultSet;
        } catch (SQLException e) {
            throw new ClientException("Error while performing database operation: " + e.getMessage());
        }
    }

    public void addBatch(String query) throws ClientException {
        try {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            stmt.addBatch(query);
            stmt.close();
        } catch (SQLException e) {
            throw new ClientException("Error while performing database operation: " + e.getMessage());
        }
    }

    public int[] executeBatch() throws ClientException {
        try {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            int[] result = stmt.executeBatch();
            stmt.close();
            return result;
        } catch (SQLException e) {
            throw new ClientException("Error while performing database operation: " + e.getMessage());
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
