package org.sentrysoftware.metricshub.extension.snmpv3;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub SNMP V3 Extension
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

import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.sentrysoftware.metricshub.engine.common.helpers.LoggingHelper;
import org.sentrysoftware.metricshub.engine.common.helpers.TextTableHelper;
import org.sentrysoftware.metricshub.engine.common.helpers.ThreadHelper;
import org.sentrysoftware.metricshub.extension.snmp.ISnmpConfiguration;
import org.sentrysoftware.metricshub.extension.snmp.ISnmpRequestExecutor;
import org.sentrysoftware.snmp.client.SnmpClient;

/**
 * The SnmpRequestExecutor class provides utility methods for executing
 * various SNMP requests on a remote hosts.
 */
@Slf4j
public class SnmpV3RequestExecutor implements ISnmpRequestExecutor {

	/**
	 * Execute SNMP GetNext request
	 *
	 * @param oid            The Object Identifier (OID) for the SNMP GETNEXT request.
	 * @param configuration  The SNMP configuration specifying parameters like version, community, etc.
	 * @param hostname       The hostname or IP address of the SNMP-enabled device.
	 * @param logMode        A boolean indicating whether to log errors and warnings during execution.
	 * @return The SNMP response as a String value.
	 * @throws InterruptedException If the execution is interrupted.
	 * @throws ExecutionException  If an exception occurs during execution.
	 * @throws TimeoutException    If the execution times out.
	 */
	@WithSpan("SNMP V3 Get Next")
	@Override
	public String executeSNMPGetNext(
		@NonNull @SpanAttribute("snmp.oid") final String oid,
		@NonNull @SpanAttribute("snmp.config") final ISnmpConfiguration configuration,
		@NonNull @SpanAttribute("host.hostname") final String hostname,
		final boolean logMode
	) throws InterruptedException, ExecutionException, TimeoutException {
		LoggingHelper.trace(() -> log.trace("Executing SNMP GetNext request:\n- OID: {}\n", oid));

		final long startTime = System.currentTimeMillis();

		String result = executeSNMPGetRequest(
			SnmpGetRequest.GETNEXT,
			oid,
			(SnmpV3Configuration) configuration,
			hostname,
			null,
			logMode
		);

		final long responseTime = System.currentTimeMillis() - startTime;

		LoggingHelper.trace(() ->
			log.trace(
				"Executed SNMP GetNext request:\n- OID: {}\n- Result: {}\n- response-time: {}\n",
				oid,
				result,
				responseTime
			)
		);

		return result;
	}

	/**
	 * Execute SNMP Get request
	 *
	 * @param oid            The Object Identifier (OID) for the SNMP GET request.
	 * @param configuration  The SNMP configuration specifying parameters like version, community, etc.
	 * @param hostname       The hostname or IP address of the SNMP-enabled device.
	 * @param logMode        A boolean indicating whether to log errors and warnings during execution.
	 * @return The SNMP response as a String value.
	 * @throws InterruptedException If the execution is interrupted.
	 * @throws ExecutionException  If an exception occurs during execution.
	 * @throws TimeoutException    If the execution times out.
	 */
	@WithSpan("SNMP V3 Get")
	@Override
	public String executeSNMPGet(
		@NonNull @SpanAttribute("snmp.oid") final String oid,
		@NonNull @SpanAttribute("snmp.config") final ISnmpConfiguration configuration,
		@NonNull @SpanAttribute("host.hostname") final String hostname,
		final boolean logMode
	) throws InterruptedException, ExecutionException, TimeoutException {
		LoggingHelper.trace(() -> log.trace("Executing SNMP V3 Get request:\n- OID: {}\n", oid));

		final long startTime = System.currentTimeMillis();

		String result = executeSNMPGetRequest(
			SnmpGetRequest.GET,
			oid,
			(SnmpV3Configuration) configuration,
			hostname,
			null,
			logMode
		);

		final long responseTime = System.currentTimeMillis() - startTime;

		LoggingHelper.trace(() ->
			log.trace(
				"Executed SNMP V3 Get request:\n- OID: {}\n- Result: {}\n- response-time: {}\n",
				oid,
				result,
				responseTime
			)
		);

		return result;
	}

