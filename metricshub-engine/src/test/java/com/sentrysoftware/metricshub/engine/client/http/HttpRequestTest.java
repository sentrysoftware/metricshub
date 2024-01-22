package com.sentrysoftware.metricshub.engine.client.http;

import static com.sentrysoftware.metricshub.engine.constants.Constants.EMBEDDED_FILE_1_REF;
import static com.sentrysoftware.metricshub.engine.constants.Constants.EMBEDDED_FILE_2_REF;
import static com.sentrysoftware.metricshub.engine.constants.Constants.HOST;
import static com.sentrysoftware.metricshub.engine.constants.Constants.MY_CONNECTOR_1_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

import com.sentrysoftware.metricshub.engine.client.http.HttpRequest.HttpRequestBuilder;
import com.sentrysoftware.metricshub.engine.configuration.HttpConfiguration;
import com.sentrysoftware.metricshub.engine.connector.model.common.EmbeddedFile;
import com.sentrysoftware.metricshub.engine.strategy.utils.EmbeddedFileHelper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class HttpRequestTest {

	private static final String CONNECTION_KEEP_ALIVE_HEADER = "Connection: keep-alive";
	private static final String BODY =
		"""
		{ "key" : "value" }
		""";
	private static final HttpConfiguration HTTP_CONFIG = HttpConfiguration.builder().build();
	private static Map<String, EmbeddedFile> commandLineEmbeddedFiles;

	/**
	 * Setup unit tests.
	 */
	@BeforeAll
	static void setup() {
		commandLineEmbeddedFiles = new HashMap<>();
	}

	@BeforeEach
	void clearEmbeddedFiles() {
		commandLineEmbeddedFiles.clear();
	}

	@Test
	void testHeaderBuilder() throws IOException {
		{
			final HttpRequest request = HttpRequest
				.builder()
				.hostname(HOST)
				.httpConfiguration(HTTP_CONFIG)
				.header(CONNECTION_KEEP_ALIVE_HEADER, MY_CONNECTOR_1_NAME, HOST)
				.build();

			assertEquals(new StringHeader(CONNECTION_KEEP_ALIVE_HEADER), request.getHeader());
		}

		{
			final HttpRequest request = HttpRequest
				.builder()
				.hostname(HOST)
				.httpConfiguration(HTTP_CONFIG)
				.header(null, MY_CONNECTOR_1_NAME, HOST)
				.build();

			assertNull(request.getHeader());
		}

		{
			final EmbeddedFile expectedEmbeddedFile = new EmbeddedFile(
				CONNECTION_KEEP_ALIVE_HEADER,
				null,
				EMBEDDED_FILE_1_REF
			);
			commandLineEmbeddedFiles.put(EMBEDDED_FILE_1_REF, expectedEmbeddedFile);

			try (final MockedStatic<EmbeddedFileHelper> mockedEmbeddedFileHelper = mockStatic(EmbeddedFileHelper.class)) {
				mockedEmbeddedFileHelper
					.when(() -> EmbeddedFileHelper.findEmbeddedFiles(anyString()))
					.thenReturn(commandLineEmbeddedFiles);

				final HttpRequest request = HttpRequest
					.builder()
					.hostname(HOST)
					.httpConfiguration(HTTP_CONFIG)
					.header(EMBEDDED_FILE_1_REF, MY_CONNECTOR_1_NAME, HOST)
					.build();

				assertEquals(new EmbeddedFileHeader(expectedEmbeddedFile), request.getHeader());
			}
		}
	}

	@Test
	void testBodyBuilder() throws IOException {
		{
			final HttpRequest request = HttpRequest
				.builder()
				.hostname(HOST)
				.httpConfiguration(HTTP_CONFIG)
				.body(BODY, MY_CONNECTOR_1_NAME, HOST)
				.build();

			assertEquals(new StringBody(BODY), request.getBody());
		}

		{
			final HttpRequest request = HttpRequest
				.builder()
				.hostname(HOST)
				.httpConfiguration(HTTP_CONFIG)
				.body(null, MY_CONNECTOR_1_NAME, HOST)
				.build();

			assertNull(request.getBody());
		}

		{
			final EmbeddedFile expectedEmbeddedFile = new EmbeddedFile(BODY, null, EMBEDDED_FILE_1_REF);
			commandLineEmbeddedFiles.put(EMBEDDED_FILE_1_REF, expectedEmbeddedFile);

			try (final MockedStatic<EmbeddedFileHelper> mockedEmbeddedFileHelper = mockStatic(EmbeddedFileHelper.class)) {
				mockedEmbeddedFileHelper
					.when(() -> EmbeddedFileHelper.findEmbeddedFiles(anyString()))
					.thenReturn(commandLineEmbeddedFiles);

				final HttpRequest request = HttpRequest
					.builder()
					.hostname(HOST)
					.httpConfiguration(HTTP_CONFIG)
					.body(EMBEDDED_FILE_1_REF, MY_CONNECTOR_1_NAME, HOST)
					.build();

				assertEquals(new EmbeddedFileBody(expectedEmbeddedFile), request.getBody());
			}
		}
	}

	@Test
	void testGetHttpEmbeddedFileFailsOnManyEmbeddedFiles() {
		commandLineEmbeddedFiles.put(
			EMBEDDED_FILE_1_REF,
			new EmbeddedFile(CONNECTION_KEEP_ALIVE_HEADER, null, EMBEDDED_FILE_1_REF)
		);

		commandLineEmbeddedFiles.put(
			EMBEDDED_FILE_2_REF,
			new EmbeddedFile(CONNECTION_KEEP_ALIVE_HEADER, null, EMBEDDED_FILE_2_REF)
		);
		try (final MockedStatic<EmbeddedFileHelper> mockedEmbeddedFileHelper = mockStatic(EmbeddedFileHelper.class)) {
			mockedEmbeddedFileHelper
				.when(() -> EmbeddedFileHelper.findEmbeddedFiles(anyString()))
				.thenReturn(commandLineEmbeddedFiles);

			assertThrows(
				IllegalStateException.class,
				() ->
					HttpRequestBuilder.getHttpEmbeddedFile(
						EMBEDDED_FILE_1_REF + " " + EMBEDDED_FILE_2_REF,
						"header",
						MY_CONNECTOR_1_NAME,
						HOST
					)
			);
		}
	}
}
