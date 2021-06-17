package com.sentrysoftware.matrix.engine.strategy.detection;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.EMPTY;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.protocol.HTTPProtocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sentrysoftware.matrix.common.exception.LocalhostCheckException;
import com.sentrysoftware.matrix.connector.model.Connector;
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
import com.sentrysoftware.matrix.engine.protocol.IProtocolConfiguration;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol;
import com.sentrysoftware.matrix.engine.protocol.WMIProtocol;
import com.sentrysoftware.matrix.engine.strategy.StrategyConfig;
import com.sentrysoftware.matrix.engine.strategy.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.engine.strategy.source.SourceTable;
import com.sentrysoftware.matrix.engine.strategy.utils.PslUtils;
import com.sentrysoftware.matsya.wmi.exceptions.WmiComException;
import com.sentrysoftware.matsya.wmi.exceptions.WmiWqlQuerySyntaxException;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CriterionVisitor implements ICriterionVisitor {

	private static final String NAMESPACE_MESSAGE = "\n- Namespace: ";

	private static final Pattern SNMP_GETNEXT_RESULT_REGEX = Pattern.compile("\\w+\\s+\\w+\\s+(.*)");
	private static final String EXPECTED_VALUE_RETURNED_VALUE = "Expected value: %s - returned value %s.";

	private static final Map<String, String> WMI_INTEROPERABILITY_NAMESPACES;
	private static final Set<String> IGNORED_WMI_NAMESPACES;
	static {
		WMI_INTEROPERABILITY_NAMESPACES = Map.of(
					"root", "__NAMESPACE"
				);

		IGNORED_WMI_NAMESPACES = Set.of("SECURITY",
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
	}

	@Autowired
	private StrategyConfig strategyConfig;

	@Autowired
	private MatsyaClientsExecutor matsyaClientsExecutor;

	@Override
	public CriterionTestResult visit(final HTTP criterion) {

		if (null == criterion) {
			return CriterionTestResult.empty();
		}

		EngineConfiguration engineConfiguration = strategyConfig.getEngineConfiguration();

		HTTPProtocol protocol = (HTTPProtocol) engineConfiguration
			.getProtocolConfigurations()
			.get(HTTPProtocol.class);

		if (protocol == null) {
			return CriterionTestResult.empty();
		}

		final String hostname = engineConfiguration
			.getTarget()
			.getHostname();

		final String result = matsyaClientsExecutor.executeHttp(criterion, protocol, hostname, false);

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
	private TestResult checkHttpResult(String hostname, String result, String expectedResult) {

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

			Pattern pattern = Pattern.compile(expectedResult);
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
		// Not implemented yet
		return CriterionTestResult.empty();
	}

	@Override
	public CriterionTestResult visit(final KMVersion kmVersion) {
		// Not implemented yet
		return CriterionTestResult.empty();
	}

	@Override
	public CriterionTestResult visit(final OS os) {
		// Not implemented yet
		return CriterionTestResult.empty();
	}

	@Override
	public CriterionTestResult visit(final OSCommand osCommand) {
		// Not implemented yet
		return CriterionTestResult.empty();
	}

	@Override
	public CriterionTestResult visit(final Process process) {
		// Not implemented yet
		return CriterionTestResult.empty();
	}

	@Override
	public CriterionTestResult visit(final Service service) {
		// Not implemented yet
		return CriterionTestResult.empty();
	}

	@Override
	public CriterionTestResult visit(final SNMPGet snmpGet) {
		if (null == snmpGet || snmpGet.getOid() == null) {
			log.error("Malformed SNMPGet criterion {}. Cannot process SNMPGet detection.", snmpGet);
			return CriterionTestResult.empty();
		}

		final Optional<IProtocolConfiguration> snmpProtocolOpt = getSnmpProtocol();

		if (!snmpProtocolOpt.isPresent()) {
			return CriterionTestResult.empty();
		}

		final SNMPProtocol protocol = (SNMPProtocol) snmpProtocolOpt.get();
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

		} catch (Exception e) {
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
	private TestResult checkSNMPGetResult(final String hostname, String oid, String expected, String result) {
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
		// Not implemented yet
		return CriterionTestResult.empty();
	}

	@Override
	public CriterionTestResult visit(final WMI wmi) {

		if (wmi == null || wmi.getWbemQuery() == null) {
			log.error("Malformed WMI criterion {}. Cannot process WMI detection.", wmi);
			return CriterionTestResult.empty();
		}

		final Optional<IProtocolConfiguration> wmiProtocolOpt = Optional.ofNullable(strategyConfig.getEngineConfiguration()
				.getProtocolConfigurations().get(WMIProtocol.class));

		if (!wmiProtocolOpt.isPresent()) {
			log.debug("The WMI Credentials are not configured. Cannot process WMI detection {}.",
					wmi);
			return CriterionTestResult.empty();
		}

		final WMIProtocol protocol = (WMIProtocol) wmiProtocolOpt.get();

		// Find the namespace
		final NamespaceResult namespaceResult = findNamespace(wmi, protocol);

		// Stop if no namespace is found
		if (!namespaceResult.isSuccess()) {
			return CriterionTestResult.builder()
					.success(false)
					.message(buildNoWmiNamespaceErrorMessage(wmi, namespaceResult))
					.build();
		}

		final String hostname = strategyConfig.getEngineConfiguration().getTarget().getHostname();

		try {

			// Run the WMI query
			final String csvTable = runWmiQueryAndGetCsv(hostname, wmi.getWbemQuery(), namespaceResult.getNamespace(), protocol);

			// Test the result
			final TestResult testResult = getMatchingWmiResult(wmi, namespaceResult.getNamespace(), csvTable);

			return CriterionTestResult.builder()
					.success(testResult.isSuccess())
					.message(testResult.getMessage())
					.result(csvTable)
					.build();

		} catch (Exception e) {
			final String message = String.format(
					"WMI Test Failed - WMI Criterion query %s on %s was unsuccessful due to an exception. Message: %s.",
					wmi.getWbemQuery(), hostname, e.getMessage());
			log.debug(message, e);
			return CriterionTestResult.builder().message(message).build();
		}
		
	}

	/**
	 * Try to find the matching line that matches the expected result in defined in the given {@link WMI} criterion
	 * 
	 * @param wmi       The WMI criterion
	 * @param namespace The WMI namespace
	 * @param csvTable  The WMI result as csv
	 * @return {@link TestResult} which indicates if the check has succeeded or not. TestResult will also wraps the message to set in the
	 *         testReport parameter
	 */
	static TestResult getMatchingWmiResult(final WMI wmi, final String namespace, final String csvTable) {

		// Not result? success = false
		if (csvTable.isEmpty()) {
			return TestResult.builder()
				.success(false)
				.message(buildWmiEmptyResultErrorMessage(wmi, namespace))
			.build();
		}

		final String expected = wmi.getExpectedResult() != null ? wmi.getExpectedResult() : EMPTY;

		final Pattern pattern = Pattern.compile(PslUtils.psl2JavaRegex(expected), Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

		final Matcher matcher = pattern.matcher(csvTable);

		// If the result is found then success = true
		if (matcher.find()) {

			return TestResult
					.builder()
					.success(true)
					.message(buildWmiSuccessMessage(wmi.getWbemQuery(),
							namespace,
							wmi.getExpectedResult(),
							matcher.group()))
					.build();
		}

		// Unfortunately success is false as there is no matched result
		return TestResult
				.builder()
				.success(false)
				.message(buildWmiFailedMessage(wmi.getWbemQuery(), namespace, wmi.getExpectedResult()))
				.build();
	}

	/**
	 * Build the WMI failed message
	 * 
	 * @param wbemQuery      The executed WBEM query
	 * @param namespace      The WMI namespace
	 * @param expected       The expected result
	 * @return {@link String} value
	 */
	static String buildWmiFailedMessage(final String wbemQuery, final String namespace, final String expected) {

		final StringBuilder message = new StringBuilder("WMI Test Failed - The following WMI query succeeded but its result did not match the expected output:\n- WMI query: ")
				.append(wbemQuery)
				.append(NAMESPACE_MESSAGE)
				.append(namespace);

		appendExpected(message, expected);

		return message.toString();
	}

	/**
	 * Build the WMI success message
	 * 
	 * @param wbemQuery      The executed WBEM query
	 * @param namespace      The WMI namespace
	 * @param expected       The expected result
	 * @param matchingResult The matching WMI result
	 * @return {@link String} value
	 */
	static String buildWmiSuccessMessage(final String wbemQuery, final String namespace,
			final String expected, final String matchingResult) {
		final StringBuilder message = new StringBuilder();

		message
			.append("The following WMI query succeeded: \n- WQL query: ")
			.append(wbemQuery)
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
	static void appendExpected(final StringBuilder message, String expected) {
		if (expected != null) {
			message.append("\n- Expected result: ").append(expected);
		}
	}

	/**
	 * When the WMI test returns an empty result this message will be set in the testReport parameter
	 * 
	 * @param wmi       The WMI criterion
	 * @param namespace The namespace used in the detection
	 * @return {@link String} value
	 */
	static String buildWmiEmptyResultErrorMessage(final WMI wmi, final String namespace) {
		final StringBuilder message = new StringBuilder()
				.append("WMI Test Failed - The following WMI query succeeded but did not have any result:\n- WQL query: ")
				.append(wmi.getWbemQuery())
				.append(NAMESPACE_MESSAGE)
				.append(namespace);

		appendExpected(message, wmi.getExpectedResult());

		return message.toString();
	}

	/**
	 * Build the error message used which will be set in the test report paramter later by {@link DetectionOperation} in case we can't detect
	 * the namespace
	 * 
	 * @param wmi             The WMI criterion
	 * @param namespaceResult The {@link NamespaceResult} defining the exact error message to append
	 * @return {@link String} value
	 */
	String buildNoWmiNamespaceErrorMessage(final WMI wmi, final NamespaceResult namespaceResult) {

		final StringBuilder message = new StringBuilder("WMI Test Failed - The following WMI query failed:\n- WQL query: ")
			.append(wmi.getWbemQuery());

		appendExpected(message, wmi.getExpectedResult());

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

		if ("automatic".equalsIgnoreCase(criterionNamespace)) {
			final String automaticWmiNamespace = strategyConfig.getHostMonitoring().getAutomaticWmiNamespace();

			// It's OK if the namespace has already been detected, we don't re-execute the heavy detection
			return automaticWmiNamespace != null ? 
					NamespaceResult.builder().namespace(automaticWmiNamespace).success(true).build()
					: detectWmiNamespace(wmi, protocol);
		} else {
			// Not automatic, then it is provided by the connector otherwise we get the one from the configuration
			String namespace = criterionNamespace != null ? criterionNamespace : protocol.getNamespace();
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
			namespaces.remove("root/cimv2");
			namespaces.remove("root\\cimv2");
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
				if (isMatchingWmiResult(wmi.getExpectedResult(), csvTable)) {
					namespaces.add(namespace);
				}

			} catch (Exception e) {
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
	static boolean isMatchingWmiResult(String expected, final String csvTable) {

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
	 * @param hostname  The target hostname
	 * @param wbemQuery The WQL to execute
	 * @param namespace The WMI namespace
	 * @param protocol  The User's configured credentials
	 * @return String value
	 * @throws LocalhostCheckException
	 * @throws WmiComException
	 * @throws TimeoutException
	 * @throws WmiWqlQuerySyntaxException
	 */
	String runWmiQueryAndGetCsv(final String hostname, final String wbemQuery, final String namespace, final WMIProtocol protocol)
			throws LocalhostCheckException, WmiComException, TimeoutException, WmiWqlQuerySyntaxException {

		final List<List<String>> queryResult = matsyaClientsExecutor.executeWmi(hostname,
				protocol.getUsername(),
				protocol.getPassword(),
				protocol.getTimeout(),
				wbemQuery,
				namespace);

		return SourceTable.tableToCsv(queryResult, ";");

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

		// Test each interoperability namespace
		for (Entry<String, String> entry : WMI_INTEROPERABILITY_NAMESPACES.entrySet()) {

			final String clazz = entry.getValue();
			final String namespace = entry.getKey();
			final String wbemQuery = "SELECT Name FROM " + clazz;
			try {
				// Do the request
				final List<List<String>> queryResult = matsyaClientsExecutor.executeWmi(hostname,
						protocol.getUsername(),
						protocol.getPassword(),
						protocol.getTimeout(),
						wbemQuery,
						namespace);

				// Add the result of this request to possibleWmiNamespaces
				// This will update the possibleWmiNamespace in the HostMonitoring
				possibleWmiNamespaces.addAll(extractPossibleNamespaces(queryResult));

			} catch (Exception e) {
				// Log the error message and proceed with the next namespace
				final String message = String.format("%s does not respond to WMI request %s. Cancelling namespace detection. Error: %s",
						hostname, wbemQuery, e.getMessage());
				log.debug(message);
			}
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
	 * Extract the possible namespaces from the given query result. We expect a query result with multiple lines and only one column defining
	 * the namespace value. <br>
	 * The namespace is selected only if it is not from the <em>interop</em> family and it is not flagged as ignored.
	 * 
	 * @param namespaceQueryResult The result which should return a collection of namespaces.
	 * @return {@link Set} of namespace values sorted according to the natural ordering of its elements.
	 */
	static Set<String> extractPossibleNamespaces(final List<List<String>> namespaceQueryResult) {

		return namespaceQueryResult.stream()
				.filter(line -> !line.isEmpty())
				.flatMap(List::stream)
				.filter(value -> !value.toLowerCase().contains("interop")
						&& !IGNORED_WMI_NAMESPACES.contains(value))
				.map(value -> "root\\" + value)
				.collect(Collectors.toCollection(TreeSet::new));
	}

	@Override
	public CriterionTestResult visit(final SNMPGetNext snmpGetNext) {

		if (snmpGetNext == null || snmpGetNext.getOid() == null) {
			log.error("Malformed SNMPGetNext criterion {}. Cannot process SNMPGetNext detection.", snmpGetNext);
			return CriterionTestResult.empty();
		}

		final Optional<IProtocolConfiguration> snmpProtocolOpt = getSnmpProtocol();

		if (!snmpProtocolOpt.isPresent()) {
			return CriterionTestResult.empty();
		}

		final SNMPProtocol protocol = (SNMPProtocol) snmpProtocolOpt.get();
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

		} catch (Exception e) {
			final String message = String.format(
					"SNMP Test Failed - SNMP GetNext of %s on %s was unsuccessful due to an exception. Message: %s.",
					snmpGetNext.getOid(), hostname, e.getMessage());
			log.debug(message, e);
			return CriterionTestResult.builder().message(message).build();
		}
	}

	/**
	 * 
	 * @return The {@link SNMPProtocol} if any
	 */
	private Optional<IProtocolConfiguration> getSnmpProtocol() {
		return Optional.ofNullable(strategyConfig.getEngineConfiguration()
				.getProtocolConfigurations().get(SNMPProtocol.class));
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
				success = true;
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
	}
}