	/**
	 * Execute SNMP Table
	 *
	 * @param oid               The SNMP Object Identifier (OID) representing the table.
	 * @param selectColumnArray An array of column names to select from the SNMP table.
	 * @param configuration     The SNMP configuration containing connection details.
	 * @param hostname          The hostname or IP address of the SNMP-enabled device.
	 * @param logMode           Flag indicating whether to log warnings in case of errors.
	 * @return A list of rows, where each row is a list of string cells representing the SNMP table.
	 * @throws InterruptedException If the thread executing this method is interrupted.
	 * @throws ExecutionException  If an exception occurs during the execution of the SNMP request.
	 * @throws TimeoutException    If the SNMP request times out.
	 */
	@WithSpan("SNMP V3 Get Table")
	@Override
	public List<List<String>> executeSNMPTable(
		@NonNull @SpanAttribute("snmp.oid") final String oid,
		@NonNull @SpanAttribute("snmp.columns") String[] selectColumnArray,
		@NonNull @SpanAttribute("snmp.config") final ISnmpConfiguration configuration,
		@NonNull @SpanAttribute("host.hostname") final String hostname,
		final boolean logMode
	) throws InterruptedException, ExecutionException, TimeoutException {
		LoggingHelper.trace(() ->
			log.trace("Executing SNMP Table request:\n- OID: {}\n- Columns: {}\n", oid, Arrays.toString(selectColumnArray))
		);

		final long startTime = System.currentTimeMillis();

		List<List<String>> result = executeSNMPGetRequest(
			SnmpGetRequest.TABLE,
			oid,
			(SnmpV3Configuration) configuration,
			hostname,
			selectColumnArray,
			logMode
		);

		final long responseTime = System.currentTimeMillis() - startTime;

		LoggingHelper.trace(() ->
			log.trace(
				"Executed SNMP Table request:\n- OID: {}\n- Columns: {}\n- Result:\n{}\n- response-time: {}\n",
				oid,
				Arrays.toString(selectColumnArray),
				TextTableHelper.generateTextTable(selectColumnArray, result),
				responseTime
			)
		);

		return result;
	}

	/**
	 * Execute an SNMP Get request based on the specified SNMP Get Request type.
	 *
	 * @param request           The type of SNMP Get request (GET, GETNEXT, TABLE).
	 * @param oid               The SNMP Object Identifier (OID) for the request.
	 * @param protocol          The SNMP configuration containing connection details.
	 * @param hostname          The hostname or IP address of the SNMP-enabled device.
	 * @param selectColumnArray An array of column names for TABLE requests.
	 * @param logMode           Flag indicating whether to log warnings in case of errors.
	 * @return The result of the SNMP request, which can be a single value, a table, or null if an error occurs.
	 * @throws InterruptedException If the thread executing this method is interrupted.
	 * @throws ExecutionException  If an exception occurs during the execution of the SNMP request.
	 * @throws TimeoutException    If the SNMP request times out.
	 */
	@SuppressWarnings("unchecked")
	private <T> T executeSNMPGetRequest(
		final SnmpGetRequest request,
		final String oid,
		final SnmpV3Configuration protocol,
		final String hostname,
		final String[] selectColumnArray,
		final boolean logMode
	) throws InterruptedException, ExecutionException, TimeoutException {
		// Create the SNMPClient and run the GetNext request
		return (T) ThreadHelper.execute(
			() -> {
				final SnmpClient snmpClient = new SnmpClient(
					hostname,
					protocol.getPort(),
					protocol.getIntVersion(),
					protocol.getRetryIntervals(),
					null,
					protocol.getAuthType().toString(),
					protocol.getUsername(),
					new String(protocol.getPassword()),
					protocol.getPrivacy().toString(),
					new String(protocol.getPrivacyPassword()),
					protocol.getContextName(),
					null
				);

				try {
					switch (request) {
						case GET:
							return snmpClient.get(oid);
						case GETNEXT:
							return snmpClient.getNext(oid);
						case TABLE:
							return snmpClient.table(oid, selectColumnArray);
						default:
							throw new IllegalArgumentException("Not implemented.");
					}
				} catch (Exception e) {
					if (logMode) {
						log.warn(
							"Hostname {} - Error detected when running SNMP {} Query OID: {}. Error message: {}.",
							hostname,
							request,
							oid,
							e.getMessage()
						);
					}
					return null;
				} finally {
					snmpClient.freeResources();
				}
			},
			protocol.getTimeout()
		);
	}

	/**
	 * Enum representing different types of SNMP requests.
	 * These requests are used to specify the type of SNMP operation
	 * when interacting with SNMP agents.
	 */
	public enum SnmpGetRequest {
		/**
		 * Represents an SNMP GET request.
		 * Used to retrieve the value of a single SNMP object.
		 */
		GET,
		/**
		 * Represents an SNMP GETNEXT request.
		 * Used to retrieve the value of the next SNMP object.
		 */
		GETNEXT,
		/**
		 * Represents an SNMP TABLE request.
		 * Used to retrieve a table of SNMP objects.
		 */
		TABLE
	}
}
