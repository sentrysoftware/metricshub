package com.sentrysoftware.matrix.engine.strategy.matsya;

import static org.springframework.util.Assert.isTrue;
import java.io.File;
import java.io.IOException;
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

import com.sentrysoftware.matrix.common.exception.MatsyaException;
import com.sentrysoftware.matrix.common.helpers.NetworkHelper;
import com.sentrysoftware.matrix.connector.model.common.http.body.Body;
import com.sentrysoftware.matrix.connector.model.common.http.header.Header;
import com.sentrysoftware.matrix.engine.protocol.HTTPProtocol;
import com.sentrysoftware.matrix.engine.protocol.IPMIOverLanProtocol;
import com.sentrysoftware.matrix.engine.protocol.IProtocolConfiguration;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol.Privacy;
import com.sentrysoftware.matrix.engine.protocol.WBEMProtocol;
import com.sentrysoftware.matrix.engine.protocol.WMIProtocol;
import com.sentrysoftware.matrix.engine.strategy.utils.OsCommandHelper;
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
import com.sentrysoftware.matsya.wmi.WmiHelper;
import com.sentrysoftware.matsya.wmi.WmiStringConverter;
import com.sentrysoftware.matsya.wmi.remotecommand.WinRemoteCommandExecutor;
import com.sentrysoftware.matsya.wmi.wbem.WmiWbemServices;
import com.sentrysoftware.matsya.xflat.XFlat;
import com.sentrysoftware.matsya.xflat.exceptions.XFlatException;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static org.springframework.util.Assert.notNull;

@Component
@Slf4j
public class MatsyaClientsExecutor {

	private static final String TIMEOUT_CANNOT_BE_NULL = "Timeout cannot be null";
	private static final String PASSWORD_CANNOT_BE_NULL = "Password cannot be null";
	private static final String USERNAME_CANNOT_BE_NULL = "Username cannot be null";
	private static final String HOSTNAME_CANNOT_BE_NULL = "hostname cannot be null";
	private static final String PROTOCOL_CANNOT_BE_NULL = "protocol cannot be null";

	private static final long JSON_2_CSV_TIMEOUT = 60; //seconds

	private static final String SSH_FILE_MODE = "0700";
	private static final String SSH_REMOTE_DIRECTORY = "/var/tmp/";

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
	public String executeSNMPGet(
			@NonNull final String oid,
			@NonNull final SNMPProtocol protocol,
			@NonNull final String hostname,
			final boolean logMode
	) throws InterruptedException, ExecutionException, TimeoutException {

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
	public List<List<String>> executeSNMPTable(
			@NonNull final String oid,
			@NonNull String[] selectColumnArray,
			@NonNull final SNMPProtocol protocol,
			@NonNull final String hostname,
			final boolean logMode
	) throws InterruptedException, ExecutionException, TimeoutException {

		return executeSNMPGetRequest(SNMPGetRequest.TABLE, oid, protocol, hostname, selectColumnArray, logMode);

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
					log.warn("Error detected when running SNMP {} query OID: {} on HOST: {}. Error message: {}", request, oid, hostname, e.getMessage());
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
			} catch (IllegalArgumentException e) {
				log.error("Error detected in the arguments when translating the JSON structure into CSV.");
			} catch (Exception e) {
				log.warn("Error detected when running jsonFlat parsing:\n{}", jsonSource);
			}

			return null;
		};

		return execute(jflatToCSV, JSON_2_CSV_TIMEOUT);
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
		return XFlat.parseXml(xml, properties, recordTag);
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
			final URL url = new URL(String.format(
					"%s://%s:%d",
					wbemConfig.getProtocol().toString(),
					hostname,
					wbemConfig.getPort()
			));

			return WbemExecutor.executeWql(
					url,
					namespace,
					wbemConfig.getUsername(),
					wbemConfig.getPassword(),
					query,
					wbemConfig.getTimeout().intValue() * 1000,
					null
			).getValues();

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
			return buildWmiTable(result, properties);

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
			final WinRemoteCommandExecutor result = WinRemoteCommandExecutor.execute(
					command,
					hostname,
					username,
					password,
					null,
					timeout * 1000L,
					localFiles,
					true);
			return result.getStdout();
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
	 * @param httpRequest		The {@link HTTPRequest} values.
	 * @param logMode	Whether or not logging is enabled.
	 *
	 * @return			The result of the execution of the given HTTP request.
	 */
	public String executeHttp(@NonNull HTTPRequest httpRequest, boolean logMode) {
		HTTPProtocol protocol = httpRequest.getHttpProtocol();
		String hostName = httpRequest.getHostname();
		notNull(protocol, PROTOCOL_CANNOT_BE_NULL);
		notNull(hostName, HOSTNAME_CANNOT_BE_NULL);

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
				protocol.getHttps() != null && protocol.getHttps() ? "HTTPS" : "HTTP",
				hostName,
				protocol.getPort(),
				url.startsWith("/") ? "" : "/",
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
			final String password, 
			final String keyFilePath,
			final String command, 
			final int timeout, 
			final List<File> localFiles,
			final String noPasswordCommand) throws MatsyaException {

		isTrue(command != null && !command.trim().isEmpty(), "command mustn't be null nor empty.");
		isTrue(timeout > 0, "timeout mustn't be negative nor zero.");
		final int timeoutInMilliseconds = timeout * 1000;

		final String updatedCommand = updateCommandWithLocalList(command, localFiles);

		final String noPasswordUpdatedCommand = noPasswordCommand == null ?
				updatedCommand :
					updateCommandWithLocalList(noPasswordCommand, localFiles);

		// We have a command: execute it
		final SSHClient sshClient = createSshClientInstance(hostname);
		try {
			sshClient.connect(timeoutInMilliseconds);

			if (password == null) {
				log.warn("Could not read password. Using an empty password instead.");
			}
			final boolean authenticated;
			if (password == null || password.isEmpty()) {
				authenticated = sshClient.authenticate(username);
			} else if (keyFilePath == null) {
				authenticated = sshClient.authenticate(username, password);
			} else {
				authenticated = sshClient.authenticate(username, keyFilePath, password);
			}
			if (!authenticated) {
				final String message = String.format("authentication failed as %s with %s on %s.", 
						username, 
						keyFilePath, 
						hostname);
				log.error(message);
				throw new MatsyaException(message);
			}

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
				final String message = String.format("command \"%s\" failed with result %s",
						noPasswordUpdatedCommand,
						commandResult.result);
				log.error(message);
				throw new MatsyaException(message);
			}
			return commandResult.result;

		} catch (final MatsyaException e) {
			throw e;
		} catch (final Exception e) {
			final String message = String.format("Failed to run SSH command \"%s\" as %s on %s",
					noPasswordUpdatedCommand,
					username,
					hostname);
			log.error("{}. Exception : {}.", message, e.getMessage());
			throw new MatsyaException(message, (Exception) e.getCause());
		} finally {
			sshClient.disconnect();
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

	static SSHClient createSshClientInstance(final String hostname) {
		return new SSHClient(hostname, StandardCharsets.UTF_8);
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
	public String executeIpmiGetSensors(String hostname, IPMIOverLanProtocol ipmiOverLanProtocol)
			throws InterruptedException, ExecutionException, TimeoutException {
		return MatsyaIpmiClient.getFrusAndSensorsAsStringResult(buildIpmiConfiguration(hostname, ipmiOverLanProtocol));
	}

}
