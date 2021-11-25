package org.swasth.job.task;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.OutputTag;
import org.swasth.job.connector.FlinkKafkaConnector;
import org.swasth.job.functions.DenormaliserFunction;


public class DenormaliserStreamTask{

	public static void main(String[] args) throws Exception {

		FlinkKafkaConnector kafkaConnector = new FlinkKafkaConnector();
		// set up the streaming execution environment
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		SingleOutputStreamOperator<String> denormaliserStream = env.addSource(kafkaConnector.kafkaStringSource(DenormaliserConfig.kafkaInputTopic))
				.name(DenormaliserConfig.eventConsumer)
				.uid(DenormaliserConfig.eventConsumer)
				.setParallelism(DenormaliserConfig.kafkaConsumerParallelism)
				.rebalance()
				.process(new DenormaliserFunction())
				.name(DenormaliserConfig.denormaliserFunction)
				.uid(DenormaliserConfig.denormaliserFunction)
				.setParallelism(DenormaliserConfig.parallelism);


		DataStream<String> invalidOutputStream = denormaliserStream.getSideOutput(DenormaliserConfig.invalidOutTag);
		invalidOutputStream.addSink(kafkaConnector.kafkaStringSink(DenormaliserConfig.kafkaInvalidTopic));

		denormaliserStream.addSink(kafkaConnector.kafkaStringSink(DenormaliserConfig.kafkaOutputTopic))
				.name(DenormaliserConfig.eventProducer);
		env.execute("Denormaliser Job");
	}
}
