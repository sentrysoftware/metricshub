package com.sentrysoftware.matrix.strategy.detection;

import com.sentrysoftware.matrix.common.exception.ControlledSshException;
import com.sentrysoftware.matrix.common.exception.IpmiCommandForSolarisException;
import com.sentrysoftware.matrix.common.exception.MatsyaException;
import com.sentrysoftware.matrix.common.helpers.LocalOsHandler;
import com.sentrysoftware.matrix.configuration.HostConfiguration;
import com.sentrysoftware.matrix.configuration.HttpConfiguration;
import com.sentrysoftware.matrix.configuration.IWinConfiguration;
import com.sentrysoftware.matrix.configuration.IpmiConfiguration;
import com.sentrysoftware.matrix.configuration.OsCommandConfiguration;
import com.sentrysoftware.matrix.configuration.SnmpConfiguration;
import com.sentrysoftware.matrix.configuration.SshConfiguration;
import com.sentrysoftware.matrix.configuration.WbemConfiguration;
import com.sentrysoftware.matrix.connector.model.common.DeviceKind;
import com.sentrysoftware.matrix.connector.model.common.http.body.StringBody;
import com.sentrysoftware.matrix.connector.model.common.http.header.StringHeader;
import com.sentrysoftware.matrix.connector.model.identity.criterion.DeviceTypeCriterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.HttpCriterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.IpmiCriterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.OsCommandCriterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.ProcessCriterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.ProductRequirementsCriterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.ServiceCriterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.SnmpCriterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.SnmpGetCriterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.SnmpGetNextCriterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.WbemCriterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.WmiCriterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.WqlCriterion;
import com.sentrysoftware.matrix.matsya.HttpRequest;
import com.sentrysoftware.matrix.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.strategy.utils.CriterionProcessVisitor;
import com.sentrysoftware.matrix.strategy.utils.OsCommandHelper;
import com.sentrysoftware.matrix.strategy.utils.PslUtils;
import com.sentrysoftware.matrix.strategy.utils.WqlDetectionHelper;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.AUTOMATIC_NAMESPACE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.BMC;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.CONFIGURE_OS_TYPE_MESSAGE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.CRITERION_WMI_NAMESPACE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.CRITERION_WMI_QUERY;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.EMPTY_PROCESS_COMMAND_LINE_MESSAGE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.END_OF_IPMI_COMMAND;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.EXPECTED_VALUE_RETURNED_VALUE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.FAILED_OS_DETECTION_MESSAGE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.HOST_OS_IS_NOT_WINDOWS_SKIP_MESSAGE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.IPMI_DETECTION_FAILURE_MESSAGE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.IPMI_SOLARIS_VERSION_NOT_IDENTIFIED;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.IPMI_TOOL_COMMAND;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.IPMI_TOOL_SUDO_COMMAND;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.IPMI_TOOL_SUDO_MACRO;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.IPMI_VERSION;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.LIPMI;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.LOCAL_OS_IS_NOT_WINDOWS_SKIP_MESSAGE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.MALFORMED_CRITERION_MESSAGE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.MALFORMED_PROCESS_CRITERION_MESSAGE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.MALFORMED_SERVICE_CRITERION_MESSAGE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.MALFORMED_SNMP_GET_CRITERION_MESSAGE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.MALFORMED_SNMP_GET_NEXT_CRITERION_MESSAGE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.NEITHER_WMI_NOR_WINRM_ERROR;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.NO_TEST_WILL_BE_PERFORMED_MESSAGE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.NO_TEST_WILL_BE_PERFORMED_REMOTELY_MESSAGE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.NO_TEST_WILL_BE_PERFORMED_UNKNOWN_OS_MESSAGE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.OLD_SOLARIS_VERSION_MESSAGE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.OPEN_IPMI_INTERFACE_DRIVER;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.REMOTE_PROCESS_MESSAGE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.RUNNING;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.SERVICE_NAME_NOT_SPECIFIED;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.SNMP_CREDENTIALS_NOT_CONFIGURED_MESSAGE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.SNMP_FAILED_EMPTY_RESULT_MESSAGE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.SNMP_FAILED_NULL_RESULT_MESSAGE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.SNMP_FAILED_WITH_EXCEPTION_MESSAGE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.SNMP_GETNEXT_CANNOT_EXTRACT_VALUE_MESSAGE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.SNMP_GETNEXT_EXPECTED_RETURNED_VALUES_MESSAGE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.SNMP_GETNEXT_FAILED_EMPTY_RESULT_MESSAGE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.SNMP_GETNEXT_FAILED_NULL_RESULT_MESSAGE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.SNMP_GETNEXT_FAILED_OID_NOT_MATCHING_MESSAGE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.SNMP_GETNEXT_FAILED_OID_NOT_UNDER_SAME_TREE_MESSAGE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.SNMP_GETNEXT_FAILED_WITH_EXCEPTION_MESSAGE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.SNMP_GETNEXT_RESULT_REGEX;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.SNMP_GETNEXT_RETURNED_RESULT_MESSAGE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.SNMP_GETNEXT_SUCCESSFUL_MESSAGE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.SNMP_OID_NOT_MATCH_MESSAGE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.SNMP_VALUE_CHECK_SUCCESSFUL_MESSAGE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.SOLARIS_VERSION_COMMAND;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.SOLARIS_VERSION_NOT_IDENTIFIED_MESSAGE_TOKEN;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.SUCCESSFUL_OS_DETECTION_MESSAGE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.SUCCESSFUL_SNMP_MESSAGE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.TABLE_SEP;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.UNKNOWN_LOCAL_OS_MESSAGE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.WBEM_CREDENTIALS_NOT_CONFIGURED_MESSAGE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.WBEM_MALFORMED_CRITERION_MESSAGE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.WINDOWS_IS_NOT_RUNNING_MESSAGE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.WINDOWS_IS_RUNNING_MESSAGE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.WMI_NAMESPACE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.WMI_QUERY;

