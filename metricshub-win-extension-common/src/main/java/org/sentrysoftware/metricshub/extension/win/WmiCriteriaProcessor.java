package org.sentrysoftware.metricshub.extension.win;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub WMI Extension
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
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.LOCALHOST;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.TABLE_SEP;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.sentrysoftware.metricshub.engine.common.exception.NoCredentialProvidedException;
import org.sentrysoftware.metricshub.engine.common.helpers.LocalOsHandler;
import org.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.CommandLineCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.IpmiCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.ProcessCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.ServiceCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.WmiCriterion;
import org.sentrysoftware.metricshub.engine.strategy.detection.CriterionTestResult;
import org.sentrysoftware.metricshub.engine.strategy.source.SourceTable;
import org.sentrysoftware.metricshub.engine.strategy.utils.OsCommandResult;
import org.sentrysoftware.metricshub.engine.strategy.utils.PslUtils;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

/**
 * A class responsible for processing WMI criteria to evaluate WMI queries against specified criteria.
 * It provides methods to perform WMI operations, evaluate the results against expected outcomes,
 * and generate criterion test results accordingly.
 */
@Slf4j
public class WmiCriteriaProcessor {

	private static final String NEITHER_WMI_NOR_WIN_RM_ERROR_MSG = "Neither WMI nor WinRM credentials are configured for this host.";

	/**
	 * WMI Default Namespace
	 */
	public static final String WMI_DEFAULT_NAMESPACE = "root\\cimv2";

	/**
	 * Namespace prefix used for filtering
	 */
	private static final String ROOT_SLASH = "root/";

	/**
	 * Ignored WMI namespaces
	 */
	private static final Set<String> IGNORED_WMI_NAMESPACES = Set.of(
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
		"perform",
		"MSCluster",
		"MicrosoftActiveDirectory",
		"MicrosoftNLB",
		"Microsoft",
		"ServiceModel",
		"nap"
	);

	private IWinRequestExecutor winRequestExecutor;
	private Function<TelemetryManager, IWinConfiguration> configurationRetriever;
	private String connectorId;
	private WinCommandService winCommandService;

	/**
	 * Creates a new {@code WmiCriteriaProcessor} with a specified WMI request
	 * executor, a function to retrieve WMI configuration based on telemetry
	 * manager, and a connector ID. This constructor allows for full customization
	 * of the processors behavior.
	 *
	 * @param winRequestExecutor     The executor used to perform WMI queries. This must not be null.
	 * @param configurationRetriever A function that retrieves the {@link IWinConfiguration} based on the given
	 *                               {@link TelemetryManager}. This can be null if no dynamic configuration retrieval is needed.
	 * @param connectorId            An identifier used to uniquely identify the connector, can be null if not used.
	 */
	public WmiCriteriaProcessor(
		@NonNull IWinRequestExecutor winRequestExecutor,
		Function<TelemetryManager, IWinConfiguration> configurationRetriever,
		String connectorId
	) {
		this.winRequestExecutor = winRequestExecutor;
		this.configurationRetriever = configurationRetriever;
		this.connectorId = connectorId;
		winCommandService = new WinCommandService(winRequestExecutor);
	}

	/**
	 * Creates a new {@code WmiCriteriaProcessor} using only a specified WMI request
	 * executor. This constructor sets up a processor with no configuration
	 * retriever and no connector ID, suitable for scenarios where static
	 * configuration is sufficient or no connector identification is required.
	 *
	 * @param winRequestExecutor The executor used to perform WMI queries, must not be null.
	 */
	public WmiCriteriaProcessor(@NonNull IWinRequestExecutor winRequestExecutor) {
		this(winRequestExecutor, null, null);
	}

