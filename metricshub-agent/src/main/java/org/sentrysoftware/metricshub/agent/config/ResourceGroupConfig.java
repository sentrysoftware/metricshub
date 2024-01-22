package org.sentrysoftware.metricshub.agent.config;

import static com.fasterxml.jackson.annotation.Nulls.SKIP;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sentrysoftware.metricshub.agent.deserialization.AttributesDeserializer;
import org.sentrysoftware.metricshub.agent.deserialization.TimeDeserializer;

/**
 * Configuration class representing the configuration for a resource group in
 * MetricsHub. It includes settings such as logger level, output directory,
 * collection period, discovery cycle, alerting system configuration, sequential
 * execution, resolving hostname to FQDN, job timeout, attributes, metrics, and
 * nested resource configurations.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResourceGroupConfig {

	private String loggerLevel;
	private String outputDirectory;

	@JsonDeserialize(using = TimeDeserializer.class)
	private Long collectPeriod;

	private Integer discoveryCycle;
	private AlertingSystemConfig alertingSystemConfig;
	private Boolean sequential;
	private Boolean resolveHostnameToFqdn;
	private Long jobTimeout;

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
}
