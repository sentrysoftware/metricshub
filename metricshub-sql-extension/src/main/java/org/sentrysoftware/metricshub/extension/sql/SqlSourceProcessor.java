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

import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sentrysoftware.metricshub.engine.common.helpers.LoggingHelper;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.SqlSource;
import org.sentrysoftware.metricshub.engine.strategy.source.SourceTable;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

/**
 * A class responsible for processing SQL sources and returning the result as a {@link SourceTable}.
 * It provides methods to run SQL queries via JDBC and generate a table with the results.
 */
@RequiredArgsConstructor
@Slf4j
public class SqlSourceProcessor {

	@NonNull
	private SqlRequestExecutor sqlRequestExecutor;

	@NonNull
	private String connectorId;

	/**
	 * Processes a SQL source by executing its associated SQL query using the configuration and hostname retrieved
	 * from a given {@link TelemetryManager}. Handles errors, logs warnings or errors, and returns an empty table
	 * in case of failure.
	 *
	 * @param sqlSource        The SQL source containing the query to be executed.
	 * @param telemetryManager The telemetry manager providing host configuration and credentials for the SQL query.
	 * @return A {@link SourceTable} containing the results of the executed query, or an empty table if an error occurs.
	 */
	public SourceTable process(final SqlSource sqlSource, final TelemetryManager telemetryManager) {
		if (sqlSource == null) {
			log.error("SQL source is null. Returning an empty table.");
			return SourceTable.empty();
		}

		final String hostname = telemetryManager.getHostname();
		final SqlConfiguration sqlConfiguration = (SqlConfiguration) telemetryManager
			.getHostConfiguration()
			.getConfigurations()
			.get(SqlConfiguration.class);

		if (sqlConfiguration == null) {
			log.debug(
				"Hostname {} - The SQL database credentials are not configured. " +
				"Returning an empty table for SQL source {}. ",
				hostname,
				sqlSource.getKey()
			);

			return SourceTable.empty();
		}

		try {
			final List<List<String>> results = sqlRequestExecutor.executeSql(
				hostname,
				sqlConfiguration,
				sqlSource.getQuery(),
				false
			);

			return SourceTable.builder().table(results).build();
		} catch (Exception e) {
			LoggingHelper.logSourceError(
				connectorId,
				sqlSource.getKey(),
				String.format(
					"SQL query=%s, Username=%s, Timeout=%d",
					sqlSource.getQuery(),
					sqlConfiguration.getUsername(),
					sqlConfiguration.getTimeout()
				),
				hostname,
				e
			);
			return SourceTable.empty();
		}
	}
}