@Slf4j
@Data
@NoArgsConstructor
public class CriterionProcessor {

	private MatsyaClientsExecutor matsyaClientsExecutor;

	private TelemetryManager telemetryManager;

	private String connectorName;

	private WqlDetectionHelper wqlDetectionHelper;

	public CriterionProcessor(MatsyaClientsExecutor matsyaClientsExecutor, TelemetryManager telemetryManager, String connectorName) {
		this.matsyaClientsExecutor = matsyaClientsExecutor;
		this.telemetryManager = telemetryManager;
		this.connectorName = connectorName;
		this.wqlDetectionHelper = new WqlDetectionHelper(matsyaClientsExecutor);
	}

	/**
	 * Process the given {@link DeviceTypeCriterion} through Matsya and return the {@link CriterionTestResult}
	 *
	 * @param deviceTypeCriterion
	 * @return
	 */
	CriterionTestResult process(DeviceTypeCriterion deviceTypeCriterion) {

		if (deviceTypeCriterion == null) {
			log.error(MALFORMED_CRITERION_MESSAGE,
					telemetryManager.getHostConfiguration().getHostname(), deviceTypeCriterion);
			return CriterionTestResult.empty();
		}

		final DeviceKind deviceKind = telemetryManager.getHostConfiguration().getHostType();

		if (DeviceKind.SOLARIS.equals(deviceKind) && !isDeviceKindIncluded(Arrays.asList(DeviceKind.SOLARIS, DeviceKind.SOLARIS), deviceTypeCriterion)
				|| !DeviceKind.SOLARIS.equals(deviceKind) && !isDeviceKindIncluded(Collections.singletonList(deviceKind), deviceTypeCriterion)) {
			return CriterionTestResult
					.builder()
					.message(FAILED_OS_DETECTION_MESSAGE)
					.result(CONFIGURE_OS_TYPE_MESSAGE + deviceKind.name())
					.success(false)
					.build();
		}

		return CriterionTestResult
				.builder()
				.message(SUCCESSFUL_OS_DETECTION_MESSAGE)
				.result(CONFIGURE_OS_TYPE_MESSAGE + deviceKind.name())
				.success(true)
				.build();
	}

	/**
	 * Return true if the deviceKind in the deviceKindList is included in the DeviceTypeCriterion detection.
	 *
	 * @param deviceKindList
	 * @param deviceTypeCriterion
	 * @return
	 */
	public boolean isDeviceKindIncluded(final List<DeviceKind> deviceKindList, final DeviceTypeCriterion deviceTypeCriterion) {

		final Set<DeviceKind> keepOnly = deviceTypeCriterion.getKeep();
		final Set<DeviceKind> exclude = deviceTypeCriterion.getExclude();

		if (keepOnly != null && deviceKindList.stream().anyMatch(keepOnly::contains)) {
			return true;
		}

		if (exclude != null && deviceKindList.stream().anyMatch(exclude::contains)) {
			return false;
		}

		// If no osType is in KeepOnly or Exclude, then return true if KeepOnly is null or empty.
		return keepOnly == null || keepOnly.isEmpty();
	}

	/**
	 * Process the given {@link HttpCriterion} through Matsya and return the {@link CriterionTestResult}
	 *
	 * @param httpCriterion
	 * @return
	 */
	CriterionTestResult process(HttpCriterion httpCriterion) {

		if (httpCriterion == null) {
			return CriterionTestResult.empty();
		}

		final HostConfiguration hostConfiguration = telemetryManager.getHostConfiguration();

		if (hostConfiguration == null) {
			log.debug("There is no host configuration. Cannot process HTTP detection {}.", httpCriterion);
			return CriterionTestResult.empty();
		}

		final String hostname = hostConfiguration.getHostname();

		final HttpConfiguration httpConfiguration = (HttpConfiguration) hostConfiguration
				.getConfigurations()
				.get(HttpConfiguration.class);

		if (httpConfiguration == null) {
			log.debug("Hostname {} - The HTTP credentials are not configured for this host. Cannot process HTTP detection {}.",
					hostname,
					httpCriterion);
			return CriterionTestResult.empty();
		}

		final String result = matsyaClientsExecutor.executeHttp(
				HttpRequest
						.builder()
						.hostname(hostname)
						.method(httpCriterion.getMethod().toString())
						.url(httpCriterion.getUrl())
						.header(new StringHeader(httpCriterion.getHeader()))
						.body(new StringBody(httpCriterion.getBody()))
						.httpConfiguration(httpConfiguration)
						.resultContent(httpCriterion.getResultContent())
						.authenticationToken(httpCriterion.getAuthenticationToken())
						.build(),
				false
		);

		return checkHttpResult(hostname, result, httpCriterion.getExpectedResult());
	}

