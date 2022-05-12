package com.sentrysoftware.matrix.connector.model.common.http.header;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class StringHeaderTest {

	@Test
	void testGetContent() {

		char[] password = {'p','w'};

		{
			StringHeader stringHeader = StringHeader
			.builder()
			.header("Cookie: PVEAuthCookie=PVE:root@pam:6273E79B::haiZ/i60")
			.build();

			assertDoesNotThrow(() -> stringHeader.getContent("username", password, "abc"));
		}

		{
			StringHeader stringHeader = StringHeader
			.builder()
			.header("Cookie")
			.build();

			assertThrows(IllegalArgumentException.class, () -> stringHeader.getContent("username", password, "abc"));
		}
	}
}
