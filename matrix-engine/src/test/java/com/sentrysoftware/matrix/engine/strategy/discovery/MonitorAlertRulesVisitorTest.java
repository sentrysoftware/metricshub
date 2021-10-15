package com.sentrysoftware.matrix.engine.strategy.discovery;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.AVAILABLE_PATH_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.AVAILABLE_PATH_WARNING;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_PERCENT_ALARM_THRESHOLD;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_PERCENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_PERCENT_WARNING_THRESHOLD;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PRESENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SPEED_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SPEED_PERCENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TEMPERATURE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.VALUE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.VOLTAGE_PARAMETER;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.common.meta.monitor.Fan;
import com.sentrysoftware.matrix.common.meta.monitor.OtherDevice;
import com.sentrysoftware.matrix.common.meta.monitor.Temperature;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.model.alert.AlertCondition;
import com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder;
import com.sentrysoftware.matrix.model.alert.AlertRule;
import com.sentrysoftware.matrix.model.alert.AlertRule.AlertRuleType;
import com.sentrysoftware.matrix.model.alert.Severity;
import com.sentrysoftware.matrix.model.monitor.Monitor;

class MonitorAlertRulesVisitorTest {

	@Test
	void testProcessStaticAlertRules() {
		final Monitor monitor = new Monitor();

		MonitorAlertRulesVisitor.processStaticAlertRules(monitor, new Fan());

		assertNotNull(monitor.getAlertRules().get(PRESENT_PARAMETER));
		assertNotNull(monitor.getAlertRules().get(STATUS_PARAMETER));
		assertNotNull(monitor.getAlertRules().get(SPEED_PARAMETER));
		assertNotNull(monitor.getAlertRules().get(SPEED_PERCENT_PARAMETER));

	}

	@Test
	void testUpdateFanInstanceSpeedAlertRulesCase1() {
		// WARN > ALARM
		final Monitor monitor = new Monitor();
		final Set<String> result = MonitorAlertRulesVisitor.updateFanInstanceSpeedAlertRules(monitor, SPEED_PARAMETER, 500D, 0D);
		assertEquals(Set.of(SPEED_PARAMETER), result);
		final Map<String, List<AlertRule>> alertRulesMap = monitor.getAlertRules();

		final Set<AlertCondition> warningConditions = AlertConditionsBuilder.newInstance()
				.lte(500D)
				.build();
		final Set<AlertCondition> alarmConditions = AlertConditionsBuilder.newInstance()
				.gte(0D)
				.lte(0D)
				.build();

		assertAlertRule(alertRulesMap, SPEED_PARAMETER, Severity.WARN, warningConditions, AlertRuleType.INSTANCE);
		assertAlertRule(alertRulesMap, SPEED_PARAMETER, Severity.ALARM, alarmConditions, AlertRuleType.INSTANCE);

	}

	@Test
	void testUpdateFanInstanceSpeedAlertRulesCase2() {
		// WARN < ALARM
		final Monitor monitor = new Monitor();
		final Set<String> result = MonitorAlertRulesVisitor.updateFanInstanceSpeedAlertRules(monitor, SPEED_PARAMETER, 5D, 500D);
		assertEquals(Set.of(SPEED_PARAMETER), result);
		final Map<String, List<AlertRule>> alertRulesMap = monitor.getAlertRules();

		final Set<AlertCondition> warningConditions = AlertConditionsBuilder.newInstance()
				.lte(500D)
				.build();
		final Set<AlertCondition> alarmConditions = AlertConditionsBuilder.newInstance()
				.gte(0D)
				.lte(5D)
				.build();

		assertAlertRule(alertRulesMap, SPEED_PARAMETER, Severity.WARN, warningConditions, AlertRuleType.INSTANCE);
		assertAlertRule(alertRulesMap, SPEED_PARAMETER, Severity.ALARM, alarmConditions, AlertRuleType.INSTANCE);

	}

