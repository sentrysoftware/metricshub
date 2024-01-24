package org.sentrysoftware.metricshub.engine.connector.model;

import static com.fasterxml.jackson.annotation.Nulls.SKIP;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sentrysoftware.metricshub.engine.connector.deserializer.custom.ExtendsDeserializer;
import org.sentrysoftware.metricshub.engine.connector.deserializer.custom.SourcesDeserializer;
import org.sentrysoftware.metricshub.engine.connector.model.common.TranslationTable;
import org.sentrysoftware.metricshub.engine.connector.model.identity.ConnectorIdentity;
import org.sentrysoftware.metricshub.engine.connector.model.metric.MetricDefinition;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.MonitorJob;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.Source;

/**
 * Represents a connector with its configuration, metrics, monitors, and other settings.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Connector implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * The identity of the connector.
	 */
	@JsonProperty("connector")
	private ConnectorIdentity connectorIdentity;

	/**
	 * Set of connector names that the current connector extends.
	 */
	@JsonProperty("extends")
	@JsonSetter(nulls = SKIP)
	@JsonDeserialize(using = ExtendsDeserializer.class)
	@Default
	private Set<String> extendsConnectors = new LinkedHashSet<>();

	/**
	 * Map of metric names to their definitions.
	 */
	@Default
	private Map<String, MetricDefinition> metrics = new HashMap<>();

	/**
	 * Map of constant names to their values.
	 */
	@Default
	@JsonSetter(nulls = SKIP)
	private Map<String, String> constants = new HashMap<>();

	/**
	 * Set of sudo commands.
	 */
	@Default
	@JsonSetter(nulls = SKIP)
	private Set<String> sudoCommands = new HashSet<>();

	/**
	 * Map of pre-sources, where each key is the name of the pre-source and the value is its definition.
	 */
	@Default
	@JsonDeserialize(using = SourcesDeserializer.class)
	@JsonSetter(nulls = SKIP)
	private Map<String, Source> pre = new HashMap<>();

	/**
	 * Map of monitor jobs, where each key is the name of the monitor job and the value is its definition.
	 */
	@Default
	private Map<String, MonitorJob> monitors = new LinkedHashMap<>();

	/**
	 * Map of translation tables, where each key is the name of the translation table and the value is its definition.
	 */
	@Default
	@JsonSetter(nulls = SKIP)
	private Map<String, TranslationTable> translations = new HashMap<>();

	/**
	 * Set of source types associated with the connector.
	 */
	@Default
	private Set<Class<? extends Source>> sourceTypes = new HashSet<>();

	/**
	 * List of source dependencies specified as sets of source names.
	 */
	@Default
	private List<Set<String>> preSourceDep = new ArrayList<>();

	/**
	 * Get the connector identity and create it if it is not created yet.
	 *
	 * @return The connector identity.
	 */
	public ConnectorIdentity getOrCreateConnectorIdentity() {
		if (connectorIdentity == null) {
			connectorIdentity = new ConnectorIdentity();
		}

		return connectorIdentity;
	}

	/**
	 * Get the compiled filename of the connector, if the compiled filename
	 * cannot be retrieved then the {@link IllegalStateException} is thrown
	 *
	 * @return String value
	 */
	public String getCompiledFilename() {
		if (connectorIdentity != null) {
			final String compiledFilename = connectorIdentity.getCompiledFilename();
			if (compiledFilename != null) {
				return compiledFilename;
			}
		}
		throw new IllegalStateException("No compiled file name found.");
	}
}
