package org.swasth.dp.notification.functions;

import org.apache.flink.streaming.api.functions.sink.SinkFunction;

public class LoggingSink implements SinkFunction<String> {
    private String topicName;

    public LoggingSink(String topicName) {
        this.topicName = topicName;
    }

    @Override
    public void invoke(String value, Context context) {
        // Log the sink operation
        System.out.println("Data sent to Kafka topic '" + topicName + "': " + value);
    }
}
