package org.swasth.kafka.client;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.TimeoutException;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.common.exception.ServerException;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class KafkaClient implements IEventService {

    private String kafkaServerUrl;
    private KafkaProducer producer;
    private AdminClient adminClient;

    public KafkaClient(String url) {
        this.kafkaServerUrl = url;
        this.producer = createProducer();
        this.adminClient = kafkaAdminClient();
    }

    public void send(String topic, String key, String message) throws Exception {
        try {
            producer.send(new ProducerRecord<>(topic, key, message));
        } catch (TimeoutException e){
            throw new ServerException(ErrorCodes.SERVER_ERR_GATEWAY_TIMEOUT, "Timeout error in pushing event to kafka: " + e.getMessage());
        } catch (Exception e){
            throw new ServerException(ErrorCodes.INTERNAL_SERVER_ERROR, "Error in pushing event to kafka : " + e.getMessage());
        }
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

    public boolean isHealthy(){
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
