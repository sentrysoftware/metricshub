package org.sentrysoftware.metricshub.engine.connector.model;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub Engine
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

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Connector implements Serializable {

	private static final long serialVersionUID = 1L;

	@JsonProperty("connector")
	private ConnectorIdentity connectorIdentity;

	@JsonProperty("extends")
	@JsonSetter(nulls = SKIP)
	@JsonDeserialize(using = ExtendsDeserializer.class)
	@Default
	private Set<String> extendsConnectors = new LinkedHashSet<>();

	@Default
	private Map<String, MetricDefinition> metrics = new HashMap<>();

	@Default
	@JsonSetter(nulls = SKIP)
	private Map<String, String> constants = new HashMap<>();

	@Default
	@JsonSetter(nulls = SKIP)
	private Set<String> sudoCommands = new HashSet<>();

	@Default
	@JsonDeserialize(using = SourcesDeserializer.class)
	@JsonSetter(nulls = SKIP)
	private Map<String, Source> pre = new HashMap<>();

	@Default
	private Map<String, MonitorJob> monitors = new LinkedHashMap<>();

	@Default
	@JsonSetter(nulls = SKIP)
	private Map<String, TranslationTable> translations = new HashMap<>();

	@Default
	private Set<Class<? extends Source>> sourceTypes = new HashSet<>();

	@Default
	private List<Set<String>> preSourceDep = new ArrayList<>();

	/**
	 * Get the connector identity and create it if it is not created yet.
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
