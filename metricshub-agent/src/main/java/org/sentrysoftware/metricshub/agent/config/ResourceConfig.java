package org.sentrysoftware.metricshub.agent.config;

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

import static com.fasterxml.jackson.annotation.Nulls.SKIP;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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
import org.sentrysoftware.metricshub.agent.deserialization.AttributesDeserializer;
import org.sentrysoftware.metricshub.agent.deserialization.ConnectorVariablesDeserializer;
import org.sentrysoftware.metricshub.agent.deserialization.ExtensionProtocolsDeserializer;
import org.sentrysoftware.metricshub.agent.deserialization.MonitorJobsDeserializer;
import org.sentrysoftware.metricshub.engine.configuration.ConnectorVariables;
import org.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.Connector;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.MonitorJob;
import org.sentrysoftware.metricshub.engine.deserialization.TimeDeserializer;

/**
 * Configuration class representing the configuration for a resource in
 * MetricsHub. It includes settings such as logger level, output directory,
 * collection period, discovery cycle, alerting system configuration, sequential
 * execution, resolving hostname to FQDN, job timeout, attributes, metrics,
 * protocols configuration, variables, selected connectors, excluded connectors,
 * monitor jobs, included connector tags, and a connector instance.
 */
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

	@JsonDeserialize(using = TimeDeserializer.class)
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
	@JsonDeserialize(using = ExtensionProtocolsDeserializer.class)
	private Map<String, IConfiguration> protocols = new HashMap<>();

	@Default
	@JsonSetter(nulls = SKIP)
	@JsonDeserialize(using = ConnectorVariablesDeserializer.class)
	private Map<String, ConnectorVariables> variables = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

	@Default
	@JsonSetter(nulls = SKIP)
	private Set<String> connectors = new HashSet<>();

	@JsonSetter(nulls = SKIP)
	@JsonDeserialize(using = MonitorJobsDeserializer.class)
	@Default
	private Map<String, MonitorJob> monitors = new HashMap<>();

	@JsonIgnore
	private Connector connector;

	private String stateSetCompression;

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
			.connectors(connectors)
			.connector(connector)
			.stateSetCompression(stateSetCompression)
			.build();
	}
}
