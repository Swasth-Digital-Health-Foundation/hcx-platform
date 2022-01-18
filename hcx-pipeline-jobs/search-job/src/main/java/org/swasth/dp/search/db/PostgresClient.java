package org.swasth.dp.search.db;

import org.swasth.dp.core.util.PostgresConnect;
import org.swasth.dp.core.util.PostgresConnectionConfig;
import org.swasth.dp.search.beans.CompositeSearch;
import org.swasth.dp.search.beans.SearchResponse;
import org.swasth.dp.search.task.CompositeSearchConfig;
import org.swasth.dp.search.utils.BaseUtils;
import org.swasth.dp.search.utils.PipelineException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PostgresClient {

    private final PostgresConnect postgresConnect;
    private final CompositeSearchConfig searchConfig;

    public PostgresClient(CompositeSearchConfig searchConfig) {
        this.postgresConnect = new PostgresConnect(new PostgresConnectionConfig(searchConfig.getPostgresUser(),
                searchConfig.getPostgresPassword(),
                searchConfig.getPostgresDb(),
                searchConfig.getPostgresHost(),
                searchConfig.getPostgresPort(),
                searchConfig.getPostgresMaxConnections()));
        this.searchConfig = searchConfig;
    }

    public PostgresConnect getPostgresConnect() {
        return postgresConnect;
    }

    public String getPayload(String payloadRefId) throws Exception {
        System.out.println("Fetching payload from postgres for mid: " + payloadRefId);
        String postgresQuery = String.format("SELECT data FROM %s WHERE mid = '%s'", searchConfig.getPostgresTable(), payloadRefId);
        PreparedStatement preparedStatement = postgresConnect.getConnection().prepareStatement(postgresQuery);
        try {
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                return resultSet.getString(1);
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

    public void insertSearchRecord(String workFlowId, String requestId, String senderCode, String recipientCode, String status, SearchResponse searchResponse) throws PipelineException, SQLException {
        PreparedStatement preStatement = null;
        try {
            postgresConnect.connection().setAutoCommit(false);
            String query = String.format("INSERT INTO  %s (workflow_id,request_id,sender_code,recipient_code,response_status,response_data,request_time) " +
                    "VALUES (?,?,?,?,?,?::JSON,?) ON CONFLICT ON CONSTRAINT composite_search_pkey " +
                    "DO NOTHING", searchConfig.getSearchTable());
            preStatement = postgresConnect.connection().prepareStatement(query);
            preStatement.setString(1, workFlowId);
            preStatement.setString(2, requestId);
            preStatement.setString(3,senderCode);
            if(recipientCode != null)
            preStatement.setString(4,recipientCode);
            else
                preStatement.setNull(4, Types.VARCHAR);
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

    public void updateSearchRecord(String workFlowId, String requestId, String status, Map<String,Object> searchResponse) throws PipelineException, SQLException {
        PreparedStatement preStatement = null;
        try {
            postgresConnect.connection().setAutoCommit(false);
            String query = String.format("UPDATE %s SET response_status ='%s',response_data=? WHERE workflow_id='%s' AND request_id='%s'",
                    searchConfig.getSearchTable(),status,workFlowId,requestId);
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
        String postgresQuery = String.format("SELECT request_id,sender_code,response_status FROM %s WHERE COALESCE(recipient_code, '') = '' and workflow_id = '%s'", searchConfig.getSearchTable(), workflowId);
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

    public boolean hasPendingResponses(String workflowId) throws Exception{
            System.out.println("Fetching hasPendingResponses from postgres for workflowId: " + workflowId);
            String postgresQuery = String.format("SELECT count(*) FROM %s WHERE response_status IN ('OPEN','RETRY') AND COALESCE(recipient_code, '') != '' AND workflow_id = '%s'", searchConfig.getSearchTable(), workflowId);
            PreparedStatement preparedStatement = postgresConnect.getConnection().prepareStatement(postgresQuery);
            try {
                ResultSet resultSet = preparedStatement.executeQuery();
                if(resultSet.next()) {
                    return  resultSet.getInt(1) > 0 ? true: false;
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

    public List<CompositeSearch> fetchAllSearchRecords(String workflowId) throws Exception{
        List<CompositeSearch> searchList = null;
        System.out.println("Fetching hasPendingResponses from postgres for workflowId: " + workflowId);
        String postgresQuery = String.format("SELECT request_id,sender_code,response_status,response_data FROM %s WHERE COALESCE(recipient_code, '') != '' AND workflow_id = '%s'", searchConfig.getSearchTable(), workflowId);
        PreparedStatement preparedStatement = postgresConnect.getConnection().prepareStatement(postgresQuery);
        try {
            ResultSet resultSet = preparedStatement.executeQuery();
            searchList = new ArrayList<>();
            CompositeSearch compositeSearch;
            if(resultSet.next()) {
                compositeSearch = new CompositeSearch();
                compositeSearch.setRequestId(resultSet.getString(1));
                compositeSearch.setSenderCode(resultSet.getString(2));
                compositeSearch.setResponseStatus(resultSet.getString(3));
                compositeSearch.setSearchResponse(BaseUtils.deserialize((String) resultSet.getObject(4),SearchResponse.class));
                searchList.add(compositeSearch);
            } else {
                throw new PipelineException("Record not found for the given workflowId: " + workflowId);
            }
            return  searchList;
        } catch(Exception e){
            throw new PipelineException("Record not found for the given workflowId: " + workflowId);
        } finally {
            if(preparedStatement != null)
                preparedStatement.close();
        }
    }
}