	@Test
	void testUpdateFanInstanceSpeedAlertRulesCase3() {
		// WARN null, ALARM OK
		final Monitor monitor = new Monitor();
		final Set<String> result = MonitorAlertRulesVisitor.updateFanInstanceSpeedAlertRules(monitor, SPEED_PARAMETER, null, 5D);
		assertEquals(Set.of(SPEED_PARAMETER), result);
		final Map<String, List<AlertRule>> alertRulesMap = monitor.getAlertRules();

		final Set<AlertCondition> warningConditions = AlertConditionsBuilder.newInstance()
				.lte(5.5)
				.build();
		final Set<AlertCondition> alarmConditions = AlertConditionsBuilder.newInstance()
				.gte(0D)
				.lte(5D)
				.build();

		assertAlertRule(alertRulesMap, SPEED_PARAMETER, Severity.WARN, warningConditions, AlertRuleType.INSTANCE);
		assertAlertRule(alertRulesMap, SPEED_PARAMETER, Severity.ALARM, alarmConditions, AlertRuleType.INSTANCE);

	}

	@Test
	void testUpdateFanInstanceSpeedAlertRulesUseCase4() {
		// WARN OK, ALARM null
		final Monitor monitor = new Monitor();
		final Set<String> result = MonitorAlertRulesVisitor.updateFanInstanceSpeedAlertRules(monitor, SPEED_PARAMETER, 5D, null);
		assertEquals(Set.of(SPEED_PARAMETER), result);
		final Map<String, List<AlertRule>> alertRulesMap = monitor.getAlertRules();

		final Set<AlertCondition> warningConditions = AlertConditionsBuilder.newInstance()
				.lte(5D)
				.build();
		final Set<AlertCondition> alarmConditions = AlertConditionsBuilder.newInstance()
				.gte(0D)
				.lte(4.5)
				.build();


		assertAlertRule(alertRulesMap, SPEED_PARAMETER, Severity.WARN, warningConditions, AlertRuleType.INSTANCE);
		assertAlertRule(alertRulesMap, SPEED_PARAMETER, Severity.ALARM, alarmConditions, AlertRuleType.INSTANCE);

	}

	@Test
	void testUpdateFanInstanceSpeedAlertRulesUseCase5() {
		// WARN null, ALARM null
		final Monitor monitor = new Monitor();
		final Set<String> result = MonitorAlertRulesVisitor.updateFanInstanceSpeedAlertRules(monitor, SPEED_PARAMETER, null, null);
		final Map<String, List<AlertRule>> alertRulesMap = monitor.getAlertRules();

		assertTrue(alertRulesMap.isEmpty());
		assertTrue(result.isEmpty());
	}

	@Test
	void testUpdateWarningToAlarmAlertRulesUseCase1() {
		// WARN OK < ALARM OK
		final Monitor monitor = new Monitor();

		final Set<String> result = MonitorAlertRulesVisitor.updateWarningToAlarmAlertRules(
				monitor,
				TEMPERATURE_PARAMETER,
				50D,
				60D,
				Temperature::checkTemperatureAbnormallyHighCondition,
				Temperature::checkTemperatureCriticallyHighCondition);

		assertEquals(Set.of(TEMPERATURE_PARAMETER), result);

		final Map<String, List<AlertRule>> alertRulesMap = monitor.getAlertRules();

		final Set<AlertCondition> warningConditions = AlertConditionsBuilder.newInstance()
				.gte(50D)
				.build();
		final Set<AlertCondition> alarmConditions = AlertConditionsBuilder.newInstance()
				.gte(60D)
				.build();

		assertAlertRule(alertRulesMap, TEMPERATURE_PARAMETER, Severity.WARN, warningConditions, AlertRuleType.INSTANCE);
		assertAlertRule(alertRulesMap, TEMPERATURE_PARAMETER, Severity.ALARM, alarmConditions, AlertRuleType.INSTANCE);
	}

	@Test
	void testUpdateWarningToAlarmAlertRulesUseCase2() {
		// WARN OK > ALARM OK
		final Monitor monitor = new Monitor();

		final Set<String> result = MonitorAlertRulesVisitor.updateWarningToAlarmAlertRules(
				monitor,
				TEMPERATURE_PARAMETER,
				60D,
				50D,
				Temperature::checkTemperatureAbnormallyHighCondition,
				Temperature::checkTemperatureCriticallyHighCondition);

		assertEquals(Set.of(TEMPERATURE_PARAMETER), result);

		final Map<String, List<AlertRule>> alertRulesMap = monitor.getAlertRules();

		final Set<AlertCondition> warningConditions = AlertConditionsBuilder.newInstance()
				.gte(50D)
				.build();
		final Set<AlertCondition> alarmConditions = AlertConditionsBuilder.newInstance()
				.gte(60D)
				.build();

		assertAlertRule(alertRulesMap, TEMPERATURE_PARAMETER, Severity.WARN, warningConditions, AlertRuleType.INSTANCE);
		assertAlertRule(alertRulesMap, TEMPERATURE_PARAMETER, Severity.ALARM, alarmConditions, AlertRuleType.INSTANCE);
	}

