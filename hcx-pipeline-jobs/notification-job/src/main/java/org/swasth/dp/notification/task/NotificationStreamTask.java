package org.swasth.dp.notification.task;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swasth.dp.core.job.BaseJobConfig;
import org.swasth.dp.core.job.FlinkKafkaConnector;
import org.swasth.dp.core.util.FlinkUtil;
import org.swasth.dp.notification.functions.NotificationProcessFunction;
import scala.Option;
import scala.Some;

import java.io.File;
import java.util.Map;

public class NotificationStreamTask {

    private final static Logger logger = LoggerFactory.getLogger(NotificationStreamTask.class);
    private NotificationConfig config;
    private FlinkKafkaConnector kafkaConnector;

    public NotificationStreamTask(NotificationConfig config, FlinkKafkaConnector kafkaConnector){
        this.config = config;
        this.kafkaConnector = kafkaConnector;
    }

    public static void main(String[] args) {
        Option<String> configFilePath = new Some<String>(ParameterTool.fromArgs(args).get("config.file.path"));
        Config conf = configFilePath.map(path -> ConfigFactory.parseFile(new File(path)).resolve())
                .getOrElse(() -> ConfigFactory.load("resources/notification.conf").withFallback(ConfigFactory.systemEnvironment()));
        NotificationConfig config = new NotificationConfig(conf,"Notification-Job");
        FlinkKafkaConnector kafkaConnector = new FlinkKafkaConnector(config);
        NotificationStreamTask notificationTask = new NotificationStreamTask(config, kafkaConnector);
        try {
            notificationTask.process(config);
        } catch (Exception e) {
            logger.error("Error while processing the job, exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void process(BaseJobConfig baseJobConfig) throws Exception {
        StreamExecutionEnvironment env = FlinkUtil.getExecutionContext(baseJobConfig);
        SourceFunction<Map<String,Object>> kafkaConsumer = kafkaConnector.kafkaMapSource(config.kafkaInputTopic);
        env.enableCheckpointing(config.checkpointingInterval());
        env.getCheckpointConfig().setCheckpointTimeout(config.checkpointingTimeout());
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(config.checkpointingPauseSeconds());
        env.addSource(kafkaConsumer, config.notificationConsumer)
                .uid(config.notificationConsumer).setParallelism(config.consumerParallelism)
                .rebalance()
                .process(new NotificationProcessFunction(config)).setParallelism(config.downstreamOperatorsParallelism);

        System.out.println(config.jobName() + " is processing");
        env.execute(config.jobName());
    }
    
}
