package com.sentrysoftware.matrix.agent.helper;

import static com.sentrysoftware.matrix.agent.helper.AgentConstants.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.sentrysoftware.matrix.agent.config.AgentConfig;
import com.sentrysoftware.matrix.agent.config.exporter.OtlpExporterConfig;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class OtelConfigHelper {

	/**
	 * Get security file path. E.g certificate or key file path
	 * 
	 * @param securityFileDir         The directory of the security file
	 * @param defaultSecurityFilename The default security filename
	 * @param securityFile            Defines the security file
	 * @param otlpEndpoint            OpenTelemetry gRPC OTLP receiver endpoint
	 * @return Optional of {@link Path}
	 */
	static Optional<String> getSecurityFilePath(
		@NonNull final String securityFileDir,
		@NonNull final String defaultSecurityFilename,
		final String securityFile,
		@NonNull final String otlpEndpoint
	) {

		final Path securityFilePath;
		// No security file path? we will use the default one
		if (securityFile == null || securityFile.isBlank()) {
			securityFilePath = ConfigHelper.getSubPath(
				String.format(AgentConstants.FILE_PATH_FORMAT, securityFileDir, defaultSecurityFilename)
			);
		} else {
			securityFilePath = Path.of(securityFile);
		}

		// No security for HTTP
		if (otlpEndpoint.toLowerCase().startsWith("http://")) {
			log.debug(
				"There is no Otel security file to load for the gRPC OTLP exporter[endpoint: {}]. The security file {} is loaded only for https connections.",
				otlpEndpoint, securityFilePath
			);
			return Optional.empty();
		}

		// No file? we cannot proceed any more...
		if (!Files.exists(securityFilePath)) {
			log.debug("There is no Otel security file to load. Expected path: {}.", securityFilePath);
			return Optional.empty();
		}

		return Optional.of(securityFilePath.toAbsolutePath().toString());
	}

	/**
	 * Build OpenTelemetry configuration properties from the given agent configuration
	 * 
	 * @param agentConfig The agent's configuration where the exporter's configuration can be overridden
	 * @return Map of key-value pair used to configure the OpenTelemetry Java SDK exporter
	 */
	public static Map<String, String> buildOtelSdkConfiguration(final AgentConfig agentConfig) {

		final Map<String, String> properties = new HashMap<>();

		// Default OTLP endpoint
		String otlpEndpoint = "https://localhost:4317";

		properties.put("otel.metrics.exporter", "otlp");
		properties.put("otel.logs.exporter", "otlp");
		properties.put("otel.exporter.otlp.endpoint", otlpEndpoint);
		properties.put("otel.metric.export.interval", String.valueOf(Duration.ofDays(365 * 10L).toMillis()));

		String certificatesFileToTrust = null;

		// Does the user configured OTLP?
		if (agentConfig.hasOtlpExporterConfig()) {

			final OtlpExporterConfig otlpExporterConfig = agentConfig.getExporter().getOtlp();

			// Endpoint overridden?
			if (otlpExporterConfig.hasEndpoint()) {
				otlpEndpoint = otlpExporterConfig.getEndpoint();
				properties.put("otel.exporter.otlp.endpoint", otlpEndpoint);
			}

			// Headers
			otlpExporterConfig.getHeadersInOtlpFormat()
				.ifPresent(headers -> properties.put("otel.exporter.otlp.headers", headers));

			certificatesFileToTrust = otlpExporterConfig.getTrustedCertificatesFile();
		}

		// Certificate file path
		getSecurityFilePath(AgentConstants.SECURITY_DIRECTORY_NAME, DEFAULT_OTEL_CRT_FILENAME, certificatesFileToTrust, otlpEndpoint)
			.ifPresent(
				trustedCertificatesFile -> properties.put("otel.exporter.otlp.certificate", trustedCertificatesFile)
			);

		return properties;
	}
}
