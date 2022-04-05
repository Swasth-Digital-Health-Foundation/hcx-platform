package org.swasth.auditindexer.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.swasth.auditindexer.utils.ElasticSearchUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

public class AuditIndexer {

    private final String auditIndex;
    private final String auditAlias;
    private final ElasticSearchUtil esUtil;
    private static ObjectMapper mapper = new ObjectMapper();

    public AuditIndexer(String esHost, int esPort, String auditIndex, String auditAlias) throws Exception {
        this.auditIndex = auditIndex;
        this.auditAlias = auditAlias;
        esUtil = new ElasticSearchUtil(esHost, esPort);
    }

    public void createDocument(Map<String, Object> event) throws Exception {
        try {
            String indexName = getIndexName((Long) event.get("auditTimeStamp"));
            String mid = (String) event.get("mid");
            createIndex(indexName);
            esUtil.addDocumentWithIndex(mapper.writeValueAsString(event), indexName, mid);
            System.out.println("Audit document created for mid: " + mid);
        } catch (Exception e) {
            throw new Exception("Error while processing event :: " + event + " :: " + e.getMessage());
        }
    }

    private String getIndexName(long ets){
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("IST"));
        cal.setTime(new Date(ets));
        return auditIndex + "_" + cal.get(Calendar.YEAR) + "_" + cal.get(Calendar.WEEK_OF_YEAR);
    }

    private void createIndex(String indexName) throws Exception {
        String settings = "{ \"index\": { } }";
        String mappings = "{ \"properties\": { \"eid\": { \"type\": \"text\" }, \"x-hcx-sender_code\": { \"type\": \"keyword\" }, \"x-hcx-recipient_code\": { \"type\": \"keyword\" }, \"x-hcx-api_call_id\": { \"type\": \"keyword\" }, \"x-hcx-correlation_id\": { \"type\": \"keyword\" }, \"x-hcx-workflow_id\": { \"type\": \"keyword\" }, \"x-hcx-timestamp\": { \"type\": \"keyword\" }, \"mid\": { \"type\": \"keyword\" }, \"action\": { \"type\": \"keyword\" }, \"x-hcx-status\": { \"type\": \"keyword\" }, \"auditTimeStamp\": { \"type\": \"keyword\" }, \"requestTimeStamp\": { \"type\": \"keyword\" }, \"updatedTimestamp\": { \"type\": \"keyword\" }, \"x-hcx-error_details\": { \"type\": \"object\" }, \"x-hcx-debug_details\": { \"type\": \"object\" }, \"senderRole\": { \"type\": \"keyword\" }, \"recipientRole\": { \"type\": \"keyword\" }, \"payload\": { \"type\": \"text\" } } }";
        esUtil.addIndex(settings, mappings, indexName, auditAlias);
    }

}
