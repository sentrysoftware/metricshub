package org.sentrysoftware.metricshub.engine.connector.model.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants;

class EmbeddedFileTest {

	@Test
	void testDescription() {
		assertEquals(
			"EmbeddedFile 1: script.bat",
			EmbeddedFile.builder().content("value".getBytes()).filename("script.bat").id(1).build().description()
		);
		assertEquals(
			"EmbeddedFile 1: <inline>",
			EmbeddedFile.builder().content("value".getBytes()).id(1).build().description()
		);
	}

	@Test
	void testGetContentAsString() {
		final EmbeddedFile embeddedFile = EmbeddedFile
			.builder()
			.content("value".getBytes())
			.filename("script.bat")
			.id(1)
			.build();
		assertEquals("value", embeddedFile.getContentAsString());
	}

	@Test
	void testUpdate() {
		final EmbeddedFile embeddedFile = EmbeddedFile
			.builder()
			.content("value".getBytes())
			.filename("script.bat")
			.id(1)
			.build();
		embeddedFile.update(value -> value + " updated");
		assertEquals("value updated", embeddedFile.getContentAsString());
	}

	@Test
	void testFromString() {
		final EmbeddedFile embeddedFile = EmbeddedFile.fromString("value");
		final EmbeddedFile expected = EmbeddedFile.builder().content("value".getBytes()).build();
		assertEquals(expected, embeddedFile);
	}

	@Test
	void testGetFileExtension() {
		{
			final EmbeddedFile embeddedFile = EmbeddedFile
				.builder()
				.content("value".getBytes())
				.filename("script.bat")
				.id(1)
				.build();
			assertEquals(".bat", embeddedFile.getFileExtension());
		}
		{
			final EmbeddedFile embeddedFile = EmbeddedFile
				.builder()
				.content("value".getBytes())
				.filename("script")
				.id(1)
				.build();
			assertEquals(MetricsHubConstants.EMPTY, embeddedFile.getFileExtension());
		}
		{
			final EmbeddedFile embeddedFile = EmbeddedFile.builder().content("value".getBytes()).id(1).build();
			assertEquals(MetricsHubConstants.EMPTY, embeddedFile.getFileExtension());
		}
	}

	@Test
	void testGetBaseName() {
		{
			final EmbeddedFile embeddedFile = EmbeddedFile
				.builder()
				.content("value".getBytes())
				.filename("script.bat")
				.id(1)
				.build();
			assertEquals("script", embeddedFile.getBaseName());
		}
		{
			final EmbeddedFile embeddedFile = EmbeddedFile
				.builder()
				.content("value".getBytes())
				.filename("script")
				.id(1)
				.build();
			assertEquals("script", embeddedFile.getBaseName());
		}
		{
			final EmbeddedFile embeddedFile = EmbeddedFile.builder().content("value".getBytes()).id(1).build();
			assertEquals(MetricsHubConstants.EMPTY, embeddedFile.getBaseName());
		}
	}
}
