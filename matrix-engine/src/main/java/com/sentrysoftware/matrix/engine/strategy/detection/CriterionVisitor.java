package com.sentrysoftware.matrix.engine.strategy.detection;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.AUTOMATIC_NAMESPACE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.EMPTY;
import static org.springframework.util.Assert.notNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sentrysoftware.javax.wbem.WBEMException;
import com.sentrysoftware.matrix.common.exception.LocalhostCheckException;
import com.sentrysoftware.matrix.common.helpers.LocalOSEnum;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.OSType;
import com.sentrysoftware.matrix.connector.model.detection.criteria.Criterion;
import com.sentrysoftware.matrix.connector.model.detection.criteria.http.HTTP;
import com.sentrysoftware.matrix.connector.model.detection.criteria.ipmi.IPMI;
import com.sentrysoftware.matrix.connector.model.detection.criteria.kmversion.KMVersion;
import com.sentrysoftware.matrix.connector.model.detection.criteria.os.OS;
import com.sentrysoftware.matrix.connector.model.detection.criteria.oscommand.OSCommand;
import com.sentrysoftware.matrix.connector.model.detection.criteria.process.Process;
import com.sentrysoftware.matrix.connector.model.detection.criteria.service.Service;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMPGet;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMPGetNext;
import com.sentrysoftware.matrix.connector.model.detection.criteria.telnet.TelnetInteractive;
import com.sentrysoftware.matrix.connector.model.detection.criteria.ucs.UCS;
import com.sentrysoftware.matrix.connector.model.detection.criteria.wbem.WBEM;
import com.sentrysoftware.matrix.connector.model.detection.criteria.wmi.WMI;
import com.sentrysoftware.matrix.engine.EngineConfiguration;
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
import com.sentrysoftware.matrix.engine.strategy.source.SourceTable;
import com.sentrysoftware.matrix.engine.strategy.utils.OsCommandHelper;
import com.sentrysoftware.matrix.engine.strategy.utils.PslUtils;
import com.sentrysoftware.matrix.engine.target.HardwareTarget;
import com.sentrysoftware.matrix.engine.target.TargetType;
import com.sentrysoftware.matsya.exceptions.WqlQuerySyntaxException;
import com.sentrysoftware.matsya.wmi.exceptions.WmiComException;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CriterionVisitor implements ICriterionVisitor {

	private static final String IPMI_VERSION = "IPMI Version";
	private static final String SOLARIS_VERSION_COMMAND = "/usr/bin/uname -r";
	private static final String IPMI_TOOL_SUDO_COMMAND = "PATH=$PATH:/usr/local/bin:/usr/sfw/bin;export PATH;%{SUDO:ipmitool}ipmitool -I ";
	private static final String IPMI_TOOL_SUDO_MACRO = "%{SUDO:ipmitool}";

	private static final String IPMI_TOOL_COMMAND = "PATH=$PATH:/usr/local/bin:/usr/sfw/bin;export PATH;ipmitool -I ";
	private static final String COLUMN_SEPARATOR = ";";

	private static final String NAMESPACE_MESSAGE = "\n- Namespace: ";
	private static final String DEFAULT_NAMESPACE = "root/cimv2";
	private static final String DEFAULT_NAMESPACE_WMI = "root\\cimv2";
	private static final String INTEROP_NAMESPACE = "interop";

	private static final Pattern SNMP_GETNEXT_RESULT_REGEX = Pattern.compile("\\w+\\s+\\w+\\s+(.*)");
	private static final String EXPECTED_VALUE_RETURNED_VALUE = "Expected value: %s - returned value %s.";

	private static final Set<String> IGNORED_WMI_NAMESPACES = Set
			.of(
					"SECURITY",
					"RSOP",
					"Cli",
					"aspnet",
					"SecurityCenter",
					"WMI",
					"Policy",
					"DEFAULT",
					"directory",
					"subscription",
					"vm",
					"root\\SECURITY",
					"root\\RSOP",
					"root\\Cli",
					"root\\aspnet",
					"root\\SecurityCenter",
					"root\\WMI",
					"root\\wmi",
					"root\\Policy",
					"root\\DEFAULT",
					"root\\directory",
					"root\\subscription",
					"root\\vm",
					"root\\perfmon",
					"root\\MSCluster",
					"root\\MicrosoftActiveDirectory",
					"root\\MicrosoftNLB",
					"root\\Microsoft",
					"root\\ServiceModel",
					"root\\nap");

	private static final Set<String> WBEM_INTEROPERABILITY_NAMESPACES = Set
			.of(
					"Interop",
					"PG_Interop",
					"root/Interop",
					"root/PG_Interop",
					INTEROP_NAMESPACE
					);

	private static final Set<String> IGNORED_WBEM_NAMESPACES = Set.of("root", "/root");

	@Autowired
	private StrategyConfig strategyConfig;

	@Autowired
	private MatsyaClientsExecutor matsyaClientsExecutor;

	@Override
	public CriterionTestResult visit(final HTTP criterion) {

		if (criterion == null) {
			return CriterionTestResult.empty();
		}

		final EngineConfiguration engineConfiguration = strategyConfig.getEngineConfiguration();

		final HTTPProtocol protocol = (HTTPProtocol) engineConfiguration
				.getProtocolConfigurations()
				.get(HTTPProtocol.class);

		if (protocol == null) {
			log.debug("The HTTP Credentials are not configured. Cannot process HTTP detection {}.",
					criterion);
			return CriterionTestResult.empty();
		}

		final String hostname = engineConfiguration
				.getTarget()
				.getHostname();

		final String result = matsyaClientsExecutor.executeHttp(HTTPRequest.builder()
				.method(criterion.getMethod())
				.url(criterion.getUrl())
				.header(criterion.getHeader())
				.body(criterion.getBody())
				.build(),
				false);

		final TestResult testResult = checkHttpResult(hostname, result, criterion.getExpectedResult());

		return CriterionTestResult
				.builder()
				.result(result)
				.success(testResult.isSuccess())
				.message(testResult.getMessage())
				.build();
	}

	/**
	 * @param hostname			The hostname against which the HTTP test has been carried out.
	 * @param result			The actual result of the HTTP test.
	 *
	 * @param expectedResult	The expected result of the HTTP test.
	 * @return					A {@link TestResult} summarizing the outcome of the HTTP test.
	 */
	private TestResult checkHttpResult(final String hostname, final String result, final String expectedResult) {

		String message;
		boolean success = false;

		if (expectedResult == null) {

			if (result == null || result.isEmpty()) {

				message = String.format("HTTP Test Failed - the HTTP Test on %s did not return any result.", hostname);

			} else {

				message = String.format("Successful HTTP Test on %s. Returned Result: %s.", hostname, result);
				success = true;
			}

		} else {

			final Pattern pattern = Pattern.compile(PslUtils.psl2JavaRegex(expectedResult));
			if (result != null && pattern.matcher(result).find()) {

				message = String.format("Successful HTTP Test on %s. Returned Result: %s.", hostname, result);
				success = true;

			} else {

				message = String
						.format("HTTP Test Failed - "
								+"the returned result (%s) of the HTTP Test on %s did not match the expected result (%s).",
								result, hostname, expectedResult);
				message += String.format(EXPECTED_VALUE_RETURNED_VALUE, expectedResult, result);
			}
		}

		log.debug(message);

		return TestResult
				.builder()
				.message(message)
				.success(success)
				.build();
	}

	@Override
	public CriterionTestResult visit(final IPMI ipmi) {

		final HardwareTarget target = strategyConfig.getEngineConfiguration().getTarget();
		final TargetType targetType = target.getType();

		if (TargetType.MS_WINDOWS.equals(targetType)) {
			return processWindowsIpmiDetection();
		} else if (TargetType.LINUX.equals(targetType) || TargetType.SUN_SOLARIS.equals(targetType)) {
			return processUnixIpmiDetection(targetType);
		} else if (TargetType.MGMT_CARD_BLADE_ESXI.equals(targetType)) {
			return processOutOfBandIpmiDetection();
		}

		final String message = String.format("Failed to perform IPMI detection on system: %s. %s is an unsupported OS for IPMI.", target.getHostname(),
				targetType.name());

		return CriterionTestResult.builder()
				.message(message)
				.success(false)
				.build();
	}

	/**
	 * Process IPMI detection for the Out Of Band device
	 *
	 * @return {@link CriterionTestResult} wrapping the status of the criterion execution
	 */
	private CriterionTestResult processOutOfBandIpmiDetection() {

		final IPMIOverLanProtocol protocol = (IPMIOverLanProtocol) strategyConfig.getEngineConfiguration()
				.getProtocolConfigurations().get(IPMIOverLanProtocol.class);

		if (protocol == null) {
			log.debug("The IPMI Credentials are not configured. Cannot process IPMI-over-LAN detection.");
			return CriterionTestResult.empty();
		}

		final String hostname = strategyConfig.getEngineConfiguration().getTarget().getHostname();

		try {
			final String result = matsyaClientsExecutor.executeIpmiDetection(hostname, protocol);
			if (result == null) {
				return CriterionTestResult
						.builder()
						.message("Received <null> result after connecting to the IPMI BMC chip with the IPMI-over-LAN interface.")
						.build();
			}

			return CriterionTestResult
					.builder()
					.result(result)
					.message("Successfully connected to the IPMI BMC chip with the IPMI-over-LAN interface.")
					.success(true)
					.build();

		} catch (final Exception e) {
			final String message = String.format("Cannot execute IPMI-over-LAN command to get the chassis status on %s. Exception: %s",
					hostname, e.getMessage());
			log.debug(message, e);
			return CriterionTestResult
					.builder()
					.message(message)
					.build();
		}
	}

	/**
	 * Process IPMI detection for the Unix system
	 *
	 * @param targetType
	 *
	 * @return
	 */
	private CriterionTestResult processUnixIpmiDetection(final TargetType targetType) {

		String ipmitoolCommand = strategyConfig.getHostMonitoring().getIpmitoolCommand();
		final String hostname = strategyConfig.getEngineConfiguration().getTarget().getHostname();
		final SSHProtocol sshProtocol = (SSHProtocol) strategyConfig.getEngineConfiguration()
				.getProtocolConfigurations().get(SSHProtocol.class);
		final OSCommandConfig osCommandConfig = (OSCommandConfig) strategyConfig.getEngineConfiguration()
				.getProtocolConfigurations().get(OSCommandConfig.class);

		if (osCommandConfig == null) {
			final String message = String.format("No OS Command Configuration for %s. Retrun empty result.",
					hostname);
			log.error(message);
			return CriterionTestResult.builder().success(false).result("").message(message).build();
		}
		final int defaultTimeout = osCommandConfig.getTimeout().intValue();
		if (ipmitoolCommand == null || ipmitoolCommand.isEmpty()) {
			ipmitoolCommand = buildIpmiCommand(targetType, hostname, sshProtocol, osCommandConfig, defaultTimeout);
		}

		// buildIpmiCommand method can either return the actual result of the built command or an error. If it an error we display it in the error message
		if (!ipmitoolCommand.startsWith("PATH=")) {
			return CriterionTestResult.builder().success(false).result("").message(ipmitoolCommand).build();
		}
		// execute the command
		try {
			String result = null;
			result = runOsCommand(ipmitoolCommand, hostname, sshProtocol, defaultTimeout);
			if (result != null && !result.contains(IPMI_VERSION)) {
				// Didn't find what we expected: exit
				return CriterionTestResult.builder().success(false).result(result)
						.message("Didn't get the expected result from ipmitool: " + ipmitoolCommand).build();
			} else {
				// everything goes well
				strategyConfig.getHostMonitoring()
				.setIpmiExecutionCount(strategyConfig.getHostMonitoring().getIpmiExecutionCount() + 1);
				return CriterionTestResult.builder().success(true).result(result)
						.message("Successfully connected to the IPMI BMC chip with the in-band driver interface.")
						.build();
			}

		} catch (final Exception e) {
			final String message = String.format("Cannot execute IPMI Tool Command %s on %s. Exception: %s",
					ipmitoolCommand, hostname, e.getMessage());
			log.debug(message, e);
			return CriterionTestResult.builder().success(false).message(message).build();
		}

	}

	/**
	 * Check the OS type and version and build the correct IPMI command. If the
	 * process fails, return the according error
	 *
	 * @param targetType
	 * @param hostname
	 * @param sshProtocol
	 * @param osCommandConfig
	 * @param defaultTimeout
	 * @return
	 */
	public String buildIpmiCommand(final TargetType targetType, final String hostname, final SSHProtocol sshProtocol,
			final OSCommandConfig osCommandConfig, final int defaultTimeout) {
		// do we need to use sudo or not?
		// If we have enabled useSudo (possible only in Web UI and CMA) --> yes
		// Or if the command is listed in useSudoCommandList (possible only in classic
		// wizard) --> yes
		String ipmitoolCommand; // Sonar don't agree with modifying arguments
		if (osCommandConfig.isUseSudo() || osCommandConfig.getUseSudoCommandList().contains("ipmitool")) {
			ipmitoolCommand = IPMI_TOOL_SUDO_COMMAND.replace(IPMI_TOOL_SUDO_MACRO, osCommandConfig.getSudoCommand());
		} else {
			ipmitoolCommand = IPMI_TOOL_COMMAND;
		}

		// figure out the version of the Solaris OS
		if (TargetType.SUN_SOLARIS.equals(targetType)) {
			String solarisOsVersion = null;
			try {
				// Execute "/usr/bin/uname -r" command in order to obtain the OS Version
				// (Solaris)
				solarisOsVersion = runOsCommand(SOLARIS_VERSION_COMMAND, hostname, sshProtocol, defaultTimeout);
			} catch (final Exception e) {
				final String message = String.format("Couldn't identify Solaris version %s on %s. Exception: %s",
						ipmitoolCommand, hostname, e.getMessage());
				log.debug(message, e);
				return message;
			}
			// Get IPMI command
			if (solarisOsVersion != null) {
				try {
					ipmitoolCommand = getIpmiCommandForSolaris(ipmitoolCommand, hostname, solarisOsVersion);
				} catch (final IpmiCommandForSolarisException e) {
					final String message = String.format("Couldn't identify Solaris version %s on %s. Exception: %s",
							ipmitoolCommand, hostname, e.getMessage());
					log.debug(message, e);
					return message;
				}
			}
		} else {
			// If not Solaris, then we're on Linux
			// On Linux, the IPMI interface driver is always 'open'
			ipmitoolCommand = ipmitoolCommand + "open";
		}
		strategyConfig.getHostMonitoring().setIpmitoolCommand(ipmitoolCommand);

		// At the very end of the command line, the actual IPMI command
		ipmitoolCommand = ipmitoolCommand + " bmc info";
		return ipmitoolCommand;
	}

	/**
	 * Run SSH command. Check if we can execute on localhost or remote
	 *
	 * @param ipmitoolCommand
	 * @param hostname
	 * @param sshProtocol
	 * @param timeout
	 * @return
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public String runOsCommand(final String ipmitoolCommand, final String hostname, final SSHProtocol sshProtocol,
			final int timeout) throws InterruptedException, IOException {
		String result;
		if (strategyConfig.getHostMonitoring().isLocalhost()) { // or we can use NetworkHelper.isLocalhost(hostname)
			result = OsCommandHelper.runLocalCommand(ipmitoolCommand);
		} else {
			if (sshProtocol == null) {
				return null;
			}
			final String keyFilePath = sshProtocol.getPrivateKey() == null ? null
					: sshProtocol.getPrivateKey().getAbsolutePath();
			result = matsyaClientsExecutor.runRemoteSshCommand(hostname, sshProtocol.getUsername(),
					Arrays.toString(sshProtocol.getPassword()), keyFilePath, ipmitoolCommand, timeout);
		}
		return result;
	}

	/**
	 * Get IPMI command based on solaris version if version == 9 than use lipmi if
	 * version > 9 than use bmc else return error
	 *
	 * @param ipmitoolCommand
	 * @param hostname
	 * @param solarisOsVersion
	 * @return
	 * @throws IpmiCommandForSolarisException
	 */
	public String getIpmiCommandForSolaris(String ipmitoolCommand, final String hostname, final String solarisOsVersion)
			throws IpmiCommandForSolarisException {
		final String[] split = solarisOsVersion.split("\\.");
		if (split.length < 2) {
			throw new IpmiCommandForSolarisException(String.format(
					"Unkown Solaris version (%s) for host: %s IPMI cannot be executed, return empty result.",
					solarisOsVersion, hostname));
		}

		final String solarisVersion = split[1];
		try {
			final int versionInt = Integer.parseInt(solarisVersion);
			if (versionInt == 9) {
				// On Solaris 9, the IPMI interface drive is 'lipmi'
				ipmitoolCommand = ipmitoolCommand + "lipmi";
			} else if (versionInt < 9) {

				throw new IpmiCommandForSolarisException(String.format(
						"Solaris version (%s) is too old for the host: %s IPMI cannot be executed, return empty result.",
						solarisOsVersion, hostname));

			} else {
				// On more modern versions of Solaris, the IPMI interface driver is 'bmc'
				ipmitoolCommand = ipmitoolCommand + "bmc";
			}
		} catch (final NumberFormatException e) {
			throw new IpmiCommandForSolarisException("Couldn't identify Solaris version as a valid one.\nThe 'uname -r' command returned: "
					+ solarisOsVersion);
		}

		return ipmitoolCommand;
	}

	/**
	 * Process IPMI detection for the Windows (NT) system
	 *
	 * @return
	 */
	private CriterionTestResult processWindowsIpmiDetection() {
		final String hostname = strategyConfig.getEngineConfiguration().getTarget().getHostname();

		final WMIProtocol wmiProtocol = (WMIProtocol) strategyConfig.getEngineConfiguration().getProtocolConfigurations().get(WMIProtocol.class);

		if (wmiProtocol == null) {
			return CriterionTestResult.builder()
					.message("No WMI credentials provided.")
					.success(false)
					.build();
		}

		String csvTable;
		final String query = "SELECT Description FROM ComputerSystem";
		try {
			csvTable = runWmiQueryAndGetCsv(hostname, query, "root/hardware", wmiProtocol);
		} catch (final Exception e) {
			final String message = String.format(
					"Ipmi Test Failed - WMI request was unsuccessful due to an exception. Message: %s.",
					e.getMessage());
			log.debug(message, e);
			return CriterionTestResult.builder().message(message).build();
		}

		if (csvTable == null || csvTable.isEmpty()) {
			return CriterionTestResult.builder()
					.message("The Microsoft IPMI WMI provider did not report the presence of any BMC controller.")
					.success(false)
					.build();
		}

		// Test the result
		final TestResult testResult = getMatchingResult(query, "root/hardware", EMPTY, csvTable, WMI.class);

		return CriterionTestResult.builder()
				.success(testResult.isSuccess())
				.message(testResult.getMessage())
				.result(csvTable)
				.build();
	}

	@Override
	public CriterionTestResult visit(final KMVersion kmVersion) {
		// Not implemented yet
		return CriterionTestResult.empty();
	}

	@Override
	public CriterionTestResult visit(final OS os) {
		if (os == null) {
			log.error("Malformed os criterion {}. Cannot process OS detection.", os);
			return CriterionTestResult.empty();
		}

		final OSType osType = strategyConfig.getEngineConfiguration().getTarget().getType().getOsType();

		if (OSType.SOLARIS.equals(osType) && !isOsTypeIncluded(Arrays.asList(OSType.SOLARIS, OSType.SUNOS), os)
				|| !OSType.SOLARIS.equals(osType) && !isOsTypeIncluded(Collections.singletonList(osType), os)) {
			return CriterionTestResult
					.builder()
					.message("Failed OS detection operation")
					.result("Configured OS Type : " + osType.name())
					.success(false)
					.build();
		}

		return CriterionTestResult
				.builder()
				.message("Successful OS detection operation")
				.result("Configured OS Type : " + osType.name())
				.success(true)
				.build();
	}

	/**
	 * Return true if on of the osType in the osTypeList is included in the OS detection.
	 * @param osType
	 * @param os
	 * @return
	 */
	public boolean isOsTypeIncluded(final List<OSType> osTypeList, final OS os) {
		final Set<OSType> keepOnly = os.getKeepOnly();
		final Set<OSType> exclude = os.getExclude();

		if (keepOnly != null && osTypeList.stream().anyMatch(keepOnly::contains)) {
			return true;
		}

		if (exclude != null && osTypeList.stream().anyMatch(exclude::contains)) {
			return false;
		}

		// If no osType is in KeepOnly or Exclude, then return true if KeepOnly is null or empty.
		return keepOnly == null || keepOnly.isEmpty();
	}

	@Override
	public CriterionTestResult visit(final OSCommand osCommand) {
		// Not implemented yet
		return CriterionTestResult.empty();
	}

	@Override
	public CriterionTestResult visit(final Process process) {
		if (process == null || process.getProcessCommandLine() == null) {
			log.error("Malformed Process Criterion {}. Cannot process Process detection.", process);
			return CriterionTestResult.empty();
		}

		if (process.getProcessCommandLine().isEmpty()) {
			log.debug("Service Criterion, Process Command Line is empty.");
			return CriterionTestResult.builder()
					.success(true)
					.message("Process presence check: actually no test were performed.")
					.result(null)
					.build();
		}

		if (!strategyConfig.getHostMonitoring().isLocalhost()) {
			log.debug("Service Criterion, Not Localhost.");
			return CriterionTestResult.builder()
					.success(true)
					.message("Process presence check: no test will be performed remotely.")
					.result(null)
					.build();
		}

		final Optional<LocalOSEnum> maybeLocalOS = LocalOSEnum.getOS();
		if (maybeLocalOS.isEmpty()) {
			log.debug("Service Criterion, Unknown Local OS.");
			return CriterionTestResult.builder()
					.success(true)
					.message("Process presence check: OS unknown, no test will be performed.")
					.result(null)
					.build();
		}

		final WMIProtocol protocol = (WMIProtocol) strategyConfig.getEngineConfiguration().getProtocolConfigurations().get(WMIProtocol.class);
		final long timeout = protocol != null ? protocol.getTimeout() : EngineConfiguration.DEFAULT_JOB_TIMEOUT;

		final CriterionProcessVisitorImpl localOSVisitor = new CriterionProcessVisitorImpl(process.getProcessCommandLine(), matsyaClientsExecutor, timeout) ;
		maybeLocalOS.get().accept(localOSVisitor);
		return localOSVisitor.getCriterionTestResult();
	}

	@Override
	public CriterionTestResult visit(final Service service) {
		if (service == null  ||  service.getServiceName() == null) {
			log.error("Malformed Service Criterion {}. Cannot process service detection.", service);
			return CriterionTestResult.empty();
		}

		final WMIProtocol protocol = (WMIProtocol) strategyConfig.getEngineConfiguration().getProtocolConfigurations().get(WMIProtocol.class);
		if (protocol == null) {
			log.debug("Service Criterion, the WMI Credentials are not configured. Cannot process service detection {}.", service);
			return CriterionTestResult.empty();
		}

		if (!TargetType.MS_WINDOWS.equals(strategyConfig.getEngineConfiguration().getTarget().getType())) {
			log.debug("Service Criterion, not running under Windows. Cannot process service detection {}.", service);
			return CriterionTestResult.builder()
					.success(false)
					.message("Windows Service check: we are not running under Windows.")
					.result(null)
					.build();
		}

		final String serviceName = service.getServiceName();
		if (serviceName.isEmpty()) {
			log.debug("Service Criterion, service name is empty.");
			return CriterionTestResult.builder()
					.success(true)
					.message("Windows Service check: actually no test were performed.")
					.result(null)
					.build();
		}

		final String hostname = strategyConfig.getEngineConfiguration().getTarget().getHostname();

		final String query = String.format("select name, state from win32_service where name = '%s'", serviceName);

		try {

			final List<List<String>> queryResult = matsyaClientsExecutor.executeWmi(
					hostname,
					protocol.getUsername(),
					protocol.getPassword(),
					protocol.getTimeout(),
					query,
					DEFAULT_NAMESPACE_WMI);

			if (queryResult.isEmpty()) {
				log.debug("Service Criterion, no {} service found.", service);
				return CriterionTestResult.builder()
						.success(false)
						.message(String.format("Windows Service check: the %s Windows service is not found.", serviceName))
						.result(null)
						.build();
			}

			final String state = queryResult.stream()
					.map(row -> row.get(1))
					.collect(Collectors.joining());

			final String result = SourceTable.tableToCsv(queryResult, COLUMN_SEPARATOR, false);

			final boolean running = "Running".equalsIgnoreCase(state);

			final String message = running ?
					String.format("Windows Service check: the %s Windows service is currently running.", serviceName) :
						String.format("Windows Service check: the %s Windows service is not reported as running.\n %s", serviceName, state); // NOSONAR on %n

					log.debug("Service Criterion, {}", message);
					return CriterionTestResult.builder()
							.success(running)
							.message(message)
							.result(result)
							.build();

		} catch (final Exception e) {
			final String message = String.format(
					"Service Criterion, WMI query %s on %s was unsuccessful due to an exception. Message: %s.",
					query,
					hostname,
					e.getMessage());
			log.error(message, e);
			return CriterionTestResult.builder()
					.message(message)
					.build();
		}

	}

	@Override
	public CriterionTestResult visit(final SNMPGet snmpGet) {
		if (null == snmpGet || snmpGet.getOid() == null) {
			log.error("Malformed SNMPGet criterion {}. Cannot process SNMPGet detection.", snmpGet);
			return CriterionTestResult.empty();
		}

		final SNMPProtocol protocol = (SNMPProtocol) strategyConfig.getEngineConfiguration()
				.getProtocolConfigurations().get(SNMPProtocol.class);

		if (protocol == null) {
			log.debug("The SNMP Credentials are not configured. Cannot process SNMP detection {}.",
					snmpGet);
			return CriterionTestResult.empty();
		}

		final String hostname = strategyConfig.getEngineConfiguration().getTarget().getHostname();

		try {

			final String result = matsyaClientsExecutor.executeSNMPGet(
					snmpGet.getOid(),
					protocol,
					hostname,
					false);

			final TestResult testResult = checkSNMPGetResult(
					hostname,
					snmpGet.getOid(),
					snmpGet.getExpectedResult(),
					result);

			return CriterionTestResult
					.builder()
					.result(result)
					.success(testResult.isSuccess())
					.message(testResult.getMessage())
					.build();

		} catch (final Exception e) {
			final String message = String.format(
					"SNMP Test Failed - SNMP Get of %s on %s was unsuccessful due to an exception. Message: %s.",
					snmpGet.getOid(), hostname, e.getMessage());
			log.debug(message, e);
			return CriterionTestResult.builder().message(message).build();
		}
	}

	/**
	 * Verify the value returned by SNMP Get query. Check the value consistency when
	 * the expected output is not defined. Otherwise check if the value matches the
	 * expected regex.
	 *
	 * @param hostname
	 * @param oid
	 * @param expected
	 * @param result
	 * @return {@link TestResult} wrapping the success status and the message
	 */
	private TestResult checkSNMPGetResult(final String hostname, final String oid, final String expected, final String result) {
		if (expected == null) {
			return checkSNMPGetValue(hostname, oid, result);
		}
		return checkSNMPGetExpectedValue(hostname, oid, expected, result);
	}

	/**
	 * Check if the result matches the expected value
	 *
	 * @param hostname
	 * @param oid
	 * @param expected
	 * @param result
	 * @return {@link TestResult} wrapping the message and the success status
	 */
	private TestResult checkSNMPGetExpectedValue(final String hostname, final String oid, final String expected,
			final String result) {
		String message;
		boolean success = false;
		final Pattern pattern = Pattern.compile(PslUtils.psl2JavaRegex(expected), Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
		if (!pattern.matcher(result).find()) {
			message = String.format(
					"SNMP Test Failed - SNMP Get of %s on %s was successful but the value of the returned OID did not match with the expected result. ",
					oid, hostname);
			message += String.format(EXPECTED_VALUE_RETURNED_VALUE, expected, result);
		} else {
			message = String.format("Successful SNMP Get of %s on %s. Returned Result: %s.", oid, hostname, result);
			success = true;
		}

		log.debug(message);

		return TestResult.builder().message(message).success(success).build();
	}

	/**
	 * Simply check the value consistency and verify whether the returned value is
	 * not null or empty
	 *
	 * @param hostname
	 * @param oid
	 * @param result
	 * @return {@link TestResult} wrapping the message and the success status
	 */
	private TestResult checkSNMPGetValue(final String hostname, final String oid, final String result) {
		String message;
		boolean success = false;
		if (result == null) {
			message = String.format("SNMP Test Failed - SNMP Get of %s on %s was unsuccessful due to a null result.",
					oid, hostname);
		} else if (result.trim().isEmpty()) {
			message = String.format("SNMP Test Failed - SNMP Get of %s on %s was unsuccessful due to an empty result.",
					oid, hostname);
		} else {
			message = String.format("Successful SNMP Get of %s on %s. Returned Result: %s.", oid, hostname, result);
			success = true;
		}

		log.debug(message);

		return TestResult.builder().message(message).success(success).build();
	}

	@Override
	public CriterionTestResult visit(final TelnetInteractive telnetInteractive) {
		// Not implemented yet
		return CriterionTestResult.empty();
	}

	@Override
	public CriterionTestResult visit(final UCS ucs) {
		// Not implemented yet
		return CriterionTestResult.empty();
	}

	@Override
	public CriterionTestResult visit(final WBEM wbem) {

		if (wbem == null || wbem.getWbemQuery() == null) {

			log.error("Malformed WBEM criterion {}. Cannot process WBEM detection.", wbem);
			return CriterionTestResult.empty();
		}

		final EngineConfiguration engineConfiguration = strategyConfig.getEngineConfiguration();

		final WBEMProtocol protocol = (WBEMProtocol) engineConfiguration
				.getProtocolConfigurations()
				.get(WBEMProtocol.class);

		if (protocol == null) {
			log.debug("The WBEM Credentials are not configured. Cannot process WBEM detection {}.",
					wbem);
			return CriterionTestResult.empty();
		}

		final String hostname = engineConfiguration
				.getTarget()
				.getHostname();

		// Find the namespace
		final NamespaceResult namespaceResult = findNamespace(wbem, protocol, hostname);

		// Stop if no namespace is found
		if (!namespaceResult.isSuccess()) {

			return CriterionTestResult
					.builder()
					.success(false)
					.message(buildNoNamespaceErrorMessage(wbem.getWbemQuery(), wbem.getExpectedResult(), namespaceResult,
							WBEM.class))
					.build();
		}

		try {

			// Run the WBEM query if necessary
			final String csvTable = namespaceResult.getCsvTable() == null
					? runWbemQueryAndGetCsv(hostname, wbem.getWbemQuery(), namespaceResult.getNamespace(), protocol)
							: namespaceResult.getCsvTable();

					// Test the result
					final TestResult testResult = getMatchingResult(wbem.getWbemQuery(), namespaceResult.getNamespace(),
							wbem.getExpectedResult(), csvTable, WBEM.class);

					return CriterionTestResult
							.builder()
							.success(testResult.isSuccess())
							.message(testResult.getMessage())
							.result(csvTable)
							.build();

		} catch (final Exception e) { // NOSONAR - not propagating InterruptedException

			final String message = String.format(
					"WBEM Test Failed - WBEM Criterion query %s on %s was unsuccessful due to an exception. Message: %s.",
					wbem.getWbemQuery(), hostname, e.getMessage());

			log.debug(message, e);

			return CriterionTestResult
					.builder()
					.success(false)
					.message(message)
					.build();
		}
	}

	/**
	 * Finds the namespace to use for the execution of the given {@link WBEM} {@link Criterion}.
	 *
	 * @param wbem		{@link WBEM} instance from which we want to extract the namespace.
	 *                  Special values are <em>automatic</em> or <em>null</em>.
	 * @param protocol	The {@link WBEMProtocol} from which we get the default namespace when the mode is not automatic.
	 * @param hostname	The hostname of the target device.
	 *
	 * @return			A {@link NamespaceResult} wrapping the suitable namespace, if there is any.
	 */
	private NamespaceResult findNamespace(final WBEM wbem, final WBEMProtocol protocol, final String hostname) {

		final String criterionNamespace = wbem.getWbemNamespace();

		if (AUTOMATIC_NAMESPACE.equalsIgnoreCase(criterionNamespace)) {

			final String automaticNamespace = strategyConfig.getHostMonitoring().getAutomaticWbemNamespace();

			// It's OK if the namespace has already been detected, we don't re-execute the heavy detection
			return automaticNamespace != null
					? NamespaceResult.builder().namespace(automaticNamespace).success(true).build()
							: detectWbemNamespace(wbem, protocol, hostname);

		} else {

			// Not automatic, then it is provided by the connector otherwise we get the one from the configuration
			return NamespaceResult
					.builder()
					.namespace(criterionNamespace != null ? criterionNamespace : protocol.getNamespace())
					.success(true)
					.build();
		}
	}

	/**
	 * Detect the WBEM namespace
	 *
	 * @param wbem		{@link WBEM} instance from which we want to extract the namespace.
	 *                  Special values are <em>automatic</em> or <em>null</em>.
	 * @param protocol	The user's configured credentials.
	 * @param hostname	The hostname of the target device.
	 *
	 * @return			A {@link NamespaceResult} wrapping the detected namespace
	 * 					and the error message if the detection fails.
	 */
	private NamespaceResult detectWbemNamespace(final WBEM wbem, final WBEMProtocol protocol, final String hostname) {

		// Detect possible namespaces
		final PossibleNamespacesResult possibleWbemNamespacesResult = detectPossibleWbemNamespaces(protocol, hostname);

		// If we can't detect the namespace then we must stop
		if (!possibleWbemNamespacesResult.isSuccess()) {

			return NamespaceResult
					.builder()
					.success(false)
					.errorMessage(possibleWbemNamespacesResult.getErrorMessage())
					.build();
		}

		// Run the query on each namespace and check if the result match the criterion
		final Map<String, String> namespaces = executeWbemAndFilterNamespaces(wbem, protocol,
				possibleWbemNamespacesResult, hostname);

		// No namespace then stop
		if (namespaces.isEmpty()) {

			final String message = String
					.format("No WBEM namespace matches the specified criterion (where '%s' should have matched with '%s')",
							wbem.getWbemQuery(), wbem.getExpectedResult());

			log.debug(message);

			return NamespaceResult
					.builder()
					.success(false)
					.errorMessage(message)
					.build();
		}

		// So, now we have a list of working namespaces.
		// We'd better have only one, but you never know, so try to be clever here...
		// If we have several matching namespaces, including root/cimv2, then exclude this one
		// because it's one that we find in many places and not necessarily with anything useful in it
		// especially if there are other matching namespaces.
		if (namespaces.size() > 1) {
			namespaces.remove(DEFAULT_NAMESPACE);
		}

		// Okay, so even if we have several, select a single one
		final String automaticNamespace = namespaces
				.keySet()
				.stream()
				.findFirst()
				.orElseThrow();

		// Remember the automatic WBEM namespace
		strategyConfig.getHostMonitoring().setAutomaticWbemNamespace(automaticNamespace);

		return NamespaceResult
				.builder()
				.success(true)
				.namespace(automaticNamespace)
				.csvTable(namespaces.get(automaticNamespace))
				.build();
	}

	/**
	 * Detects the possible WBEM namespaces using the configured {@link WBEMProtocol}.
	 *
	 * @param protocol	The user's configured {@link WBEMProtocol}.
	 * @param hostname	The hostname of the target device.
	 *
	 * @return 			A {@link PossibleNamespacesResult} wrapping the success state, the message in case of errors
	 * 					and the possibleWmiNamespaces {@link Set}.
	 */
	private PossibleNamespacesResult detectPossibleWbemNamespaces(final WBEMProtocol protocol, final String hostname) {

		// This list was already retrieved by a previous call, just take this one
		// This will avoid multiple calls to findWbemNamespace doing exactly the same thing several times
		Set<String> possibleWbemNamespaces = strategyConfig.getHostMonitoring().getPossibleWbemNamespaces();
		if (!possibleWbemNamespaces.isEmpty()) {

			return PossibleNamespacesResult
					.builder()
					.possibleNamespaces(possibleWbemNamespaces)
					.success(true)
					.build();
		}

		// Preparing arguments for the WBEM executor
		final boolean useEncryption = WBEMProtocol.WBEMProtocols.HTTPS.equals(protocol.getProtocol()); // protocol cannot be null here
		final String url = MatsyaClientsExecutor.buildWbemUrl(hostname, protocol.getPort(), useEncryption);

		final String username = protocol.getUsername();
		final char[] password = protocol.getPassword();

		final Long timeout = protocol.getTimeout();

		// First, let us execute "SELECT Name FROM __NAMESPACE" on the "root" namespace
		String wbemQuery = null;
		List<List<String>> queryResult;
		String message;
		try {

			wbemQuery = "SELECT Name FROM __NAMESPACE";
			queryResult = matsyaClientsExecutor.executeWbem(url, username, password, timeout.intValue() * 1000,
					wbemQuery, "root");

			possibleWbemNamespaces = extractPossibleNamespaces(queryResult, IGNORED_WBEM_NAMESPACES, "root/");
			if (possibleWbemNamespaces.isEmpty()) {

				message = String.format("%s does not respond to WBEM request %s. Canceling namespace detection.",
						hostname, wbemQuery);

				log.debug(message);

				return PossibleNamespacesResult
						.builder()
						.errorMessage(message)
						.success(false)
						.build();
			}

		} catch (final WBEMException e) {

			final int id = e.getID();

			if (id != WBEMException.CIM_ERR_INVALID_NAMESPACE && id != WBEMException.CIM_ERR_INVALID_CLASS
					&& id != WBEMException.CIM_ERR_NOT_FOUND) {

				message = String.format("%s does not respond to WBEM requests. Error is: %s" +
						"\nCanceling namespace detection.", hostname, e.toString());

				log.debug(message);

				return PossibleNamespacesResult
						.builder()
						.errorMessage(message)
						.success(false)
						.build();
			}

		} catch (final Exception e) { // NOSONAR - not propagating InterruptedException

			message = String.format("%s does not respond to WBEM request %s. Error is: %s" +
					"\nMoving on to testing each interoperability namespace...",
					hostname, wbemQuery, e.getMessage()
					);

			log.debug(message);
		}

		// Now testing each interoperability namespace
		wbemQuery = "SELECT Name FROM CIM_NameSpace";
		for (final String namespace : WBEM_INTEROPERABILITY_NAMESPACES) {

			try {

				queryResult = matsyaClientsExecutor.executeWbem(url, username, password, timeout.intValue() * 1000,
						wbemQuery, namespace);

				possibleWbemNamespaces.addAll(extractPossibleNamespaces(queryResult, IGNORED_WBEM_NAMESPACES,
						"root/"));

			} catch (final Exception e) { // NOSONAR - not propagating InterruptedException

				message = String.format("%s does not respond to WBEM request %s (%s)." +
						"Trying with the next interoperability namespace left.",
						hostname, wbemQuery, namespace);

				log.debug(message);
			}
		}

		if (possibleWbemNamespaces.isEmpty()) {

			return PossibleNamespacesResult
					.builder()
					.errorMessage("No suitable namespace could be found to query host " + hostname + ".")
					.success(false)
					.build();
		}

		return PossibleNamespacesResult
				.builder()
				.possibleNamespaces(possibleWbemNamespaces)
				.success(true)
				.build();
	}

	/**
	 * Executes the given {@link WBEM} criteria
	 * and selects the matching namespaces from the passed {@link PossibleNamespacesResult} instance.
	 *
	 * @param wbem						The WBEM criterion we wish to execute.
	 * @param protocol					The user's configured WBEM protocol (credentials).
	 * @param possibleNamespacesResult	The possible namespaces, always shows success = true.
	 * @param hostname					The hostname of the target device.
	 *
	 * @return							A {@link Map}
	 * 									associating each matching namespace to the corresponding query result.
	 */
	private Map<String, String> executeWbemAndFilterNamespaces(final WBEM wbem, final WBEMProtocol protocol,
			final PossibleNamespacesResult possibleNamespacesResult,
			final String hostname) {

		final Map<String, String> result = new TreeMap<>();

		// Loop over each namespace and run the WBEM query and check if the result matches
		for (final String namespace : possibleNamespacesResult.getPossibleNamespaces()) {

			try {

				// Do the request
				final String csvTable = runWbemQueryAndGetCsv(hostname, wbem.getWbemQuery(), namespace, protocol);

				// If the result matched then the namespace is selected
				if (isMatchingResult(wbem.getExpectedResult(), csvTable)) {
					result.put(namespace, csvTable);
				}

			} catch (final Exception e) { // NOSONAR - not propagating InterruptedException

				// Log an error and go to the next iteration
				final String message = String.format("Query %s failed for namespace %s on host %s. Error: %s",
						wbem.getWbemQuery(), namespace, hostname, e.getMessage());

				log.debug(message);
			}
		}

		return result;
	}

	/**
	 * @param hostname	The target hostname.
	 * @param wbemQuery	The query to execute.
	 * @param namespace	The WBEM namespace.
	 * @param protocol	The User's configured credentials.
	 *
	 * @return			The result of the WBEM query formatted as a CSV.
	 *
	 * @throws WqlQuerySyntaxException	If there is a WQL syntax error.
	 * @throws WBEMException			If there is a WBEM error.
	 * @throws TimeoutException			If the query did not complete on time.
	 * @throws InterruptedException		If the current thread was interrupted while waiting.
	 */
	private String runWbemQueryAndGetCsv(final String hostname, final String wbemQuery, final String namespace, final WBEMProtocol protocol)
			throws WqlQuerySyntaxException, WBEMException, TimeoutException, InterruptedException, MalformedURLException {

		// Preparing arguments for the WBEM executor
		final boolean useEncryption = WBEMProtocol.WBEMProtocols.HTTPS.equals(protocol.getProtocol()); // protocol cannot be null here
		final String url = MatsyaClientsExecutor.buildWbemUrl(hostname, protocol.getPort(), useEncryption);

		final Long timeout = protocol.getTimeout();

		final List<List<String>> queryResult = matsyaClientsExecutor.executeWbem(url, protocol.getUsername(),
				protocol.getPassword(), timeout.intValue() * 1000, wbemQuery, namespace);

		return SourceTable.tableToCsv(queryResult, COLUMN_SEPARATOR, true);
	}

	@Override
	public CriterionTestResult visit(final WMI wmi) {

		if (wmi == null || wmi.getWbemQuery() == null) {
			log.error("Malformed WMI criterion {}. Cannot process WMI detection.", wmi);
			return CriterionTestResult.empty();
		}

		final WMIProtocol protocol = (WMIProtocol) strategyConfig.getEngineConfiguration()
				.getProtocolConfigurations().get(WMIProtocol.class);

		if (protocol == null) {
			log.debug("The WMI Credentials are not configured. Cannot process WMI detection {}.",
					wmi);
			return CriterionTestResult.empty();
		}

		// Find the namespace
		final NamespaceResult namespaceResult = findNamespace(wmi, protocol);

		// Stop if no namespace is found
		if (!namespaceResult.isSuccess()) {
			return CriterionTestResult.builder()
					.success(false)
					.message(buildNoNamespaceErrorMessage(wmi.getWbemQuery(), wmi.getExpectedResult(), namespaceResult,
							WMI.class))
					.build();
		}

		final String hostname = strategyConfig.getEngineConfiguration().getTarget().getHostname();

		try {

			// Run the WMI query
			final String csvTable = runWmiQueryAndGetCsv(hostname, wmi.getWbemQuery(), namespaceResult.getNamespace(), protocol);

			// Test the result
			final TestResult testResult = getMatchingResult(wmi.getWbemQuery(), namespaceResult.getNamespace(),
					wmi.getExpectedResult(), csvTable, WMI.class);

			return CriterionTestResult.builder()
					.success(testResult.isSuccess())
					.message(testResult.getMessage())
					.result(csvTable)
					.build();

		} catch (final Exception e) {
			final String message = String.format(
					"WMI Test Failed - WMI Criterion query %s on %s was unsuccessful due to an exception. Message: %s.",
					wmi.getWbemQuery(), hostname, e.getMessage());
			log.debug(message, e);
			return CriterionTestResult.builder().message(message).build();
		}
	}

	/**
	 * Tries to find the row that matches the expected result in the given {@link Criterion}.
	 *
	 * @param query				The query that was executed.
	 * @param namespace			The namespace in which the query was executed.
	 * @param expectedResult	The expected result.
	 * @param csvTable			The CSV resulting from the execution of the query.
	 *
	 * @param criterionType		The type of {@link Criterion}.
	 *
	 * @return					{@link TestResult} which indicates if the check has succeeded or not.
	 *							TestResult will also wraps the message to set in the testReport parameter.
	 */
	static TestResult getMatchingResult(final String query, final String namespace, final String expectedResult,
			final String csvTable, final Class<? extends Criterion> criterionType) {

		// Not result? success = false
		if (csvTable.isEmpty()) {

			return TestResult
					.builder()
					.success(false)
					.message(buildEmptyResultErrorMessage(query, namespace, expectedResult, criterionType))
					.build();
		}

		final String expected = expectedResult != null ? expectedResult : EMPTY;

		final Pattern pattern = Pattern.compile(PslUtils.psl2JavaRegex(expected),
				Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

		final Matcher matcher = pattern.matcher(csvTable);

		// If the result is found then success = true
		if (matcher.find()) {

			return TestResult
					.builder()
					.success(true)
					.message(buildSuccessMessage(query, namespace, expectedResult, matcher.group(), criterionType))
					.build();
		}

		// Unfortunately success is false as there is no matched result
		return TestResult
				.builder()
				.success(false)
				.message(buildFailedMessage(query, namespace, expectedResult, criterionType))
				.build();
	}

	/**
	 * Builds a failure message in a WMI or WBEM context.
	 *
	 * @param query			The executed query.
	 * @param namespace		The namespace in which the query was executed.
	 * @param expected		The expected result.
	 * @param criterionType	The type of {@link Criterion}.
	 *
	 * @return				A customized message indicating that the test failed.
	 */
	static String buildFailedMessage(final String query, final String namespace, final String expected,
			final Class<? extends Criterion> criterionType) {

		final StringBuilder message = new StringBuilder(criterionType.getSimpleName())
				.append(" Test Failed - The following query succeeded but its result did not match the expected output:\n- query: ")
				.append(query)
				.append(NAMESPACE_MESSAGE)
				.append(namespace);

		appendExpected(message, expected);

		return message.toString();
	}

	/**
	 * Builds a success message in a WMI or WBEM context.
	 *
	 * @param query				The executed query.
	 * @param namespace			The namespace in which the query was executed.
	 * @param expected			The expected result.
	 * @param matchingResult	The matching result.
	 * @param criterionType		The type of {@link Criterion}.
	 *
	 * @return					A customized message indicating that the test succeeded.
	 */
	static String buildSuccessMessage(final String query, final String namespace, final String expected,
			final String matchingResult, final Class<? extends Criterion> criterionType) {

		final StringBuilder message = new StringBuilder();

		message
		.append("The following ")
		.append(criterionType.getSimpleName())
		.append(" query succeeded:\n- ")
		.append(query)
		.append(NAMESPACE_MESSAGE)
		.append(namespace);

		appendExpected(message, expected);

		message.append("\n- Matching Result:\n").append(matchingResult);

		return message.toString();
	}

	/**
	 * Append the expected result to the given message
	 *
	 * @param message  The {@link StringBuilder} wrapping the message
	 * @param expected The expected result from the {@link Connector} criterion
	 */
	static void appendExpected(final StringBuilder message, final String expected) {
		if (expected != null) {
			message.append("\n- Expected result: ").append(expected);
		}
	}

	/**
	 * In a WMI or WBEM context,
	 * when the test returns an empty result, this message will be set in the testReport parameter.
	 *
	 * @param query				The query that was executed.
	 * @param namespace			The namespace used in the detection.
	 * @param expectedResult	The expected result of the query.
	 * @param criterionType		The type of {@link Criterion}.
	 *
	 * @return					A customized error message indicating that the test returned an empty result.
	 */
	static String buildEmptyResultErrorMessage(final String query, final String namespace, final String expectedResult,
			final Class<? extends Criterion> criterionType) {

		final StringBuilder message = new StringBuilder()
				.append(criterionType.getSimpleName())
				.append(" Test Failed - The following query succeeded but did not have any result:\n- query: ")
				.append(query)
				.append(NAMESPACE_MESSAGE)
				.append(namespace);

		appendExpected(message, expectedResult);

		return message.toString();
	}

	/**
	 * In a WMI or WBEM context,
	 * builds the error message which will be set in the test report parameter later by {@link DetectionOperation}
	 * in case we can't detect the namespace.
	 *
	 * @param query             The query.
	 * @param expectedResult	The expected result.
	 * @param namespaceResult	The {@link NamespaceResult} defining the exact error message to append.
	 * @param criterionType		{@link WMI} or {@link WBEM}.
	 * @return					The "no namespace found" error message.
	 */
	String buildNoNamespaceErrorMessage(final String query, final String expectedResult,
			final NamespaceResult namespaceResult,
			final Class<? extends Criterion> criterionType) {

		notNull(criterionType, "criterionType cannot be null.");
		final String type = criterionType.getSimpleName();

		final StringBuilder message = new StringBuilder(type)
				.append(" Test Failed - The following ")
				.append(type)
				.append(" query failed:\n- WQL query: ")
				.append(query);

		appendExpected(message, expectedResult);

		return message
				.append("\n- No valid namespace could be found.\n- Error Message:\n")
				.append(namespaceResult.getErrorMessage()).toString();
	}

	/**
	 * Find the namespace to use for the execution of the given {@link WMI} criterion
	 *
	 * @param wmi      {@link WMI} instance from which we want to extract the namespace. Expected "automatic", null or <em>any string</em>
	 * @param protocol The {@link WMIProtocol} from which we get the default namespace when the mode is not automatic
	 * @return {@link String} value
	 */
	NamespaceResult findNamespace(final WMI wmi, final WMIProtocol protocol) {

		final String criterionNamespace = wmi.getWbemNamespace();

		if (AUTOMATIC_NAMESPACE.equalsIgnoreCase(criterionNamespace)) {
			final String automaticWmiNamespace = strategyConfig.getHostMonitoring().getAutomaticWmiNamespace();

			// It's OK if the namespace has already been detected, we don't re-execute the heavy detection
			return automaticWmiNamespace != null ?
					NamespaceResult.builder().namespace(automaticWmiNamespace).success(true).build()
					: detectWmiNamespace(wmi, protocol);
		} else {
			// Not automatic, then it is provided by the connector otherwise we get the one from the configuration
			final String namespace = criterionNamespace != null ? criterionNamespace : protocol.getNamespace();
			return NamespaceResult.builder().namespace(namespace).success(true).build();
		}

	}

	/**
	 * Detect the WMI namespace
	 *
	 * @param wmi      The WMI criterion which defines the WBEM query and the expected result that need to be used in the detection
	 * @param protocol The user's configured credentials
	 * @return {@link NamespaceResult} wrapping the detected namespace and the error message if the detection fails
	 */
	NamespaceResult detectWmiNamespace(final WMI wmi, final WMIProtocol protocol) {

		// Detect possible namespaces
		final PossibleNamespacesResult possibleWmiNamespacesResult = detectPossibleWmiNamespaces(wmi, protocol);

		// If we can't detect the namespace then we must stop
		if (!possibleWmiNamespacesResult.isSuccess()) {
			return NamespaceResult.builder()
					.success(false)
					.errorMessage(possibleWmiNamespacesResult.getErrorMessage())
					.build();
		}

		final String hostname = strategyConfig.getEngineConfiguration().getTarget().getHostname();

		// Run the WQL on each namespace and check if the result match the criterion
		final Set<String> namespaces = executeWmiAndFilterNamespaces(wmi, protocol, possibleWmiNamespacesResult, hostname);

		// No namespace then stop
		if (namespaces.isEmpty()) {
			final String message = String.format("No WMI namespace matches the specified criterion (where '%s' should have matched with '%s')",
					wmi.getWbemQuery(), wmi.getExpectedResult());
			log.debug(message);
			return NamespaceResult.builder()
					.success(false)
					.errorMessage(message)
					.build();
		}

		// So, now we have a list of working namespaceList
		// We'd better have only one, but you never know, so try to be clever here...
		if (namespaces.size() > 1) {
			namespaces.remove(DEFAULT_NAMESPACE);
			namespaces.remove(DEFAULT_NAMESPACE_WMI);
		}

		// Okay, so even if we have several, select a single one
		final String automaticNamespace = namespaces.stream()
				.findFirst()
				.orElseThrow();

		// Remember the automatic WMI namespace
		strategyConfig.getHostMonitoring().setAutomaticWmiNamespace(automaticNamespace);

		return NamespaceResult.builder()
				.success(true)
				.namespace(automaticNamespace)
				.build();
	}

	/**
	 * Execute the given {@link WMI} criteria and selected the matched namespaces from the passed {@link PossibleNamespacesResult} instance
	 *
	 * @param wmi                      The WMI criterion we wish to execute
	 * @param protocol                 The user's configured WMI protocol (credentials)
	 * @param possibleNamespacesResult The possible namespaces, always shows success = true
	 * @param hostname                 The hostname of the target device
	 * @return {@link Set} of namespace values
	 */
	Set<String> executeWmiAndFilterNamespaces(final WMI wmi, final WMIProtocol protocol,
			final PossibleNamespacesResult possibleNamespacesResult, final String hostname) {

		final Set<String> namespaces = new TreeSet<>();

		// Loop over each namespace and run the WMI query and check if the result matches
		for (final String namespace : possibleNamespacesResult.getPossibleNamespaces()) {

			try {
				// Do the request
				final String csvTable = runWmiQueryAndGetCsv(hostname, wmi.getWbemQuery(), namespace, protocol);

				// If the result matched then the namespace is selected
				if (isMatchingResult(wmi.getExpectedResult(), csvTable)) {
					namespaces.add(namespace);
				}

			} catch (final Exception e) {
				// Log an error and go to the next iteration
				final String message = String.format("Query %s failed for namespace %s on host %s. Error: %s",
						wmi.getWbemQuery(), namespace, hostname, e.getMessage());
				log.debug(message);
			}
		}
		return namespaces;
	}

	/**
	 * Check if the given CSV table matches the expected result
	 *
	 * @param expected The expected result defined in the {@link Connector} instance
	 * @param csvTable The CSV table returned by the WMI client after having queried the service
	 *
	 * @return <code>true</code> if the result matches otherwise <code>false</code>
	 */
	static boolean isMatchingResult(final String expected, final String csvTable) {

		// No result means not match
		if (csvTable.isEmpty()) {
			return false;
		}

		// The expected is not always provided,
		// if it is null and the result is not empty then we are good
		if (expected == null) {
			return true;
		}

		// Perform the check
		final Pattern pattern = Pattern.compile(PslUtils.psl2JavaRegex(expected), Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

		return pattern.matcher(csvTable).find();
	}

	/**
	 * Run the given WMI query provided in the {@link WMI} criterion then create a csv table
	 *
	 * @param hostname					The target hostname
	 * @param wbemQuery					The WQL to execute
	 * @param namespace					The WMI namespace
	 * @param protocol					The User's configured credentials
	 *
	 * @return 							String value
	 *
	 * @throws LocalhostCheckException	If the localhost check fails
	 * @throws WmiComException			For any problem encountered with JNA. I.e. on the connection or the query execution
	 * @throws TimeoutException			When the given timeout is reached
	 * @throws WqlQuerySyntaxException	In case of invalid query
	 */
	String runWmiQueryAndGetCsv(final String hostname, final String wbemQuery, final String namespace,
			final WMIProtocol protocol)
					throws LocalhostCheckException, WmiComException, TimeoutException, WqlQuerySyntaxException {

		final List<List<String>> queryResult = matsyaClientsExecutor.executeWmi(hostname,
				protocol.getUsername(),
				protocol.getPassword(),
				protocol.getTimeout(),
				wbemQuery,
				namespace);

		return SourceTable.tableToCsv(queryResult, COLUMN_SEPARATOR, true);
	}

	/**
	 * Detect the possible WMI namespaces using the configured {@link WMIProtocol}
	 *
	 * @param wmi      The WMI criterion instance we wish to indicate in the failure message and in the debug
	 * @param protocol The user's configured {@link WMIProtocol}
	 * @return {@link PossibleNamespacesResult} wrapping the success state, the message in case of errors and the possibleWmiNamespaces
	 *         {@link Set}
	 */
	PossibleNamespacesResult detectPossibleWmiNamespaces(final WMI wmi, final WMIProtocol protocol) {

		// This list was already retrieved by a previous call, just take this one
		// This will avoid multiple calls to findWbemNamespace doing exactly the same thing several times
		final Set<String> possibleWmiNamespaces = strategyConfig.getHostMonitoring().getPossibleWmiNamespaces();
		if (!possibleWmiNamespaces.isEmpty()) {
			return PossibleNamespacesResult.builder().possibleNamespaces(possibleWmiNamespaces).success(true).build();
		}

		final String hostname = strategyConfig.getEngineConfiguration().getTarget().getHostname();

		// Test root namespace
		final String wbemQuery = "SELECT Name FROM __NAMESPACE";
		try {
			// Do the request
			final List<List<String>> queryResult = matsyaClientsExecutor.executeWmi(hostname,
					protocol.getUsername(),
					protocol.getPassword(),
					protocol.getTimeout(),
					wbemQuery,
					"root");

			// Add the result of this request to possibleWmiNamespaces
			// This will update the possibleWmiNamespace in the HostMonitoring
			possibleWmiNamespaces.addAll(extractPossibleNamespaces(queryResult, IGNORED_WMI_NAMESPACES, "root\\"));

		} catch (final Exception e) {
			// Log the error message and proceed with the next namespace
			final String message = String.format("%s does not respond to WMI request %s. Canceling namespace detection. Error: %s",
					hostname, wbemQuery, e.getMessage());
			log.debug(message);
		}

		// No namespace? then it is a test failure
		if (possibleWmiNamespaces.isEmpty()) {
			final String message = String.format("No WMI namespace matches the specified criterion (where '%s' should have matched with '%s')",
					wmi.getWbemQuery(), wmi.getExpectedResult());
			log.debug(message);
			return PossibleNamespacesResult.builder().errorMessage(message).success(false).build();
		}

		// Success
		return PossibleNamespacesResult.builder().possibleNamespaces(possibleWmiNamespaces).success(true).build();
	}

	/**
	 * Extract the possible namespaces from the given query result.
	 * We expect a query result with multiple lines and only one column defining the namespace value.<br>
	 * The namespace is selected only if it is not from the <em>interop</em> family and it is not flagged as ignored.
	 *
	 * @param namespaceQueryResult	The result which should return a collection of namespaces.
	 * @param ignoredNameSpaces		The {@link Set} of namespaces that should be ignored.
	 * @param prefix				Add this prefix to each valid namespace.
	 *
	 * @return						{@link Set} of namespace values sorted according to the natural ordering.
	 */
	static Set<String> extractPossibleNamespaces(final List<List<String>> namespaceQueryResult,
			final Set<String> ignoredNameSpaces, final String prefix) {

		return namespaceQueryResult
				.stream()
				.filter(row -> !row.isEmpty())
				.flatMap(List::stream)
				.filter(namespace -> !namespace.toLowerCase().contains(INTEROP_NAMESPACE) && !ignoredNameSpaces.contains(namespace))
				.map(namespace -> prefix + namespace)
				.collect(Collectors.toCollection(TreeSet::new));
	}

	@Override
	public CriterionTestResult visit(final SNMPGetNext snmpGetNext) {

		if (snmpGetNext == null || snmpGetNext.getOid() == null) {
			log.error("Malformed SNMPGetNext criterion {}. Cannot process SNMPGetNext detection.", snmpGetNext);
			return CriterionTestResult.empty();
		}

		final SNMPProtocol protocol = (SNMPProtocol) strategyConfig.getEngineConfiguration()
				.getProtocolConfigurations().get(SNMPProtocol.class);

		if (protocol == null) {
			log.debug("The SNMP Credentials are not configured. Cannot process SNMP detection {}.",
					snmpGetNext);
			return CriterionTestResult.empty();
		}

		final String hostname = strategyConfig.getEngineConfiguration().getTarget().getHostname();

		try {

			final String result = matsyaClientsExecutor.executeSNMPGetNext(
					snmpGetNext.getOid(),
					protocol,
					hostname,
					false);

			final TestResult testResult = checkSNMPGetNextResult(
					hostname,
					snmpGetNext.getOid(),
					snmpGetNext.getExpectedResult(),
					result);

			return CriterionTestResult.builder()
					.result(result)
					.success(testResult.isSuccess())
					.message(testResult.getMessage())
					.build();

		} catch (final Exception e) {
			final String message = String.format(
					"SNMP Test Failed - SNMP GetNext of %s on %s was unsuccessful due to an exception. Message: %s.",
					snmpGetNext.getOid(), hostname, e.getMessage());
			log.debug(message, e);
			return CriterionTestResult.builder().message(message).build();
		}
	}

	/**
	 * Verify the value returned by SNMP GetNext query. Check the value consistency
	 * when the expected output is not defined. Otherwise check if the value matches
	 * the expected regex.
	 *
	 * @param hostname
	 * @param oid
	 * @param expected
	 * @param result
	 * @return {@link TestResult} wrapping the success status and the message
	 */
	private TestResult checkSNMPGetNextResult(final String hostname, final String oid, final String expected,
			final String result) {
		if (expected == null) {
			return checkSNMPGetNextValue(hostname, oid, result);
		}

		return checkSNMPGetNextExpectedValue(hostname, oid, expected, result);
	}

	/**
	 * Check if the result matches the expected value
	 *
	 * @param hostname
	 * @param oid
	 * @param expected
	 * @param result
	 * @return {@link TestResult} wrapping the message and the success status
	 */
	private TestResult checkSNMPGetNextExpectedValue(final String hostname, final String oid, final String expected,
			final String result) {
		String message;
		boolean success = true;
		final Matcher matcher = SNMP_GETNEXT_RESULT_REGEX.matcher(result);
		if (matcher.find()) {
			final String value = matcher.group(1);
			final Pattern pattern = Pattern.compile(PslUtils.psl2JavaRegex(expected), Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
			if (!pattern.matcher(value).find()) {
				message = String.format(
						"SNMP Test Failed - SNMP GetNext of %s on %s was successful but the value of the returned OID did not match with the expected result. ",
						oid, hostname);
				message += String.format(EXPECTED_VALUE_RETURNED_VALUE, expected, value);
				success = false;
			} else {
				message = String.format("Successful SNMP GetNext of %s on %s. Returned Result: %s.", oid, hostname, result);
			}
		} else {
			message = String.format(
					"SNMP Test Failed - SNMP GetNext of %s on %s was successful but the value cannot be extracted. ",
					oid, hostname);
			message += String.format("Returned Result: %s.", result);
			success = false;
		}

		log.debug(message);

		return TestResult.builder().message(message).success(success).build();
	}

	/**
	 * Simply check the value consistency and verify whether the returned OID is
	 * under the same tree of the requested OID.
	 *
	 * @param hostname
	 * @param oid
	 * @param result
	 * @return {@link TestResult} wrapping the message and the success status
	 */
	private TestResult checkSNMPGetNextValue(final String hostname, final String oid, final String result) {
		String message;
		boolean success = false;
		if (result == null) {
			message = String.format(
					"SNMP Test Failed - SNMP GetNext of %s on %s was unsuccessful due to a null result.", oid,
					hostname);
		} else if (result.trim().isEmpty()) {
			message = String.format(
					"SNMP Test Failed - SNMP GetNext of %s on %s was unsuccessful due to an empty result.", oid,
					hostname);
		} else if (!result.startsWith(oid)) {
			message = String.format(
					"SNMP Test Failed - SNMP GetNext of %s on %s was successful but the returned OID is not under the same tree. Returned OID: %s.",
					oid, hostname, result.split("\\s")[0]);
		} else {
			message = String.format("Successful SNMP GetNext of %s on %s. Returned Result: %s.", oid, hostname, result);
			success = true;
		}

		log.debug(message);

		return TestResult.builder().message(message).success(success).build();
	}

	@Data
	@Builder
	public static class TestResult {
		private String message;
		private boolean success;
	}

	@Data
	@Builder
	public static class PossibleNamespacesResult {
		private Set<String> possibleNamespaces;
		private boolean success;
		private String errorMessage;
	}

	@Data
	@Builder
	public static class NamespaceResult {
		private String namespace;
		private boolean success;
		private String errorMessage;
		private String csvTable;
	}

	private class IpmiCommandForSolarisException extends Exception {

		private static final long serialVersionUID = 1L;

		public IpmiCommandForSolarisException(final String message) {
			super(message);
		}

	}
}
