package com.sentrysoftware.hardware.cli;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HardwareSentryCliApplication {

	public static void main(String[] args) {

		System.exit(SpringApplication.exit(SpringApplication.run(HardwareSentryCliApplication.class, args)));

	}
}
