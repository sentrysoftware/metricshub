package org.sentrysoftware.metricshub.engine.strategy.source;

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

import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.AUTOMATIC_NAMESPACE;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.NEW_LINE;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.SEMICOLON;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.TABLE_SEP;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.WMI_DEFAULT_NAMESPACE;

import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sentrysoftware.metricshub.engine.client.ClientsExecutor;
import org.sentrysoftware.metricshub.engine.client.http.HttpRequest;
import org.sentrysoftware.metricshub.engine.common.helpers.FilterResultHelper;
import org.sentrysoftware.metricshub.engine.common.helpers.StringHelper;
import org.sentrysoftware.metricshub.engine.common.helpers.TextTableHelper;
import org.sentrysoftware.metricshub.engine.configuration.HttpConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.IWinConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.IpmiConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.OsCommandConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.SnmpConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.SshConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.WbemConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.CopySource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.HttpSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.IpmiSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.OsCommandSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.SnmpGetSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.SnmpTableSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.StaticSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.TableJoinSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.TableUnionSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.WbemSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.WmiSource;
import org.sentrysoftware.metricshub.engine.strategy.utils.IpmiHelper;
import org.sentrysoftware.metricshub.engine.strategy.utils.OsCommandHelper;
import org.sentrysoftware.metricshub.engine.strategy.utils.OsCommandResult;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
public class SourceProcessor implements ISourceProcessor {

	private TelemetryManager telemetryManager;
	private String connectorId;
	private ClientsExecutor clientsExecutor;

	@WithSpan("Source Copy Exec")
	@Override
	public SourceTable process(@SpanAttribute("source.definition") final CopySource copySource) {
		final String hostname = telemetryManager.getHostConfiguration().getHostname();

		if (copySource == null) {
			log.error(
				"Hostname {} - CopySource cannot be null, the CopySource operation will return an empty result.",
				hostname
			);
			return SourceTable.empty();
		}

		final String copyFrom = copySource.getFrom();

		if (copyFrom == null || copyFrom.isEmpty()) {
			log.error(
				"Hostname {} - CopySource reference cannot be null. Returning an empty table for source {}.",
				hostname,
				copySource
			);
			return SourceTable.empty();
		}

		final SourceTable sourceTable = new SourceTable();

		final Optional<SourceTable> maybeOrigin = SourceTable.lookupSourceTable(copyFrom, connectorId, telemetryManager);

		if (maybeOrigin.isEmpty()) {
			return SourceTable.empty();
		}

		final SourceTable origin = maybeOrigin.get();

		final List<List<String>> table = origin
			.getTable()
			.stream()
			// Map each row in the table to a new ArrayList, effectively performing a deep copy of each row.
			.map(ArrayList::new)
			.filter(row -> !row.isEmpty())
			.collect(Collectors.toList()); // NOSONAR

		sourceTable.setTable(table);

		if (origin.getRawData() != null) {
			sourceTable.setRawData(origin.getRawData());
		}

		logSourceCopy(connectorId, copyFrom, copySource.getKey(), sourceTable, hostname);

		return sourceTable;
	}

	@WithSpan("Source HTTP Exec")
	@Override
	public SourceTable process(@SpanAttribute("source.definition") final HttpSource httpSource) {
		final String hostname = telemetryManager.getHostConfiguration().getHostname();
		if (httpSource == null) {
			log.error(
				"Hostname {} - HttpSource cannot be null, the HttpSource operation will return an empty result.",
				hostname
			);
			return SourceTable.empty();
		}

		final HttpConfiguration httpConfiguration = (HttpConfiguration) telemetryManager
			.getHostConfiguration()
			.getConfigurations()
			.get(HttpConfiguration.class);

		if (httpConfiguration == null) {
			log.debug(
				"Hostname {} - The HTTP credentials are not configured. Returning an empty table for HttpSource {}.",
				hostname,
				httpSource
			);

			return SourceTable.empty();
		}

		try {
			final String result = clientsExecutor.executeHttp(
				HttpRequest
					.builder()
					.hostname(hostname)
					.method(httpSource.getMethod().toString())
					.url(httpSource.getUrl())
					.path(httpSource.getPath())
					.header(httpSource.getHeader(), connectorId, hostname)
					.body(httpSource.getBody(), connectorId, hostname)
					.resultContent(httpSource.getResultContent())
					.authenticationToken(httpSource.getAuthenticationToken())
					.httpConfiguration(httpConfiguration)
					.build(),
				true
			);

			if (result != null && !result.isEmpty()) {
				return SourceTable.builder().rawData(result).build();
			}
		} catch (Exception e) {
			logSourceError(
				connectorId,
				httpSource.getKey(),
				String.format("HTTP %s %s", httpSource.getMethod(), httpSource.getUrl()),
				hostname,
				e
			);
		}

		return SourceTable.empty();
	}

