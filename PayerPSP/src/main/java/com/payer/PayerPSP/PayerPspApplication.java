package com.payer.PayerPSP;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class
PayerPspApplication {

	public static void main(String[] args) {
				SpringApplication.run(PayerPspApplication.class, args);
	}

}
