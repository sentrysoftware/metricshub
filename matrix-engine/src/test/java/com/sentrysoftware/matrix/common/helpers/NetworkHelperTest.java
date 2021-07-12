package com.sentrysoftware.matrix.common.helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.sentrysoftware.matrix.common.exception.LocalhostCheckException;

class NetworkHelperTest {

	@Test
	void testIsLocalhost() throws Exception {

		assertTrue(NetworkHelper.isLocalhost("localhost"));
		assertTrue(NetworkHelper.isLocalhost("127.0.0.1"));
		assertTrue(NetworkHelper.isLocalhost("0:0:0:0:0:0:0:1"));
		assertTrue(NetworkHelper.isLocalhost("::1"));
		assertTrue(NetworkHelper.isLocalhost("0000:0000:0000:0000:0000:0000:0000:0001"));
		assertTrue(NetworkHelper.isLocalhost(InetAddress.getLocalHost().getHostName()));
		assertThrows(LocalhostCheckException.class, () -> NetworkHelper.isLocalhost(UUID.randomUUID().toString()));
		assertThrows(IllegalArgumentException.class, () -> NetworkHelper.isLocalhost(" "));
		assertThrows(IllegalArgumentException.class, () -> NetworkHelper.isLocalhost(""));
		assertThrows(IllegalArgumentException.class, () ->NetworkHelper.isLocalhost(null));
		try (MockedStatic<InetAddress> inetAddressMock = mockStatic(InetAddress.class)) {
			final String randomHost = UUID.randomUUID().toString();
			inetAddressMock.when(() -> InetAddress.getByName(eq(randomHost))).thenReturn(null);
			assertThrows(LocalhostCheckException.class, () -> NetworkHelper.isLocalhost(randomHost));
		}

		try (MockedStatic<NetworkInterface> networkInterface = mockStatic(NetworkInterface.class)) {
			networkInterface.when(() -> NetworkInterface.getByInetAddress(InetAddress.getLocalHost())).thenThrow(new SocketException());
			assertThrows(LocalhostCheckException.class, () -> NetworkHelper.isLocalhost(InetAddress.getLocalHost().getHostName()));
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
		assertThrows(LocalhostCheckException.class, () -> NetworkHelper.getFqdn("FOO"));

		// hostname can be resolved
		hostname = "localhost";
		assertEquals(hostname, NetworkHelper.getFqdn(hostname));
	}
}
