package org.swasth.dp.notification.task;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swasth.dp.core.function.ContextEnrichmentFunction;
import org.swasth.dp.core.function.SubscriptionEnrichmentFunction;
import org.swasth.dp.core.job.BaseJobConfig;
import org.swasth.dp.core.job.FlinkKafkaConnector;
import org.swasth.dp.core.util.FlinkUtil;
import org.swasth.dp.notification.functions.SubscriptionDispatcherFunction;
import org.swasth.dp.notification.functions.SubscriptionFilterFunction;
import scala.Option;
import scala.Some;

import java.io.File;
import java.util.Map;

public class SubscriptionStreamTask {

    private final static Logger logger = LoggerFactory.getLogger(NotificationStreamTask.class);
    private NotificationConfig config;
    private FlinkKafkaConnector kafkaConnector;

    public SubscriptionStreamTask(NotificationConfig config, FlinkKafkaConnector kafkaConnector){
        this.config = config;
        this.kafkaConnector = kafkaConnector;
    }

    public static void main(String[] args) {
        Option<String> configFilePath = new Some<String>(ParameterTool.fromArgs(args).get("config.file.path"));
        Config conf = configFilePath.map(path -> ConfigFactory.parseFile(new File(path)).resolve())
                .getOrElse(() -> ConfigFactory.load("resources/notification.conf").withFallback(ConfigFactory.systemEnvironment()));
        NotificationConfig config = new NotificationConfig(conf,"Subscription-Job");
        FlinkKafkaConnector kafkaConnector = new FlinkKafkaConnector(config);
        SubscriptionStreamTask subscriptionStreamTask = new SubscriptionStreamTask(config, kafkaConnector);
        try {
            subscriptionStreamTask.process(config);
        } catch (Exception e) {
            logger.error("Error while processing the job, exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    void process(BaseJobConfig baseJobConfig) throws Exception {
        StreamExecutionEnvironment env = FlinkUtil.getExecutionContext(baseJobConfig);
        SourceFunction<Map<String,Object>> kafkaConsumer = kafkaConnector.kafkaMapSource(config.subscriptionInputTopic);
        env.enableCheckpointing(config.checkpointingInterval());
        env.getCheckpointConfig().setCheckpointTimeout(config.checkpointingTimeout());
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(config.checkpointingPauseSeconds());
        //Filter the records based on the action type
        SingleOutputStreamOperator<Map<String,Object>> filteredStream = env.addSource(kafkaConsumer, config.subscriptionConsumer)
                .uid(config.subscriptionConsumer).setParallelism(config.consumerParallelism).rebalance()
                .process(new SubscriptionFilterFunction(config)).setParallelism(config.downstreamOperatorsParallelism);

        //Do the context enrichment for subscribe and unsubscribe events
        SingleOutputStreamOperator<Map<String,Object>> enrichedSubscriptionStream = filteredStream.getSideOutput(config.subscribeOutputTag())
                .process(new SubscriptionEnrichmentFunction(config, TypeExtractor.getForClass(String.class))).setParallelism(config.downstreamOperatorsParallelism);

        //Do the context enrichment for on_subscribe events
        SingleOutputStreamOperator<Map<String,Object>> enrichedOnSubscriptionStream = filteredStream.getSideOutput(config.onSubscribeOutputTag())
                .process(new SubscriptionEnrichmentFunction(config, TypeExtractor.getForClass(String.class))).setParallelism(config.downstreamOperatorsParallelism);

        //Dispatch subscribe and unsubscribe events
        enrichedSubscriptionStream.getSideOutput(config.enrichedSubscriptionsOutputTag())
                .process(new SubscriptionDispatcherFunction(config)).setParallelism(config.downstreamOperatorsParallelism);

        //Dispatch on_subscribe events
        enrichedOnSubscriptionStream.getSideOutput(config.enrichedSubscriptionsOutputTag())
                .process(new SubscriptionDispatcherFunction(config)).setParallelism(config.downstreamOperatorsParallelism);

        System.out.println(config.jobName() + " is processing");
        env.execute(config.jobName());
    }
}
