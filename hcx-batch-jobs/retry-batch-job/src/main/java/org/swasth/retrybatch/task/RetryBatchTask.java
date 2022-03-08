package org.swasth.retrybatch.task;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swasth.retrybatch.functions.RetryFunction;

public class RetryBatchTask {

	private final static Logger logger = LoggerFactory.getLogger(RetryBatchTask.class);

	public static void main(String[] args) {
		Config conf = ConfigFactory.load().withFallback(ConfigFactory.systemEnvironment());
		RetryConfig config = new RetryConfig(conf);
		try {
			RetryFunction retryFunction = new RetryFunction(config);
			retryFunction.process();
		} catch (Exception e) {
			logger.error("Error while running the batch process: " + e.getMessage());
			e.printStackTrace();
		}
	}

}