	/**
	 * Processes a WMI criterion by executing a WMI request and evaluating the result.
	 * The method retrieves Win configuration, executes the WMI request,
	 * and checks the result against the expected value defined in the criterion. It then returns a
	 * {@link CriterionTestResult} indicating the success or failure of the criterion evaluation.
	 *
	 * @param wmiCriterion     The criterion including the WMI query and the expected result.
	 * @param telemetryManager The telemetry manager providing access to host configuration and WMI/WinRm credentials.
	 * @return A {@link CriterionTestResult} representing the outcome of the criterion evaluation.
	 */
	public CriterionTestResult process(final WmiCriterion wmiCriterion, final TelemetryManager telemetryManager) {
		// Sanity check
		if (wmiCriterion == null) {
			return CriterionTestResult.error(wmiCriterion, "Malformed criterion. Cannot perform detection.");
		}

		final String hostname = telemetryManager.getHostConfiguration().getHostname();

		// Find the configured Windows protocol (WMI or WinRM)
		final IWinConfiguration winConfiguration = configurationRetriever.apply(telemetryManager);

		if (winConfiguration == null) {
			return CriterionTestResult.error(wmiCriterion, NEITHER_WMI_NOR_WIN_RM_ERROR_MSG);
		}

		// If namespace is specified as "Automatic"
		if (AUTOMATIC_NAMESPACE.equalsIgnoreCase(wmiCriterion.getNamespace())) {
			final String cachedNamespace = telemetryManager
				.getHostProperties()
				.getConnectorNamespace(connectorId)
				.getAutomaticWmiNamespace();

			// If not detected already, find the namespace
			if (cachedNamespace == null) {
				return findNamespace(connectorId, telemetryManager, hostname, winConfiguration, wmiCriterion);
			}

			// Update the criterion with the cached namespace
			final WmiCriterion cachedNamespaceCriterion = wmiCriterion.copy();
			cachedNamespaceCriterion.setNamespace(cachedNamespace);

			// Run the test
			return performDetectionTest(hostname, winConfiguration, cachedNamespaceCriterion);
		}

		// Run the test
		return performDetectionTest(hostname, winConfiguration, wmiCriterion);
	}

	/**
	 * Processes a Windows Service criterion by executing a WMI request to get the windows service then evaluating its state.
	 * The method retrieves Win configuration, executes the WMI request to get the service name and its state,
	 * and checks the result against WMI result. It then returns a
	 * {@link CriterionTestResult} indicating the success or failure of the criterion evaluation.
	 *
	 * @param serviceCriterion The service criterion including the service name to check.
	 * @param telemetryManager The telemetry manager providing access to host configuration and WMI/WinRM credentials.
	 * @return A {@link CriterionTestResult} representing the outcome of the criterion evaluation.
	 */
	public CriterionTestResult process(final ServiceCriterion serviceCriterion, final TelemetryManager telemetryManager) {
		// Sanity checks
		if (serviceCriterion == null) {
			return CriterionTestResult.error(serviceCriterion, "Malformed Service criterion.");
		}

		// Find the configured protocol (WinRM or WMI)
		final IWinConfiguration winConfiguration = configurationRetriever.apply(telemetryManager);

		if (winConfiguration == null) {
			return CriterionTestResult.error(
				serviceCriterion,
				NEITHER_WMI_NOR_WIN_RM_ERROR_MSG
			);
		}

		// The host system must be Windows
		if (!DeviceKind.WINDOWS.equals(telemetryManager.getHostConfiguration().getHostType())) {
			return CriterionTestResult.error(serviceCriterion, "Host OS is not Windows. Skipping this test.");
		}

		// Our local system must be Windows
		if (!LocalOsHandler.isWindows()) {
			return CriterionTestResult.success(serviceCriterion, "Local OS is not Windows. Skipping this test.");
		}

		// Check the service name
		final String serviceName = serviceCriterion.getName();
		if (serviceName.isBlank()) {
			return CriterionTestResult.success(serviceCriterion, "Service name is not specified. Skipping this test.");
		}

		final String hostname = telemetryManager.getHostConfiguration().getHostname();

		// Build a new WMI criterion to check the service existence
		WmiCriterion serviceWmiCriterion = WmiCriterion
			.builder()
			.query(String.format("SELECT Name, State FROM Win32_Service WHERE Name = '%s'", serviceName))
			.namespace(WMI_DEFAULT_NAMESPACE)
			.build();

		// Perform this WMI test
		CriterionTestResult wmiTestResult = performDetectionTest(hostname, winConfiguration, serviceWmiCriterion);
		if (!wmiTestResult.isSuccess()) {
			return wmiTestResult;
		}

		// The result contains ServiceName;State
		final String result = wmiTestResult.getResult();

		// Check whether the reported state is "Running"
		if (result != null && result.toLowerCase().contains(TABLE_SEP + "running")) {
			return CriterionTestResult.success(
				serviceCriterion,
				String.format("The %s Windows Service is currently running.", serviceName)
			);
		}

		// We're here: no good!
		return CriterionTestResult.failure(
			serviceWmiCriterion,
			String.format("The %s Windows Service is not reported as running:\n%s", serviceName, result) //NOSONAR
		);
	}

