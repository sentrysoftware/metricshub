package com.sentrysoftware.matrix.common.helpers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

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
		assertTrue(NetworkHelper.isLocalhost(" "));
		assertTrue(NetworkHelper.isLocalhost(""));
		assertTrue(NetworkHelper.isLocalhost(null));
		try (MockedStatic<InetAddress> inetAddressMock = mockStatic(InetAddress.class)) {
			final String randomHost = UUID.randomUUID().toString();
			inetAddressMock.when(() -> InetAddress.getByName(eq(randomHost))).thenReturn(null);
			assertFalse(NetworkHelper.isLocalhost(randomHost));
		}

		try (MockedStatic<NetworkInterface> networkInterface = mockStatic(NetworkInterface.class)) {
			networkInterface.when(() -> NetworkInterface.getByInetAddress(InetAddress.getLocalHost())).thenThrow(new SocketException());
			assertFalse(NetworkHelper.isLocalhost(InetAddress.getLocalHost().getHostName()));
		}
	}

	@Test
	void testGetFqdn() throws Exception {

		// hostname is null
		assertNull(NetworkHelper.getFqdn(null));

		// hostname is blank
		String hostname = "   ";
		assertEquals(hostname, NetworkHelper.getFqdn(hostname));

		// hostname cannot be resolved
		assertThrows(UnknownHostException.class, () -> NetworkHelper.getFqdn(UUID.randomUUID().toString()));

		// hostname can be resolved
		assertNotNull(NetworkHelper.getFqdn(hostname));
	}
}
