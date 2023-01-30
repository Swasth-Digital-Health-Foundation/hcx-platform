package org.swasth.hcx;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.swasth.hcx.controllers.HealthController;

@EnableAspectJAutoProxy
@SpringBootApplication
public class HCXApplication {


	public static void main(String[] args) {
		SpringApplication.run(HCXApplication.class, args);
		HealthController healthController = new HealthController();
		System.out.println(healthController.serviceHealth().getBody());
	}

}
