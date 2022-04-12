package com.sentrysoftware.hardware.agent.configuration;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.hardware.agent.dto.MultiHostsConfigurationDTO;
import com.sentrysoftware.hardware.agent.dto.exporter.ExporterConfigDTO;
import com.sentrysoftware.hardware.agent.dto.exporter.OtlpConfigDTO;

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
				() -> new OtelConfig().otelSdkConfiguration(new MultiHostsConfigurationDTO(), HTTPS_GRPC_ENDPOINT));
	}

	@Test
	void testOtelSdkConfigurationHeaders() {

		{
			final Map<String, String> sdkConfig = new OtelConfig().otelSdkConfiguration(MultiHostsConfigurationDTO.empty(),
					HTTPS_GRPC_ENDPOINT);
			assertTrue(sdkConfig.containsKey("otel.exporter.otlp.headers"));
		}

		{
			final MultiHostsConfigurationDTO mHCD = MultiHostsConfigurationDTO.builder()
					.exporter(ExporterConfigDTO
							.builder()
							.otlp(OtlpConfigDTO.builder().headers(null).build())
							.build())
					.build();
			final Map<String, String> sdkConfig = new OtelConfig().otelSdkConfiguration(mHCD, HTTPS_GRPC_ENDPOINT);
			assertFalse(sdkConfig.containsKey("otel.exporter.otlp.headers"));
		}

		{
			final MultiHostsConfigurationDTO mHCD = MultiHostsConfigurationDTO
					.builder()
					.exporter(ExporterConfigDTO
							.builder()
							.otlp(OtlpConfigDTO.builder().headers(Collections.emptyMap()).build())
							.build())
					.build();
			final Map<String, String> sdkConfig = new OtelConfig().otelSdkConfiguration(mHCD, HTTPS_GRPC_ENDPOINT);
			assertFalse(sdkConfig.containsKey("otel.exporter.otlp.headers"));
		}

		{
			final MultiHostsConfigurationDTO mHCD = MultiHostsConfigurationDTO
					.builder()
					.exporter(ExporterConfigDTO.builder().otlp(null).build())
					.build();
			final Map<String, String> sdkConfig = new OtelConfig().otelSdkConfiguration(mHCD, HTTPS_GRPC_ENDPOINT);
			assertFalse(sdkConfig.containsKey("otel.exporter.otlp.headers"));
		}

		{
			final MultiHostsConfigurationDTO mHCD = MultiHostsConfigurationDTO
					.builder()
					.exporter(null)
					.build();
			final Map<String, String> sdkConfig = new OtelConfig().otelSdkConfiguration(mHCD, HTTPS_GRPC_ENDPOINT);
			assertFalse(sdkConfig.containsKey("otel.exporter.otlp.headers"));
		}

	}
}
