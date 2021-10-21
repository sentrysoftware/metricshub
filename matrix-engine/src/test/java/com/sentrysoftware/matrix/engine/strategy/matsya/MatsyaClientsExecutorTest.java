package com.sentrysoftware.matrix.engine.strategy.matsya;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sentrysoftware.matrix.common.exception.MatsyaException;
import com.sentrysoftware.matrix.connector.model.common.http.ResultContent;
import com.sentrysoftware.matrix.connector.model.common.http.body.StringBody;
import com.sentrysoftware.matrix.connector.model.common.http.header.StringHeader;
import com.sentrysoftware.matrix.engine.protocol.HTTPProtocol;
import com.sentrysoftware.matrix.engine.protocol.IPMIOverLanProtocol;
import com.sentrysoftware.matrix.engine.protocol.WBEMProtocol;
import com.sentrysoftware.matrix.engine.protocol.WBEMProtocol.WBEMProtocols;
import com.sentrysoftware.matsya.http.HttpClient;
import com.sentrysoftware.matsya.http.HttpResponse;
import com.sentrysoftware.matsya.ipmi.IpmiConfiguration;
import com.sentrysoftware.matsya.ipmi.MatsyaIpmiClient;
import com.sentrysoftware.matsya.ssh.SSHClient;
import com.sentrysoftware.matsya.ssh.SSHClient.CommandResult;
import com.sentrysoftware.matsya.wbem2.WbemExecutor;
import com.sentrysoftware.matsya.wbem2.WbemQueryResult;
import com.sentrysoftware.matsya.xflat.exceptions.XFlatException;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.COLON_DOUBLE_SLASH;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.NEW_LINE;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@ExtendWith(MockitoExtension.class)
class MatsyaClientsExecutorTest {

	private static MatsyaClientsExecutor matsyaClientsExecutor;

	private static final String PUREM_SAN = "purem-san";
	private static final String DEV_HV_01 = "dev-hv-01";
	private static final String FOO = "FOO";
	private static final String BAR = "BAR";
	private static final String BAZ = "BAZ";
	private static final int DEFAULT_PORT = 443;

	@BeforeAll
	static void setUp() {

		matsyaClientsExecutor = new MatsyaClientsExecutor();
	}

	@Test
	void testExecute() throws InterruptedException, ExecutionException, TimeoutException {
		assertEquals("value", new MatsyaClientsExecutor().execute(() -> "value", 10L));
	}

	@Test
	void testBuildWMITable() {

		final List<List<String>> result = new MatsyaClientsExecutor().buildWmiTable(
			Arrays.asList(
				Map.of("DeviceID", "1.1", "Name", "Disk 1"),
				Map.of("DeviceID", "1.2", "Name", "Disk 2"),
				Map.of("DeviceID", "1.3", "Name", "Disk 3")),
			Arrays.asList("DeviceID", "Name"));

		final List<List<String>> expected = Arrays.asList(
			Arrays.asList("1.1", "Disk 1"),
			Arrays.asList("1.2", "Disk 2"),
			Arrays.asList("1.3", "Disk 3"));

		assertEquals(expected, result);
	}

	@Test
	void testExecuteHttpWithoutSendHttpRequest() {

		// http is null
		assertThrows(IllegalArgumentException.class, () -> matsyaClientsExecutor.executeHttp(null, false));

		// http is not null, protocol is null
		HTTPRequest httpRequest = new HTTPRequest();
		assertThrows(IllegalArgumentException.class, () -> matsyaClientsExecutor.executeHttp(httpRequest, false));

		// http is not null, protocol is not null, hostname is null
		HTTPProtocol httpProtocol = new HTTPProtocol();
		httpRequest.setHttpProtocol(httpProtocol);
		assertThrows(IllegalArgumentException.class,
			() -> matsyaClientsExecutor.executeHttp(httpRequest, false));

		// http is not null, protocol is not null, hostname is not null, header is null, body is null, url is null
		httpRequest.setHostname(PUREM_SAN);
		assertThrows(IllegalArgumentException.class,
			() -> matsyaClientsExecutor.executeHttp(httpRequest, false));

		// http is not null, protocol is not null, hostname is not null, header is not null, username is null
		httpRequest.setHeader(StringHeader.builder().header(FOO).build());
		assertThrows(IllegalArgumentException.class,
			() -> matsyaClientsExecutor.executeHttp(httpRequest, false));

		// http is not null, protocol is not null, hostname is not null, header is not null, username is not null,
		// password is null
		httpProtocol.setUsername(FOO);
		assertThrows(IllegalArgumentException.class,
			() -> matsyaClientsExecutor.executeHttp(httpRequest, false));

		// http is not null, protocol is not null, hostname is not null, header is not null, username is not null,
		// password is not null, invalid header
		httpProtocol.setPassword(FOO.toCharArray());
		assertThrows(IllegalArgumentException.class,
			() -> matsyaClientsExecutor.executeHttp(httpRequest, false));

		// http is not null, protocol is not null, hostname is not null, header is not null, username is not null,
		// password is not null, header content is not null and valid, body content is not null, url is null
		httpRequest.setHeader(StringHeader.builder().header(FOO + ":" + FOO).build());
		httpRequest.setBody(StringBody.builder().body(FOO).build());
		assertThrows(IllegalArgumentException.class,
			() -> matsyaClientsExecutor.executeHttp(httpRequest, false));
	}