	/**
	 * Perform the specified WMI detection test, on the specified Win protocol configuration.
	 * <br>
	 * Note: "Automatic" namespace is not supported in this method.
	 * <br>
	 *
	 * @param hostname         Host name
	 * @param winConfiguration Win configuration (credentials, timeout)
	 * @param wmiCriterion     WMI detection properties (WQL, namespace, expected result)
	 * @return {@link CriterionTestResult} which indicates if the check has succeeded or not.
	 */
	public CriterionTestResult performDetectionTest(
		final String hostname,
		@NonNull final IWinConfiguration winConfiguration,
		@NonNull final WmiCriterion wmiCriterion
	) {
		// Make the WBEM query
		final List<List<String>> queryResult;
		try {
			queryResult =
				winRequestExecutor.executeWmi(hostname, winConfiguration, wmiCriterion.getQuery(), wmiCriterion.getNamespace());
		} catch (Exception e) {
			return CriterionTestResult.error(wmiCriterion, e);
		}

		// Serialize the result as a CSV
		String actualResult = SourceTable.tableToCsv(queryResult, TABLE_SEP, true);

		// Empty result? ==> failure
		if (actualResult == null || actualResult.isBlank()) {
			return CriterionTestResult.failure(wmiCriterion, "No result.");
		}

		// No expected result (and non-empty result)? ==> success
		if (wmiCriterion.getExpectedResult() == null || wmiCriterion.getExpectedResult().isBlank()) {
			return CriterionTestResult.success(wmiCriterion, actualResult);
		}

		// Search for the expected result
		final Matcher matcher = Pattern
			.compile(PslUtils.psl2JavaRegex(wmiCriterion.getExpectedResult()), Pattern.CASE_INSENSITIVE | Pattern.MULTILINE)
			.matcher(actualResult);

		// If the expected result is found ==> success
		if (matcher.find()) {
			return CriterionTestResult.success(wmiCriterion, matcher.group());
		}

		// No match!
		return CriterionTestResult.failure(wmiCriterion, actualResult);
	}

	/**
	 * Detect the WBEM/WMI namespace applicable to the specified WBEM/WMI criterion.
	 * <br>
	 * The namespace in the criterion must be "Automatic".
	 * <br>
	 *
	 * @param hostname           The host name
	 * @param configuration      Win configuration (credentials, timeout)
	 * @param wmiCriterion       WMI detection properties (WQL, expected result, namespace must be "Automatic")
	 * @param possibleNamespaces The possible namespaces to execute the WQL on
	 * @return A {@link NamespaceResult} wrapping the detected namespace
	 * and the error message if the detection fails.
	 */
	public NamespaceResult detectNamespace(
		final String hostname,
		final IWinConfiguration configuration,
		final WmiCriterion wmiCriterion,
		final Set<String> possibleNamespaces
	) {
		// Run the query on each namespace and check if the result match the criterion
		final Map<String, CriterionTestResult> namespaces = new TreeMap<>();
		final WmiCriterion tentativeCriterion = wmiCriterion.copy();

		// Loop over each namespace and run the WBEM query and check if the result matches
		for (final String namespace : possibleNamespaces) {
			// Update the criterion with the current namespace that needs to be tested
			tentativeCriterion.setNamespace(namespace);

			// Do the request
			final CriterionTestResult testResult = performDetectionTest(hostname, configuration, tentativeCriterion);

			// If the result matched then the namespace is selected
			if (testResult.isSuccess()) {
				namespaces.put(namespace, testResult);
			} else {
				// If the test failed with an exception, we probably don't need to go further
				Throwable e = testResult.getException();
				if (e != null && !winRequestExecutor.isAcceptableException(e)) {
					// This error indicates that the CIM server will probably never respond to anything
					// (timeout, or bad credentials), so there's no point in pursuing our efforts here.
					log.debug(
						"Hostname %s - Does not respond to %s requests. %s: %s\nCancelling namespace detection.",
						hostname,
						wmiCriterion.getClass().getSimpleName(),
						e.getClass().getSimpleName(),
						e.getMessage()
					);

					return NamespaceResult.builder().result(testResult).build();
				}
			}
		}

		// No namespace => failure
		if (namespaces.isEmpty()) {
			String formattedNamespaceList = possibleNamespaces.stream().collect(Collectors.joining("\n- "));
			return NamespaceResult
				.builder()
				.result(
					CriterionTestResult.failure(
						wmiCriterion,
						"None of the possible namespaces match the criterion:" + formattedNamespaceList
					)
				)
				.build();
		}

		// So, now we have a list of working namespaces.
		// We'd better have only one, but you never know, so try to be clever here...
		// If we have several matching namespaces, including root/cimv2, then exclude this one
		// because it's one that we find in many places and not necessarily with anything useful in it
		// especially if there are other matching namespaces.
		if (namespaces.size() > 1) {
			namespaces.remove("root/cimv2");
			namespaces.remove("root\\cimv2");
		}

		// Okay, so even if we have several, select a single one
		final String detectedNamespace = namespaces.keySet().stream().findFirst().orElseThrow();

		return NamespaceResult.builder().namespace(detectedNamespace).result(namespaces.get(detectedNamespace)).build();
	}

