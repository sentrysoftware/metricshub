package com.sentrysoftware.hardware.agent.configuration;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.hardware.agent.dto.MultiHostsConfigurationDto;
import com.sentrysoftware.hardware.agent.dto.exporter.ExporterConfigDto;
import com.sentrysoftware.hardware.agent.dto.exporter.OtlpConfigDto;

class OtelConfigTest {

	private static final String HTTP_GRPC_ENDPOINT = "http://localhost:4317";
	private static final String HTTPS_GRPC_ENDPOINT = "https://localhost:4317";

	@Test
	void testGetSecurityFilePath() {
		// Null certificate file configured, the default is loaded
		assertTrue(OtelConfig.getSecurityFilePath("src/test/resources/security", "otel.crt", null, HTTPS_GRPC_ENDPOINT)
				.isPresent());
		// Empty certificate file configured, the default is loaded
		assertTrue(OtelConfig.getSecurityFilePath("src/test/resources/security", "otel.crt", "", HTTPS_GRPC_ENDPOINT)
				.isPresent());
		// Blank certificate file, the default is loaded
		assertTrue(OtelConfig.getSecurityFilePath("src/test/resources/security", "otel.crt", " ", HTTPS_GRPC_ENDPOINT)
				.isPresent());
		// Certificate file not found, it cannot be loaded
		final String randomFilename = UUID.randomUUID().toString();
		assertFalse(OtelConfig
				.getSecurityFilePath("src/test/resources/security", "otel.crt", randomFilename, HTTPS_GRPC_ENDPOINT)
				.isPresent());
		// HTTP instead of HTTPS, no certificate to load
		assertTrue(OtelConfig.getSecurityFilePath("src/test/resources/security", "otel.crt", null, HTTP_GRPC_ENDPOINT)
				.isEmpty());
		// The certificate file is found
		assertFalse(OtelConfig.getSecurityFilePath("src/test/resources/security", "otel.crt",
				"src/test/resources/security/otel.crt", HTTPS_GRPC_ENDPOINT).isEmpty());
	}

	@Test
	void testOtelSdkConfiguration() {
		assertDoesNotThrow(
				() -> new OtelConfig().otelSdkConfiguration(new MultiHostsConfigurationDto(), HTTPS_GRPC_ENDPOINT));
	}

	@Test
	void testOtelSdkConfigurationHeaders() {

		{
			final Map<String, String> sdkConfig = new OtelConfig().otelSdkConfiguration(MultiHostsConfigurationDto.empty(),
					HTTPS_GRPC_ENDPOINT);
			assertTrue(sdkConfig.containsKey("otel.exporter.otlp.headers"));
		}

		{
			final MultiHostsConfigurationDto mHCD = MultiHostsConfigurationDto.builder()
					.exporter(ExporterConfigDto
							.builder()
							.otlp(OtlpConfigDto.builder().headers(null).build())
							.build())
					.build();
			final Map<String, String> sdkConfig = new OtelConfig().otelSdkConfiguration(mHCD, HTTPS_GRPC_ENDPOINT);
			assertFalse(sdkConfig.containsKey("otel.exporter.otlp.headers"));
		}

		{
			final MultiHostsConfigurationDto mHCD = MultiHostsConfigurationDto
					.builder()
					.exporter(ExporterConfigDto
							.builder()
							.otlp(OtlpConfigDto.builder().headers(Collections.emptyMap()).build())
							.build())
					.build();
			final Map<String, String> sdkConfig = new OtelConfig().otelSdkConfiguration(mHCD, HTTPS_GRPC_ENDPOINT);
			assertFalse(sdkConfig.containsKey("otel.exporter.otlp.headers"));
		}

		{
			final MultiHostsConfigurationDto mHCD = MultiHostsConfigurationDto
					.builder()
					.exporter(ExporterConfigDto.builder().otlp(null).build())
					.build();
			final Map<String, String> sdkConfig = new OtelConfig().otelSdkConfiguration(mHCD, HTTPS_GRPC_ENDPOINT);
			assertFalse(sdkConfig.containsKey("otel.exporter.otlp.headers"));
		}

		{
			final MultiHostsConfigurationDto mHCD = MultiHostsConfigurationDto
					.builder()
					.exporter(null)
					.build();
			final Map<String, String> sdkConfig = new OtelConfig().otelSdkConfiguration(mHCD, HTTPS_GRPC_ENDPOINT);
			assertFalse(sdkConfig.containsKey("otel.exporter.otlp.headers"));
		}

	}
}
