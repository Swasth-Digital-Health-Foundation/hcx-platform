package org.swasth.dp.auditindexer.functions;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swasth.dp.auditindexer.task.AuditIndexerConfig;
import org.swasth.dp.auditindexer.utils.Util;
import org.swasth.dp.core.job.Metrics;
import org.swasth.dp.core.util.ElasticSearchUtil;
import org.swasth.dp.core.util.JSONUtil;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

public class AuditIndexerProcessFunction extends ProcessFunction<Map<String,Object>,Metrics> {

    private Logger logger = LoggerFactory.getLogger(AuditIndexerProcessFunction.class);
    private AuditIndexerConfig config;
    private ElasticSearchUtil esUtil;

    public AuditIndexerProcessFunction(AuditIndexerConfig config) {
        this.config = config;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        esUtil = new ElasticSearchUtil(config.esUrl, config.auditIndex, config.batchSize);
    }

    @Override
    public void close() throws Exception {
        super.close();
        esUtil.close();
    }

    @Override
    public void processElement(Map<String, Object> event, ProcessFunction<Map<String, Object>, Metrics>.Context context, Collector<Metrics> collector) throws Exception {
        try {
            String indexName = getIndexName((Long) event.get("auditTimeStamp"));
            String apiCallId = (String) event.get("x-hcx-api_call_id");
            createIndex(indexName);
            esUtil.addDocumentWithIndex(JSONUtil.serialize(event), indexName, apiCallId);
            logger.info("Audit record created for " + apiCallId);
            //TODO: add metrics
        }
        catch (IOException e) {
            logger.error("Error while indexing message :: " + event + " :: " + e.getMessage());
            throw e;
        }
        catch (Exception e) {
            logger.error("Error while processing message :: " + event + " :: " + e.getMessage());
            throw e;
        }
    }

    private String getIndexName(long ets){
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(config.timeZone));
        cal.setTime(new Date(ets));
        return config.auditIndex + "_" + cal.get(Calendar.YEAR) + "_" + cal.get(Calendar.WEEK_OF_YEAR);
    }

    private void createIndex(String indexName){
        String settings = "{ \"index\": { } }";
        if (settings == null) {
            logger.error("Failed to load index settings");
        }
        String mappings = "{ \"properties\": { \"x-hcx-sender_code\": { \"type\": \"keyword\" }, \"x-hcx-recipient_code\": { \"type\": \"keyword\" }, \"x-hcx-api_call_id\": { \"type\": \"keyword\" }, \"x-hcx-correlation_id\": { \"type\": \"keyword\" }, \"x-hcx-workflow_id\": { \"type\": \"keyword\" }, \"x-hcx-timestamp\": { \"type\": \"keyword\" }, \"mid\": { \"type\": \"keyword\" }, \"action\": { \"type\": \"keyword\" }, \"status\": { \"type\": \"keyword\" }, \"auditTimeStamp\": { \"type\": \"keyword\" }, \"requestTimeStamp\": { \"type\": \"keyword\" }, \"updatedTimestamp\": { \"type\": \"keyword\" }, \"error_details\": { \"type\": \"object\" }, \"debug_details\": { \"type\": \"object\" } } }";
        if (mappings == null) {
            logger.error("Failed to load mappings for index with name '{}'", config.auditAlias);
        }
        esUtil.addIndex(settings, mappings, indexName, config.auditAlias);
    }

}
