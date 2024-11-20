package org.sentrysoftware.metricshub.extension.internal.db;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub Internal DB Extension
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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sentrysoftware.metricshub.engine.connector.model.common.SqlColumn;
import org.sentrysoftware.metricshub.engine.connector.model.common.SqlTable;
import org.sentrysoftware.metricshub.engine.strategy.source.SourceTable;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

/**
 * The SqlClientExecutor class provides a method for executing
 * SQL queries on a list of tables.
 */
@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SqlClientExecutor {

	private TelemetryManager telemetryManager;
	private String connectorId;

	/**
	 * Creates a new H2 in-memory database and create a connection to it.
	 * Creates and fill the SQL tables corresponding to the {@link SqlTable} in the sqlTables {@link List}.
	 * Execute the query and return the result in a form of a list of lists of strings.
	 * @param sqlTables The tables to execute the query on.
	 * @param query     The query to execute.
	 * @return          The result of the query.
	 */
	public List<List<String>> executeQuery(final List<SqlTable> sqlTables, final String query) {
		if (sqlTables == null) {
			log.error("Malformed Local SQL Source, no SQL Table is provided.");
			return new ArrayList<>();
		}

		if (query == null) {
			log.error("Malformed Local SQL Source, no SQL Query is provided.");
			return new ArrayList<>();
		}

		final String hostId = telemetryManager.getHostConfiguration().getHostId();
		final String connectionName = "jdbc:h2:mem:" + hostId + UUID.randomUUID().toString();

		// Creation of the connection to the H2 database in memory
		try (Connection connection = DriverManager.getConnection(connectionName)) {
			// Prepare the SQL tables
			for (SqlTable sqlTable : sqlTables) {
				createAndInsert(sqlTable, connection);
			}

			return executeQuery(query, connection);
		} catch (SQLException exception) {
			log.error("Error when creating the Local SQL database: {}", exception.getMessage());
			log.debug("SQL Exception: ", exception);
			return new ArrayList<>();
		}
	}

	/**
	 * Execute a SQL query on the connection object and return the result in a form
	 * of a list of lists of strings.
	 *
	 * @param query      The query to execute.
	 * @param connection The connection to the database.
	 * @return The result of the query.
	 */
	private List<List<String>> executeQuery(final String query, Connection connection) {
		final List<List<String>> result = new ArrayList<>();

		try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(query)) {
			if (resultSet == null) {
				log.error("The Local SQL query {} returned a null result.", query);
				return result;
			}

			final ResultSetMetaData rsmd = resultSet.getMetaData();

			final int columnsNumber = rsmd.getColumnCount();

			while (resultSet.next()) {
				final List<String> row = new ArrayList<>();
				for (int i = 1; i <= columnsNumber; i++) {
					final String resultValue = resultSet.getString(i);
					row.add(resultValue != null ? resultValue : "");
				}
				result.add(row);
			}
		} catch (SQLException exception) {
			log.error("Error when executing Local SQL query {}: {}", query, exception.getMessage());
			log.debug("SQL Exception: ", exception);
		}

		return result;
	}

	/**
	 * Create the SQL tables corresponding to the sqlTables in the database linked to the connection object.
	 * @param sqlTable   The tables to use to create SQL tables.
	 * @param connection The connection to the database.
	 */
	private void createAndInsert(final SqlTable sqlTable, final Connection connection) {
		final String createTableQuery = createTableQuery(sqlTable);
		if (createTableQuery == null) {
			log.debug("Error when creating Local SQL CREATE TABLE query for source {}", sqlTable.getSource());
			return;
		}

		try {
			final Statement statement = connection.createStatement();

			statement.execute(createTableQuery);
			log.debug("Executing CREATE TABLE query: {}", createTableQuery);
		} catch (SQLException exception) {
			log.error("Error when executing CREATE TABLE query {}: {}", createTableQuery, exception.getMessage());
			log.debug("CREATE TABLE SQL Exception: ", exception);
			return;
		}

		final String insertTableQuery = insertTableQuery(sqlTable);
		if (insertTableQuery == null) {
			return;
		}

		try {
			final Statement statement = connection.createStatement();

			statement.execute(insertTableQuery);
			log.debug("Executing INSERT TABLE query: {}", insertTableQuery);
		} catch (SQLException exception) {
			log.error("Error when executing INSERT TABLE query {}: {}", insertTableQuery, exception.getMessage());
			log.debug("INSERT TABLE SQL Exception: ", exception);
		}
	}

	/**
	 * Create the SQL 'CREATE TABLE' query to create the SQL table corresponding to the {@link SqlTable} object.
	 * @param sqlTable the {@link SqlTable} to use to create the SQL 'CREATE TABLE' query.
	 * @return The SQL 'CREATE TABLE' query to create the table.
	 */
	private String createTableQuery(final SqlTable sqlTable) {
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("CREATE TABLE ");

		final String alias = sqlTable.getAlias().strip();

		if (alias.isBlank() || alias.contains(";") || alias.contains(" ") || alias.contains("\"")) {
			log.error("Invalid alias {} for the source {}", sqlTable.getAlias(), sqlTable.getSource());
			return null;
		}

		queryBuilder.append(alias);
		queryBuilder.append(" (");

		final List<String> columnNameType = new ArrayList<>();

		for (final SqlColumn sqlColumn : sqlTable.getColumns()) {
			final String name = sqlColumn.getName();
			if (name == null || name.isBlank()) {
				log.error("Invalid name in lookup source table {}", sqlTable.getSource());
				return null;
			}

			final String type = sqlColumn.getType();
			if (type == null || type.isBlank()) {
				log.error("Invalid type for column {} in lookup source table {}", name, sqlTable.getSource());
				return null;
			}

			final int number = sqlColumn.getNumber();

			// Check if number is valid
			if (number < 1) {
				log.error("Wrong number {} for column {} in lookup source table {}", number, name, sqlTable.getSource());
				return null;
			}
			columnNameType.add(String.join(" ", name, type));
		}

		queryBuilder.append(String.join(",", columnNameType));
		queryBuilder.append(");");

		return queryBuilder.toString();
	}

	/**
	 * Create the INSERT SQL query needed to insert the values in the {@link SqlTable} into a SQL Database.
	 * @param sqlTable The table to insert the data from.
	 * @return The resulting SQL query.
	 */
	private String insertTableQuery(final SqlTable sqlTable) {
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("INSERT INTO ");

		final String alias = sqlTable.getAlias().strip();

		queryBuilder.append(alias);
		queryBuilder.append(" (");

		final List<String> columnNames = new ArrayList<>();

		sqlTable.getColumns().stream().forEach(sqlColumn -> columnNames.add(sqlColumn.getName()));

		queryBuilder.append(String.join(",", columnNames));
		queryBuilder.append(") VALUES ");

		final String valuesToInsert = formatInsertValues(sqlTable);

		// If there is no value to insert, we stop the job completely
		if (valuesToInsert == null) {
			return null;
		}

		queryBuilder.append(valuesToInsert);
		queryBuilder.append(";");

		return queryBuilder.toString();
	}

	/**
	 * Extract the {@link SourceTable} from a {@link SqlTable}, extract its data and format them for a INSERT SQL query.
	 * @param sqlTable The table to extract the {@link SourceTable} and the {@link SqlColumn} from.
	 * @return The formated data.
	 */
	private String formatInsertValues(final SqlTable sqlTable) {
		SourceTable sourceTable = SourceTable.lookupSourceTable(sqlTable.getSource(), connectorId, telemetryManager).get();

		if (sourceTable == null) {
			log.error(
				"The source table {} is not found during the Local SQL Query job. Skip processing.",
				sqlTable.getSource()
			);
			return null;
		}

		List<List<String>> table = sourceTable.getTable();

		if (table == null || table.isEmpty()) {
			log.error("The source table {} is empty. Skip Local SQL job processing.", sqlTable.getSource());
			return null;
		}

		final List<SqlColumn> sqlColumns = sqlTable.getColumns();

		final List<String> columnValues = new ArrayList<>();

		for (final List<String> row : table) {
			final List<String> rowValues = new ArrayList<>();
			for (final SqlColumn sqlColumn : sqlColumns) {
				final String separator = sqlColumn.getType().contains("CHAR") ? "'" : "";
				final String value = row.get(sqlColumn.getNumber() - 1);
				rowValues.add(value != null && !value.isEmpty() ? (separator + value + separator) : "NULL");
			}
			columnValues.add("(" + String.join(",", rowValues) + ")");
		}

		return String.join(",", columnValues);
	}
}
