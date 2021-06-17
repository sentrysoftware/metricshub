package com.sentrysoftware.matrix.engine.strategy.matsya;

import com.sentrysoftware.javax.wbem.WBEMException;
import com.sentrysoftware.matrix.common.exception.LocalhostCheckException;
import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.common.helpers.NetworkHelper;
import com.sentrysoftware.matrix.connector.model.common.http.body.Body;
import com.sentrysoftware.matrix.connector.model.common.http.header.Header;
import com.sentrysoftware.matrix.connector.model.detection.Detection;
import com.sentrysoftware.matrix.connector.model.detection.criteria.http.HTTP;
import com.sentrysoftware.matrix.engine.protocol.HTTPProtocol;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol.Privacy;
import com.sentrysoftware.matrix.engine.protocol.WBEMProtocol;
import com.sentrysoftware.matsya.awk.AwkException;
import com.sentrysoftware.matsya.awk.AwkExecutor;
import com.sentrysoftware.matsya.exceptions.WqlQuerySyntaxException;
import com.sentrysoftware.matsya.http.HttpClient;
import com.sentrysoftware.matsya.http.HttpResponse;
import com.sentrysoftware.matsya.jflat.JFlat;
import com.sentrysoftware.matsya.snmp.SNMPClient;
import com.sentrysoftware.matsya.tablejoin.TableJoin;
import com.sentrysoftware.matsya.wbem2.WbemExecuteQuery;
import com.sentrysoftware.matsya.wbem2.WbemQueryResult;
import com.sentrysoftware.matsya.wmi.WmiHelper;
import com.sentrysoftware.matsya.wmi.exceptions.WmiComException;
import com.sentrysoftware.matsya.wmi.handlers.WmiStringConverter;
import com.sentrysoftware.matsya.wmi.handlers.WmiWbemServicesHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.COLON;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.COLON_DOUBLE_SLASH;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.EMPTY;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.HTTPS;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SLASH;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static org.springframework.util.Assert.notNull;

@Component
@Slf4j
public class MatsyaClientsExecutor {

	private static final String SELECTED_COLUMN_CANNOT_BE_NULL = "selectedColumn cannot be null";
	private static final String HOSTNAME_CANNOT_BE_NULL = "hostname cannot be null";
	private static final String PROTOCOL_CANNOT_BE_NULL = "protocol cannot be null";
	private static final String OID_CANNOT_BE_NULL = "oid cannot be null";

	private static final long JSON_2_CSV_TIMEOUT = 60; //seconds

