package com.teleops.teleops_ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TeleopsAiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TeleopsAiApplication.class, args);
	}

}
