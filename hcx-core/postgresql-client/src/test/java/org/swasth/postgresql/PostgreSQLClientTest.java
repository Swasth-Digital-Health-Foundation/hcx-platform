package org.swasth.postgresql;

import org.junit.Test;
import org.swasth.common.exception.ClientException;

import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

public class PostgreSQLClientTest {

    PostgreSQLClient postgreSQLClient;

    public PostgreSQLClientTest() throws ClientException, SQLException {
        postgreSQLClient  = new PostgreSQLClient("jdbc:postgresql://localhost:5432/postgres", "user", "password");
    }

    @Test
    public void check_health() {
        boolean result = postgreSQLClient.isHealthy();
        assertEquals(false, result);
    }

}
