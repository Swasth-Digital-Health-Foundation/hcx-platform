package org.swasth.dp.task;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.swasth.dp.core.function.ContextEnrichmentFunction;
import org.swasth.dp.core.job.BaseJobConfig;
import org.swasth.dp.core.job.FlinkKafkaConnector;
import org.swasth.dp.core.util.FlinkUtil;
import org.swasth.dp.functions.*;
import scala.Option;
import scala.Some;

import java.io.File;
import java.util.Map;

public class ProtocolRequestProcessorStreamTask {

    private ProtocolRequestProcessorConfig config;

    private FlinkKafkaConnector kafkaConnector;

    public ProtocolRequestProcessorStreamTask(ProtocolRequestProcessorConfig config, FlinkKafkaConnector kafkaConnector) {
        this.config = config;
        this.kafkaConnector = kafkaConnector;
    }

    public static void main(String[] args) {
        Option<String> configFilePath = new Some<String>(ParameterTool.fromArgs(args).get("config.file.path"));
        Config conf = configFilePath.map(path -> ConfigFactory.parseFile(new File(path)).resolve())
                .getOrElse(() -> ConfigFactory.load("resources/protocol-request-processor.conf").withFallback(ConfigFactory.systemEnvironment()));
        ProtocolRequestProcessorConfig config = new ProtocolRequestProcessorConfig(conf, "protocol-request-processor-Job");
        FlinkKafkaConnector kafkaConnector = new FlinkKafkaConnector(config);
        ProtocolRequestProcessorStreamTask task = new ProtocolRequestProcessorStreamTask(config, kafkaConnector);
        try {
            task.process(config);
        } catch (Exception e) {
            //TODO Add loggers
            e.printStackTrace();
        }
    }

    void process(BaseJobConfig baseJobConfig) throws Exception {
        StreamExecutionEnvironment env = FlinkUtil.getExecutionContext(baseJobConfig);
        SourceFunction<Map<String, Object>> kafkaConsumer = kafkaConnector.kafkaMapSource(config.kafkaInputTopic);

        SingleOutputStreamOperator<Map<String, Object>> enrichedStream = env.addSource(kafkaConsumer, config.protocolRequestConsumer)
                .uid(config.protocolRequestConsumer).setParallelism(config.consumerParallelism)
                .rebalance()
                .process(new ContextEnrichmentFunction(config, TypeExtractor.getForClass(String.class))).setParallelism(config.downstreamOperatorsParallelism);

        // filter based on the action type
        SingleOutputStreamOperator<Map<String, Object>> eventStream = enrichedStream.getSideOutput(config.enrichedOutputTag())
                .process(new ProcessorFilterFunction(config)).setParallelism(config.downstreamOperatorsParallelism);

        // coverageEligibility Job
        SingleOutputStreamOperator<Map<String,Object>> coverageEligibilityJob = eventStream.getSideOutput(config.coverageEligibilityOutputTag)
                .process(new CoverageEligibilityProcessFunction(config)).setParallelism(config.downstreamOperatorsParallelism);
        sendAuditToKafka(coverageEligibilityJob, config, kafkaConnector,"coverage-eligibility-audit-sink");

        // claims Job
        SingleOutputStreamOperator<Map<String,Object>> claimsJob = eventStream.getSideOutput(config.claimOutputTag)
                .process(new ClaimsProcessFunction(config)).setParallelism(config.downstreamOperatorsParallelism);
        sendAuditToKafka(claimsJob, config, kafkaConnector,"claims-audit-events-sink-");

        // preAuth Job
        SingleOutputStreamOperator<Map<String,Object>> preAuthJob = eventStream.getSideOutput(config.preAuthOutputTag)
                .process(new PreauthProcessFunction(config)).setParallelism(config.downstreamOperatorsParallelism);
        sendAuditToKafka(preAuthJob, config, kafkaConnector,"preauth-audit-events-sink");

        // predetermination Job
//        SingleOutputStreamOperator<Map<String,Object>> preDeterminationJob = eventStream.getSideOutput(config.preDeterminationOutputTag)
//                .process(new PredeterminationProcessFunction(config)).setParallelism(config.downstreamOperatorsParallelism);
//        sendAuditToKafka(preDeterminationJob, config, kafkaConnector,"predetermination-audit-events-sink");

        // payment Job
//        SingleOutputStreamOperator<Map<String,Object>> paymentJob = eventStream.getSideOutput(config.paymentOutputTag)
//                .process(new PaymentsProcessFunction(config)).setParallelism(config.downstreamOperatorsParallelism);
//        sendAuditToKafka(paymentJob, config, kafkaConnector,"payment-audit-events-sink");

        // fetch Job
//        SingleOutputStreamOperator<Map<String,Object>> fetchJob = eventStream.getSideOutput(config.fetchOutputTag)
//                .process(new PaymentsProcessFunction(config)).setParallelism(config.downstreamOperatorsParallelism);
//        sendAuditToKafka(fetchJob, config, kafkaConnector,"fetch-audit-events-sink");

        // retry Job
//        SingleOutputStreamOperator<Map<String,Object>> retryJob = eventStream.getSideOutput(config.retryOutputTag)
//                .process(new RetryProcessFunction(config)).setParallelism(config.downstreamOperatorsParallelism);
//        sendAuditToKafka(retryJob, config, kafkaConnector,"retry-audit-events-sink");

        // communication Job
//        SingleOutputStreamOperator<Map<String,Object>> communicationJob = eventStream.getSideOutput(config.communicationOutputTag)
//                .process(new CommunicationProcessFunction(config)).setParallelism(config.downstreamOperatorsParallelism);
//        sendAuditToKafka(communicationJob, config, kafkaConnector,"communication-audit-events-sink");

        // status search Job
//        SingleOutputStreamOperator<Map<String,Object>> statusSearchJob = eventStream.getSideOutput(config.statusSearchOutputTag)
//                .process(new StatusSearchProcessFunction(config)).setParallelism(config.downstreamOperatorsParallelism);
//        sendAuditToKafka(statusSearchJob, config, kafkaConnector,"status-audit-events-sink");

        System.out.println(config.jobName() + " is processing");
        env.execute(config.jobName());
    }


    private <T> void sendAuditToKafka(SingleOutputStreamOperator<Map<String, T>> eventStream, ProtocolRequestProcessorConfig config, FlinkKafkaConnector kafkaConnector,String uid) {
        eventStream.getSideOutput(config.auditOutputTag()).addSink(kafkaConnector.kafkaStringSink(config.auditTopic()))
                .name(config.auditProducer()).uid(uid).setParallelism(config.downstreamOperatorsParallelism);
    }
}
