package org.sentrysoftware.metricshub.extension.sql;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub SQL Extension
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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import lombok.extern.slf4j.Slf4j;
import org.sentrysoftware.metricshub.engine.common.exception.InvalidConfigurationException;
import org.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.Criterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.SqlCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.Source;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.SqlSource;
import org.sentrysoftware.metricshub.engine.extension.IProtocolExtension;
import org.sentrysoftware.metricshub.engine.strategy.detection.CriterionTestResult;
import org.sentrysoftware.metricshub.engine.strategy.source.SourceTable;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

/**
 * This class implements the {@link IProtocolExtension} contract, reports the supported features,
 * processes SQL sources and criteria.
 */
@Slf4j
public class SqlExtension implements IProtocolExtension {

	/**
	 * The identifier for sql.
	 */
	private static final String IDENTIFIER = "sql";

	private SqlRequestExecutor sqlRequestExecutor;

	/**
	 * Creates a new instance of the {@link SqlExtension} implementation.
	 */
	public SqlExtension() {
		sqlRequestExecutor = new SqlRequestExecutor();
	}

	@Override
	public boolean isValidConfiguration(IConfiguration configuration) {
		return configuration instanceof SqlConfiguration;
	}

	@Override
	public Set<Class<? extends Source>> getSupportedSources() {
		return Set.of(SqlSource.class);
	}

	@Override
	public Map<Class<? extends IConfiguration>, Set<Class<? extends Source>>> getConfigurationToSourceMapping() {
		return Map.of(SqlConfiguration.class, Set.of(SqlSource.class));
	}

	@Override
	public Set<Class<? extends Criterion>> getSupportedCriteria() {
		return Set.of(SqlCriterion.class);
	}

	@Override
	public Optional<Boolean> checkProtocol(TelemetryManager telemetryManager) {
		// Create and set the SQL result to null
		List<List<String>> sqlResult = null;

		// Retrieve SQL Configuration from the telemetry manager host configuration
		final SqlConfiguration sqlConfiguration = (SqlConfiguration) telemetryManager
			.getHostConfiguration()
			.getConfigurations()
			.get(SqlConfiguration.class);

		// Stop the health check if there is not an SQL configuration
		if (sqlConfiguration == null) {
			return Optional.empty();
		}

		// Retrieve the hostname from the SqlConfiguration
		final String hostname = sqlConfiguration.getHostname();
		log.info("Hostname {} - Performing {} protocol health check.", hostname, getIdentifier());

		try {
			sqlResult = sqlRequestExecutor.executeSql(hostname, sqlConfiguration, "SELECT 1", false);
		} catch (Exception e) {
			log.error("Hostname {} - SQL protocol health check failed: {}", hostname, e.getMessage(), e);
		}
		return Optional.of(sqlResult != null);
	}

	@Override
	public CriterionTestResult processCriterion(
		Criterion criterion,
		String connectorId,
		TelemetryManager telemetryManager
	) {
		if (criterion instanceof SqlCriterion sqlCriterion) {
			return new SqlCriterionProcessor(sqlRequestExecutor).process(sqlCriterion, telemetryManager);
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
	public SourceTable processSource(Source source, String connectorId, TelemetryManager telemetryManager) {
		if (source instanceof SqlSource sqlSource) {
			return new SqlSourceProcessor(sqlRequestExecutor, connectorId).process(sqlSource, telemetryManager);
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
	public boolean isSupportedConfigurationType(String configurationType) {
		return IDENTIFIER.equalsIgnoreCase(configurationType);
	}

	@Override
	public IConfiguration buildConfiguration(String configurationType, JsonNode jsonNode, UnaryOperator<char[]> decrypt)
		throws InvalidConfigurationException {
		try {
			final SqlConfiguration sqlConfiguration = newObjectMapper().treeToValue(jsonNode, SqlConfiguration.class);

			if (decrypt != null) {
				char[] password = sqlConfiguration.getPassword();
				char[] url = sqlConfiguration.getUrl();
				if (password != null) {
					sqlConfiguration.setPassword(decrypt.apply(password));
				}
				if (url != null) {
					sqlConfiguration.setUrl(decrypt.apply(url));
				}
			}
			return sqlConfiguration;
		} catch (Exception e) {
			final String errorMessage = String.format(
				"Error while reading SQL Configuration: %s. Error: %s",
				jsonNode,
				e.getMessage()
			);
			log.error(errorMessage);
			log.debug("Error while reading SQL Configuration: {}. Stack trace:", jsonNode, e);
			throw new InvalidConfigurationException(errorMessage, e);
		}
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

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}
}
