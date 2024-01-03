package org.swasth.dp.retry.task;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swasth.dp.core.function.ContextEnrichmentFunction;
import org.swasth.dp.core.job.BaseJobConfig;
import org.swasth.dp.core.job.FlinkKafkaConnector;
import org.swasth.dp.core.util.FlinkUtil;
import org.swasth.dp.retry.functions.RetryProcessFunction;
import scala.Option;
import scala.Some;

import java.io.File;
import java.util.Map;

public class RetryStreamTask {
    
    private RetryConfig config;
    private FlinkKafkaConnector kafkaConnector;
    private final static Logger logger = LoggerFactory.getLogger(RetryStreamTask.class);
    public RetryStreamTask(RetryConfig config, FlinkKafkaConnector kafkaConnector){
        this.config = config;
        this.kafkaConnector = kafkaConnector;
    }

    public static void main(String[] args) {
        Option<String> configFilePath = new Some<String>(ParameterTool.fromArgs(args).get("config.file.path"));
        Config conf = configFilePath.map(path -> ConfigFactory.parseFile(new File(path)).resolve())
                .getOrElse(() -> ConfigFactory.load("resources/retry.conf").withFallback(ConfigFactory.systemEnvironment()));
        RetryConfig config = new RetryConfig(conf,"Retry-Job");
        FlinkKafkaConnector kafkaConnector = new FlinkKafkaConnector(config);
        RetryStreamTask retryTask = new RetryStreamTask(config, kafkaConnector);
        try {
            retryTask.process(config);
        } catch (Exception e) {
            logger.error("Error while processing the job, exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    void process(BaseJobConfig baseJobConfig) throws Exception {
        StreamExecutionEnvironment env = FlinkUtil.getExecutionContext(baseJobConfig);
        KafkaSource kafkaConsumer = kafkaConnector.kafkaMapSource(config.kafkaInputTopic);

        SingleOutputStreamOperator<Map<String,Object>> enrichedStream = env.fromSource(kafkaConsumer, WatermarkStrategy.noWatermarks(), config.RetryConsumer)
                .uid(config.RetryConsumer).setParallelism(config.consumerParallelism)
                .rebalance()
                .process(new ContextEnrichmentFunction(config, TypeExtractor.getForClass(String.class))).setParallelism(config.downstreamOperatorsParallelism);

        enrichedStream.getSideOutput(config.enrichedOutputTag())
                .process(new RetryProcessFunction(config)).setParallelism(config.downstreamOperatorsParallelism);

        System.out.println(config.jobName() + " is processing");
        env.execute(config.jobName());
    }
    
}
