package com.sentrysoftware.metricshub.engine.it.job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import com.sentrysoftware.metricshub.engine.alert.AlertRule;
import com.sentrysoftware.metricshub.engine.common.helpers.JsonHelper;
import com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants;
import com.sentrysoftware.metricshub.engine.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.metricshub.engine.strategy.collect.CollectStrategy;
import com.sentrysoftware.metricshub.engine.strategy.collect.PostCollectStrategy;
import com.sentrysoftware.metricshub.engine.strategy.collect.PrepareCollectStrategy;
import com.sentrysoftware.metricshub.engine.strategy.detection.DetectionStrategy;
import com.sentrysoftware.metricshub.engine.strategy.discovery.DiscoveryStrategy;
import com.sentrysoftware.metricshub.engine.strategy.discovery.PostDiscoveryStrategy;
import com.sentrysoftware.metricshub.engine.strategy.simple.SimpleStrategy;
import com.sentrysoftware.metricshub.engine.telemetry.Monitor;
import com.sentrysoftware.metricshub.engine.telemetry.MonitorsVo;
import com.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import com.sentrysoftware.metricshub.engine.telemetry.metric.AbstractMetric;
import com.sentrysoftware.metricshub.engine.telemetry.Resource;

import lombok.Data;
import lombok.NonNull;

@Data
public abstract class AbstractITJob implements ITJob {

	@NonNull
	private final MatsyaClientsExecutor matsyaClientsExecutor;

