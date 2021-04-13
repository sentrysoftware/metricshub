package com.sentrysoftware.matrix.common.helpers;

import static org.junit.jupiter.api.Assertions.*;

import java.net.InetAddress;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class NetworkHelperTest {

	@Test
	void testIsLocalhost() throws Exception {
		assertTrue(NetworkHelper.isLocalhost("localhost"));
		assertTrue(NetworkHelper.isLocalhost("127.0.0.1"));
		assertTrue(NetworkHelper.isLocalhost("0:0:0:0:0:0:0:1"));
		assertTrue(NetworkHelper.isLocalhost("::1"));
		assertTrue(NetworkHelper.isLocalhost("0000:0000:0000:0000:0000:0000:0000:0001"));
		assertTrue(NetworkHelper.isLocalhost(InetAddress.getLocalHost().getHostName()));
		assertFalse(NetworkHelper.isLocalhost(UUID.randomUUID().toString()));
		assertFalse(NetworkHelper.isLocalhost(" "));
		assertFalse(NetworkHelper.isLocalhost(""));
		assertFalse(NetworkHelper.isLocalhost(null));

	}

}
