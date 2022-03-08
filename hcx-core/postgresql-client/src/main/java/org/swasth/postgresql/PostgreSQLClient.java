package org.swasth.postgresql;

import org.swasth.common.exception.ClientException;
import org.swasth.common.utils.Constants;

import java.sql.*;

public class PostgreSQLClient implements IDatabaseService {

    private String url;
    private String user;
    private String password;

    public PostgreSQLClient(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public Connection getConnection() throws ClientException {
        Connection conn;
        try {
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (Exception e) {
            throw new ClientException("Error connecting to the PostgreSQL server: " + e.getMessage());
        }
        return conn;
    }


    public boolean execute(String query) throws ClientException, SQLException {
        Connection connection = getConnection();
        try {
            connection.setAutoCommit(false);
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            boolean result = preparedStatement.execute();
            System.out.println("Database operation completed successfully");
            connection.commit();
            return result;
        } catch (Exception e) {
            throw new ClientException("Error while performing database operation: " + e.getMessage());
        } finally {
            connection.close();
            System.out.println("Connection is closed successfully.");
        }
    }

    public ResultSet executeQuery(String query) throws ClientException, SQLException {
        Connection connection = getConnection();
        try {
            connection.setAutoCommit(false);
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            System.out.println("Database operation completed successfully");
            connection.commit();
            return resultSet;
        } catch (Exception e) {
            throw new ClientException("Error while performing database operation: " + e.getMessage());
        } finally {
            connection.close();
            System.out.println("Connection is closed successfully.");
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
