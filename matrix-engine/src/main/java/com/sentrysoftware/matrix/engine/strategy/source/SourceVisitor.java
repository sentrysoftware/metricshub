package com.sentrysoftware.matrix.engine.strategy.source;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.AUTOMATIC_NAMESPACE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.NEW_LINE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TABLE_SEP;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.common.helpers.StringHelper;
import com.sentrysoftware.matrix.common.helpers.TextTableHelper;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.http.HTTPSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.ipmi.IPMI;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.oscommand.OSCommandSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.reference.ReferenceSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.reference.StaticSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.sshinteractive.SshInteractiveSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tablejoin.TableJoinSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tableunion.TableUnionSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.ucs.UCSSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.wbem.WBEMSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.wmi.WMISource;
import com.sentrysoftware.matrix.engine.protocol.AbstractCommand;
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
import com.sentrysoftware.matrix.engine.strategy.utils.FilterResultHelper;
import com.sentrysoftware.matrix.engine.strategy.utils.IpmiHelper;
import com.sentrysoftware.matrix.engine.strategy.utils.OsCommandHelper;
import com.sentrysoftware.matrix.engine.strategy.utils.OsCommandResult;
import com.sentrysoftware.matrix.engine.strategy.utils.SshInteractiveHelper;
import com.sentrysoftware.matrix.engine.target.HardwareTarget;
import com.sentrysoftware.matrix.engine.target.TargetType;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class SourceVisitor implements ISourceVisitor {

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
					.hostname(hostname)
					.method(httpSource.getMethod())
					.url(httpSource.getUrl())
					.header(httpSource.getHeader())
					.body(httpSource.getBody())
					.resultContent(httpSource.getResultContent())
					.authenticationToken(httpSource.getAuthenticationToken())
					.httpProtocol(protocol)
					.build(),
					true);

			if (result != null && !result.isEmpty()) {

				return SourceTable
					.builder()
					.rawData(result)
					.build();
			}

		} catch (Exception e) {
			logSourceError(connector.getCompiledFilename(), httpSource.getKey(), String.format("HTTP %s %s", httpSource.getMethod(),
					httpSource.getUrl()) , hostname, e);
		}

		return SourceTable.empty();
	}

	@Override
	public SourceTable visit(final IPMI ipmi) {

		HardwareTarget target = strategyConfig.getEngineConfiguration().getTarget();
		final TargetType targetType = target.getType();

		String sourceKey = ipmi.getKey();

		if (TargetType.MS_WINDOWS.equals(targetType)) {
			return processWindowsIpmiSource(sourceKey);
		} else if (TargetType.LINUX.equals(targetType) || TargetType.SUN_SOLARIS.equals(targetType)) {
			return processUnixIpmiSource(sourceKey);
		} else if (TargetType.MGMT_CARD_BLADE_ESXI.equals(targetType)) {
			return processOutOfBandIpmiSource(sourceKey);
		}

		log.info("Failed to process IPMI source on system: {}. {} is an unsupported OS for IPMI. Returning an empty table.",
			target.getHostname(), targetType.name());

		return SourceTable.empty();
	}

	/**
	 * Process IPMI source via IPMI Over-LAN
	 *
	 * @param sourceKey The key of the source
	 *
	 * @return {@link SourceTable} containing the IPMI result expected by the IPMI connector embedded AWK script
	 */
	SourceTable processOutOfBandIpmiSource(String sourceKey) {

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

				return SourceTable
					.builder()
					.rawData(result)
					.build();

			} else {
				log.error("IPMI-over-LAN request on system {} returned <null> result. Returning an empty table.", hostname);
			}
		} catch (Exception e) {
			logSourceError(connector.getCompiledFilename(), sourceKey, "IPMI-over-LAN", hostname, e);
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
	SourceTable processUnixIpmiSource(String sourceKey) {

		final String hostname = strategyConfig.getEngineConfiguration().getTarget().getHostname();

		// get the ipmiTool command to execute
		String ipmitoolCommand = strategyConfig.getHostMonitoring().getIpmitoolCommand();
		if (ipmitoolCommand == null || ipmitoolCommand.isEmpty()) {
			final String message = String.format("IPMI Tool Command cannot be found for %s. Return empty result.",
					hostname);
			log.error(message);
			return SourceTable.empty();
		}

		boolean isLocalHost = strategyConfig.getHostMonitoring().isLocalhost();

		final SSHProtocol sshProtocol = (SSHProtocol) strategyConfig.getEngineConfiguration()
				.getProtocolConfigurations().get(SSHProtocol.class);
		final OSCommandConfig osCommandConfig = (OSCommandConfig) strategyConfig.getEngineConfiguration()
				.getProtocolConfigurations().get(OSCommandConfig.class);

		final int defaultTimeout = osCommandConfig != null ? osCommandConfig.getTimeout().intValue()
				: AbstractCommand.DEFAULT_TIMEOUT.intValue();

		// fru command
		String fruCommand = ipmitoolCommand + "fru";
		String fruResult;
		try {
			if (isLocalHost) {
				fruResult = OsCommandHelper.runLocalCommand(fruCommand, defaultTimeout, null);
			} else if (sshProtocol != null){
				fruResult = OsCommandHelper.runSshCommand(fruCommand, hostname, sshProtocol, defaultTimeout, null, null);
			} else {
				log.warn("Couldn't process unix IPMI Source. SSH Protocol credentials missing.");
				return SourceTable.empty();
			}

			log.debug("processUnixIpmiSource({}): OS Command: {}:\n{}", hostname, fruCommand, fruResult);

		} catch (Exception e) {

			logSourceError(connector.getCompiledFilename(), 
					sourceKey, String.format("IPMI OS Command: %s", fruCommand), hostname, e);

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
				sensorResult = OsCommandHelper.runSshCommand(sdrCommand, hostname, sshProtocol, defaultTimeout,	null, null);
			}
			log.debug("processUnixIpmiSource({}): OS Command: {}:\n{}", hostname, sdrCommand, sensorResult);
		} catch (Exception e) {

			logSourceError(connector.getCompiledFilename(), 
					sourceKey, String.format("IPMI OS Command: %s", sdrCommand), hostname, e);

			Thread.currentThread().interrupt();

			return SourceTable.empty();
		}

		return SourceTable
				.builder()
				.table(IpmiHelper.ipmiTranslateFromIpmitool(fruResult, sensorResult))
				.build();

	}

	/**
	 * Process IPMI source for the Windows (NT) system
	 *
	 * @param sourceKey The key of the source
	 *
	 * @return {@link SourceTable} containing the IPMI result expected by the IPMI connector embedded AWK script
	 */
	SourceTable processWindowsIpmiSource(String sourceKey) {

		final WMIProtocol wmiProtocol = (WMIProtocol) strategyConfig.getEngineConfiguration().getProtocolConfigurations().get(WMIProtocol.class);
		if (wmiProtocol == null) {
			return SourceTable.empty();
		}

		final String hostname = strategyConfig.getEngineConfiguration().getTarget().getHostname();
		final String nameSpaceRootCimv2 = "root/cimv2";
		final String nameSpaceRootHardware = "root/hardware";

		String wmiQuery = "SELECT IdentifyingNumber,Name,Vendor FROM Win32_ComputerSystemProduct";
		List<List<String>> wmiCollection1 = executeIpmiWmiRequest(hostname, wmiProtocol, wmiQuery, nameSpaceRootCimv2,
			sourceKey);

		wmiQuery = "SELECT BaseUnits,CurrentReading,Description,LowerThresholdCritical,LowerThresholdNonCritical,SensorType,UnitModifier,UpperThresholdCritical,UpperThresholdNonCritical FROM NumericSensor";
		List<List<String>> wmiCollection2 = executeIpmiWmiRequest(hostname, wmiProtocol, wmiQuery, nameSpaceRootHardware,
			sourceKey);

		wmiQuery = "SELECT CurrentState,Description FROM Sensor";
		List<List<String>> wmiCollection3 = executeIpmiWmiRequest(hostname, wmiProtocol, wmiQuery,
			nameSpaceRootHardware, sourceKey);

		return SourceTable
				.builder()
				.table(IpmiHelper.ipmiTranslateFromWmi(wmiCollection1, wmiCollection2, wmiCollection3))
				.build();

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

			final List<String> filteredLines = FilterResultHelper.filterLines(
					resultLines,
					osCommandSource.getRemoveHeader(),
					osCommandSource.getRemoveFooter(),
					osCommandSource.getExcludeRegExp(),
					osCommandSource.getKeepOnlyRegExp());

			final List<String> selectedColumnsLines = FilterResultHelper.selectedColumns(
					filteredLines,
					osCommandSource.getSeparators(),
					osCommandSource.getSelectColumns());

			return SourceTable
				.builder()
				.rawData(selectedColumnsLines.stream()
					.collect(Collectors.joining(NEW_LINE)))
				.table(selectedColumnsLines.stream()
						.map(line -> Stream.of(line.split(HardwareConstants.TABLE_SEP)).collect(Collectors.toList()))
						.collect(Collectors.toList()))
				.build();

		} catch(Exception e) {

			logSourceError(connector.getCompiledFilename(), osCommandSource.getKey(),
					String.format("OS Command: %s", osCommandSource.getCommandLine()),
					strategyConfig.getEngineConfiguration().getTarget().getHostname(), e);

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

		if (origin.getRawData() != null) {
			sourceTable.setRawData(origin.getRawData());
		}

		logSourceReference(connector.getCompiledFilename(), reference, referenceSource.getKey(), sourceTable);

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

		log.debug("Got Static Source Value [{}] referenced in Source [{}].",
				staticValue,
				staticSource.getKey());

		final SourceTable sourceTable = new SourceTable();

		// Call getSourceTable, in case there are ';' in the static source and it's needed to be separated into multiple columns
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

			logSourceError(connector.getCompiledFilename(), 
					snmpGetSource.getKey(), String.format("SNMP Get: %s", snmpGetSource.getOid()),
					hostname, e);
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

			logSourceError(connector.getCompiledFilename(), snmpGetTableSource.getKey(),
					String.format("SNMP Table: %s", snmpGetTableSource.getOid()),
					hostname, e);

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

		final SourceTable leftTable = sources.get(tableJoinSource.getLeftTable());
		if (tableJoinSource.getLeftTable() == null || leftTable == null ||  leftTable.getTable() == null) {
			log.debug("LeftTable cannot be null, the Join {} will return an empty result.", tableJoinSource);
			return SourceTable.empty();
		}

		final SourceTable rightTable = sources.get(tableJoinSource.getRightTable());
		if (tableJoinSource.getRightTable() == null || rightTable == null || rightTable.getTable() == null) {
			log.debug("RightTable cannot be null, the Join {} will return an empty result.", tableJoinSource);
			return SourceTable.empty();
		}

		if (tableJoinSource.getLeftKeyColumn() < 1 || tableJoinSource.getRightKeyColumn() < 1) {
			log.error("Invalid key column number (leftKeyColumnNumber=" + tableJoinSource.getLeftKeyColumn()
			+ ", rightKeyColumnNumber=" + tableJoinSource.getDefaultRightLine() + ")");
			return SourceTable.empty();
		}

		logTableJoin(tableJoinSource.getKey(), tableJoinSource.getLeftTable(), tableJoinSource.getRightTable(),
				leftTable, rightTable);

		final List<List<String>> executeTableJoin = matsyaClientsExecutor.executeTableJoin(
				leftTable.getTable(),
				rightTable.getTable(),
				tableJoinSource.getLeftKeyColumn(),
				tableJoinSource.getRightKeyColumn(),
				tableJoinSource.getDefaultRightLine(),
				WBEM.equalsIgnoreCase(tableJoinSource.getKeyType()),
				true);

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
	 * Get source table based on the key
	 *
	 * @param key	The key of the source
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
	public SourceTable visit(final SshInteractiveSource sshInteractiveSource) {

		try {
			final List<String> result =
					SshInteractiveHelper.runSshInteractive(
							strategyConfig.getEngineConfiguration(),
							sshInteractiveSource.getSteps(),
							"sshInteractive " + sshInteractiveSource.getKey());

			final List<String> filteredLines = FilterResultHelper.filterLines(
					result,
					sshInteractiveSource.getRemoveHeader(),
					sshInteractiveSource.getRemoveFooter(),
					sshInteractiveSource.getExcludeRegExp(),
					sshInteractiveSource.getKeepOnlyRegExp());

			final List<String> selectedColumnsLines = FilterResultHelper.selectedColumns(
					filteredLines,
					sshInteractiveSource.getSeparators(),
					sshInteractiveSource.getSelectColumns());

			return SourceTable.builder()
					.rawData(selectedColumnsLines.stream().collect(Collectors.joining(NEW_LINE)))
					.table(selectedColumnsLines.stream()
							.map(line -> Stream.of(line.split(HardwareConstants.TABLE_SEP)).collect(Collectors.toList()))
							.collect(Collectors.toList()))
					.build();

		} catch(final Exception e) {

			logSourceError(connector.getCompiledFilename(), sshInteractiveSource.getKey(), "SSH Interactive",
					strategyConfig.getEngineConfiguration().getTarget().getHostname(), e);

			return SourceTable.empty();
		}
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
		String namespace = wbemSource.getWbemNamespace();
		if (namespace == null) {
			namespace = "root/cimv2";
		} else if (AUTOMATIC_NAMESPACE.equalsIgnoreCase(namespace)) {
			namespace = strategyConfig
					.getHostMonitoring()
					.getConnectorNamespace(connector)
					.getAutomaticWbemNamespace();
		}

		// Replace the automatic namespace
		if (AUTOMATIC_NAMESPACE.equalsIgnoreCase(namespace)) {
			final String cachedNamespace = strategyConfig
					.getHostMonitoring()
					.getConnectorNamespace(connector)
					.getAutomaticWbemNamespace();

			// Update the namespace with the cached namespace
			namespace = cachedNamespace;
		}

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

			logSourceError(connector.getCompiledFilename(), wbemSource.getKey(),
					String.format("wbemQuery=%s, username=%s, timeout=%d, namespace=%s",
							wbemSource.getWbemQuery(), protocol.getUsername(), protocol.getTimeout(),
							namespace),
					hostname, e);

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
		final String namespace = getNamespace(wmiSource);

		if (namespace == null) {
			log.error("Failed to retrieve the WMI namespace to run the WMI source {}. Returning an empty table.", wmiSource.getKey());
			return SourceTable.empty();
		}

		final String hostname = strategyConfig.getEngineConfiguration().getTarget().getHostname();

		try {

			final List<List<String>> table =
					matsyaClientsExecutor.executeWmi(hostname, protocol, wmiSource.getWbemQuery(), namespace);

			return SourceTable
					.builder()
					.table(table)
					.build();


		} catch (Exception e) {

			logSourceError(connector.getCompiledFilename(), wmiSource.getKey(),
					String.format("wmiQuery=%s, username=%s, timeout=%d, namespace=%s",
							wmiSource.getWbemQuery(), protocol.getUsername(), protocol.getTimeout(),
							namespace),
					hostname, e);

			return SourceTable.empty();
		}

	}

	/**
	 * Get the namespace to use for the execution of the given {@link WMISource} instance
	 *
	 * @param wmiSource {@link WMISource} instance from which we want to extract the namespace. Expected "automatic", null or <em>any
	 *                  string</em>
	 * @return {@link String} value
	 */
	String getNamespace(final WMISource wmiSource) {

		final String sourceNamespace = wmiSource.getWbemNamespace();

		if (sourceNamespace == null) {
			return "root\\cimv2";
		}

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
	 *
	 * @param hostname		The host against the query will be run.
	 * @param wmiProtocol	The information used to connect to the host and perform the query.
	 * @param wmiQuery		The query that will be executed.
	 * @param namespace		The namespace in which the query will be executed.
	 * @param sourceKey		The key of the source.
	 *
	 * @return				The result of the execution of the query.
	 */
	private List<List<String>> executeIpmiWmiRequest(final String hostname, final WMIProtocol wmiProtocol,
			final String wmiQuery, final String namespace, final String sourceKey) {

		log.info("Executing IPMI Query for [{}] on {}:\nWMI Query: {}:\n", sourceKey, hostname, wmiQuery);

		List<List<String>> result;

		try {

			result = matsyaClientsExecutor.executeWmi(
				hostname,
				wmiProtocol,
				wmiQuery,
				namespace
			);
		} catch (Exception e) {

			logSourceError(connector.getCompiledFilename(),
					sourceKey,
					String.format("IPMI wmiQuery=%s, hostname=%s, username=%s, timeout=%d, namespace=%s",
							wmiQuery, hostname, wmiProtocol.getUsername(), wmiProtocol.getTimeout(),
							namespace),
					hostname,
					e
			);

			result = Collections.emptyList();
		}

		log.info("IPMI query for [{}] result:\n{}\n", sourceKey, TextTableHelper.generateTextTable(result));

		return result;
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
	private static void logTableJoin(final String sourceKey, final String leftSourceKey, final String rightSourceKey,
			final SourceTable leftTable, final SourceTable rightTable) {

		if (!log.isDebugEnabled()) {
			return;
		}

		log.debug("Table Join Source [{}]:\nLeft table [{}]:\n{}\nRight table [{}]:\n{}\n",
				sourceKey,
				leftSourceKey,
				TextTableHelper.generateTextTable(leftTable.getHeaders(), leftTable.getTable()),
				rightSourceKey,
				TextTableHelper.generateTextTable(rightTable.getHeaders(), rightTable.getTable()));

	}

	/**
	 * Log the source reference data 
	 * 
	 * @param connectorName   the name of the connector defining the source
	 * @param childSourceKey  the source key referencing the parent source
	 * @param parentSourceKey the parent source key referenced in the source reference
	 * @param sourceTable     the source's result we wish to log
	 */
	private static void logSourceReference(final String connectorName, final String parentSourceKey,
			final String childSourceKey, final SourceTable sourceTable) {

		if (!log.isDebugEnabled()) {
			return;
		}

		// Is there any raw data to log?
		if (sourceTable.getRawData() != null && (sourceTable.getTable() == null || sourceTable.getTable().isEmpty())) {
			log.debug("Got Source [{}] referenced in Source [{}]. Connector: [{}].\nRaw result:\n{}\n",
					parentSourceKey,
					childSourceKey,
					connectorName,
					sourceTable.getRawData());
			return;
		}

		if (sourceTable.getRawData() == null) {
			log.debug("Got Source [{}] referenced in Source [{}]. Connector: [{}].\nTable result:\n{}\n",
					parentSourceKey,
					childSourceKey,
					connectorName,
					TextTableHelper.generateTextTable(sourceTable.getHeaders(), sourceTable.getTable()));
			return;
		}

		log.debug("Got Source [{}] referenced in Source [{}]. Connector: [{}].\nRaw result:\n{}\nTable result:\n{}\n",
				parentSourceKey,
				childSourceKey,
				connectorName,
				sourceTable.getRawData(),
				TextTableHelper.generateTextTable(sourceTable.getHeaders(), sourceTable.getTable()));

	}

	/**
	 * Log the given throwable
	 * 
	 * @param connectorName  The name of the connector
	 * @param sourceKey      The key of the source
	 * @param hostname       The target's hostname
	 * @param context        Additional information about the operation
	 * @param throwable      The catched throwable to log
	 */
	private static void logSourceError(final String connectorName, final String sourceKey, final String context,
			final String hostname, final Throwable throwable) {

		if (log.isErrorEnabled()) {
			log.error(
					"Source [{}] on {} was unsuccessful due to an exception."
					+ " Context [{}]. Connector: [{}]. Returning an empty table. Errors:\n{}\n",
					sourceKey, hostname, context, connectorName, StringHelper.getStackMessages(throwable));
		}

		if (log.isDebugEnabled()) {
			log.debug(String.format(
					"Source [%s] on %s was unsuccessful due to an exception. Context [%s]. Connector: [%s]. Returning an empty table. Stack trace:",
					sourceKey, hostname, context, connectorName), throwable);
		}
	}
}