	@Test
	void testUpdateWarningToAlarmAlertRulesUseCase3() {
		// WARN OK, ALARM null
		final Monitor monitor = new Monitor();

		final Set<String> result = MonitorAlertRulesVisitor.updateWarningToAlarmAlertRules(
				monitor,
				TEMPERATURE_PARAMETER,
				40D,
				null,
				Temperature::checkTemperatureAbnormallyHighCondition,
				Temperature::checkTemperatureCriticallyHighCondition);

		assertEquals(Set.of(TEMPERATURE_PARAMETER), result);

		final Map<String, List<AlertRule>> alertRulesMap = monitor.getAlertRules();

		final Set<AlertCondition> warningConditions = AlertConditionsBuilder.newInstance()
				.gte(40D)
				.build();

		assertAlertRule(alertRulesMap, TEMPERATURE_PARAMETER, Severity.WARN, warningConditions, AlertRuleType.INSTANCE);
		assertEquals(1, alertRulesMap.get(TEMPERATURE_PARAMETER).size());
	}

	@Test
	void testUpdateWarningToAlarmAlertRulesUseCase4() {
		// WARN null, ALARM OK
		final Monitor monitor = new Monitor();

		final Set<String> result = MonitorAlertRulesVisitor.updateWarningToAlarmAlertRules(
				monitor,
				TEMPERATURE_PARAMETER,
				null,
				40D,
				Temperature::checkTemperatureAbnormallyHighCondition,
				Temperature::checkTemperatureCriticallyHighCondition);

		assertEquals(Set.of(TEMPERATURE_PARAMETER), result);

		final Map<String, List<AlertRule>> alertRulesMap = monitor.getAlertRules();

		final Set<AlertCondition> warningConditions = AlertConditionsBuilder.newInstance()
				.gte(40D)
				.build();

		assertAlertRule(alertRulesMap, TEMPERATURE_PARAMETER, Severity.ALARM, warningConditions, AlertRuleType.INSTANCE);
		assertEquals(1, alertRulesMap.get(TEMPERATURE_PARAMETER).size());
	}

	@Test
	void testUpdateWarningToAlarmAlertRulesUseCase5() {
		// WARN null, ALARM null
		final Monitor monitor = new Monitor();

		final Set<String> result = MonitorAlertRulesVisitor.updateWarningToAlarmAlertRules(
				monitor,
				TEMPERATURE_PARAMETER,
				null,
				null,
				Temperature::checkTemperatureAbnormallyHighCondition,
				Temperature::checkTemperatureCriticallyHighCondition);

		final Map<String, List<AlertRule>> alertRulesMap = monitor.getAlertRules();

		assertTrue(alertRulesMap.isEmpty());
		assertTrue(result.isEmpty());
	}

	@Test
	void testUpdateVoltageInstanceAlertRulesUseCase1() {
		// Lower OK <  Upper OK
		final Monitor monitor = new Monitor();

		final Set<String> result = MonitorAlertRulesVisitor.updateVoltageInstanceAlertRules(monitor, 50D, 1000D);

		assertEquals(Set.of(VOLTAGE_PARAMETER), result);

		final Map<String, List<AlertRule>> alertRulesMap = monitor.getAlertRules();

		final Set<AlertCondition> warningConditions = AlertConditionsBuilder.newInstance()
				.lte(50D)
				.build();
		final Set<AlertCondition> alarmConditions = AlertConditionsBuilder.newInstance()
				.gte(1000D)
				.build();

		assertAlertRule(alertRulesMap, VOLTAGE_PARAMETER, Severity.ALARM, warningConditions, AlertRuleType.INSTANCE);
		assertAlertRule(alertRulesMap, VOLTAGE_PARAMETER, Severity.ALARM, alarmConditions, AlertRuleType.INSTANCE);
	}

