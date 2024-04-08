package org.sentrysoftware.metricshub.extension.http;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.sentrysoftware.metricshub.engine.common.helpers.KnownMonitorType.HOST;
import static org.sentrysoftware.metricshub.extension.http.HttpExtension.HTTP_UP_METRIC;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sentrysoftware.metricshub.engine.client.http.HttpRequest;
import org.sentrysoftware.metricshub.engine.common.exception.InvalidConfigurationException;
import org.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.HttpCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.HttpSource;
import org.sentrysoftware.metricshub.engine.telemetry.Monitor;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

@ExtendWith(MockitoExtension.class)
class HttpExtensionTest {

	@Mock
	private HttpRequestExecutor httpRequestExecutorMock;

	@InjectMocks
	private HttpExtension httpExtension;

	static Map<String, Map<String, Monitor>> monitors;

	private static final String HOSTNAME = "test-host";

	/**
	 * Creates and returns a TelemetryManager instance with an HTTP configuration.
	 *
	 * @return A TelemetryManager instance configured with an HTTP configuration.
	 */
	private TelemetryManager createTelemetryManagerWithHttpConfig() {
		Monitor hostMonitor = Monitor.builder().type(HOST.getKey()).isEndpoint(true).build();

		monitors = new HashMap<>(Map.of(HOST.getKey(), Map.of(HOSTNAME, hostMonitor)));

		// Create a telemetry manager
		return TelemetryManager
			.builder()
			.monitors(monitors)
			.hostConfiguration(
				HostConfiguration
					.builder()
					.hostId(HOSTNAME)
					.hostname(HOSTNAME)
					.configurations(Map.of(HttpConfiguration.class, HttpConfiguration.builder().build()))
					.build()
			)
			.build();
	}

	@Test
	void testCheckHttpDownHealth() {
		// Create a telemetry manager using an HTTP HostConfiguration.
		final TelemetryManager telemetryManager = createTelemetryManagerWithHttpConfig();

		// Mock HTTP protocol health check response
		doReturn(null)
			.when(httpRequestExecutorMock)
			.executeHttp(any(HttpRequest.class), anyBoolean(), any(TelemetryManager.class));

		httpExtension.checkProtocol(telemetryManager, System.currentTimeMillis());

		assertEquals(HttpExtension.DOWN, telemetryManager.getEndpointHostMonitor().getMetric(HTTP_UP_METRIC).getValue());
	}

	@Test
	void testCheckHttpUpHealth() {
		// Create a telemetry manager using an HTTP HostConfiguration.
		final TelemetryManager telemetryManager = createTelemetryManagerWithHttpConfig();

		// Mock HTTP protocol health check response
		doReturn("success")
			.when(httpRequestExecutorMock)
			.executeHttp(any(HttpRequest.class), anyBoolean(), any(TelemetryManager.class));

		httpExtension.checkProtocol(telemetryManager, System.currentTimeMillis());

		assertEquals(HttpExtension.UP, telemetryManager.getEndpointHostMonitor().getMetric(HTTP_UP_METRIC).getValue());
	}

	@Test
	void testIsValidConfiguration() {
		assertTrue(httpExtension.isValidConfiguration(HttpConfiguration.builder().build()));
		assertFalse(
			httpExtension.isValidConfiguration(
				new IConfiguration() {
					@Override
					public void validateConfiguration(String resourceKey) throws InvalidConfigurationException {}
				}
			)
		);
	}

	@Test
	void testGetSupportedSources() {
		assertFalse(httpExtension.getSupportedSources().isEmpty());
		assertTrue(httpExtension.getSupportedSources().contains(HttpSource.class));
	}

	@Test
	void testGetSupportedCriteria() {
		assertFalse(httpExtension.getSupportedCriteria().isEmpty());
		assertTrue(httpExtension.getSupportedCriteria().contains(HttpCriterion.class));
	}

	@Test
	void testGetConfigurationToSourceMapping() {
		assertFalse(httpExtension.getConfigurationToSourceMapping().isEmpty());
	}

	@Test
	void testIsSupportedConfigurationType() {
		assertTrue(httpExtension.isSupportedConfigurationType("http"));
		assertFalse(httpExtension.isSupportedConfigurationType("snmp"));
	}
}
