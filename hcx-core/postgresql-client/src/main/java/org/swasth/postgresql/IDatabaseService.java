package org.swasth.postgresql;

import org.swasth.common.exception.ClientException;

public interface IDatabaseService {

    Object executeQuery(String query) throws Exception;
    boolean execute(String query) throws Exception;
    boolean isHealthy();
    Object getConnection() throws ClientException;
}
