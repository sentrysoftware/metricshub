package org.sentrysoftware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.apache.http.protocol.HttpRequestExecutor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.sentrysoftware.http.HttpClient;
import org.sentrysoftware.ipmi.client.IpmiClient;
import org.sentrysoftware.ipmi.client.IpmiClientConfiguration;
import org.sentrysoftware.ipmi.core.api.async.messages.IpmiResponse;
import org.sentrysoftware.metricshub.engine.client.http.HttpRequest;
import org.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.HttpConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind;
import org.sentrysoftware.metricshub.engine.connector.model.common.ResultContent;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import org.sentrysoftware.metricshub.extension.ipmi.IpmiConfiguration;
import org.sentrysoftware.metricshub.extension.ipmi.IpmiRequestExecutor;

public class IpmiRequestExecutorTest {

	@Test
	void testExecuteIpmiDetection() {
		try (MockedStatic<IpmiClient> ipmiClientMock = mockStatic(IpmiClient.class)) {
			final String username = "username";
			final char[] password = "password".toCharArray();
			final int timeout = 120;
			final byte[] BMC_KEY = new byte[] { 0x06, 0x66 };
			final String expected = "result";

			final IpmiConfiguration ipmiConfiguration = IpmiConfiguration
				.builder()
				.username(username)
				.password(password)
				.timeout(timeout * 1L)
				.bmcKey(BMC_KEY)
				.build();

			ipmiClientMock
				.when(() -> IpmiClient.getChassisStatusAsStringResult(any(IpmiClientConfiguration.class)))
				.thenReturn(expected);

			final String hostname = "hostname";

			final String actual = new IpmiRequestExecutor().executeIpmiDetection(hostname, ipmiConfiguration);

			assertEquals(expected, actual);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (TimeoutException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	void testExecuteIpmiDetectionThrowsException() {
		try (MockedStatic<IpmiClient> ipmiClientMock = mockStatic(IpmiClient.class)) {
			final String username = "username";
			final char[] password = "password".toCharArray();
			final int timeout = 120;
			final byte[] BMC_KEY = new byte[] { 0x06, 0x66 };

			final IpmiConfiguration ipmiConfiguration = IpmiConfiguration
				.builder()
				.username(username)
				.password(password)
				.timeout(timeout * 1L)
				.bmcKey(BMC_KEY)
				.build();

			ipmiClientMock
				.when(() -> IpmiClient.getChassisStatus(any(IpmiClientConfiguration.class)))
				.thenThrow(new InterruptedException());

			final String hostname = "hostname";

			final String result = new IpmiRequestExecutor().executeIpmiDetection(hostname, ipmiConfiguration);

			assertNull(result);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (TimeoutException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	void testExecuteIpmiGetSensors() {
		try (MockedStatic<IpmiClient> ipmiClientMock = mockStatic(IpmiClient.class)) {
			final String username = "username";
			final char[] password = "password".toCharArray();
			final int timeout = 120;
			final byte[] BMC_KEY = new byte[] { 0x06, 0x66 };
			final String expected = "result";

			final IpmiConfiguration ipmiConfiguration = IpmiConfiguration
				.builder()
				.username(username)
				.password(password)
				.timeout(timeout * 1L)
				.bmcKey(BMC_KEY)
				.build();

			ipmiClientMock
				.when(() -> IpmiClient.getFrusAndSensorsAsStringResult(any(IpmiClientConfiguration.class)))
				.thenReturn(expected);

			final String hostname = "hostname";

			assertEquals(expected, new IpmiRequestExecutor().executeIpmiGetSensors(hostname, ipmiConfiguration));
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (TimeoutException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	void testExecuteIpmiGetSensorsThrowsException() {
		try (MockedStatic<IpmiClient> ipmiClientMock = mockStatic(IpmiClient.class)) {
			final String username = "username";
			final char[] password = "password".toCharArray();
			final int timeout = 120;
			final byte[] BMC_KEY = new byte[] { 0x06, 0x66 };

			final IpmiConfiguration ipmiConfiguration = IpmiConfiguration
				.builder()
				.username(username)
				.password(password)
				.timeout(timeout * 1L)
				.bmcKey(BMC_KEY)
				.build();

			ipmiClientMock
				.when(() -> IpmiClient.getFrusAndSensorsAsStringResult(any(IpmiClientConfiguration.class)))
				.thenThrow(new InterruptedException());

			final String hostname = "hostname";

			assertThrows(
				InterruptedException.class,
				() -> new IpmiRequestExecutor().executeIpmiGetSensors(hostname, ipmiConfiguration)
			);
		}
	}
}
