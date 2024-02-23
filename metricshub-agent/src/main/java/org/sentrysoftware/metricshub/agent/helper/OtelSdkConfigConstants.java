package org.sentrysoftware.metricshub.agent.helper;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub Agent
 * ჻჻჻჻჻჻
 * Copyright 2023 - 2024 Sentry Software
 * ჻჻჻჻჻჻
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱
 */

import java.time.Duration;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Configuration class defining properties used to configure OTEL SDK.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OtelSdkConfigConstants {

	/**
	 * Property used to configure the interval, in milliseconds, between the start of two export attempts.
	 */
	public static final String OTEL_METRIC_EXPORT_INTERVAL = "otel.metric.export.interval";

	/**
	 * Property used to configure the transport protocol to use on OTLP log requests.
	 * Options include grpc and http/protobuf. Default is grpc.
	 */
	public static final String OTEL_EXPORTER_OTLP_LOGS_PROTOCOL = "otel.exporter.otlp.logs.protocol";

	/**
	 * Property used to configure the transport protocol to use on OTLP metric requests.
	 * Options include grpc and http/protobuf. Default is grpc.
	 */
	public static final String OTEL_EXPORTER_OTLP_METRICS_PROTOCOL = "otel.exporter.otlp.metrics.protocol";

	/**
	 * Property used to configure the OTLP logs endpoint to connect to. Must be a URL with a scheme of either http or https based on the use of TLS.
	 * Default is http://localhost:4317 when protocol is grpc.
	 * Set http://localhost:4318/v1/logs when protocol is http/protobuf.
	 */
	public static final String OTEL_EXPORTER_OTLP_LOGS_ENDPOINT = "otel.exporter.otlp.logs.endpoint";

	/**
	 * Property used to configure the OTLP traces, metrics, and logs endpoint to connect to. Must be a URL with a scheme of either http or https based on the use of TLS.
	 * If protocol is http/protobuf the version and signal will be appended to the path (e.g. v1/traces, v1/metrics, or v1/logs).
	 * Default is http://localhost:4317 when protocol is grpc.
	 * Set http://localhost:4318/v1/{signal} when protocol is http/protobuf.
	 */
	public static final String OTEL_EXPORTER_OTLP_ENDPOINT = "otel.exporter.otlp.endpoint";

	/**
	 * Property used to configure the path to the file containing trusted certificates to use when verifying an OTLP trace, metric, or log server's TLS credentials.
	 * The file should contain one or more X.509 certificates in PEM format.
	 * By default the host platform's trusted root certificates are used.
	 */
	public static final String OTEL_EXPORTER_OTLP_CERTIFICATE = "otel.exporter.otlp.certificate";

	/**
	 * Property used to configure the OTLP metrics endpoint to connect to. Must be a URL with a scheme of either http or https based on the use of TLS.
	 * Default is http://localhost:4317 when protocol is grpc, and http://localhost:4318/v1/metrics when protocol is http/protobuf.
	 */
	public static final String OTEL_EXPORTER_OTLP_METRICS_ENDPOINT = "otel.exporter.otlp.metrics.endpoint";

	/**
	 * Property used to configure the path to the file containing trusted certificates to use when verifying an OTLP metric server's TLS credentials.
	 * The file should contain one or more X.509 certificates in PEM format.
	 * By default the host platform's trusted root certificates are used.
	 */
	public static final String OTEL_EXPORTER_OTLP_METRICS_CERTIFICATE = "otel.exporter.otlp.metrics.certificate";

	/**
	 * Property used to configure the path to the file containing trusted certificates to use when verifying an OTLP log server's TLS credentials.
	 * The file should contain one or more X.509 certificates in PEM format.
	 * By default the host platform's trusted root certificates are used.
	 */
	public static final String OTEL_EXPORTER_OTLP_LOGS_CERTIFICATE = "otel.exporter.otlp.logs.certificate";

	/**
	 * Property used to configure the OpenTelemetry exporter for logs. Default is otlp
	 */
	public static final String OTEL_LOGS_EXPORTER = "otel.logs.exporter";

	/**
	 * Property used to configure the OpenTelemetry exporter for metrics. Default is otlp
	 */
	public static final String OTEL_METRICS_EXPORTER = "otel.metrics.exporter";

	/**
	 * Default Metrics Export Interval
	 */
	public static final String DEFAULT_METRICS_EXPORT_INTERVAL = String.valueOf(Duration.ofDays(365 * 10L).toMillis());

	/**
	 * Default OTEL Exporter: OTLP
	 */
	public static final String DEFAULT_EXPORTER = "otlp";

	/**
	 * Default OTLP protocol: gRPC
	 */
	public static final String DEFAULT_GRPC_PROTOCOL = "grpc";

	/**
	 * Default OTLP Endpoint: http://localhost:431
	 */
	public static final String DEFAULT_OTLP_ENDPOINT = "http://localhost:4317";

	/**
	 * Default configuration
	 */
	// @formatter:off
	public static final Map<String, String> DEFAULT_CONFIGURATION = Map.of(
		OTEL_METRICS_EXPORTER, DEFAULT_EXPORTER,
		OTEL_LOGS_EXPORTER, DEFAULT_EXPORTER,
		OTEL_EXPORTER_OTLP_METRICS_ENDPOINT, DEFAULT_OTLP_ENDPOINT,
		OTEL_EXPORTER_OTLP_LOGS_ENDPOINT, DEFAULT_OTLP_ENDPOINT,
		OTEL_EXPORTER_OTLP_METRICS_PROTOCOL, DEFAULT_GRPC_PROTOCOL,
		OTEL_EXPORTER_OTLP_LOGS_PROTOCOL, DEFAULT_GRPC_PROTOCOL
	);
	// @formatter:on

}
