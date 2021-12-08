package org.swasth.postgresql;

import org.swasth.common.exception.ClientException;

import java.sql.SQLException;

public interface IDatabaseService {

    void insert(String mid, String payload) throws ClientException, SQLException;
    boolean isHealthy();
}