	@WithSpan("Source IPMI Exec")
	@Override
	public SourceTable process(@SpanAttribute("source.definition") final IpmiSource ipmiSource) {
		final String hostname = telemetryManager.getHostConfiguration().getHostname();

		if (ipmiSource == null) {
			log.error("Hostname {} - IPMI Source cannot be null, the IPMI operation will return an empty result.", hostname);
			return SourceTable.empty();
		}

		String sourceKey = ipmiSource.getKey();
		final DeviceKind hostType = telemetryManager.getHostConfiguration().getHostType();

		if (DeviceKind.WINDOWS.equals(hostType)) {
			return processWindowsIpmiSource(sourceKey);
		} else if (DeviceKind.LINUX.equals(hostType) || DeviceKind.SOLARIS.equals(hostType)) {
			return processUnixIpmiSource(sourceKey);
		} else if (DeviceKind.OOB.equals(hostType)) {
			return processOutOfBandIpmiSource(sourceKey);
		}

		log.info(
			"Hostname {} - Failed to process IPMI source. {} is an unsupported OS for IPMI. Returning an empty table.",
			hostname,
			hostType.name()
		);

		return SourceTable.empty();
	}

	/**
	 * Process IPMI source via IPMI Over-LAN
	 *
	 * @param sourceKey The key of the source
	 *
	 * @return {@link SourceTable} containing the IPMI result expected by the IPMI connector embedded AWK script
	 */
	SourceTable processOutOfBandIpmiSource(final String sourceKey) {
		final IpmiConfiguration ipmiConfiguration = (IpmiConfiguration) telemetryManager
			.getHostConfiguration()
			.getConfigurations()
			.get(IpmiConfiguration.class);

		final String hostname = telemetryManager.getHostConfiguration().getHostname();

		if (ipmiConfiguration == null) {
			log.warn("Hostname {} - The IPMI credentials are not configured. Cannot process IPMI-over-LAN source.", hostname);
			return SourceTable.empty();
		}

		try {
			final String result = clientsExecutor.executeIpmiGetSensors(hostname, ipmiConfiguration);

			if (result != null) {
				return SourceTable.builder().rawData(result).build();
			} else {
				log.error("Hostname {} - IPMI-over-LAN request returned <null> result. Returning an empty table.", hostname);
			}
		} catch (Exception e) {
			logSourceError(connectorId, sourceKey, "IPMI-over-LAN", hostname, e);
		}

		return SourceTable.empty();
	}