	@Test
	void testExecuteHttpWithSendHttpRequest() {

		HTTPRequest httpRequest = new HTTPRequest();
		httpRequest.setHeader(StringHeader.builder().header(FOO + ":" + FOO).build());
		httpRequest.setBody(StringBody.builder().body(FOO).build());
		httpRequest.setUrl(FOO);
		httpRequest.setHostname(PUREM_SAN);

		HTTPProtocol httpProtocol = new HTTPProtocol();
		httpProtocol.setHttps(false);
		httpProtocol.setUsername(FOO);
		httpProtocol.setPassword(FOO.toCharArray());

		httpRequest.setHttpProtocol(httpProtocol);

		try (MockedStatic<HttpClient> mockedHttpClient = mockStatic(HttpClient.class)) {

			// protocol.getHttps() is null, logMode is true
			String fullHttpUrl = "HTTP" + COLON_DOUBLE_SLASH + PUREM_SAN + ":" + DEFAULT_PORT + "/"
				+ FOO;
			mockedHttpClient.when(() -> HttpClient.sendRequest(
				anyString(), // URL
				isNull(), // method
				isNull(),
				anyString(), // username
				any(char[].class), // password
				isNull(),
				eq(0),
				isNull(),
				isNull(),
				isNull(),
				anyMap(), // header content
				anyString(), // body content
				anyInt(), // timeout
				isNull()))
				.thenThrow(IOException.class);
			assertNull(matsyaClientsExecutor.executeHttp(httpRequest, true));
			mockedHttpClient.verify(() -> HttpClient.sendRequest(eq(fullHttpUrl), isNull(), isNull(), anyString(),
				any(char[].class), isNull(), eq(0), isNull(), isNull(), isNull(), anyMap(), anyString(), anyInt(),
				isNull()));

			// protocol.getHttps() is false, logMode is false
			httpProtocol.setHttps(false);
			assertNull(matsyaClientsExecutor.executeHttp(httpRequest, false));
			mockedHttpClient.verify(times(2), () -> HttpClient.sendRequest(eq(fullHttpUrl), isNull(), isNull(),
				anyString(), any(char[].class), isNull(), eq(0), isNull(), isNull(), isNull(), anyMap(), anyString(),
				anyInt(), isNull()));

			// protocol.getHttps() is true
			String fullHttpsUrl = "HTTPS" + COLON_DOUBLE_SLASH + PUREM_SAN + ":" + DEFAULT_PORT + "/" + FOO;
			httpProtocol.setHttps(true);
			assertNull(matsyaClientsExecutor.executeHttp(httpRequest, false));
			mockedHttpClient.verify(() -> HttpClient.sendRequest(eq(fullHttpsUrl), isNull(), isNull(),
				anyString(), any(char[].class), isNull(), eq(0), isNull(), isNull(), isNull(), anyMap(), anyString(),
				anyInt(), isNull()));

			// protocol.getHttps() is true, url starts with /
			httpRequest.setUrl("/" + FOO);
			assertNull(matsyaClientsExecutor.executeHttp(httpRequest, false));
			mockedHttpClient.verify(times(2), () -> HttpClient.sendRequest(eq(fullHttpsUrl), isNull(), isNull(),
				anyString(), any(char[].class), isNull(), eq(0), isNull(), isNull(), isNull(), anyMap(), anyString(),
				anyInt(), isNull()));

			// httpResponse.getStatusCode() >= HTTP_BAD_REQUEST
			HttpResponse httpResponse = new HttpResponse();
			httpResponse.setStatusCode(HTTP_BAD_REQUEST);
			mockedHttpClient.when(() -> HttpClient.sendRequest(
				anyString(), // URL
				isNull(), // method
				isNull(),
				anyString(), // username
				any(char[].class), // password
				isNull(),
				eq(0),
				isNull(),
				isNull(),
				isNull(),
				anyMap(), // header content
				anyString(), // body content
				anyInt(), // timeout
				isNull()))
				.thenReturn(httpResponse);
			String result = matsyaClientsExecutor.executeHttp(httpRequest, false);
			assertNotNull(result);
			assertEquals("", result);
			mockedHttpClient.verify(times(5), () -> HttpClient.sendRequest(anyString(), isNull(), isNull(),
				anyString(), any(char[].class), isNull(), eq(0), isNull(), isNull(), isNull(), anyMap(), anyString(),
				anyInt(), isNull()));

			// httpResponse.getStatusCode() < HTTP_BAD_REQUEST, ResultContent == BODY
			httpResponse.setStatusCode(HTTP_OK);
			httpResponse.appendHeader(FOO, BAR);
			httpResponse.appendBody(BAZ);
			mockedHttpClient.when(() -> HttpClient.sendRequest(
				anyString(), // URL
				isNull(), // method
				isNull(),
				anyString(), // username
				any(char[].class), // password
				isNull(),
				eq(0),
				isNull(),
				isNull(),
				isNull(),
				anyMap(), // header content
				anyString(), // body content
				anyInt(), // timeout
				isNull()))
				.thenReturn(httpResponse);
			httpRequest.setResultContent(ResultContent.BODY);
			result = matsyaClientsExecutor.executeHttp(httpRequest, false);
			assertNotNull(result);
			assertEquals(BAZ, result);
			mockedHttpClient.verify(times(6), () -> HttpClient.sendRequest(anyString(), isNull(), isNull(),
				anyString(), any(char[].class), isNull(), eq(0), isNull(), isNull(), isNull(), anyMap(), anyString(),
				anyInt(), isNull()));

			// httpResponse.getStatusCode() < HTTP_BAD_REQUEST, ResultContent == HEADER
			httpRequest.setResultContent(ResultContent.HEADER);
			result = matsyaClientsExecutor.executeHttp(httpRequest, false);
			assertNotNull(result);
			assertEquals(FOO + ": " + BAR + NEW_LINE, result);
			mockedHttpClient.verify(times(7), () -> HttpClient.sendRequest(anyString(), isNull(), isNull(),
				anyString(), any(char[].class), isNull(), eq(0), isNull(), isNull(), isNull(), anyMap(), anyString(),
				anyInt(), isNull()));

			// httpResponse.getStatusCode() < HTTP_BAD_REQUEST, ResultContent == HTTP_STATUS
			httpRequest.setResultContent(ResultContent.HTTP_STATUS);
			result = matsyaClientsExecutor.executeHttp(httpRequest, false);
			assertNotNull(result);
			assertEquals(String.valueOf(HTTP_OK), result);
			mockedHttpClient.verify(times(8), () -> HttpClient.sendRequest(anyString(), isNull(), isNull(),
				anyString(), any(char[].class), isNull(), eq(0), isNull(), isNull(), isNull(), anyMap(), anyString(),
				anyInt(), isNull()));

			// httpResponse.getStatusCode() < HTTP_BAD_REQUEST, ResultContent == ALL
			httpRequest.setResultContent(ResultContent.ALL);
			result = matsyaClientsExecutor.executeHttp(httpRequest, false);
			assertNotNull(result);
			assertEquals(FOO + ": " + BAR + NEW_LINE + NEW_LINE + BAZ, result);
			mockedHttpClient.verify(times(9), () -> HttpClient.sendRequest(anyString(), isNull(), isNull(),
				anyString(), any(char[].class), isNull(), eq(0), isNull(), isNull(), isNull(), anyMap(), anyString(),
				anyInt(), isNull()));
		}
	}

