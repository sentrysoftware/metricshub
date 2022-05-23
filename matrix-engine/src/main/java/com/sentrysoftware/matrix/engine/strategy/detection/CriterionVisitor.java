package com.sentrysoftware.matrix.engine.strategy.detection;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.AUTOMATIC_NAMESPACE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TABLE_SEP;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.sentrysoftware.matrix.common.exception.MatsyaException;
import com.sentrysoftware.matrix.common.exception.NoCredentialProvidedException;
import com.sentrysoftware.matrix.common.exception.StepException;
import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.common.helpers.LocalOsHandler;
import com.sentrysoftware.matrix.common.helpers.LocalOsHandler.ILocalOs;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.OsType;
import com.sentrysoftware.matrix.connector.model.detection.criteria.Criterion;
import com.sentrysoftware.matrix.connector.model.detection.criteria.WqlCriterion;
import com.sentrysoftware.matrix.connector.model.detection.criteria.http.Http;
import com.sentrysoftware.matrix.connector.model.detection.criteria.ipmi.Ipmi;
import com.sentrysoftware.matrix.connector.model.detection.criteria.kmversion.KmVersion;
import com.sentrysoftware.matrix.connector.model.detection.criteria.os.Os;
import com.sentrysoftware.matrix.connector.model.detection.criteria.oscommand.OsCommand;
import com.sentrysoftware.matrix.connector.model.detection.criteria.process.Process;
import com.sentrysoftware.matrix.connector.model.detection.criteria.service.Service;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SnmpGet;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SnmpGetNext;
import com.sentrysoftware.matrix.connector.model.detection.criteria.sshinteractive.SshInteractive;
import com.sentrysoftware.matrix.connector.model.detection.criteria.ucs.Ucs;
import com.sentrysoftware.matrix.connector.model.detection.criteria.wbem.Wbem;
import com.sentrysoftware.matrix.connector.model.detection.criteria.wmi.Wmi;
import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.protocol.AbstractCommand;
import com.sentrysoftware.matrix.engine.protocol.HttpProtocol;
import com.sentrysoftware.matrix.engine.protocol.IpmiOverLanProtocol;
import com.sentrysoftware.matrix.engine.protocol.OsCommandConfig;
import com.sentrysoftware.matrix.engine.protocol.SnmpProtocol;
import com.sentrysoftware.matrix.engine.protocol.SshProtocol;
import com.sentrysoftware.matrix.engine.protocol.WbemProtocol;
import com.sentrysoftware.matrix.engine.protocol.WmiProtocol;
import com.sentrysoftware.matrix.engine.strategy.StrategyConfig;
import com.sentrysoftware.matrix.engine.strategy.matsya.HttpRequest;
import com.sentrysoftware.matrix.engine.strategy.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.engine.strategy.utils.OsCommandHelper;
import com.sentrysoftware.matrix.engine.strategy.utils.OsCommandResult;
import com.sentrysoftware.matrix.engine.strategy.utils.PslUtils;
import com.sentrysoftware.matrix.engine.strategy.utils.SshInteractiveHelper;
import com.sentrysoftware.matrix.engine.strategy.utils.WqlDetectionHelper;
import com.sentrysoftware.matrix.engine.strategy.utils.WqlDetectionHelper.NamespaceResult;
import com.sentrysoftware.matrix.engine.strategy.utils.WqlDetectionHelper.PossibleNamespacesResult;