	/**
	 * Process IPMI Source for the Unix system
	 *
	 * @param sourceKey The key of the source
	 *
	 * @return {@link SourceTable} containing the IPMI result expected by the IPMI connector embedded AWK script
	 */
	SourceTable processUnixIpmiSource(final String sourceKey) {
		final String hostname = telemetryManager.getHostConfiguration().getHostname();

		// get the ipmiTool command to execute
		String ipmitoolCommand = telemetryManager.getHostProperties().getIpmitoolCommand();
		if (ipmitoolCommand == null || ipmitoolCommand.isEmpty()) {
			final String message = String.format(
				"Hostname %s - IPMI tool command cannot be found. Returning an empty result.",
				hostname
			);
			log.error(message);
			return SourceTable.empty();
		}

		final boolean isLocalHost = telemetryManager.getHostProperties().isLocalhost();

		final SshConfiguration sshConfiguration = (SshConfiguration) telemetryManager
			.getHostConfiguration()
			.getConfigurations()
			.get(SshConfiguration.class);

		final OsCommandConfiguration osCommandConfiguration = (OsCommandConfiguration) telemetryManager
			.getHostConfiguration()
			.getConfigurations()
			.get(OsCommandConfiguration.class);

		final int defaultTimeout = osCommandConfiguration != null
			? osCommandConfiguration.getTimeout().intValue()
			: OsCommandConfiguration.DEFAULT_TIMEOUT.intValue();

		// fru command
		String fruCommand = ipmitoolCommand + "fru";
		String fruResult;
		try {
			if (isLocalHost) {
				fruResult = OsCommandHelper.runLocalCommand(fruCommand, defaultTimeout, null);
			} else if (sshConfiguration != null) {
				fruResult = OsCommandHelper.runSshCommand(fruCommand, hostname, sshConfiguration, defaultTimeout, null, null);
			} else {
				log.warn("Hostname {} - Could not process UNIX IPMI Source. SSH protocol credentials are missing.", hostname);
				return SourceTable.empty();
			}

			log.debug("Hostname {} - IPMI OS command: {}:\n{}", hostname, fruCommand, fruResult);
		} catch (Exception e) {
			logSourceError(connectorId, sourceKey, String.format("IPMI OS command: %s.", fruCommand), hostname, e);

			Thread.currentThread().interrupt();

			return SourceTable.empty();
		}

		// "-v sdr elist all"
		String sdrCommand = ipmitoolCommand + "-v sdr elist all";
		String sensorResult;
		try {
			if (isLocalHost) {
				sensorResult = OsCommandHelper.runLocalCommand(sdrCommand, defaultTimeout, null);
			} else {
				sensorResult =
					OsCommandHelper.runSshCommand(sdrCommand, hostname, sshConfiguration, defaultTimeout, null, null);
			}
			log.debug("Hostname {} - IPMI OS command: {}:\n{}", hostname, sdrCommand, sensorResult);
		} catch (Exception e) {
			logSourceError(connectorId, sourceKey, String.format("IPMI OS command: %s.", sdrCommand), hostname, e);

			Thread.currentThread().interrupt();

			return SourceTable.empty();
		}

		return SourceTable.builder().table(IpmiHelper.ipmiTranslateFromIpmitool(fruResult, sensorResult)).build();
	}

	/**
	 * Process IPMI source for the Windows (NT) system
	 *
	 * @param sourceKey The key of the source
	 *
	 * @return {@link SourceTable} containing the IPMI result expected by the IPMI connector embedded AWK script
	 */
	SourceTable processWindowsIpmiSource(final String sourceKey) {
		// Find the configured protocol (WinRM or WMI)

		final IWinConfiguration winConfiguration = (IWinConfiguration) telemetryManager
			.getHostConfiguration()
			.getWinConfiguration();

		final String hostname = telemetryManager.getHostConfiguration().getHostname();

		if (winConfiguration == null) {
			log.warn(
				"Hostname {} - The Windows protocols credentials are not configured. Cannot process Windows IPMI source.",
				hostname
			);
			return SourceTable.empty();
		}

		final String nameSpaceRootCimv2 = "root/cimv2";
		final String nameSpaceRootHardware = "root/hardware";

		String wmiQuery = "SELECT IdentifyingNumber,Name,Vendor FROM Win32_ComputerSystemProduct";
		List<List<String>> wmiCollection1 = executeIpmiWmiRequest(
			hostname,
			winConfiguration,
			wmiQuery,
			nameSpaceRootCimv2,
			sourceKey
		);

		wmiQuery =
			"SELECT BaseUnits,CurrentReading,Description,LowerThresholdCritical,LowerThresholdNonCritical,SensorType,UnitModifier,UpperThresholdCritical,UpperThresholdNonCritical" +
			" FROM NumericSensor";
		List<List<String>> wmiCollection2 = executeIpmiWmiRequest(
			hostname,
			winConfiguration,
			wmiQuery,
			nameSpaceRootHardware,
			sourceKey
		);

		wmiQuery = "SELECT CurrentState,Description FROM Sensor";
		List<List<String>> wmiCollection3 = executeIpmiWmiRequest(
			hostname,
			winConfiguration,
			wmiQuery,
			nameSpaceRootHardware,
			sourceKey
		);

		return SourceTable
			.builder()
			.table(IpmiHelper.ipmiTranslateFromWmi(wmiCollection1, wmiCollection2, wmiCollection3))
			.build();
	}