	@NonNull
	private final TelemetryManager telemetryManager;

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
		assertEquals(expected.getResource(), actual.getResource());
		assertNotNull(actual.getDiscoveryTime());
		assertEquals(expected.getType(), actual.getType(), "Type doesn't match actual Type on monitor: " + expected.getId());
		assertEquals(expected.getId(), actual.getId(), () -> "ID doesn't match actual ID on monitor: " + expected.getId());
		assertEquals(expected.isEndpoint(), actual.isEndpoint(), () -> "isEndpoint doesn't match actual isEndpoint on monitor: " + expected.getId());
		assertEquals(expected.isEndpointHost(), actual.isEndpointHost(), () -> "isEndpointHost doesn't match actual EndpointHost on monitor: " + expected.getId());
	}

	/**
	 * Assert that expected and actual alert rules are equal. <br>
	 * We only test testable/important data. For example the {@link AlertRule} conditionsChecker cannot be checked as it is a function
	 *
	 * @param expected
	 * @param actual
	 */
	private static void assertAlertRules(final Monitor expected, final Monitor actual) {
		for (final Entry<String, List<AlertRule>> expectedEntry : expected.getAlertRules().entrySet()) {
			final List<AlertRule> expectedAlertRules = expectedEntry.getValue();
			final String parameterName = expectedEntry.getKey();

			assertNotNull(expectedAlertRules, () -> "Expected alert rules cannot be null for monitor identifier: " + expected.getId());
			for (AlertRule expectedAlertRule : expectedAlertRules) {

				final List<AlertRule> actualAlertRules = actual.getAlertRules().get(parameterName);

				assertNotNull(actualAlertRules, 
						() -> "Cannot find actual alert rules on monitor: " + expected.getId() + ". For parameter: "
								+ parameterName);

				final AlertRule actualAlertRule = actualAlertRules.stream()
						.filter(rule -> rule.same(expectedAlertRule)).findFirst().orElse(null);

				assertNotNull(actualAlertRule,
						() -> "Cannot find actual alert rule on monitor: " + expected.getId() + ". For parameter: "
								+ parameterName);

				assertEquals(expectedAlertRule.getActive(), actualAlertRule.getActive(),
						() -> "Alert Rule Active doesn’t match actual value on monitor: " + expected.getId() + ". For parameter: "
								+ parameterName);

				assertEquals(expectedAlertRule.getConditions(), actualAlertRule.getConditions(),
						() -> "Alert Rule Conditions doesn’t match actual conditions on monitor: " + expected.getId() + ". For parameter: "
								+ parameterName);

				assertEquals(expectedAlertRule.getSeverity(), actualAlertRule.getSeverity(),
						() -> "Alert Rule Severity doesn’t match actual severity on monitor: " + expected.getId() + ". For parameter: "
								+ parameterName);

				assertEquals(expectedAlertRule.getDetails(), actualAlertRule.getDetails(),
						() -> "Alert Rule Details don't match actual details on monitor: " + expected.getId() + ". For parameter: "
								+ parameterName);
				assertEquals(expectedAlertRule.getType(), actualAlertRule.getType(),
						() -> "Alert Rule Type doesn't match actual type on monitor: " + expected.getId() + ". For parameter: "
								+ parameterName);

				assertEquals(expectedAlertRule.getPeriod(), actualAlertRule.getPeriod(),
						() -> "Alert Rule Period doesn't match actual type on monitor: " + expected.getId() + ". For parameter: "
								+ parameterName);
			}
		}
	}

	/**
	 * Assert that expected and actual metrics are equal. <br>
	 * We only test testable/important data. For example the {@link Metric} conditionsChecker cannot be checked as it is a function
	 *
	 * @param expected
	 * @param actual
	 */
	private static void assertMetrics(final Monitor expected, final Monitor actual) {

		for (final Entry<String, AbstractMetric> expectedEntry : expected.getMetrics().entrySet()) {

			final AbstractMetric expectedMetric = expectedEntry.getValue();
			final String expectedKey = expectedEntry.getKey();

			assertNotNull(expectedMetric, () -> "Expected metric cannot be null for monitor identifier: " + expected.getId());

			final AbstractMetric actualMetric = actual.getMetric(expectedMetric.getName(), expectedMetric.getClass());

			assertNotNull(actualMetric, 
					() -> "Cannot find actual metric on monitor: " + expected.getId() + ". For parameter: "
							+ expectedKey);

			assertEquals(expectedMetric.getName(), actualMetric.getName(),
					"Name doesn’t match actual value on monitor: " + expected.getId() + ". For parameter: "
							+ expectedMetric.getName());

			assertNotNull(actualMetric.getCollectTime(),
					"CollectTime doesn’t match actual collect time on monitor: " + expected.getId() + ". For parameter: "
							+ expectedMetric.getName());

			assertNotNull(actualMetric.getPreviousCollectTime(),
					"PreviousCollectTime doesn’t match actual previous collect time on monitor: " + expected.getId() + ". For parameter: "
							+ expectedMetric.getName());

			assertMetricAttributes(expectedMetric, actualMetric);

			assertEquals(expectedMetric.isResetMetricTime(), actualMetric.isResetMetricTime(),
					() -> "IsResetMetricTime doesn't match actual isResetMetricTime on monitor: " + expected.getId() + ". For parameter: "
							+ expectedMetric.getName());
		}
	}

	/**
	 * Assert that expected and actual monitor attributes are equal
	 * @param expectedMonitor
	 * @param actualMonitor
	 */
	private static void assertMonitorAttributes(final Monitor expectedMonitor, final Monitor actualMonitor) {
		for (final Entry<String, String> expectedEntry : expectedMonitor.getAttributes().entrySet()) {
		
			final String expected = expectedEntry.getValue();
			final String expectedKey = expectedEntry.getKey();

			assertNotNull(expected, () -> "Expected attribute cannot be null for monitor identifier: " + expectedMonitor.getId());

			final String actual = actualMonitor.getAttribute(expectedKey);

			assertNotNull(actual, 
					() -> "Cannot find actual attribute on monitor: " + expectedMonitor.getId() + ". For parameter: "
							+ expectedKey);

			if(expectedKey.equals(MetricsHubConstants.MONITOR_ATTRIBUTE_APPLIES_TO_OS)) {
				assertEquals(getSetFromAppliesToOsString(expected), getSetFromAppliesToOsString(actual));
			} else {
				assertEquals(expected, actual);
			}
		}
	}

	/**
	 * Assert that expected and actual monitor resource attributes are equal
	 * @param expectedMonitor
	 * @param actualMonitor
	 */
	private static void assertResource(final Monitor expectedMonitor, final Monitor actualMonitor) {
		final Resource expectedResource = expectedMonitor.getResource();
		final Resource actualResource = expectedMonitor.getResource();

		if(expectedResource != null) {
			assertEquals(expectedResource.getType(), actualResource.getType());
			assertEquals(expectedResource.getAttributes(), actualResource.getAttributes());
		}
	}

	/**
	 * given the expected input from example applies_to_os: [ WINDOW, LINUX ]
	 * return a set with expected values WINDOWs, LINUX (for example)
	 * 
	 * @param setString input to parse
	 * @return parsedSet
	 */
	private static Set<String> getSetFromAppliesToOsString(final String setString) {
		final Set<String> parsedSet = new HashSet<>();
		parsedSet.addAll(Arrays.asList(setString.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(" ", "").split(",")));
		return parsedSet;
	}

	/**
	 * Assert that expected and actual metric attributes are equal
	 * @param expectedMetric
	 * @param actualMetric
	 */
	private static void assertMetricAttributes(final AbstractMetric expectedMetric, final AbstractMetric actualMetric) {
		for (final Entry<String, String> expectedEntry : expectedMetric.getAttributes().entrySet()) {
		
			final String expected = expectedEntry.getValue();
			final String expectedKey = expectedEntry.getKey();

			assertNotNull(expected, () -> "Expected attribute cannot be null for metric identifier: " + expectedMetric.getName());

			final String actual = actualMetric.getAttributes().get(expectedKey);

			assertNotNull(actual, 
					() -> "Cannot find actual attribute for metric: " + expectedMetric.getName() + ". For parameter: "
							+ expectedKey);

			assertEquals(expected, actual, "Expected attribute did not match actual: " + expectedKey);
		}
	}

	/**
	 * Assert that expected and actual conditional collection are equal
	 * @param expectedMonitor
	 * @param actualMonitor
	 */
	private static void assertConditionalCollection(final Monitor expectedMonitor, final Monitor actualMonitor) {
		for (final Entry<String, String> expectedEntry : expectedMonitor.getConditionalCollection().entrySet()) {
		
			final String expected = expectedEntry.getValue();
			final String expectedKey = expectedEntry.getKey();

			assertNotNull(expected, () -> "Expected conditional collection cannot be null for monitor identifier: " + expectedMonitor.getId());

			final String actual = actualMonitor.getAttribute(expectedKey);

			assertNotNull(actual, 
					() -> "Cannot find actual conditional collection on monitor: " + expectedMonitor.getId() + ". For parameter: "
							+ expectedKey);

			assertEquals(expected, actual);
		}
	}

	/**
	 * Assert that expected and actual legacy text parameters are equal
	 * @param expectedMonitor
	 * @param actualMonitor
	 */
	private static void assertLegacyTextParameters(final Monitor expectedMonitor, final Monitor actualMonitor) {
		for (final Entry<String, String> expectedEntry : expectedMonitor.getLegacyTextParameters().entrySet()) {
		
			final String expected = expectedEntry.getValue();
			final String expectedKey = expectedEntry.getKey();

			assertNotNull(expected, () -> "Expected Legacy text parameter cannot be null for monitor identifier: " + expectedMonitor.getId());

			final String actual = actualMonitor.getAttribute(expectedKey);

			assertNotNull(actual, 
					() -> "Cannot find actual legacy text parameter on monitor: " + expectedMonitor.getId() + ". For parameter: "
							+ expectedKey);

			assertEquals(expected, actual);
		}
	}


	@Override
	public ITJob verifyExpected(final String expectedPath) throws Exception {

		stopServer();

		final InputStream is = ITJobUtils.getItResourceAsInputStream(expectedPath);
		final MonitorsVo expected = JsonHelper.deserialize(is, MonitorsVo.class);

		final MonitorsVo actual = telemetryManager.getVo();

		assertEquals(expected.getTotal(), actual.getTotal());

		for (int i = 0; i < expected.getMonitors().size(); i++) {
			assertMonitor(expected.getMonitors().get(i), actual.getMonitors().get(i));
		}

		return this;
	}

	@Override
	public ITJob executeDiscoveryStrategy() {

		final Long discoveryTime = System.currentTimeMillis();

		telemetryManager.run(
			new DetectionStrategy(telemetryManager, discoveryTime, matsyaClientsExecutor),
			new DiscoveryStrategy(telemetryManager, discoveryTime, matsyaClientsExecutor),
			new SimpleStrategy(telemetryManager, discoveryTime, matsyaClientsExecutor),
			new PostDiscoveryStrategy(telemetryManager, discoveryTime, matsyaClientsExecutor)
		);

		assertTrue(isServerStarted(), "Server not ready.");

		return this;
	}


	@Override
	public ITJob executeCollectStrategy() {
		final Long collectTime = System.currentTimeMillis();

		telemetryManager.run(
			new PrepareCollectStrategy(telemetryManager, collectTime, matsyaClientsExecutor),
			new CollectStrategy(telemetryManager, collectTime, matsyaClientsExecutor),
			new SimpleStrategy(telemetryManager, collectTime, matsyaClientsExecutor),
			new PostCollectStrategy(telemetryManager, collectTime, matsyaClientsExecutor)
		);

		assertTrue(isServerStarted(), "Server not ready.");

		return this;
	}

	@Override
	public ITJob saveTelemetryManagerJson(Path path) throws IOException {
		Files.write(path, telemetryManager.toJson().getBytes());
	
		return this;
	}
}
