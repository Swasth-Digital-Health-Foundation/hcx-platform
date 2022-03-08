package org.swasth.dp.retrybatch.task;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swasth.dp.retrybatch.functions.RetryFunction;

public class RetryBatchTask {

	private final static Logger logger = LoggerFactory.getLogger(RetryBatchTask.class);

	public static void main(String[] args) {
		Config conf = ConfigFactory.load("resources/retry.conf").withFallback(ConfigFactory.systemEnvironment());
		RetryConfig config = new RetryConfig(conf,"Retry-Job");
		try {
			RetryFunction retryFunction = new RetryFunction(config);
			retryFunction.process();
		} catch (Exception e) {
			logger.error("Error while running the batch process: " + e.getMessage());
			e.printStackTrace();
		}
	}

}