	/**
	 * Call the client executor to execute a WMI request.
	 *
	 * @param hostname		The host against the query will be run.
	 * @param winConfiguration	The information used to connect to the host and perform the query.
	 * @param wmiQuery		The query that will be executed.
	 * @param namespace		The namespace in which the query will be executed.
	 * @param sourceKey		The key of the source.
	 *
	 * @return				The result of the execution of the query.
	 */
	private List<List<String>> executeIpmiWmiRequest(
		final String hostname,
		final IWinConfiguration winConfiguration,
		final String wmiQuery,
		final String namespace,
		final String sourceKey
	) {
		log.info("Hostname {} - Executing IPMI Query for source [{}]:\nWMI Query: {}:\n", hostname, sourceKey, wmiQuery);

		List<List<String>> result;

		try {
			result = clientsExecutor.executeWql(hostname, winConfiguration, wmiQuery, namespace);
		} catch (Exception exception) {
			logSourceError(
				connectorId,
				sourceKey,
				String.format(
					"IPMI WMI query=%s, Hostname=%s, Username=%s, Timeout=%d, Namespace=%s",
					wmiQuery,
					hostname,
					winConfiguration.getUsername(),
					winConfiguration.getTimeout(),
					namespace
				),
				hostname,
				exception
			);

			result = Collections.emptyList();
		}

		log.info(
			"Hostname {} - IPMI query for [{}] result:\n{}\n",
			hostname,
			sourceKey,
			TextTableHelper.generateTextTable(result)
		);

		return result;
	}

	@WithSpan("Source OS Command Exec")
	@Override
	public SourceTable process(@SpanAttribute("source.definition") final OsCommandSource osCommandSource) {
		final String hostname = telemetryManager.getHostConfiguration().getHostname();

		if (
			osCommandSource == null || osCommandSource.getCommandLine() == null || osCommandSource.getCommandLine().isEmpty()
		) {
			log.error("Hostname {} - Malformed OS command source.", hostname);
			return SourceTable.empty();
		}

		try {
			final OsCommandResult osCommandResult = OsCommandHelper.runOsCommand(
				osCommandSource.getCommandLine(),
				telemetryManager,
				osCommandSource.getTimeout(),
				osCommandSource.getExecuteLocally(),
				telemetryManager.getHostProperties().isLocalhost()
			);

			// transform to lines
			final List<String> resultLines = SourceTable.lineToList(osCommandResult.getResult(), NEW_LINE);

			final List<String> filteredLines = FilterResultHelper.filterLines(
				resultLines,
				osCommandSource.getBeginAtLineNumber(),
				osCommandSource.getEndAtLineNumber(),
				osCommandSource.getExclude(),
				osCommandSource.getKeep()
			);

			final List<String> selectedColumnsLines = FilterResultHelper.selectedColumns(
				filteredLines,
				osCommandSource.getSeparators(),
				osCommandSource.getSelectColumns()
			);

			return SourceTable
				.builder()
				.rawData(selectedColumnsLines.stream().collect(Collectors.joining(NEW_LINE)))
				.table(
					selectedColumnsLines
						.stream()
						.map(line -> Stream.of(line.split(TABLE_SEP)).collect(Collectors.toList()))
						.collect(Collectors.toList())
				)
				.build();
		} catch (Exception e) {
			logSourceError(
				connectorId,
				osCommandSource.getKey(),
				String.format("OS command: %s.", osCommandSource.getCommandLine()),
				hostname,
				e
			);

			return SourceTable.empty();
		}
	}

	@WithSpan("Source SNMP Get Exec")
	@Override
	public SourceTable process(@SpanAttribute("source.definition") final SnmpGetSource snmpGetSource) {
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
			final String result = clientsExecutor.executeSNMPGet(snmpGetSource.getOid(), snmpConfiguration, hostname, true);

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
			logSourceError(
				connectorId,
				snmpGetSource.getKey(),
				String.format("SNMP Get: %s.", snmpGetSource.getOid()),
				hostname,
				e
			);
		}

