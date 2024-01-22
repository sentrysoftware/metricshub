package com.sentrysoftware.metricshub.agent.config;

import static com.fasterxml.jackson.annotation.Nulls.SKIP;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.metricshub.agent.config.exporter.ExporterConfig;
import com.sentrysoftware.metricshub.agent.config.exporter.OtlpExporterConfig;
import com.sentrysoftware.metricshub.agent.config.otel.OtelCollectorConfig;
import com.sentrysoftware.metricshub.agent.deserialization.AttributesDeserializer;
import com.sentrysoftware.metricshub.agent.deserialization.TimeDeserializer;
import com.sentrysoftware.metricshub.agent.helper.AgentConstants;
import com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AgentConfig represents the configuration for the MetricsHub agent. It includes settings for
 * job pool size, logger level, output directory, collect period, discovery cycle, alerting system configuration,
 * sequential mode, hostname resolution, job timeout, OpenTelemetry (OTel) collector configuration,
 * exporter configuration, custom attributes, custom metrics, and resource group configurations.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AgentConfig {

	/**
	 * Default problem template for alerts
	 */
	public static final String PROBLEM_DEFAULT_TEMPLATE =
		"Problem on ${FQDN} with ${MONITOR_NAME}.${NEWLINE}${NEWLINE}${ALERT_DETAILS}${NEWLINE}${NEWLINE}${FULLREPORT}";
	/**
	 * Default job pool size
	 */
	public static final int DEFAULT_JOB_POOL_SIZE = 20;
	/**
	 * Default job collect period in seconds
	 */
	public static final long DEFAULT_COLLECT_PERIOD = 120;
	/**
	 * Default discovery cycle in minutes
	 */
	public static final int DEFAULT_DISCOVERY_CYCLE = 30;

	@Default
	private int jobPoolSize = DEFAULT_JOB_POOL_SIZE;

	@Default
	@JsonSetter(nulls = SKIP)
	private String loggerLevel = "error";

	@Default
	@JsonSetter(nulls = SKIP)
	private String outputDirectory = AgentConstants.DEFAULT_OUTPUT_DIRECTORY.toString();

	@Default
	@JsonDeserialize(using = TimeDeserializer.class)
	private long collectPeriod = DEFAULT_COLLECT_PERIOD;

	@Default
	private int discoveryCycle = DEFAULT_DISCOVERY_CYCLE;

	@Default
	@JsonSetter(nulls = SKIP)
	private AlertingSystemConfig alertingSystemConfig = AlertingSystemConfig.builder().build();

	private boolean sequential;

	@Default
	private boolean resolveHostnameToFqdn = true;

	@Default
	@JsonSetter(nulls = SKIP)
	@JsonDeserialize(using = TimeDeserializer.class)
	private long jobTimeout = MetricsHubConstants.DEFAULT_JOB_TIMEOUT;

	@Default
	@JsonSetter(nulls = SKIP)
	private OtelCollectorConfig otelCollector = OtelCollectorConfig.builder().build();

	@Default
	@JsonSetter(nulls = SKIP)
	private ExporterConfig exporter = ExporterConfig.builder().build();

	@Default
	@JsonSetter(nulls = SKIP)
	@JsonDeserialize(using = AttributesDeserializer.class)
	private Map<String, String> attributes = new HashMap<>();

	@Default
	@JsonSetter(nulls = SKIP)
	private Map<String, Double> metrics = new HashMap<>();

	@Default
	@JsonSetter(nulls = SKIP)
	private Map<String, ResourceConfig> resources = new HashMap<>();

	@Default
	@JsonSetter(nulls = SKIP)
	private Map<String, ResourceGroupConfig> resourceGroups = new HashMap<>();

	/**
	 * Whether the {@link OtlpExporterConfig} is present or not
	 *
	 * @return boolean value
	 */
	public boolean hasOtlpExporterConfig() {
		return hasExporterConfig() && exporter.getOtlp() != null;
	}

	/**
	 * Whether the {@link ExporterConfig} is present or not
	 *
	 * @return boolean value
	 */
	public boolean hasExporterConfig() {
		return exporter != null;
	}

	/**
	 * Build a new empty instance
	 *
	 * @return {@link AgentConfig} object
	 */
	public static AgentConfig empty() {
		return AgentConfig.builder().build();
	}
}
