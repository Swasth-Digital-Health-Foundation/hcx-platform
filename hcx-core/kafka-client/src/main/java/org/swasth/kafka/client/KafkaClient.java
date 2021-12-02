package org.swasth.kafka.client;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class KafkaClient {

    private String kafkaServerUrl;
    private KafkaProducer producer;

    public KafkaClient(String url) {
        this.kafkaServerUrl = url;
        this.producer = createProducer();
    }

    public void send(String topic, String key, String message) {
        producer.send(new ProducerRecord<>(topic, key, message));
    }

    private KafkaProducer<String,String> createProducer(){
        Properties props = new Properties();
        props.put("bootstrap.servers", kafkaServerUrl);
        props.put("client.id", "KafkaClientProducer");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        return new KafkaProducer<>(props);
    }

    private AdminClient kafkaAdminClient() {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", kafkaServerUrl);
        return AdminClient.create(properties);
    }

    public boolean health(){
        AdminClient adminClient = kafkaAdminClient();
        try
        {
            adminClient.listTopics(new ListTopicsOptions().timeoutMs(5000)).listings().get();
            return true;
        }
        catch (InterruptedException | ExecutionException e)
        {
            return false;
        }
    }

}
