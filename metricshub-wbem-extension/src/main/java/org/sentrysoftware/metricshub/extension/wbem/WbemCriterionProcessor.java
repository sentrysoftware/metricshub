package org.sentrysoftware.metricshub.extension.wbem;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub IWbem Extension
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

import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.TABLE_SEP;
import static org.sentrysoftware.metricshub.extension.wbem.WbemRequestExecutor.isAcceptableException;

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
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.sentrysoftware.metricshub.engine.common.exception.ClientException;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.WbemCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.WqlCriterion;
import org.sentrysoftware.metricshub.engine.strategy.detection.CriterionTestResult;
import org.sentrysoftware.metricshub.engine.strategy.source.SourceTable;
import org.sentrysoftware.metricshub.engine.strategy.utils.PslUtils;
import org.sentrysoftware.metricshub.engine.strategy.utils.WqlDetectionHelper;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

@Slf4j
@AllArgsConstructor
public class WbemCriterionProcessor {

	private static final String SELECT_NAME_FROM_CIM_NAMESPACE = "SELECT Name from CIM_Namespace";
	private static final Set<String> IGNORED_WBEM_NAMESPACES = Set.of("root", "/root");
	private static final String INTEROP_LOWER_CASE = "interop";
	private static final String ROOT_SLASH = "root/";

	static final List<WbemExtension.WqlQuery> WBEM_INTEROP_QUERIES = List.of(
		new WbemExtension.WqlQuery("SELECT Name FROM __NAMESPACE", "root"),
		new WbemExtension.WqlQuery(SELECT_NAME_FROM_CIM_NAMESPACE, "Interop"),
		new WbemExtension.WqlQuery(SELECT_NAME_FROM_CIM_NAMESPACE, "PG_Interop"),
		new WbemExtension.WqlQuery(SELECT_NAME_FROM_CIM_NAMESPACE, "root/Interop"),
		new WbemExtension.WqlQuery(SELECT_NAME_FROM_CIM_NAMESPACE, "root/PG_Interop"),
		new WbemExtension.WqlQuery(SELECT_NAME_FROM_CIM_NAMESPACE, INTEROP_LOWER_CASE)
	);

	private WbemRequestExecutor wbemRequestExecutor;