		return SourceTable.empty();
	}

	@WithSpan("Source SNMP Table Exec")
	@Override
	public SourceTable process(@SpanAttribute("source.definition") final SnmpTableSource snmpTableSource) {
		final String hostname = telemetryManager.getHostConfiguration().getHostname();

		if (snmpTableSource == null) {
			log.error(
				"Hostname {} - SNMP Get Table Source cannot be null, the SNMP Get Table operation will return an empty result.",
				hostname
			);
			return SourceTable.empty();
		}

		// run the Client in order to execute the snmpTable
		// receives a List structure
		SourceTable sourceTable = new SourceTable();
		String selectedColumns = snmpTableSource.getSelectColumns();

		if (selectedColumns.isBlank()) {
			return SourceTable.empty();
		}

		// The selectedColumns String is like "column1, column2, column3" and we want to split it into ["column1", "column2", "column3"]
		final String[] selectedColumnArray = selectedColumns.split("\\s*,\\s*");

		final SnmpConfiguration protocol = (SnmpConfiguration) telemetryManager
			.getHostConfiguration()
			.getConfigurations()
			.get(SnmpConfiguration.class);

		if (protocol == null) {
			log.debug(
				"Hostname {} - The SNMP credentials are not configured. Returning an empty table for SNMP Get Table Source {}.",
				hostname,
				snmpTableSource
			);
			return SourceTable.empty();
		}

		try {
			final List<List<String>> result = clientsExecutor.executeSNMPTable(
				snmpTableSource.getOid(),
				selectedColumnArray,
				protocol,
				hostname,
				true
			);

			sourceTable.setHeaders(Arrays.asList(selectedColumnArray));
			sourceTable.setTable(result);

			return sourceTable;
		} catch (Exception e) { // NOSONAR on interruption
			logSourceError(
				connectorId,
				snmpTableSource.getKey(),
				String.format("SNMP Table: %s", snmpTableSource.getOid()),
				hostname,
				e
			);

			return SourceTable.empty();
		}
	}

	@WithSpan("Source Static Exec")
	@Override
	public SourceTable process(@SpanAttribute("source.definition") final StaticSource staticSource) {
		final String hostname = telemetryManager.getHostConfiguration().getHostname();

		if (staticSource == null) {
			log.error(
				"Hostname {} - Static Source cannot be null, the StaticSource operation will return an empty result.",
				hostname
			);
			return SourceTable.empty();
		}

		final String staticValue = staticSource.getValue();

		if (staticValue == null || staticValue.isEmpty()) {
			log.error(
				"Hostname {} - Static Source reference cannot be null. Returning an empty table for source {}.",
				hostname,
				staticSource
			);
			return SourceTable.empty();
		}

		log.debug(
			"Hostname {} - Got Static Source value [{}] referenced in source [{}].",
			hostname,
			staticValue,
			staticSource.getKey()
		);

		final SourceTable sourceTable = new SourceTable();

		final Optional<SourceTable> maybeStaticTable = SourceTable.lookupSourceTable(
			staticValue,
			connectorId,
			telemetryManager
		);

		if (maybeStaticTable.isEmpty()) {
			return SourceTable.empty();
		}

		// Note: In case of the static source getSourceTable never returns null
		final List<List<String>> table = maybeStaticTable
			.get()
			.getTable()
			.stream()
			// Map each row in the table to a new ArrayList, effectively performing a deep copy of each row.
			.map(ArrayList::new)
			.filter(row -> !row.isEmpty())
			.collect(Collectors.toList());

		sourceTable.setTable(table);
		sourceTable.setRawData(SourceTable.tableToCsv(sourceTable.getTable(), SEMICOLON, false));

		return sourceTable;
	}

	@WithSpan("Source TableJoin Exec")
	@Override
	public SourceTable process(@SpanAttribute("source.definition") final TableJoinSource tableJoinSource) {
		final String hostname = telemetryManager.getHostConfiguration().getHostname();

		if (tableJoinSource == null) {
			log.error(
				"Hostname {} - Table Join Source cannot be null, the Table Join will return an empty result.",
				hostname
			);
			return SourceTable.empty();
		}

		if (tableJoinSource.getLeftTable() == null) {
			log.debug(
				"Hostname {} - Left table cannot be null, the Join {} will return an empty result.",
				hostname,
				tableJoinSource
			);
			return SourceTable.empty();
		}

		final Optional<SourceTable> maybeLeftTable = SourceTable.lookupSourceTable(
			tableJoinSource.getLeftTable(),
			connectorId,
			telemetryManager
		);
		if (maybeLeftTable.isEmpty()) {
			log.debug(
				"Hostname {} - Reference to Left table cannot be found, the Join {} will return an empty result.",
				hostname,
				tableJoinSource
			);
			return SourceTable.empty();
		}

		if (tableJoinSource.getRightTable() == null) {
			log.debug(
				"Hostname {} - Right table cannot be null, the Join {} will return an empty result.",
				hostname,
				tableJoinSource
			);
			return SourceTable.empty();
		}

		final Optional<SourceTable> maybeRightTable = SourceTable.lookupSourceTable(
			tableJoinSource.getRightTable(),
			connectorId,
			telemetryManager
		);
		if (maybeRightTable.isEmpty()) {
			log.debug(
				"Hostname {} - Reference to Right table cannot be found, the Join {} will return an empty result.",
				hostname,
				tableJoinSource
			);
			return SourceTable.empty();
		}

		if (tableJoinSource.getLeftKeyColumn() < 1 || tableJoinSource.getRightKeyColumn() < 1) {
			log.error(
				"Hostname {} - Invalid key column number (leftKeyColumnNumber={}, rightKeyColumnNumber={}).",
				tableJoinSource.getLeftKeyColumn(),
				tableJoinSource.getDefaultRightLine(),
				hostname
			);
			return SourceTable.empty();
		}

		final SourceTable leftTable = maybeLeftTable.get();
		final SourceTable rightTable = maybeRightTable.get();
		logTableJoin(
			tableJoinSource.getKey(),
			tableJoinSource.getLeftTable(),
			tableJoinSource.getRightTable(),
			leftTable,
			rightTable,
			hostname
		);

		String defaultRightLine = tableJoinSource.getDefaultRightLine();

		final List<List<String>> executeTableJoin = clientsExecutor.executeTableJoin(
			leftTable.getTable(),
			rightTable.getTable(),
			tableJoinSource.getLeftKeyColumn(),
			tableJoinSource.getRightKeyColumn(),
			defaultRightLine != null ? Arrays.asList(defaultRightLine.split(";")) : null,
			"wbem".equalsIgnoreCase(tableJoinSource.getKeyType()),
			true
		);

		SourceTable sourceTable = new SourceTable();

		if (executeTableJoin != null) {
			sourceTable.setTable(executeTableJoin);
		}
		return sourceTable;
	}

	/**
	 * Log the table join left and right tables
	 *
	 * @param sourceKey      the table join source key
	 * @param leftSourceKey  the source key referencing the left source
	 * @param rightSourceKey the source key referencing the right source
	 * @param leftTable      the left table
	 * @param rightTable     the right table
	 */
	private static void logTableJoin(
		final String sourceKey,
		final String leftSourceKey,
		final String rightSourceKey,
		final SourceTable leftTable,
		final SourceTable rightTable,
		final String hostname
	) {
		if (!log.isDebugEnabled()) {
			return;
		}

		log.debug(
			"Hostname {} - Table Join Source [{}]:\nLeft table [{}]:\n{}\nRight table [{}]:\n{}\n",
			hostname,
			sourceKey,
			leftSourceKey,
			TextTableHelper.generateTextTable(leftTable.getHeaders(), leftTable.getTable()),
			rightSourceKey,
			TextTableHelper.generateTextTable(rightTable.getHeaders(), rightTable.getTable())
		);
	}

	@WithSpan("Source TableUnion Exec")
	@Override
	public SourceTable process(@SpanAttribute("source.definition") final TableUnionSource tableUnionSource) {
		final String hostname = telemetryManager.getHostConfiguration().getHostname();

		if (tableUnionSource == null) {
			log.warn(
				"Hostname {} - Table Union Source cannot be null, the Table Union operation will return an empty result.",
				hostname
			);
			return SourceTable.empty();
		}

		final List<String> unionTables = tableUnionSource.getTables();
		if (unionTables == null) {
			log.debug(
				"Hostname {} - Table list in the Union cannot be null, the Union operation {} will return an empty result.",
				hostname,
				tableUnionSource
			);
			return SourceTable.empty();
		}

		final List<SourceTable> sourceTablesToConcat = unionTables
			.stream()
			.map(key -> SourceTable.lookupSourceTable(key, connectorId, telemetryManager))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toList()); //NOSONAR

		final SourceTable sourceTable = new SourceTable();
		final List<List<String>> executeTableUnion = sourceTablesToConcat
			.stream()
			.map(SourceTable::getTable)
			.flatMap(Collection::stream)
			.collect(Collectors.toList()); //NOSONAR

		sourceTable.setTable(executeTableUnion);

		String rawData = sourceTablesToConcat
			.stream()
			.map(SourceTable::getRawData)
			.filter(Objects::nonNull)
			.collect(Collectors.joining(NEW_LINE))
			.replace("\n\n", NEW_LINE);

		sourceTable.setRawData(rawData);

		return sourceTable;
	}

	/**
	 * This method processes {@link WbemSource} instance
	 * @param wbemSource {@link WbemSource} instance
	 * @return {@link SourceTable} instance
	 */

	@WithSpan("Source WBEM HTTP Exec")
	public SourceTable process(@SpanAttribute("source.definition") final WbemSource wbemSource) {
		final String hostname = telemetryManager.getHostConfiguration().getHostname();

		if (wbemSource == null || wbemSource.getQuery() == null) {
			log.error("Hostname {} - Malformed WBEM Source {}. Returning an empty table.", hostname, wbemSource);
			return SourceTable.empty();
		}

		final WbemConfiguration wbemConfiguration = (WbemConfiguration) telemetryManager
			.getHostConfiguration()
			.getConfigurations()
			.get(WbemConfiguration.class);

		if (wbemConfiguration == null) {
			log.debug(
				"Hostname {} - The WBEM credentials are not configured. Returning an empty table for WBEM source {}.",
				hostname,
				wbemSource.getKey()
			);
			return SourceTable.empty();
		}

		// Get the namespace, the default one is : root/cimv2
		final String namespace = getNamespace(wbemSource);

		try {
			if (hostname == null) {
				log.error("Hostname {} - No hostname indicated, the URL cannot be built.", hostname);
				return SourceTable.empty();
			}
			if (wbemConfiguration.getPort() == null || wbemConfiguration.getPort() == 0) {
				log.error("Hostname {} - No port indicated to connect to the host", hostname);
				return SourceTable.empty();
			}

			final List<List<String>> table = clientsExecutor.executeWbem(
				hostname,
				wbemConfiguration,
				wbemSource.getQuery(),
				namespace
			);

			return SourceTable.builder().table(table).build();
		} catch (Exception e) {
			logSourceError(
				connectorId,
				wbemSource.getKey(),
				String.format(
					"WBEM query=%s, Username=%s, Timeout=%d, Namespace=%s",
					wbemSource.getQuery(),
					wbemConfiguration.getUsername(),
					wbemConfiguration.getTimeout(),
					namespace
				),
				hostname,
				e
			);

			return SourceTable.empty();
		}
	}

	/**
	 * Get the namespace to use for the execution of the given {@link WmiSource} instance
	 *
	 * @param wmiSource {@link WmiSource} instance from which we want to extract the namespace. Expected "automatic", null or <em>any
	 *                  string</em>
	 * @return {@link String} value
	 */
	String getNamespace(final WmiSource wmiSource) {
		final String sourceNamespace = wmiSource.getNamespace();

		if (sourceNamespace == null) {
			return WMI_DEFAULT_NAMESPACE;
		}

		if (AUTOMATIC_NAMESPACE.equalsIgnoreCase(sourceNamespace)) {
			// The namespace should be detected correctly in the detection strategy phase
			return telemetryManager.getHostProperties().getConnectorNamespace(connectorId).getAutomaticWmiNamespace();
		}

		return sourceNamespace;
	}

	/**
	 * This method processes {@link WmiSource} source
	 * @param wmiSource {@link WmiSource} source instance
	 * @return {@link SourceTable} instance
	 */
	@WithSpan("Source WMI Exec")
	@Override
	public SourceTable process(@SpanAttribute("source.definition") final WmiSource wmiSource) {
		final String hostname = telemetryManager.getHostConfiguration().getHostname();

		if (wmiSource == null || wmiSource.getQuery() == null) {
			log.warn("Hostname {} - Malformed WMI source {}. Returning an empty table.", hostname, wmiSource);
			return SourceTable.empty();
		}

		// Find the configured protocol (WinRM or WMI)
		final IWinConfiguration winConfiguration = telemetryManager.getWinConfiguration();

		if (winConfiguration == null) {
			log.debug(
				"Hostname {} - Neither WMI nor WinRM credentials are configured for this host. Returning an empty table for WMI source {}.",
				hostname,
				wmiSource.getKey()
			);
			return SourceTable.empty();
		}

		// Get the namespace
		final String namespace = getNamespace(wmiSource);

		if (namespace == null) {
			log.error(
				"Hostname {} - Failed to retrieve the WMI namespace to run the WMI source {}. Returning an empty table.",
				hostname,
				wmiSource.getKey()
			);
			return SourceTable.empty();
		}

		try {
			final List<List<String>> table = clientsExecutor.executeWql(
				hostname,
				winConfiguration,
				wmiSource.getQuery(),
				namespace
			);

			return SourceTable.builder().table(table).build();
		} catch (Exception e) {
			logSourceError(
				connectorId,
				wmiSource.getKey(),
				String.format(
					"WMI query=%s, Username=%s, Timeout=%d, Namespace=%s",
					wmiSource.getQuery(),
					winConfiguration.getUsername(),
					winConfiguration.getTimeout(),
					namespace
				),
				hostname,
				e
			);

			return SourceTable.empty();
		}
	}

	/**
	 * Log the given throwable
	 *
	 * @param connectorId  The identifier of the connector
	 * @param sourceKey    The key of the source
	 * @param hostname     The host's hostname
	 * @param context      Additional information about the operation
	 * @param throwable    The catched throwable to log
	 */
	private static void logSourceError(
		final String connectorId,
		final String sourceKey,
		final String context,
		final String hostname,
		final Throwable throwable
	) {
		if (log.isErrorEnabled()) {
			log.error(
				"Hostname {} - Source [{}] was unsuccessful due to an exception." +
				" Context [{}]. Connector: [{}]. Returning an empty table. Errors:\n{}\n",
				hostname,
				sourceKey,
				context,
				connectorId,
				StringHelper.getStackMessages(throwable)
			);
		}

		if (log.isDebugEnabled()) {
			log.debug(
				String.format(
					"Hostname %s - Source [%s] was unsuccessful due to an exception. Context [%s]. Connector: [%s]. Returning an empty table. Stack trace:",
					hostname,
					sourceKey,
					context,
					connectorId
				),
				throwable
			);
		}
	}

	/**
	 * Log the source copy data
	 *
	 * @param connectorId     the identifier of the connector defining the source
	 * @param parentSourceKey the parent source key referenced in the source copy
	 * @param childSourceKey  the source key referencing the parent source
	 * @param sourceTable     the source's result we wish to log
	 * @param hostname        the host's name
	 */
	private static void logSourceCopy(
		final String connectorId,
		final String parentSourceKey,
		final String childSourceKey,
		final SourceTable sourceTable,
		final String hostname
	) {
		if (!log.isDebugEnabled()) {
			return;
		}

		// Is there any raw data to log?
		if (sourceTable.getRawData() != null && (sourceTable.getTable() == null || sourceTable.getTable().isEmpty())) {
			log.debug(
				"Hostname {} - Got Source [{}] referenced in Source [{}]. Connector: [{}].\nRaw result:\n{}\n",
				hostname,
				parentSourceKey,
				childSourceKey,
				connectorId,
				sourceTable.getRawData()
			);
			return;
		}

		if (sourceTable.getRawData() == null) {
			log.debug(
				"Hostname {} - Got Source [{}] referenced in Source [{}]. Connector: [{}].\nTable result:\n{}\n",
				hostname,
				parentSourceKey,
				childSourceKey,
				connectorId,
				TextTableHelper.generateTextTable(sourceTable.getHeaders(), sourceTable.getTable())
			);
			return;
		}

		log.debug(
			"Hostname {} - Got Source [{}] referenced in Source [{}]. Connector: [{}].\nRaw result:\n{}\nTable result:\n{}\n",
			hostname,
			parentSourceKey,
			childSourceKey,
			connectorId,
			sourceTable.getRawData(),
			TextTableHelper.generateTextTable(sourceTable.getHeaders(), sourceTable.getTable())
		);
	}

	/**
	 * Get the namespace to use for the execution of the given {@link WbemSource} instance
	 *
	 * @param wbemSource {@link WbemSource} instance from which we want to extract the namespace. Expected "automatic", null or <em>any string</em>
	 * @return {@link String} value
	 */
	String getNamespace(final WbemSource wbemSource) {
		String namespace = wbemSource.getNamespace();
		if (namespace == null) {
			namespace = WMI_DEFAULT_NAMESPACE;
		} else if (AUTOMATIC_NAMESPACE.equalsIgnoreCase(namespace)) {
			namespace = telemetryManager.getHostProperties().getConnectorNamespace(connectorId).getAutomaticWbemNamespace();
		}
		return namespace;
	}
}
