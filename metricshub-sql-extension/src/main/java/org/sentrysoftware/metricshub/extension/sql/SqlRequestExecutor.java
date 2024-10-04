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
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.sentrysoftware.metricshub.engine.common.exception.ClientException;
import org.sentrysoftware.metricshub.extension.jdbc.JdbcClient;
import org.sentrysoftware.metricshub.extension.jdbc.SqlResult;

/**
 * Provides functionality to execute SQL queries via JDBC.
 */
@Slf4j
public class SqlRequestExecutor {

	/**
	 * Execute an SQL query using the provided configuration and return the result.
	 *
	 * @param hostname        The hostname of the database server.
	 * @param sqlConfig       SQL configuration including URL, username, password, and timeout.
	 * @param sqlQuery        The SQL query to execute.
	 * @param showWarnings    Whether to show SQL warnings.
	 * @return A {@link List} of {@link List} of {@link String}s representing the result table.
	 * @throws ClientException when anything goes wrong (details in cause)
	 */
	@WithSpan("SQL")
	public List<List<String>> executeSql(
		@SpanAttribute("host.hostname") final String hostname,
		@SpanAttribute("sql.config") @NonNull final SqlConfiguration sqlConfig,
		@SpanAttribute("sql.query") @NonNull final String sqlQuery,
		@SpanAttribute("sql.showWarnings") final boolean showWarnings
	) throws ClientException {
		try {
			final String url = Optional.ofNullable(sqlConfig.getUrl()).map(String::valueOf).orElse(null);

			// Log the details of the SQL request including the hostname
			log.trace(
				"Executing SQL query on database {} hosted on {}\n- " +
				"URL: {}\n- Username: {}\n- Query: {}\n- Timeout: {} s\n",
				sqlConfig.getDatabase(),
				hostname,
				url,
				sqlConfig.getUsername(),
				sqlQuery,
				sqlConfig.getTimeout()
			);

			// Execute the SQL query
			SqlResult sqlResult = JdbcClient.execute(
				url,
				sqlConfig.getUsername(),
				sqlConfig.getPassword(),
				sqlQuery,
				showWarnings,
				sqlConfig.getTimeout().intValue()
			);

			log.debug("Executed SQL query on hostname {}. Result: {}", hostname, sqlResult.getResults());
			return sqlResult.getResults();
		} catch (SQLException e) {
			log.error("SQL query failed on hostname {}: {}", hostname, e.getMessage(), e);

			throw new ClientException("SQL query failed on hostname " + hostname, e);
		}
	}
}
