package com.example.authgatewayservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


@SpringBootApplication(scanBasePackages = {
        "com.example.authgatewayservice",
        "org.example.common.dto"
})
@EnableDiscoveryClient
public class AuthgatewayserviceApplication {

	public static void main(String[] args) {

        SpringApplication.run(AuthgatewayserviceApplication.class, args);
	}

}
