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

import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.TABLE_SEP;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.SqlCriterion;
import org.sentrysoftware.metricshub.engine.strategy.detection.CriterionTestResult;
import org.sentrysoftware.metricshub.engine.strategy.source.SourceTable;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

/**
 * A class responsible for processing SQL criteria to evaluate SQL queries.
 * and generates criterion test results accordingly.
 */
@Slf4j
@AllArgsConstructor
public class SqlCriterionProcessor {

	@NonNull
	private SqlRequestExecutor sqlRequestExecutor;

	/**
	 * Processes a SQL criterion by executing an SQL query.
	 *
	 * @param sqlCriterion     The criterion including the SQL query.
	 * @param telemetryManager The telemetry manager providing access to host configuration
	 * @return {@link CriterionTestResult} instance.
	 */
	public CriterionTestResult process(SqlCriterion sqlCriterion, TelemetryManager telemetryManager) {
		if (sqlCriterion == null) {
			return CriterionTestResult.error(sqlCriterion, "Malformed criterion. Cannot perform detection.");
		}

		final SqlConfiguration sqlConfiguration = (SqlConfiguration) telemetryManager
			.getHostConfiguration()
			.getConfigurations()
			.get(SqlConfiguration.class);

		if (sqlConfiguration == null) {
			return CriterionTestResult.error(sqlCriterion, "The SQL database credentials are not configured for this host.");
		}

		final String hostname = telemetryManager.getHostConfiguration().getHostname();
		final List<List<String>> queryResult;
		try {
			queryResult = sqlRequestExecutor.executeSql(hostname, sqlConfiguration, sqlCriterion.getQuery(), false);
		} catch (Exception e) {
			log.error("Error executing SQL criterion on hostname {}: {}", hostname, e.getMessage(), e);
			return CriterionTestResult.error(sqlCriterion, e.getMessage());
		}

		// Serialize the result as a CSV
		String result = SourceTable.tableToCsv(queryResult, TABLE_SEP, true);

		if (result == null || result.isEmpty()) {
			return CriterionTestResult.failure(sqlCriterion, "No results returned by the query.");
		}

		return CriterionTestResult.success(sqlCriterion, result);
	}
}