	@Test
	void testUpdateVoltageInstanceAlertRulesUseCase2() {
		// Lower OK >  Upper OK
		final Monitor monitor = new Monitor();

		final Set<String> result = MonitorAlertRulesVisitor.updateVoltageInstanceAlertRules(monitor, 1000D, 50D);

		assertEquals(Set.of(VOLTAGE_PARAMETER), result);

		final Map<String, List<AlertRule>> alertRulesMap = monitor.getAlertRules();

		final Set<AlertCondition> warningConditions = AlertConditionsBuilder.newInstance()
				.lte(50D)
				.build();
		final Set<AlertCondition> alarmConditions = AlertConditionsBuilder.newInstance()
				.gte(1000D)
				.build();

		assertAlertRule(alertRulesMap, VOLTAGE_PARAMETER, Severity.ALARM, warningConditions, AlertRuleType.INSTANCE);
		assertAlertRule(alertRulesMap, VOLTAGE_PARAMETER, Severity.ALARM, alarmConditions, AlertRuleType.INSTANCE);
	}

	@Test
	void testUpdateVoltageInstanceAlertRulesUseCase3() {
		// Lower null,  Upper OK > 0
		final Monitor monitor = new Monitor();

		final Set<String> result = MonitorAlertRulesVisitor.updateVoltageInstanceAlertRules(monitor, null, 1000D);

		assertEquals(Set.of(VOLTAGE_PARAMETER), result);

		final Map<String, List<AlertRule>> alertRulesMap = monitor.getAlertRules();

		Set<AlertCondition> warningConditions = AlertConditionsBuilder.newInstance()
				.gte(1000D)
				.build();
		Set<AlertCondition> alarmConditions = AlertConditionsBuilder.newInstance()
				.gte(1100D)
				.build();

		assertAlertRule(alertRulesMap, VOLTAGE_PARAMETER, Severity.WARN, warningConditions, AlertRuleType.INSTANCE);
		assertAlertRule(alertRulesMap, VOLTAGE_PARAMETER, Severity.ALARM, alarmConditions, AlertRuleType.INSTANCE);
	}

	@Test
	void testUpdateVoltageInstanceAlertRulesUseCase4() {
		// Lower null,  Upper OK < 0
		final Monitor monitor = new Monitor();

		final Set<String> result = MonitorAlertRulesVisitor.updateVoltageInstanceAlertRules(monitor, null, -50D);

		assertEquals(Set.of(VOLTAGE_PARAMETER), result);

		final Map<String, List<AlertRule>> alertRulesMap = monitor.getAlertRules();

		final Set<AlertCondition> warningConditions = AlertConditionsBuilder.newInstance()
				.lte(-50D)
				.build();
		final Set<AlertCondition> alarmConditions = AlertConditionsBuilder.newInstance()
				.lte(-55D)
				.build();

		assertAlertRule(alertRulesMap, VOLTAGE_PARAMETER, Severity.WARN, warningConditions, AlertRuleType.INSTANCE);
		assertAlertRule(alertRulesMap, VOLTAGE_PARAMETER, Severity.ALARM, alarmConditions, AlertRuleType.INSTANCE);
	}

	@Test
	void testUpdateVoltageInstanceAlertRulesUseCase5() {
		// Lower OK > 0,  Upper null
		final Monitor monitor = new Monitor();

		final Set<String> result = MonitorAlertRulesVisitor.updateVoltageInstanceAlertRules(monitor, 50D, null);

		assertEquals(Set.of(VOLTAGE_PARAMETER), result);

		final Map<String, List<AlertRule>> alertRulesMap = monitor.getAlertRules();

		final Set<AlertCondition> warningConditions = AlertConditionsBuilder.newInstance()
				.lte(50D)
				.gte(0D)
				.build();
		final Set<AlertCondition> alarmConditions = AlertConditionsBuilder.newInstance()
				.lte(45D)
				.gte(0D)
				.build();

		assertAlertRule(alertRulesMap, VOLTAGE_PARAMETER, Severity.WARN, warningConditions, AlertRuleType.INSTANCE);
		assertAlertRule(alertRulesMap, VOLTAGE_PARAMETER, Severity.ALARM, alarmConditions, AlertRuleType.INSTANCE);
	}

