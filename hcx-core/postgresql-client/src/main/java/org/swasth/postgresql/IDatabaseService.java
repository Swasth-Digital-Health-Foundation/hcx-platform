package org.swasth.postgresql;

public interface IDatabaseService {

    Object executeQuery(String query) throws Exception;
    boolean execute(String query) throws Exception;
    boolean isHealthy();
    Object getConnection() throws Exception;
    void close() throws Exception;
    void addBatch(String query) throws Exception;
}
