package org.sentrysoftware.metricshub.cli.http;

import static org.junit.jupiter.api.Assertions.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.cli.service.protocol.HttpConfigCli;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import picocli.CommandLine;
import picocli.CommandLine.ParameterException;

class HttpCliTest {

	HttpCli httpCli;
	CommandLine commandLine;
	PrintWriter printWriter = new PrintWriter(new StringWriter());

	private final static String HOSTNAME = "";
	private final static String HTTP_GET = "";
	private final static String URL = "";
	private final static Map<String, String> HEADERS = Map.of("Content-Type", "application/xml");
	private final static String BODY = "<aaaLogin inName=\" inPassword=\" />";
	private final static String AUTHENTICATION_TOKEN = "Q5SD7SDF2BCV8ZER4";
	private final static String RESULT_CONTENT = "all";
	private final static String FILE_HEADER = 
			"Content-Type: application/xml\r\n"
			+ "User-Agent: Mozilla/5.0\r\n"
			+ "Accept: text/html\r\n"
			+ "Accept-Language: en-US\r\n"
			+ "Cache-Control: no-cache";

	void initCli() {
		httpCli = new HttpCli();
		commandLine = new CommandLine(httpCli);
	}

	void initHttpCli() {
		httpCli = new HttpCli();
		final CommandLine commandLine = new CommandLine(httpCli);

		commandLine.execute(
			"hostname",
			"--http",
			"--http-username",
			"username",
			"--http-password",
			"password",
			"--http-path",
			"path",
			"--http-url",
			"url",
			"--http-header",
			"key1, value1",
			"--http-header",
			"key2, value2",
			"--http-body",
			"body",
			"--http-authenticationToken",
			"authenticationToken",
			"--http-resultContent",
			"all"
		);
	}

	@Test
	void testGetQuery() {
		initCli();
		httpCli.setHostname(HOSTNAME);
		httpCli.setMethod(HTTP_GET);
		httpCli.setUrl(URL);
		httpCli.setHeaders(HEADERS);
		httpCli.setBody(BODY);
		httpCli.setAuthenticationToken(AUTHENTICATION_TOKEN);
		httpCli.setResultContent(RESULT_CONTENT);

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
	void testGetHeader() throws Exception{
		initCli();
		httpCli.setHeaders(HEADERS);
		assertEquals("Content-Type: application/xml\r\n", httpCli.getHeaderContent());
		httpCli.setHeaders(null);
		httpCli.setHeaderFile("src/test/resources/cli/header.txt");
		assertEquals(FILE_HEADER, httpCli.getHeaderContent());
	}

	@Test
	void testGetBody() throws Exception{
		initCli();
		httpCli.setBody(BODY);
		assertEquals(BODY, httpCli.getBody());
		httpCli.setBody(null);
		httpCli.setBodyFile("src/test/resources/cli/body.txt");
		assertEquals(BODY, httpCli.getBodyContent());
	}

	@Test
	void testValidate() {
		initCli();

		// HttpConfigCli must be configured.
		ParameterException exceptionMessage = assertThrows(ParameterException.class, () -> httpCli.validate());
		assertEquals("HTTP/HTTPS protocol must be configured: --http, --https.", exceptionMessage.getMessage());
		httpCli.setHttpConfigCli(new HttpConfigCli());

		// Method must be : GET/POST/PUT/DELETE or empty (default: GET)
		httpCli.setMethod("WrongMethod");
		exceptionMessage = assertThrows(ParameterException.class, () -> httpCli.validate());
		assertEquals("Unknown HTTP request method: WrongMethod.", exceptionMessage.getMessage());
		httpCli.setMethod("get");

		// At least URL or Path must be speficied
		exceptionMessage = assertThrows(ParameterException.class, () -> httpCli.validate());
		assertEquals("At least one of the parameters must be specified: --http-url or --http-path.", exceptionMessage.getMessage());
		httpCli.setUrl(URL);

		// Wrong headerFile path
		httpCli.setHeaderFile("wrong/path/header.txt");
		exceptionMessage = assertThrows(ParameterException.class, () -> httpCli.validate());
		assertTrue(exceptionMessage.getMessage().contains("Error while reading header file"));

		// Only one header (headers or headerFile) must be specified.
		httpCli.setHeaders(HEADERS);
		httpCli.setHeaderFile("src/test/resources/cli/header.txt");
		exceptionMessage = assertThrows(ParameterException.class, () -> httpCli.validate());
		assertEquals("Conflict - Two headers have been configured: --http-header and --http-header-file.", exceptionMessage.getMessage());
		httpCli.setHeaders(null);

		// Wrong bodyFile path
		httpCli.setBodyFile("wrong/path/body.txt");
		exceptionMessage = assertThrows(ParameterException.class, () -> httpCli.validate());
		assertTrue(exceptionMessage.getMessage().contains("Error while reading body file"));

		// Only one body (body or bodyFile) must be specified.
		httpCli.setBody(BODY);
		httpCli.setBodyFile("src/test/resources/cli/body.txt");
		exceptionMessage = assertThrows(ParameterException.class, () -> httpCli.validate());
		assertEquals("Conflict - Two bodies have been configured: --http-body and --http-body-file.", exceptionMessage.getMessage());
		httpCli.setBody(null);

		// Result Content must be : All, Body, Header, httpStatus or http_status
		httpCli.setResultContent("wrongResultContent");
		IllegalArgumentException illegalExceptionMessage = assertThrows(IllegalArgumentException.class, () -> httpCli.validate());
		assertTrue(illegalExceptionMessage.getMessage().contains("is not a supported ResultContent"));
		httpCli.setResultContent(RESULT_CONTENT);

		assertDoesNotThrow(() -> httpCli.validate());
	}
}
