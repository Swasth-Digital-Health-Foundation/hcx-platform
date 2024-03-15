package org.swasth.dp.notification.trigger.task;

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
import org.swasth.dp.notification.trigger.functions.NotificationTriggerProcessFunction;
import scala.Option;
import scala.Some;

import java.io.File;
import java.util.Map;

public class NotificationTriggerStreamTask {

    private final static Logger logger = LoggerFactory.getLogger(NotificationTriggerStreamTask.class);
    private NotificationTriggerConfig config;
    private FlinkKafkaConnector kafkaConnector;

    public NotificationTriggerStreamTask(NotificationTriggerConfig config, FlinkKafkaConnector kafkaConnector){
        this.config = config;
        this.kafkaConnector = kafkaConnector;
    }

    public static void main(String[] args) {
        Option<String> configFilePath = new Some<String>(ParameterTool.fromArgs(args).get("config.file.path"));
        Config conf = configFilePath.map(path -> ConfigFactory.parseFile(new File(path)).resolve())
                .getOrElse(() -> ConfigFactory.load("resources/notification-trigger.conf").withFallback(ConfigFactory.systemEnvironment()));
        NotificationTriggerConfig config = new NotificationTriggerConfig(conf,"Notification-Trigger-Job");
        FlinkKafkaConnector kafkaConnector = new FlinkKafkaConnector(config);
        NotificationTriggerStreamTask notificationTriggerTask = new NotificationTriggerStreamTask(config, kafkaConnector);
        try {
            notificationTriggerTask.process(config);
        } catch (Exception e) {
            logger.error("Error while processing the job, exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void process(BaseJobConfig baseJobConfig) throws Exception {
        StreamExecutionEnvironment env = FlinkUtil.getExecutionContext(baseJobConfig);
        KafkaSource<Map<String,Object>> kafkaConsumer = kafkaConnector.kafkaMapSource(config.kafkaInputTopic);
        env.enableCheckpointing(config.checkpointingInterval());
        env.getCheckpointConfig().setCheckpointTimeout(config.checkpointingTimeout());
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(config.checkpointingPauseSeconds());
        SingleOutputStreamOperator<Map<String,Object>> eventStream = env.fromSource(kafkaConsumer, WatermarkStrategy.noWatermarks(), config.notificationTriggerConsumer)
                .uid(config.notificationTriggerConsumer).setParallelism(config.consumerParallelism)
                .rebalance()
                .process(new NotificationTriggerProcessFunction(config)).setParallelism(config.downstreamOperatorsParallelism);

        /** Sink for notify events */
        eventStream.getSideOutput(config.notifyOutputTag).sinkTo(kafkaConnector.kafkaStringSink(config.kafkaOutputTopic))
                .name(config.notifyProducer).uid(config.notifyProducer).setParallelism(config.downstreamOperatorsParallelism);

        System.out.println(config.jobName() + " is processing");
        env.execute(config.jobName());
    }
}