	@Test
	void testExecuteWbem() throws Exception {

		// url is not null
		try (MockedStatic<WbemExecutor> mockedWbemExecuteQuery = mockStatic(WbemExecutor.class)) {

			WbemQueryResult wbemQueryResult = new WbemQueryResult(Collections.emptyList(), Collections.emptyList());

			mockedWbemExecuteQuery.when(() -> WbemExecutor.executeWql(
				any(URL.class),
				anyString(), // namespace
				isNull(), // username
				isNull(), // password
				anyString(), // query
				anyInt(), // timeout
				isNull()))
				.thenReturn(wbemQueryResult);

			String url = "https://" + DEV_HV_01 + ":5989";
			assertEquals(Collections.emptyList(), matsyaClientsExecutor.executeWbem(url, WBEMProtocol.builder()
					.protocol(WBEMProtocols.HTTPS)
					.build(), "SELECT Name FROM EMC_StorageSystem", "root/emc"));

			mockedWbemExecuteQuery.verify(() -> WbemExecutor.executeWql(any(URL.class), anyString(), isNull(),
				isNull(), anyString(), anyInt(), isNull()));
		}
	}

	@Test
	void testExecuteIpmiDetection() throws Exception {

		IPMIOverLanProtocol ipmiOverLanProtocol = new IPMIOverLanProtocol();
		assertThrows(IllegalArgumentException.class,
				() -> matsyaClientsExecutor.executeIpmiDetection(null, ipmiOverLanProtocol));

		ipmiOverLanProtocol.setUsername(null);
		assertThrows(IllegalArgumentException.class,
				() -> matsyaClientsExecutor.executeIpmiDetection(FOO, ipmiOverLanProtocol));

		ipmiOverLanProtocol.setUsername(FOO);
		ipmiOverLanProtocol.setPassword(null);
		assertThrows(IllegalArgumentException.class,
				() -> matsyaClientsExecutor.executeIpmiDetection(FOO, ipmiOverLanProtocol));

		ipmiOverLanProtocol.setPassword(FOO.toCharArray());
		ipmiOverLanProtocol.setTimeout(null);
		assertThrows(IllegalArgumentException.class,
				() -> matsyaClientsExecutor.executeIpmiDetection(FOO, ipmiOverLanProtocol));

		try (MockedStatic<MatsyaIpmiClient> mockedMatsyaIpmiClient = mockStatic(MatsyaIpmiClient.class)) {
			mockedMatsyaIpmiClient.when(() -> MatsyaIpmiClient.getChassisStatusAsStringResult(any(IpmiConfiguration.class)))
					.thenReturn("System power state is up");
			ipmiOverLanProtocol.setTimeout(120L);
			assertEquals("System power state is up", matsyaClientsExecutor.executeIpmiDetection(FOO, ipmiOverLanProtocol));
		}
	}

