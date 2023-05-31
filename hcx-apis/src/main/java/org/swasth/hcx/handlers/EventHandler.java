package org.swasth.hcx.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.swasth.auditindexer.function.AuditIndexer;
import org.swasth.common.dto.Request;
import org.swasth.common.exception.ClientException;
import org.swasth.common.helpers.EventGenerator;
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.JSONUtils;
import org.swasth.common.utils.PayloadUtils;
import org.swasth.kafka.client.IEventService;
import org.swasth.postgresql.IDatabaseService;

import java.sql.ResultSet;
import java.util.Map;

import static org.swasth.common.utils.Constants.*;

@Component
public class EventHandler {

    private static final Logger logger = LoggerFactory.getLogger(EventHandler.class);

    @Autowired
    protected Environment env;

    @Autowired
    private IEventService kafkaClient;

    @Autowired
    protected IDatabaseService postgreSQLClient;

    @Autowired
    protected AuditIndexer auditIndexer;

    @Autowired
    protected EventGenerator eventGenerator;

    @Value("${postgres.tablename}")
    private String postgresTableName;

    @Value("${kafka.topic.audit}")
    private String auditTopic;

    @Value("${kafka.topic.retry}")
    private String retryTopic;

    public void processAndSendEvent(String metadataTopic, Request request) throws Exception {
        String payloadTopic = env.getProperty(KAFKA_TOPIC_PAYLOAD);
        String key = request.getHcxSenderCode();
        // TODO: check and remove writing payload event to kafka
        String payloadEvent = eventGenerator.generatePayloadEvent(request);
        String metadataEvent = eventGenerator.generateMetadataEvent(request);
        String query = String.format("INSERT INTO %s (mid,data,action,status,retrycount,lastupdatedon) VALUES ('%s','%s','%s','%s',%d,%d)",
                postgresTableName, request.getMid(), JSONUtils.serialize(PayloadUtils.removeParticipantDetails(request.getPayload())),
                request.getApiAction(), QUEUED_STATUS, 0, System.currentTimeMillis());
        logger.info("Mid: " + request.getMid() + " :: Event: " + metadataEvent);
        postgreSQLClient.execute(query);
        kafkaClient.send(payloadTopic, key, payloadEvent);
        kafkaClient.send(metadataTopic, key, metadataEvent);
        if (request.getApiAction().equalsIgnoreCase(NOTIFICATION_NOTIFY)) {
            auditIndexer.createDocument(eventGenerator.createNotifyAuditEvent(request));
        } else {
            auditIndexer.createDocument(eventGenerator.generateAuditEvent(request));
        }
        logger.info("Request processed and event is pushed to kafka");
    }

    public void createAudit(Map<String,Object> event) throws Exception {
        kafkaClient.send(auditTopic , (String) ((Map<String,Object>) event.get(Constants.OBJECT)).get(Constants.TYPE) , JSONUtils.serialize(event));
    }

    public void createRetryEvent(String mid) throws Exception {
        String query = String.format("SELECT * from %s WHERE mid ='%s'", postgresTableName, mid);
        ResultSet resultSet = (ResultSet) postgreSQLClient.executeQuery(query);
        if(resultSet.next()){
            String action = resultSet.getString(Constants.ACTION);
            Request request = new Request(JSONUtils.deserialize(resultSet.getString(Constants.DATA), Map.class), action);
            request.setMid(resultSet.getString(Constants.MID));
            request.setApiAction(action);
            String event = eventGenerator.generateMetadataEvent(request);
            kafkaClient.send(retryTopic, request.getHcxSenderCode(), event);
            logger.info("Retry request is processed successfully :: mid: {}", mid);
        } else {
            throw new ClientException("Invalid mid, request does not exist");
        }
    }

}
