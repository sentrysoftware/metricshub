package com.sentrysoftware.matrix.engine.strategy.source;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
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
import com.sentrysoftware.matrix.engine.target.HardwareTarget;
import com.sentrysoftware.matrix.engine.target.TargetType;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SourceVisitor implements ISourceVisitor {

	private static final String EXCEPTION = "Exception";

	private static final String WBEM = "wbem";

	private static final Pattern SOURCE_PATTERN = Pattern.compile(
			"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\)(.*))\\s*$",
			Pattern.CASE_INSENSITIVE);

	@Autowired
	private StrategyConfig strategyConfig;

	@Autowired
	private MatsyaClientsExecutor matsyaClientsExecutor;

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
					.method(httpSource.getMethod())
					.url(httpSource.getUrl())
					.header(httpSource.getHeader())
					.body(httpSource.getBody())
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
			return processUnixIpmiSource(targetType);
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
		SourceTable processUnixIpmiSource(TargetType targetType) {
		final String hostname = strategyConfig.getEngineConfiguration().getTarget().getHostname();

		// get the ipmiTool command to execute
		String ipmitoolCommand = strategyConfig.getHostMonitoring().getIpmitoolCommand();
		if (ipmitoolCommand == null || ipmitoolCommand.isEmpty()) {
			final String message = String.format("IPMI Tool Command cannot be found for %s. Retrun empty result.",
					hostname);
			log.error(message);
			return SourceTable.empty();
		}

		final SSHProtocol sshProtocol = (SSHProtocol) strategyConfig.getEngineConfiguration()
				.getProtocolConfigurations().get(SSHProtocol.class);
		final OSCommandConfig osCommandConfig = (OSCommandConfig) strategyConfig.getEngineConfiguration()
				.getProtocolConfigurations().get(OSCommandConfig.class);

		if (osCommandConfig == null) {
			final String message = String.format("No OS Command Configuration for %s. Retrun empty result.", hostname);
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
				fruResult = OsCommandHelper.runLocalCommand(fruCommand);
			} else {
				fruResult = OsCommandHelper.runRemoteCommand(fruCommand, hostname, sshProtocol, defaultTimeout,
						matsyaClientsExecutor);
			}
			// TODO : Log the result in debug log
//			log.ebug("processUnixIpmiSource(".host."): OS Command: ".osCommandLine.":\n".fruResult, host);

		} catch (IOException |InterruptedException  e) {
			final String message = String.format("Failed to execute the OS Command %s for %s. Retrun empty result.",
					fruCommand, hostname);
			log.error(message);
			return SourceTable.empty();
		} 

		// "-v sdr elist all"
		String sdrCommand = ipmitoolCommand + "-v sdr elist all";
		String sensorResult;
		try {
			if (isLocalHost) {
				sensorResult = OsCommandHelper.runLocalCommand(sdrCommand);
			} else {
				sensorResult = OsCommandHelper.runRemoteCommand(sdrCommand, hostname, sshProtocol, defaultTimeout,
						matsyaClientsExecutor);
			}
			// TODO : Log the result in debug log
//			log.debug("processUnixIpmiSource(".host."): OS Command: ".osCommandLine.":\n".sensorResult, host);
		} catch (IOException | InterruptedException e) {
			final String message = String.format("Failed to execute the OS Command %s for %s. Retrun empty result.",
					sdrCommand, hostname);
			log.error(message);
			return SourceTable.empty();
		}

		return SourceTable.builder().table(ipmiTranslateFromIpmitool(fruResult, sensorResult)).build();

	}

	/**
	 * Process what we got from ipmitool and return a pretty table
	 * 
	 * @param fruResult
	 * @param sdrResult
	 * @return
	 */
	private List<List<String>> ipmiTranslateFromIpmitool(String fruResult, String sdrResult) {
		List<List<String>> result = new ArrayList<>();
		sdrResult = cleanSensorCommandResult(sdrResult);
		ipmiBuildDeviceListFromIpmitool(fruResult, sdrResult);

		//  Now process the numeric sensor list
		// TODO
		// example of expectedResult
//		[
//			[FRU, FUJITSU, PRIMERGY RX300 S7, YLAR004219],
//			[FRU, FUJITSU, D2939, 39159317],
//			[FRU, FUJITSU, D2939, 39159317],
//			[Temperature, 1, Ambient, External Environment 0(External, 19, , ],
//			[Temperature, 3, Systemboard 2, System Board 0(System, 32, , ],
//			[Temperature, 4, CPU1, Processor 0(Processor), 30, , ],
//			[Temperature, 5, CPU2, Processor 1(Processor), 32, , ],
//			[Temperature, 6, MEM A, Memory Device 0(Memory, 30, , ],
//			[Temperature, a, MEM E, Memory Device 4, 30, , ]
//		]
		return result;

	}

	/**
	 * Process what we got from ipmitool and return a pretty device table
	 * @param fruResult
	 * @param sdrResult
	 * @return
	 */
	private List<List<String>> ipmiBuildDeviceListFromIpmitool(String fruResult, String sdrResult) {
		List<List<String>> result = new ArrayList<>();
		processFruResult(fruResult);

		List<String> deviceList = new ArrayList<>();
		// Parse the SDR records
		for (String sensorEntry : sdrResult.split(HardwareConstants.NEW_LINE)) {
			if (!sensorEntry.startsWith("Sensor ID ") || !sensorEntry.contains("States Asserted")) { // Bypass sensors with no state asserted
				continue;
			}

			String sensorName = null;
			String entityId = null;
			String deviceType = null;
			String statusArray = null;
			String deviceId = null;

			sensorEntry = sensorEntry.replaceAll(HardwareConstants.SEMICOLON, HardwareConstants.NEW_LINE);

			// Get name, ID, entity ID and device type
			for (String entryLine : sensorEntry.split(HardwareConstants.NEW_LINE)) {
				if (entryLine.startsWith("Sensor ID") && entryLine.contains(" : ")) {
					String[] entrySplit = entryLine.split(" : ");
					if (entrySplit.length > 0) {
						sensorName = entrySplit[1]
								.substring(0, entrySplit[1].indexOf(HardwareConstants.OPENING_PARENTHESIS)).trim();
					}
				}
				if (entryLine.startsWith(" Entity ID ") && entryLine.contains(" : ")) {
					String[] entrySplit = entryLine.split(" : ");
					if (entrySplit.length > 0) {
						entityId = entrySplit[1];
						deviceType = entityId.substring(entityId.indexOf(HardwareConstants.OPENING_PARENTHESIS) + 1,
								entityId.indexOf(HardwareConstants.CLOSING_PARENTHESIS)).trim();
						deviceId = entityId.substring(entityId.indexOf(HardwareConstants.OPENING_PARENTHESIS) + 1,
								entityId.indexOf(HardwareConstants.CLOSING_PARENTHESIS)).trim();

					}
				}
			}
			if (entityId == null) {
				// TODO : complete the debug log
				log.debug("Cannot retreive entity Id");
				continue;
			}

			// check if OEM Specific
			if (sensorEntry.matches("States Asserted +: 0x[0-9a-zA-Z]+ +OEM Specific")) {
				// TODO : PSL Code ==>
//				oemSpecific = ntharg(oemSpecific, 4, " \t");
//				oemSpecific = substr(oemSpecific, 3, length(oemSpecific) - 2);
//				oemSpecific = int(convert_base(oemSpecific, 16, 10) | 32768);
//				oemSpecific = convert_base(oemSpecific, 10, 16);
//				if (length(oemSpecific) < 4) { oemSpecific = substr("0000", 1, int(4-length(oemSpecific))).oemSpecific; }
//				statusArray = sensorName."=0x".oemSpecific;
			} else {
				if (!sensorEntry.contains("Assertions Enabled ")) {
					continue;
				}
				sensorEntry = sensorEntry.substring(0, sensorEntry.indexOf("Assertions Enabled "));
				// get the values between the first and the last [, ]
				statusArray = sensorEntry.substring(
						sensorEntry.indexOf(HardwareConstants.OPENING_SQUARE_BRACKET) + 1, 
						sensorEntry.lastIndexOf(HardwareConstants.CLOSING_SQUARE_BRACKET))
						.trim();
				statusArray = statusArray.replace(HardwareConstants.OPENING_SQUARE_BRACKET, "")
						.replace(HardwareConstants.CLOSING_SQUARE_BRACKET, "").trim();
				statusArray = sensorName + "=" + statusArray.replaceAll(HardwareConstants.NEW_LINE,
						HardwareConstants.NEW_LINE + sensorName + HardwareConstants.EQUAL);
				statusArray = statusArray.replaceAll(HardwareConstants.NEW_LINE, HardwareConstants.PIPE);
			}
			if (statusArray == null || statusArray.isEmpty() || statusArray.equals("|")) {
				continue;
			}
			if (!deviceList.contains(";" + deviceType + " " + deviceId + ";")) {
				// It's the first time we meet this entityID, look up its FRU entry
				if (sdrResult.matches(";Entity ID +: " + entityId.replace(".", "\\.") + " .*;Logical FRU Device ")) {
					// I am totally lost here .....
				}
			} else {
				// If this entityID was already present in the list, we just need to add the
				// statusArray to it
			}

		} // end of sensorEntry

		return result;
	}

	/**
	 * Process the raw result of the FRU command and return the list of good FRU list and poor FRU list
	 * @param fruResult
	 * @return
	 */
	public Map<String, List<String>> processFruResult(String fruResult) {
		List<String> goodFruList = new ArrayList<>();
		List<String> poorFruList = new ArrayList<>();
		List<String> fruList = new ArrayList<>();
		Map<String, List<String>> ipmiTable = new HashMap<>();

		// extract each FRU bloc, which are separated by an empty line
		for (String fruEntry : fruResult.split("\n\n")) {
			String fruID = null;
			String fruVendor = null;
			String fruModel = null;
			String fruSN = null;
			String boardVendor = null;
			String boardModel = null;
			String boardSN = null;
			boolean board = false;

			for (String fruLine : fruEntry.split(HardwareConstants.NEW_LINE)) {
				if (fruLine.startsWith("FRU Device Description ")) {
					fruID = fruLine.substring(fruLine.indexOf(HardwareConstants.OPENING_PARENTHESIS) + 3,
							fruLine.indexOf(HardwareConstants.CLOSING_PARENTHESIS)).trim();
				}
				if (fruLine.startsWith(" Product Manufacturer") && fruLine.contains(" : ")) {
					fruVendor = fruLine.split(" : ")[1].trim();
				}
				if (fruLine.startsWith(" Product Name") && fruLine.contains(" : ")) {
					fruModel = fruLine.split(" : ")[1].trim();
				}
				if (fruLine.startsWith(" Product Serial") && fruLine.contains(" : ")) {
					fruSN = fruLine.split(" : ")[1].trim();
				}
				if (fruLine.startsWith(" Board Mfg") && fruLine.contains(" : ")) {
					boardVendor = fruLine.split(" : ")[1].trim();
					board = true;
				}
				if (fruLine.startsWith(" Board Product") && fruLine.contains(" : ")) {
					boardModel = fruLine.split(" : ")[1].trim();
					board = true;
				}
				if (fruLine.startsWith(" Board Serial") && fruLine.contains(" : ")) {
					boardSN = fruLine.split(" : ")[1].trim();
					board = true;
				}
			}

			StringBuilder fruEntryResult = new StringBuilder();
			fruEntryResult = fruEntryResult.append("FRU;").append(fruVendor).append(HardwareConstants.SEMICOLON)
					.append(fruModel).append(HardwareConstants.SEMICOLON).append(fruSN);
			if (!board) {
				if (fruModel != null && fruSN != null) {
					goodFruList.add(fruEntryResult.toString());
				} else if (fruVendor != null) {
					poorFruList.add(fruEntryResult.toString());
				}
			} else if (boardVendor != null) {
				fruEntryResult = new StringBuilder();
				fruEntryResult = fruEntryResult.append("FRU;").append(boardVendor).append(HardwareConstants.SEMICOLON)
						.append(boardModel).append(HardwareConstants.SEMICOLON).append(boardSN);
				poorFruList.add(fruEntryResult.toString());
			}

			fruList.add(fruID + HardwareConstants.SEMICOLON + fruEntryResult.toString());
		}
		ipmiTable.put("goodList", goodFruList);
		ipmiTable.put("poorList", poorFruList);
		return ipmiTable;
	}

	/**
	 * Reformat the ipmitoolSdr list so we have one line per sdr entry Remove lines
	 * that starts with BMC req and --
	 * 
	 * @param sdrResult
	 * @return
	 */
	public String cleanSensorCommandResult(String sdrResult) {
		if(sdrResult == null || sdrResult.isEmpty()) {
			return sdrResult;
		}

		// exclude rows that start with "^BMC req" and "-- "
		// in order to differentiate blocs of sensorID and the empty lines that will be
		// created by the replace operation
		sdrResult = Pattern.compile("(?m)^(BMC req|--).*").matcher(sdrResult).replaceAll("");
		sdrResult = sdrResult.replaceAll(HardwareConstants.SEMICOLON, HardwareConstants.COMMA);
		sdrResult = sdrResult.replaceAll(HardwareConstants.NEW_LINE, HardwareConstants.SEMICOLON);
		sdrResult = sdrResult.replace(";;", HardwareConstants.NEW_LINE);

		return sdrResult;
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
		List<List<String>> wmiCollection1 = executeWmiRequest(hostname, wmiProtocol, wmiQuery, nameSpaceRootCimv2);

		wmiQuery = "SELECT BaseUnits,CurrentReading,Description,LowerThresholdCritical,LowerThresholdNonCritical,SensorType,UnitModifier,UpperThresholdCritical,UpperThresholdNonCritical FROM NumericSensor";
		List<List<String>> wmiCollection2 = executeWmiRequest(hostname, wmiProtocol, wmiQuery, nameSpaceRootHardware);

		wmiQuery = "SELECT CurrentState,Description FROM Sensor";
		List<List<String>> wmiCollection3 = executeWmiRequest(hostname, wmiProtocol, wmiQuery, nameSpaceRootHardware);

		return SourceTable.builder().table(IpmiHelper.ipmiTranslateFromWmi(wmiCollection1, wmiCollection2, wmiCollection3)).build();
	}

	@Override
	public SourceTable visit(final OSCommandSource osCommandSource) {
		return SourceTable.empty();
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

		final Map<String, SourceTable> sources = strategyConfig.getHostMonitoring().getSourceTables();
		if (sources == null ) {
			log.error("SourceTable Map cannot be null, the Table Join {} will return an empty result.", tableJoinSource);
			return SourceTable.empty();
		}

		if (tableJoinSource.getLeftTable() == null || sources.get(tableJoinSource.getLeftTable()) == null ||  sources.get(tableJoinSource.getLeftTable()).getTable() == null) {
			log.error("LeftTable cannot be null, the Join {} will return an empty result.", tableJoinSource);
			return SourceTable.empty();
		}

		if (tableJoinSource.getRightTable() == null || sources.get(tableJoinSource.getRightTable()) == null || sources.get(tableJoinSource.getRightTable()).getTable() == null) {
			log.error("RightTable cannot be null, the Join {} will return an empty result.", tableJoinSource);
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
			log.warn("Table list in the Union cannot be null, the Union operation {} will return an empty result.",
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
			final SourceTable sourceTable = strategyConfig.getHostMonitoring().getSourceTableByKey(key);
			if (sourceTable == null) {
				log.warn("The following source table {} cannot be found.", key);
			}
			return sourceTable;
		}

		return SourceTable.builder()
				.table(SourceTable.csvToTable(key, HardwareConstants.SEMICOLON))
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

			// if protocol = null than we use the default one : https
			String transferProtocol = protocol.getProtocol() == null ? WBEMProtocol.WBEMProtocols.HTTPS.name().toLowerCase()
					: protocol.getProtocol().name().toLowerCase();
			final String wbemUrl = String.format("%s://%s:%d", transferProtocol, hostname, protocol.getPort());
			if (wbemUrl == null) {
				log.error(
						"Cannot build URL with the following information : hostname :{}, port : {}. Returning an empty table.",
						hostname, protocol.getPort());
				return SourceTable.empty();
			}

			int timeout = protocol.getTimeout() == null ? 120000 : protocol.getTimeout().intValue() * 1000; // seconds to milliseconds
			final List<List<String>> table = matsyaClientsExecutor.executeWbem(wbemUrl, protocol.getUsername(),
					protocol.getPassword(), timeout, wbemSource.getWbemQuery(), namespace);

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

			final List<List<String>> table = matsyaClientsExecutor.executeWmi(hostname,
					protocol.getUsername(),
					protocol.getPassword(),
					protocol.getTimeout(),
					wmiSource.getWbemQuery(),
					namespace);

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

		if ("automatic".equalsIgnoreCase(sourceNamespace)) {
			// The namespace should be detected correctly in the detection strategy phase
			return strategyConfig.getHostMonitoring().getAutomaticWmiNamespace();
		} else {
			return sourceNamespace != null ? sourceNamespace : protocol.getNamespace();
		}

	}

	/**
	 * Call the matsya client executor to execute a WMI request. 
	 * @param hostname
	 * @param wmiProtocol
	 * @param wmiQuery
	 * @param namespace
	 * @return
	 */
	private List<List<String>> executeWmiRequest(final String hostname, final WMIProtocol wmiProtocol,
			final String wmiQuery, final String namespace) {
		List<List<String>> wmiCollection1 = new ArrayList<>();
		try {
			wmiCollection1 = matsyaClientsExecutor.executeWmi(
					hostname,
					wmiProtocol.getUsername(),
					wmiProtocol.getPassword(),
					wmiProtocol.getTimeout(),
					wmiQuery,
					namespace);
			log.debug("Executed IPMI Query ({}) : WMI Query: {}:\n",
					hostname,
					wmiQuery);
		} catch (Exception e) {
			log.error("Error detected when running IPMI Query: {}. hostname={}, username={}, timeout={}, namespace={}",
					wmiQuery,
					hostname,
					wmiProtocol.getUsername(),
					wmiProtocol.getTimeout(),
					namespace);
		}
		return wmiCollection1;
	}
}
