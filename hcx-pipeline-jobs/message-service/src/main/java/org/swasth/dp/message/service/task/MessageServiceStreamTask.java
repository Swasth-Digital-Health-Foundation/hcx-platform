package org.swasth.dp.message.service.task;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swasth.dp.core.job.BaseJobConfig;
import org.swasth.dp.core.job.FlinkKafkaConnector;
import org.swasth.dp.core.util.FlinkUtil;
import org.swasth.dp.message.service.functions.EmailDispatcher;
import org.swasth.dp.message.service.functions.MessageFilterFunction;
import org.swasth.dp.message.service.functions.SMSDispatcher;
import scala.Option;
import scala.Some;

import java.io.File;
import java.util.Map;

public class MessageServiceStreamTask {

    private final static Logger logger = LoggerFactory.getLogger(MessageServiceStreamTask.class);
    private MessageServiceConfig config;
    private FlinkKafkaConnector kafkaConnector;

    public MessageServiceStreamTask(MessageServiceConfig config, FlinkKafkaConnector kafkaConnector){
        this.config = config;
        this.kafkaConnector = kafkaConnector;
    }

    public static void main(String[] args) {
        Option<String> configFilePath = new Some<String>(ParameterTool.fromArgs(args).get("config.file.path"));
        Config conf = configFilePath.map(path -> ConfigFactory.parseFile(new File(path)).resolve())
                .getOrElse(() -> ConfigFactory.load("resources/message-service.conf").withFallback(ConfigFactory.systemEnvironment()));
        MessageServiceConfig config = new MessageServiceConfig(conf,"Message-Service-Job");
        FlinkKafkaConnector kafkaConnector = new FlinkKafkaConnector(config);
        MessageServiceStreamTask messageServiceStreamTask = new MessageServiceStreamTask(config, kafkaConnector);
        try {
            messageServiceStreamTask.process(config);
        } catch (Exception e) {
            logger.error("Error while processing the job, exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void process(BaseJobConfig baseJobConfig) throws Exception {
        StreamExecutionEnvironment env = FlinkUtil.getExecutionContext(baseJobConfig);
        KafkaSource kafkaConsumer = kafkaConnector.kafkaMapSource(config.inputTopic);
        env.enableCheckpointing(config.checkpointingInterval());
        env.getCheckpointConfig().setCheckpointTimeout(config.checkpointingTimeout());
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(config.checkpointingPauseSeconds());
        SingleOutputStreamOperator<Map<String,Object>> eventStream = env.fromSource(kafkaConsumer, WatermarkStrategy.noWatermarks(), config.messageServiceConsumer)
                .uid(config.messageServiceConsumer).setParallelism(config.consumerParallelism)
                .rebalance()
                .process(new MessageFilterFunction(config)).setParallelism(config.downstreamOperatorsParallelism);

        // Dispatch SMS Events
        eventStream.getSideOutput(config.smsOutputTag).process(new SMSDispatcher(config))
                .setParallelism(config.smsDispatcherParallelism);

        // Dispatch Email Events
        eventStream.getSideOutput(config.emailOutputTag).process(new EmailDispatcher(config))
                .setParallelism(config.emailDispatcherParallelism);

        System.out.println(config.jobName() + " is processing");
        env.execute(config.jobName());
    }
}
