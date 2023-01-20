package com.sentrysoftware.matrix.common.helpers;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.WHITE_SPACE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

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
		assertTrue(NetworkHelper.isLocalhost(WHITE_SPACE));
		assertTrue(NetworkHelper.isLocalhost(""));
		assertTrue(NetworkHelper.isLocalhost(null));
		try (MockedStatic<InetAddress> inetAddressMock = mockStatic(InetAddress.class)) {
			final String randomHost = UUID.randomUUID().toString();
			inetAddressMock.when(() -> InetAddress.getByName(eq(randomHost))).thenReturn(null);
			assertFalse(NetworkHelper.isLocalhost(randomHost));
		}

		try (MockedStatic<NetworkInterface> networkInterface = mockStatic(NetworkInterface.class)) {
			final String randomHost = UUID.randomUUID().toString();
			final InetAddress mockedInetAddress = Mockito.mock(InetAddress.class);
			try (MockedStatic<InetAddress> inetAddressMock = mockStatic(InetAddress.class)) {
				inetAddressMock.when(() -> InetAddress.getByName(eq(randomHost))).thenReturn(mockedInetAddress);
				networkInterface.when(() -> NetworkInterface.getByInetAddress(mockedInetAddress)).thenThrow(new SocketException());
				assertFalse(NetworkHelper.isLocalhost(randomHost));
			}
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
		hostname = UUID.randomUUID().toString();
		assertEquals(hostname, NetworkHelper.getFqdn(hostname));

		// hostname can be resolved
		assertNotNull(NetworkHelper.getFqdn("localhost"));
	}

	@Test
	void testResolveDns() throws Exception {

		// hostname is null
		assertNull(NetworkHelper.resolveDns(null));

		// hostname is blank
		String hostname = "   ";
		assertNull(NetworkHelper.resolveDns(hostname));
		
		// hostname is empty
		hostname = "";
		assertNull(NetworkHelper.resolveDns(hostname));
		
		// hostname is an illegal hostname
		hostname = "-host";
		assertNull(NetworkHelper.resolveDns(hostname));
		
		// hostname can be resolved
		hostname = "localhost";
		assertNotNull(NetworkHelper.resolveDns(hostname));
	}
}