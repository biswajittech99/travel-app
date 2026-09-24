package com.example.delaydetectionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class DelaydetectionserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DelaydetectionserviceApplication.class, args);
	}

}
