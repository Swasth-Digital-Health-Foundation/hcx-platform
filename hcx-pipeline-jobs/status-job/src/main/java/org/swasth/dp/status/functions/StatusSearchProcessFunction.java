package org.swasth.dp.status.functions;


import org.apache.flink.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swasth.dp.core.function.BaseDispatcherFunction;
import org.swasth.dp.core.function.ValidationResult;
import org.swasth.dp.core.job.Metrics;
import org.swasth.dp.core.util.JSONUtil;
import org.swasth.dp.core.util.PostgresConnect;
import org.swasth.dp.core.util.PostgresConnectionConfig;
import org.swasth.dp.status.task.StatusSearchConfig;
import scala.reflect.ManifestFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

public class StatusSearchProcessFunction extends BaseDispatcherFunction {

    private Logger logger = LoggerFactory.getLogger(StatusSearchProcessFunction.class);
    private StatusSearchConfig config;
    private PostgresConnect postgresConnect = null;

    public StatusSearchProcessFunction(StatusSearchConfig config) {
        super(config);
        this.config = config;
    }

    @Override
    public void open(Configuration parameters) {
        super.open(parameters);
        if(postgresConnect == null) {
            this.postgresConnect = new PostgresConnect(new PostgresConnectionConfig(
                    config.postgresUser,
                    config.postgresPassword,
                    config.postgresDb,
                    config.postgresHost,
                    config.postgresPort,
                    config.postgresMaxConnections));
        }
    }

    @Override
    public void close() throws Exception {
        super.close();
        this.postgresConnect.closeConnection();
    }

    @Override
    public ValidationResult validate(Map<String, Object> event) {
        // TODO: Add domain specific validations
        return new ValidationResult(true, null);
    }

    @Override
    public Map<String, Object> getPayload(Map<String, Object> event) throws Exception {
        String payloadRefId = getPayloadRefId(event);
        System.out.println("Fetching payload from postgres for mid: " + payloadRefId);
        String postgresQuery = String.format("SELECT data FROM %s WHERE mid = '%s'", config.postgresTable, payloadRefId);
        PreparedStatement preparedStatement = postgresConnect.getConnection().prepareStatement(postgresQuery);
        try {
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                String payload = resultSet.getString(1);
                return JSONUtil.deserialize(payload, ManifestFactory.classType(Map.class));
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

    @Override
    public void audit(Map<String, Object> event, boolean status, Context context, Metrics metrics) throws Exception{
        context.output(config.auditOutputTag(), JSONUtil.serialize(createAuditRecord(event,"AUDIT")));
    }


}