	@Test
	void testExecuteXmlParsing() throws Exception {

		final String xml =
				"<?xml version=\"1.0\"?>\n" +
				"<Document>\n" +
				"	<Owner>User</Owner>\n" +
				"	<Disks>\n" +
				"		<Disk name=\"Disk1\" size=\"1000\">\n" +
				"			<Free>500</Free>\n" +
				"			<Volumes>\n" +
				"				<Volume name=\"Vol1\">\n" +
				"					<Subscribe>600</Subscribe>\n" +
				"				</Volume>\n" +
				"			</Volumes>\n" +
				"		</Disk>\n" +
				"		<Disk name=\"Disk2\" size=\"2000\">\n" +
				"			<Free>750</Free>\n" +
				"		</Disk>\n" +
				"		<Disk name=\"Disk3\" size=\"2900\">\n" +
				"			<Free>1500</Free>\n" +
				"			<Volumes>\n" +
				"				<Volume name=\"Vol3.0\">\n" +
				"					<Subscribe>3000</Subscribe>\n" +
				"				</Volume>\n" +
				"				<Volume name=\"Vol3.1\">\n" +
				"					<Subscribe>3100</Subscribe>\n" +
				"				</Volume>\n" +
				"				<Volume name=\"Vol3.2\">\n" +
				"					<Subscribe>3200</Subscribe>\n" +
				"				</Volume>\n" +
				"			</Volumes>\n" +
				"		</Disk>\n" +
				"	</Disks>\n" +
				"	<OS name=\"Linux\"/>\n" +
				"</Document>\n";

		final String properties =
				"OS>name;"
						+ "Owner;"
						+ "Disks/Disk/Volumes/Volume>name;"
						+ "Disks/Disk/Volumes/Volume/Subscribe;"
						+ "Disks/Disk>name;"
						+ "Disks/Disk>size;"
						+ "Disks/Disk/Free";

		final String recordTag = "/Document";

		assertThrows(IllegalArgumentException.class, () -> matsyaClientsExecutor.executeXmlParsing(null, properties, recordTag));
		assertThrows(IllegalArgumentException.class, () -> matsyaClientsExecutor.executeXmlParsing(xml, null, recordTag));

		assertThrows(XFlatException.class, () -> matsyaClientsExecutor.executeXmlParsing("<Document>...", properties, recordTag));

		final List<List<String>> expected = List.of(
				List.of("Linux", "User", "Vol1", "600", "Disk1", "1000", "500"),
				List.of("Linux", "User", "", "", "Disk2", "2000", "750"),
				List.of("Linux", "User", "Vol3.0", "3000", "Disk3", "2900",  "1500"),
				List.of("Linux", "User", "Vol3.1", "3100", "Disk3", "2900",  "1500"),
				List.of("Linux", "User", "Vol3.2", "3200", "Disk3", "2900",  "1500"));

		assertEquals(expected, matsyaClientsExecutor.executeXmlParsing(xml, properties, recordTag));
	}

	@Test
	void testRunRemoteSshCommandArgumentsKO() throws Exception  {
		final String command = "/bin/sh /tmp/SEN_Embedded_file.sh";
		final String hostname = "host";
		final String username = "user";
		final char[] password = "pwd".toCharArray();
		final File privateKey = new File("path");
		final int timeout = 120;

		assertThrows(IllegalArgumentException.class, () -> MatsyaClientsExecutor.runRemoteSshCommand(null, username, password, privateKey, command, timeout, null, null));
		assertThrows(IllegalArgumentException.class, () -> MatsyaClientsExecutor.runRemoteSshCommand(hostname, null, password, privateKey, command, timeout, null, null));
		assertThrows(IllegalArgumentException.class, () -> MatsyaClientsExecutor.runRemoteSshCommand(hostname, username, password, privateKey, null, timeout, null, null));
		assertThrows(IllegalArgumentException.class, () -> MatsyaClientsExecutor.runRemoteSshCommand(hostname, username, password, privateKey, " ", timeout, null, null));
		assertThrows(IllegalArgumentException.class, () -> MatsyaClientsExecutor.runRemoteSshCommand(hostname, username, password, privateKey, command, -1, null, null));
		assertThrows(IllegalArgumentException.class, () -> MatsyaClientsExecutor.runRemoteSshCommand(hostname, username, password, privateKey, command, 0, null, null));
	}

