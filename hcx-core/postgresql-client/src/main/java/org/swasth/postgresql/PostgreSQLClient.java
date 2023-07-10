package org.swasth.postgresql;

import org.swasth.common.exception.ClientException;

import java.sql.*;

public class PostgreSQLClient implements IDatabaseService {

    private final String url;
    private final String user;
    private final String password;
    private Connection connection;

    public PostgreSQLClient(String url, String user, String password) throws ClientException, SQLException {
        this.url = url;
        this.user = user;
        this.password = password;
        initializeConnection();
    }

    private void initializeConnection() throws ClientException {
        try {
            connection = DriverManager.getConnection(url, user, password);
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
        if (connection != null) {
            connection.close();
        }
    }

    public boolean execute(String query) throws ClientException {
        Connection conn = getConnection();
        try (Statement statement = conn.createStatement()) {
            return statement.execute(query);
        } catch (SQLException e) {
            throw new ClientException("Error while performing database operation: " + e.getMessage());
        }
    }

    public ResultSet executeQuery(String query) throws ClientException {
        try {
            Connection conn = getConnection();
            Statement statement = conn.createStatement();
            return statement.executeQuery(query);
        } catch (SQLException e) {
            throw new ClientException("Error while performing database operation: " + e.getMessage());
        }
    }

    public void addBatch(String query) throws ClientException {
        try {
            Connection conn = getConnection();
            Statement statement = conn.createStatement();
            statement.addBatch(query);
        } catch (SQLException e) {
            throw new ClientException("Error while performing database operation: " + e.getMessage());
        }
    }

    public int[] executeBatch() throws ClientException {
        try {
            Connection conn = getConnection();
            Statement statement = conn.createStatement();
            return statement.executeBatch();
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