	/**
	 * Find the namespace to use for the execution of the given {@link WmiCriterion}.
	 *
	 * @param telemetryManager The telemetry manager providing access to host configuration and WMI credentials.
	 * @param hostname         The hostname of the device
	 * @param winConfiguration The WMI protocol configuration (credentials, etc.)
	 * @param wmiCriterion     The WMI criterion with an "Automatic" namespace
	 * @return A {@link CriterionTestResult} telling whether we found the proper namespace for the specified WQL
	 */
	CriterionTestResult findNamespace(
		final String connectorId,
		final TelemetryManager telemetryManager,
		final String hostname,
		final IWinConfiguration winConfiguration,
		final WmiCriterion wqlCriterion
	) {
		// Get the list of possible namespaces on this host
		Set<String> possibleWmiNamespaces = telemetryManager.getHostProperties().getPossibleWmiNamespaces();

		// Only one thread at a time must be figuring out the possible namespaces on a given host
		synchronized (possibleWmiNamespaces) {
			if (possibleWmiNamespaces.isEmpty()) {
				// If we don't have this list already, figure it out now
				final PossibleNamespacesResult possibleWmiNamespacesResult = findPossibleNamespaces(hostname, winConfiguration);

				// If we can't detect the namespace then we must stop
				if (!possibleWmiNamespacesResult.isSuccess()) {
					return CriterionTestResult.error(wqlCriterion, possibleWmiNamespacesResult.getErrorMessage());
				}

				// Store the list of possible namespaces in HostMonitoring, for next time we need it
				possibleWmiNamespaces.clear();
				possibleWmiNamespaces.addAll(possibleWmiNamespacesResult.getPossibleNamespaces());
			}
		}

		// Perform a namespace detection
		NamespaceResult namespaceResult = detectNamespace(
			hostname,
			winConfiguration,
			wqlCriterion,
			Collections.unmodifiableSet(possibleWmiNamespaces)
		);

		// If that was successful, remember it in HostMonitoring, so we don't perform this
		// (costly) detection again
		if (namespaceResult.getResult().isSuccess()) {
			telemetryManager
				.getHostProperties()
				.getConnectorNamespace(connectorId)
				.setAutomaticWmiNamespace(namespaceResult.getNamespace());
		}

		return namespaceResult.getResult();
	}