	/**
	 * Run the given {@link Callable} using the passed timeout in seconds.
	 * @param <T>
	 * 
	 * @param callable
	 * @param timeout
	 * @return {@link T} result returned by the callable.
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	<T> T execute(final Callable<T> callable, final long timeout)
			throws InterruptedException, ExecutionException, TimeoutException {
		final ExecutorService executorService = Executors.newSingleThreadExecutor();
		try {
			final Future<T> handler = executorService.submit(callable);

			return handler.get(timeout, TimeUnit.SECONDS);
		} finally {
			executorService.shutdownNow();
		}
	}

	/**
	 * Execute SNMP GetNext request through Matsya
	 * 
	 * @param oid
	 * @param protocol
	 * @param hostname
	 * @param logMode
	 * @return {@link String} value
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	public String executeSNMPGetNext(final String oid, final SNMPProtocol protocol, final String hostname,
			final boolean logMode) throws InterruptedException, ExecutionException, TimeoutException {
		notNull(oid, OID_CANNOT_BE_NULL);
		notNull(protocol, PROTOCOL_CANNOT_BE_NULL);
		notNull(hostname, HOSTNAME_CANNOT_BE_NULL);

		return executeSNMPGetRequest(SNMPGetRequest.GETNEXT, oid, protocol, hostname, null, logMode);

	}

	/**
	 * Execute SNMP Get request through Matsya
	 * 
	 * @param oid
	 * @param protocol
	 * @param hostname
	 * @param logMode
	 * @return {@link String} value
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	public String executeSNMPGet(final String oid, final SNMPProtocol protocol, final String hostname,
			final boolean logMode) throws InterruptedException, ExecutionException, TimeoutException {
		notNull(oid, OID_CANNOT_BE_NULL);
		notNull(protocol, PROTOCOL_CANNOT_BE_NULL);
		notNull(hostname, HOSTNAME_CANNOT_BE_NULL);

		return executeSNMPGetRequest(SNMPGetRequest.GET, oid, protocol, hostname, null, logMode);

	}

	/**
	 * Execute SNMP Table through matsya
	 * @param oid
	 * @param selectColumnArray
	 * @param protocol
	 * @param hostname
	 * @param logMode
	 * @return {@link List} of rows where each row is a {@link List} of {@link String} cells
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	public List<List<String>> executeSNMPTable(final String oid, String[] selectColumnArray, final SNMPProtocol protocol, final String hostname,
			final boolean logMode) throws InterruptedException, ExecutionException, TimeoutException {
		notNull(oid, OID_CANNOT_BE_NULL);
		notNull(protocol, PROTOCOL_CANNOT_BE_NULL);
		notNull(hostname, HOSTNAME_CANNOT_BE_NULL);
		notNull(selectColumnArray, SELECTED_COLUMN_CANNOT_BE_NULL);

		return executeSNMPGetRequest(SNMPGetRequest.TABLE, oid, protocol, hostname, selectColumnArray, logMode);

	}

	@SuppressWarnings("unchecked")
	private <T> T executeSNMPGetRequest(final SNMPGetRequest request, final String oid, final SNMPProtocol protocol,
			final String hostname, final String[] selectColumnArray, final boolean logMode)
			throws InterruptedException, ExecutionException, TimeoutException {
		final int port = protocol.getPort();
		final int version = protocol.getVersion().getIntVersion();
		final int[] retryIntervals = null;
		final String community = protocol.getCommunity();
		final String authType = protocol.getVersion().getAuthType();
		final String authUsername = protocol.getUsername();
		final String authPassword = protocol.getPassword();
		final String privacyType = protocol.getPrivacy() != Privacy.NO_ENCRYPTION && protocol.getPrivacy() != null
				? protocol.getPrivacy().name()
				: null;

		final String privacyPassword = protocol.getPrivacyPassword();
		final String contextName = Thread.currentThread().getName();
		final byte[] contextID = String.valueOf(Thread.currentThread().getId()).getBytes();

		// Create the Matsya SNMPClient and run the GetNext request
		return (T) execute(() -> {

			final SNMPClient snmpClient = new SNMPClient(hostname, port, version, retryIntervals, community, authType,
					authUsername, authPassword, privacyType, privacyPassword, contextName, contextID);

			try {
				switch (request) {
				case GET:
					return snmpClient.get(oid);
				case GETNEXT:
					return snmpClient.getNext(oid);
				case TABLE : 
					return snmpClient.table(oid, selectColumnArray);
				default : 
					throw new IllegalArgumentException("Not implemented.");
				}
			} catch (Exception e) {
				if (logMode) {
					log.error("Error detected when running SNMP {} query OID:{} on HOST:{}", request, oid, hostname);
				}
				return null;
			} finally {
				snmpClient.freeResources();
			}
		}, protocol.getTimeout());
	}

	public enum SNMPGetRequest {
		GET, GETNEXT, TABLE
	}
	
	/**
	 * Execute TableJoin Using Matsya
	 * @param leftTable
	 * @param rightTable
	 * @param leftKeyColumnNumber
	 * @param rightKeyColumnNumber
	 * @param defaultRightLine
	 * @param wbemKeyType {@link true} if WBEM 
	 * @param caseInsensitive
	 * @return
	 */
	public List<List<String>> executeTableJoin(final List<List<String>> leftTable,
			final List<List<String>> rightTable,
			final int leftKeyColumnNumber, 
			final int rightKeyColumnNumber, 
			final List<String> defaultRightLine,
			final boolean wbemKeyType, 
			boolean caseInsensitive){
		return TableJoin.join(leftTable, rightTable, leftKeyColumnNumber, rightKeyColumnNumber, defaultRightLine, false, caseInsensitive);
		
	}

	/**
	 * Call Matsya in order to execute the Awk script on the given input
	 * 
	 * @param embeddedFileScript
	 * @param input
	 * @return
	 * @throws AwkException
	 */
	public String executeAwkScript(String embeddedFileScript, String input) throws AwkException {
		if (embeddedFileScript == null || input == null) {
			return null;
		}
		return AwkExecutor.executeAwk(embeddedFileScript, input);
	}