	@Test
	void testRunRemoteSshCommandConnectionKO() throws Exception  {
		final String command = "/bin/sh /tmp/SEN_Embedded_file.sh";
		final String hostname = "host";
		final String username = "user";
		final char[] password = "pwd".toCharArray();
		final File privateKey = new File("path");
		final int timeout = 120;
		final File localFile = mock(File.class);
		final List<File> localFiles = List.of(localFile);


		try (final MockedStatic<MatsyaClientsExecutor> mockedMatsyaClientsExecutor = mockStatic(MatsyaClientsExecutor.class)) {
			final SSHClient sshClient = mock(SSHClient.class);
			mockedMatsyaClientsExecutor.when(() -> MatsyaClientsExecutor.createSshClientInstance(hostname)).thenReturn(sshClient);
			mockedMatsyaClientsExecutor.when(() -> MatsyaClientsExecutor.updateCommandWithLocalList(command, null)).thenCallRealMethod();
			mockedMatsyaClientsExecutor.when(() -> MatsyaClientsExecutor.updateCommandWithLocalList(command, localFiles)).thenCallRealMethod();

			mockedMatsyaClientsExecutor.when(() -> MatsyaClientsExecutor.runRemoteSshCommand(hostname, username, password, privateKey, command, timeout, null, null)).thenCallRealMethod();
			mockedMatsyaClientsExecutor.when(() -> MatsyaClientsExecutor.runRemoteSshCommand(hostname, username, password, privateKey, command, timeout, localFiles, null)).thenCallRealMethod();

			doReturn("/tmp/SEN_Embedded_file.sh").when(localFile).getAbsolutePath();
			doReturn("SEN_Embedded_file.sh").when(localFile).getName();

			doThrow(IOException.class).when(sshClient).connect(timeout * 1000);

			assertThrows(MatsyaException.class, () -> MatsyaClientsExecutor.runRemoteSshCommand(hostname, username, password, privateKey, command, timeout, null, null));
			assertThrows(MatsyaException.class, () -> MatsyaClientsExecutor.runRemoteSshCommand(hostname, username, password, privateKey, command, timeout, localFiles, null));
		}
	}

	@Test
	void testRunRemoteSshCommandAuthenticationfailed() throws Exception  {
		final String command = "/bin/sh /tmp/SEN_Embedded_file.sh";
		final String hostname = "host";
		final String username = "user";
		final char[] password = "pwd".toCharArray();
		final File privateKey = new File("path");
		final int timeout = 120;
		final File localFile = mock(File.class);
		final List<File> localFiles = List.of(localFile);

		try (final MockedStatic<MatsyaClientsExecutor> mockedMatsyaClientsExecutor = mockStatic(MatsyaClientsExecutor.class)) {
			final SSHClient sshClient = mock(SSHClient.class);
			mockedMatsyaClientsExecutor.when(() -> MatsyaClientsExecutor.createSshClientInstance(hostname)).thenReturn(sshClient);
			mockedMatsyaClientsExecutor.when(() -> MatsyaClientsExecutor.updateCommandWithLocalList(command, localFiles)).thenCallRealMethod();
			mockedMatsyaClientsExecutor.when(() -> MatsyaClientsExecutor.authenticateSsh(sshClient, hostname, username, password, privateKey)).thenThrow(MatsyaException.class);
			

			mockedMatsyaClientsExecutor.when(() -> MatsyaClientsExecutor.runRemoteSshCommand(hostname, username, password, privateKey, command, timeout, localFiles, null)).thenCallRealMethod();

			doReturn("/tmp/SEN_Embedded_file.sh").when(localFile).getAbsolutePath();
			doReturn("SEN_Embedded_file.sh").when(localFile).getName();

			doNothing().when(sshClient).connect(timeout * 1000);

			assertThrows(MatsyaException.class, () -> MatsyaClientsExecutor.runRemoteSshCommand(hostname, username, password, privateKey, command, timeout, localFiles, null));
		}
	}

	@Test
	void testRunRemoteSshCommandScpKO() throws Exception  {
		final String command = "/bin/sh /tmp/SEN_Embedded_file.sh";
		final String hostname = "host";
		final String username = "user";
		final char[] password = "pwd".toCharArray();
		final File privateKey = new File("path");
		final int timeout = 120;
		final File localFile = mock(File.class);
		final List<File> localFiles = List.of(localFile);

		try (final MockedStatic<MatsyaClientsExecutor> mockedMatsyaClientsExecutor = mockStatic(MatsyaClientsExecutor.class)) {
			final SSHClient sshClient = mock(SSHClient.class);
			mockedMatsyaClientsExecutor.when(() -> MatsyaClientsExecutor.createSshClientInstance(hostname)).thenReturn(sshClient);
			mockedMatsyaClientsExecutor.when(() -> MatsyaClientsExecutor.updateCommandWithLocalList(command, localFiles)).thenCallRealMethod();

			mockedMatsyaClientsExecutor.when(() -> MatsyaClientsExecutor.runRemoteSshCommand(hostname, username, password, privateKey, command, timeout, localFiles, null)).thenCallRealMethod();

			doReturn("/tmp/SEN_Embedded_file.sh").when(localFile).getAbsolutePath();
			doReturn("SEN_Embedded_file.sh").when(localFile).getName();

			doNothing().when(sshClient).connect(timeout * 1000);
			
			doThrow(IOException.class).when(sshClient).scp("/tmp/SEN_Embedded_file.sh", "SEN_Embedded_file.sh", "/var/tmp/", "0700");
			assertThrows(MatsyaException.class, () -> MatsyaClientsExecutor.runRemoteSshCommand(hostname, username, password, privateKey, command, timeout, localFiles, null));
		}
	}

