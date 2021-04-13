package com.sentrysoftware.matrix.engine.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.sentrysoftware.matrix.connector.ConnectorStore;

@Configuration
@ComponentScan(basePackages =  "com.sentrysoftware.matrix")
public class ApplicationBeans {

	@Bean
	public ConnectorStore store() {
		return ConnectorStore.getInstance();
	}

}