	@Test
	void testUpdateVoltageInstanceAlertRulesUseCase6() {
		// Lower OK < 0,  Upper null
		final Monitor monitor = new Monitor();

		final Set<String> result = MonitorAlertRulesVisitor.updateVoltageInstanceAlertRules(monitor, -50D, null);

		assertEquals(Set.of(VOLTAGE_PARAMETER), result);

		final Map<String, List<AlertRule>> alertRulesMap = monitor.getAlertRules();

		final Set<AlertCondition> warningConditions = AlertConditionsBuilder.newInstance()
				.gte(-50D)
				.lte(0D)
				.build();
		final Set<AlertCondition> alarmConditions = AlertConditionsBuilder.newInstance()
				.gte(-45D)
				.lte(0D)
				.build();

		assertAlertRule(alertRulesMap, VOLTAGE_PARAMETER, Severity.WARN, warningConditions, AlertRuleType.INSTANCE);
		assertAlertRule(alertRulesMap, VOLTAGE_PARAMETER, Severity.ALARM, alarmConditions, AlertRuleType.INSTANCE);
	}

	@Test
	void testUpdateVoltageInstanceAlertRulesUseCase7() {
		// Lower null,  Upper null
		final Monitor monitor = new Monitor();

		final Set<String> result = MonitorAlertRulesVisitor.updateVoltageInstanceAlertRules(monitor, null, null);

		final Map<String, List<AlertRule>> alertRulesMap = monitor.getAlertRules();

		assertTrue(alertRulesMap.isEmpty());
		assertTrue(result.isEmpty());
	}

	@Test
	void testUpdateWarningToAlarmEnhancedAlertRulesUseCase1() {
		// WARN OK < ALARM OK
		final Monitor monitor = new Monitor();

		final Set<String> result = MonitorAlertRulesVisitor.updateWarningToAlarmEnhancedAlertRules(monitor, VALUE_PARAMETER, 100D, 200D,
				OtherDevice::checkValueWarnCondition,
				OtherDevice::checkValueAlarmCondition);

		assertEquals(Set.of(VALUE_PARAMETER), result);

		final Map<String, List<AlertRule>> alertRulesMap = monitor.getAlertRules();

		final Set<AlertCondition> warningConditions = AlertConditionsBuilder.newInstance()
				.gte(100D)
				.build();
		final Set<AlertCondition> alarmConditions = AlertConditionsBuilder.newInstance()
				.gte(200D)
				.build();

		assertAlertRule(alertRulesMap, VALUE_PARAMETER, Severity.WARN, warningConditions, AlertRuleType.INSTANCE);
		assertAlertRule(alertRulesMap, VALUE_PARAMETER, Severity.ALARM, alarmConditions, AlertRuleType.INSTANCE);
	}

	@Test
	void testUpdateWarningToAlarmEnhancedAlertRulesUseCase2() {
		// WARN OK > ALARM OK
		final Monitor monitor = new Monitor();

		final Set<String> result = MonitorAlertRulesVisitor.updateWarningToAlarmEnhancedAlertRules(monitor, VALUE_PARAMETER, 200D, 100D,
				OtherDevice::checkValueWarnCondition,
				OtherDevice::checkValueAlarmCondition);

		assertEquals(Set.of(VALUE_PARAMETER), result);

		final Map<String, List<AlertRule>> alertRulesMap = monitor.getAlertRules();

		final Set<AlertCondition> warningConditions = AlertConditionsBuilder.newInstance()
				.gte(100D)
				.build();
		final Set<AlertCondition> alarmConditions = AlertConditionsBuilder.newInstance()
				.gte(200D)
				.build();

		assertAlertRule(alertRulesMap, VALUE_PARAMETER, Severity.WARN, warningConditions, AlertRuleType.INSTANCE);
		assertAlertRule(alertRulesMap, VALUE_PARAMETER, Severity.ALARM, alarmConditions, AlertRuleType.INSTANCE);
	}

	@Test
	void testUpdateWarningToAlarmEnhancedAlertRulesUseCase3() {
		// WARN null, ALARM OK
		final Monitor monitor = new Monitor();

		final Set<String> result = MonitorAlertRulesVisitor.updateWarningToAlarmEnhancedAlertRules(monitor, VALUE_PARAMETER, null, 100D,
				OtherDevice::checkValueWarnCondition,
				OtherDevice::checkValueAlarmCondition);

		assertEquals(Set.of(VALUE_PARAMETER), result);

		final Map<String, List<AlertRule>> alertRulesMap = monitor.getAlertRules();

		final Set<AlertCondition> warningConditions = AlertConditionsBuilder.newInstance()
				.gte(90D)
				.build();
		final Set<AlertCondition> alarmConditions = AlertConditionsBuilder.newInstance()
				.gte(100D)
				.build();

		assertAlertRule(alertRulesMap, VALUE_PARAMETER, Severity.WARN, warningConditions, AlertRuleType.INSTANCE);
		assertAlertRule(alertRulesMap, VALUE_PARAMETER, Severity.ALARM, alarmConditions, AlertRuleType.INSTANCE);
	}

