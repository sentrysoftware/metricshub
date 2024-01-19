package com.sentrysoftware.metricshub.hardware.it.job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sentrysoftware.metricshub.engine.alert.AlertRule;
import com.sentrysoftware.metricshub.engine.client.ClientsExecutor;
import com.sentrysoftware.metricshub.engine.common.helpers.JsonHelper;
import com.sentrysoftware.metricshub.engine.strategy.collect.CollectStrategy;
import com.sentrysoftware.metricshub.engine.strategy.collect.PrepareCollectStrategy;
import com.sentrysoftware.metricshub.engine.strategy.detection.DetectionStrategy;
import com.sentrysoftware.metricshub.engine.strategy.discovery.DiscoveryStrategy;
import com.sentrysoftware.metricshub.engine.strategy.simple.SimpleStrategy;
import com.sentrysoftware.metricshub.engine.telemetry.Monitor;
import com.sentrysoftware.metricshub.engine.telemetry.MonitorsVo;
import com.sentrysoftware.metricshub.engine.telemetry.Resource;
import com.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import com.sentrysoftware.metricshub.engine.telemetry.metric.AbstractMetric;
import com.sentrysoftware.metricshub.hardware.strategy.HardwarePostCollectStrategy;
import com.sentrysoftware.metricshub.hardware.strategy.HardwarePostDiscoveryStrategy;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map.Entry;
import lombok.Data;
import lombok.NonNull;

@Data
public abstract class AbstractITJob implements ITJob {

	private static final String AGENT_HOSTNAME_ATTRIBUTE = "agent.host.name";

	@NonNull
	protected final ClientsExecutor clientsExecutor;

	@NonNull
	protected final TelemetryManager telemetryManager;

	/**
	 * Assert that expected and actual are equal.
	 *
	 * @param expected
	 * @param actual
	 */
	private static void assertMonitor(final Monitor expected, final Monitor actual) {
		assertMetrics(expected, actual);
		assertMonitorAttributes(expected, actual);
		assertConditionalCollection(expected, actual);
		assertLegacyTextParameters(expected, actual);
		assertAlertRules(expected, actual);
		assertResource(expected, actual);
		assertNotNull(actual.getDiscoveryTime());

		final String expectedMonitorId = expected.getId();
		assertEquals(
			expected.getType(),
			actual.getType(),
			() -> String.format("Type doesn't match actual on monitor identifier: %s.", expectedMonitorId)
		);
		assertEquals(
			expected.getId(),
			actual.getId(),
			() -> String.format("ID doesn't match actual on monitor identifier: %s.", expectedMonitorId)
		);
		assertEquals(
			expected.isEndpoint(),
			actual.isEndpoint(),
			() -> String.format("isEndpoint doesn't match actual on monitor identifier: %s.", expectedMonitorId)
		);
		assertEquals(
			expected.isEndpointHost(),
			actual.isEndpointHost(),
			() -> String.format("isEndpointHost doesn't match actual on monitor identifier: %s.", expectedMonitorId)
		);
	}

	/**
	 * Assert that expected and actual alert rules are equal. <br>
	 * We only test testable/important data. For example the {@link AlertRule} conditionsChecker cannot be checked as it is a function
	 *
	 * @param expectedMonitor Expected monitor defined in the expected JSON file
	 * @param actualMonitor   Actual collected monitor from the {@link TelemetryManager}
	 */
	private static void assertAlertRules(final Monitor expectedMonitor, final Monitor actualMonitor) {
		// Alert rules are not available yet
		assertTrue(
			actualMonitor.getAlertRules().isEmpty(),
			() -> String.format("Alert rules are not empty on monitor identifier: %s.", actualMonitor.getId())
		);
	}

