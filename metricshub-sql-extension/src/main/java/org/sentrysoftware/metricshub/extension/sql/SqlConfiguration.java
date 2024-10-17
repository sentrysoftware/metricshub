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

import static com.fasterxml.jackson.annotation.Nulls.SKIP;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sentrysoftware.metricshub.engine.common.exception.InvalidConfigurationException;
import org.sentrysoftware.metricshub.engine.common.helpers.StringHelper;
import org.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import org.sentrysoftware.metricshub.engine.deserialization.TimeDeserializer;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
/**
 * Represents SQL configuration in the MetricsHub extension system.
 */
public class SqlConfiguration implements IConfiguration {

	private static final int DEFAULT_POSTGRESQL_PORT = 5432;
	private static final int DEFAULT_MYSQL_PORT = 3306;
	private static final int DEFAULT_ORACLE_PORT = 1521;
	private static final int DEFAULT_MSSQL_PORT = 1433;
	private static final int DEFAULT_INFORMIX_PORT = 9088;
	private static final int DEFAULT_H2_PORT = 9092;
	private static final int INVALID_PORT = -1;

	private String username;
	private char[] password;
	private char[] url;

	@Default
	@JsonSetter(nulls = SKIP)
	@JsonDeserialize(using = TimeDeserializer.class)
	private final Long timeout = 120L;

	private String type;

	private Integer port;
	private String database;

	private String hostname;

	@Override
	public String toString() {
		return username != null ? "SQL as " + username : "SQL";
	}

	@Override
	public void validateConfiguration(String resourceKey) throws InvalidConfigurationException {
		StringHelper.validateConfigurationAttribute(
			username,
			attr -> attr == null || attr.isBlank(),
			() ->
				String.format(
					"Resource %s - No username configured for %s." +
					" This resource will not be monitored." +
					" Please verify the configured username value.",
					resourceKey,
					"SQL"
				)
		);

		StringHelper.validateConfigurationAttribute(
			timeout,
			attr -> attr == null || attr < 0L,
			() ->
				String.format(
					"Resource %s - Timeout value is invalid for protocol %s." +
					" Timeout value returned: %s. This resource will not be monitored." +
					" Please verify the configured timeout value.",
					resourceKey,
					"SQL",
					timeout
				)
		);

		if (url == null || url.length == 0) {
			StringHelper.validateConfigurationAttribute(
				database,
				attr -> attr == null || attr.isBlank(),
				() ->
					String.format(
						"Resource %s - No database name configured for protocol %s." +
						" Database value returned: %s. This resource will not be monitored." +
						" Please verify the configured database value.",
						resourceKey,
						"SQL",
						database
					)
			);

			StringHelper.validateConfigurationAttribute(
				type,
				attr -> attr == null || attr.isBlank(),
				() ->
					String.format(
						"Resource %s - Invalid Type configured for protocol %s." +
						" Type value returned: %s. This resource will not be monitored.",
						" Please verify the configured type value.",
						resourceKey,
						"SQL",
						type
					)
			);

			if (port == null) {
				port = getDefaultPort(type);
			}

			StringHelper.validateConfigurationAttribute(
				port,
				attr -> attr < 1 || attr > 65535,
				() ->
					String.format(
						"Resource %s - Invalid port configured for protocol %s." +
						" Port value returned: %s. This resource will not be monitored." +
						" Please verify the configured port value.",
						resourceKey,
						"SQL",
						port
					)
			);
			url = generateUrl();
		}

		StringHelper.validateConfigurationAttribute(
			url,
			attr -> attr.length == 0,
			() ->
				String.format(
					"Resource %s - Invalid url configured for protocol %s. " +
					"Url value returned: %s. This resource will not be monitored." +
					" Please verify the configured url value.",
					resourceKey,
					"SQL",
					new String(url)
				)
		);
	}

	@Override
	public IConfiguration copy() {
		return SqlConfiguration
			.builder()
			.database(database)
			.password(password)
			.port(port)
			.timeout(timeout)
			.type(type)
			.username(username)
			.url(url)
			.build();
	}

	/**
	 * Returns the default port based on the database type.
	 *
	 * @param databaseType The type of the database.
	 * @return The default port number for the given database type, or -1 if the type is not recognized.
	 */
	public static Integer getDefaultPort(String databaseType) {
		return switch (databaseType.toLowerCase()) {
			case "postgresql" -> DEFAULT_POSTGRESQL_PORT;
			case "mysql" -> DEFAULT_MYSQL_PORT;
			case "oracle" -> DEFAULT_ORACLE_PORT;
			case "mssql" -> DEFAULT_MSSQL_PORT;
			case "informix" -> DEFAULT_INFORMIX_PORT;
			case "h2" -> DEFAULT_H2_PORT;
			default -> INVALID_PORT;
		};
	}

	/**
	 * Generates a JDBC URL based on the database type, hostname, database name, and port
	 * @return The generated JDBC URL.
	 */
	public char[] generateUrl() {
		return switch (type.toLowerCase()) {
			case "postgresql" -> String.format("jdbc:%s://%s:%d/%s", type, hostname, port, database).toCharArray();
			case "mysql" -> String.format("jdbc:%s://%s:%d/%s", type, hostname, port, database).toCharArray();
			case "sqlserver" -> String
				.format("jdbc:%s://%s:%d;databaseName=%s", type, hostname, port, database)
				.toCharArray();
			default -> new char[0];
		};
	}
}
