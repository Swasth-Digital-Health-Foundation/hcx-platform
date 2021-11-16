package org.swasth.job.task;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.swasth.job.Platform;
import org.swasth.job.connector.FlinkKafkaConnector;
import org.swasth.job.functions.DenormaliserFunction;


public class DenormaliserStreamTask{

	public static void main(String[] args) throws Exception {
		// set up the streaming execution environment
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		DataStream<String> denormaliserStream = env.fromSource(FlinkKafkaConnector.kafkaStringSource(DenormaliserConfig.kafkaInputTopic), WatermarkStrategy.noWatermarks(), DenormaliserConfig.eventConsumer)
				.uid(DenormaliserConfig.eventConsumer).setParallelism(DenormaliserConfig.kafkaConsumerParallelism)
						.rebalance().process(new DenormaliserFunction());

		denormaliserStream.sinkTo(FlinkKafkaConnector.kafkaStringSink(DenormaliserConfig.kafkaOutputTopic));

		env.execute("Denormaliser Job Executed");
	}
}