	@Test
	void testRunRemoteSshCommandExecuteCommandKO() throws Exception  {
		final String command = "/bin/sh /tmp/SEN_Embedded_file.sh";
		final String hostname = "host";
		final String username = "user";
		final char[] password = "pwd".toCharArray();
		final File privateKey = new File("path");
		final int timeout = 120;
		final File localFile = mock(File.class);
		final List<File> localFiles = List.of(localFile);

		try (final MockedStatic<MatsyaClientsExecutor> mockedMatsyaClientsExecutor = mockStatic(MatsyaClientsExecutor.class)) {
			final SSHClient sshClient = mock(SSHClient.class);
			mockedMatsyaClientsExecutor.when(() -> MatsyaClientsExecutor.createSshClientInstance(hostname)).thenReturn(sshClient);
			mockedMatsyaClientsExecutor.when(() -> MatsyaClientsExecutor.updateCommandWithLocalList(command, null)).thenCallRealMethod();
			mockedMatsyaClientsExecutor.when(() -> MatsyaClientsExecutor.updateCommandWithLocalList(command, localFiles)).thenCallRealMethod();

			mockedMatsyaClientsExecutor.when(() -> MatsyaClientsExecutor.runRemoteSshCommand(hostname, username, password, privateKey, command, timeout, null, null)).thenCallRealMethod();
			mockedMatsyaClientsExecutor.when(() -> MatsyaClientsExecutor.runRemoteSshCommand(hostname, username, password, privateKey, command, timeout, localFiles, null)).thenCallRealMethod();

			doReturn("/tmp/SEN_Embedded_file.sh").when(localFile).getAbsolutePath();
			doReturn("SEN_Embedded_file.sh").when(localFile).getName();

			doNothing().when(sshClient).connect(timeout * 1000);
			doNothing().when(sshClient).scp("/tmp/SEN_Embedded_file.sh", "SEN_Embedded_file.sh", "/var/tmp/", "0700");

			doThrow(IOException.class).when(sshClient).executeCommand(command, timeout * 1000);
			doThrow(IOException.class).when(sshClient).executeCommand("/bin/sh /var/tmp/SEN_Embedded_file.sh", timeout * 1000);

			assertThrows(MatsyaException.class, () -> MatsyaClientsExecutor.runRemoteSshCommand(hostname, username, password, privateKey, command, timeout, null, null));
			assertThrows(MatsyaException.class, () -> MatsyaClientsExecutor.runRemoteSshCommand(hostname, username, password, privateKey, command, timeout, localFiles, null));
		}
	}

	@Test
	void testRunRemoteSshCommandResultFalse() throws Exception  {
		final String command = "/bin/sh /tmp/SEN_Embedded_file.sh";
		final String hostname = "host";
		final String username = "user";
		final char[] password = "pwd".toCharArray();
		final File privateKey = new File("path");
		final int timeout = 120;
		final File localFile = mock(File.class);
		final List<File> localFiles = List.of(localFile);

		final CommandResult commandResult = new CommandResult();
		commandResult.success = false;

		try (final MockedStatic<MatsyaClientsExecutor> mockedMatsyaClientsExecutor = mockStatic(MatsyaClientsExecutor.class)) {
			final SSHClient sshClient = mock(SSHClient.class);
			mockedMatsyaClientsExecutor.when(() -> MatsyaClientsExecutor.createSshClientInstance(hostname)).thenReturn(sshClient);
			mockedMatsyaClientsExecutor.when(() -> MatsyaClientsExecutor.updateCommandWithLocalList(command, null)).thenCallRealMethod();
			mockedMatsyaClientsExecutor.when(() -> MatsyaClientsExecutor.updateCommandWithLocalList(command, localFiles)).thenCallRealMethod();

			mockedMatsyaClientsExecutor.when(() -> MatsyaClientsExecutor.runRemoteSshCommand(hostname, username, password, privateKey, command, timeout, null, null)).thenCallRealMethod();
			mockedMatsyaClientsExecutor.when(() -> MatsyaClientsExecutor.runRemoteSshCommand(hostname, username, password, privateKey, command, timeout, localFiles, null)).thenCallRealMethod();

			doReturn("/tmp/SEN_Embedded_file.sh").when(localFile).getAbsolutePath();
			doReturn("SEN_Embedded_file.sh").when(localFile).getName();

			doNothing().when(sshClient).connect(timeout * 1000);
			doNothing().when(sshClient).scp("/tmp/SEN_Embedded_file.sh", "SEN_Embedded_file.sh", "/var/tmp/", "0700");
			doReturn(commandResult).when(sshClient).executeCommand(command, timeout * 1000);
			doReturn(commandResult).when(sshClient).executeCommand("/bin/sh /var/tmp/SEN_Embedded_file.sh", timeout * 1000);

			assertThrows(MatsyaException.class, () -> MatsyaClientsExecutor.runRemoteSshCommand(hostname, username, password, privateKey, command, timeout, null, null));
			assertThrows(MatsyaException.class, () -> MatsyaClientsExecutor.runRemoteSshCommand(hostname, username, password, privateKey, command, timeout, localFiles, null));
		}
	}

