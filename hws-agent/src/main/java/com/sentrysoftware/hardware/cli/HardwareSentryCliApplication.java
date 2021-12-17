package com.sentrysoftware.hardware.cli;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HardwareSentryCliApplication {

	public static void main(String[] args) {

		System.setProperty("log4j2.configurationFile", "log4j2-cli.xml");

		final SpringApplication application = new SpringApplication(HardwareSentryCliApplication.class);

		application.setAdditionalProfiles("cli");

		System.exit(SpringApplication.exit(application.run(args)));

	}
}