	/**
	 * Execute JSON to CSV operation using Matsya.
	 * @param jsonSource
	 * @param jsonEntryKey
	 * @param propertyList
	 * @param separator
	 * @return
	 * @throws TimeoutException 
	 * @throws ExecutionException 
	 * @throws InterruptedException
	 */
	public String executeJson2Csv(String jsonSource, String jsonEntryKey, List<String> propertyList, String separator) throws InterruptedException, ExecutionException, TimeoutException {

		final Callable<String> jflatToCSV = () -> {

			try {
				JFlat jsonFlat = new JFlat(jsonSource);

				jsonFlat.parse();

				// Get the CSV
				return jsonFlat.toCSV(jsonEntryKey, propertyList.toArray(new String[propertyList.size()]), separator).toString();
			} catch(IllegalArgumentException e) {
				log.error("Error detected in the arguments when translating the JSON structure into CSV.");
			} catch(Exception e) {
				log.error("Error detected when running jsonFlat parsing.");
			}

			return null;
		};

		return execute(jflatToCSV, JSON_2_CSV_TIMEOUT);
	}

	/**
	 * Execute a WMI query through Matsya
	 * 
	 * @param hostname  The hostname of the device where the WMI service is running
	 * @param username  The username to establish the connection with the device through the WMI protocol
	 * @param password  The password to establish the connection with the device through the WMI protocol
	 * @param timeout   The timeout in seconds after which the query is rejected
	 * @param wbemQuery The WQL to execute
	 * @param namespace The WBEM namespace where all the classes reside
	 * @throws LocalhostCheckException	If the localhost check fails
	 * @throws WmiComException			For any problem encountered with JNA. I.e. on the connection or the query execution
	 * @throws WqlQuerySyntaxException	In case of not valid query
	 * @throws TimeoutException			When the given timeout is reached
	 */
	public List<List<String>> executeWmi(final String hostname, final String username,
			final char[] password, final Long timeout,
			final String wbemQuery, final String namespace)
		throws LocalhostCheckException, WmiComException, TimeoutException, WqlQuerySyntaxException {

		// Where to connect to?
		// Local: namespace
		// Remote: hostname\namespace
		final String networkResource = buildWmiNetworkResource(hostname, namespace);

		// Go!
		try (final WmiWbemServicesHandler wbemServices = 
				new WmiWbemServicesHandler(networkResource, username, password, timeout.intValue() * 1000)) {

			// Connect
			wbemServices.connect();

			// Execute the WQL and get the result
			final List<Map<String, Object>> result = wbemServices.executeWql(wbemQuery);

			// Extract the exact property names (case sensitive), in the right order
			final List<String> properties = WmiHelper.extractPropertiesFromResult(result, wbemQuery);

			// Build the table
			return buildWmiTable(result, properties);

		}
	}

	/**
	 * Build the WMI network resource
	 * @param hostname    The hostname of the device where the WMI service is running
	 * @param namespace   The WMI namespace
	 * @return {@link String} value
	 * @throws LocalhostCheckException
	 */
	String buildWmiNetworkResource(final String hostname, final String namespace) throws LocalhostCheckException {
		return NetworkHelper.isLocalhost(hostname) ?
				namespace : String.format("\\\\%s\\%s", hostname, namespace);
	}

	/**
	 * Convert the given result to a {@link List} of {@link List} table
	 * 
	 * @param result          The result we want to process
	 * @param properties      The ordered properties
	 * @return {@link List} of {@link List} table
	 */
	List<List<String>> buildWmiTable(final List<Map<String, Object>> result, final List<String> properties) {
		final List<List<String>> table = new ArrayList<>();
		final WmiStringConverter stringConverter = new WmiStringConverter();

		// Transform the result to a list of list
		result.forEach(row ->
			{
				final List<String> line = new ArrayList<>();

				// loop over the right order
				properties.forEach(property -> line.add(stringConverter.convert(row.get(property))
								.replace(HardwareConstants.SEMICOLON, HardwareConstants.EMPTY)));

				// We have a line?
				if (!line.isEmpty()) {
					table.add(line);
				}
			});
		return table;
	}

