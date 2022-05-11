package com.sentrysoftware.matrix.engine.strategy.matsya;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sentrysoftware.matrix.common.exception.MatsyaException;
import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.common.helpers.NetworkHelper;
import com.sentrysoftware.matrix.common.helpers.StringHelper;
import com.sentrysoftware.matrix.common.helpers.TextTableHelper;
import com.sentrysoftware.matrix.connector.model.common.http.body.Body;
import com.sentrysoftware.matrix.connector.model.common.http.header.Header;
import com.sentrysoftware.matrix.connector.model.common.http.url.Url;
import com.sentrysoftware.matrix.engine.protocol.HTTPProtocol;
import com.sentrysoftware.matrix.engine.protocol.IPMIOverLanProtocol;
import com.sentrysoftware.matrix.engine.protocol.IProtocolConfiguration;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol.Privacy;
import com.sentrysoftware.matrix.engine.protocol.WBEMProtocol;
import com.sentrysoftware.matrix.engine.protocol.WMIProtocol;
import com.sentrysoftware.matrix.engine.strategy.StrategyConfig;
import com.sentrysoftware.matrix.engine.strategy.utils.OsCommandHelper;
import com.sentrysoftware.matsya.WmiHelper;
import com.sentrysoftware.matsya.awk.AwkException;
import com.sentrysoftware.matsya.awk.AwkExecutor;
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
import com.sentrysoftware.matsya.wmi.WmiStringConverter;
import com.sentrysoftware.matsya.wmi.remotecommand.WinRemoteCommandExecutor;
import com.sentrysoftware.matsya.wmi.wbem.WmiWbemServices;
import com.sentrysoftware.matsya.xflat.XFlat;
import com.sentrysoftware.matsya.xflat.exceptions.XFlatException;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MatsyaClientsExecutor {

	private static final String MASK = "*****";
	private static final char[] CHAR_ARRAY_MASK = MASK.toCharArray();
	private static final String TIMEOUT_CANNOT_BE_NULL = "Timeout cannot be null";
	private static final String PASSWORD_CANNOT_BE_NULL = "Password cannot be null";
	private static final String USERNAME_CANNOT_BE_NULL = "Username cannot be null";
	private static final String HOSTNAME_CANNOT_BE_NULL = "hostname cannot be null";
	private static final String PROTOCOL_CANNOT_BE_NULL = "protocol cannot be null";

	private static final long JSON_2_CSV_TIMEOUT = 60; //seconds

	private static final String SSH_FILE_MODE = "0700";
	private static final String SSH_REMOTE_DIRECTORY = "/var/tmp/";

	@Autowired
	@Getter
	private StrategyConfig strategyConfig;

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
	public String executeSNMPGetNext(
			@NonNull final String oid,
			@NonNull final SNMPProtocol protocol,
			@NonNull final String hostname,
			final boolean logMode
	) throws InterruptedException, ExecutionException, TimeoutException {

		trace(() -> 
			log.trace("Executing SNMP GetNext request:\n- oid: {}\n", oid)
		);

		String result = executeSNMPGetRequest(SNMPGetRequest.GETNEXT, oid, protocol, hostname, null, logMode);

		trace(() -> 
			log.trace("Executed SNMP GetNext request:\n- oid: {}\n- result: {}\n", oid, result)
		);

		return result;
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
	public String executeSNMPGet(
			@NonNull final String oid,
			@NonNull final SNMPProtocol protocol,
			@NonNull final String hostname,
			final boolean logMode
	) throws InterruptedException, ExecutionException, TimeoutException {

		trace(() -> 
			log.trace("Executing SNMP Get request:\n- oid: {}\n", oid)
		);

		String result = executeSNMPGetRequest(SNMPGetRequest.GET, oid, protocol, hostname, null, logMode);

		trace(() -> 
			log.trace("Executed SNMP Get request:\n- oid: {}\n- result: {}\n", oid, result)
		);

		return result;
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
	public List<List<String>> executeSNMPTable(
			@NonNull final String oid,
			@NonNull String[] selectColumnArray,
			@NonNull final SNMPProtocol protocol,
			@NonNull final String hostname,
			final boolean logMode
	) throws InterruptedException, ExecutionException, TimeoutException {

		trace(() -> 
			log.trace("Executing SNMP Table request:\n- oid: {}\n- columns: {}\n",
					oid,
					Arrays.toString(selectColumnArray)
			)
		);

		List<List<String>> result = executeSNMPGetRequest(SNMPGetRequest.TABLE, oid, protocol,
			hostname, selectColumnArray, logMode);

		trace(() -> 
			log.trace("Executed SNMP Table request:\n- oid: {}\n- columns: {}\n- result:\n{}\n",
				oid,
				Arrays.toString(selectColumnArray),
				TextTableHelper.generateTextTable(selectColumnArray, result)
			)
		);

		return result;
	}

	@SuppressWarnings("unchecked")
	private <T> T executeSNMPGetRequest(
			final SNMPGetRequest request,
			final String oid,
			final SNMPProtocol protocol,
			final String hostname,
			final String[] selectColumnArray,
			final boolean logMode
	) throws InterruptedException, ExecutionException, TimeoutException {

		final String privacyType =
				protocol.getPrivacy() != Privacy.NO_ENCRYPTION && protocol.getPrivacy() != null
				? protocol.getPrivacy().name()
				: null;

		// Create the Matsya SNMPClient and run the GetNext request
		return (T) execute(() -> {

			final SNMPClient snmpClient = new SNMPClient(
					hostname,
					protocol.getPort(),
					protocol.getVersion().getIntVersion(),
					null,
					protocol.getCommunity(),
					protocol.getVersion().getAuthType(),
					protocol.getUsername(),
					protocol.getPassword() != null ? new String(protocol.getPassword()) : null,
					privacyType,
					protocol.getPrivacyPassword() != null ? new String(protocol.getPrivacyPassword()) : null,
					null,
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

				default :
					throw new IllegalArgumentException("Not implemented.");
				}

			} catch (Exception e) {

				if (logMode) {
					log.warn("Hostname {} - Error detected when running SNMP {} query OID: {}. Error message: {}", 
							hostname, request, oid, e.getMessage());
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

		trace(() -> 
			log.trace("Executing Table Join request:\n- left-table:\n{}\n- right-table:\n{}\n",
				TextTableHelper.generateTextTable(leftTable),
				TextTableHelper.generateTextTable(rightTable)
			)
		);

		List<List<String>> result = TableJoin.join(leftTable, rightTable, leftKeyColumnNumber, rightKeyColumnNumber,
			defaultRightLine, wbemKeyType, caseInsensitive);

		trace(() -> 
			log.trace("Executed Table Join request:\n- left-table:\n{}\n- right-table:\n{}\n- result:\n{}\n",
				TextTableHelper.generateTextTable(leftTable),
				TextTableHelper.generateTextTable(rightTable),
				TextTableHelper.generateTextTable(result)
			)
		);

		return result;
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

		trace(() -> 
			log.trace(
					"Executing JSON to CSV conversion:\n- json-source:\n{}\n- json-entry-key: {}\n"
						+ "- protperty-list: {}\n- separator: {}\n",
					jsonSource,
					jsonEntryKey,
					propertyList,
					separator
			)
		);
		
		final String hostname = getStrategyConfig().getEngineConfiguration().getTarget().getHostname();

		final Callable<String> jflatToCSV = () -> {

			try {
				JFlat jsonFlat = new JFlat(jsonSource);

				jsonFlat.parse();

				// Get the CSV
				return jsonFlat.toCSV(jsonEntryKey, propertyList.toArray(new String[0]), separator).toString();
			} catch (IllegalArgumentException e) {
				log.error("Hostname {} - Error detected in the arguments when translating the JSON structure into CSV.", hostname);
			} catch (Exception e) {
				log.warn("Hostname {} - Error detected when running jsonFlat parsing:\n{}", hostname, jsonSource);
				log.debug("Hostname {} - Exception detected when running jsonFlat parsing: ", hostname, e);
			}

			return null;
		};

		String result = execute(jflatToCSV, JSON_2_CSV_TIMEOUT);

		trace(() -> 
			log.trace("Executed JSON to CSV conversion:\n- json-source:\n{}\n- json-entry-key: {}\n"
						+ "- protperty-list: {}\n- separator: {}\n- result:\n{}\n",
					jsonSource,
					jsonEntryKey,
					propertyList,
					separator,
					result
			)
		);

		return result;
	}

	/**
	 * Parse a XML with the argument properties into a list of values list.
	 *
	 * @param xml The XML.
	 * @param properties A string containing the paths to properties to retrieve separated by a semi-colon character. If the property comes from an attribute, it will be preceded by a superior character: '>'.
	 * @param recordTag A string containing the first element xml tags path to convert. example: /rootTag/tag2
	 * @return  The list of values list.
	 * @throws XFlatException if an error occurred in the XML parsing.
	 */
	public List<List<String>> executeXmlParsing(
			final String xml,
			final String properties,
			final String recordTag) throws XFlatException {

		trace(() -> 
			log.trace("Executing XML parsing:\n- xml-source:\n{}\n- properties: {}\n- record-tag: {}\n",
					xml,
					properties,
					recordTag)
		);

		List<List<String>> result = XFlat.parseXml(xml, properties, recordTag);

		trace(() -> 
			log.trace("Executed XML parsing:\n- xml-source:\n{}\n- properties: {}\n- record-tag: {}\n- result:\n{}\n",
					xml,
					properties,
					recordTag,
					TextTableHelper.generateTextTable(properties, result)
			)
		);

		return result;
	}

	/**
	 * Perform a WQL query, either against a CIM server (WBEM) or WMI
	 * <p>
	 * @param hostname Hostname
	 * @param protoConfig The WBEMProtocol or WMIProtocol object specifying how to connect to specified host
	 * @param query WQL query to execute
	 * @param namespace The namespace
	 *
	 * @return A table (as a {@link List} of {@link List} of {@link String}s)
	 * resulting from the execution of the query.
	 * @throws MatsyaException when anything wrong happens with the Matsya library
	 */
	public List<List<String>> executeWql(
		final String hostname,
		final IProtocolConfiguration protoConfig,
		final String query,
		final String namespace
	) throws MatsyaException {

		if (protoConfig instanceof WBEMProtocol) {
			return executeWbem(hostname, (WBEMProtocol) protoConfig, query, namespace);
		} else if (protoConfig instanceof WMIProtocol) {
			return executeWmi(hostname, (WMIProtocol) protoConfig, query, namespace);
		}

		throw new IllegalStateException("WQL queries can be executed only in WBEM and WMI protocols.");
	}

	/**
	 * Perform a WBEM query.
	 * <p>
	 * @param hostname Hostname
	 * @param wbemConfig WBEM Protocol configuration, incl. credentials
	 * @param query WQL query to execute
	 * @param namespace WBEM namespace
	 *
	 * @return A table (as a {@link List} of {@link List} of {@link String}s)
	 * resulting from the execution of the query.
	 *
	 * @throws MatsyaException when anything goes wrong (details in cause)
	 */
	public List<List<String>> executeWbem(
			@NonNull final String hostname,
			@NonNull final WBEMProtocol wbemConfig,
			@NonNull final String query,
			@NonNull final String namespace
	) throws MatsyaException {

		try {
			String urlSpec = String.format(
					"%s://%s:%d",
					wbemConfig.getProtocol().toString(),
					hostname,
					wbemConfig.getPort()
			);

			final URL url = new URL(urlSpec);

			trace(() -> 
				log.trace("Executing WBEM request:\n- hostname: {}\n- port: {}\n- protocol: {}\n- url: {}\n"
							+ "- username: {}\n- query: {}\n- namespace: {}\n- timeout: {} s\n",
						hostname,
						wbemConfig.getPort(),
						wbemConfig.getProtocol().toString(),
						url,
						wbemConfig.getUsername(),
						query,
						namespace,
						wbemConfig.getTimeout()
				)
			);

			WbemQueryResult wbemQueryResult = WbemExecutor.executeWql(
				url,
				namespace,
				wbemConfig.getUsername(),
				wbemConfig.getPassword(),
				query,
				wbemConfig.getTimeout().intValue() * 1000,
				null
			);

			List<List<String>> result = wbemQueryResult.getValues();

			trace(() -> 
				log.trace("Executed WBEM request:\n- hostname: {}\n- port: {}\n- protocol: {}\n- url: {}\n"
							+ "- username: {}\n- query: {}\n- namespace: {}\n- timeout: {} s\n- result:\n{}\n",
						hostname,
						wbemConfig.getPort(),
						wbemConfig.getProtocol().toString(),
						url,
						wbemConfig.getUsername(),
						query,
						namespace,
						wbemConfig.getTimeout(),
						TextTableHelper.generateTextTable(wbemQueryResult.getProperties(), result)
				)
			);

			return result;

		} catch (Exception e) {
			throw new MatsyaException("WBEM query failed on " + hostname, e);
		}

	}


	/**
	 * Execute a WMI query through Matsya
	 *
	 * @param hostname  The hostname of the device where the WMI service is running (<code>null</code> for localhost)
	 * @param wmiConfig WMI Protocol configuration (credentials, timeout)
	 * @param wbemQuery The WQL to execute
	 * @param namespace The WBEM namespace where all the classes reside
	 *
	 * @throws MatsyaException when anything goes wrong (details in cause)
	 */
	public List<List<String>> executeWmi(
			final String hostname,
			@NonNull final WMIProtocol wmiConfig,
			@NonNull final String wbemQuery,
			@NonNull final String namespace
	) throws MatsyaException {

		// Where to connect to?
		// Local: namespace
		// Remote: hostname\namespace
		final String networkResource =
				NetworkHelper.isLocalhost(hostname) ?
						namespace : String.format("\\\\%s\\%s", hostname, namespace);

		trace(() -> 
			log.trace("Executing WMI request:\n- hostname: {}\n- network-resource: {}\n- username: {}\n- query: {}\n"
						+ "- namespace: {}\n- timeout: {} s\n",
					hostname,
					networkResource,
					wmiConfig.getUsername(),
					wbemQuery,
					namespace,
					wmiConfig.getTimeout()
			)
		);

		// Go!
		try (final WmiWbemServices wbemServices = WmiWbemServices.getInstance(
				networkResource,
				wmiConfig.getUsername(),
				wmiConfig.getPassword()
		)) {

			// Execute the WQL and get the result
			final List<Map<String, Object>> result = wbemServices.executeWql(wbemQuery, wmiConfig.getTimeout() * 1000);

			// Extract the exact property names (case sensitive), in the right order
			final List<String> properties = WmiHelper.extractPropertiesFromResult(result, wbemQuery);

			// Build the table
			List<List<String>> resultTable = buildWmiTable(result, properties);

			trace(() -> 
				log.trace(
						"Executed WMI request:\n- hostname: {}\n- network-resource: {}\n- username: {}\n- query: {}\n"
							+ "- namespace: {}\n- timeout: {} s\n- result:\n{}\n",
						hostname,
						networkResource,
						wmiConfig.getUsername(),
						wbemQuery,
						namespace,
						wmiConfig.getTimeout(),
						TextTableHelper.generateTextTable(properties, resultTable)
				)
			);

			return resultTable;

		} catch (Exception e) {
			throw new MatsyaException("WMI query failed on " + hostname, e);
		}
	}

	/**
	 * Execute a command on a remote Windows system through Matsya and return an object with
	 * the output of the command.
	 *
	 * @param command The command to execute. (Mandatory)
	 * @param hostname Host to connect to.  (Mandatory)
	 * @param username The username name.
	 * @param password The password.
	 * @param timeout Timeout in seconds
	 * @param localFiles The local files list
	 * @return
	 * @throws MatsyaException For any problem encountered.
	 */
	public static String executeWmiRemoteCommand(
			final String command,
			final String hostname,
			final String username,
			final char[] password,
			final int timeout,
			final List<String> localFiles) throws MatsyaException {
		try {

			trace(() -> 
				log.trace("Executing WMI remote command:\n- command: {}\n- hostname: {}\n- username: {}\n"
							+ "- timeout: {} s\n- local-files: {}\n",
						command,
						hostname,
						username,
						timeout,
						localFiles
				)
			);

			final WinRemoteCommandExecutor result = WinRemoteCommandExecutor.execute(
					command,
					hostname,
					username,
					password,
					null,
					timeout * 1000L,
					localFiles,
					true);

			String resultStdout = result.getStdout();

			trace(() -> 
				log.trace("Executing WMI remote command:\n- command: {}\n- hostname: {}\n- username: {}\n"
							+ "- timeout: {} s\n- local-files: {}\n- result:\n{}\n",
						command,
						hostname,
						username,
						timeout,
						localFiles,
						resultStdout
				)
			);

			return resultStdout;

		} catch (Exception e) {
			throw new MatsyaException((Exception) e.getCause());
		}
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
	 * @param httpRequest	The {@link HTTPRequest} values.
	 * @param logMode		Whether or not logging is enabled.
	 *
	 * @return				The result of the execution of the given HTTP request.
	 */
	public String executeHttp(@NonNull HTTPRequest httpRequest, boolean logMode) {

		HTTPProtocol httpProtocol = httpRequest.getHttpProtocol();
		String hostname = httpRequest.getHostname();
		notNull(httpProtocol, PROTOCOL_CANNOT_BE_NULL);
		notNull(hostname, HOSTNAME_CANNOT_BE_NULL);

		String method = httpRequest.getMethod();

		String username = httpProtocol.getUsername();
		char[] password = httpProtocol.getPassword();
		String authenticationToken = httpRequest.getAuthenticationToken();

		Header header = httpRequest.getHeader();
		Map<String, String> headerContent = header == null ? null : header.getContent(username, password, authenticationToken, hostname);

		Map<String, String> headerContentProtected = header == null ? null : header.getContent(username, CHAR_ARRAY_MASK, MASK, hostname);

		Body body = httpRequest.getBody();
		String bodyContent = body == null ? null : body.getContent(username, password, authenticationToken, hostname);
		String bodyContentProtected = body == null ? HardwareConstants.EMPTY
				: body.getContent(username, CHAR_ARRAY_MASK, MASK, hostname);

		String url = httpRequest.getUrl();
		notNull(url, "URL cannot be null");
		
		String protocol = httpProtocol.getHttps() != null && httpProtocol.getHttps() ? "https" : "http";
		String fullUrl = Url.getContent(hostname, httpProtocol.getPort(), url, protocol);

		trace(() -> 
			log.trace(
					"Executing HTTP request: {} {}\n- hostname: {}\n- url: {}\n- protocol: {}\n- port: {}\n"
						+ "- request-headers:\n{}\n- request-body:\n{}\n- timeout: {} s\n- get-result-content: {}\n",
					method,
					fullUrl,
					hostname,
					url,
					protocol,
					httpProtocol.getPort(),
					StringHelper.prettyHttpHeaders(headerContentProtected),
					bodyContentProtected,
					httpProtocol.getTimeout().intValue(),
					httpRequest.getResultContent()

			)
		);

		try {

			// Sending the request
			HttpResponse httpResponse = sendHttpRequest(fullUrl, method, username, password, headerContent, bodyContent,
					httpProtocol.getTimeout().intValue());

			// The request returned an error
			if (httpResponse.getStatusCode() >= HTTP_BAD_REQUEST) {
				log.warn("Hostname {} - Bad response for HTTP request {} {}: {}", hostname, method, fullUrl, httpResponse.getStatusCode());
				return "";
			}

			// The request has been successful
			String result;
			switch (httpRequest.getResultContent()) {
				case BODY: result = httpResponse.getBody(); break;
				case HEADER: result = httpResponse.getHeader(); break;
				case HTTP_STATUS: result = String.valueOf(httpResponse.getStatusCode()); break;
				case ALL: result = httpResponse.toString(); break;
				default: throw new IllegalArgumentException("Unsupported ResultContent: " + httpRequest.getResultContent());
			}

			trace(() -> 
				log.trace(
						"Executed HTTP request: {} {}\n- hostname: {}\n- url: {}\n- protocol: {}\n- port: {}\n"
							+ "- request-headers:\n{}\n- request-body:\n{}\n- timeout: {} s\n"
							+ "- get-result-content: {}\n- response-status: {}\n- response-headers:\n{}\n"
							+ "- response-body:\n{}\n",
					method,
					fullUrl,
					hostname,
					url,
					protocol,
					httpProtocol.getPort(),
					StringHelper.prettyHttpHeaders(headerContentProtected),
					bodyContentProtected,
					httpProtocol.getTimeout().intValue(),
					httpRequest.getResultContent(),
					httpResponse.getStatusCode(),
					httpResponse.getHeader(),
					httpResponse.getBody()
				)
			);

			return result;

		} catch (IOException e) {

			if (logMode) {

				log.error("Hostname {} - Error detected when running HTTP request {} {}: {}\nReturning null.", hostname, method, fullUrl,
					e.getMessage());
				log.debug("Hostname {} - Exception detected when running HTTP request {} {}:", hostname, method, fullUrl, e);
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
	 * @param localFiles
	 * @param noPasswordCommand
	 * @return
	 * @throws IOException
	 */
	public static String runRemoteSshCommand(
			@NonNull
			final String hostname,
			@NonNull
			final String username,
			final char[] password, 
			final File keyFilePath,
			final String command,
			final int timeout,
			final List<File> localFiles,
			final String noPasswordCommand) throws MatsyaException {

		trace(() -> 
			log.trace("Executing Remote SSH command:\n- hostname: {}\n- username: {}\n- key-file-path: {}\n"
						+ "- command: {}\n- timeout: {} s\n- local-files: {}\n",
					hostname,
					username,
					keyFilePath,
					command,
					timeout,
					localFiles
			)
		);

		isTrue(command != null && !command.trim().isEmpty(), "command mustn't be null nor empty.");
		isTrue(timeout > 0, "timeout mustn't be negative nor zero.");
		final int timeoutInMilliseconds = timeout * 1000;

		final String updatedCommand = updateCommandWithLocalList(command, localFiles);

		final String noPasswordUpdatedCommand = noPasswordCommand == null ?
				updatedCommand :
					updateCommandWithLocalList(noPasswordCommand, localFiles);

		// We have a command: execute it
		try (final SSHClient sshClient = createSshClientInstance(hostname)) {
			sshClient.connect(timeoutInMilliseconds);

			if (password == null) {
				log.warn("Hostname {} - Could not read password. Using an empty password instead.", hostname);
			}

			authenticateSsh(sshClient, hostname, username, password, keyFilePath);

			if (localFiles != null && !localFiles.isEmpty()) {
				// copy all local files using SCP
				for (final File file : localFiles) {
					sshClient.scp(
							file.getAbsolutePath(),
							file.getName(),
							SSH_REMOTE_DIRECTORY,
							SSH_FILE_MODE);
				}
			}

			final SSHClient.CommandResult commandResult = sshClient.executeCommand(
					updatedCommand,
					timeoutInMilliseconds);
			if (!commandResult.success) {
				final String message = String.format("Hostname %s - Command \"%s\" failed with result %s",
						hostname,
						noPasswordUpdatedCommand,
						commandResult.result);
				log.error(message);
				throw new MatsyaException(message);
			}

			String result = commandResult.result;

			trace(() -> 
				log.trace("Executed Remote SSH command:\n- hostname: {}\n- username: {}\n- key-file-path: {}\n"
							+ "- command: {}\n- timeout: {} s\n- local-files: {}\n- result:\n{}\n",
						hostname,
						username,
						keyFilePath,
						command,
						timeout,
						localFiles,
						result
				)
			);

			return result;

		} catch (final MatsyaException e) {
			throw e;
		} catch (final Exception e) {
			final String message = String.format("Failed to run SSH command \"%s\" as %s on %s",
					noPasswordUpdatedCommand,
					username,
					hostname);
			log.error("Hostname {} - {}. Exception : {}.", hostname, message, e.getMessage());
			throw new MatsyaException(message, (Exception) e.getCause());
		}
	}

	/**
	 * <p>Authenticate SSH with:
	 * <li>username, privateKey and password first</li>
	 * <li>username and password</li>
	 * <li>username only</li>
	 * </p>
	 * 
	 * @param sshClient The Matsya SSH client
	 * @param hostname The hostname
	 * @param username The username
	 * @param password The password
	 * @param privateKey The private key file
	 * @throws MatsyaException If a Matsya error occurred.
	 */
	static void authenticateSsh(
			final SSHClient sshClient, 
			final String hostname, 
			final String username,
			final char[] password, 
			final File privateKey) throws MatsyaException {
		final boolean authenticated;
		try {
			if (privateKey != null) {
				authenticated = sshClient.authenticate(username, privateKey, password);
			} else if (password != null && password.length > 0) {
				authenticated = sshClient.authenticate(username, password);
			} else {
				authenticated = sshClient.authenticate(username);
			}
		} catch (final Exception e) {
			final String message = String.format("Hostname %s - Authentication failed as %s with %s.", 
					hostname,
					username, 
					privateKey != null ? privateKey.getAbsolutePath() : null);
			log.error("Hostname {} - {}. Exception : {}.", hostname, message, e.getMessage());
			throw new MatsyaException(message, e);
		}
		if (!authenticated) {
			final String message = String.format("Hostname %s - Authentication failed as %s with %s.", 
					hostname,
					username, 
					privateKey != null ? privateKey.getAbsolutePath() : null);
			log.error(message);
			throw new MatsyaException(message);
		}
	}

	/**
	 * Replace in the SSH command all the local files path with their remote path.
	 *
	 * @param command The SSH command.
	 * @param localFiles The local files list.
	 * @return The updated command.
	 */
	static String updateCommandWithLocalList(
			final String command,
			final List<File> localFiles) {
		return  localFiles == null || localFiles.isEmpty() ?
				command :
					localFiles.stream().reduce(
							command,
							(s, file) -> command.replaceAll(
									OsCommandHelper.toCaseInsensitiveRegex(file.getAbsolutePath()),
									SSH_REMOTE_DIRECTORY + file.getName()),
							(s1, s2) -> null);
	}

	public static SSHClient createSshClientInstance(final String hostname) {
		return new SSHClient(hostname, StandardCharsets.UTF_8);
	}

	/**
	 * <p>Connect to the SSH terminal with Matsya. For that:
	 * <li>Create a Matsya SSH Client instance.</li>
	 * <li>Connect to SSH.</li>
	 * <li>Open a SSH session.</li>
	 * <li>Open a terminal.</li>
	 * </p>
	 * 
	 * @param hostname The hostname (mandatory)
	 * @param username The username (mandatory)
	 * @param password The password
	 * @param privateKey The private key file
	 * @param timeout The timeout (>0) in seconds
	 * @return The Matsya SSH client
	 * @throws MatsyaException If a Matsya error occurred.
	 */
	public static SSHClient connectSshClientTerminal(
			@NonNull
			final String hostname,
			@NonNull
			final String username,
			final char[] password,
			final File privateKey,
			final int timeout) throws MatsyaException {

		isTrue(timeout > 0, "timeout must be > 0");

		final SSHClient sshClient = createSshClientInstance(hostname);

		try {

			sshClient.connect(timeout * 1000);

			authenticateSsh(sshClient, hostname, username, password, privateKey);

			sshClient.openSession();

			sshClient.openTerminal();

			return sshClient;

		} catch (final IOException e) {
			sshClient.close();
			throw new MatsyaException(e);
		}
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
	public String executeIpmiDetection(String hostname, @NonNull IPMIOverLanProtocol ipmiOverLanProtocol)
			throws InterruptedException, ExecutionException, TimeoutException {

		trace(() -> 
			log.trace("Executing IPMI detection request:\n- hostname: {}\n- username: {}\n- skipAuth: {}\n"
						+ "- timeout: {} s\n",
					hostname,
					ipmiOverLanProtocol.getUsername(),
					ipmiOverLanProtocol.isSkipAuth(),
					ipmiOverLanProtocol.getTimeout()
			)
		);

		String result = MatsyaIpmiClient.getChassisStatusAsStringResult(buildIpmiConfiguration(hostname,
			ipmiOverLanProtocol));

		trace(() -> 
			log.trace("Executed IPMI detection request:\n- hostname: {}\n- username: {}\n- skipAuth: {}\n"
						+ "- timeout: {} s\n- result:\n{}\n",
					hostname,
					ipmiOverLanProtocol.getUsername(),
					ipmiOverLanProtocol.isSkipAuth(),
					ipmiOverLanProtocol.getTimeout(),
					result
			)
		);

		return result;
	}

	/**
	 * Build MATSYA IPMI configuration
	 *
	 * @param hostname            The host we wish to set in the {@link IpmiConfiguration}
	 * @param ipmiOverLanProtocol Matrix {@link IPMIOverLanProtocol} instance including all the required fields to perform IPMI requests
	 * @return new instance of MATSYA {@link IpmiConfiguration}
	 */
	private static IpmiConfiguration buildIpmiConfiguration(@NonNull String hostname, @NonNull IPMIOverLanProtocol ipmiOverLanProtocol) {
		String username = ipmiOverLanProtocol.getUsername();
		char[] password = ipmiOverLanProtocol.getPassword();
		Long timeout = ipmiOverLanProtocol.getTimeout();

		notNull(username, USERNAME_CANNOT_BE_NULL);
		notNull(password, PASSWORD_CANNOT_BE_NULL);
		notNull(timeout, TIMEOUT_CANNOT_BE_NULL);

		return new IpmiConfiguration(hostname,
				username,
				password,
				ipmiOverLanProtocol.getBmcKey(),
				ipmiOverLanProtocol.isSkipAuth(),
				timeout);
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
	public String executeIpmiGetSensors(String hostname, @NonNull IPMIOverLanProtocol ipmiOverLanProtocol)
			throws InterruptedException, ExecutionException, TimeoutException {

		trace(() -> 
			log.trace("Executing IPMI FRUs and Sensors request:\n- hostname: {}\n- username: {}\n- skipAuth: {}\n"
						+ "- timeout: {} s\n",
					hostname,
					ipmiOverLanProtocol.getUsername(),
					ipmiOverLanProtocol.isSkipAuth(),
					ipmiOverLanProtocol.getTimeout()
			)
		);

		String result = MatsyaIpmiClient.getFrusAndSensorsAsStringResult(buildIpmiConfiguration(hostname,
			ipmiOverLanProtocol));

		trace(() -> 
			log.trace("Executed IPMI FRUs and Sensors request:\n- hostname: {}\n- username: {}\n- skipAuth: {}\n"
						+ "- timeout: {} s\n- result:\n{}\n",
					hostname,
					ipmiOverLanProtocol.getUsername(),
					ipmiOverLanProtocol.isSkipAuth(),
					ipmiOverLanProtocol.getTimeout(),
					result
			)
		);

		return result;
	}


	/**
	 * Run the given runnable if the tracing mode of the logger is enabled
	 * 
	 * @param runnable
	 */
	static void trace(final Runnable runnable) {
		if (log.isTraceEnabled()) {
			runnable.run();
		}
	}

}