	@Test
	void testUpdateWarningToAlarmEnhancedAlertRulesUseCase4() {
		// WARN OK, ALARM null
		final Monitor monitor = new Monitor();

		final Set<String> result = MonitorAlertRulesVisitor.updateWarningToAlarmEnhancedAlertRules(monitor, VALUE_PARAMETER, 90D, null,
				OtherDevice::checkValueWarnCondition,
				OtherDevice::checkValueAlarmCondition);

		assertEquals(Set.of(VALUE_PARAMETER), result);

		final Map<String, List<AlertRule>> alertRulesMap = monitor.getAlertRules();

		final Set<AlertCondition> warningConditions = AlertConditionsBuilder.newInstance()
				.gte(90D)
				.build();
		final Set<AlertCondition> alarmConditions = AlertConditionsBuilder.newInstance()
				.gte(99D)
				.build();

		assertAlertRule(alertRulesMap, VALUE_PARAMETER, Severity.WARN, warningConditions, AlertRuleType.INSTANCE);
		assertAlertRule(alertRulesMap, VALUE_PARAMETER, Severity.ALARM, alarmConditions, AlertRuleType.INSTANCE);
	}

	@Test
	void testUpdateWarningToAlarmEnhancedAlertRulesUseCase5() {
		// WARN null, ALARM null
		final Monitor monitor = new Monitor();

		final Set<String> result = MonitorAlertRulesVisitor.updateWarningToAlarmEnhancedAlertRules(monitor, VALUE_PARAMETER, null, null,
				OtherDevice::checkValueWarnCondition,
				OtherDevice::checkValueAlarmCondition);

		final Map<String, List<AlertRule>> alertRulesMap = monitor.getAlertRules();

		assertTrue(alertRulesMap.isEmpty());
		assertTrue(result.isEmpty());
	}

	/**
	 * Check alert rule instance by type, conditions and severity
	 * 
	 * @param actualAlertRules
	 * @param parameterName
	 * @param expectedSeverity
	 * @param expectedConditions
	 * @param expectedType
	 */
	private void assertAlertRule(final Map<String, List<AlertRule>> actualAlertRules, final String parameterName,
			final Severity expectedSeverity, final Set<AlertCondition> expectedConditions, final AlertRuleType expectedType) {
		final AlertRule alertRule = actualAlertRules.get(parameterName)
				.stream()
				.filter(rule -> expectedSeverity.equals(rule.getSeverity())
							&& expectedConditions.equals(rule.getConditions()))
				.findFirst().orElse(null);

		assertNotNull(alertRule);
		assertEquals(expectedType, alertRule.getType());
		assertEquals(expectedConditions, alertRule.getConditions());
	}

	@Test
	void testCustructor() {
		assertDoesNotThrow(() -> new MonitorAlertRulesVisitor(null));
		final Monitor monitor = Monitor.builder().metadata(null).build();
		assertThrows(IllegalArgumentException.class, () -> new MonitorAlertRulesVisitor(monitor));
		monitor.setMetadata(new HashMap<>());
		assertDoesNotThrow(() -> new MonitorAlertRulesVisitor(monitor));
	}

	@Test
	void testVisitNullMonitor() {
		final MonitorAlertRulesVisitor visitor = new MonitorAlertRulesVisitor(null);
		Stream.of(MonitorType.values()).forEach(type -> assertDoesNotThrow(() -> type.getMetaMonitor().accept(visitor)));
	}

	@Test
	void testProcessLunInstanceAlertRules() {
		final Monitor monitor = new Monitor();
		monitor.addMetadata(AVAILABLE_PATH_WARNING, "2");

		final Set<String> result = MonitorAlertRulesVisitor.processLunInstanceAlertRules(monitor);
		assertEquals(Collections.singleton(AVAILABLE_PATH_COUNT_PARAMETER), result);

		final Set<AlertCondition> warningConditions = AlertConditionsBuilder.newInstance()
				.gte(2D)
				.build();

		assertAlertRule(monitor.getAlertRules(), AVAILABLE_PATH_COUNT_PARAMETER, Severity.WARN, warningConditions, AlertRuleType.INSTANCE);
	}

