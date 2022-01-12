package org.swasth.dp.search.db;

import org.swasth.dp.core.util.PostgresConnect;
import org.swasth.dp.core.util.PostgresConnectionConfig;
import org.swasth.dp.search.beans.CompositeSearch;
import org.swasth.dp.search.beans.SearchResponse;
import org.swasth.dp.search.task.CompositeSearchConfig;
import org.swasth.dp.search.utils.BaseUtils;
import org.swasth.dp.search.utils.PipelineException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;

public class PostgresClient {

    private final PostgresConnect postgresConnect;
    private final CompositeSearchConfig searchConfig;

    public PostgresClient(CompositeSearchConfig searchConfig) {
        this.postgresConnect = new PostgresConnect(new PostgresConnectionConfig(searchConfig.postgresUser,
                searchConfig.postgresPassword,
                searchConfig.postgresDb,
                searchConfig.postgresHost,
                searchConfig.postgresPort,
                searchConfig.postgresMaxConnections));
        this.searchConfig = searchConfig;
    }

    public PostgresConnect getPostgresConnect() {
        return postgresConnect;
    }

    //TODO use upsert query and modify this method for both base and child records
    //FIXME use only one method instead of 2 different methods
    public void insertBaseRecord(String workFlowId, String requestId,String senderCode) throws PipelineException, SQLException {
        PreparedStatement pstmt = null;
        try {
            postgresConnect.connection().setAutoCommit(false);
            String query = String.format("INSERT INTO  %s (workflow_id,request_id,sender_code,request_time,expiry_time) VALUES (?,?,?,?,?)", searchConfig.searchTable);
            pstmt = postgresConnect.connection().prepareStatement(query);
            pstmt.setString(1, workFlowId);
            pstmt.setString(2, requestId);
            pstmt.setString(3, senderCode);
            Timestamp requestTime = new Timestamp(System.currentTimeMillis());
            pstmt.setTimestamp(4,requestTime);
            //TODO add 24 hours for time being in millis
            Timestamp expiryTime = new Timestamp(requestTime.getTime()+searchConfig.searchExpiry);
            pstmt.setTimestamp(5,expiryTime);
            pstmt.executeUpdate();
            System.out.println("Insert operation completed successfully.");
            postgresConnect.connection().commit();
        } catch (Exception e) {
            throw new PipelineException("Error while performing insert operation: " + e.getMessage());
        } finally {
            if(pstmt != null)
                pstmt.close();
        }
    }

    public String getPayload(String payloadRefId) throws Exception {
        System.out.println("Fetching payload from postgres for mid: " + payloadRefId);
        String postgresQuery = String.format("SELECT data FROM %s WHERE mid = '%s'", searchConfig.postgresTable, payloadRefId);
        PreparedStatement preparedStatement = postgresConnect.getConnection().prepareStatement(postgresQuery);
        try {
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                String payload = resultSet.getString(1);
                //BaseUtils.deserialize(payload, Map.class);
                return payload;
            } else {
                throw new Exception("Payload not found for the given mid: " + payloadRefId);
            }
        } catch(Exception e){
            throw e;
        } finally {
            if(preparedStatement != null)
                preparedStatement.close();
        }
    }

    //FIXME use only one method instead of 2 different methods
    public void insertSearchRecord(String workFlowId, String requestId, String senderCode, String recipientCode, String status, SearchResponse searchResponse) throws PipelineException, SQLException {
        PreparedStatement preStatement = null;
        try {
            postgresConnect.connection().setAutoCommit(false);
            String query = String.format("INSERT INTO  %s (workflow_id,request_id,sender_code,recipient_code,response_status,response_data,request_time) VALUES (?,?,?,?,?,?::JSON,?)", searchConfig.searchTable);
            preStatement = postgresConnect.connection().prepareStatement(query);
            preStatement.setString(1, workFlowId);
            preStatement.setString(2, requestId);
            preStatement.setString(3,senderCode);
            preStatement.setString(4,recipientCode);
            preStatement.setString(5,status);
            preStatement.setObject(6,BaseUtils.serialize(searchResponse));
            Timestamp requestTime = new Timestamp(System.currentTimeMillis());
            preStatement.setTimestamp(7,requestTime);
            preStatement.executeUpdate();
            System.out.println("Insert operation completed successfully.");
            postgresConnect.connection().commit();
        } catch (Exception e) {
            throw new PipelineException("Error while performing insert operation: " + e.getMessage());
        } finally {
            if(preStatement != null)
                preStatement.close();
        }
    }

    public void updateBaseRecord(String workFlowId, String requestId, String status) throws PipelineException, SQLException {
        PreparedStatement preStatement = null;
        try {
            postgresConnect.connection().setAutoCommit(false);
            String query = String.format("UPDATE %s SET response_status ='%s' WHERE workflow_id='%s' AND request_id='%s'",
                    searchConfig.searchTable,status,workFlowId,requestId);
            preStatement = postgresConnect.connection().prepareStatement(query);
            preStatement.executeUpdate();
            System.out.println("Update operation completed successfully.");
            postgresConnect.connection().commit();
        } catch (Exception e) {
            throw new PipelineException("Error while performing insert operation: " + e.getMessage());
        } finally {
            if(preStatement != null)
                preStatement.close();
        }
    }

    public void updateChildRecord(String workFlowId, String requestId, String status, Map<String,Object> searchResponse) throws PipelineException, SQLException {
        PreparedStatement preStatement = null;
        try {
            postgresConnect.connection().setAutoCommit(false);
            String query = String.format("UPDATE %s SET response_status ='%s', WHERE workflow_id='%s' AND request_id='%s'",
                    searchConfig.searchTable,status,workFlowId,requestId);
            preStatement = postgresConnect.connection().prepareStatement(query);
            preStatement.setObject(1,BaseUtils.serialize(searchResponse));
            preStatement.executeUpdate();
            System.out.println("Update operation completed successfully.");
            postgresConnect.connection().commit();
        } catch (Exception e) {
            throw new PipelineException("Error while performing insert operation: " + e.getMessage());
        } finally {
            if(preStatement != null)
                preStatement.close();
        }
    }

    public CompositeSearch getBaseRecord(String workflowId) throws Exception {
        System.out.println("Fetching base record from postgres for workflowId: " + workflowId);
        String postgresQuery = String.format("SELECT request_id,sender_code,response_status FROM %s WHERE recipient_code IS NULL and workflow_id = '%s'", searchConfig.searchTable, workflowId);
        PreparedStatement preparedStatement = postgresConnect.getConnection().prepareStatement(postgresQuery);
        CompositeSearch compositeSearch = null;
        try {
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                compositeSearch = new CompositeSearch();
                compositeSearch.setRequestId(resultSet.getString(1));
                compositeSearch.setSenderCode(resultSet.getString(2));
                compositeSearch.setResponseStatus(resultSet.getString(3));
                return compositeSearch;
            } else {
                throw new PipelineException("Record not found for the given workflowId: " + workflowId);
            }
        } catch(Exception e){
            throw new PipelineException("Record not found for the given workflowId: " + workflowId);
        } finally {
            if(preparedStatement != null)
                preparedStatement.close();
        }
    }
}
