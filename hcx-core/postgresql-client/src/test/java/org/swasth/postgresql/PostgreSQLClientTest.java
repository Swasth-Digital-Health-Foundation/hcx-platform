package org.swasth.postgresql;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.Before;
import org.junit.Test;
import org.swasth.common.exception.ClientException;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PostgreSQLClientTest {

    private PostgreSQLClient postgreSQLClient;

    private PostgreSQLClient invalidPostgreSQLClient;

    private EmbeddedPostgres embeddedPostgres;

    @Before()
    public void setup() throws Exception {
        embeddedPostgres = EmbeddedPostgres.builder().setPort(5432).start();
        postgreSQLClient = new PostgreSQLClient("jdbc:postgresql://localhost:5432/postgres", "postgres", "postgres");
    }


    @Test
    public void testHealth() {
        assertTrue(postgreSQLClient.isHealthy());
    }

    @Test
    public void testQueryExecute() throws ClientException, SQLException {
        postgreSQLClient.execute("CREATE TABLE payload(mid text PRIMARY KEY, data text)");
        postgreSQLClient.execute("INSERT INTO payload(mid,data)  VALUES('12345','eyJlbmMiOiJBMjU2R')");
        postgreSQLClient.close();
        ResultSet rs = postgreSQLClient.executeQuery("select * from payload");
        while(rs.next()){
            assertEquals("12345", rs.getString("mid"));
            assertEquals("eyJlbmMiOiJBMjU2R", rs.getString("data"));
        }
    }

    @Test
    public void testBatchQuery() throws ClientException, SQLException {
        String str = "INSERT INTO payload(mid,data)  VALUES('12346','eyJlbmMiOiJBMjU2R')" +
                "INSERT INTO payload(mid,data)  VALUES('12347','eyJlbmMiOiJBMjU2R')";
        postgreSQLClient.addBatch(str);
    }


    @Test(expected = Exception.class)
    public void testExecuteException() throws ClientException {
        postgreSQLClient.execute("CREATE TABLE payload");
    }

    @Test(expected = Exception.class)
    public void testExecuteQueryException() throws ClientException, SQLException {
        postgreSQLClient.executeQuery("select * from");
    }

}

