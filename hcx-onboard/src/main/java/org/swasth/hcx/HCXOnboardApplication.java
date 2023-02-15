package org.swasth.hcx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAspectJAutoProxy
@SpringBootApplication
@EnableAsync
public class HCXOnboardApplication {

	public static void main(String[] args) {
		SpringApplication.run(HCXOnboardApplication.class, args);
	}

}