import com.sentrysoftware.matrix.engine.host.HardwareHost;
import com.sentrysoftware.matrix.engine.host.HostType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class CriterionVisitor implements ICriterionVisitor {

	private static final String IPMI_VERSION = "IPMI Version";
	private static final String SOLARIS_VERSION_COMMAND = "/usr/bin/uname -r";
	private static final String IPMI_TOOL_SUDO_COMMAND = "PATH=$PATH:/usr/local/bin:/usr/sfw/bin;export PATH;%{SUDO:ipmitool}ipmitool -I ";
	private static final String IPMI_TOOL_SUDO_MACRO = "%{SUDO:ipmitool}";

	private static final String IPMI_TOOL_COMMAND = "PATH=$PATH:/usr/local/bin:/usr/sfw/bin;export PATH;ipmitool -I ";

	private static final Pattern SNMP_GETNEXT_RESULT_REGEX = Pattern.compile("\\w+\\s+\\w+\\s+(.*)");
	private static final String EXPECTED_VALUE_RETURNED_VALUE = "Expected value: %s - returned value %s.";

	private StrategyConfig strategyConfig;
	private MatsyaClientsExecutor matsyaClientsExecutor;
	private WqlDetectionHelper wqlDetectionHelper;
	private Connector connector;

	@Override
	public CriterionTestResult visit(final Http criterion) {

		if (criterion == null) {
			return CriterionTestResult.empty();
		}

		final EngineConfiguration engineConfiguration = strategyConfig.getEngineConfiguration();

		final HttpProtocol protocol = (HttpProtocol) engineConfiguration
				.getProtocolConfigurations()
				.get(HttpProtocol.class);

		final String hostname = engineConfiguration
				.getHost()
				.getHostname();
		
		if (protocol == null) {
			log.debug("Hostname {} - The HTTP Credentials are not configured. Cannot process HTTP detection {}.", hostname, criterion);
			return CriterionTestResult.empty();
		}

		final String result = matsyaClientsExecutor.executeHttp(HttpRequest.builder()
				.hostname(hostname)
				.method(criterion.getMethod())
				.url(criterion.getUrl())
				.header(criterion.getHeader())
				.body(criterion.getBody())
				.httpProtocol(protocol)
				.resultContent(criterion.getResultContent())
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

				message = String.format("Hostname %s - HTTP Test Failed - the HTTP Test did not return any result.", hostname);

			} else {

				message = String.format("Hostname %s - Successful HTTP Test. Returned result: %s", hostname, result);
				success = true;
			}

		} else {

			final Pattern pattern = Pattern.compile(PslUtils.psl2JavaRegex(expectedResult));
			if (result != null && pattern.matcher(result).find()) {

				message = String.format("Hostname %s - Successful HTTP Test. Returned result: %s", hostname, result);
				success = true;

			} else {

				message = String
						.format("Hostname %s - HTTP Test Failed - "
								+"the returned result (%s) of the HTTP Test did not match the expected result (%s).",
								hostname, result, expectedResult);
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
	public CriterionTestResult visit(final Ipmi ipmi) {

		final HardwareHost host = strategyConfig.getEngineConfiguration().getHost();
		final HostType hostType = host.getType();

		if (HostType.MS_WINDOWS.equals(hostType)) {
			return processWindowsIpmiDetection(ipmi);
		} else if (HostType.LINUX.equals(hostType) || HostType.SUN_SOLARIS.equals(hostType)) {
			return processUnixIpmiDetection(hostType);
		} else if (HostType.MGMT_CARD_BLADE_ESXI.equals(hostType)) {
			return processOutOfBandIpmiDetection();
		}

		final String message = String.format("Hostname %s - Failed to perform IPMI detection. %s is an unsupported OS for IPMI.", host.getHostname(),
				hostType.name());

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

		final IpmiOverLanProtocol protocol = (IpmiOverLanProtocol) strategyConfig.getEngineConfiguration()
				.getProtocolConfigurations().get(IpmiOverLanProtocol.class);

		final String hostname = strategyConfig.getEngineConfiguration().getHost().getHostname();
		
		if (protocol == null) {
			log.debug("Hostname {} - The IPMI Credentials are not configured. Cannot process IPMI-over-LAN detection.", hostname);
			return CriterionTestResult.empty();
		}

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
			final String message = String.format("Hostname %s - Cannot execute IPMI-over-LAN command to get the chassis status. Exception: %s",
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
	 * @param hostType
	 *
	 * @return
	 */
	private CriterionTestResult processUnixIpmiDetection(final HostType hostType) {

		String ipmitoolCommand = strategyConfig.getHostMonitoring().getIpmitoolCommand();
		final String hostname = strategyConfig.getEngineConfiguration().getHost().getHostname();
		final SshProtocol sshProtocol = (SshProtocol) strategyConfig.getEngineConfiguration()
				.getProtocolConfigurations().get(SshProtocol.class);

		// Retrieve the sudo and timeout settings from OSCommandConfig for localhost, or directly from SSH for remote
		final AbstractCommand osCommandConfig = strategyConfig.getHostMonitoring().isLocalhost()
				? (OsCommandConfig) strategyConfig.getEngineConfiguration().getProtocolConfigurations().get(OsCommandConfig.class)
				: sshProtocol;

		if (osCommandConfig == null) {
			final String message = String.format("Hostname %s - No OS Command Configuration. Return empty result.", hostname);
			log.warn(message);
			return CriterionTestResult.builder().success(false).result("").message(message).build();
		}
		final int defaultTimeout = osCommandConfig.getTimeout().intValue();
		if (ipmitoolCommand == null || ipmitoolCommand.isEmpty()) {
			ipmitoolCommand = buildIpmiCommand(hostType, hostname, sshProtocol, osCommandConfig, defaultTimeout);
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
			final String message = String.format("Hostname %s - Cannot execute IPMI Tool Command %s. Exception: %s",
					hostname, ipmitoolCommand, e.getMessage());
			log.debug(message, e);
			return CriterionTestResult.builder().success(false).message(message).build();
		}

	}

	/**
	 * Check the OS type and version and build the correct IPMI command. If the
	 * process fails, return the according error
	 *
	 * @param hostType
	 * @param hostname
	 * @param sshProtocol
	 * @param osCommandConfig
	 * @param defaultTimeout
	 * @return
	 */
	public String buildIpmiCommand(final HostType hostType, final String hostname, final SshProtocol sshProtocol,
			final AbstractCommand osCommandConfig, final int defaultTimeout) {
		// do we need to use sudo or not?
		// If we have enabled useSudo (possible only in Web UI and CMA) --> yes
		// Or if the command is listed in useSudoCommandList (possible only in classic
		// wizard) --> yes
		String ipmitoolCommand; // Sonar don't agree with modifying arguments
		if (osCommandConfig.isUseSudo() || osCommandConfig.getUseSudoCommands().contains("ipmitool")) {
			ipmitoolCommand = IPMI_TOOL_SUDO_COMMAND.replace(IPMI_TOOL_SUDO_MACRO, osCommandConfig.getSudoCommand());
		} else {
			ipmitoolCommand = IPMI_TOOL_COMMAND;
		}

		// figure out the version of the Solaris OS
		if (HostType.SUN_SOLARIS.equals(hostType)) {
			String solarisOsVersion = null;
			try {
				// Execute "/usr/bin/uname -r" command in order to obtain the OS Version
				// (Solaris)
				solarisOsVersion = runOsCommand(SOLARIS_VERSION_COMMAND, hostname, sshProtocol, defaultTimeout);
			} catch (final Exception e) {
				final String message = String.format("Hostname %s - Couldn't identify Solaris version %s. Exception: %s",
						hostname, ipmitoolCommand, e.getMessage());
				log.debug(message, e);
				return message;
			}
			// Get IPMI command
			if (solarisOsVersion != null) {
				try {
					ipmitoolCommand = getIpmiCommandForSolaris(ipmitoolCommand, hostname, solarisOsVersion);
				} catch (final IpmiCommandForSolarisException e) {
					final String message = String.format("Hostname %s - Couldn't identify Solaris version %s. Exception: %s",
							hostname, ipmitoolCommand, e.getMessage());
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
	 * @throws TimeoutException
	 * @throws MatsyaException
	 */
	String runOsCommand(
			final String ipmitoolCommand,
			final String hostname,
			final SshProtocol sshProtocol,
			final int timeout) throws InterruptedException, IOException, TimeoutException, MatsyaException {
		return strategyConfig.getHostMonitoring().isLocalhost() ? // or we can use NetworkHelper.isLocalhost(hostname)
				OsCommandHelper.runLocalCommand(ipmitoolCommand, timeout, null) :
					OsCommandHelper.runSshCommand(ipmitoolCommand, hostname, sshProtocol, timeout, null, null);
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
	private CriterionTestResult processWindowsIpmiDetection(final Ipmi ipmi) {

		final String hostname = strategyConfig.getEngineConfiguration().getHost().getHostname();
		final WmiProtocol wmiConfig =
				(WmiProtocol) strategyConfig.getEngineConfiguration().getProtocolConfigurations().get(WmiProtocol.class);

		if (wmiConfig == null) {
			return CriterionTestResult.error(ipmi, "No WMI credentials provided.");
		}

		Wmi ipmiWmiCriterion = Wmi
				.builder()
				.wbemQuery("SELECT Description FROM ComputerSystem")
				.wbemNamespace("root\\hardware")
				.build();

		return wqlDetectionHelper.performDetectionTest(hostname, wmiConfig, ipmiWmiCriterion);
	}

	@Override
	public CriterionTestResult visit(final KmVersion kmVersion) {
		// Not implemented yet
		return CriterionTestResult.empty();
	}

	@Override
	public CriterionTestResult visit(final Os os) {
		if (os == null) {
			log.error("Hostname {} - Malformed os criterion {}. Cannot process OS detection.", 
					strategyConfig.getEngineConfiguration().getHost().getHostname(), os);
			return CriterionTestResult.empty();
		}

		final OsType osType = strategyConfig.getEngineConfiguration().getHost().getType().getOsType();

		if (OsType.SOLARIS.equals(osType) && !isOsTypeIncluded(Arrays.asList(OsType.SOLARIS, OsType.SUNOS), os)
				|| !OsType.SOLARIS.equals(osType) && !isOsTypeIncluded(Collections.singletonList(osType), os)) {
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
	 * @param osTypeList
	 * @param os
	 * @return
	 */
	public boolean isOsTypeIncluded(final List<OsType> osTypeList, final Os os) {
		final Set<OsType> keepOnly = os.getKeepOnly();
		final Set<OsType> exclude = os.getExclude();

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
	public CriterionTestResult visit(final OsCommand osCommand) {
		if (osCommand == null || osCommand.getCommandLine() == null) {
			return CriterionTestResult.error(osCommand, "Malformed OSCommand criterion.");
		}

		if (osCommand.getCommandLine().isEmpty() ||
				osCommand.getExpectedResult() == null || osCommand.getExpectedResult().isEmpty()) {
			return CriterionTestResult.success(osCommand, "CommandLine or ExpectedResult are empty. Skipping this test.");
		}


		try {
			final OsCommandResult osCommandResult = OsCommandHelper.runOsCommand(
					osCommand.getCommandLine(),
					strategyConfig.getEngineConfiguration(),
					connector.getEmbeddedFiles(),
					osCommand.getTimeout(),
					osCommand.isExecuteLocally(),
					strategyConfig.getHostMonitoring().isLocalhost());

			final OsCommand osCommandNoPassword = OsCommand.builder()
					.commandLine(osCommandResult.getNoPasswordCommand())
					.executeLocally(osCommand.isExecuteLocally())
					.timeout(osCommand.getTimeout())
					.expectedResult(osCommand.getExpectedResult())
					.build();

			final Matcher matcher = Pattern
					.compile(PslUtils.psl2JavaRegex(osCommand.getExpectedResult()),
							Pattern.CASE_INSENSITIVE | Pattern.MULTILINE)
					.matcher(osCommandResult.getResult());

			return matcher.find()?
					CriterionTestResult.success(osCommandNoPassword, osCommandResult.getResult()) :
						CriterionTestResult.failure(osCommandNoPassword, osCommandResult.getResult());

		} catch(NoCredentialProvidedException e) {
			return CriterionTestResult.error(osCommand, e.getMessage());
		} catch (Exception e) {
			return CriterionTestResult.error(osCommand, e);
		}
	}

	@Override
	public CriterionTestResult visit(final Process process) {
		final String hostname = strategyConfig.getEngineConfiguration().getHost().getHostname();
		
		if (process == null || process.getProcessCommandLine() == null) {
			log.error("Hostname {} - Malformed Process Criterion {}. Cannot process Process detection.", hostname, process);
			return CriterionTestResult.empty();
		}

		if (process.getProcessCommandLine().isEmpty()) {
			log.debug("Hostname {} - Process Criterion, Process Command Line is empty.", hostname);
			return CriterionTestResult.builder()
					.success(true)
					.message("Process presence check: actually no test were performed.")
					.result(null)
					.build();
		}

		if (!strategyConfig.getHostMonitoring().isLocalhost()) {
			log.debug("Hostname {} - Process Criterion, Not Localhost.", hostname);
			return CriterionTestResult.builder()
					.success(true)
					.message("Process presence check: no test will be performed remotely.")
					.result(null)
					.build();
		}

		final Optional<ILocalOs> maybeLocalOS = LocalOsHandler.getOs();
		if (maybeLocalOS.isEmpty()) {
			log.debug("Hostname {} - Process Criterion, Unknown Local OS.", hostname);
			return CriterionTestResult.builder()
					.success(true)
					.message("Process presence check: OS unknown, no test will be performed.")
					.result(null)
					.build();
		}

		final CriterionProcessVisitor localOSVisitor = new CriterionProcessVisitor(
				process.getProcessCommandLine(),
				wqlDetectionHelper,
				hostname
		);
		maybeLocalOS.get().accept(localOSVisitor);
		return localOSVisitor.getCriterionTestResult();
	}

	@Override
	public CriterionTestResult visit(final Service service) {

		// Sanity checks
		if (service == null  ||  service.getServiceName() == null) {
			return CriterionTestResult.error(service, "Malformed Service criterion.");
		}

		// We need WMI for this
		final WmiProtocol wmiConfig =
				(WmiProtocol) strategyConfig.getEngineConfiguration().getProtocolConfigurations().get(WmiProtocol.class);
		if (wmiConfig == null) {
			return CriterionTestResult.error(service, "WMI Credentials are not configured.");
		}

		// The host system must be Windows
		if (!HostType.MS_WINDOWS.equals(strategyConfig.getEngineConfiguration().getHost().getType())) {
			return CriterionTestResult.error(service, "Host system is not Windows.");
		}

		// Our local system must be Windows
		if (!LocalOsHandler.isWindows()) {
			return CriterionTestResult.success(service, "We're not running on Windows. Skipping this test.");
		}

		// Check the service name
		final String serviceName = service.getServiceName();
		if (serviceName.isBlank()) {
			return CriterionTestResult.success(service, "Service name is not specified. No test performed.");
		}

		final String hostname = strategyConfig.getEngineConfiguration().getHost().getHostname();

		// Build a new WMI criterion to check the service existence
		Wmi serviceWmiCriterion = Wmi
				.builder()
				.wbemQuery(String.format("SELECT Name, State FROM Win32_Service WHERE Name = '%s'", serviceName))
				.wbemNamespace("root\\cimv2")
				.build();

		// Perform this WMI test
		CriterionTestResult wmiTestResult = wqlDetectionHelper.performDetectionTest(hostname, wmiConfig, serviceWmiCriterion);
		if (!wmiTestResult.isSuccess()) {
			return wmiTestResult;
		}

		// The result contains ServiceName;State
		final String result = wmiTestResult.getResult();

		// Check whether the reported state is "Running"
		if (result != null && result.toLowerCase().contains(TABLE_SEP + "running")) {
			return CriterionTestResult.success(service, String.format("The %s Windows Service is currently running.", serviceName));
		}

		// We're here: no good!
		return CriterionTestResult.failure(
				service,
				String.format("The %s Windows Service is not reported as running:\n%s", serviceName, result) //NOSONAR
		);
	}

	@Override
	public CriterionTestResult visit(final SnmpGet snmpGet) {
		final String hostname = strategyConfig.getEngineConfiguration().getHost().getHostname();
		if (null == snmpGet || snmpGet.getOid() == null) {
			log.error("Hostname {} - Malformed SNMPGet criterion {}. Cannot process SNMPGet detection.", hostname, snmpGet);
			return CriterionTestResult.empty();
		}

		final SnmpProtocol protocol = (SnmpProtocol) strategyConfig.getEngineConfiguration()
				.getProtocolConfigurations().get(SnmpProtocol.class);

		if (protocol == null) {
			log.debug("Hostname {} - The SNMP Credentials are not configured. Cannot process SNMP detection {}.", hostname, snmpGet);
			return CriterionTestResult.empty();
		}

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
					"Hostname %s - SNMP Test Failed - SNMP Get of %s was unsuccessful due to an exception. Message: %s.",
					hostname, snmpGet.getOid(), e.getMessage());
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
		if (result == null || !pattern.matcher(result).find()) {
			message = String.format(
					"Hostname %s - SNMP Test Failed - SNMP Get of %s was successful but the value of the returned OID did not match with the expected result. ",
					hostname, oid);
			message += String.format(EXPECTED_VALUE_RETURNED_VALUE, expected, result);
		} else {
			message = String.format("Hostname %s - Successful SNMP Get of %s. Returned Result: %s.", hostname, oid, result);
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
			message = String.format("Hostname %s - SNMP Test Failed - SNMP Get of %s was unsuccessful due to a null result.",
					hostname, oid);
		} else if (result.trim().isEmpty()) {
			message = String.format("Hostname %s - SNMP Test Failed - SNMP Get of %s was unsuccessful due to an empty result.",
					hostname, oid);
		} else {
			message = String.format("Hostname %s - Successful SNMP Get of %s. Returned Result: %s.", hostname, oid, result);
			success = true;
		}

		log.debug(message);

		return TestResult.builder().message(message).success(success).build();
	}

	@Override
	public CriterionTestResult visit(final SshInteractive sshInteractive) {
		if (sshInteractive == null || sshInteractive.getSteps() == null) {
			return CriterionTestResult.error(sshInteractive, "Malformed SshInteractive criterion.");
		}

		try {
			final List<String> results =
					SshInteractiveHelper.runSshInteractive(
							strategyConfig.getEngineConfiguration(),
							sshInteractive.getSteps(),
							String.format("sshInteractive detection.criteria(%d)", sshInteractive.getIndex()));

			final String result = results.stream().collect(Collectors.joining(HardwareConstants.NEW_LINE));

			if (sshInteractive.getExpectedResult() == null || sshInteractive.getExpectedResult().isEmpty()) {
				return result.isEmpty() ?
						CriterionTestResult.failure(sshInteractive, result) :
							CriterionTestResult.success(
									sshInteractive,
									results.stream().collect(Collectors.joining(HardwareConstants.NEW_LINE)));
			}

			final Matcher matcher = Pattern
					.compile(PslUtils.psl2JavaRegex(sshInteractive.getExpectedResult()))
					.matcher(result);
			return matcher.find() ?
					CriterionTestResult.success(
							sshInteractive,
							results.stream().collect(Collectors.joining(HardwareConstants.NEW_LINE))) :
					CriterionTestResult.failure(sshInteractive, result);

		} catch(final NoCredentialProvidedException e) {
			return CriterionTestResult.error(sshInteractive, e.getMessage());

		} catch(final StepException e) {
			if (e.getCause() != null) {
				return CriterionTestResult.error(
						sshInteractive,
						String.format("%s - %s: %s",
								e.getMessage(),
								e.getCause().getClass().getSimpleName(),
								e.getCause().getMessage()));
			} else {
				return CriterionTestResult.error(sshInteractive, e.getMessage());
			}

		} catch (final Exception e) {
			return CriterionTestResult.error(sshInteractive, e);
		}
	}

	@Override
	public CriterionTestResult visit(final Ucs ucs) {
		// Not implemented yet
		return CriterionTestResult.empty();
	}

	@Override
	public CriterionTestResult visit(final Wbem wbemCriterion) {

		// Sanity check
		if (wbemCriterion == null || wbemCriterion.getWbemQuery() == null) {
			return CriterionTestResult.error(wbemCriterion, "Malformed criterion. Cannot perform detection.");
		}

		// Gather the necessary info on the test that needs to be performed
		final EngineConfiguration engineConfiguration = strategyConfig.getEngineConfiguration();

		final String hostname = engineConfiguration.getHost().getHostname();

		final WbemProtocol wbemConfig =
				(WbemProtocol) engineConfiguration.getProtocolConfigurations().get(WbemProtocol.class);
		if (wbemConfig == null) {
			return CriterionTestResult.error(wbemCriterion, "The WBEM Credentials are not configured");
		}

		// If namespace is specified as "Automatic"
		if (AUTOMATIC_NAMESPACE.equalsIgnoreCase(wbemCriterion.getWbemNamespace())) {

			final String cachedNamespace = strategyConfig
					.getHostMonitoring()
					.getConnectorNamespace(connector)
					.getAutomaticWbemNamespace();

			// If not detected already, find the namespace
			if (cachedNamespace == null) {
				return findNamespace(hostname, wbemConfig, wbemCriterion);
			}

			// Update the criterion with the cached namespace
			WqlCriterion cachedNamespaceCriterion = wbemCriterion.copy();
			cachedNamespaceCriterion.setWbemNamespace(cachedNamespace);

			// Run the test
			return wqlDetectionHelper.performDetectionTest(hostname, wbemConfig, cachedNamespaceCriterion);
		}

		// Run the test
		return wqlDetectionHelper.performDetectionTest(hostname, wbemConfig, wbemCriterion);

	}

	/**
	 * Find the namespace to use for the execution of the given {@link Wbem} {@link Criterion}.
	 *
	 * @param hostname The hostname of the host device
	 * @param wbemConfig The WBEM protocol configuration (port, credentials, etc.)
	 * @param criterion The WQL criterion with an "Automatic" namespace
	 *
	 * @return A {@link CriterionTestResult} telling whether we found the proper namespace for the specified WQL
	 */
	private CriterionTestResult findNamespace(final String hostname, final WbemProtocol wbemConfig, final Wbem criterion) {

		// Get the list of possible namespaces on this host
		Set<String> possibleWbemNamespaces = strategyConfig.getHostMonitoring().getPossibleWbemNamespaces();

		// Only one thread at a time must be figuring out the possible namespaces on a given host
		synchronized (possibleWbemNamespaces) {

			if (possibleWbemNamespaces.isEmpty()) {

				// If we don't have this list already, figure it out now
				final PossibleNamespacesResult possibleWbemNamespacesResult =
						wqlDetectionHelper.findPossibleNamespaces(hostname, wbemConfig);

				// If we can't detect the namespace then we must stop
				if (!possibleWbemNamespacesResult.isSuccess()) {
					return CriterionTestResult.error(criterion, possibleWbemNamespacesResult.getErrorMessage());
				}

				// Store the list of possible namespaces in HostMonitoring, for next time we need it
				possibleWbemNamespaces.clear();
				possibleWbemNamespaces.addAll(possibleWbemNamespacesResult.getPossibleNamespaces());

			}
		}

		// Perform a namespace detection
		NamespaceResult namespaceResult =
				wqlDetectionHelper.detectNamespace(hostname, wbemConfig, criterion, Collections.unmodifiableSet(possibleWbemNamespaces));

		// If that was successful, remember it in HostMonitoring, so we don't perform this
		// (costly) detection again
		if (namespaceResult.getResult().isSuccess()) {
			strategyConfig
				.getHostMonitoring()
				.getConnectorNamespace(connector)
				.setAutomaticWbemNamespace(namespaceResult.getNamespace());
		}

		return namespaceResult.getResult();
	}


	@Override
	public CriterionTestResult visit(final Wmi wmiCriterion) {

		// Sanity check
		if (wmiCriterion == null || wmiCriterion.getWbemQuery() == null) {
			return CriterionTestResult.error(wmiCriterion, "Malformed criterion. Cannot perform detection.");
		}

		// Gather the necessary info on the test that needs to be performed
		final EngineConfiguration engineConfiguration = strategyConfig.getEngineConfiguration();

		final String hostname = engineConfiguration.getHost().getHostname();

		final WmiProtocol wmiConfig =
				(WmiProtocol) engineConfiguration.getProtocolConfigurations().get(WmiProtocol.class);
		if (wmiConfig == null) {
			return CriterionTestResult.error(wmiCriterion, "The WBEM Credentials are not configured");
		}

		// If namespace is specified as "Automatic"
		if (AUTOMATIC_NAMESPACE.equalsIgnoreCase(wmiCriterion.getWbemNamespace())) {

			final String cachedNamespace = strategyConfig
					.getHostMonitoring()
					.getConnectorNamespace(connector)
					.getAutomaticWmiNamespace();

			// If not detected already, find the namespace
			if (cachedNamespace == null) {
				return findNamespace(hostname, wmiConfig, wmiCriterion);
			}

			// Update the criterion with the cached namespace
			WqlCriterion cachedNamespaceCriterion = wmiCriterion.copy();
			cachedNamespaceCriterion.setWbemNamespace(cachedNamespace);

			// Run the test
			return wqlDetectionHelper.performDetectionTest(hostname, wmiConfig, cachedNamespaceCriterion);
		}

		// Run the test
		return wqlDetectionHelper.performDetectionTest(hostname, wmiConfig, wmiCriterion);
	}


	/**
	 * Find the namespace to use for the execution of the given {@link Wmi} {@link Criterion}.
	 *
	 * @param hostname The hostname of the host device
	 * @param wmiConfig The WMI protocol configuration (credentials, etc.)
	 * @param criterion The WQL criterion with an "Automatic" namespace
	 *
	 * @return A {@link CriterionTestResult} telling whether we found the proper namespace for the specified WQL
	 */
	CriterionTestResult findNamespace(final String hostname, final WmiProtocol wmiConfig, final Wmi criterion) {

		// Get the list of possible namespaces on this host
		Set<String> possibleWmiNamespaces = strategyConfig.getHostMonitoring().getPossibleWmiNamespaces();

		// Only one thread at a time must be figuring out the possible namespaces on a given host
		synchronized (possibleWmiNamespaces) {

			if (possibleWmiNamespaces.isEmpty()) {

				// If we don't have this list already, figure it out now
				final PossibleNamespacesResult possibleWmiNamespacesResult =
						wqlDetectionHelper.findPossibleNamespaces(hostname, wmiConfig);

				// If we can't detect the namespace then we must stop
				if (!possibleWmiNamespacesResult.isSuccess()) {
					return CriterionTestResult.error(criterion, possibleWmiNamespacesResult.getErrorMessage());
				}

				// Store the list of possible namespaces in HostMonitoring, for next time we need it
				possibleWmiNamespaces.clear();
				possibleWmiNamespaces.addAll(possibleWmiNamespacesResult.getPossibleNamespaces());

			}
		}

		// Perform a namespace detection
		NamespaceResult namespaceResult =
				wqlDetectionHelper.detectNamespace(hostname, wmiConfig, criterion, Collections.unmodifiableSet(possibleWmiNamespaces));

		// If that was successful, remember it in HostMonitoring, so we don't perform this
		// (costly) detection again
		if (namespaceResult.getResult().isSuccess()) {
			strategyConfig
				.getHostMonitoring()
				.getConnectorNamespace(connector)
				.setAutomaticWmiNamespace(namespaceResult.getNamespace());
		}

		return namespaceResult.getResult();
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


	@Override
	public CriterionTestResult visit(final SnmpGetNext snmpGetNext) {

		final String hostname = strategyConfig.getEngineConfiguration().getHost().getHostname();
		
		if (snmpGetNext == null || snmpGetNext.getOid() == null) {
			log.error("Hostname {} - Malformed SNMPGetNext criterion {}. Cannot process SNMPGetNext detection.", hostname, snmpGetNext);
			return CriterionTestResult.empty();
		}

		final SnmpProtocol protocol = (SnmpProtocol) strategyConfig.getEngineConfiguration()
				.getProtocolConfigurations().get(SnmpProtocol.class);

		if (protocol == null) {
			log.debug("Hostname {} - The SNMP Credentials are not configured. Cannot process SNMP detection {}.", hostname, snmpGetNext);
			return CriterionTestResult.empty();
		}

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
					"Hostname %s - SNMP Test Failed - SNMP GetNext of %s was unsuccessful due to an exception. Message: %s.",
					hostname, snmpGetNext.getOid(), e.getMessage());
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
						"Hostname %s - SNMP Test Failed - SNMP GetNext of %s was successful but the value of the returned OID did not match with the expected result. ",
						hostname, oid);
				message += String.format(EXPECTED_VALUE_RETURNED_VALUE, expected, value);
				success = false;
			} else {
				message = String.format("Hostname %s - Successful SNMP GetNext of %s. Returned Result: %s.", hostname, oid, result);
			}
		} else {
			message = String.format(
					"Hostname %s - SNMP Test Failed - SNMP GetNext of %s was successful but the value cannot be extracted. ",
					hostname, oid);
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
					"Hostname %s - SNMP Test Failed - SNMP GetNext of %s was unsuccessful due to a null result.", hostname, oid);
		} else if (result.trim().isEmpty()) {
			message = String.format(
					"Hostname %s - SNMP Test Failed - SNMP GetNext of %s was unsuccessful due to an empty result.", hostname, oid);
		} else if (!result.startsWith(oid)) {
			message = String.format(
					"Hostname %s - SNMP Test Failed - SNMP GetNext of %s was successful but the returned OID is not under the same tree. Returned OID: %s.",
					hostname, oid, result.split("\\s")[0]);
		} else {
			message = String.format("Hostname %s - Successful SNMP GetNext of %s. Returned Result: %s.", hostname, oid, result);
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
		private String csvTable;
	}

	private class IpmiCommandForSolarisException extends Exception {

		private static final long serialVersionUID = 1L;

		public IpmiCommandForSolarisException(final String message) {
			super(message);
		}

	}

}