	/**
	 * Find the possible WMI namespaces on specified hostname with specified credentials.
	 *
	 * @param hostname         The hostname of the device.
	 * @param winConfiguration Wmi configuration (credentials, timeout)
	 * @return A {@link PossibleNamespacesResult} wrapping the success state, the message in case of errors
	 * and the possibleWmiNamespaces {@link Set}.
	 */
	public PossibleNamespacesResult findPossibleNamespaces(
		final String hostname,
		final IWinConfiguration winConfiguration
	) {
		// If the user specified a namespace, we return it as if it was the only namespace available
		// and for which we're going to test our connector
		if (winConfiguration.getNamespace() != null && !winConfiguration.getNamespace().isBlank()) {
			return PossibleNamespacesResult
				.builder()
				.possibleNamespaces(Collections.singleton(winConfiguration.getNamespace()))
				.success(true)
				.build();
		}

		// Possible namespace will be stored in this set
		Set<String> possibleWmiNamespaces = new TreeSet<>();

		try {
			winRequestExecutor
				.executeWmi(hostname, winConfiguration, "SELECT Name FROM __NAMESPACE", "root")
				.stream()
				.filter(row -> !row.isEmpty())
				.map(row -> row.get(0))
				.filter(Objects::nonNull)
				.filter(namespace -> !namespace.isBlank())
				.filter(namespace -> !namespace.toLowerCase().contains("interop"))
				.filter(namespace -> !IGNORED_WMI_NAMESPACES.contains(namespace))
				.filter(namespace ->
					IGNORED_WMI_NAMESPACES
						.stream()
						.noneMatch(ignoredNamespace -> (ROOT_SLASH + ignoredNamespace).equalsIgnoreCase(namespace))
				)
				.filter(namespace ->
					IGNORED_WMI_NAMESPACES
						.stream()
						.noneMatch(ignoredNamespace -> ("root\\" + ignoredNamespace).equalsIgnoreCase(namespace))
				)
				.map(namespace -> ROOT_SLASH + namespace)
				.forEach(possibleWmiNamespaces::add);
		} catch (final Exception e) {
			// Get the cause in the exception
			Throwable cause = e.getCause();

			String message = String.format(
				"Hostname %s - Does not respond to WMI requests. %s: %s%nCancelling namespace detection.",
				hostname,
				cause != null ? cause.getClass().getSimpleName() : e.getClass().getSimpleName(),
				cause != null ? cause.getMessage() : e.getMessage()
			);

			log.debug(message);

			return PossibleNamespacesResult.builder().errorMessage(message).success(false).build();
		}

		if (possibleWmiNamespaces.isEmpty()) {
			return PossibleNamespacesResult
				.builder()
				.errorMessage("No suitable namespace could be found to query host " + hostname + ".")
				.success(false)
				.build();
		}

		return PossibleNamespacesResult.builder().possibleNamespaces(possibleWmiNamespaces).success(true).build();
	}

	/**
	 * Processes a {@link CommandLineCriterion} using the provided {@link TelemetryManager} to test
	 * command execution outcomes based on expected results. The method validates the criterion and,
	 * based on system properties, decides whether to proceed with command execution or not.
	 *
	 * @param commandLineCriterion The command line criterion to be evaluated.
	 * @param telemetryManager     Provides system configuration and properties for context.
	 * @return {@link CriterionTestResult} reflecting the outcome of the evaluation.
	 */
	public CriterionTestResult process(CommandLineCriterion commandLineCriterion, TelemetryManager telemetryManager) {
		if (commandLineCriterion == null) {
			return CriterionTestResult.error(commandLineCriterion, "Malformed CommandLine criterion.");
		}

		if (
			commandLineCriterion.getCommandLine().isEmpty() ||
			commandLineCriterion.getExpectedResult() == null ||
			commandLineCriterion.getExpectedResult().isEmpty()
		) {
			return CriterionTestResult.success(
				commandLineCriterion,
				"CommandLine or ExpectedResult are empty. Skipping this test."
			);
		}

		if (Boolean.FALSE.equals(commandLineCriterion.getExecuteLocally())) {
			return CriterionTestResult.error(
				commandLineCriterion,
				"The CommandLine criterion cannot be executed locally through WMI. Skipping this test."
			);
		}

		final boolean isLocalhost = telemetryManager.getHostProperties().isLocalhost();
		final DeviceKind hostType = telemetryManager.getHostConfiguration().getHostType();

		if (isLocalhost || hostType != DeviceKind.WINDOWS) {
			return CriterionTestResult.error(
				commandLineCriterion,
				String.format(
					"Cannot process CommandLine criterion for %s host of type %s.",
					isLocalhost ? "local" : "remote",
					hostType
				)
			);
		}

		try {
			final OsCommandResult osCommandResult = winCommandService.runOsCommand(
				commandLineCriterion.getCommandLine(),
				telemetryManager.getHostname(),
				configurationRetriever.apply(telemetryManager)
			);

			final CommandLineCriterion osCommandNoPassword = CommandLineCriterion
				.builder()
				.commandLine(osCommandResult.getNoPasswordCommand())
				.executeLocally(commandLineCriterion.getExecuteLocally())
				.timeout(commandLineCriterion.getTimeout())
				.expectedResult(commandLineCriterion.getExpectedResult())
				.build();

			final Matcher matcher = Pattern
				.compile(
					PslUtils.psl2JavaRegex(commandLineCriterion.getExpectedResult()),
					Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
				)
				.matcher(osCommandResult.getResult());

			return matcher.find()
				? CriterionTestResult.success(osCommandNoPassword, osCommandResult.getResult())
				: CriterionTestResult.failure(osCommandNoPassword, osCommandResult.getResult());
		} catch (NoCredentialProvidedException noCredentialProvidedException) {
			return CriterionTestResult.error(commandLineCriterion, noCredentialProvidedException.getMessage());
		} catch (Exception exception) { // NOSONAR on interruption
			return CriterionTestResult.error(commandLineCriterion, exception);
		}
	}

