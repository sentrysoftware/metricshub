package com.sentrysoftware.matrix.engine.strategy.matsya;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static org.springframework.util.Assert.notNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.stereotype.Component;

import com.sentrysoftware.javax.wbem.WBEMException;
import com.sentrysoftware.matrix.common.exception.LocalhostCheckException;
import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.common.helpers.NetworkHelper;
import com.sentrysoftware.matrix.connector.model.common.http.body.Body;
import com.sentrysoftware.matrix.connector.model.common.http.header.Header;
import com.sentrysoftware.matrix.engine.protocol.HTTPProtocol;
import com.sentrysoftware.matrix.engine.protocol.IPMIOverLanProtocol;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol.Privacy;
import com.sentrysoftware.matsya.awk.AwkException;
import com.sentrysoftware.matsya.awk.AwkExecutor;
import com.sentrysoftware.matsya.exceptions.WqlQuerySyntaxException;
import com.sentrysoftware.matsya.http.HttpClient;
import com.sentrysoftware.matsya.http.HttpResponse;
import com.sentrysoftware.matsya.ipmi.IpmiConfiguration;
import com.sentrysoftware.matsya.ipmi.MatsyaIpmiClient;
import com.sentrysoftware.matsya.jflat.JFlat;
import com.sentrysoftware.matsya.snmp.SNMPClient;
import com.sentrysoftware.matsya.ssh.SSHClient;
import com.sentrysoftware.matsya.tablejoin.TableJoin;
import com.sentrysoftware.matsya.wbem2.WbemExecutor;
import com.sentrysoftware.matsya.wbem2.WbemQueryResult;
import com.sentrysoftware.matsya.wmi.WmiHelper;
import com.sentrysoftware.matsya.wmi.exceptions.WmiComException;
import com.sentrysoftware.matsya.wmi.WmiStringConverter;
import com.sentrysoftware.matsya.wmi.wbem.WmiWbemServices;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MatsyaClientsExecutor {

	private static final String TIMEOUT_CANNOT_BE_NULL = "Timeout cannot be null";
	private static final String PASSWORD_CANNOT_BE_NULL = "Password cannot be null";
	private static final String USERNAME_CANNOT_BE_NULL = "Username cannot be null";
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
	 *
	 * @param url						The target URL, as a {@link String}.
	 * @param username					The username to access the host.
	 * @param password					The password to access the host.
	 * @param timeout					The timeout, in milliseconds.
	 * @param query						The query that is being executed.
	 * @param namespace					The namespace with which the query should be executed.
	 *
	 * @return							A table (as a {@link List} of {@link List} of {@link String}s)
	 * 									resulting from the execution of the query.
	 *
	 * @throws MalformedURLException	If no {@link URL} object could be built from <em>url</em>.
	 * @throws WqlQuerySyntaxException	If there is a WQL syntax error.
	 * @throws WBEMException			If there is a WBEM error.
	 * @throws TimeoutException			If the query did not complete on time.
	 * @throws InterruptedException		If the current thread was interrupted while waiting.
	 */
	public List<List<String>> executeWbem(final String url, final String username, final char[] password,
			final int timeout, final String query, final String namespace) throws MalformedURLException,
			WqlQuerySyntaxException, WBEMException, TimeoutException, InterruptedException {

		WbemQueryResult  wbemResult = WbemExecutor.executeWql(new URL(url), namespace, username, password, query,
				timeout, null);

		return wbemResult.getValues();
	}

	/**
	 * @param hostname		The hostname of the target device.
	 * @param port			The port used to access the target device.
	 * @param useEncryption	Indicate whether HTTPS should be used (as opposed to using HTTP).
	 *
	 * @return				A url based on the given input, as a {@link String}.
	 */
	public static String buildWbemUrl(String hostname, int port, boolean useEncryption) {

		String protocolName = useEncryption ? "https" : "http";

		return String.format("%s://%s:%d", protocolName, hostname, port);
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
	 * @throws LocalhostCheckException  If the localhost check fails
	 * @throws WmiComException          For any problem encountered with JNA. I.e. on the connection or the query execution
	 * @throws WqlQuerySyntaxException  In case of not valid query
	 * @throws TimeoutException         When the given timeout is reached
	 */
	public List<List<String>> executeWmi(final String hostname, final String username,
			final char[] password, final Long timeout,
			final String wbemQuery, final String namespace)
			throws LocalhostCheckException, WmiComException, TimeoutException, WqlQuerySyntaxException  {

		// Where to connect to?
		// Local: namespace
		// Remote: hostname\namespace
		final String networkResource = buildWmiNetworkResource(hostname, namespace);
		// Go!
		try (final WmiWbemServices wbemServices = WmiWbemServices.getInstance(networkResource, username, password)) {

			// Execute the WQL and get the result
			final List<Map<String, Object>> result = wbemServices.executeWql(wbemQuery, timeout.intValue() * 1000);

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
		result.forEach(row -> {
				final List<String> line = new ArrayList<>();

				// loop over the right order
				properties.forEach(property -> line.add(stringConverter.convert(row.get(property))));

				// We have a line?
				if (!line.isEmpty()) {
					table.add(line);
				}
			});
		return table;
	}

	/**
	 * @param httpRequest		The {@link HTTPRequest} values.
	 * @param logMode	Whether or not logging is enabled.
	 *
	 * @return			The result of the execution of the given HTTP request.
	 */
	public String executeHttp(HTTPRequest httpRequest, boolean logMode) {

		notNull(httpRequest, "httpRequest cannot be null");

		HTTPProtocol protocol = httpRequest.getHttpProtocol();
		notNull(httpRequest.getHttpProtocol(), PROTOCOL_CANNOT_BE_NULL);
		notNull(httpRequest.getHostname(), HOSTNAME_CANNOT_BE_NULL);

		String method = httpRequest.getMethod();

		String username = protocol.getUsername();
		char[] password = protocol.getPassword();
		String authenticationToken = httpRequest.getAuthenticationToken();

		Header header = httpRequest.getHeader();
		Map<String, String> headerContent = header == null ? null : header.getContent(username, password, authenticationToken);

		Body body = httpRequest.getBody();
		String bodyContent = body == null ? null : body.getContent(username, password, authenticationToken);

		// Building the full URL
		String url = httpRequest.getUrl();
		notNull(url, "URL cannot be null");

		String fullUrl = String.format(
				"%s://%s:%d%s%s",
				protocol.getHttps() != null && protocol.getHttps() ? HardwareConstants.HTTPS : HardwareConstants.HTTP,
				httpRequest.getHostname(),
				protocol.getPort(),
				url.startsWith(HardwareConstants.SLASH) ? HardwareConstants.EMPTY : HardwareConstants.SLASH,
				url
		);

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
			switch (httpRequest.getResultContent()) {
				case BODY: return httpResponse.getBody();
				case HEADER: return httpResponse.getHeader();
				case HTTP_STATUS: return String.valueOf(httpResponse.getStatusCode());
				case ALL: return httpResponse.toString();
				default: throw new IllegalArgumentException("Unsupported ResultContent: " + httpRequest.getResultContent());
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
	 * Use Matsya ssh-client in order to run ssh command
	 * @param hostname
	 * @param username
	 * @param password
	 * @param keyFilePath
	 * @param command
	 * @param timeout
	 * @return
	 * @throws IOException
	 * @throws IllegalStateException
	 */
	public String runRemoteSshCommand(String hostname, String username, String password, String keyFilePath,
			String command, int timeout) throws IOException {

		notNull(hostname, HOSTNAME_CANNOT_BE_NULL);
		notNull(username, USERNAME_CANNOT_BE_NULL);
		notNull(command, "Command cannot be null");

		if (timeout < 0) {
			log.error("Invalid value of the specified timeout {} ", timeout);
			return null;
		}

		// Password
		if (password == null) {
			log.warn("Could not read password. Using an empty password instead.");
			password = "";
		}

		command = command.trim(); // has already been tested that is not null
		if (command.isEmpty()) {
			command = null;
		}

		// Connect
		SSHClient client = new SSHClient(hostname, StandardCharsets.UTF_8);
		boolean authenticated = false;
		client.connect(timeout * 1000);

		try {
			if (password.isEmpty()) {
				authenticated = client.authenticate(username);
			} else if (keyFilePath == null) {
				authenticated = client.authenticate(username, password);
			} else {
				authenticated = client.authenticate(username, keyFilePath, password);
			}

		} catch (IOException e) {
			log.error("Failed to authenticate as {} on {}. Exception : {}.", username, hostname, e.getMessage());
			client.disconnect();
			throw e;
		}

		if (!authenticated) {
			log.warn("Failed to authenticate as {} on {}.", username, hostname);
		}

		// Command or interactive shell?
		if (command != null) {

			// We have a command: execute it
			SSHClient.CommandResult result;
			try {
				result = client.executeCommand(command, timeout * 1000);
			} catch (IOException e) {
				log.error("Failed to authenticate as {} on {}. Exception : {}.", username, hostname, e.getMessage());
				throw e;
			} finally {
				client.disconnect();
			}

			if (result.success) {
				return result.result;
			} else {
				// Failure
				log.error("Execution failed: {}.", result.result);
			}

		}
		return null;
	}

	/**
	 * Run the IPMI detection in order to detect the Chassis power state
	 *
	 * @param hostname            The host name or the IP address we wish to query
	 * @param ipmiOverLanProtocol The Matrix {@link IPMIOverLanProtocol} instance including all the required fields to perform IPMI requests
	 * @return String value. E.g. System power state is up
	 * @throws TimeoutException
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public String executeIpmiDetection(String hostname, IPMIOverLanProtocol ipmiOverLanProtocol)
			throws InterruptedException, ExecutionException, TimeoutException {

		return MatsyaIpmiClient.getChassisStatusAsStringResult(buildIpmiConfiguration(hostname, ipmiOverLanProtocol));
	}

	/**
	 * Build MATSYA IPMI configuration
	 *
	 * @param hostname            The host we wish to set in the {@link IpmiConfiguration}
	 * @param ipmiOverLanProtocol Matrix {@link IPMIOverLanProtocol} instance including all the required fields to perform IPMI requests
	 * @return new instance of MATSYA {@link IpmiConfiguration}
	 */
	private static IpmiConfiguration buildIpmiConfiguration(@NonNull String hostname, @NonNull IPMIOverLanProtocol ipmiOverLanProtocol) {
		notNull(ipmiOverLanProtocol.getUsername(), USERNAME_CANNOT_BE_NULL);
		notNull(ipmiOverLanProtocol.getPassword(), PASSWORD_CANNOT_BE_NULL);
		notNull(ipmiOverLanProtocol.getTimeout(), TIMEOUT_CANNOT_BE_NULL);

		return new IpmiConfiguration(hostname,
				ipmiOverLanProtocol.getUsername(),
				ipmiOverLanProtocol.getPassword(),
				ipmiOverLanProtocol.getBmcKey(),
				ipmiOverLanProtocol.isSkipAuth(),
				ipmiOverLanProtocol.getTimeout());
	}

	/**
	 * Run IPMI Over-LAN request in order to get all the sensors
	 *
	 * @param hostname            The host we wish to set in the {@link IpmiConfiguration}
	 * @param ipmiOverLanProtocol The Matrix {@link IPMIOverLanProtocol} instance including all the required fields to perform IPMI requests
	 * @return String output contains FRUs and Sensor states and readings
	 * @throws TimeoutException
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public String executeIpmiGetSensors(String hostname, IPMIOverLanProtocol ipmiOverLanProtocol)
			throws InterruptedException, ExecutionException, TimeoutException {
		return MatsyaIpmiClient.getFrusAndSensorsAsStringResult(buildIpmiConfiguration(hostname, ipmiOverLanProtocol));
	}
}
