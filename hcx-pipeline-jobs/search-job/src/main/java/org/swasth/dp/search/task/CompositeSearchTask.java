package org.swasth.dp.search.task;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.swasth.dp.core.job.BaseJobConfig;
import org.swasth.dp.core.job.FlinkKafkaConnector;
import org.swasth.dp.core.util.FlinkUtil;
import org.swasth.dp.search.functions.CompositeSearchProcessFunction;
import org.swasth.dp.search.functions.ResponseDispatchProcessFunction;
import org.swasth.dp.search.functions.SearchDispatchProcessFunction;
import scala.Option;
import scala.Some;

import java.io.File;
import java.util.Map;

public class CompositeSearchTask {

    private CompositeSearchConfig searchConfig;
    private FlinkKafkaConnector kafkaConnector;

    public CompositeSearchTask(CompositeSearchConfig searchConfig,FlinkKafkaConnector kafkaConnector){
        this.searchConfig = searchConfig;
        this.kafkaConnector = kafkaConnector;
    }

    public static void main(String[] args) {
        Option<String> configFilePath = new Some<String>(ParameterTool.fromArgs(args).get("config.file.path"));
        Config config = configFilePath.map(path -> ConfigFactory.parseFile(new File(path)).resolve())
                .getOrElse(() -> ConfigFactory.load("search.conf").withFallback(ConfigFactory.systemEnvironment()));

        CompositeSearchConfig searchConfig = new CompositeSearchConfig(config,"CompositeSearch-Job");
        FlinkKafkaConnector kafkaConnector = new FlinkKafkaConnector(searchConfig);
        CompositeSearchTask searchTask = new CompositeSearchTask(searchConfig,kafkaConnector);
        try {
            searchTask.process(searchConfig);
        } catch (Exception e) {
            //TODO Add loggers
            e.printStackTrace();
        }
    }



    private void process(BaseJobConfig baseJobConfig) throws Exception {
        StreamExecutionEnvironment env = FlinkUtil.getExecutionContext(baseJobConfig);

        SourceFunction<Map<String,Object>> kafkaConsumer = kafkaConnector.kafkaMapSource(searchConfig.kafkaInputTopic);

       SingleOutputStreamOperator<Map<String,Object>> dataStream = env.addSource(kafkaConsumer, searchConfig.searchConsumer)
                .uid(searchConfig.searchConsumer).setParallelism(searchConfig.consumerParallelism)
                .rebalance()
                .process(new CompositeSearchProcessFunction(searchConfig)).setParallelism(searchConfig.downstreamOperatorsParallelism);

        dataStream.getSideOutput(searchConfig.auditOutputTag()).addSink(kafkaConnector.kafkaStringSink(searchConfig.auditTopic())).name(searchConfig.auditProducer()).uid(searchConfig.auditProducer()).setParallelism(searchConfig.downstreamOperatorsParallelism);

        SingleOutputStreamOperator<Map<String,Object>> dispatchStream = dataStream.getSideOutput(searchConfig.searchOutputTag)
                .process(new SearchDispatchProcessFunction(searchConfig)).setParallelism(searchConfig.downstreamOperatorsParallelism);
        /** Sink for audit events */
        dispatchStream.getSideOutput(searchConfig.auditOutputTag()).addSink(kafkaConnector.kafkaStringSink(searchConfig.auditTopic())).name(searchConfig.auditProducer()).uid(searchConfig.auditProducer()).setParallelism(searchConfig.downstreamOperatorsParallelism);

        SingleOutputStreamOperator<Map<String,Object>> responseStream = dataStream.getSideOutput(searchConfig.searchResponseOutputTag).process(new ResponseDispatchProcessFunction(searchConfig)).setParallelism(searchConfig.downstreamOperatorsParallelism);
        responseStream.getSideOutput(searchConfig.auditOutputTag()).addSink(kafkaConnector.kafkaStringSink(searchConfig.auditTopic())).name(searchConfig.auditProducer()).uid(searchConfig.auditProducer()).setParallelism(searchConfig.downstreamOperatorsParallelism);

        System.out.println(searchConfig.jobName() + " is processing");
        env.execute(searchConfig.jobName());
    }

}