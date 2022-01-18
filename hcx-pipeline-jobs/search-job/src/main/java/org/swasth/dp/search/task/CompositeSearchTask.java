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
import org.swasth.dp.search.functions.*;
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

    public static void main(String[] args) throws Exception {
        Option<String> configFilePath = new Some<String>(ParameterTool.fromArgs(args).get("config.file.path"));
        Config config = configFilePath.map(path -> ConfigFactory.parseFile(new File(path)).resolve())
                .getOrElse(() -> ConfigFactory.load("search.conf").withFallback(ConfigFactory.systemEnvironment()));

        CompositeSearchConfig searchConfig = new CompositeSearchConfig(config,"CompositeSearch-Job");
        FlinkKafkaConnector kafkaConnector = new FlinkKafkaConnector(searchConfig);
        CompositeSearchTask searchTask = new CompositeSearchTask(searchConfig,kafkaConnector);
        try {
            searchTask.process(searchConfig);
        } catch (Exception e) {
            throw e;
        }
    }



    private void process(BaseJobConfig baseJobConfig) throws Exception {
        StreamExecutionEnvironment env = FlinkUtil.getExecutionContext(baseJobConfig);

        SourceFunction<Map<String,Object>> kafkaConsumer = kafkaConnector.kafkaMapSource(searchConfig.getKafkaInputTopic());
        System.out.println("Input topic is:"+searchConfig.getKafkaInputTopic());

       SingleOutputStreamOperator<Map<String,Object>> dataStream = env.addSource(kafkaConsumer, searchConfig.getSearchConsumer())
                .uid(searchConfig.getSearchConsumer()).setParallelism(searchConfig.getConsumerParallelism())
                .rebalance()
                .process(new EventRouterFunction(searchConfig)).setParallelism(searchConfig.getDownstreamOperatorsParallelism());

        /** Sink for EventRouterFunction audit events */
        dataStream.getSideOutput(searchConfig.auditOutputTag()).addSink(kafkaConnector.kafkaStringSink(searchConfig.getKafkaAuditTopic())).name(searchConfig.auditSearch).uid(searchConfig.auditSearch).setParallelism(searchConfig.getDownstreamOperatorsParallelism());

        SingleOutputStreamOperator<Map<String,Object>> requestDispatchStream = dataStream.getSideOutput(searchConfig.searchRequestOutputTag)
                .process(new SearchRequestDispatchFunction(searchConfig)).setParallelism(searchConfig.getDownstreamOperatorsParallelism());
        /** Sink for SearchRequestDispatchFunction audit events */
        //requestDispatchStream.getSideOutput(searchConfig.auditOutputTag()).addSink(kafkaConnector.kafkaStringSink(searchConfig.auditTopic())).name(searchConfig.auditProducer()).uid(searchConfig.auditProducer()).setParallelism(searchConfig.getDownstreamOperatorsParallelism());


        SingleOutputStreamOperator<Map<String,Object>> responseDispatchStream = dataStream.getSideOutput(searchConfig.searchResponseOutputTag)
               .process(new SearchResponseDispatchFunction(searchConfig)).setParallelism(searchConfig.getDownstreamOperatorsParallelism());
        /** Sink for SearchResponseDispatchFunction audit events */
        //responseDispatchStream.getSideOutput(searchConfig.auditOutputTag()).addSink(kafkaConnector.kafkaStringSink(searchConfig.auditTopic())).name(searchConfig.auditProducer()).uid(searchConfig.auditProducer()).setParallelism(searchConfig.getDownstreamOperatorsParallelism());


      SingleOutputStreamOperator<Map<String,Object>> completeResponseStream = responseDispatchStream.getSideOutput(searchConfig.searchCompleteResponseOutputTag)
              .process(new SearchCompletionDispatchFunction(searchConfig)).setParallelism(searchConfig.getDownstreamOperatorsParallelism());
        /** Sink for SearchCompletionDispatchFunction audit events */
        //completeResponseStream.getSideOutput(searchConfig.auditOutputTag()).addSink(kafkaConnector.kafkaStringSink(searchConfig.auditTopic())).name(searchConfig.auditProducer()).uid(searchConfig.auditProducer()).setParallelism(searchConfig.getDownstreamOperatorsParallelism());

        System.out.println(searchConfig.jobName() + " is processing");
        env.execute(searchConfig.jobName());
    }

}