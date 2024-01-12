package com.sentrysoftware.metricshub.agent.config;

import static com.fasterxml.jackson.annotation.Nulls.SKIP;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.metricshub.agent.config.protocols.ProtocolsConfig;
import com.sentrysoftware.metricshub.agent.deserialization.AttributesDeserializer;
import com.sentrysoftware.metricshub.agent.deserialization.ConnectorVariablesDeserializer;
import com.sentrysoftware.metricshub.agent.deserialization.MonitorJobsDeserializer;
import com.sentrysoftware.metricshub.agent.deserialization.TimeDeserializer;
import com.sentrysoftware.metricshub.engine.connector.model.Connector;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.MonitorJob;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResourceConfig {

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

	private ProtocolsConfig protocols;

	@Default
	@JsonSetter(nulls = SKIP)
	@JsonDeserialize(using = ConnectorVariablesDeserializer.class)
	private Map<String, ConnectorVariables> variables = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

	@Default
	@JsonSetter(nulls = SKIP)
	private Set<String> selectConnectors = new HashSet<>();

	@Default
	@JsonSetter(nulls = SKIP)
	private Set<String> excludeConnectors = new HashSet<>();

	@JsonSetter(nulls = SKIP)
	@JsonDeserialize(using = MonitorJobsDeserializer.class)
	@Default
	private Map<String, MonitorJob> monitors = new HashMap<>();

	@JsonIgnore
	private Connector connector;

	@Default
	@JsonSetter(nulls = SKIP)
	private Set<String> includeConnectorTags = new HashSet<>();

	/**
	 * Creates and returns a shallow copy of all the fields in this
	 * ResourceConfig object except the attributes map which is deeply copied.
	 *
	 * @return A new ResourceConfig object with the same property values as this one.
	 */
	public ResourceConfig copy() {
		return ResourceConfig
			.builder()
			.loggerLevel(loggerLevel)
			.outputDirectory(outputDirectory)
			.collectPeriod(collectPeriod)
			.discoveryCycle(discoveryCycle)
			.alertingSystemConfig(alertingSystemConfig)
			.sequential(sequential)
			.resolveHostnameToFqdn(resolveHostnameToFqdn)
			.jobTimeout(jobTimeout)
			.attributes(
				attributes
					.entrySet()
					.stream()
					.collect(
						Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, HashMap::new)
					)
			)
			.metrics(metrics)
			.protocols(protocols)
			.variables(variables)
			.selectConnectors(selectConnectors)
			.excludeConnectors(excludeConnectors)
			.includeConnectorTags(includeConnectorTags)
			.connector(connector)
			.build();
	}
}
