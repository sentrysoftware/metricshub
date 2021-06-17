package com.sentrysoftware.matrix.engine.strategy.matsya;

import com.sentrysoftware.javax.wbem.WBEMException;
import com.sentrysoftware.matrix.common.exception.LocalhostCheckException;
import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.common.helpers.NetworkHelper;
import com.sentrysoftware.matrix.connector.model.common.http.ResultContent;
import com.sentrysoftware.matrix.connector.model.common.http.body.StringBody;
import com.sentrysoftware.matrix.connector.model.common.http.header.StringHeader;
import com.sentrysoftware.matrix.connector.model.detection.criteria.http.HTTP;
import com.sentrysoftware.matrix.engine.protocol.HTTPProtocol;
import com.sentrysoftware.matrix.engine.protocol.WBEMProtocol;
import com.sentrysoftware.matsya.exceptions.WqlQuerySyntaxException;
import com.sentrysoftware.matsya.http.HttpClient;
import com.sentrysoftware.matsya.http.HttpResponse;
import com.sentrysoftware.matsya.wbem2.WbemExecuteQuery;
import com.sentrysoftware.matsya.wbem2.WbemQueryResult;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.COLON;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.COLON_DOUBLE_SLASH;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.NEW_LINE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SLASH;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.WHITE_SPACE;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mockStatic;
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
	void testBuildWMINetworkResource() throws LocalhostCheckException {
		try (MockedStatic<NetworkHelper> networkHelper = mockStatic(NetworkHelper.class)) {
			networkHelper.when(() -> NetworkHelper.isLocalhost("hostname")).thenReturn(true);
			assertEquals("root/cimv2", new MatsyaClientsExecutor().buildWmiNetworkResource("hostname", "root/cimv2"));
		}

		try (MockedStatic<NetworkHelper> networkHelper = mockStatic(NetworkHelper.class)) {
			networkHelper.when(() -> NetworkHelper.isLocalhost("hostname")).thenReturn(false);
			assertEquals("\\\\hostname\\root/cimv2", new MatsyaClientsExecutor()
				.buildWmiNetworkResource("hostname", "root/cimv2"));
		}
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
		assertThrows(IllegalArgumentException.class, () -> matsyaClientsExecutor.executeHttp(null, null, null, false));

		// http is not null, protocol is null
		HTTP http = new HTTP();
		assertThrows(IllegalArgumentException.class, () -> matsyaClientsExecutor.executeHttp(http, null, null, false));

		// http is not null, protocol is not null, hostname is null
		HTTPProtocol httpProtocol = new HTTPProtocol();
		assertThrows(IllegalArgumentException.class,
			() -> matsyaClientsExecutor.executeHttp(http, httpProtocol, null, false));

		// http is not null, protocol is not null, hostname is not null, header is null, body is null, url is null
		assertThrows(IllegalArgumentException.class,
			() -> matsyaClientsExecutor.executeHttp(http, httpProtocol, PUREM_SAN, false));

		// http is not null, protocol is not null, hostname is not null, header is not null, username is null
		http.setHeader(StringHeader.builder().header(FOO).build());
		assertThrows(IllegalArgumentException.class,
			() -> matsyaClientsExecutor.executeHttp(http, httpProtocol, PUREM_SAN, false));

		// http is not null, protocol is not null, hostname is not null, header is not null, username is not null,
		// password is null
		httpProtocol.setUsername(FOO);
		assertThrows(IllegalArgumentException.class,
			() -> matsyaClientsExecutor.executeHttp(http, httpProtocol, PUREM_SAN, false));

		// http is not null, protocol is not null, hostname is not null, header is not null, username is not null,
		// password is not null, invalid header
		httpProtocol.setPassword(FOO.toCharArray());
		assertThrows(IllegalArgumentException.class,
			() -> matsyaClientsExecutor.executeHttp(http, httpProtocol, PUREM_SAN, false));

		// http is not null, protocol is not null, hostname is not null, header is not null, username is not null,
		// password is not null, header content is not null and valid, body content is not null, url is null
		http.setHeader(StringHeader.builder().header(FOO + COLON + FOO).build());
		http.setBody(StringBody.builder().body(FOO).build());
		assertThrows(IllegalArgumentException.class,
			() -> matsyaClientsExecutor.executeHttp(http, httpProtocol, PUREM_SAN, false));
	}

	@Test
	void testExecuteHttpWithSendHttpRequest() {

		HTTP http = new HTTP();
		http.setHeader(StringHeader.builder().header(FOO + COLON + FOO).build());
		http.setBody(StringBody.builder().body(FOO).build());
		http.setUrl(FOO);

		HTTPProtocol httpProtocol = new HTTPProtocol();
		httpProtocol.setHttps(false);
		httpProtocol.setUsername(FOO);
		httpProtocol.setPassword(FOO.toCharArray());

		try (MockedStatic<HttpClient> mockedHttpClient = mockStatic(HttpClient.class)) {

			// protocol.getHttps() is null, logMode is true
			String fullHttpUrl = HardwareConstants.HTTP + COLON_DOUBLE_SLASH + PUREM_SAN + COLON + DEFAULT_PORT + SLASH
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
			assertNull(matsyaClientsExecutor.executeHttp(http, httpProtocol, PUREM_SAN, true));
			mockedHttpClient.verify(() -> HttpClient.sendRequest(eq(fullHttpUrl), isNull(), isNull(), anyString(),
				any(char[].class), isNull(), eq(0), isNull(), isNull(), isNull(), anyMap(), anyString(), anyInt(),
				isNull()));

			// protocol.getHttps() is false, logMode is false
			httpProtocol.setHttps(false);
			assertNull(matsyaClientsExecutor.executeHttp(http, httpProtocol, PUREM_SAN, false));
			mockedHttpClient.verify(times(2), () -> HttpClient.sendRequest(eq(fullHttpUrl), isNull(), isNull(),
				anyString(), any(char[].class), isNull(), eq(0), isNull(), isNull(), isNull(), anyMap(), anyString(),
				anyInt(), isNull()));

			// protocol.getHttps() is true
			String fullHttpsUrl = HardwareConstants.HTTPS + COLON_DOUBLE_SLASH + PUREM_SAN + COLON + DEFAULT_PORT
				+ SLASH + FOO;
			httpProtocol.setHttps(true);
			assertNull(matsyaClientsExecutor.executeHttp(http, httpProtocol, PUREM_SAN, false));
			mockedHttpClient.verify(() -> HttpClient.sendRequest(eq(fullHttpsUrl), isNull(), isNull(),
				anyString(), any(char[].class), isNull(), eq(0), isNull(), isNull(), isNull(), anyMap(), anyString(),
				anyInt(), isNull()));

			// protocol.getHttps() is true, url starts with /
			http.setUrl(SLASH + FOO);
			assertNull(matsyaClientsExecutor.executeHttp(http, httpProtocol, PUREM_SAN, false));
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
			String result = matsyaClientsExecutor.executeHttp(http, httpProtocol, PUREM_SAN, false);
			assertNotNull(result);
			assertTrue(result.startsWith("HTTP Error " + HTTP_BAD_REQUEST));
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
			http.setResultContent(ResultContent.BODY);
			result = matsyaClientsExecutor.executeHttp(http, httpProtocol, PUREM_SAN, false);
			assertNotNull(result);
			assertEquals(BAZ, result);
			mockedHttpClient.verify(times(6), () -> HttpClient.sendRequest(anyString(), isNull(), isNull(),
				anyString(), any(char[].class), isNull(), eq(0), isNull(), isNull(), isNull(), anyMap(), anyString(),
				anyInt(), isNull()));

			// httpResponse.getStatusCode() < HTTP_BAD_REQUEST, ResultContent == HEADER
			http.setResultContent(ResultContent.HEADER);
			result = matsyaClientsExecutor.executeHttp(http, httpProtocol, PUREM_SAN, false);
			assertNotNull(result);
			assertEquals(FOO + COLON + WHITE_SPACE + BAR + NEW_LINE, result);
			mockedHttpClient.verify(times(7), () -> HttpClient.sendRequest(anyString(), isNull(), isNull(),
				anyString(), any(char[].class), isNull(), eq(0), isNull(), isNull(), isNull(), anyMap(), anyString(),
				anyInt(), isNull()));

			// httpResponse.getStatusCode() < HTTP_BAD_REQUEST, ResultContent == HTTP_STATUS
			http.setResultContent(ResultContent.HTTP_STATUS);
			result = matsyaClientsExecutor.executeHttp(http, httpProtocol, PUREM_SAN, false);
			assertNotNull(result);
			assertEquals(String.valueOf(HTTP_OK), result);
			mockedHttpClient.verify(times(8), () -> HttpClient.sendRequest(anyString(), isNull(), isNull(),
				anyString(), any(char[].class), isNull(), eq(0), isNull(), isNull(), isNull(), anyMap(), anyString(),
				anyInt(), isNull()));

			// httpResponse.getStatusCode() < HTTP_BAD_REQUEST, ResultContent == ALL
			http.setResultContent(ResultContent.ALL);
			result = matsyaClientsExecutor.executeHttp(http, httpProtocol, PUREM_SAN, false);
			assertNotNull(result);
			assertEquals(FOO + COLON + WHITE_SPACE + BAR + NEW_LINE + NEW_LINE + BAZ, result);
			mockedHttpClient.verify(times(9), () -> HttpClient.sendRequest(anyString(), isNull(), isNull(),
				anyString(), any(char[].class), isNull(), eq(0), isNull(), isNull(), isNull(), anyMap(), anyString(),
				anyInt(), isNull()));
		}
	}

	@Test
	void testExecuteWbem() throws WqlQuerySyntaxException, WBEMException, TimeoutException, InterruptedException {

		// protocol is null
		assertThrows(IllegalArgumentException.class, () -> matsyaClientsExecutor.executeWbem(null, null, null, null, false));

		// protocol is not null, hostname is null
		WBEMProtocol wbemProtocol = new WBEMProtocol();
		assertThrows(IllegalArgumentException.class, () -> matsyaClientsExecutor.executeWbem(null, null, wbemProtocol, null, false));

		// protocol is not null, hostname is not null, wbemProtocol.protocol is null
		assertThrows(IllegalArgumentException.class,
			() -> matsyaClientsExecutor.executeWbem(null, null, wbemProtocol, DEV_HV_01, false));

		// protocol is not null, hostname is not null, wbemProtocol.protocol is not null, port is null
		wbemProtocol.setProtocol(WBEMProtocol.WBEMProtocols.HTTPS);
		wbemProtocol.setPort(null);
		assertThrows(IllegalArgumentException.class,
			() -> matsyaClientsExecutor.executeWbem(null, null, wbemProtocol, DEV_HV_01, false));

		// protocol is not null, hostname is not null, wbemProtocol.protocol is not null, port is not null
		// timeout is null
		wbemProtocol.setPort(5989);
		wbemProtocol.setTimeout(null);
		assertThrows(IllegalArgumentException.class,
			() -> matsyaClientsExecutor.executeWbem(null, null, wbemProtocol, DEV_HV_01, false));

		// protocol is not null, hostname is not null, wbemProtocol.protocol is not null, port is not null
		// timeout is not null, query execution throws exception
		wbemProtocol.setTimeout(120L);
		assertThrows(IllegalArgumentException.class,
			() -> matsyaClientsExecutor.executeWbem(null, null, wbemProtocol, DEV_HV_01, false));

		// protocol is not null, hostname is not null, wbemProtocol.protocol is not null, port is not null
		// timeout is not null, query execution completes
		try (MockedStatic<WbemExecuteQuery> mockedWbemExecuteQuery = mockStatic(WbemExecuteQuery.class)) {

			WbemQueryResult wbemQueryResult = new WbemQueryResult(Collections.emptySet(), Collections.emptyList());

			mockedWbemExecuteQuery.when(() -> WbemExecuteQuery.executeQuery(
				any(URL.class),
				anyString(), // namespace
				isNull(), // username
				isNull(), // password
				anyString(), // query
				anyInt())) // timeout
				.thenReturn(wbemQueryResult);

			assertEquals(Collections.emptyList(), matsyaClientsExecutor.executeWbem(FOO, BAR, wbemProtocol, DEV_HV_01, false));

			mockedWbemExecuteQuery.verify(() -> WbemExecuteQuery.executeQuery(any(URL.class), anyString(), isNull(),
				isNull(), anyString(), anyInt()));
		}
	}
}