	@Test
	void testRunRemoteSshCommand() throws Exception  {
		final String command = "/bin/sh /tmp/SEN_Embedded_file.sh";
		final String hostname = "host";
		final String username = "user";
		final char[] password = "pwd".toCharArray();
		final File privateKey = new File("path");
		final int timeout = 120;
		final File localFile = mock(File.class);
		final List<File> localFiles = List.of(localFile);

		final CommandResult commandResult = new CommandResult();
		commandResult.success = true;
		commandResult.result = "result";

		try (final MockedStatic<MatsyaClientsExecutor> mockedMatsyaClientsExecutor = mockStatic(MatsyaClientsExecutor.class)) {
			final SSHClient sshClient = mock(SSHClient.class);
			mockedMatsyaClientsExecutor.when(() -> MatsyaClientsExecutor.createSshClientInstance(hostname)).thenReturn(sshClient);
			mockedMatsyaClientsExecutor.when(() -> MatsyaClientsExecutor.updateCommandWithLocalList(command, null)).thenCallRealMethod();
			mockedMatsyaClientsExecutor.when(() -> MatsyaClientsExecutor.updateCommandWithLocalList(command, localFiles)).thenCallRealMethod();

			mockedMatsyaClientsExecutor.when(() -> MatsyaClientsExecutor.runRemoteSshCommand(hostname, username, password, privateKey, command, timeout, null, null)).thenCallRealMethod();
			mockedMatsyaClientsExecutor.when(() -> MatsyaClientsExecutor.runRemoteSshCommand(hostname, username, password, privateKey, command, timeout, localFiles, null)).thenCallRealMethod();

			doReturn("/tmp/SEN_Embedded_file.sh").when(localFile).getAbsolutePath();
			doReturn("SEN_Embedded_file.sh").when(localFile).getName();

			doNothing().when(sshClient).connect(timeout * 1000);
			doNothing().when(sshClient).scp("/tmp/SEN_Embedded_file.sh", "SEN_Embedded_file.sh", "/var/tmp/", "0700");
			doReturn(commandResult).when(sshClient).executeCommand(command, timeout * 1000);
			doReturn(commandResult).when(sshClient).executeCommand("/bin/sh /var/tmp/SEN_Embedded_file.sh", timeout * 1000);

			assertEquals("result", MatsyaClientsExecutor.runRemoteSshCommand(hostname, username, password, privateKey, command, timeout, null, null));
			assertEquals("result", MatsyaClientsExecutor.runRemoteSshCommand(hostname, username, password, privateKey, command, timeout, localFiles, null));
		}
	}
	
	@Test
	void testUpdateCommandWithLocalList() {
		final File localFile = mock(File.class);

		doReturn("/tmp/SEN_Embedded_file.sh").when(localFile).getAbsolutePath();
		doReturn("SEN_Embedded_file.sh").when(localFile).getName();

		assertEquals("cmd /tmp/SEN_Embedded_file.sh", MatsyaClientsExecutor.updateCommandWithLocalList("cmd /tmp/SEN_Embedded_file.sh", null));
		assertEquals("cmd /tmp/SEN_Embedded_file.sh", MatsyaClientsExecutor.updateCommandWithLocalList("cmd /tmp/SEN_Embedded_file.sh", Collections.emptyList()));
		assertEquals("cmd /var/tmp/SEN_Embedded_file.sh", MatsyaClientsExecutor.updateCommandWithLocalList("cmd /tmp/SEN_Embedded_file.sh", List.of(localFile)));
	}

	@Test
	void testAuthenticateSshPrivateKey() throws Exception {
		final String hostname = "host";
		final String username = "user";
		final char[] password = null;
		final File privateKey = new File("path");

		// check IOException
		{
			final SSHClient sshClient = mock(SSHClient.class);

			doThrow(IOException.class).when(sshClient).authenticate(username, privateKey, password);

			assertThrows(MatsyaException.class, () -> MatsyaClientsExecutor.authenticateSsh(sshClient, hostname, username, password, privateKey));
		}

		// check authenticate failed
		{
			final SSHClient sshClient = mock(SSHClient.class);

			doReturn(false).when(sshClient).authenticate(username, privateKey, password);

			assertThrows(MatsyaException.class, () -> MatsyaClientsExecutor.authenticateSsh(sshClient, hostname, username, password, privateKey));
		}

		// check ok
		{
			final SSHClient sshClient = mock(SSHClient.class);

			doReturn(true).when(sshClient).authenticate(username, privateKey, password);

			 MatsyaClientsExecutor.authenticateSsh(sshClient, hostname, username, password, privateKey);
		}
	}

