package com.sentrysoftware.matrix.connector.model.common.http.header;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.common.EmbeddedFile;

class EmbeddedFileHeaderTest {

	@Test
	void testGetByName() {

		char[] password = {'p','w'};

		{
			EmbeddedFile header = EmbeddedFile
			.builder()
			.content("Cookie: PVEAuthCookie=PVE:root@pam:6273E79B::haiZ/i60")
			.build();

			EmbeddedFileHeader embeddedHeader = EmbeddedFileHeader
					.builder()
					.header(header)
					.build();

			assertDoesNotThrow(() -> embeddedHeader.getContent("username", password, "abc"));
		}

		{
			EmbeddedFile header = EmbeddedFile
			.builder()
			.content("Cookie")
			.build();

			EmbeddedFileHeader embeddedHeader = EmbeddedFileHeader
					.builder()
					.header(header)
					.build();

			assertThrows(IllegalArgumentException.class, () -> embeddedHeader.getContent("username", password, "abc"));
		}
	}
}