	/**
	 * Process the given {@link IpmiCriterion} through Matsya and return the {@link CriterionTestResult}
	 *
	 * @param ipmiCriterion
	 * @return CriterionTestResult instance
	 */
	CriterionTestResult process(IpmiCriterion ipmiCriterion) {
		final DeviceKind hostType = telemetryManager.getHostConfiguration().getHostType();

		if (DeviceKind.WINDOWS.equals(hostType)) {
			return processWindowsIpmiDetection(ipmiCriterion);
		} else if (DeviceKind.LINUX.equals(hostType) || DeviceKind.SOLARIS.equals(hostType)) {
			return processUnixIpmiDetection(hostType);
		} else if (DeviceKind.OOB.equals(hostType)) {
			return processOutOfBandIpmiDetection();
		}

		return CriterionTestResult.builder()
				.message(String.format(IPMI_DETECTION_FAILURE_MESSAGE,
						telemetryManager.getHostConfiguration().getHostname(),
						hostType.name()))
				.success(false)
				.build();
	}

	/**
	 * Process IPMI detection for the Windows (NT) system
	 *
	 * @param ipmiCriterion instance of IpmiCriterion
	 * @return CriterionTestResult instance
	 */
	private CriterionTestResult processWindowsIpmiDetection(final IpmiCriterion ipmiCriterion) {

		final String hostname = telemetryManager.getHostConfiguration().getHostname();

		// Find the configured protocol (WinRM or WMI)
		final IWinConfiguration configuration = telemetryManager.getWinConfiguration();

		if (configuration == null) {
			return CriterionTestResult.error(ipmiCriterion, NEITHER_WMI_NOR_WINRM_ERROR);
		}

		WmiCriterion ipmiWmiCriterion = WmiCriterion
				.builder()
				.query(WMI_QUERY)
				.namespace(WMI_NAMESPACE)
				.build();

		return wqlDetectionHelper.performDetectionTest(hostname, configuration, ipmiWmiCriterion);
	}

	/**
	 * Process IPMI detection for the Unix system
	 *
	 * @param hostType
	 * @return CriterionTestResult instance
	 */
	private CriterionTestResult processUnixIpmiDetection(final DeviceKind hostType) {
		String ipmitoolCommand = telemetryManager.getHostProperties().getIpmitoolCommand();
		final String hostname = telemetryManager.getHostConfiguration().getHostname();
		final SshConfiguration sshConfiguration = (SshConfiguration) telemetryManager
				.getHostConfiguration().getConfigurations().get(SshConfiguration.class);

		// Retrieve the sudo and timeout settings from OSCommandConfig for localhost, or directly from SSH for remote
		final OsCommandConfiguration osCommandConfiguration = telemetryManager.getHostProperties().isLocalhost()
				? (OsCommandConfiguration) telemetryManager.getHostConfiguration().getConfigurations().get(OsCommandConfiguration.class)
				: sshConfiguration;

		if (osCommandConfiguration == null) {
			final String message = String.format("Hostname %s - No OS command configuration for this host. Returning an empty result", hostname);
			log.warn(message);
			return CriterionTestResult.builder().success(false).result("").message(message).build();
		}
		final int defaultTimeout = osCommandConfiguration.getTimeout().intValue();
		if (ipmitoolCommand == null || ipmitoolCommand.isEmpty()) {
			ipmitoolCommand = buildIpmiCommand(hostType, hostname, sshConfiguration, osCommandConfiguration, defaultTimeout);
		}

		// buildIpmiCommand method can either return the actual result of the built command or an error. If it is an error we display it in the error message
		if (!ipmitoolCommand.startsWith("PATH=")) {
			return CriterionTestResult.builder().success(false).result("").message(ipmitoolCommand).build();
		}
		// execute the command
		try {
			String result = null;
			result = runOsCommand(ipmitoolCommand, hostname, sshConfiguration, defaultTimeout);
			if (result != null && !result.contains(IPMI_VERSION)) {
				// Didn't find what we expected: exit
				return CriterionTestResult.builder().success(false).result(result)
						.message("Did not get the expected result from the IPMI tool command: " + ipmitoolCommand).build();
			} else {
				// everything goes well
				telemetryManager.getHostProperties().setIpmiExecutionCount(telemetryManager.getHostProperties().getIpmiExecutionCount() + 1);
				return CriterionTestResult.builder().success(true).result(result)
						.message("Successfully connected to the IPMI BMC chip with the in-band driver interface.")
						.build();
			}

		} catch (final Exception e) {
			final String message = String.format("Hostname %s - Cannot execute the IPMI tool command %s. Exception: %s.",
					hostname, ipmitoolCommand, e.getMessage());
			log.debug(message, e);
			return CriterionTestResult.builder().success(false).message(message).build();
		}

	}

