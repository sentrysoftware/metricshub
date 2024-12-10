package org.sentrysoftware.metricshub.cli.http;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;
import picocli.CommandLine.ParameterException;

class HttpCliTest {

	HttpCli httpCli;
	CommandLine commandLine;
	PrintWriter printWriter = new PrintWriter(new StringWriter());

	private static final String HTTP_GET = "GET";
	private static final String WRONG_HTTP_METHOD = "WrongMethod";
	private static final String URL = "https://hostname:443/www.test.com";
	private static final String WRONG_URL = "WrongUrl";
	private static final String WRONG_URL_WITH_SPACE = "https://hostname:443/www.test.com/path 1/";
	private static final String WRONG_HEADER_FILE_PATH = "wrong/path/header.txt";
	private static final String HEADER_FILE_PATH = "src/test/resources/cli/header.txt";
	private static final Map<String, String> HEADERS = Map.of("Content-Type", "application/xml");
	private static final String BODY = "<aaaLogin inName=\" inPassword=\" />";
	private static final String BODY_FILE_PATH = "src/test/resources/cli/body.txt";
	private static final String WRONG_BODY_FILE_PATH = "wrong/path/body.txt";
	private static final String AUTHENTICATION_TOKEN = "Q5SD7SDF2BCV8ZER4";
	private static final String RESULT_CONTENT = "all";
	private static final String FILE_HEADER = 
		String.join("\r\n",
			"Content-Type: application/xml",
			"User-Agent: Mozilla/5.0",
			"Accept: text/html",
			"Accept-Language: en-US",
			"Cache-Control: no-cache"
		);

	void initCli() {
		httpCli = new HttpCli();
		commandLine = new CommandLine(httpCli);
	}

	@Test
	void testGetQuery() {
		initCli();
		httpCli.setMethod(HTTP_GET);
		httpCli.setUrl(URL);
		httpCli.setHeaders(HEADERS);
		httpCli.setBody(BODY);
		httpCli.setAuthenticationToken(AUTHENTICATION_TOKEN);

		StringBuilder header = new StringBuilder();
		HEADERS.forEach((key, value) -> header.append(String.format("%s: %s%n", key, value)));

		ObjectNode queryNode = JsonNodeFactory.instance.objectNode();
		queryNode.set("method", new TextNode(HTTP_GET));
		queryNode.set("url", new TextNode(URL));
		queryNode.set("header", new TextNode(header.toString()));
		queryNode.set("body", new TextNode(BODY));
		queryNode.set("resultContent", new TextNode(RESULT_CONTENT));
		queryNode.set("authenticationToken", new TextNode(AUTHENTICATION_TOKEN));
		assertEquals(queryNode, httpCli.getQuery());
	}

	@Test
	void testGetHeader() throws Exception {
		initCli();
		httpCli.setHeaders(HEADERS);
		assertEquals("Content-Type: application/xml\r\n", httpCli.getHeaderContent());
		httpCli.setHeaders(null);
		httpCli.setHeaderFile(HEADER_FILE_PATH);
		assertEquals(FILE_HEADER, httpCli.getHeaderContent());
	}

	@Test
	void testGetBody() throws Exception {
		initCli();
		httpCli.setBody(BODY);
		assertEquals(BODY, httpCli.getBody());
		httpCli.setBody(null);
		httpCli.setBodyFile(BODY_FILE_PATH);
		assertEquals(BODY, httpCli.getBodyContent());
	}

	@Test
	void testValidateUrl() {
		initCli();
		httpCli.setUrl(WRONG_URL_WITH_SPACE);
		ParameterException exceptionMessage = assertThrows(ParameterException.class, () -> httpCli.validateUrl());
		assertTrue(exceptionMessage.getMessage().contains("Invalid URL"));
		httpCli.setUrl(URL);
		assertDoesNotThrow(() -> httpCli.validateUrl());
	}

	@Test
	void testResolvePortFromUrl() throws MalformedURLException {
		initCli();
		httpCli.parsedUrl = new java.net.URL(URL);
		assertEquals(443, httpCli.resolvePortFromUrl());

		httpCli.parsedUrl = new java.net.URL("https://www.test.com");
		assertEquals(443, httpCli.resolvePortFromUrl());

		httpCli.parsedUrl = new java.net.URL("http://www.test.com");
		assertEquals(80, httpCli.resolvePortFromUrl());

		httpCli.parsedUrl = new java.net.URL("http://hostname:555/subdomain");
		assertEquals(555, httpCli.resolvePortFromUrl());
	}

	@Test
	void testValidate() {
		initCli();

		// URL must be valid (contains the protocol at the beginning).
		httpCli.setUrl(WRONG_URL);
		ParameterException exceptionMessage = assertThrows(ParameterException.class, () -> httpCli.validate());
		assertTrue(exceptionMessage.getMessage().contains("Invalid URL:"));
		httpCli.setUrl(URL);

		// Method must be : GET/POST/PUT/DELETE or empty (default: GET)
		httpCli.setMethod(WRONG_HTTP_METHOD);
		exceptionMessage = assertThrows(ParameterException.class, () -> httpCli.validate());
		assertEquals("Unknown HTTP request method: WrongMethod.", exceptionMessage.getMessage());
		httpCli.setMethod(HTTP_GET);

		// Wrong headerFile path
		httpCli.setHeaderFile(WRONG_HEADER_FILE_PATH);
		exceptionMessage = assertThrows(ParameterException.class, () -> httpCli.validate());
		assertTrue(exceptionMessage.getMessage().contains("Error while reading header file"));

		// Only one header (headers or headerFile) must be specified.
		httpCli.setHeaders(HEADERS);
		httpCli.setHeaderFile(HEADER_FILE_PATH);
		exceptionMessage = assertThrows(ParameterException.class, () -> httpCli.validate());
		assertEquals(
			"Conflict - Two headers have been configured: --http-header and --http-header-file.",
			exceptionMessage.getMessage()
		);
		httpCli.setHeaders(null);

		// Wrong bodyFile path
		httpCli.setBodyFile(WRONG_BODY_FILE_PATH);
		exceptionMessage = assertThrows(ParameterException.class, () -> httpCli.validate());
		assertTrue(exceptionMessage.getMessage().contains("Error while reading body file"));

		// Only one body (body or bodyFile) must be specified.
		httpCli.setBody(BODY);
		httpCli.setBodyFile(BODY_FILE_PATH);
		exceptionMessage = assertThrows(ParameterException.class, () -> httpCli.validate());
		assertEquals(
			"Conflict - Two bodies have been configured: --http-body and --http-body-file.",
			exceptionMessage.getMessage()
		);
		httpCli.setBody(null);

		assertDoesNotThrow(() -> httpCli.validate());
	}
}
