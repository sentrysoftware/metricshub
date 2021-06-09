package com.sentrysoftware.matrix.engine.strategy.matsya;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.sentrysoftware.matrix.common.exception.LocalhostCheckException;
import com.sentrysoftware.matrix.common.helpers.NetworkHelper;

class MatsyaClientsExecutorTest {

	@Test
	void testExecute() throws InterruptedException, ExecutionException, TimeoutException {
		assertEquals("value", new MatsyaClientsExecutor().execute(() -> "value", 10L));
	}

	@Test
	void testBuildWMINetworkResource() throws LocalhostCheckException {
		try (MockedStatic<NetworkHelper> networkHelper = mockStatic(NetworkHelper.class)) {
			networkHelper.when(() -> NetworkHelper.isLocalhost("hostname")).thenReturn(true);
			assertEquals("root/cimv2", new MatsyaClientsExecutor().buildWMINetworkResource("hostname", "root/cimv2"));
		}

		try (MockedStatic<NetworkHelper> networkHelper = mockStatic(NetworkHelper.class)) {
			networkHelper.when(() -> NetworkHelper.isLocalhost("hostname")).thenReturn(false);
			assertEquals("\\\\hostname\\root/cimv2", new MatsyaClientsExecutor()
					.buildWMINetworkResource("hostname", "root/cimv2"));
		}
	}

	@Test
	void testBuildWMITable() {
		final List<List<String>> result = new MatsyaClientsExecutor().buildWMITable(
				Arrays.asList(
						Map.of("DeviceID", "1.1","Name", "Disk 1"),
						Map.of("DeviceID", "1.2","Name", "Disk 2"),
						Map.of("DeviceID", "1.3","Name", "Disk 3")), 
				Arrays.asList("DeviceID", "Name"));
		final List<List<String>> expected = Arrays.asList(
				Arrays.asList("1.1", "Disk 1"),
				Arrays.asList("1.2", "Disk 2"),
				Arrays.asList("1.3", "Disk 3"));

		assertEquals(expected, result);
	}
}