	/**
	 * @param http		The {@link Detection} values.
	 * @param protocol	The {@link HTTPProtocol} containing the connexion configuration.
	 * @param hostname	The hostname against which the HTTP request is being executed.
	 * @param logMode	Whether or not logging is enabled.
	 *
	 * @return			The result of the execution of the given HTTP request.
	 */
	public String executeHttp(HTTP http, HTTPProtocol protocol, String hostname, boolean logMode) {

		notNull(http, "http cannot be null");
		notNull(protocol, PROTOCOL_CANNOT_BE_NULL);
		notNull(hostname, HOSTNAME_CANNOT_BE_NULL);

		String method = http.getMethod();

		String username = protocol.getUsername();
		char[] password = protocol.getPassword();

		Header header = http.getHeader();
		Map<String, String> headerContent = header == null ? null : header.getContent(username, password, EMPTY);

		Body body = http.getBody();
		String bodyContent = body == null ? null : body.getContent(username, password, EMPTY);

		// Building the full URL
		String url = http.getUrl();
		notNull(url, "URL cannot be null");

		String fullUrl = (protocol.getHttps() != null && protocol.getHttps() ? HTTPS : HardwareConstants.HTTP)
			+ COLON_DOUBLE_SLASH
			+ hostname
			+ COLON
			+ protocol.getPort()
			+ (url.startsWith(SLASH) ? url : SLASH + url);

		try {

			// Sending the request
			HttpResponse httpResponse = sendHttpRequest(fullUrl, method, username, password, headerContent, bodyContent,
				protocol.getTimeout().intValue());

			// The request returned an error
			if (httpResponse.getStatusCode() >= HTTP_BAD_REQUEST) {

				return "HTTP Error "
					+ httpResponse.getStatusCode()
					+ httpResponse;
			}

			// The request has been successful
			switch (http.getResultContent()) {

				case BODY: return httpResponse.getBody();
				case HEADER: return httpResponse.getHeader();
				case HTTP_STATUS: return String.valueOf(httpResponse.getStatusCode());
				case ALL: return httpResponse.toString();
				default: throw new IllegalArgumentException("Unsupported ResultContent: " + http.getResultContent());
			}

		} catch (IOException e) {

			if (logMode) {
				log.error("Error detected when running HTTP request {} {} : {}", method, fullUrl, e.getMessage());
			}
		}

		return null;
	}

	/**
	 * @param url			The full URL of the HTTP request.
	 * @param method		The HTTP method (GET, POST, ...).
	 * @param username		The username for the connexion.
	 * @param password		The password for the connexion.
	 * @param headerContent	The {@link Map} of properties-values in the header.
	 * @param bodyContent	The body as a plain text.
	 * @param timeout		The timeout of the request.
	 *
	 * @return				The {@link HttpResponse} returned by the server.
	 *
	 * @throws IOException	If a reading or writing operation fails.
	 */
	protected HttpResponse sendHttpRequest(String url, String method, String username, char[] password,
										 Map<String, String> headerContent, String bodyContent, int timeout) throws IOException {

		return HttpClient
			.sendRequest(
				url,
				method,
				null,
				username,
				password,
				null,
				0,
				null,
				null,
				null,
				headerContent,
				bodyContent,
				timeout,
				null
			);
	}

	/**
	 * @param namespace	The namespace with which the query should be executed.
	 * @param query		The query that is being executed.
	 * @param protocol	The {@link WBEMProtocol} properties.
	 * @param hostname	The name of the host against which the query is being executed.
	 * @param logMode	Whether or not logging is enabled.
	 *
	 * @return			A table (as a {@link List} of {@link List} of {@link String}s)
	 * 					resulting from the execution of the query.
	 *
	 * @throws WqlQuerySyntaxException	If there is a WQL syntax error.
	 * @throws WBEMException			If there is a WBEM error.
	 * @throws TimeoutException			If the query did not complete on time.
	 * @throws InterruptedException		If the current thread was interrupted while waiting.
	 */
	public List<List<String>> executeWbem(String namespace, String query, WBEMProtocol protocol, String hostname,
										  boolean logMode)
		throws WqlQuerySyntaxException, WBEMException, TimeoutException, InterruptedException {

		notNull(protocol, PROTOCOL_CANNOT_BE_NULL);
		notNull(hostname, HOSTNAME_CANNOT_BE_NULL);

		WBEMProtocol.WBEMProtocols wbemProtocols = protocol.getProtocol();
		notNull(wbemProtocols, "wbemProtocols cannot be null");
		String protocolName = wbemProtocols.name();

		Integer port = protocol.getPort();
		notNull(port, "port cannot be null");

		URL url;
		try {

			url = new URL(String.format("%s://%s:%d", protocolName, hostname, port));

		} catch (MalformedURLException e) {

			if (logMode) {

				log.error("Error detected when creating URL {}://{}:{} : {}", protocolName, hostname, port,
					e.getMessage());
			}

			return Collections.emptyList();
		}

		Long timeout = protocol.getTimeout();
		notNull(timeout, "timeout cannot be null");

		WbemQueryResult wbemQueryResult = WbemExecuteQuery.executeQuery(
			url,
			namespace,
			protocol.getUsername(),
			protocol.getPassword(),
			query,
			timeout.intValue() * 1000);

		return wbemQueryResult.getValues();
	}
}
