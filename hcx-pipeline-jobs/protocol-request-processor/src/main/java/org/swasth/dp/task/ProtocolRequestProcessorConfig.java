package org.swasth.dp.task;

import com.typesafe.config.Config;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.util.OutputTag;
import org.swasth.dp.core.job.BaseJobConfig;

import java.util.Map;

public class ProtocolRequestProcessorConfig extends BaseJobConfig {

    private final Config config;
    // kafka
    public String kafkaInputTopic;

    // Consumers
    public String protocolRequestConsumer = "protocol-request-consumer";

    public OutputTag<Map<String, Object>> coverageEligibilityOutputTag = new OutputTag<>("coverage-eligibility-event", TypeInformation.of(new TypeHint<>() {}));
    public OutputTag<Map<String,Object>> preAuthOutputTag = new OutputTag<>("preauth-event", TypeInformation.of(new TypeHint<>() {}));
    public OutputTag<Map<String,Object>> preDeterminationOutputTag = new OutputTag<>("predetermination-event") {};
    public OutputTag<Map<String, Object>> claimOutputTag = new OutputTag<>("claim-event", TypeInformation.of(new TypeHint<>() {}));
    public OutputTag<Map<String, Object>> fetchOutputTag = new OutputTag<>("fetch-event", TypeInformation.of(new TypeHint<>() {}));
    public OutputTag<Map<String,Object>> retryOutputTag = new OutputTag<>("retry-event", TypeInformation.of(new TypeHint<>() {}));
    public OutputTag<Map<String,Object>> statusSearchOutputTag = new OutputTag<>("status-event", TypeInformation.of(new TypeHint<>() {}));
    public OutputTag<Map<String,Object>> communicationOutputTag = new OutputTag<>("communication-event", TypeInformation.of(new TypeHint<>() {}));
    public OutputTag<Map<String,Object>> paymentOutputTag = new OutputTag<>("payment-event", TypeInformation.of(new TypeHint<>() {}));

    public int consumerParallelism;
    public int downstreamOperatorsParallelism;

    public ProtocolRequestProcessorConfig(Config config, String jobName) {
        super(config, jobName);
        this.config = config;
        initValues();
    }

    private void initValues(){
        kafkaInputTopic = config.getString("kafka.input.topic");
        consumerParallelism = config.getInt("task.consumer.parallelism");
        downstreamOperatorsParallelism = config.getInt("task.downstream.operators.parallelism");

    }
}
