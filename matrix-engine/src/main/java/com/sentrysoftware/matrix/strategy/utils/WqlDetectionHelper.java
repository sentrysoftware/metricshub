package com.sentrysoftware.matrix.strategy.utils;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.TABLE_SEP;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.sentrysoftware.javax.wbem.WBEMException;
import com.sentrysoftware.matrix.common.exception.MatsyaException;
import com.sentrysoftware.matrix.configuration.IConfiguration;
import com.sentrysoftware.matrix.configuration.IWinConfiguration;
import com.sentrysoftware.matrix.configuration.WbemConfiguration;
import com.sentrysoftware.matrix.connector.model.identity.criterion.WqlCriterion;
import com.sentrysoftware.matrix.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.strategy.detection.CriterionTestResult;
import com.sentrysoftware.matrix.strategy.source.SourceTable;
import com.sentrysoftware.matsya.exceptions.WqlQuerySyntaxException;
import com.sentrysoftware.matsya.wmi.exceptions.WmiComException;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WqlDetectionHelper {


	private static final String INTEROP_LOWER_CASE = "interop";

	private static final String SELECT_NAME_FROM_CIM_NAMESPACE = "SELECT Name from CIM_Namespace";

	private static final String ROOT_SLASH = "root/";

	private MatsyaClientsExecutor matsyaClientsExecutor;

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

	private static final List<WqlQuery> WBEM_INTEROP_QUERIES = List.of(
			new WqlQuery("SELECT Name FROM __NAMESPACE", "root"),
			new WqlQuery(SELECT_NAME_FROM_CIM_NAMESPACE, "Interop"),
			new WqlQuery(SELECT_NAME_FROM_CIM_NAMESPACE, "PG_Interop"),
			new WqlQuery(SELECT_NAME_FROM_CIM_NAMESPACE, "root/Interop"),
			new WqlQuery(SELECT_NAME_FROM_CIM_NAMESPACE, "root/PG_Interop"),
			new WqlQuery(SELECT_NAME_FROM_CIM_NAMESPACE, INTEROP_LOWER_CASE)
	);

	private static final Set<String> IGNORED_WBEM_NAMESPACES = Set.of("root", "/root");

	public WqlDetectionHelper(final MatsyaClientsExecutor matsyaClientsExecutor) {
		this.matsyaClientsExecutor = matsyaClientsExecutor;
	}

	/**
	 * Find the possible WBEM namespaces using the configured {@link WbemConfiguration}.
	 *
	 * @param hostname      The hostname of the host device.
	 * @param configuration The user's configured {@link WbemConfiguration}.
	 * @return A {@link PossibleNamespacesResult} wrapping the success state, the message in case of errors
	 * and the possibleWmiNamespaces {@link Set}.
	 */
	public PossibleNamespacesResult findPossibleNamespaces(final String hostname, final WbemConfiguration configuration) { // NOSONAR

		// If the user specified a namespace, we return it as if it was the only namespace available
		// and for which we're going to test our connector
		if (configuration.getNamespace() != null && !configuration.getNamespace().isBlank()) {
			return PossibleNamespacesResult
				.builder()
				.possibleNamespaces(Collections.singleton(configuration.getNamespace()))
				.success(true)
				.build();
		}

		// Possible namespace will be stored in this set
		Set<String> possibleWbemNamespaces = new TreeSet<>();

		// Try all "interop" queries that could retrieve a list of namespaces in this CIM server
		for (WqlQuery interopQuery : WBEM_INTEROP_QUERIES) {

			try {

				matsyaClientsExecutor.executeWbem(
							hostname,
							configuration,
							interopQuery.getWql(),
							interopQuery.getNamespace()
						).stream()
						.filter(row -> !row.isEmpty())
						.map(row -> row.get(0))
						.filter(Objects::nonNull)
						.filter(namespace -> !namespace.isBlank())
						.filter(namespace -> !namespace.toLowerCase().contains(INTEROP_LOWER_CASE))
						.filter(namespace -> !IGNORED_WBEM_NAMESPACES.contains(namespace))
						.map(namespace -> ROOT_SLASH + namespace)
						.forEach(namespace -> possibleWbemNamespaces.add(namespace));

			} catch (final MatsyaException e) {

				// If the CIM server doesn't know the requested class, we will get a WBEM exception
				// saying so. Such exceptions are okay and will not fail the detection.
				// That's why we return in failure if and only if the error type is neither "invalid namespace",
				// nor "invalid class", nor "not found".

				if (!isAcceptableException(e)) {

					// This error indicates that the CIM server will probably never respond to anything
					// (timeout, or bad credentials), so there's no point in pursuing our efforts here.
					Throwable cause = e.getCause();
					String message = String.format( // NOSONAR on \n
							"Hostname %s - Does not respond to WBEM requests. %s: %s\nCancelling namespace detection.",
							hostname,
							cause != null ? cause.getClass().getSimpleName() : e.getClass().getSimpleName(),
							cause != null ? cause.getMessage() : e.getMessage()
					);

					log.debug(message);

					return PossibleNamespacesResult
						.builder()
						.errorMessage(message)
						.success(false)
						.build();
				}

			}
		}

		// No namespace love?
		if (possibleWbemNamespaces.isEmpty()) {

			return PossibleNamespacesResult
				.builder()
				.errorMessage("No suitable namespace could be found to query host " + hostname + ".")
				.success(false)
				.build();
		}

		// Yay!
		return PossibleNamespacesResult
			.builder()
			.possibleNamespaces(possibleWbemNamespaces)
			.success(true)
			.build();
	}


	/**
	 * Find the possible WMI namespaces on specified hostname with specified credentials.
	 *
	 * @param hostname      The hostname of the device.
	 * @param configuration Win configuration (credentials, timeout)
	 * @return A {@link PossibleNamespacesResult} wrapping the success state, the message in case of errors
	 * and the possibleWmiNamespaces {@link Set}.
	 */
	public PossibleNamespacesResult findPossibleNamespaces(final String hostname, final IWinConfiguration configuration) {

		// If the user specified a namespace, we return it as if it was the only namespace available
		// and for which we're going to test our connector
		if (configuration.getNamespace() != null && !configuration.getNamespace().isBlank()) {
			return PossibleNamespacesResult
				.builder()
				.possibleNamespaces(Collections.singleton(configuration.getNamespace()))
				.success(true)
				.build();
		}

		// Possible namespace will be stored in this set
		Set<String> possibleWmiNamespaces = new TreeSet<>();

		try {

			matsyaClientsExecutor.executeWql(
					hostname,
					configuration,
					"SELECT Name FROM __NAMESPACE",
					"root"
				)
				.stream()
				.filter(row -> !row.isEmpty())
				.map(row -> row.get(0))
				.filter(Objects::nonNull)
				.filter(namespace -> !namespace.isBlank())
				.filter(namespace -> !namespace.toLowerCase().contains(INTEROP_LOWER_CASE))
				.filter(namespace -> !IGNORED_WMI_NAMESPACES.contains(namespace))
				.filter(namespace -> IGNORED_WMI_NAMESPACES.stream().noneMatch(ignoredNamespace -> (ROOT_SLASH + ignoredNamespace).equalsIgnoreCase(namespace)))
				.filter(namespace -> IGNORED_WMI_NAMESPACES.stream().noneMatch(ignoredNamespace -> ("root\\" + ignoredNamespace).equalsIgnoreCase(namespace)))
				.map(namespace -> ROOT_SLASH + namespace)
				.forEach(possibleWmiNamespaces::add);

		} catch (final MatsyaException e) {

			// Get the cause in the exception
			Throwable cause = e.getCause();

			String message = String.format( // NOSONAR on \n
				"Hostname %s - Does not respond to WMI requests. %s: %s\nCancelling namespace detection.",
				hostname,
				cause != null ? cause.getClass().getSimpleName() : e.getClass().getSimpleName(),
				cause != null ? cause.getMessage() : e.getMessage()
			);

			log.debug(message);

			return PossibleNamespacesResult
				.builder()
				.errorMessage(message)
				.success(false)
				.build();

		}

		if (possibleWmiNamespaces.isEmpty()) {

			return PossibleNamespacesResult
				.builder()
				.errorMessage("No suitable namespace could be found to query host " + hostname + ".")
				.success(false)
				.build();
		}

		return PossibleNamespacesResult
			.builder()
			.possibleNamespaces(possibleWmiNamespaces)
			.success(true)
			.build();
	}


	/**
	 * Detect the WBEM/WMI namespace applicable to the specified WBEM/WMI criterion.
	 * <p>
	 * The namespace in the criterion must be "Automatic".
	 * <p>
	 *
	 * @param hostname           The host name
	 * @param configuration      WBEM/WMI configuration (credentials, timeout)
	 * @param criterion          WQL detection properties (WQL, expected result, namespace must be "Automatic")
	 * @param possibleNamespaces The possible namespaces to execute the WQL on
	 * @return A {@link NamespaceResult} wrapping the detected namespace
	 * and the error message if the detection fails.
	 */
	public NamespaceResult detectNamespace(
		final String hostname,
		final IConfiguration configuration,
		final WqlCriterion criterion,
		final Set<String> possibleNamespaces
	) {

		// Run the query on each namespace and check if the result match the criterion
		final Map<String, CriterionTestResult> namespaces = new TreeMap<>();
		final WqlCriterion tentativeCriterion = criterion.copy();

		// Loop over each namespace and run the WBEM query and check if the result matches
		for (final String namespace : possibleNamespaces) {

			// Update the criterion with the current namespace that needs to be tested
			tentativeCriterion.setNamespace(namespace);

			// Do the request
			CriterionTestResult testResult = performDetectionTest(hostname, configuration, tentativeCriterion);

			// If the result matched then the namespace is selected
			if (testResult.isSuccess()) {

				namespaces.put(namespace, testResult);

			} else {

				// If the test failed with an exception, we probably don't need to go further
				Throwable e = testResult.getException();
				if (e != null && !isAcceptableException(e)) {

					// This error indicates that the CIM server will probably never respond to anything
					// (timeout, or bad credentials), so there's no point in pursuing our efforts here.
					log.debug(
						"Hostname %s - Does not respond to %s requests. %s: %s\nCancelling namespace detection.",
						hostname,
						criterion.getClass().getSimpleName(),
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
						criterion,
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

		return NamespaceResult
			.builder()
			.namespace(detectedNamespace)
			.result(namespaces.get(detectedNamespace))
			.build();
	}


	/**
	 * Perform the specified WQL detection test, on the specified WBEM/WMI protocol configuration.
	 * <p>
	 * Note: "Automatic" namespace is not supported in this method.
	 * <p>
	 *
	 * @param hostname      Host name
	 * @param configuration WBEM/WMI configuration (credentials, timeout)
	 * @param criterion     WQL detection properties (WQL, namespace, expected result)
	 * @return {@link CriterionTestResult} which indicates if the check has succeeded or not.
	 */
	public CriterionTestResult performDetectionTest(
			final String hostname,
			@NonNull final IConfiguration configuration,
			@NonNull final WqlCriterion criterion
	) {

		// Make the WBEM query
		final List<List<String>> queryResult;
		try {
			queryResult = matsyaClientsExecutor.executeWql(
				hostname,
				configuration,
				criterion.getQuery(),
				criterion.getNamespace()
			);
		} catch (MatsyaException e) {
			return CriterionTestResult.error(criterion, e);
		}

		// Serialize the result as a CSV
		String actualResult = SourceTable.tableToCsv(queryResult, TABLE_SEP, true);

		// Empty result? ==> failure
		if (actualResult == null || actualResult.isBlank()) {
			return CriterionTestResult.failure(criterion, "No result.");
		}

		// No expected result (and non-empty result)? ==> success
		if (criterion.getExpectedResult() == null || criterion.getExpectedResult().isBlank()) {
			return CriterionTestResult.success(criterion, actualResult);
		}

		// Search for the expected result
		final Matcher matcher = Pattern
			.compile(PslUtils.psl2JavaRegex(criterion.getExpectedResult()), Pattern.CASE_INSENSITIVE | Pattern.MULTILINE)
			.matcher(actualResult);

		// If the expected result is found ==> success
		if (matcher.find()) {
			return CriterionTestResult.success(criterion, matcher.group());
		}

		// No match!
		return CriterionTestResult.failure(criterion, actualResult);
	}


	/**
	 * Assess whether an exception (or any of its causes) is simply an error saying that the
	 * requested namespace of class doesn't exist, which is considered okay.
	 * <p>
	 *
	 * @param t Exception to verify
	 * @return whether specified exception is acceptable while performing namespace detection
	 */
	public static boolean isAcceptableException(Throwable t) {

		if (t == null) {
			return false;
		}

		if (t instanceof WBEMException wbemException) {
			final int cimErrorType = wbemException.getID();
			return cimErrorType == WBEMException.CIM_ERR_INVALID_NAMESPACE
					|| cimErrorType == WBEMException.CIM_ERR_INVALID_CLASS
					|| cimErrorType == WBEMException.CIM_ERR_NOT_FOUND;
		} else if (t instanceof WmiComException) {
			final String message = t.getMessage();
			return message != null && (
					message.contains("WBEM_E_NOT_FOUND")
							|| message.contains("WBEM_E_INVALID_NAMESPACE")
							|| message.contains("WBEM_E_INVALID_CLASS")
			);
		} else if (t instanceof WqlQuerySyntaxException) {
			return true;
		}

		// Now check recursively the cause
		return isAcceptableException(t.getCause());
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
		private CriterionTestResult result;
	}

	/**
	 * Represents a WQL Query (i.e. a query in a namespace)
	 */
	@Data
	private static class WqlQuery {
		private String wql;
		private String namespace;

		public WqlQuery(final String wql, final String namespace) {
			this.wql = wql;
			this.namespace = namespace;
		}
	}

}
