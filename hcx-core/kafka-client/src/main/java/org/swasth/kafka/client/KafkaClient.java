package org.swasth.kafka.client;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.swasth.common.exception.ClientException;
import org.swasth.common.exception.ErrorCodes;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class KafkaClient {

    private String kafkaServerUrl;

    public void setKafkaServerUrl(String url){
        this.kafkaServerUrl = url;
    }

    public void send(String topic, String key, String message) throws ClientException {
        if (validate(topic)) {
            KafkaProducer producer = createProducer();
            producer.send(new ProducerRecord<>(topic, key, message));
        }
		else {
            throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_TOPIC, "Topic with name: " + topic + ", does not exists.");
        }
    }

    public boolean validate(String topic) {
        KafkaConsumer consumer = createConsumer();
        Map topics = consumer.listTopics();
        return topics.keySet().contains(topic);
    }

    public KafkaProducer createProducer(){
        Properties props = new Properties();
        props.put("bootstrap.servers", kafkaServerUrl);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        return  producer;
    }

    public KafkaConsumer createConsumer(){
        Properties props = new Properties();
        props.put("bootstrap.servers", kafkaServerUrl);
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        return consumer;
    }

    public AdminClient kafkaAdminClient() {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", kafkaServerUrl);
        properties.put("connections.max.idle.ms", 10000);
        properties.put("request.timeout.ms", 5000);
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