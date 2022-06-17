package com.sentrysoftware.matrix.it.job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.sentrysoftware.matrix.common.helpers.JsonHelper;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.EngineResult;
import com.sentrysoftware.matrix.engine.OperationStatus;
import com.sentrysoftware.matrix.engine.strategy.IStrategy;
import com.sentrysoftware.matrix.model.alert.AlertRule;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoringVo;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;
import com.sentrysoftware.matrix.model.parameter.DiscreteParam;
import com.sentrysoftware.matrix.model.parameter.IParameter;
import com.sentrysoftware.matrix.model.parameter.NumberParam;
import com.sentrysoftware.matrix.model.parameter.TextParam;

import lombok.Data;

@Data
public abstract class AbstractITJob implements ITJob {

	protected EngineConfiguration engineConfiguration;
	protected IHostMonitoring hostMonitoring;
	protected EngineResult lastEngineResult;

	/**
	 * Assert the expected {@link Monitor} instance and the corresponding actual instance from the given Map of monitors
	 *
	 * @param expected
	 * @param monitors
	 */
	private static void assertMonitor(final Monitor expected, final Map<String, Monitor> monitors) {
		final MonitorType monitorType = expected.getMonitorType();

		assertNotNull(monitors, () -> "<null> monitors found for type " + monitorType);
		assertFalse(monitors.isEmpty(), () -> "No monitor found for type " + monitorType);

		assertMonitor(expected, monitors.get(expected.getId()));
	}

