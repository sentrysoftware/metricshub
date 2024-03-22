package org.sentrysoftware.metricshub.extension.snmp;

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

import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sentrysoftware.metricshub.engine.common.helpers.LoggingHelper;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.SnmpGetSource;
import org.sentrysoftware.metricshub.engine.strategy.source.SourceTable;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

/**
 * A utility class for processing SNMP Get sources.
 * This class processes SNMP GET sources, fetch data from SNMP devices using
 * the given configuration, and return it as a {@link SourceTable} object.
 */
@Slf4j
@AllArgsConstructor
public class SnmpGetSourceProcessor {

	private SnmpRequestExecutor snmpRequestExecutor;

	/**
	 * Fetches data from an SNMP device based on the provided {@link SnmpGetSource} connector's directive,
	 * parses the data into a structured table format, and returns it.
	 * If any errors occur during the fetch or processing, or if the provided configurations are invalid,
	 * an empty {@link SourceTable} is returned.
	 *
	 * @param snmpGetSource    The {@link SnmpGetSource} defining the SNMP OID.
	 * @param connectorId      The connector identifier used for logging purposes.
	 * @param telemetryManager The telemetry manager providing access to host configuration and SNMP credentials.
	 * @return a {@link SourceTable} containing the fetched SNMP GET data, or an empty table if processing fails.
	 */
	public SourceTable process(
		final SnmpGetSource snmpGetSource,
		final String connectorId,
		final TelemetryManager telemetryManager
	) {
		final String hostname = telemetryManager.getHostConfiguration().getHostname();

		if (snmpGetSource == null) {
			log.error(
				"Hostname {} - SNMP Get Source cannot be null, the SNMP Get operation will return an empty result.",
				hostname
			);
			return SourceTable.empty();
		}

		final SnmpConfiguration snmpConfiguration = (SnmpConfiguration) telemetryManager
			.getHostConfiguration()
			.getConfigurations()
			.get(SnmpConfiguration.class);

		if (snmpConfiguration == null) {
			log.debug(
				"Hostname {} - The SNMP credentials are not configured. Returning an empty table for SNMP Get Source {}.",
				hostname,
				snmpGetSource
			);
			return SourceTable.empty();
		}

		try {
			final String result = snmpRequestExecutor
				.executeSNMPGet(snmpGetSource.getOid(), snmpConfiguration, hostname, true);

			if (result != null) {
				return SourceTable
					.builder()
					.table(
						Stream
							.of(
								Stream.of(result).collect(Collectors.toList()) // NOSONAR
							)
							.collect(Collectors.toList()) // NOSONAR
					)
					.build();
			}
		} catch (Exception e) { // NOSONAR on interruption
			LoggingHelper.logSourceError(
				connectorId,
				snmpGetSource.getKey(),
				String.format("SNMP Get: %s.", snmpGetSource.getOid()),
				hostname,
				e
			);
		}

		return SourceTable.empty();
	}
}
