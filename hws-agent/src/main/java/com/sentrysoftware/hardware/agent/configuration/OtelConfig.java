package com.sentrysoftware.hardware.agent.configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sentrysoftware.hardware.agent.dto.MultiHostsConfigurationDto;
import com.sentrysoftware.hardware.agent.dto.exporter.OtlpConfigDto;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class OtelConfig {

	private static final String SECURITY_DIR_NAME = "security";

	/**
	 * Get security file path. E.g certificate or key file path
	 * 
	 * @param fileDir         The directory of the security file
	 * @param defaultFilename the default security filename
	 * @param file            Defines the security file
	 * @param otlpEndpoint    OpenTelemetry gRPC OTLP receiver endpoint
	 * @return Optional of {@link Path}
	 */
	static Optional<String> getSecurityFilePath(@NonNull final String fileDir, @NonNull final String defaultFilename,
			final String file, @NonNull final String otlpEndpoint) {
		final Path securityFilePath;
		// No security file path? we will use the default one
		if (file == null || file.isBlank()) {
			securityFilePath = ConfigHelper
					.getSubPath(String.format("%s/%s", fileDir, defaultFilename));
		} else {
			securityFilePath = Path.of(file);
		}

		// No security for HTTP
		if (otlpEndpoint.toLowerCase().startsWith("http://")) {
			log.debug(
					"There is no Otel security file to load for the gRPC OTLP exporter[endpoint: {}]. The security file {} is loaded only for https connections.",
					otlpEndpoint, securityFilePath);
			return Optional.empty();
		}

		// No file? we cannot proceed any more...
		if (!Files.exists(securityFilePath)) {
			log.debug("There is no Otel security file to load. Expected path: {}.", securityFilePath);
			return Optional.empty();
		}

		return Optional.of(securityFilePath.toAbsolutePath().toString());
	}

	@Bean
	public Map<String, String> otelSdkConfiguration(final MultiHostsConfigurationDto multiHostsConfigurationDto) {

		final Map<String, String> properties = new HashMap<>();

		// Default OTLP endpoint
		String otlpEndpoint = "https://localhost:4317";

		properties.put("otel.metrics.exporter", "otlp");
		properties.put("otel.logs.exporter", "otlp");
		properties.put("otel.exporter.otlp.endpoint", otlpEndpoint);
		properties.put("otel.metric.export.interval", String.valueOf(Duration.ofDays(365 * 10L).toMillis()));

		String certificatesFileToTrust = null;

		// Does the user configured OTLP?
		if (multiHostsConfigurationDto.hasExporterOtlpConfig()) {

			final OtlpConfigDto otlpConfig = multiHostsConfigurationDto.getExporter().getOtlp();

			// Endpoint overridden?
			if (otlpConfig.hasEndpoint()) {
				otlpEndpoint = otlpConfig.getEndpoint();
				properties.put("otel.exporter.otlp.endpoint", otlpEndpoint);
			}

			// Headers
			otlpConfig.getHeadersInOtlpFormat()
					.ifPresent(headers -> properties.put("otel.exporter.otlp.headers", headers));

			certificatesFileToTrust = otlpConfig.getTrustedCertificatesFile();
		}

		// Certificate file path
		getSecurityFilePath(SECURITY_DIR_NAME, "otel.crt", certificatesFileToTrust, otlpEndpoint)
			.ifPresent(
				trustedCertificatesFile -> properties.put("otel.exporter.otlp.certificate", trustedCertificatesFile));

		return properties;
	}
}