	/**
	 * Assert that expected and actual metrics are equal. <br>
	 *
	 * @param expectedMonitor Expected monitor defined in the expected JSON file
	 * @param actualMonitor   Actual collected monitor from the {@link TelemetryManager}
	 */
	private static void assertMetrics(final Monitor expectedMonitor, final Monitor actualMonitor) {
		for (final Entry<String, AbstractMetric> expectedEntry : expectedMonitor.getMetrics().entrySet()) {
			final AbstractMetric expectedMetric = expectedEntry.getValue();
			final String expectedKey = expectedEntry.getKey();
			final String expectedMonitorId = expectedMonitor.getId();

			assertNotNull(
				expectedMetric,
				() -> String.format("Expected metric cannot be null for monitor identifier: %s.", expectedMonitorId)
			);

			final String expectedMetricName = expectedMetric.getName();

			final AbstractMetric actualMetric = actualMonitor.getMetric(expectedMetricName, expectedMetric.getClass());

			assertNotNull(
				actualMetric,
				() ->
					String.format(
						"Cannot find actual metric on monitor identifier: %s. Metric name: %s.",
						expectedMonitorId,
						expectedKey
					)
			);

			assertEquals(
				expectedMetricName,
				actualMetric.getName(),
				() ->
					String.format(
						"Name doesn’t match actual on monitor identifier: %s. Metric name: %s.",
						expectedMonitorId,
						expectedMetricName
					)
			);

			assertNotNull(
				actualMetric.getCollectTime(),
				() ->
					String.format(
						"CollectTime doesn’t match actual on monitor identifier: %s. Metric name: %s.",
						expectedMonitorId,
						expectedMetricName
					)
			);

			assertMetricAttributes(expectedMetric, actualMetric, expectedMonitorId);

			assertEquals(
				expectedMetric.isResetMetricTime(),
				actualMetric.isResetMetricTime(),
				() ->
					String.format(
						"IsResetMetricTime doesn't match actual on monitor identifier: %s.  Metric name: %s.",
						expectedMonitorId,
						expectedMetricName
					)
			);

			final Object expectedValue = expectedMetric.getValue();
			final Object actualValue = actualMetric.getValue();
			assertEquals(
				expectedValue,
				actualValue,
				() ->
					String.format(
						"Value doesn't match actual on monitor identifier: %s. Metric name: %s.",
						expectedMonitorId,
						expectedMetricName
					)
			);
		}
	}

	/**
	 * Assert that expected and actual monitor attributes are equal
	 *
	 * @param expectedMonitor Expected monitor defined in the expected JSON file
	 * @param actualMonitor   Actual collected monitor from the {@link TelemetryManager}
	 */
	private static void assertMonitorAttributes(final Monitor expectedMonitor, final Monitor actualMonitor) {
		for (final Entry<String, String> expectedEntry : expectedMonitor.getAttributes().entrySet()) {
			final String expected = expectedEntry.getValue();
			final String expectedKey = expectedEntry.getKey();

			final String actual = actualMonitor.getAttribute(expectedKey);

			assertEquals(
				expected,
				actual,
				() ->
					String.format(
						"Actual monitor's attribute did not match expected: %s on monitor identifier: %s.",
						expectedKey,
						expectedMonitor.getId()
					)
			);
		}
	}

	/**
	 * Assert that expected and actual monitor resource attributes are equal
	 *
	 * @param expectedMonitor Expected monitor defined in the expected JSON file
	 * @param actualMonitor   Actual collected monitor from the {@link TelemetryManager}
	 */
	private static void assertResource(final Monitor expectedMonitor, final Monitor actualMonitor) {
		final Resource expectedResource = expectedMonitor.getResource();
		final Resource actualResource = expectedMonitor.getResource();

		final String expectedMonitorId = expectedMonitor.getId();
		if (expectedResource != null) {
			assertEquals(
				expectedResource.getType(),
				actualResource.getType(),
				() ->
					String.format(
						"Actual resouruce type did not match expected: %s on monitor identifier: %s.",
						expectedResource.getType(),
						expectedMonitorId
					)
			);
			for (Entry<String, String> expectedAttribute : expectedResource.getAttributes().entrySet()) {
				// host name can change from different IT runs. if it is not null, check that actual has value.
				final String expectedKey = expectedAttribute.getKey();
				final String expectedValue = expectedAttribute.getValue();
				if (expectedKey.equals(AGENT_HOSTNAME_ATTRIBUTE)) {
					assertNotNull(
						actualResource.getAttributes().get(expectedKey),
						() ->
							String.format(
								"%s cannot be null on the resource for the monitor identifier: %s.",
								AGENT_HOSTNAME_ATTRIBUTE,
								expectedMonitorId
							)
					);
				} else if (expectedValue != null) {
					assertEquals(
						expectedValue,
						actualResource.getAttributes().get(expectedKey),
						() ->
							String.format(
								"Actual attribute did not match expected: %s on monitor identifier: %s.",
								expectedKey,
								expectedMonitorId
							)
					);
				}
			}
		}
	}

