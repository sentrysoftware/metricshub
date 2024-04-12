package org.sentrysoftware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.sentrysoftware.metricshub.engine.common.helpers.KnownMonitorType.HOST;
import static org.sentrysoftware.metricshub.engine.strategy.collect.ProtocolHealthCheckStrategy.DOWN;
import static org.sentrysoftware.metricshub.engine.strategy.collect.ProtocolHealthCheckStrategy.UP;
import static org.sentrysoftware.metricshub.extension.ipmi.IpmiExtension.IPMI_UP_METRIC;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sentrysoftware.ipmi.client.IpmiClient;
import org.sentrysoftware.ipmi.client.IpmiClientConfiguration;
import org.sentrysoftware.metricshub.engine.common.exception.InvalidConfigurationException;
import org.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.Criterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.HttpCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.IpmiCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.HttpSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.IpmiSource;
import org.sentrysoftware.metricshub.engine.telemetry.Monitor;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import org.sentrysoftware.metricshub.extension.ipmi.IpmiConfiguration;
import org.sentrysoftware.metricshub.extension.ipmi.IpmiExtension;

@ExtendWith(MockitoExtension.class)
class IpmiExtensionTest {

	private static final String HOSTNAME = "hostname";
	private static final String HOST_NAME = "test-host" + UUID.randomUUID();
	private static final String SUCCESS_RESPONSE = "Success";

	@InjectMocks
	private IpmiExtension ipmiExtension;

	/**
	 * Creates and returns a TelemetryManager instance with an IPMI configuration.
	 *
	 * @return A TelemetryManager instance configured with an IPMI configuration.
	 */
	private TelemetryManager createTelemetryManagerWithIpmiConfig() {
		final Monitor hostMonitor = Monitor.builder().type(HOST.getKey()).isEndpoint(true).build();
		final Map<String, Map<String, Monitor>> monitors = new HashMap<>(
			Map.of(HOST.getKey(), Map.of(HOST_NAME, hostMonitor))
		);

		// Create a telemetry manager
		return TelemetryManager
			.builder()
			.monitors(monitors)
			.hostConfiguration(
				HostConfiguration
					.builder()
					.hostId(HOSTNAME)
					.hostname(HOSTNAME)
					.configurations(
						Map.of(
							IpmiConfiguration.class,
							IpmiConfiguration
								.builder()
								.username("username")
								.password("password".toCharArray())
								.bmcKey(null)
								.skipAuth(false)
								.timeout(60L)
								.build()
						)
					)
					.build()
			)
			.build();
	}

	@Test
	void testCheckIpmiUpHealth() {
		// Create a telemetry manager using an IPMI HostConfiguration.
		final TelemetryManager telemetryManager = createTelemetryManagerWithIpmiConfig();

		// The time at which the collect of the protocol up metric is triggered.
		final long collectTime = System.currentTimeMillis();

		// Mock successful IPMI protocol health check response
		try (MockedStatic<IpmiClient> staticIpmiClient = Mockito.mockStatic(IpmiClient.class)) {
			staticIpmiClient
				.when(() -> IpmiClient.getChassisStatusAsStringResult(any(IpmiClientConfiguration.class)))
				.thenReturn(SUCCESS_RESPONSE);

			// Start the IPMI Health Check strategy
			ipmiExtension.checkProtocol(telemetryManager, collectTime);

			assertEquals(UP, telemetryManager.getEndpointHostMonitor().getMetric(IPMI_UP_METRIC).getValue());
		}
	}

	@Test
	void testCheckIpmiDownHealth() {
		// Create a telemetry manager using an IPMI HostConfiguration.
		final TelemetryManager telemetryManager = createTelemetryManagerWithIpmiConfig();

		// The time at which the collect of the protocol up metric is triggered.
		final long collectTime = System.currentTimeMillis();

		// Mock null IPMI protocol health check response
		try (MockedStatic<IpmiClient> staticIpmiClient = Mockito.mockStatic(IpmiClient.class)) {
			staticIpmiClient
				.when(() -> IpmiClient.getChassisStatusAsStringResult(any(IpmiClientConfiguration.class)))
				.thenReturn(null);

			// Start the IPMI Health Check strategy
			ipmiExtension.checkProtocol(telemetryManager, collectTime);

			assertEquals(DOWN, telemetryManager.getEndpointHostMonitor().getMetric(IPMI_UP_METRIC).getValue());
		}
	}

	@Test
	void testGetSupportedSources() {
		assertFalse(ipmiExtension.getSupportedSources().isEmpty());
		assertTrue(ipmiExtension.getSupportedSources().contains(IpmiSource.class));
	}

	@Test
	void testGetSupportedCriteria() {
		assertFalse(ipmiExtension.getSupportedCriteria().isEmpty());
		assertTrue(ipmiExtension.getSupportedCriteria().contains(IpmiCriterion.class));
	}

	@Test
	void testGetConfigurationToSourceMapping() {
		assertFalse(ipmiExtension.getConfigurationToSourceMapping().isEmpty());
	}

	@Test
	void testIsSupportedConfigurationType() {
		assertTrue(ipmiExtension.isSupportedConfigurationType("ipmi"));
		assertFalse(ipmiExtension.isSupportedConfigurationType("snmp"));
	}

	@Test
	void testBuildConfiguration() throws InvalidConfigurationException {
		final ObjectNode configuration = JsonNodeFactory.instance.objectNode();
		configuration.set("username", new TextNode("username"));
		configuration.set("password", new TextNode("password"));
		configuration.set("timeout", new TextNode("120"));
		configuration.set("bmcKey", null);

		assertEquals(
			IpmiConfiguration
				.builder()
				.username("username")
				.password("password".toCharArray())
				.bmcKey(null)
				.timeout(120L)
				.build(),
			ipmiExtension.buildConfiguration("ipmi", configuration, value -> value)
		);

		assertEquals(
			IpmiConfiguration
				.builder()
				.username("username")
				.password("password".toCharArray())
				.bmcKey(null)
				.timeout(120L)
				.build(),
			ipmiExtension.buildConfiguration("ipmi", configuration, null)
		);
	}
}
