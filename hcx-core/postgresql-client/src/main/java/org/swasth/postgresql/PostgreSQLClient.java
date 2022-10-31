package org.swasth.postgresql;

import org.swasth.common.exception.ClientException;

import java.sql.*;

public class PostgreSQLClient implements IDatabaseService {

    private final String url;
    private final String user;
    private final String password;
    private final Connection connection;
    private final Statement statement;

    public PostgreSQLClient(String url, String user, String password) throws ClientException, SQLException {
        this.url = url;
        this.user = user;
        this.password = password;
        this.connection = getConnection();
        this.statement = this.connection.createStatement();
    }

    public Connection getConnection() throws ClientException {
        Connection conn;
        try {
            conn = DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            throw new ClientException("Error connecting to the PostgreSQL server: " + e.getMessage());
        }
        return conn;
    }

    public void close() throws SQLException {
        statement.close();
        connection.close();
    }

    public boolean execute(String query) throws ClientException {
        try {
            return statement.execute(query);
        } catch (Exception e) {
            throw new ClientException("Error while performing database operation: " + e.getMessage());
        }
    }

    public ResultSet executeQuery(String query) throws ClientException {
        try {
            return statement.executeQuery(query);
        } catch (Exception e) {
            throw new ClientException("Error while performing database operation: " + e.getMessage());
        }
    }

    public void addBatch(String query) throws ClientException {
        try {
             statement.addBatch(query);
        } catch (Exception e) {
            throw new ClientException("Error while performing database operation: " + e.getMessage());
        }
    }

    public int[] executeBatch() throws ClientException {
        try {
            return statement.executeBatch();
        } catch (Exception e) {
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
