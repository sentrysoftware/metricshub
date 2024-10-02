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
import lombok.extern.slf4j.Slf4j;
import org.sentrysoftware.metricshub.engine.common.helpers.LoggingHelper;
import org.sentrysoftware.metricshub.engine.common.helpers.TextTableHelper;
import org.sentrysoftware.metricshub.engine.connector.model.common.SqlTable;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.Source;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.SqlSource;
import org.sentrysoftware.metricshub.engine.extension.ISourceComputationExtension;
import org.sentrysoftware.metricshub.engine.strategy.source.SourceTable;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

/**
 * This class implements the {@link ISourceComputationExtension} contract, reports the supported features,
 * processes Sql sources.
 */
@Slf4j
public class SqlExtension implements ISourceComputationExtension {

	@Override
	public SourceTable processSource(Source source, String connectorId, TelemetryManager telemetryManager) {
		final String hostname = telemetryManager.getHostname();

		if (source == null) {
			log.warn("Hostname {} - SQL Source cannot be null, the SQL operation will return an empty result.", hostname);
			return SourceTable.empty();
		}

		if (!(source instanceof SqlSource sqlSource)) {
			log.warn("Hostname {} - SQL Source is invalid, the SQL operation will return an empty result.", hostname);
			return SourceTable.empty();
		}

		final List<SqlTable> sqlTables = sqlSource.getTables();
		if (sqlTables == null) {
			log.debug(
				"Hostname {} - Table list in the SQL Source cannot be null, the SQL operation {} will return an empty result.",
				hostname,
				sqlSource
			);
			return SourceTable.empty();
		}

		final String query = sqlSource.getQuery();
		if (query == null || query.isBlank()) {
			log.debug(
				"Hostname {} - Query in the SQL Source cannot be null, the SQL operation {} will return an empty result.",
				hostname,
				sqlSource
			);
			return SourceTable.empty();
		}

		final List<List<String>> executeSqlQuery = new SqlClientExecutor(telemetryManager, connectorId)
			.executeQuery(sqlTables, query);

		SourceTable sourceTable = new SourceTable();

		if (!executeSqlQuery.isEmpty()) {
			sourceTable.setTable(executeSqlQuery);
		}

		LoggingHelper.debug(() ->
			log.trace("Executed SQL request:{}\n- Result:\n{}\n", query, TextTableHelper.generateTextTable(executeSqlQuery))
		);

		return sourceTable;
	}

	@Override
	public boolean isValidSource(Source source) {
		return source instanceof SqlSource;
	}
}
