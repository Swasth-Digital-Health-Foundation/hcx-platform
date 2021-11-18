package org.swasth.hcx.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.HashMap;

@SpringBootTest(classes = {KafkaEventGenerator.class})
public class KafkaEventGeneratorTests {

    @Autowired
    KafkaEventGenerator kafkaEventGenerator;

    @Test
    public void check_generatePayloadEvent() throws JsonProcessingException {
        String result = kafkaEventGenerator.generatePayloadEvent("test_123", new HashMap<>());
        assert (!result.isEmpty());
    }

}
