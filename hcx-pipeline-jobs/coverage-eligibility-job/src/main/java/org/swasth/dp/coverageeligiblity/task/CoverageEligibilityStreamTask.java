package org.swasth.dp.coverageeligiblity.task;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.swasth.dp.core.function.ContextEnrichmentFunction;
import org.swasth.dp.core.job.BaseJobConfig;
import org.swasth.dp.core.job.FlinkKafkaConnector;
import org.swasth.dp.core.util.FlinkUtil;
import org.swasth.dp.coverageeligiblity.functions.CoverageEligibilityProcessFunction;
import scala.Option;
import scala.Some;

import java.io.File;
import java.util.Map;

public class CoverageEligibilityStreamTask {

    private CoverageEligibilityConfig config;
    private FlinkKafkaConnector kafkaConnector;
    public CoverageEligibilityStreamTask(CoverageEligibilityConfig config, FlinkKafkaConnector kafkaConnector){
        this.config = config;
        this.kafkaConnector = kafkaConnector;
    }

    public static void main(String[] args) {
        Option<String> configFilePath = new Some<String>(ParameterTool.fromArgs(args).get("config.file.path"));
        Config conf = configFilePath.map(path -> ConfigFactory.parseFile(new File(path)).resolve())
                .getOrElse(() -> ConfigFactory.load("resources/coverage-eligibility.conf").withFallback(ConfigFactory.systemEnvironment()));
        CoverageEligibilityConfig config = new CoverageEligibilityConfig(conf,"Coverage-Eligibility-Job");
        FlinkKafkaConnector kafkaConnector = new FlinkKafkaConnector(config);
        CoverageEligibilityStreamTask task = new CoverageEligibilityStreamTask(config, kafkaConnector);
        try {
            task.process(config);
        } catch (Exception e) {
            //TODO Add loggers
            e.printStackTrace();
        }
    }

    public void process(BaseJobConfig baseJobConfig) throws Exception {
        StreamExecutionEnvironment env = FlinkUtil.getExecutionContext(baseJobConfig);
        KafkaSource kafkaConsumer = kafkaConnector.kafkaMapSource(config.kafkaInputTopic);

        SingleOutputStreamOperator<Map<String,Object>> enrichedStream = env.fromSource(kafkaConsumer, WatermarkStrategy.noWatermarks(), config.coverageEligibilityConsumer)
                .uid(config.coverageEligibilityConsumer).setParallelism(config.consumerParallelism)
                .rebalance()
                .process(new ContextEnrichmentFunction(config, TypeExtractor.getForClass(String.class))).setParallelism(config.downstreamOperatorsParallelism);

        SingleOutputStreamOperator<Map<String,Object>> eventStream = enrichedStream.getSideOutput(config.enrichedOutputTag())
                .process(new CoverageEligibilityProcessFunction(config)).setParallelism(config.downstreamOperatorsParallelism);

        /** Sink for audit events */
        eventStream.getSideOutput(config.auditOutputTag()).sinkTo(kafkaConnector.kafkaStringSink(config.auditTopic()))
                .name(config.auditProducer()).uid(config.auditProducer()).setParallelism(config.downstreamOperatorsParallelism);

        System.out.println(config.jobName() + " is processing");
        env.execute(config.jobName());
    }

}