	/**
	 * Process IPMI detection for the Out-Of-Band device
	 *
	 * @return {@link CriterionTestResult} wrapping the status of the criterion execution
	 */
	private CriterionTestResult processOutOfBandIpmiDetection() {

		final IpmiConfiguration configuration = (IpmiConfiguration) telemetryManager.getHostConfiguration()
				.getConfigurations().get(IpmiConfiguration.class);

		final String hostname = telemetryManager.getHostConfiguration().getHostname();

		if (configuration == null) {
			log.debug("Hostname {} - The IPMI credentials are not configured for this host. Cannot process IPMI-over-LAN detection.", hostname);
			return CriterionTestResult.empty();
		}

		try {
			final String result = matsyaClientsExecutor.executeIpmiDetection(hostname, configuration);
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
	 * Check the OS type and version and build the correct IPMI command. If the
	 * process fails, return the according error
	 *
	 * @param hostType
	 * @param hostname
	 * @param sshConfiguration
	 * @param osCommandConfiguration
	 * @param defaultTimeout
	 * @return String : the ipmi Command
	 */
	public String buildIpmiCommand(final DeviceKind hostType, final String hostname, final SshConfiguration sshConfiguration,
								   final OsCommandConfiguration osCommandConfiguration, final int defaultTimeout) {
		// do we need to use sudo or not?
		// If we have enabled useSudo (possible only in Web UI and CMA) --> yes
		// Or if the command is listed in useSudoCommandList (possible only in classic
		// wizard) --> yes
		String ipmitoolCommand; // Sonar don't agree with modifying arguments
		if (osCommandConfiguration.isUseSudo()
				|| (osCommandConfiguration.getUseSudoCommands() != null && osCommandConfiguration.getUseSudoCommands().contains("ipmitool"))) {
			ipmitoolCommand = IPMI_TOOL_SUDO_COMMAND.replace(IPMI_TOOL_SUDO_MACRO, osCommandConfiguration.getSudoCommand());
		} else {
			ipmitoolCommand = IPMI_TOOL_COMMAND;
		}

		// figure out the version of the Solaris OS
		if (DeviceKind.SOLARIS.equals(hostType)) {
			String solarisOsVersion = null;
			try {
				// Execute "/usr/bin/uname -r" command in order to obtain the OS Version
				// (Solaris)
				solarisOsVersion = runOsCommand(SOLARIS_VERSION_COMMAND, hostname, sshConfiguration, defaultTimeout);
			} catch (final Exception e) {
				final String message = String.format(IPMI_SOLARIS_VERSION_NOT_IDENTIFIED,
						hostname, ipmitoolCommand, e.getMessage());
				log.debug(message, e);
				return message;
			}
			// Get IPMI command
			if (solarisOsVersion != null) {
				try {
					ipmitoolCommand = getIpmiCommandForSolaris(ipmitoolCommand, hostname, solarisOsVersion);
				} catch (final IpmiCommandForSolarisException e) {
					final String message = String.format(IPMI_SOLARIS_VERSION_NOT_IDENTIFIED,
							hostname, ipmitoolCommand, e.getMessage());
					log.debug(message, e);
					return message;
				}
			}
		} else {
			// If not Solaris, then we're on Linux
			// On Linux, the IPMI interface driver is always 'open'
			ipmitoolCommand = ipmitoolCommand + OPEN_IPMI_INTERFACE_DRIVER;
		}
		telemetryManager.getHostProperties().setIpmitoolCommand(ipmitoolCommand);

		// At the very end of the command line, the actual IPMI command
		ipmitoolCommand = ipmitoolCommand + END_OF_IPMI_COMMAND;
		return ipmitoolCommand;
	}

	/**
	 * Get IPMI command based on solaris version if version == 9 than use lipmi if
	 * version > 9 than use bmc else return error
	 *
	 * @param ipmitoolCommand
	 * @param hostname
	 * @param solarisOsVersion
	 * @return String : ipmi command for Solaris
	 * @throws IpmiCommandForSolarisException
	 */
	public String getIpmiCommandForSolaris(String ipmitoolCommand, final String hostname, final String solarisOsVersion)
			throws IpmiCommandForSolarisException {
		final String[] split = solarisOsVersion.split("\\.");
		if (split.length < 2) {
			throw new IpmiCommandForSolarisException(String.format(
					"Unknown Solaris version (%s) for host: %s IPMI cannot be executed. Returning an empty result.",
					solarisOsVersion, hostname));
		}

		final String solarisVersion = split[1];
		try {
			final int versionInt = Integer.parseInt(solarisVersion);
			if (versionInt == 9) {
				// On Solaris 9, the IPMI interface drive is 'lipmi'
				ipmitoolCommand = ipmitoolCommand + LIPMI;
			} else if (versionInt < 9) {

				throw new IpmiCommandForSolarisException(String.format(
						OLD_SOLARIS_VERSION_MESSAGE,
						solarisOsVersion, hostname));

			} else {
				// On more modern versions of Solaris, the IPMI interface driver is 'bmc'
				ipmitoolCommand = ipmitoolCommand + BMC;
			}
		} catch (final NumberFormatException e) {
			throw new IpmiCommandForSolarisException(SOLARIS_VERSION_NOT_IDENTIFIED_MESSAGE_TOKEN
					+ solarisOsVersion + ".");
		}

		return ipmitoolCommand;
	}

	/**
	 * Run SSH command. Check if we can execute on localhost or remote
	 *
	 * @param ipmitoolCommand
	 * @param hostname
	 * @param sshConfiguration
	 * @param timeout
	 * @return
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws TimeoutException
	 * @throws MatsyaException
	 * @throws ControlledSshException
	 */
	String runOsCommand(
			final String ipmitoolCommand,
			final String hostname,
			final SshConfiguration sshConfiguration,
			final int timeout) throws InterruptedException, IOException, TimeoutException, MatsyaException, ControlledSshException {
		return telemetryManager.getHostProperties().isLocalhost() ? // or we can use NetworkHelper.isLocalhost(hostname)
				OsCommandHelper.runLocalCommand(ipmitoolCommand, timeout, null) :
				OsCommandHelper.runSshCommand(ipmitoolCommand, hostname, sshConfiguration, timeout, null, null);
	}

	/**
	 * Process the given {@link OsCommandCriterion} through Matsya and return the {@link CriterionTestResult}
	 *
	 * @param osCommandCriterion
	 * @return
	 */
	CriterionTestResult process(OsCommandCriterion osCommandCriterion) {
		// TODO
		return null;
	}

	/**
	 * Process the given {@link ProcessCriterion} through Matsya and return the {@link CriterionTestResult}
	 *
	 * @param processCriterion
	 * @return CriterionTestResult instance
	 */
	CriterionTestResult process(ProcessCriterion processCriterion) {
		final String hostname = telemetryManager.getHostConfiguration().getHostname();

		if (processCriterion == null || processCriterion.getCommandLine() == null) {
			log.error(MALFORMED_PROCESS_CRITERION_MESSAGE, hostname, processCriterion);
			return CriterionTestResult.empty();
		}

		if (processCriterion.getCommandLine().isEmpty()) {
			log.debug(EMPTY_PROCESS_COMMAND_LINE_MESSAGE, hostname);
			return CriterionTestResult.builder()
					.success(true)
					.message(NO_TEST_WILL_BE_PERFORMED_MESSAGE)
					.result(null)
					.build();
		}

		if (!telemetryManager.getHostProperties().isLocalhost()) {
			log.debug(REMOTE_PROCESS_MESSAGE, hostname);
			return CriterionTestResult.builder()
					.success(true)
					.message(NO_TEST_WILL_BE_PERFORMED_REMOTELY_MESSAGE)
					.result(null)
					.build();
		}

		final Optional<LocalOsHandler.ILocalOs> maybeLocalOS = LocalOsHandler.getOs();
		if (maybeLocalOS.isEmpty()) {
			log.debug(UNKNOWN_LOCAL_OS_MESSAGE, hostname);
			return CriterionTestResult.builder()
					.success(true)
					.message(NO_TEST_WILL_BE_PERFORMED_UNKNOWN_OS_MESSAGE)
					.result(null)
					.build();
		}

		final CriterionProcessVisitor localOSVisitor = new CriterionProcessVisitor(
				processCriterion.getCommandLine(),
				wqlDetectionHelper,
				hostname
		);
		maybeLocalOS.get().accept(localOSVisitor);
		return localOSVisitor.getCriterionTestResult();
	}

	/**
	 * Process the given {@link ProductRequirementsCriterion} through Matsya and return the {@link CriterionTestResult}
	 *
	 * @param productRequirementsCriterion
	 * @return
	 */
	CriterionTestResult process(ProductRequirementsCriterion productRequirementsCriterion) {
		// TODO
		return null;
	}

	/**
	 * Process the given {@link ServiceCriterion} through Matsya and return the {@link CriterionTestResult}
	 *
	 * @param serviceCriterion
	 * @return
	 */
	CriterionTestResult process(ServiceCriterion serviceCriterion) {
		// Sanity checks
		if (serviceCriterion == null || serviceCriterion.getName() == null) {
			return CriterionTestResult.error(serviceCriterion, MALFORMED_SERVICE_CRITERION_MESSAGE);
		}

		// Find the configured protocol (WinRM or WMI)
		final IWinConfiguration winConfiguration = telemetryManager.getWinConfiguration();

		if (winConfiguration == null) {
			return CriterionTestResult.error(serviceCriterion, NEITHER_WMI_NOR_WINRM_ERROR);
		}

		// The host system must be Windows
		if (!DeviceKind.WINDOWS.equals(telemetryManager.getHostConfiguration().getHostType())) {
			return CriterionTestResult.error(serviceCriterion, HOST_OS_IS_NOT_WINDOWS_SKIP_MESSAGE);
		}

		// Our local system must be Windows
		if (!LocalOsHandler.isWindows()) {
			return CriterionTestResult.success(serviceCriterion, LOCAL_OS_IS_NOT_WINDOWS_SKIP_MESSAGE);

		}

		// Check the service name
		final String serviceName = serviceCriterion.getName();
		if (serviceName.isBlank()) {
			return CriterionTestResult.success(serviceCriterion, SERVICE_NAME_NOT_SPECIFIED);
		}

		final String hostname = telemetryManager.getHostConfiguration().getHostname();

		// Build a new WMI criterion to check the service existence
		WmiCriterion serviceWmiCriterion = WmiCriterion
				.builder()
				.query(String.format(CRITERION_WMI_QUERY, serviceName))
				.namespace(CRITERION_WMI_NAMESPACE)
				.build();

		// Perform this WMI test
		CriterionTestResult wmiTestResult = wqlDetectionHelper.performDetectionTest(hostname, winConfiguration, serviceWmiCriterion);
		if (!wmiTestResult.isSuccess()) {
			return wmiTestResult;
		}

		// The result contains ServiceName;State
		final String result = wmiTestResult.getResult();

		// Check whether the reported state is "Running"
		if (result != null && result.toLowerCase().contains(TABLE_SEP + RUNNING)) {
			return CriterionTestResult.success(serviceCriterion, String.format(WINDOWS_IS_RUNNING_MESSAGE, serviceName));
		}

		// We're here: no good!
		return CriterionTestResult.failure(
				serviceWmiCriterion,
				String.format(WINDOWS_IS_NOT_RUNNING_MESSAGE, serviceName, result) //NOSONAR
		);
	}

	/**
	 * Process the given {@link SnmpCriterion} through Matsya and return the {@link CriterionTestResult}
	 *
	 * @param snmpCriterion
	 * @return
	 */
	CriterionTestResult process(SnmpCriterion snmpCriterion) {
		// TODO
		return null;
	}

	@Data
	@Builder
	public static class TestResult {
		private String message;
		private boolean success;
		private String csvTable;
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
			message = String.format(SNMP_FAILED_NULL_RESULT_MESSAGE,
					hostname, oid);
		} else if (result.isBlank()) {
			message = String.format(SNMP_FAILED_EMPTY_RESULT_MESSAGE,
					hostname, oid);
		} else {
			message = String.format(SNMP_VALUE_CHECK_SUCCESSFUL_MESSAGE, hostname, oid, result);
			success = true;
		}

		log.debug(message);

		return TestResult.builder().message(message).success(success).build();
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
					SNMP_OID_NOT_MATCH_MESSAGE,
					hostname, oid);
			message += String.format(EXPECTED_VALUE_RETURNED_VALUE, expected, result);
		} else {
			message = String.format(SUCCESSFUL_SNMP_MESSAGE, hostname, oid, result);
			success = true;
		}

