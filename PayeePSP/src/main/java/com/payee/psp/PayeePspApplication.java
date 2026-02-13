package com.payee.psp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PayeePspApplication {

	public static void main(String[] args) {
		SpringApplication.run(PayeePspApplication.class, args);
	}

}
