package org.sentrysoftware.metricshub.engine.client.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.sentrysoftware.metricshub.engine.configuration.HttpConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.common.EmbeddedFile;
import org.sentrysoftware.metricshub.engine.constants.Constants;
import org.sentrysoftware.metricshub.engine.strategy.utils.EmbeddedFileHelper;

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
				.hostname(Constants.HOST)
				.httpConfiguration(HTTP_CONFIG)
				.header(CONNECTION_KEEP_ALIVE_HEADER, Constants.MY_CONNECTOR_1_NAME, Constants.HOST)
				.build();

			assertEquals(new StringHeader(CONNECTION_KEEP_ALIVE_HEADER), request.getHeader());
		}

		{
			final HttpRequest request = HttpRequest
				.builder()
				.hostname(Constants.HOST)
				.httpConfiguration(HTTP_CONFIG)
				.header(null, Constants.MY_CONNECTOR_1_NAME, Constants.HOST)
				.build();

			assertNull(request.getHeader());
		}

		{
			final EmbeddedFile expectedEmbeddedFile = new EmbeddedFile(
				CONNECTION_KEEP_ALIVE_HEADER,
				null,
				Constants.EMBEDDED_FILE_1_REF
			);
			commandLineEmbeddedFiles.put(Constants.EMBEDDED_FILE_1_REF, expectedEmbeddedFile);

			try (final MockedStatic<EmbeddedFileHelper> mockedEmbeddedFileHelper = mockStatic(EmbeddedFileHelper.class)) {
				mockedEmbeddedFileHelper
					.when(() -> EmbeddedFileHelper.findEmbeddedFiles(anyString()))
					.thenReturn(commandLineEmbeddedFiles);

				final HttpRequest request = HttpRequest
					.builder()
					.hostname(Constants.HOST)
					.httpConfiguration(HTTP_CONFIG)
					.header(Constants.EMBEDDED_FILE_1_REF, Constants.MY_CONNECTOR_1_NAME, Constants.HOST)
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
				.hostname(Constants.HOST)
				.httpConfiguration(HTTP_CONFIG)
				.body(BODY, Constants.MY_CONNECTOR_1_NAME, Constants.HOST)
				.build();

			assertEquals(new StringBody(BODY), request.getBody());
		}

		{
			final HttpRequest request = HttpRequest
				.builder()
				.hostname(Constants.HOST)
				.httpConfiguration(HTTP_CONFIG)
				.body(null, Constants.MY_CONNECTOR_1_NAME, Constants.HOST)
				.build();

			assertNull(request.getBody());
		}

		{
			final EmbeddedFile expectedEmbeddedFile = new EmbeddedFile(BODY, null, Constants.EMBEDDED_FILE_1_REF);
			commandLineEmbeddedFiles.put(Constants.EMBEDDED_FILE_1_REF, expectedEmbeddedFile);

			try (final MockedStatic<EmbeddedFileHelper> mockedEmbeddedFileHelper = mockStatic(EmbeddedFileHelper.class)) {
				mockedEmbeddedFileHelper
					.when(() -> EmbeddedFileHelper.findEmbeddedFiles(anyString()))
					.thenReturn(commandLineEmbeddedFiles);

				final HttpRequest request = HttpRequest
					.builder()
					.hostname(Constants.HOST)
					.httpConfiguration(HTTP_CONFIG)
					.body(Constants.EMBEDDED_FILE_1_REF, Constants.MY_CONNECTOR_1_NAME, Constants.HOST)
					.build();

				assertEquals(new EmbeddedFileBody(expectedEmbeddedFile), request.getBody());
			}
		}
	}

	@Test
	void testGetHttpEmbeddedFileFailsOnManyEmbeddedFiles() {
		commandLineEmbeddedFiles.put(
			Constants.EMBEDDED_FILE_1_REF,
			new EmbeddedFile(CONNECTION_KEEP_ALIVE_HEADER, null, Constants.EMBEDDED_FILE_1_REF)
		);

		commandLineEmbeddedFiles.put(
			Constants.EMBEDDED_FILE_2_REF,
			new EmbeddedFile(CONNECTION_KEEP_ALIVE_HEADER, null, Constants.EMBEDDED_FILE_2_REF)
		);
		try (final MockedStatic<EmbeddedFileHelper> mockedEmbeddedFileHelper = mockStatic(EmbeddedFileHelper.class)) {
			mockedEmbeddedFileHelper
				.when(() -> EmbeddedFileHelper.findEmbeddedFiles(anyString()))
				.thenReturn(commandLineEmbeddedFiles);

			assertThrows(
				IllegalStateException.class,
				() ->
					HttpRequest.HttpRequestBuilder.getHttpEmbeddedFile(
						Constants.EMBEDDED_FILE_1_REF + " " + Constants.EMBEDDED_FILE_2_REF,
						"header",
						Constants.MY_CONNECTOR_1_NAME,
						Constants.HOST
					)
			);
		}
	}
}