	@Test
	void testAuthenticateSshPassword() throws Exception {
		final String hostname = "host";
		final String username = "user";
		final char[] password = {'p', 'w', 'd'};
		final File privateKey = null;

		// check IOException
		{
			final SSHClient sshClient = mock(SSHClient.class);

			doThrow(IOException.class).when(sshClient).authenticate(username, password);

			assertThrows(MatsyaException.class, () -> MatsyaClientsExecutor.authenticateSsh(sshClient, hostname, username, password, privateKey));
		}

		// check authenticate failed
		{
			final SSHClient sshClient = mock(SSHClient.class);

			doReturn(false).when(sshClient).authenticate(username, password);

			assertThrows(MatsyaException.class, () -> MatsyaClientsExecutor.authenticateSsh(sshClient, hostname, username, password, privateKey));
		}

		// check ok
		{
			final SSHClient sshClient = mock(SSHClient.class);

			doReturn(true).when(sshClient).authenticate(username, password);

			 MatsyaClientsExecutor.authenticateSsh(sshClient, hostname, username, password, privateKey);
		}
	}

	@Test
	void testAuthenticateSshUserOnly() throws Exception {
		final String hostname = "host";
		final String username = "user";
		final File privateKey = null;

		// check IOException and password null or empty
		{
			final SSHClient sshClient = mock(SSHClient.class);

			doThrow(IOException.class).when(sshClient).authenticate(username);

			assertThrows(MatsyaException.class, () -> MatsyaClientsExecutor.authenticateSsh(sshClient, hostname, username, null, privateKey));
			assertThrows(MatsyaException.class, () -> MatsyaClientsExecutor.authenticateSsh(sshClient, hostname, username, new char[0], privateKey));
		}

		// check authenticate failed and password null or empty
		{
			final SSHClient sshClient = mock(SSHClient.class);

			doReturn(false).when(sshClient).authenticate(username);

			assertThrows(MatsyaException.class, () -> MatsyaClientsExecutor.authenticateSsh(sshClient, hostname, username, null, privateKey));
			assertThrows(MatsyaException.class, () -> MatsyaClientsExecutor.authenticateSsh(sshClient, hostname, username, new char[0], privateKey));
		}

		// check ok and password null or empty
		{
			final SSHClient sshClient = mock(SSHClient.class);

			doReturn(true).when(sshClient).authenticate(username);

			 MatsyaClientsExecutor.authenticateSsh(sshClient, hostname, username, null, privateKey);
			 MatsyaClientsExecutor.authenticateSsh(sshClient, hostname, username, new char[0], privateKey);
		}
	}

	@Test
	void testConnectSshClientTerminal() throws Exception {

		final String hostname = "host";
		final String username = "user";
		final char[] password = null;
		final File privateKey = null;
		final int timeout = 30;

		// check hostname null
		assertThrows(IllegalArgumentException.class, () -> MatsyaClientsExecutor.connectSshClientTerminal(null, username, password, privateKey, timeout));

		// check username null
		assertThrows(IllegalArgumentException.class, () -> MatsyaClientsExecutor.connectSshClientTerminal(hostname, null, password, privateKey, timeout));

		// check timeout negative or zero
		assertThrows(IllegalArgumentException.class, () -> MatsyaClientsExecutor.connectSshClientTerminal(hostname, username, password, privateKey, -1));
		assertThrows(IllegalArgumentException.class, () -> MatsyaClientsExecutor.connectSshClientTerminal(hostname, username, password, privateKey, 0));

		// check IOException
		try (final MockedStatic<MatsyaClientsExecutor> mockedMatsyaClientsExecutor = mockStatic(MatsyaClientsExecutor.class)) {
			final SSHClient sshClient = mock(SSHClient.class);

			mockedMatsyaClientsExecutor.when(() -> MatsyaClientsExecutor.createSshClientInstance(hostname)).thenReturn(sshClient);
			mockedMatsyaClientsExecutor.when(() -> MatsyaClientsExecutor.connectSshClientTerminal(hostname, username, password, privateKey, timeout)).thenCallRealMethod();

			doThrow(IOException.class).when(sshClient).connect(timeout * 1000);

			assertThrows(MatsyaException.class, () -> MatsyaClientsExecutor.connectSshClientTerminal(hostname, username, password, privateKey, timeout));
			verify(sshClient, times(1)).close();
		}

		// check ok
		try (final MockedStatic<MatsyaClientsExecutor> mockedMatsyaClientsExecutor = mockStatic(MatsyaClientsExecutor.class)) {
			final SSHClient sshClient = mock(SSHClient.class);

			mockedMatsyaClientsExecutor.when(() -> MatsyaClientsExecutor.createSshClientInstance(hostname)).thenReturn(sshClient);
			mockedMatsyaClientsExecutor.when(() -> MatsyaClientsExecutor.connectSshClientTerminal(hostname, username, password, privateKey, timeout)).thenCallRealMethod();

			doNothing().when(sshClient).connect(timeout * 1000);
			doNothing().when(sshClient).openSession();
			doNothing().when(sshClient).openTerminal();

			assertEquals(sshClient, MatsyaClientsExecutor.connectSshClientTerminal(hostname, username, password, privateKey, timeout));
			verify(sshClient, never()).close();
		}
	}
}