	/**
	 * Assert that expected and actual are equal.
	 *
	 * @param expected
	 * @param actual
	 */
	private static void assertMonitor(final Monitor expected, final Monitor actual) {

		final MonitorType monitorType = expected.getMonitorType();

		assertNotNull(actual, () -> "Cannot find the Monitor with type " + monitorType + " and ID: " + expected.getId());

		assertEquals(expected.getExtendedType(), actual.getExtendedType(), () -> "ExtendedType doesn't match. MonitorType: " + monitorType + ". ID: " + expected.getId());
		assertEquals(expected.getName(), actual.getName(), () -> "Name doesn't match. MonitorType: " + monitorType + ". ID: " + expected.getId());
		assertEquals(expected.getParentId(), actual.getParentId(), () -> "ParentId doesn't match. MonitorType: " + monitorType + ". ID: " + expected.getId());
		assertEquals(expected.getHostId(), actual.getHostId(), () -> "HostId doesn't match. MonitorType: " + monitorType + ". ID: " + expected.getId());

		assertMetadata(expected, actual);

		assertParameters(expected, actual);

		assertAlertRules(expected, actual);
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
	 * Assert that expected and actual parameters are equal. <br>
	 * We only test testable/important data, for example the collectTime cannot be tested.
	 *
	 * @param expected
	 * @param actual
	 */
	private static void assertParameters(final Monitor expected, final Monitor actual) {
		for (final Entry<String, IParameter> expectedEntry : expected.getParameters().entrySet()) {
			final IParameter expectedParameter = expectedEntry.getValue();
			final MonitorType monitorType = expected.getMonitorType();
			final String monitorId = expected.getId();
			switch (expectedParameter.getType()) {
			case NumberParam.NUMBER_TYPE:
				assertNumberParam((NumberParam) expectedParameter, actual.getParameter(expectedEntry.getKey(), NumberParam.class), monitorType, monitorId);
				break;
			case TextParam.TEXT_TYPE:
				assertTextParam((TextParam) expectedParameter, actual.getParameter(expectedEntry.getKey(), TextParam.class), monitorType, monitorId);
				break;
			case DiscreteParam.DISCRETE_TYPE:
				assertDiscreteParam((DiscreteParam) expectedParameter, actual.getParameter(expectedEntry.getKey(), DiscreteParam.class), monitorType, monitorId);
				break;
			}
		}
	}

	/**
	 * Assert that expected and actual are equal.
	 *
	 * @param expected
	 * @param actual
	 * @param monitorType
	 * @param monitorId
	 */
	private static void assertNumberParam(final NumberParam expected, final NumberParam actual, final MonitorType monitorType, final String monitorId) {
		assertNotNull(actual,
				() -> "NumberParam not collected. Parameter name: " + expected.getName() + " MonitorType: " + monitorType + ". ID: " + monitorId);
		assertEquals(expected.getValue(), actual.getValue(), "NumberParam value doesn't match. Parameter name: " + expected.getName() + ". MonitorType: " + monitorType + ". ID: " + monitorId);
		assertEquals(expected.getRawValue(), actual.getRawValue(), "NumberParam raw value doesn't match. Parameter name: " + expected.getName() + ". MonitorType: " + monitorType + ". ID: " + monitorId);
	}

	/**
	 * Assert that expected and actual are equal.
	 *
	 * @param expected
	 * @param actual
	 * @param monitorType
	 * @param monitorId
	 */
	private static void assertTextParam(final TextParam expected, final TextParam actual, final MonitorType monitorType, final String monitorId) {
		assertNotNull(actual,
				() -> "TextParam not collected. Parameter name: " + expected.getName() + " MonitorType: " + monitorType + ". ID: " + monitorId);
		assertEquals(expected.getValue(), actual.getValue(), "TextParam value doesn't match. Parameter name: " + expected.getName() + ". MonitorType: " + monitorType + ". ID: " + monitorId);
	}

	/**
	 * Assert that expected and actual are equal.
	 *
	 * @param expected
	 * @param actual
	 * @param monitorType
	 * @param monitorId
	 */
	private static void assertDiscreteParam(final DiscreteParam expected, final DiscreteParam actual, final MonitorType monitorType, final String monitorId) {
		assertNotNull(actual,
				() -> "DiscreteParam not collected. Parameter name: " + expected.getName() + " MonitorType: " + monitorType + ". ID: " + monitorId);
		assertEquals(expected.getState(), actual.getState(), "DiscreteParam doesn't match. Parameter name: " + expected.getName() + ". MonitorType: " + monitorType + ". ID: " + monitorId);
	}

	/**
	 * Assert that expected and actual metadata are equal.
	 *
	 * @param expected
	 * @param actual
	 */
	private static void assertMetadata(final Monitor expected, final Monitor actual) {
		for (final Entry<String, String> expectedMetadata : expected.getMetadata().entrySet()) {
			assertEquals(expectedMetadata.getValue(), actual.getMetadata().get(expectedMetadata.getKey()),
					() -> "metadata doesn't match. metadata key: " + expectedMetadata.getKey() + ". MonitorType: " + expected.getMonitorType()
					+ ". ID: " + expected.getId());
		}
	}


	@Override
	public ITJob verifyExpected(final String expectedPath) throws Exception {

		stopServer();

		assertEquals(OperationStatus.SUCCESS, lastEngineResult.getOperationStatus(), "Last strategy failed.");

		final InputStream is = ITJobUtils.getItResourceAsInputStream(expectedPath);
		final HostMonitoringVo hostMonitoringVo = JsonHelper.deserialize(is, HostMonitoringVo.class);

		final HostMonitoringVo actual = hostMonitoring.getVo();

		assertEquals(hostMonitoringVo.getTotal(), actual.getTotal());

		hostMonitoringVo.getMonitors().forEach(monitor -> assertMonitor(
				monitor, hostMonitoring.selectFromType(monitor.getMonitorType())));

		return this;
	}

	@Override
	public ITJob executeStrategy(final IStrategy strategy) {

		assertTrue(isServerStarted(), "Server not ready.");

		hostMonitoring.setEngineConfiguration(engineConfiguration);

		lastEngineResult = hostMonitoring.run(strategy);

		return this;
	}

	@Override
	public ITJob prepareEngine(final EngineConfiguration engineConfiguration, final IHostMonitoring hostMonitoring) {
		this.engineConfiguration = engineConfiguration;
		this.hostMonitoring = hostMonitoring;
		return this;
	}

	/**
	 * Save the hostMonitoring JSON into a file.
	 *
	 * @param path path of the saving file.
	 * @return
	 * @throws IOException
	 */
	public ITJob saveHostMonitoringJson(final Path path) throws IOException {

		Files.write(path, hostMonitoring.toJson().getBytes());

		return this;
	}

}
