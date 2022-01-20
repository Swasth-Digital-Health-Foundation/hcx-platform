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
    public void processElement(Map<String, Object> event, ProcessFunction<Map<String, Object>, Metrics>.Context context, Collector<Metrics> collector) {
        try {
            String indexName = getIndexName((Long) event.get("auditTimeStamp"));
            String apiCallId = (String) event.get("api_call_id");
            createIndex(indexName);
            esUtil.addDocumentWithIndex(JSONUtil.serialize(event), indexName, apiCallId);
            logger.info("Audit record created for " + apiCallId);
            //TODO: add metrics
        }
        catch (IOException e) {
            logger.error("Error while indexing message :: " + event + " :: " + e.getMessage());
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("Error while processing message :: " + event + " :: " + e.getMessage());
        }
    }

    private String getIndexName(long ets){
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(config.timeZone));
        cal.setTime(new Date(ets));
        return config.auditIndex + "_" + cal.get(Calendar.YEAR) + "_" + cal.get(Calendar.WEEK_OF_YEAR);
    }

    private void createIndex(String indexName){
        String settings = Util.loadAsString("hcx-pipeline-jobs\\audit-indexer\\src\\main\\resources\\static\\es-settings.json");
        if (settings == null) {
            logger.error("Failed to load index settings");
        }
        String mappings = Util.loadAsString("hcx-pipeline-jobs\\audit-indexer\\src\\main\\resources\\static\\mappings\\hcx_audit.json");
        if (mappings == null) {
            logger.error("Failed to load mappings for index with name '{}'", config.auditAlias);
        }
        esUtil.addIndex(settings, mappings, indexName, config.auditAlias);
    }

}
