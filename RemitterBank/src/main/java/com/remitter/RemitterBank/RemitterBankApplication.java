package com.remitter.RemitterBank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RemitterBankApplication {

	public static void main(String[] args) {
		SpringApplication.run(RemitterBankApplication.class, args);
	}

}
