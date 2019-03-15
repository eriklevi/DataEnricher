package com.example.DataEnricher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DataEnricherApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataEnricherApplication.class, args);
	}
}
