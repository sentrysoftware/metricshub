package com.sentrysoftware.hardware.cli.configuration;

import org.jline.reader.LineReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sentrysoftware.hardware.cli.component.pwd.PasswordReader;

@Configuration
public class PasswordReaderConfig {

	@Bean
	public PasswordReader passwordReader(LineReader lineReader) {
		return new PasswordReader(lineReader);
	}
}
