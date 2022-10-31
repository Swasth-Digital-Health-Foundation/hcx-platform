package org.swasth.dp.communication.task;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.swasth.dp.communication.functions.CommunicationProcessFunction;
import org.swasth.dp.core.function.ContextEnrichmentFunction;
import org.swasth.dp.core.job.BaseJobConfig;
import org.swasth.dp.core.job.FlinkKafkaConnector;
import org.swasth.dp.core.util.FlinkUtil;
import scala.Option;
import scala.Some;

import java.io.File;
import java.util.Map;

public class CommunicationStreamTask {

	private final CommunicationConfig config;
	private final FlinkKafkaConnector kafkaConnector;

	public CommunicationStreamTask(CommunicationConfig config, FlinkKafkaConnector kafkaConnector){
		this.config = config;
		this.kafkaConnector = kafkaConnector;
	}

	public static void main(String[] args) {
		Option<String> configFilePath = new Some<>(ParameterTool.fromArgs(args).get("config.file.path"));
		Config conf = configFilePath.map(path -> ConfigFactory.parseFile(new File(path)).resolve())
				.getOrElse(() -> ConfigFactory.load("resources/communication.conf").withFallback(ConfigFactory.systemEnvironment()));
		CommunicationConfig config = new CommunicationConfig(conf,"Communication-Job");
		FlinkKafkaConnector kafkaConnector = new FlinkKafkaConnector(config);
		CommunicationStreamTask task = new CommunicationStreamTask(config, kafkaConnector);
		try {
			task.process(config);
		} catch (Exception e) {
			//TODO Add loggers
			e.printStackTrace();
		}
	}

	void process(BaseJobConfig baseJobConfig) throws Exception {
		StreamExecutionEnvironment env = FlinkUtil.getExecutionContext(baseJobConfig);
		SourceFunction<Map<String,Object>> kafkaConsumer = kafkaConnector.kafkaMapSource(config.kafkaInputTopic);

		SingleOutputStreamOperator<Map<String,Object>> enrichedStream = env.addSource(kafkaConsumer, config.communicationConsumer)
				.uid(config.communicationConsumer).setParallelism(config.consumerParallelism)
				.rebalance()
				.process(new ContextEnrichmentFunction(config, TypeExtractor.getForClass(String.class))).setParallelism(config.downstreamOperatorsParallelism);

		SingleOutputStreamOperator<Map<String,Object>> eventStream = enrichedStream.getSideOutput(config.enrichedOutputTag())
				.process(new CommunicationProcessFunction(config)).setParallelism(config.downstreamOperatorsParallelism);

		/** Sink for audit events */
		eventStream.getSideOutput(config.auditOutputTag()).addSink(kafkaConnector.kafkaStringSink(config.auditTopic()))
				.name(config.auditProducer()).uid(config.auditProducer()).setParallelism(config.downstreamOperatorsParallelism);

		System.out.println(config.jobName() + " is processing");
		env.execute(config.jobName());
	}

}