package org.sentrysoftware.metricshub.agent.extension;

import com.fasterxml.jackson.databind.DeserializationFeature;
/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub OsCommand Extension
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;
import org.sentrysoftware.metricshub.engine.common.exception.InvalidConfigurationException;
import org.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.CommandLineCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.Criterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.IpmiCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.CommandLineSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.IpmiSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.Source;
import org.sentrysoftware.metricshub.engine.extension.IProtocolExtension;
import org.sentrysoftware.metricshub.engine.strategy.detection.CriterionTestResult;
import org.sentrysoftware.metricshub.engine.strategy.source.SourceTable;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

public class OsCommandTestExtension implements IProtocolExtension {

	/**
	 * Protocol up status value '1.0'
	 */
	public static final Double UP = 1.0;

	/**
	 * Protocol down status value '0.0'
	 */
	public static final Double DOWN = 0.0;

	/**
	 * Up metric name format that will be saved by the metric factory
	 */
	public static final String SSH_UP_METRIC = "metricshub.host.up{protocol=\"ssh\"}";

	/**
	 * SSH test command to execute
	 */
	public static final String SSH_TEST_COMMAND = "echo test";

	@Override
	public boolean isValidConfiguration(IConfiguration configuration) {
		return true;
	}

	@Override
	public Set<Class<? extends Source>> getSupportedSources() {
		return Set.of(CommandLineSource.class, IpmiSource.class);
	}

	@Override
	public Map<Class<? extends IConfiguration>, Set<Class<? extends Source>>> getConfigurationToSourceMapping() {
		return Map.ofEntries();
	}

	@Override
	public Set<Class<? extends Criterion>> getSupportedCriteria() {
		return Set.of(CommandLineCriterion.class, IpmiCriterion.class);
	}

	@Override
	public void checkProtocol(TelemetryManager telemetryManager) {}

	@Override
	public SourceTable processSource(Source source, String connectorId, TelemetryManager telemetryManager) {
		return SourceTable.empty();
	}

	@Override
	public CriterionTestResult processCriterion(
		Criterion criterion,
		String connectorId,
		TelemetryManager telemetryManager
	) {
		return CriterionTestResult.empty();
	}

	@Override
	public boolean isSupportedConfigurationType(String configurationType) {
		Set<String> supportedConfigurations = Set.of("ssh", "oscommand");
		return supportedConfigurations.contains(configurationType.toLowerCase());
	}

	@Override
	public IConfiguration buildConfiguration(String configurationType, JsonNode jsonNode, UnaryOperator<char[]> decrypt)
		throws InvalidConfigurationException {
		if (configurationType.equalsIgnoreCase("ssh")) {
			try {
				final SshTestConfiguration sshConfiguration = newObjectMapper()
					.treeToValue(jsonNode, SshTestConfiguration.class);

				if (decrypt != null) {
					// Decrypt the password
					final char[] passwordDecypted = decrypt.apply(sshConfiguration.getPassword());
					sshConfiguration.setPassword(passwordDecypted);
				}

				return sshConfiguration;
			} catch (Exception e) {
				final String errorMessage = String.format(
					"Error while reading SSH Configuration: %s. Error: %s",
					jsonNode,
					e.getMessage()
				);
				throw new InvalidConfigurationException(errorMessage, e);
			}
		} else if (configurationType.equalsIgnoreCase("oscommand")) {
			try {
				return newObjectMapper().treeToValue(jsonNode, OsCommandTestConfiguration.class);
			} catch (Exception e) {
				final String errorMessage = String.format(
					"Error while reading OsCommand Configuration: %s. Error: %s",
					jsonNode,
					e.getMessage()
				);
				throw new InvalidConfigurationException(errorMessage, e);
			}
		}
		final String errorMessage = String.format(
			"Unhandled %s configuration in the OsCommandExtension.",
			configurationType
		);
		throw new InvalidConfigurationException(errorMessage);
	}

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