		log.debug(message);

		return TestResult.builder().message(message).success(success).build();
	}


	/**
	 * Process the given {@link SnmpGetCriterion} through Matsya and return the {@link CriterionTestResult}
	 *
	 * @param snmpGetCriterion
	 * @return SnmpCriterion instance
	 */
	CriterionTestResult process(SnmpGetCriterion snmpGetCriterion) {
		final String hostname = telemetryManager.getHostConfiguration().getHostname();
		if (snmpGetCriterion == null || snmpGetCriterion.getOid() == null) {
			log.error(MALFORMED_SNMP_GET_CRITERION_MESSAGE, hostname, snmpGetCriterion);
			return CriterionTestResult.empty();
		}

		final SnmpConfiguration protocol = (SnmpConfiguration) telemetryManager.getHostConfiguration()
				.getConfigurations().get(SnmpConfiguration.class);

		if (protocol == null) {
			log.debug(SNMP_CREDENTIALS_NOT_CONFIGURED_MESSAGE, hostname, snmpGetCriterion);
			return CriterionTestResult.empty();
		}

		try {

			final String result = matsyaClientsExecutor.executeSNMPGet(
					snmpGetCriterion.getOid(),
					protocol,
					hostname,
					false);

			final TestResult testResult = checkSNMPGetResult(
					hostname,
					snmpGetCriterion.getOid(),
					snmpGetCriterion.getExpectedResult(),
					result);

			return CriterionTestResult
					.builder()
					.result(result)
					.success(testResult.isSuccess())
					.message(testResult.getMessage())
					.build();

		} catch (final Exception e) {
			final String message = String.format(SNMP_FAILED_WITH_EXCEPTION_MESSAGE, hostname, snmpGetCriterion.getOid(), e.getMessage());
			log.debug(message, e);
			return CriterionTestResult.builder().message(message).build();
		}
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
			message = String.format(SNMP_GETNEXT_FAILED_NULL_RESULT_MESSAGE, hostname, oid);
		} else if (result.isBlank()) {
			message = String.format(SNMP_GETNEXT_FAILED_EMPTY_RESULT_MESSAGE, hostname, oid);
		} else if (!result.startsWith(oid)) {
			message = String.format(SNMP_GETNEXT_FAILED_OID_NOT_UNDER_SAME_TREE_MESSAGE, hostname, oid, result.split("\\s")[0]);
		} else {
			message = String.format(SNMP_GETNEXT_SUCCESSFUL_MESSAGE, hostname, oid, result);
			success = true;
		}

		log.debug(message);

		return TestResult.builder().message(message).success(success).build();
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
				message = String.format(SNMP_GETNEXT_FAILED_OID_NOT_MATCHING_MESSAGE, hostname, oid);
				message += String.format(SNMP_GETNEXT_EXPECTED_RETURNED_VALUES_MESSAGE, expected, value);
				success = false;
			} else {
				message = String.format(SNMP_GETNEXT_SUCCESSFUL_MESSAGE, hostname, oid, result);
			}
		} else {
			message = String.format(SNMP_GETNEXT_CANNOT_EXTRACT_VALUE_MESSAGE, hostname, oid);
			message += String.format(SNMP_GETNEXT_RETURNED_RESULT_MESSAGE, result);
			success = false;
		}

		log.debug(message);

		return TestResult.builder().message(message).success(success).build();
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
	 * Process the given {@link SnmpGetNextCriterion} through Matsya and return the {@link CriterionTestResult}
	 *
	 * @param snmpGetNextCriterion
	 * @return
	 */
	CriterionTestResult process(SnmpGetNextCriterion snmpGetNextCriterion) {
		final String hostname = telemetryManager.getHostConfiguration().getHostname();

		if (snmpGetNextCriterion == null || snmpGetNextCriterion.getOid() == null) {
			log.error(MALFORMED_SNMP_GET_NEXT_CRITERION_MESSAGE, hostname, snmpGetNextCriterion);
			return CriterionTestResult.empty();
		}

		final SnmpConfiguration snmpConfiguration = (SnmpConfiguration) telemetryManager.getHostConfiguration()
				.getConfigurations().get(SnmpConfiguration.class);

		if (snmpConfiguration == null) {
			log.debug(SNMP_CREDENTIALS_NOT_CONFIGURED_MESSAGE, hostname, snmpGetNextCriterion);
			return CriterionTestResult.empty();
		}

		try {

			final String result = matsyaClientsExecutor.executeSNMPGetNext(
					snmpGetNextCriterion.getOid(),
					snmpConfiguration,
					hostname,
					false);

			final TestResult testResult = checkSNMPGetNextResult(
					hostname,
					snmpGetNextCriterion.getOid(),
					snmpGetNextCriterion.getExpectedResult(),
					result);

			return CriterionTestResult.builder()
					.result(result)
					.success(testResult.isSuccess())
					.message(testResult.getMessage())
					.build();

		} catch (final Exception e) {
			final String message = String.format(SNMP_GETNEXT_FAILED_WITH_EXCEPTION_MESSAGE, hostname, snmpGetNextCriterion.getOid(), e.getMessage());
			log.debug(message, e);
			return CriterionTestResult.builder().message(message).build();
		}
	}

	/**
	 * Process the given {@link WmiCriterion} through Matsya and return the {@link CriterionTestResult}
	 *
	 * @param wmiCriterion
	 * @return
	 */
	CriterionTestResult process(WmiCriterion wmiCriterion) {
		// TODO
		return null;
	}


	/**
	 * Find the namespace to use for the execution of the given {@link WbemCriterion}.
	 *
	 * @param hostname          The hostname of the host device
	 * @param wbemConfiguration The WBEM protocol configuration (port, credentials, etc.)
	 * @param wbemCriterion     The WQL criterion with an "Automatic" namespace
	 * @return A {@link CriterionTestResult} telling whether we found the proper namespace for the specified WQL
	 */
	private CriterionTestResult findNamespace(final String hostname, final WbemConfiguration wbemConfiguration, final WbemCriterion wbemCriterion) {
		// Get the list of possible namespaces on this host
		Set<String> possibleWbemNamespaces = telemetryManager.getHostProperties().getPossibleWbemNamespaces();

		// Only one thread at a time must be figuring out the possible namespaces on a given host
		synchronized (possibleWbemNamespaces) {

			if (possibleWbemNamespaces.isEmpty()) {

				// If we don't have this list already, figure it out now
				final WqlDetectionHelper.PossibleNamespacesResult possibleWbemNamespacesResult =
						wqlDetectionHelper.findPossibleNamespaces(hostname, wbemConfiguration);

				// If we can't detect the namespace then we must stop
				if (!possibleWbemNamespacesResult.isSuccess()) {
					return CriterionTestResult.error(wbemCriterion, possibleWbemNamespacesResult.getErrorMessage());
				}

				// Store the list of possible namespaces in HostMonitoring, for next time we need it
				possibleWbemNamespaces.clear();
				possibleWbemNamespaces.addAll(possibleWbemNamespacesResult.getPossibleNamespaces());

			}
		}

		// Perform a namespace detection
		WqlDetectionHelper.NamespaceResult namespaceResult =
				wqlDetectionHelper.detectNamespace(hostname, wbemConfiguration, wbemCriterion, Collections.unmodifiableSet(possibleWbemNamespaces));

		// If that was successful, remember it in HostMonitoring, so we don't perform this
		// (costly) detection again
		if (namespaceResult.getResult().isSuccess()) {
			telemetryManager
					.getHostProperties()
					.getConnectorNamespaces()
					.get(namespaceResult.getNamespace())
					.setAutomaticWbemNamespace(namespaceResult.getNamespace());
		}

		return namespaceResult.getResult();
	}

	/**
	 * Process the given {@link WbemCriterion} through Matsya and return the {@link CriterionTestResult}
	 *
	 * @param wbemCriterion
	 * @return
	 */
	CriterionTestResult process(WbemCriterion wbemCriterion) {
		// Sanity check
		if (wbemCriterion == null || wbemCriterion.getQuery() == null) {
			return CriterionTestResult.error(wbemCriterion, WBEM_MALFORMED_CRITERION_MESSAGE);
		}

		// Gather the necessary info on the test that needs to be performed
		final String hostname = telemetryManager.getHostConfiguration().getHostname();

		final WbemConfiguration wbemConfiguration =
				(WbemConfiguration) telemetryManager.getHostConfiguration().getConfigurations().get(WbemConfiguration.class);
		if (wbemConfiguration == null) {
			return CriterionTestResult.error(wbemCriterion, WBEM_CREDENTIALS_NOT_CONFIGURED_MESSAGE);
		}

		// If namespace is specified as "Automatic"
		if (AUTOMATIC_NAMESPACE.equalsIgnoreCase(wbemCriterion.getNamespace())) {

			final String cachedNamespace = telemetryManager
					.getHostProperties()
					.getConnectorNamespaces()
					.get(wbemCriterion.getNamespace())
					.getAutomaticWbemNamespace();

			// If not detected already, find the namespace
			if (cachedNamespace == null) {
				return findNamespace(hostname, wbemConfiguration, wbemCriterion);
			}

			// Update the criterion with the cached namespace
			WqlCriterion cachedNamespaceCriterion = wbemCriterion.copy();
			cachedNamespaceCriterion.setNamespace(cachedNamespace);

			// Run the test
			return wqlDetectionHelper.performDetectionTest(hostname, wbemConfiguration, cachedNamespaceCriterion);
		}

		// Run the test
		return wqlDetectionHelper.performDetectionTest(hostname, wbemConfiguration, wbemCriterion);
	}

	/**
	 * Process the given {@link WqlCriterion} through Matsya and return the {@link CriterionTestResult}
	 *
	 * @param wqlCriterion
	 * @return
	 */
	CriterionTestResult process(WqlCriterion wqlCriterion) {
		// TODO
		return null;
	}

	/**
	 * @param hostname       The hostname against which the HTTP test has been carried out.
	 * @param result         The actual result of the HTTP test.
	 * @param expectedResult The expected result of the HTTP test.
	 * @return A {@link TestResult} summarizing the outcome of the HTTP test.
	 */
	private CriterionTestResult checkHttpResult(final String hostname, final String result,
												final String expectedResult) {

		String message;
		boolean success = false;

		if (expectedResult == null) {
			if (result == null || result.isEmpty()) {
				message = String.format("Hostname %s - HTTP test failed - The HTTP test did not return any result.", hostname);
			} else {
				message = String.format("Hostname %s - HTTP test succeeded. Returned result: %s.", hostname, result);
				success = true;
			}

		} else {
			// We convert the PSL regex from the expected result into a Java regex to be able to compile and test it
			final Pattern pattern = Pattern.compile(PslUtils.psl2JavaRegex(expectedResult), Pattern.CASE_INSENSITIVE);
			if (result != null && pattern.matcher(result).find()) {
				message = String.format("Hostname %s - HTTP test succeeded. Returned result: %s.", hostname, result);
				success = true;
			} else {
				message = String
						.format("Hostname %s - HTTP test failed - "
										+ "The result (%s) returned by the HTTP test did not match the expected result (%s).",
								hostname, result, expectedResult);
				message += String.format(EXPECTED_VALUE_RETURNED_VALUE, expectedResult, result);
			}
		}

		log.debug(message);

		return CriterionTestResult
				.builder()
				.result(result)
				.message(message)
				.success(success)
				.build();
	}
}
