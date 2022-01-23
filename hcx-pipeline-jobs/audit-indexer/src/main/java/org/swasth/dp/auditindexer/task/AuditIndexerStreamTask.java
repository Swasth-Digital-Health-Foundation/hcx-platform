package org.swasth.dp.auditindexer.task;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swasth.dp.auditindexer.functions.AuditIndexerProcessFunction;
import org.swasth.dp.core.job.BaseJobConfig;
import org.swasth.dp.core.job.FlinkKafkaConnector;
import org.swasth.dp.core.util.FlinkUtil;
import scala.Option;
import scala.Some;

import java.io.File;
import java.util.Map;


public class AuditIndexerStreamTask {

	private final static Logger logger = LoggerFactory.getLogger(AuditIndexerStreamTask.class);
	private AuditIndexerConfig config;
	private FlinkKafkaConnector kafkaConnector;

	public AuditIndexerStreamTask(AuditIndexerConfig config, FlinkKafkaConnector kafkaConnector){
		this.config = config;
		this.kafkaConnector = kafkaConnector;
	}

	public static void main(String[] args) throws Exception {
		Option<String> configFilePath = new Some<String>(ParameterTool.fromArgs(args).get("config.file.path"));
		Config conf = configFilePath.map(path -> ConfigFactory.parseFile(new File(path)).resolve())
				.getOrElse(() -> ConfigFactory.load("resources/audit-indexer.conf").withFallback(ConfigFactory.systemEnvironment()));
		AuditIndexerConfig config = new AuditIndexerConfig(conf,"AuditIndexer-Job");
		FlinkKafkaConnector kafkaConnector = new FlinkKafkaConnector(config);
		AuditIndexerStreamTask auditIndexerStreamTask = new AuditIndexerStreamTask(config, kafkaConnector);
		try {
			auditIndexerStreamTask.process(config);
		} catch (Exception e) {
			logger.error("Error while processing audit indexer stream job: " + e.getMessage());
			throw e;
		}
	}

	private void process(BaseJobConfig baseJobConfig) throws Exception {
		StreamExecutionEnvironment env = FlinkUtil.getExecutionContext(baseJobConfig);
		SourceFunction<Map<String,Object>> kafkaConsumer = kafkaConnector.kafkaMapSource(config.kafkaInputTopic);

		env.addSource(kafkaConsumer, config.auditIndexerConsumer)
				.uid(config.auditIndexerConsumer).setParallelism(config.consumerParallelism)
				.rebalance()
				.process(new AuditIndexerProcessFunction(config)).setParallelism(config.parallelism);

		System.out.println(config.jobName() + " is processing");
		env.execute(config.jobName());
	}
}