	/**
	 * Processes the given {@link ProcessCriterion} using the specified Windows configuration to evaluate
	 * if a process is running based on the command line provided. This method constructs a WMI query criterion,
	 * then executes a detection test against the localhost machine.
	 *
	 * @param processCriterion      The process criterion that specifies the command line to look for in the running processes.
	 * @param localWinConfiguration The Windows configuration to be used for the WMI query execution.
	 * @return A {@link CriterionTestResult} indicating the result of the detection test.
	 */
	public CriterionTestResult process(
		final ProcessCriterion processCriterion,
		final IWinConfiguration localWinConfiguration
	) {
		final WmiCriterion criterion = WmiCriterion
			.builder()
			.query("SELECT ProcessId,Name,ParentProcessId,CommandLine FROM Win32_Process")
			.namespace(WMI_DEFAULT_NAMESPACE)
			.expectedResult(processCriterion.getCommandLine())
			.build();

		return performDetectionTest(LOCALHOST, localWinConfiguration, criterion);
	}

	/**
	 * Processes an {@link IpmiCriterion} using the telemetry manager to perform a detection test
	 * based on the Windows management protocol configuration. This method retrieves the Windows
	 * configuration for the telemetry context, constructs a WMI query, and executes a detection test
	 * using a new WMI criterion.
	 *
	 * @param ipmiCriterion    The IPMI criterion to be tested.
	 * @param telemetryManager Provides host configuration and properties.
	 * @return {@link CriterionTestResult} indicating the result of the detection test, including success or error information.
	 */
	public CriterionTestResult process(final IpmiCriterion ipmiCriterion, TelemetryManager telemetryManager) {
		// Find the configured Windows protocol (WMI or WinRM)
		final IWinConfiguration winConfiguration = configurationRetriever.apply(telemetryManager);
		if (winConfiguration == null) {
			return CriterionTestResult.error(ipmiCriterion, NEITHER_WMI_NOR_WIN_RM_ERROR_MSG);
		}

		final WmiCriterion ipmiWmiCriterion = WmiCriterion.builder().query("SELECT Description FROM ComputerSystem").namespace("root\\hardware").build();

		return performDetectionTest(telemetryManager.getHostname(), winConfiguration, ipmiWmiCriterion);
	}

	/**
	 * Data class representing the result of querying for possible namespaces.
	 * Provides information about the possible namespaces, success status, and an error message if applicable.
	 */
	@Data
	@Builder
	public static class PossibleNamespacesResult {

		private Set<String> possibleNamespaces;
		private boolean success;
		private String errorMessage;
	}

	/**
	 * Data class representing the result for a specific namespace.
	 * Contains information about the namespace itself and a CriterionTestResult.
	 */
	@Data
	@Builder
	public static class NamespaceResult {

		private String namespace;
		private CriterionTestResult result;
	}

}
