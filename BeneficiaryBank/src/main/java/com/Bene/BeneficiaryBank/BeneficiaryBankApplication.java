package com.Bene.BeneficiaryBank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BeneficiaryBankApplication {

	public static void main(String[] args) {
		SpringApplication.run(BeneficiaryBankApplication.class, args);
	}

}
