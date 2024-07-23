package org.sentrysoftware.metricshub.extension.snmp;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub SNMP Extension Common
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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.Criterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.SnmpGetCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.SnmpGetNextCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.SnmpGetSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.SnmpTableSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.Source;
import org.sentrysoftware.metricshub.engine.extension.IProtocolExtension;
import org.sentrysoftware.metricshub.engine.strategy.detection.CriterionTestResult;
import org.sentrysoftware.metricshub.engine.strategy.source.SourceTable;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import org.sentrysoftware.metricshub.extension.snmp.detection.SnmpGetCriterionProcessor;
import org.sentrysoftware.metricshub.extension.snmp.detection.SnmpGetNextCriterionProcessor;
import org.sentrysoftware.metricshub.extension.snmp.source.SnmpGetSourceProcessor;
import org.sentrysoftware.metricshub.extension.snmp.source.SnmpTableSourceProcessor;

/**
 * An abstract base class for SNMP protocol extensions.
 * Provides common methods and functionality for handling SNMP sources, criteria, and configurations.
 */
public abstract class AbstractSnmpExtension implements IProtocolExtension {

	/**
	 * Returns the SNMP request executor used for executing SNMP requests.
	 * @return The SNMP request executor.
	 */
	protected abstract AbstractSnmpRequestExecutor getRequestExecutor();

	/**
	 * Returns the class type of the SNMP configuration.
	 * @return The SNMP configuration class type.
	 */
	protected abstract Class<? extends ISnmpConfiguration> getConfigurationClass();

	@Override
	public Set<Class<? extends Source>> getSupportedSources() {
		return Set.of(SnmpTableSource.class, SnmpGetSource.class);
	}

	@Override
	public Set<Class<? extends Criterion>> getSupportedCriteria() {
		return Set.of(SnmpGetCriterion.class, SnmpGetNextCriterion.class);
	}

	@Override
	public SourceTable processSource(Source source, String connectorId, TelemetryManager telemetryManager) {
		final Function<TelemetryManager, ISnmpConfiguration> configurationRetriever = manager ->
			(ISnmpConfiguration) manager.getHostConfiguration().getConfigurations().get(getConfigurationClass());

		if (source instanceof SnmpTableSource snmpTableSource) {
			return new SnmpTableSourceProcessor(getRequestExecutor(), configurationRetriever)
				.process(snmpTableSource, connectorId, telemetryManager);
		} else if (source instanceof SnmpGetSource snmpGetSource) {
			return new SnmpGetSourceProcessor(getRequestExecutor(), configurationRetriever)
				.process(snmpGetSource, connectorId, telemetryManager);
		}
		throw new IllegalArgumentException(
			String.format(
				"Hostname %s - Cannot process source %s.",
				telemetryManager.getHostname(),
				source != null ? source.getClass().getSimpleName() : "<null>"
			)
		);
	}

	@Override
	public CriterionTestResult processCriterion(
		Criterion criterion,
		String connectorId,
		TelemetryManager telemetryManager
	) {
		final Function<TelemetryManager, ISnmpConfiguration> configurationRetriever = manager ->
			(ISnmpConfiguration) manager.getHostConfiguration().getConfigurations().get(getConfigurationClass());

		if (criterion instanceof SnmpGetCriterion snmpGetCriterion) {
			return new SnmpGetCriterionProcessor(getRequestExecutor(), configurationRetriever)
				.process(snmpGetCriterion, connectorId, telemetryManager);
		} else if (criterion instanceof SnmpGetNextCriterion snmpGetNextCriterion) {
			return new SnmpGetNextCriterionProcessor(getRequestExecutor(), configurationRetriever)
				.process(snmpGetNextCriterion, connectorId, telemetryManager);
		}
		throw new IllegalArgumentException(
			String.format(
				"Hostname %s - Cannot process criterion %s.",
				telemetryManager.getHostname(),
				criterion != null ? criterion.getClass().getSimpleName() : "<null>"
			)
		);
	}

	@Override
	public Map<Class<? extends IConfiguration>, Set<Class<? extends Source>>> getConfigurationToSourceMapping() {
		return Map.of(getConfigurationClass(), Set.of(SnmpTableSource.class, SnmpGetSource.class));
	}

	@Override
	public boolean isSupportedConfigurationType(String configurationType) {
		return getIdentifier().equalsIgnoreCase(configurationType);
	}

	/**
	 * Creates and configures a new instance of the Jackson ObjectMapper for handling YAML data.
	 *
	 * @return A configured ObjectMapper instance.
	 */
	public static JsonMapper newObjectMapper() {
		return JsonMapper
			.builder(new YAMLFactory())
			.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
			.enable(SerializationFeature.INDENT_OUTPUT)
			.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false)
			.build();
	}
}