	/**
	 * Find the namespace to use for the execution of the given {@link WbemCriterion}.
	 *
	 * @param hostname          The hostname of the host device
	 * @param wbemConfiguration The WBEM protocol configuration (port, credentials, etc.)
	 * @param wbemCriterion     The WQL criterion with an "Automatic" namespace
	 * @return A {@link CriterionTestResult} telling whether we found the proper namespace for the specified WQL
	 */
	CriterionTestResult findNamespace(
		final String hostname,
		final WbemConfiguration wbemConfiguration,
		final WbemCriterion wbemCriterion,
		final TelemetryManager telemetryManager,
		final String connectorId
	) {
		// Get the list of possible namespaces on this host
		Set<String> possibleWbemNamespaces = telemetryManager.getHostProperties().getPossibleWbemNamespaces();

		// Only one thread at a time must be figuring out the possible namespaces on a given host
		synchronized (possibleWbemNamespaces) {
			if (possibleWbemNamespaces.isEmpty()) {
				// If we don't have this list already, figure it out now
				final WqlDetectionHelper.PossibleNamespacesResult possibleWbemNamespacesResult = findPossibleNamespaces(
					hostname,
					wbemConfiguration,
					telemetryManager
				);

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
		WqlDetectionHelper.NamespaceResult namespaceResult = detectNamespace(
			hostname,
			wbemConfiguration,
			wbemCriterion,
			Collections.unmodifiableSet(possibleWbemNamespaces),
			telemetryManager
		);

		// If that was successful, remember it in HostMonitoring, so we don't perform this
		// (costly) detection again
		if (namespaceResult.getResult().isSuccess()) {
			telemetryManager
				.getHostProperties()
				.getConnectorNamespace(connectorId)
				.setAutomaticWbemNamespace(namespaceResult.getNamespace());
		}

		return namespaceResult.getResult();
	}

	/**
	 * Detect the WBEM/WMI namespace applicable to the specified WBEM/WMI criterion.
	 * <br>
	 * The namespace in the criterion must be "Automatic".
	 * <br>
	 *
	 * @param hostname           The host name
	 * @param configuration      WBEM/WMI configuration (credentials, timeout)
	 * @param criterion          WQL detection properties (WQL, expected result, namespace must be "Automatic")
	 * @param possibleNamespaces The possible namespaces to execute the WQL on
	 * @return A {@link WqlDetectionHelper.NamespaceResult} wrapping the detected namespace
	 * and the error message if the detection fails.
	 */
	public WqlDetectionHelper.NamespaceResult detectNamespace(
		final String hostname,
		final WbemConfiguration configuration,
		final WqlCriterion criterion,
		final Set<String> possibleNamespaces,
		final TelemetryManager telemetryManager
	) {
		// Run the query on each namespace and check if the result match the criterion
		final Map<String, CriterionTestResult> namespaces = new TreeMap<>();
		final WqlCriterion tentativeCriterion = criterion.copy();

		// Loop over each namespace and run the WBEM query and check if the result matches
		for (final String namespace : possibleNamespaces) {
			// Update the criterion with the current namespace that needs to be tested
			tentativeCriterion.setNamespace(namespace);

			// Do the request
			CriterionTestResult testResult = performDetectionTest(
				hostname,
				configuration,
				tentativeCriterion,
				telemetryManager
			);

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

					return WqlDetectionHelper.NamespaceResult.builder().result(testResult).build();
				}
			}
		}

		// No namespace => failure
		if (namespaces.isEmpty()) {
			String formattedNamespaceList = possibleNamespaces.stream().collect(Collectors.joining("\n- "));
			return WqlDetectionHelper.NamespaceResult
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

		return WqlDetectionHelper.NamespaceResult
			.builder()
			.namespace(detectedNamespace)
			.result(namespaces.get(detectedNamespace))
			.build();
	}

	/**
	 * Find the possible WBEM namespaces using the configured {@link WbemConfiguration}.
	 *
	 * @param hostname      The hostname of the host device.
	 * @param configuration The user's configured {@link WbemConfiguration}.
	 * @return A {@link WqlDetectionHelper.PossibleNamespacesResult} wrapping the success state, the message in case of errors
	 * and the possibleWmiNamespaces {@link Set}.
	 */
	public WqlDetectionHelper.PossibleNamespacesResult findPossibleNamespaces(
		final String hostname,
		final WbemConfiguration configuration,
		final TelemetryManager telemetryManager
	) {
		// If the user specified a namespace, we return it as if it was the only namespace available
		// and for which we're going to test our connector
		if (configuration.getNamespace() != null && !configuration.getNamespace().isBlank()) {
			return WqlDetectionHelper.PossibleNamespacesResult
				.builder()
				.possibleNamespaces(Collections.singleton(configuration.getNamespace()))
				.success(true)
				.build();
		}

		// Possible namespace will be stored in this set
		Set<String> possibleWbemNamespaces = new TreeSet<>();

		// Try all "interop" queries that could retrieve a list of namespaces in this CIM server
		for (WbemExtension.WqlQuery interopQuery : WBEM_INTEROP_QUERIES) {
			try {
				wbemRequestExecutor
					.executeWbem(hostname, configuration, interopQuery.getWql(), interopQuery.getNamespace(), telemetryManager)
					.stream()
					.filter(row -> !row.isEmpty())
					.map(row -> row.get(0))
					.filter(Objects::nonNull)
					.filter(namespace -> !namespace.isBlank())
					.filter(namespace -> !namespace.toLowerCase().contains(INTEROP_LOWER_CASE))
					.filter(namespace -> !IGNORED_WBEM_NAMESPACES.contains(namespace))
					.map(namespace -> ROOT_SLASH + namespace)
					.forEach(namespace -> possibleWbemNamespaces.add(namespace));
			} catch (final ClientException e) {
				// If the CIM server doesn't know the requested class, we will get a WBEM exception
				// saying so. Such exceptions are okay and will not fail the detection.
				// That's why we return in failure if and only if the error type is neither "invalid namespace",
				// That's why we return in failure if and only if the error type is neither "invalid namespace",
				// nor "invalid class", nor "not found".

				if (!isAcceptableException(e)) {
					// This error indicates that the CIM server will probably never respond to anything
					// (timeout, or bad credentials), so there's no point in pursuing our efforts here.
					Throwable cause = e.getCause();
					final String messageFormat =
						"Hostname %s - Does not respond to WBEM requests. %s: %s\nCancelling namespace detection.";
					String message = String.format(
						messageFormat,
						hostname,
						cause != null ? cause.getClass().getSimpleName() : e.getClass().getSimpleName(),
						cause != null ? cause.getMessage() : e.getMessage()
					);

					log.debug(message);

					return WqlDetectionHelper.PossibleNamespacesResult.builder().errorMessage(message).success(false).build();
				}
			}
		}

		// No namespace love?
		if (possibleWbemNamespaces.isEmpty()) {
			return WqlDetectionHelper.PossibleNamespacesResult
				.builder()
				.errorMessage("No suitable namespace could be found to query host " + hostname + ".")
				.success(false)
				.build();
		}

		// Yay!
		return WqlDetectionHelper.PossibleNamespacesResult
			.builder()
			.possibleNamespaces(possibleWbemNamespaces)
			.success(true)
			.build();
	}

	/**
	 * Perform the specified WQL detection test, on the specified WBEM/WMI protocol configuration.
	 * <br>
	 * Note: "Automatic" namespace is not supported in this method.
	 * <br>
	 *
	 * @param hostname      Host name
	 * @param configuration WBEM/WMI configuration (credentials, timeout)
	 * @param criterion     WQL detection properties (WQL, namespace, expected result)
	 * @return {@link CriterionTestResult} which indicates if the check has succeeded or not.
	 */
	public CriterionTestResult performDetectionTest(
		final String hostname,
		@NonNull final WbemConfiguration configuration,
		@NonNull final WqlCriterion criterion,
		@NonNull final TelemetryManager telemetryManager
	) {
		// Make the WBEM query
		final List<List<String>> queryResult;
		try {
			queryResult =
				wbemRequestExecutor.executeWbem(
					hostname,
					configuration,
					criterion.getQuery(),
					criterion.getNamespace(),
					telemetryManager
				);
		} catch (ClientException e) {
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
}