	@Test
	void testProcessLunInstanceAlertRulesNoMetadataThreshold() {
		final Monitor monitor = new Monitor();

		final Set<String> result = MonitorAlertRulesVisitor.processLunInstanceAlertRules(monitor);
		assertTrue(result.isEmpty());
		assertTrue(monitor.getAlertRules().isEmpty());
	}

	@Test
	void testProcessLunInstanceAlertRulesSameThresholdsAsStatic() {
		final Monitor monitor = new Monitor();
		monitor.addMetadata(AVAILABLE_PATH_WARNING, "0");
		final Set<String> result = MonitorAlertRulesVisitor.processLunInstanceAlertRules(monitor);
		assertTrue(result.isEmpty());
		assertTrue(monitor.getAlertRules().isEmpty());
	}

	@Test
	void testProcessNetworkCardInstanceAlertRulesNonValidThresholds() {
		{
			final Monitor monitor = new Monitor();
			monitor.addMetadata(ERROR_PERCENT_WARNING_THRESHOLD, "-1");
			monitor.addMetadata(ERROR_PERCENT_ALARM_THRESHOLD, "100");

			final Set<String> result = MonitorAlertRulesVisitor.processNetworkCardInstanceAlertRules(monitor);
			assertEquals(Collections.singleton(ERROR_PERCENT_PARAMETER), result);

			assertErrorPercentDefault(monitor);
		}

		{
			final Monitor monitor = new Monitor();
			monitor.addMetadata(ERROR_PERCENT_WARNING_THRESHOLD, "20");
			monitor.addMetadata(ERROR_PERCENT_ALARM_THRESHOLD, "-100");

			final Set<String> result = MonitorAlertRulesVisitor.processNetworkCardInstanceAlertRules(monitor);
			assertEquals(Collections.singleton(ERROR_PERCENT_PARAMETER), result);

			assertErrorPercentDefault(monitor);
		}

		{
			final Monitor monitor = new Monitor();
			monitor.addMetadata(ERROR_PERCENT_WARNING_THRESHOLD, "-20");
			monitor.addMetadata(ERROR_PERCENT_ALARM_THRESHOLD, "-100");

			final Set<String> result = MonitorAlertRulesVisitor.processNetworkCardInstanceAlertRules(monitor);
			assertEquals(Collections.singleton(ERROR_PERCENT_PARAMETER), result);

			assertErrorPercentDefault(monitor);
		}
	}

	private void assertErrorPercentDefault(final Monitor monitor) {
		final Set<AlertCondition> warningConditions = AlertConditionsBuilder.newInstance()
				.gte(20D)
				.build();

		final Set<AlertCondition> alarmConditions = AlertConditionsBuilder.newInstance()
				.gte(30D)
				.build();

		assertAlertRule(monitor.getAlertRules(), ERROR_PERCENT_PARAMETER, Severity.WARN, warningConditions, AlertRuleType.INSTANCE);
		assertAlertRule(monitor.getAlertRules(), ERROR_PERCENT_PARAMETER, Severity.ALARM, alarmConditions, AlertRuleType.INSTANCE);
	}

	@Test
	void testProcessNetworkCardInstanceAlertRules() {
		final Monitor monitor = new Monitor();
		monitor.addMetadata(ERROR_PERCENT_WARNING_THRESHOLD, "80");
		monitor.addMetadata(ERROR_PERCENT_ALARM_THRESHOLD, "90");

		final Set<String> result = MonitorAlertRulesVisitor.processNetworkCardInstanceAlertRules(monitor);
		assertEquals(Collections.singleton(ERROR_PERCENT_PARAMETER), result);

		final Set<AlertCondition> warningConditions = AlertConditionsBuilder.newInstance()
				.gte(80D)
				.build();

		final Set<AlertCondition> alarmConditions = AlertConditionsBuilder.newInstance()
				.gte(90D)
				.build();

		assertAlertRule(monitor.getAlertRules(), ERROR_PERCENT_PARAMETER, Severity.WARN, warningConditions, AlertRuleType.INSTANCE);
		assertAlertRule(monitor.getAlertRules(), ERROR_PERCENT_PARAMETER, Severity.ALARM, alarmConditions, AlertRuleType.INSTANCE);
	}
}
