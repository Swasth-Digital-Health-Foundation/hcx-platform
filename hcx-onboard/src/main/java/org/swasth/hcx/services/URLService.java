package org.swasth.hcx.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.swasth.postgresql.IDatabaseService;

import java.sql.ResultSet;

@Service
public class URLService {

    @Autowired
    private IDatabaseService postgreSQLClient;

    @Value("${postgres.table.onboard-verification}")
    private String onboardVerificationTable;

    @Value("${hcxURL}")
    private String hcxURL;

    @Value("${apiVersion}")
    private String apiVersion;

    public String getLongUrl(String id) throws Exception {
        String shortUrl = hcxURL+"/api/url/"+id;
        String selectQuery = String.format("SELECT * FROM %s WHERE phone_short_url = '%s'", onboardVerificationTable, shortUrl);
        ResultSet resultSet = (ResultSet) postgreSQLClient.executeQuery(selectQuery);
        String longUrl = null;
        while (resultSet.next()) {
            longUrl = resultSet.getString("phone_long_url");
        }
        return longUrl;
    }
}
