package org.swasth.dp.status.task;

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
import org.swasth.dp.status.functions.StatusSearchProcessFunction;
import scala.Option;
import scala.Some;

import java.io.File;
import java.util.Map;

public class StatusSearchStreamTask {

	private StatusSearchConfig config;
	private FlinkKafkaConnector kafkaConnector;
	public StatusSearchStreamTask(StatusSearchConfig config, FlinkKafkaConnector kafkaConnector){
		this.config = config;
		this.kafkaConnector = kafkaConnector;
	}

	public static void main(String[] args) {
		Option<String> configFilePath = new Some<String>(ParameterTool.fromArgs(args).get("config.file.path"));
		Config conf = configFilePath.map(path -> ConfigFactory.parseFile(new File(path)).resolve())
				.getOrElse(() -> ConfigFactory.load("resources/status.conf").withFallback(ConfigFactory.systemEnvironment()));
		//Config conf = ConfigFactory.parseResources("resources/status.conf");
		StatusSearchConfig config = new StatusSearchConfig(conf,"StatusSearch-Job");
		FlinkKafkaConnector kafkaConnector = new FlinkKafkaConnector(config);
		StatusSearchStreamTask searchTask = new StatusSearchStreamTask(config, kafkaConnector);
		try {
			searchTask.process(config);
		} catch (Exception e) {
			//TODO Add loggers
			e.printStackTrace();
		}
	}

	private void process(BaseJobConfig baseJobConfig) throws Exception {
		StreamExecutionEnvironment env = FlinkUtil.getExecutionContext(baseJobConfig);
		SourceFunction<Map<String,Object>> kafkaConsumer = kafkaConnector.kafkaMapSource(config.kafkaInputTopic);

		SingleOutputStreamOperator<Map<String,Object>> enrichedStream = env.addSource(kafkaConsumer, config.statusSearchConsumer)
				.uid(config.statusSearchConsumer).setParallelism(config.consumerParallelism)
				.rebalance()
				.process(new ContextEnrichmentFunction(config, TypeExtractor.getForClass(String.class))).setParallelism(config.downstreamOperatorsParallelism);

		enrichedStream.getSideOutput(config.enrichedOutputTag())
				.process(new StatusSearchProcessFunction(config)).setParallelism(config.downstreamOperatorsParallelism);

		System.out.println(config.jobName() + " is processing");
		env.execute(config.jobName());
	}

}