	/**
	 * Assert that expected and actual metric attributes are equal
	 *
	 * @param expectedMetric    Expected metric defined in the expected JSON file
	 * @param actualMetric      Actual collected metric from the {@link TelemetryManager}
	 * @param expectedMonitorId Used to add more context in case the test has failed
	 */
	private static void assertMetricAttributes(
		final AbstractMetric expectedMetric,
		final AbstractMetric actualMetric,
		final String expectedMonitorId
	) {
		for (final Entry<String, String> expectedEntry : expectedMetric.getAttributes().entrySet()) {
			final String expected = expectedEntry.getValue();

			final String expectedKey = expectedEntry.getKey();

			final String actual = actualMetric.getAttributes().get(expectedKey);

			assertEquals(
				expected,
				actual,
				() ->
					String.format(
						"actual attribute did not match expected: %s on monitor identifier: %s.",
						expectedKey,
						expectedMonitorId
					)
			);
		}
	}

	/**
	 * Assert that expected and actual conditional collection are equal
	 *
	 * @param expectedMonitor Expected monitor defined in the expected JSON file
	 * @param actualMonitor   Actual collected monitor from the {@link TelemetryManager}
	 */
	private static void assertConditionalCollection(final Monitor expectedMonitor, final Monitor actualMonitor) {
		for (final Entry<String, String> expectedEntry : expectedMonitor.getConditionalCollection().entrySet()) {
			final String expected = expectedEntry.getValue();
			final String expectedKey = expectedEntry.getKey();

			final String actual = expectedMonitor.getConditionalCollection().get(expectedKey);

			assertEquals(
				expected,
				actual,
				() ->
					String.format(
						"Actual conditional collection did not match expected: %s on monitor identifier: %s." + expectedKey,
						expectedMonitor.getId()
					)
			);
		}
	}

	/**
	 * Assert that expected and actual legacy text parameters are equal
	 *
	 * @param expectedMonitor Expected monitor defined in the expected JSON file
	 * @param actualMonitor   Actual collected monitor from the {@link TelemetryManager}
	 */
	private static void assertLegacyTextParameters(final Monitor expectedMonitor, final Monitor actualMonitor) {
		for (final Entry<String, String> expectedEntry : expectedMonitor.getLegacyTextParameters().entrySet()) {
			final String expected = expectedEntry.getValue();
			final String expectedKey = expectedEntry.getKey();

			final String actual = expectedMonitor.getLegacyTextParameters().get(expectedKey);

			assertEquals(
				expected,
				actual,
				() ->
					String.format(
						"Actual LegacyTextParameter did not match expected: %s on monitor identifier: %s.",
						expectedKey,
						expectedMonitor.getId()
					)
			);
		}
	}

	@Override
	public ITJob verifyExpected(final String expectedPath) throws Exception {
		stopServer();

		final InputStream is = ITJobUtils.getItResourceAsInputStream(expectedPath);
		final MonitorsVo expectedMonitors = JsonHelper.deserialize(is, MonitorsVo.class);

		final MonitorsVo actual = telemetryManager.getVo();

		assertEquals(expectedMonitors.getTotal(), actual.getTotal());

		expectedMonitors
			.getMonitors()
			.forEach(expectedMonitor -> {
				final String expectedType = expectedMonitor.getType();
				assertNotNull(
					expectedType,
					() ->
						String.format("Expected monitor 'type' cannot be null for monitor identifier: %s.", expectedMonitor.getId())
				);
				final Monitor actualMonitor = telemetryManager.findMonitorByTypeAndId(expectedType, expectedMonitor.getId());
				assertMonitor(expectedMonitor, actualMonitor);
			});

		return this;
	}

	@Override
	public ITJob executeDiscoveryStrategy() {
		final Long discoveryTime = System.currentTimeMillis();

		telemetryManager.run(
			new DetectionStrategy(telemetryManager, discoveryTime, clientsExecutor),
			new DiscoveryStrategy(telemetryManager, discoveryTime, clientsExecutor),
			new SimpleStrategy(telemetryManager, discoveryTime, clientsExecutor),
			new HardwarePostDiscoveryStrategy(telemetryManager, discoveryTime, clientsExecutor)
		);

		assertTrue(isServerStarted(), () -> "Server not started.");

		return this;
	}

	@Override
	public ITJob executeCollectStrategy() {
		final Long collectTime = System.currentTimeMillis();

		telemetryManager.run(
			new PrepareCollectStrategy(telemetryManager, collectTime, clientsExecutor),
			new CollectStrategy(telemetryManager, collectTime, clientsExecutor),
			new SimpleStrategy(telemetryManager, collectTime, clientsExecutor),
			new HardwarePostCollectStrategy(telemetryManager, collectTime, clientsExecutor)
		);

		assertTrue(isServerStarted(), () -> "Server not started.");

		return this;
	}

	@Override
	public ITJob saveTelemetryManagerJson(Path path) throws IOException {
		Files.write(path, telemetryManager.toJson().getBytes());

		return this;
	}
}
