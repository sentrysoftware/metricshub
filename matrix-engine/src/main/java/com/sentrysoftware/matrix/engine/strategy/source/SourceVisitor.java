package com.sentrysoftware.matrix.engine.strategy.source;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.AUTOMATIC_NAMESPACE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.NEW_LINE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TABLE_SEP;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.common.exception.MatsyaException;
import com.sentrysoftware.matrix.common.exception.NoCredentialProvidedException;
import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.http.HTTPSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.ipmi.IPMI;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.oscommand.OSCommandSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.reference.ReferenceSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.reference.StaticSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tablejoin.TableJoinSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tableunion.TableUnionSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.telnet.TelnetInteractiveSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.ucs.UCSSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.wbem.WBEMSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.wmi.WMISource;
import com.sentrysoftware.matrix.engine.protocol.HTTPProtocol;
import com.sentrysoftware.matrix.engine.protocol.IPMIOverLanProtocol;
import com.sentrysoftware.matrix.engine.protocol.OSCommandConfig;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol;
import com.sentrysoftware.matrix.engine.protocol.SSHProtocol;
import com.sentrysoftware.matrix.engine.protocol.WBEMProtocol;
import com.sentrysoftware.matrix.engine.protocol.WMIProtocol;
import com.sentrysoftware.matrix.engine.strategy.StrategyConfig;
import com.sentrysoftware.matrix.engine.strategy.matsya.HTTPRequest;
import com.sentrysoftware.matrix.engine.strategy.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.engine.strategy.utils.IpmiHelper;
import com.sentrysoftware.matrix.engine.strategy.utils.OsCommandHelper;
import com.sentrysoftware.matrix.engine.strategy.utils.OsCommandResult;
import com.sentrysoftware.matrix.engine.target.HardwareTarget;
import com.sentrysoftware.matrix.engine.target.TargetType;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class SourceVisitor implements ISourceVisitor {

	private static final String EXCEPTION = "Exception";

	private static final String WBEM = "wbem";

	private static final Pattern SOURCE_PATTERN = Pattern.compile(
			"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\)(.*))\\s*$",
			Pattern.CASE_INSENSITIVE);

	private StrategyConfig strategyConfig;
	private MatsyaClientsExecutor matsyaClientsExecutor;
	private Connector connector;

	@Override
	public SourceTable visit(final HTTPSource httpSource) {
		if (httpSource == null) {
			log.error("HTTPSource cannot be null, the HTTPSource operation will return an empty result.");
			return SourceTable.empty();
		}

		final HTTPProtocol protocol = (HTTPProtocol) strategyConfig.getEngineConfiguration()
				.getProtocolConfigurations().get(HTTPProtocol.class);

		if (protocol == null) {
			log.debug("The HTTP Credentials are not configured. Returning an empty table for HTTPSource {}.",
					httpSource);
			return SourceTable.empty();
		}

		final String hostname = strategyConfig.getEngineConfiguration().getTarget().getHostname();

		try {
			final String result = matsyaClientsExecutor.executeHttp(
					HTTPRequest.builder()
					.hostname(strategyConfig.getEngineConfiguration().getTarget().getHostname())
					.method(httpSource.getMethod())
					.url(httpSource.getUrl())
					.header(httpSource.getHeader())
					.body(httpSource.getBody())
					.resultContent(httpSource.getResultContent())
					.authenticationToken(httpSource.getAuthenticationToken())
					.httpProtocol(protocol)
					.build(),
					true);

			if (result != null) {
				return SourceTable
						.builder()
						.rawData(result)
						.build();
			}

		} catch (Exception e) {
			final String message = String.format(
					"HTTP request of %s was unsuccessful due to an exception. Message: %s.",
					hostname, e.getMessage());
			log.debug(message, e);
		}

		return SourceTable.empty();
	}

	@Override
	public SourceTable visit(final IPMI ipmi) {
		HardwareTarget target = strategyConfig.getEngineConfiguration().getTarget();
		final TargetType targetType = target.getType();

		if (TargetType.MS_WINDOWS.equals(targetType)) {
			return processWindowsIpmiSource();
		} else if (TargetType.LINUX.equals(targetType) || TargetType.SUN_SOLARIS.equals(targetType)) {
			return processUnixIpmiSource();
		} else if (TargetType.MGMT_CARD_BLADE_ESXI.equals(targetType)) {
			return processOutOfBandIpmiSource();
		}

		log.debug("Failed to process IPMI source on system: {}. {} is an unsupported OS for IPMI.",
				target.getHostname(), targetType.name());

		return SourceTable.empty();
	}

	/**
	 * Process IPMI source via IPMI Over-LAN
	 *
	 * @return {@link SourceTable} containing the IPMI result expected by the IPMI connector embedded AWK script
	 */
	SourceTable processOutOfBandIpmiSource() {

		final IPMIOverLanProtocol protocol = (IPMIOverLanProtocol) strategyConfig.getEngineConfiguration()
				.getProtocolConfigurations().get(IPMIOverLanProtocol.class);

		if (protocol == null) {
			log.warn("The IPMI Credentials are not configured. Cannot process IPMI-over-LAN source.");
			return SourceTable.empty();
		}

		String hostname = strategyConfig.getEngineConfiguration().getTarget().getHostname();

		try {
			String result = matsyaClientsExecutor.executeIpmiGetSensors(hostname, protocol);

			if (result != null) {
				return SourceTable.builder().rawData(result).build();
			} else {
				log.error("IPMI-over-LAN request on system {} returned <null> result.", hostname);
			}
		} catch (Exception e) {
			log.error("IPMI-over-LAN request on system {} was unsuccessful due to an exception.", hostname);
			log.error(EXCEPTION, e);
		}

		return SourceTable.empty();
	}

	/**
	 * Process IPMI Source for the Unix system
	 *
	 * @return {@link SourceTable} containing the IPMI result expected by the IPMI connector embedded AWK script
	 */
	SourceTable processUnixIpmiSource() {
		final String hostname = strategyConfig.getEngineConfiguration().getTarget().getHostname();

		// get the ipmiTool command to execute
		String ipmitoolCommand = strategyConfig.getHostMonitoring().getIpmitoolCommand();
		if (ipmitoolCommand == null || ipmitoolCommand.isEmpty()) {
			final String message = String.format("IPMI Tool Command cannot be found for %s. Return empty result.",
					hostname);
			log.error(message);
			return SourceTable.empty();
		}

		final SSHProtocol sshProtocol = (SSHProtocol) strategyConfig.getEngineConfiguration()
				.getProtocolConfigurations().get(SSHProtocol.class);
		final OSCommandConfig osCommandConfig = (OSCommandConfig) strategyConfig.getEngineConfiguration()
				.getProtocolConfigurations().get(OSCommandConfig.class);

		if (osCommandConfig == null) {
			final String message = String.format("No OS Command Configuration for %s. Return empty result.", hostname);
			log.error(message);
			return SourceTable.empty();
		}
		final int defaultTimeout = osCommandConfig.getTimeout().intValue();

		boolean isLocalHost = strategyConfig.getHostMonitoring().isLocalhost();
		// fru command
		String fruCommand = ipmitoolCommand + "fru";
		String fruResult;
		try {
			if (isLocalHost) {
				fruResult = OsCommandHelper.runLocalCommand(fruCommand, defaultTimeout, null);
			} else {
				fruResult = OsCommandHelper.runSshCommand(fruCommand, hostname, sshProtocol, defaultTimeout, null, null);
			}
			log.debug("processUnixIpmiSource(%s): OS Command: %s:\n%s", hostname, fruCommand, fruResult);

		} catch (IOException |InterruptedException | TimeoutException | MatsyaException  e) {
			final String message = String.format("Failed to execute the OS Command %s for %s. Return empty result. Exception : %s",
					fruCommand, hostname, e);
			log.error(message);
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
				sensorResult = OsCommandHelper.runSshCommand(sdrCommand, hostname, sshProtocol, defaultTimeout,	null, null);;
			}
			log.debug("processUnixIpmiSource(%s): OS Command: %s:\n%s", hostname, sdrCommand, sensorResult);
		} catch (IOException | InterruptedException | TimeoutException | MatsyaException e) {
			final String message = String.format("Failed to execute the OS Command %s for %s. Return empty result. Exception : %s",
					sdrCommand, hostname, e);
			log.error(message);
			Thread.currentThread().interrupt();
			return SourceTable.empty();
		}

		return SourceTable.builder().table(IpmiHelper.ipmiTranslateFromIpmitool(fruResult, sensorResult)).build();

	}

	/**
	 * Process IPMI source for the Windows (NT) system
	 *
	 * @return {@link SourceTable} containing the IPMI result expected by the IPMI connector embedded AWK script
	 */
	SourceTable processWindowsIpmiSource() {
		final WMIProtocol wmiProtocol = (WMIProtocol) strategyConfig.getEngineConfiguration().getProtocolConfigurations().get(WMIProtocol.class);
		if (wmiProtocol == null) {
			return SourceTable.empty();
		}

		final String hostname = strategyConfig.getEngineConfiguration().getTarget().getHostname();
		final String nameSpaceRootCimv2 = "root/cimv2";
		final String nameSpaceRootHardware = "root/hardware";

		String wmiQuery = "SELECT IdentifyingNumber,Name,Vendor FROM Win32_ComputerSystemProduct";
		List<List<String>> wmiCollection1 = executeIpmiWmiRequest(hostname, wmiProtocol, wmiQuery, nameSpaceRootCimv2);

		wmiQuery = "SELECT BaseUnits,CurrentReading,Description,LowerThresholdCritical,LowerThresholdNonCritical,SensorType,UnitModifier,UpperThresholdCritical,UpperThresholdNonCritical FROM NumericSensor";
		List<List<String>> wmiCollection2 = executeIpmiWmiRequest(hostname, wmiProtocol, wmiQuery, nameSpaceRootHardware);

		wmiQuery = "SELECT CurrentState,Description FROM Sensor";
		List<List<String>> wmiCollection3 = executeIpmiWmiRequest(hostname, wmiProtocol, wmiQuery, nameSpaceRootHardware);

		return SourceTable.builder().table(IpmiHelper.ipmiTranslateFromWmi(wmiCollection1, wmiCollection2, wmiCollection3)).build();
	}

	@Override
	public SourceTable visit(final OSCommandSource osCommandSource) {
		if (osCommandSource == null ||
				osCommandSource.getCommandLine() == null || osCommandSource.getCommandLine().isEmpty()) {
			log.error("OSCommandSource Malformed OSCommand source.");
			return SourceTable.empty();
		}

		try {
			final OsCommandResult osCommandResult = OsCommandHelper.runOsCommand(
					osCommandSource.getCommandLine(),
					strategyConfig.getEngineConfiguration(),
					connector.getEmbeddedFiles(),
					osCommandSource.getTimeout(),
					osCommandSource.isExecuteLocally(),
					strategyConfig.getHostMonitoring().isLocalhost());

			// transform to lines
			final List<String> resultLines = SourceTable.lineToList(
					osCommandResult.getResult(),
					HardwareConstants.NEW_LINE);

			final List<String> filteredLines = OsCommandHelper.filterLines(
					resultLines,
					osCommandSource.getRemoveHeader(),
					osCommandSource.getRemoveFooter(),
					osCommandSource.getExcludeRegExp(),
					osCommandSource.getKeepOnlyRegExp());

			final List<String> selectedColumnsLines = OsCommandHelper.selectedColumns(
					filteredLines,
					osCommandSource.getSeparators(),
					osCommandSource.getSelectColumns());
			
			return SourceTable.builder()
					.rawData(selectedColumnsLines.stream()
							// add the TABLE_SEP at the end of each lines.
							.map(line -> line + TABLE_SEP)
							.collect(Collectors.joining(NEW_LINE)))
					.table(selectedColumnsLines.stream()
							// Replace all separators by ";", which is the standard separator used by MS_HW
							.map(line -> SourceTable.lineToList(line, TABLE_SEP))
							.collect(Collectors.toList()))
					.build();

		} catch(NoCredentialProvidedException e) {
			log.debug("OSCommandSource " + e.getMessage());
			return SourceTable.empty();
		} catch (Exception e) {
			log.error("OSCommandSource error runing command", e.getMessage());
			log.debug("OSCommandSource error runing command", e);
			return SourceTable.empty();
		}
	}

	@Override
	public SourceTable visit(final ReferenceSource referenceSource) {
		if (referenceSource == null) {
			log.error("ReferenceSource cannot be null, the ReferenceSource operation will return an empty result.");
			return SourceTable.empty();
		}

		final String reference = referenceSource.getReference();

		if (reference == null || reference.isEmpty()) {
			log.error("ReferenceSource reference cannot be null. Returning an empty table for source {}.", referenceSource);
			return SourceTable.empty();
		}

		final SourceTable sourceTable = new SourceTable();

		final SourceTable origin = getSourceTable(reference);
		if (origin == null) {
			return SourceTable.empty();
		}

		final List<List<String>> table = origin.getTable()
				.stream()
				.map(ArrayList::new)
				.filter(row -> !row.isEmpty())
				.collect(Collectors.toList());

		sourceTable.setTable(table);

		return sourceTable;
	}

	@Override
	public SourceTable visit(final StaticSource staticSource) {
		if (staticSource == null) {
			log.error("StaticSource cannot be null, the StaticSource operation will return an empty result.");
			return SourceTable.empty();
		}

		final String staticValue = staticSource.getStaticValue();

		if (staticValue == null || staticValue.isEmpty()) {
			log.error("StaticSource reference cannot be null. Returning an empty table for source {}.", staticSource);
			return SourceTable.empty();
		}

		final SourceTable sourceTable = new SourceTable();

		// Call getSpourceTable, in case there are ';' in the static source and it's needed to be separated into multiple columns
		// Note: In case of the static source getSourceTable never returns null
		final List<List<String>> table = getSourceTable(staticValue).getTable()
				.stream()
				.map(ArrayList::new)
				.filter(row -> !row.isEmpty())
				.collect(Collectors.toList());

		sourceTable.setTable(table);

		return sourceTable;
	}

	@Override
	public SourceTable visit(final SNMPGetSource snmpGetSource) {
		if (snmpGetSource == null) {
			log.error("SNMPGetSource cannot be null, the SNMP Get operation will return an empty result.");
			return SourceTable.empty();

		}

		if (snmpGetSource.getOid() == null) {
			log.error("SNMPGetSource OID cannot be null. Returning an empty table for source {}.", snmpGetSource);
			return SourceTable.empty();
		}

		final SNMPProtocol protocol = (SNMPProtocol) strategyConfig.getEngineConfiguration()
				.getProtocolConfigurations().get(SNMPProtocol.class);

		if (protocol == null) {
			log.debug("The SNMP Credentials are not configured. Returning an empty table for SNMPGetSource {}.",
					snmpGetSource);
			return SourceTable.empty();
		}

		final String hostname = strategyConfig.getEngineConfiguration().getTarget().getHostname();

		try {

			final String result = matsyaClientsExecutor.executeSNMPGet(
					snmpGetSource.getOid(),
					protocol,
					hostname,
					true);

			if (result != null) {
					return SourceTable
							.builder()
							.table(Stream.of(Stream.of(result).collect(Collectors.toList())).collect(Collectors.toList()))
							.build();
			}

		} catch (Exception e) {
			final String message = String.format(
					"SNMP Get Source of %s on %s was unsuccessful due to an exception. Message: %s.",
					snmpGetSource.getOid(), hostname, e.getMessage());
			log.debug(message, e);
		}

		return SourceTable.empty();
	}

	@Override
	public SourceTable visit(final SNMPGetTableSource snmpGetTableSource) {
		if (snmpGetTableSource == null) {
			log.error("SNMPGetTableSource cannot be null, the SNMP GetTable operation will return an empty result.");
			return SourceTable.empty();

		}

		if (snmpGetTableSource.getOid() == null) {
			log.error("SNMPGetTableSource OID cannot be null. Returning an empty table for source {}.", snmpGetTableSource);
			return SourceTable.empty();
		}

		// run Matsya in order to execute the snmpTable
		// receives a List structure
		SourceTable sourceTable = new SourceTable();
		List<String> selectedColumns = snmpGetTableSource.getSnmpTableSelectColumns();

		if (selectedColumns == null) {
			return SourceTable.empty();
		}
		String[] selectColumnArray = new String[selectedColumns.size()];
		selectColumnArray = selectedColumns.toArray(selectColumnArray);

		final SNMPProtocol protocol = (SNMPProtocol) strategyConfig.getEngineConfiguration()
				.getProtocolConfigurations().get(SNMPProtocol.class);

		if (protocol == null) {
			log.debug("The SNMP Credentials are not configured. Returning an empty table for SNMPGetTableSource {}.",
					snmpGetTableSource);
			return SourceTable.empty();
		}

		final String hostname = strategyConfig.getEngineConfiguration().getTarget().getHostname();

		try {

			final List<List<String>> result = matsyaClientsExecutor.executeSNMPTable(
					snmpGetTableSource.getOid(),
					selectColumnArray,
					protocol,
					hostname,
					true);

			sourceTable.setHeaders(selectedColumns);
			sourceTable.setTable(result);

			return sourceTable;

		} catch (Exception e) {
			final String message = String.format(
					"SNMP Test Failed - SNMP Table of %s on %s was unsuccessful due to an exception. Message: %s.",
					snmpGetTableSource.getOid(), hostname, e.getMessage());
			log.error(message, e);
			return SourceTable.empty();
		}
	}

	@Override
	public SourceTable visit(final TableJoinSource tableJoinSource) {
		if (tableJoinSource == null) {
			log.error("TableJoinSource cannot be null, the Table Join will return an empty result.");
			return SourceTable.empty();
		}

		final Map<String, SourceTable> sources = strategyConfig.getHostMonitoring()
				.getConnectorNamespace(connector)
				.getSourceTables();

		if (sources == null ) {
			log.warn("SourceTable Map cannot be null, the Table Join {} will return an empty result.", tableJoinSource);
			return SourceTable.empty();
		}

		if (tableJoinSource.getLeftTable() == null || sources.get(tableJoinSource.getLeftTable()) == null ||  sources.get(tableJoinSource.getLeftTable()).getTable() == null) {
			log.debug("LeftTable cannot be null, the Join {} will return an empty result.", tableJoinSource);
			return SourceTable.empty();
		}

		if (tableJoinSource.getRightTable() == null || sources.get(tableJoinSource.getRightTable()) == null || sources.get(tableJoinSource.getRightTable()).getTable() == null) {
			log.debug("RightTable cannot be null, the Join {} will return an empty result.", tableJoinSource);
			return SourceTable.empty();
		}

		if (tableJoinSource.getLeftKeyColumn() < 1 || tableJoinSource.getRightKeyColumn() < 1) {
			log.error("Invalid key column number (leftKeyColumnNumber=" + tableJoinSource.getLeftKeyColumn()
			+ ", rightKeyColumnNumber=" + tableJoinSource.getDefaultRightLine() + ")");
			return SourceTable.empty();
		}

		final List<List<String>> executeTableJoin = matsyaClientsExecutor.executeTableJoin(
				sources.get(tableJoinSource.getLeftTable()).getTable(),
				sources.get(tableJoinSource.getRightTable()).getTable(),
				tableJoinSource.getLeftKeyColumn(),
				tableJoinSource.getRightKeyColumn(),
				tableJoinSource.getDefaultRightLine(),
				WBEM.equalsIgnoreCase(tableJoinSource.getKeyType()),
				false);

		SourceTable sourceTable = new SourceTable();
		if (executeTableJoin != null) {
			sourceTable.setTable(executeTableJoin);
		}

		return sourceTable;
	}

	@Override
	public SourceTable visit(final TableUnionSource tableUnionSource) {

		if (tableUnionSource == null) {
			log.warn("TableUnionSource cannot be null, the Table Union operation will return an empty result.");
			return SourceTable.empty();
		}

		final List<String> unionTables = tableUnionSource.getTables();
		if (unionTables == null) {
			log.debug("Table list in the Union cannot be null, the Union operation {} will return an empty result.",
					tableUnionSource);
			return SourceTable.empty();
		}

		final List<SourceTable> sourceTablesToConcat = unionTables
				.stream()
				.map(this::getSourceTable)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

		final SourceTable sourceTable = new SourceTable();
		final List<List<String>> executeTableUnion = sourceTablesToConcat
				.stream()
				.map(SourceTable::getTable)
				.flatMap(Collection::stream)
				.collect(Collectors.toList());

		sourceTable.setTable(executeTableUnion);

		return sourceTable;
	}

	/**
	 * Get source table based on the key
	 *
	 * @param key
	 * @return A {@link SourceTable} already defined in the current {@link IHostMonitoring} or a hard-coded CSV sourceTable
	 */
	SourceTable getSourceTable(final String key) {

		if (SOURCE_PATTERN.matcher(key).matches()) {
			final SourceTable sourceTable = strategyConfig
					.getHostMonitoring()
					.getConnectorNamespace(connector)
					.getSourceTable(key);
			if (sourceTable == null) {
				log.warn("The following source table {} cannot be found.", key);
			}
			return sourceTable;
		}

		return SourceTable.builder()
				.table(SourceTable.csvToTable(key, TABLE_SEP))
				.build();
	}

	@Override
	public SourceTable visit(final TelnetInteractiveSource telnetInteractiveSource) {
		return SourceTable.empty();
	}

	@Override
	public SourceTable visit(final UCSSource ucsSource) {
		return SourceTable.empty();
	}

	@Override
	public SourceTable visit(final WBEMSource wbemSource) {

		if (wbemSource == null || wbemSource.getWbemQuery() == null) {
			log.error("Malformed WBEMSource {}. Returning an empty table.", wbemSource);
			return SourceTable.empty();
		}

		final WBEMProtocol protocol = (WBEMProtocol) strategyConfig.getEngineConfiguration()
				.getProtocolConfigurations().get(WBEMProtocol.class);

		if (protocol == null) {
			log.debug("The WBEM Credentials are not configured. Returning an empty table for WBEM source {}.",
					wbemSource.getKey());
			return SourceTable.empty();
		}

		// Get the namespace, the default one is : root/cimv2
		String namespace = wbemSource.getWbemNamespace() != null ? wbemSource.getWbemNamespace()
				: protocol.getNamespace();

		final String hostname = strategyConfig.getEngineConfiguration().getTarget().getHostname();

		try {
			if (hostname == null) {
				log.error("No hostname indicated, the URL cannot be built.");
				return SourceTable.empty();
			}
			if (protocol.getPort() == null || protocol.getPort() == 0) {
				log.error("No port indicated to connect to the following hostname : {}", hostname);
				return SourceTable.empty();
			}

			final List<List<String>> table = matsyaClientsExecutor.executeWbem(hostname, protocol, wbemSource.getWbemQuery(), namespace);

			return SourceTable.builder().table(table).build();

		} catch (Exception e) {
			log.error("Error detected when running WBEM Query: {}. hostname={}, username={}, timeout={}, namespace={}",
					wbemSource.getWbemQuery(), hostname, protocol.getUsername(), protocol.getTimeout(),
					wbemSource.getWbemNamespace());
			log.error(EXCEPTION, e);
			return SourceTable.empty();
		}
	}

	@Override
	public SourceTable visit(final WMISource wmiSource) {
		if (wmiSource == null || wmiSource.getWbemQuery() == null) {
			log.warn("Malformed WMISource {}. Returning an empty table.", wmiSource);
			return SourceTable.empty();
		}

		final WMIProtocol protocol = (WMIProtocol) strategyConfig.getEngineConfiguration()
				.getProtocolConfigurations().get(WMIProtocol.class);

		if (protocol == null) {
			log.debug("The WMI Credentials are not configured. Returning an empty table for WMI source {}.",
					wmiSource.getKey());
			return SourceTable.empty();
		}

		// Get the namespace
		final String namespace = getNamespace(wmiSource, protocol);

		if (namespace == null) {
			log.error("Failed to retrieve the WMI namespace to run the WMI source {}. Returning an empty table.", wmiSource.getKey());
			return SourceTable.empty();
		}

		final String hostname = strategyConfig.getEngineConfiguration().getTarget().getHostname();

		try {

			final List<List<String>> table =
					matsyaClientsExecutor.executeWmi(hostname, protocol, wmiSource.getWbemQuery(), namespace);

			return SourceTable.builder().table(table).build();

		} catch (Exception e) {
			log.error("Error detected when running WMI Query: {}. hostname={}, username={}, timeout={}, namespace={}",
					wmiSource.getWbemQuery(), hostname,
					protocol.getUsername(), protocol.getTimeout(),
					wmiSource.getWbemNamespace());
			log.error(EXCEPTION, e);
			return SourceTable.empty();
		}

	}

	/**
	 * Get the namespace to use for the execution of the given {@link WMISource} instance
	 *
	 * @param wmiSource {@link WMISource} instance from which we want to extract the namespace. Expected "automatic", null or <em>any
	 *                  string</em>
	 * @param protocol The {@link WMIProtocol} from which we get the default namespace when the mode is not automatic
	 * @return {@link String} value
	 */
	String getNamespace(final WMISource wmiSource, final WMIProtocol protocol) {

		final String sourceNamespace = wmiSource.getWbemNamespace();

		if (AUTOMATIC_NAMESPACE.equalsIgnoreCase(sourceNamespace)) {
			// The namespace should be detected correctly in the detection strategy phase
			return strategyConfig
					.getHostMonitoring()
					.getConnectorNamespace(connector)
					.getAutomaticWmiNamespace();
		}

		return sourceNamespace;

	}

	/**
	 * Call the matsya client executor to execute a WMI request.
	 * @param hostname
	 * @param wmiProtocol
	 * @param wmiQuery
	 * @param namespace
	 * @return
	 */
	private List<List<String>> executeIpmiWmiRequest(final String hostname, final WMIProtocol wmiProtocol,
			final String wmiQuery, final String namespace) {

		log.debug("Executing IPMI Query ({}): WMI Query: {}:\n", hostname, wmiQuery);

		try {
			return matsyaClientsExecutor.executeWmi(
					hostname,
					wmiProtocol,
					wmiQuery,
					namespace
			);
		} catch (Exception e) {
			log.error("Error detected when running IPMI Query: {}. hostname={}, username={}, timeout={}, namespace={}",
					wmiQuery,
					hostname,
					wmiProtocol.getUsername(),
					wmiProtocol.getTimeout(),
					namespace);
			return Collections.emptyList();
		}
	}
}
