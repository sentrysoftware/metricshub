package com.sentrysoftware.matrix.engine.strategy.discovery;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.*;


import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import com.sentrysoftware.matrix.common.meta.monitor.Gpu;
import com.sentrysoftware.matrix.common.meta.monitor.OtherDevice;
import com.sentrysoftware.matrix.common.meta.monitor.Host;
import com.sentrysoftware.matrix.common.meta.monitor.NetworkCard;
import com.sentrysoftware.matrix.common.meta.monitor.Temperature;
import com.sentrysoftware.matrix.common.meta.monitor.Vm;
import com.sentrysoftware.matrix.common.meta.parameter.state.Up;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.strategy.collect.CollectHelper;
import com.sentrysoftware.matrix.engine.strategy.discovery.MonitorAlertRulesVisitor.ThresholdInfo;
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

		new MonitorAlertRulesVisitor(monitor).processStaticAlertRules(monitor, new Fan());

		assertNotNull(monitor.getAlertRules().get(PRESENT_PARAMETER));
		assertNotNull(monitor.getAlertRules().get(STATUS_PARAMETER));
		assertNotNull(monitor.getAlertRules().get(SPEED_PARAMETER));
		assertNotNull(monitor.getAlertRules().get(SPEED_PERCENT_PARAMETER));

	}

	@Test 
	void testProcessHostProtocolAlertRules() {
		final Monitor monitor = new Monitor();

		CollectHelper.updateDiscreteParameter(monitor, SNMP_UP_PARAMETER, monitor.getDiscoveryTime(), Up.UP);
		CollectHelper.updateDiscreteParameter(monitor, SSH_UP_PARAMETER, monitor.getDiscoveryTime(), Up.UP);
		CollectHelper.updateDiscreteParameter(monitor, WMI_UP_PARAMETER, monitor.getDiscoveryTime(), Up.UP);
		CollectHelper.updateDiscreteParameter(monitor, WBEM_UP_PARAMETER, monitor.getDiscoveryTime(), Up.UP);
		CollectHelper.updateDiscreteParameter(monitor, HTTP_UP_PARAMETER, monitor.getDiscoveryTime(), Up.UP);
		CollectHelper.updateDiscreteParameter(monitor, IPMI_UP_PARAMETER, monitor.getDiscoveryTime(), Up.UP);

		new MonitorAlertRulesVisitor(monitor).processStaticAlertRules(monitor, new Host());
	
		final Map<String, List<AlertRule>> alertRulesMap = monitor.getAlertRules();
		final Set<AlertCondition> alarmConditions = AlertConditionsBuilder.newInstance()
			.eq(0D)
			.build();

		assertAlertRule(alertRulesMap, SNMP_UP_PARAMETER, Severity.ALARM, alarmConditions, AlertRuleType.STATIC);
		assertAlertRule(alertRulesMap, SSH_UP_PARAMETER, Severity.ALARM, alarmConditions, AlertRuleType.STATIC);
		assertAlertRule(alertRulesMap, WMI_UP_PARAMETER, Severity.ALARM, alarmConditions, AlertRuleType.STATIC);
		assertAlertRule(alertRulesMap, WBEM_UP_PARAMETER, Severity.ALARM, alarmConditions, AlertRuleType.STATIC);
		assertAlertRule(alertRulesMap, HTTP_UP_PARAMETER, Severity.ALARM, alarmConditions, AlertRuleType.STATIC);
		assertAlertRule(alertRulesMap, IPMI_UP_PARAMETER, Severity.ALARM, alarmConditions, AlertRuleType.STATIC);
	}

	@Test
	void testUpdateFanInstanceSpeedAlertRulesCase1() {
		// WARN > ALARM
		final Monitor monitor = new Monitor();
		final Set<String> result = new MonitorAlertRulesVisitor(monitor)
			.updateFanInstanceSpeedAlertRules(
				monitor,
				SPEED_PARAMETER,
				new ThresholdInfo(WARNING_THRESHOLD, 500D),
				new ThresholdInfo(ALARM_THRESHOLD, 0D),
				false
			);
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

		assertEquals("500", monitor.getMetadata(WARNING_THRESHOLD));
		assertEquals("0", monitor.getMetadata(ALARM_THRESHOLD));

	}

	@Test
	void testUpdateFanInstanceSpeedAlertRulesCase2() {
		// WARN < ALARM
		final Monitor monitor = new Monitor();
		final Set<String> result = new MonitorAlertRulesVisitor(monitor)
			.updateFanInstanceSpeedAlertRules(
				monitor,
				SPEED_PARAMETER,
				new ThresholdInfo(WARNING_THRESHOLD, 5D),
				new ThresholdInfo(ALARM_THRESHOLD, 500D),
				false
			);
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

		assertEquals("500", monitor.getMetadata(WARNING_THRESHOLD));
		assertEquals("5", monitor.getMetadata(ALARM_THRESHOLD));
	}

	@Test
	void testUpdateFanInstanceSpeedAlertRulesCase3() {
		// WARN null, ALARM OK
		final Monitor monitor = new Monitor();
		final Set<String> result = new MonitorAlertRulesVisitor(monitor)
			.updateFanInstanceSpeedAlertRules(
				monitor,
				SPEED_PARAMETER,
				new ThresholdInfo(WARNING_THRESHOLD, null),
				new ThresholdInfo(ALARM_THRESHOLD, 5D),
				false
			);
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

		assertEquals("5.5", monitor.getMetadata(WARNING_THRESHOLD));
		assertEquals("5", monitor.getMetadata(ALARM_THRESHOLD));
	}

	@Test
	void testUpdateFanInstanceSpeedAlertRulesUseCase4() {
		// WARN OK, ALARM null
		final Monitor monitor = new Monitor();
		final Set<String> result = new MonitorAlertRulesVisitor(monitor)
			.updateFanInstanceSpeedAlertRules(
				monitor,
				SPEED_PARAMETER,
				new ThresholdInfo(WARNING_THRESHOLD, 5D),
				new ThresholdInfo(ALARM_THRESHOLD, null),
				false
			);
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

		assertEquals("5", monitor.getMetadata(WARNING_THRESHOLD));
		assertEquals("4.5", monitor.getMetadata(ALARM_THRESHOLD));
	}

	@Test
	void testUpdateFanInstanceSpeedAlertRulesUseCase5() {
		// WARN null, ALARM null
		final Monitor monitor = new Monitor();
		final Set<String> result = new MonitorAlertRulesVisitor(monitor)
			.updateFanInstanceSpeedAlertRules(
				monitor,
				SPEED_PARAMETER,
				new ThresholdInfo(WARNING_THRESHOLD, null),
				new ThresholdInfo(ALARM_THRESHOLD, null),
				false
			);
		final Map<String, List<AlertRule>> alertRulesMap = monitor.getAlertRules();

		assertTrue(alertRulesMap.isEmpty());
		assertTrue(result.isEmpty());

		assertEquals("500", monitor.getMetadata(WARNING_THRESHOLD));
		assertEquals("0", monitor.getMetadata(ALARM_THRESHOLD));
	}

	@Test
	void testUpdateWarningToAlarmAlertRulesUseCase1() {
		// WARN OK < ALARM OK
		final Monitor monitor = new Monitor();

		final Set<String> result = new MonitorAlertRulesVisitor(monitor)
			.updateWarningToAlarmAlertRules(
				monitor,
				TEMPERATURE_PARAMETER,
				new ThresholdInfo(WARNING_THRESHOLD, 50D),
				new ThresholdInfo(ALARM_THRESHOLD, 60D),
				Temperature::checkTemperatureAbnormallyHighCondition,
				Temperature::checkTemperatureCriticallyHighCondition
			);

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

		assertEquals("50", monitor.getMetadata(WARNING_THRESHOLD));
		assertEquals("60", monitor.getMetadata(ALARM_THRESHOLD));
	}

	@Test
	void testUpdateWarningToAlarmAlertRulesUseCase2() {
		// WARN OK > ALARM OK
		final Monitor monitor = new Monitor();

		final Set<String> result = new MonitorAlertRulesVisitor(monitor)
			.updateWarningToAlarmAlertRules(
				monitor,
				TEMPERATURE_PARAMETER,
				new ThresholdInfo(WARNING_THRESHOLD, 60D),
				new ThresholdInfo(ALARM_THRESHOLD, 50D),
				Temperature::checkTemperatureAbnormallyHighCondition,
				Temperature::checkTemperatureCriticallyHighCondition
			);

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

		assertEquals("50", monitor.getMetadata(WARNING_THRESHOLD));
		assertEquals("60", monitor.getMetadata(ALARM_THRESHOLD));
	}

	@Test
	void testUpdateWarningToAlarmAlertRulesUseCase3() {
		// WARN OK, ALARM null
		final Monitor monitor = new Monitor();

		final Set<String> result = new MonitorAlertRulesVisitor(monitor)
			.updateWarningToAlarmAlertRules(
				monitor,
				TEMPERATURE_PARAMETER,
				new ThresholdInfo(WARNING_THRESHOLD, 40D),
				new ThresholdInfo(ALARM_THRESHOLD, null),
				Temperature::checkTemperatureAbnormallyHighCondition,
				Temperature::checkTemperatureCriticallyHighCondition
			);

		assertEquals(Set.of(TEMPERATURE_PARAMETER), result);

		final Map<String, List<AlertRule>> alertRulesMap = monitor.getAlertRules();

		final Set<AlertCondition> warningConditions = AlertConditionsBuilder.newInstance()
			.gte(40D)
			.build();

		assertAlertRule(alertRulesMap, TEMPERATURE_PARAMETER, Severity.WARN, warningConditions, AlertRuleType.INSTANCE);
		assertEquals(1, alertRulesMap.get(TEMPERATURE_PARAMETER).size());

		assertEquals("40", monitor.getMetadata(WARNING_THRESHOLD));
		assertNull(monitor.getMetadata(ALARM_THRESHOLD));
	}

	@Test
	void testUpdateWarningToAlarmAlertRulesUseCase4() {
		// WARN null, ALARM OK
		final Monitor monitor = new Monitor();

		final Set<String> result = new MonitorAlertRulesVisitor(monitor)
			.updateWarningToAlarmAlertRules(
				monitor,
				TEMPERATURE_PARAMETER,
				new ThresholdInfo(WARNING_THRESHOLD, null),
				new ThresholdInfo(ALARM_THRESHOLD, 40D),
				Temperature::checkTemperatureAbnormallyHighCondition,
				Temperature::checkTemperatureCriticallyHighCondition
			);

		assertEquals(Set.of(TEMPERATURE_PARAMETER), result);

		final Map<String, List<AlertRule>> alertRulesMap = monitor.getAlertRules();

		final Set<AlertCondition> alarmConditions = AlertConditionsBuilder.newInstance()
			.gte(40D)
			.build();

		assertAlertRule(alertRulesMap, TEMPERATURE_PARAMETER, Severity.ALARM, alarmConditions, AlertRuleType.INSTANCE);
		assertEquals(1, alertRulesMap.get(TEMPERATURE_PARAMETER).size());

		assertEquals("40", monitor.getMetadata(ALARM_THRESHOLD));
		assertNull(monitor.getMetadata(WARNING_THRESHOLD));
	}

	@Test
	void testUpdateWarningToAlarmAlertRulesUseCase5() {
		// WARN null, ALARM null
		final Monitor monitor = new Monitor();

		final Set<String> result = new MonitorAlertRulesVisitor(monitor)
			.updateWarningToAlarmAlertRules(
				monitor,
				TEMPERATURE_PARAMETER,
				new ThresholdInfo(WARNING_THRESHOLD, null),
				new ThresholdInfo(ALARM_THRESHOLD, null),
				Temperature::checkTemperatureAbnormallyHighCondition,
				Temperature::checkTemperatureCriticallyHighCondition
			);

		final Map<String, List<AlertRule>> alertRulesMap = monitor.getAlertRules();

		assertTrue(alertRulesMap.isEmpty());
		assertTrue(result.isEmpty());

		assertNull(monitor.getMetadata(WARNING_THRESHOLD));
		assertNull(monitor.getMetadata(ALARM_THRESHOLD));
	}

	@Test
	void testUpdateVoltageInstanceAlertRulesUseCase1() {
		// Lower OK <  Upper OK
		final Monitor monitor = new Monitor();
		monitor.addMetadata(LOWER_THRESHOLD, "50");
		monitor.addMetadata(UPPER_THRESHOLD, "1000");

		final Set<String> result = new MonitorAlertRulesVisitor(monitor).updateVoltageInstanceAlertRules(monitor);

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

		assertEquals("50", monitor.getMetadata(LOWER_THRESHOLD));
		assertEquals("1000", monitor.getMetadata(UPPER_THRESHOLD));
	}

	@Test
	void testUpdateVoltageInstanceAlertRulesUseCase2() {
		// Lower OK >  Upper OK
		final Monitor monitor = new Monitor();
		monitor.addMetadata(LOWER_THRESHOLD, "1000");
		monitor.addMetadata(UPPER_THRESHOLD, "50");

		final Set<String> result = new MonitorAlertRulesVisitor(monitor).updateVoltageInstanceAlertRules(monitor);

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

		assertEquals("50", monitor.getMetadata(LOWER_THRESHOLD));
		assertEquals("1000", monitor.getMetadata(UPPER_THRESHOLD));
	}

	@Test
	void testUpdateVoltageInstanceAlertRulesUseCase3() {
		// Lower null,  Upper OK > 0
		final Monitor monitor = new Monitor();
		monitor.addMetadata(LOWER_THRESHOLD, null);
		monitor.addMetadata(UPPER_THRESHOLD, "1000");

		final Set<String> result = new MonitorAlertRulesVisitor(monitor).updateVoltageInstanceAlertRules(monitor);

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

		assertEquals("1000", monitor.getMetadata(LOWER_THRESHOLD));
		assertEquals("1100", monitor.getMetadata(UPPER_THRESHOLD));
	}

	@Test
	void testUpdateVoltageInstanceAlertRulesUseCase4() {
		// Lower null,  Upper OK < 0
		final Monitor monitor = new Monitor();
		monitor.addMetadata(LOWER_THRESHOLD, null);
		monitor.addMetadata(UPPER_THRESHOLD, "-50");

		final Set<String> result = new MonitorAlertRulesVisitor(monitor).updateVoltageInstanceAlertRules(monitor);

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

		assertEquals("-50", monitor.getMetadata(UPPER_THRESHOLD));
		assertEquals("-55", monitor.getMetadata(LOWER_THRESHOLD));
	}

	@Test
	void testUpdateVoltageInstanceAlertRulesUseCase5() {
		// Lower OK > 0,  Upper null
		final Monitor monitor = new Monitor();
		monitor.addMetadata(LOWER_THRESHOLD, "50");
		monitor.addMetadata(UPPER_THRESHOLD, null);

		final Set<String> result = new MonitorAlertRulesVisitor(monitor)
				.updateVoltageInstanceAlertRules(monitor);

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

		assertEquals("45", monitor.getMetadata(LOWER_THRESHOLD));
		assertEquals("50", monitor.getMetadata(UPPER_THRESHOLD));
	}

	@Test
	void testUpdateVoltageInstanceAlertRulesUseCase6() {
		// Lower OK < 0,  Upper null
		final Monitor monitor = new Monitor();
		monitor.addMetadata(LOWER_THRESHOLD, "-50");
		monitor.addMetadata(UPPER_THRESHOLD, null);

		final Set<String> result = new MonitorAlertRulesVisitor(monitor)
				.updateVoltageInstanceAlertRules(monitor);

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

		assertEquals("-45", monitor.getMetadata(UPPER_THRESHOLD));
		assertEquals("-50", monitor.getMetadata(LOWER_THRESHOLD));
	}

	@Test
	void testUpdateVoltageInstanceAlertRulesUseCase7() {
		// Lower null,  Upper null
		final Monitor monitor = new Monitor();

		final Set<String> result = new MonitorAlertRulesVisitor(monitor)
				.updateVoltageInstanceAlertRules(monitor);

		final Map<String, List<AlertRule>> alertRulesMap = monitor.getAlertRules();

		assertTrue(alertRulesMap.isEmpty());
		assertTrue(result.isEmpty());

		assertNull(monitor.getMetadata(LOWER_THRESHOLD));
		assertNull(monitor.getMetadata(UPPER_THRESHOLD));
	}

	@Test
	void testUpdateWarningToAlarmEnhancedAlertRulesUseCase1() {
		// WARN OK < ALARM OK
		final Monitor monitor = new Monitor();

		final Set<String> result = new MonitorAlertRulesVisitor(monitor)
			.updateWarningToAlarmEnhancedAlertRules(
				monitor,
				VALUE_PARAMETER,
				new ThresholdInfo(VALUE_WARNING_THRESHOLD, 100D),
				new ThresholdInfo(VALUE_ALARM_THRESHOLD, 200D),
				OtherDevice::checkValueWarnCondition,
				OtherDevice::checkValueAlarmCondition,
				false
			);

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

		assertEquals("100", monitor.getMetadata(VALUE_WARNING_THRESHOLD));
		assertEquals("200", monitor.getMetadata(VALUE_ALARM_THRESHOLD));
	}

	@Test
	void testUpdateWarningToAlarmEnhancedAlertRulesUseCase2() {
		// WARN OK > ALARM OK
		final Monitor monitor = new Monitor();

		final Set<String> result = new MonitorAlertRulesVisitor(monitor)
			.updateWarningToAlarmEnhancedAlertRules(
				monitor,
				VALUE_PARAMETER,
				new ThresholdInfo(VALUE_WARNING_THRESHOLD, 200D),
				new ThresholdInfo(VALUE_ALARM_THRESHOLD, 100D),
				OtherDevice::checkValueWarnCondition,
				OtherDevice::checkValueAlarmCondition,
				false
			);

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

		assertEquals("100", monitor.getMetadata(VALUE_WARNING_THRESHOLD));
		assertEquals("200", monitor.getMetadata(VALUE_ALARM_THRESHOLD));
	}

	@Test
	void testUpdateWarningToAlarmEnhancedAlertRulesUseCase3() {
		// WARN null, ALARM OK
		final Monitor monitor = new Monitor();

		final Set<String> result = new MonitorAlertRulesVisitor(monitor)
			.updateWarningToAlarmEnhancedAlertRules(
				monitor,
				VALUE_PARAMETER,
				new ThresholdInfo(VALUE_WARNING_THRESHOLD, null),
				new ThresholdInfo(VALUE_ALARM_THRESHOLD, 100D),
				OtherDevice::checkValueWarnCondition,
				OtherDevice::checkValueAlarmCondition,
				false
			);

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

		assertEquals("90", monitor.getMetadata(VALUE_WARNING_THRESHOLD));
		assertEquals("100", monitor.getMetadata(VALUE_ALARM_THRESHOLD));
	}

	@Test
	void testUpdateWarningToAlarmEnhancedAlertRulesUseCase4() {
		// WARN OK, ALARM null
		final Monitor monitor = new Monitor();

		final Set<String> result = new MonitorAlertRulesVisitor(monitor)
			.updateWarningToAlarmEnhancedAlertRules(
				monitor,
				VALUE_PARAMETER,
				new ThresholdInfo(VALUE_WARNING_THRESHOLD, 90D),
				new ThresholdInfo(VALUE_ALARM_THRESHOLD, null),
				OtherDevice::checkValueWarnCondition,
				OtherDevice::checkValueAlarmCondition,
				false
			);

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

		assertEquals("90", monitor.getMetadata(VALUE_WARNING_THRESHOLD));
		assertEquals("99", monitor.getMetadata(VALUE_ALARM_THRESHOLD));
	}

	@Test
	void testUpdateWarningToAlarmEnhancedAlertRulesUseCase5() {
		// WARN null, ALARM null
		final Monitor monitor = new Monitor();

		final Set<String> result = new MonitorAlertRulesVisitor(monitor)
			.updateWarningToAlarmEnhancedAlertRules(
				monitor,
				VALUE_PARAMETER,
				new ThresholdInfo(VALUE_WARNING_THRESHOLD, null),
				new ThresholdInfo(VALUE_ALARM_THRESHOLD, null),
				OtherDevice::checkValueWarnCondition,
				OtherDevice::checkValueAlarmCondition,
				false
			);

		final Map<String, List<AlertRule>> alertRulesMap = monitor.getAlertRules();

		assertTrue(alertRulesMap.isEmpty());
		assertTrue(result.isEmpty());

		assertNull(monitor.getMetadata(VALUE_WARNING_THRESHOLD));
		assertNull(monitor.getMetadata(VALUE_ALARM_THRESHOLD));
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
			.filter(rule -> expectedSeverity.equals(rule.getSeverity()) && expectedConditions.equals(rule.getConditions()))
			.findFirst()
			.orElse(null);

		assertNotNull(alertRule);
		assertEquals(expectedType, alertRule.getType());
		assertEquals(expectedConditions, alertRule.getConditions());
	}

	@Test
	void testCustructor() {
		assertThrows(IllegalArgumentException.class, () -> new MonitorAlertRulesVisitor(null));
		final Monitor monitor = Monitor.builder().metadata(null).build();
		assertThrows(IllegalArgumentException.class, () -> new MonitorAlertRulesVisitor(monitor));
		monitor.setMetadata(new HashMap<>());
		assertDoesNotThrow(() -> new MonitorAlertRulesVisitor(monitor));
	}

	@Test
	void testVisitNullMonitor() {
		final Monitor monitor = Monitor.builder().metadata(new HashMap<>()).build();
		final MonitorAlertRulesVisitor visitor = new MonitorAlertRulesVisitor(monitor);
		Stream.of(MonitorType.values()).forEach(type -> assertDoesNotThrow(() -> type.getMetaMonitor().accept(visitor)));
	}

	@Test
	void testProcessLunInstanceAlertRules() {
		final Monitor monitor = new Monitor();
		monitor.addMetadata(AVAILABLE_PATH_WARNING, "2");

		final Set<String> result = new MonitorAlertRulesVisitor(monitor).processLunInstanceAlertRules(monitor);

		assertEquals(Collections.singleton(AVAILABLE_PATH_COUNT_PARAMETER), result);

		final Set<AlertCondition> warningConditions = AlertConditionsBuilder.newInstance()
			.lte(2D)
			.build();

		assertAlertRule(monitor.getAlertRules(), AVAILABLE_PATH_COUNT_PARAMETER, Severity.WARN, warningConditions, AlertRuleType.INSTANCE);
	}

	@Test
	void testProcessLunInstanceAlertRulesNoMetadataThreshold() {
		final Monitor monitor = new Monitor();

		final Set<String> result = new MonitorAlertRulesVisitor(monitor).processLunInstanceAlertRules(monitor);

		assertTrue(result.isEmpty());
		assertTrue(monitor.getAlertRules().isEmpty());
	}

	@Test
	void testProcessLunInstanceAlertRulesSameThresholdsAsStatic() {
		final Monitor monitor = new Monitor();
		monitor.addMetadata(AVAILABLE_PATH_WARNING, "0");
		final Set<String> result = new MonitorAlertRulesVisitor(monitor).processLunInstanceAlertRules(monitor);

		assertTrue(result.isEmpty());
		assertTrue(monitor.getAlertRules().isEmpty());
	}

	@Test
	void testVisitNetworkCardInstanceAlertRulesNonValidThresholds() {
		{
			final Monitor monitor = new Monitor();
			monitor.addMetadata(ERROR_PERCENT_WARNING_THRESHOLD, "-1");
			monitor.addMetadata(ERROR_PERCENT_ALARM_THRESHOLD, "100");

			new MonitorAlertRulesVisitor(monitor).visit(new NetworkCard());

			assertErrorPercentDefault(monitor);
		}

		{
			final Monitor monitor = new Monitor();
			monitor.addMetadata(ERROR_PERCENT_WARNING_THRESHOLD, "20");
			monitor.addMetadata(ERROR_PERCENT_ALARM_THRESHOLD, "-100");

			new MonitorAlertRulesVisitor(monitor).visit(new NetworkCard());

			assertErrorPercentDefault(monitor);
		}

		{
			final Monitor monitor = new Monitor();
			monitor.addMetadata(ERROR_PERCENT_WARNING_THRESHOLD, "-20");
			monitor.addMetadata(ERROR_PERCENT_ALARM_THRESHOLD, "-100");

			new MonitorAlertRulesVisitor(monitor).visit(new NetworkCard());

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

		assertAlertRule(monitor.getAlertRules(), ERROR_PERCENT_PARAMETER, Severity.WARN, warningConditions, AlertRuleType.STATIC);
		assertAlertRule(monitor.getAlertRules(), ERROR_PERCENT_PARAMETER, Severity.ALARM, alarmConditions, AlertRuleType.STATIC);
	}

	@Test
	void testProcessNetworkCardInstanceAlertRules() {
		final Monitor monitor = new Monitor();
		monitor.addMetadata(ERROR_PERCENT_WARNING_THRESHOLD, "80");
		monitor.addMetadata(ERROR_PERCENT_ALARM_THRESHOLD, "90");

		final Set<String> result = new MonitorAlertRulesVisitor(monitor)
			.processNetworkCardInstanceAlertRules(monitor);

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

	@Test
	void testVisitVm() {
		final Monitor monitor = Monitor.builder().metadata(new HashMap<>()).build();
		assertDoesNotThrow(() -> new MonitorAlertRulesVisitor(monitor).visit(new Vm()));
	}

	@Test
	void testVisitGpu() {
		final Monitor monitor = Monitor.builder().metadata(new HashMap<>()).build();
		assertDoesNotThrow(() -> new MonitorAlertRulesVisitor(monitor).visit(new Gpu()));
	}

	@Test
	void testProcessGpuInstanceAlertRules() {
		final Monitor monitor = Monitor.builder().metadata(new HashMap<>()).build();
		final Set<AlertCondition> userTimePercentWarningConditions = AlertConditionsBuilder.newInstance()
			.gte(80D)
			.build();

		final Set<AlertCondition> userTimePercentAlarmConditions = AlertConditionsBuilder.newInstance()
			.gte(90D)
			.build();

		new MonitorAlertRulesVisitor(monitor).processGpuInstanceAlertRules(monitor);

		assertAlertRule(monitor.getAlertRules(), USED_TIME_PERCENT_PARAMETER, Severity.WARN, userTimePercentWarningConditions, AlertRuleType.INSTANCE);
		assertAlertRule(monitor.getAlertRules(), USED_TIME_PERCENT_PARAMETER, Severity.ALARM, userTimePercentAlarmConditions, AlertRuleType.INSTANCE);

		final Set<AlertCondition> memoryUtilizationWarningConditions = AlertConditionsBuilder.newInstance()
			.gte(90D)
			.build();

		final Set<AlertCondition> memoryUtilizationAlarmConditions = AlertConditionsBuilder.newInstance()
			.gte(95D)
			.build();

		assertAlertRule(monitor.getAlertRules(), MEMORY_UTILIZATION_PARAMETER, Severity.WARN, memoryUtilizationWarningConditions, AlertRuleType.INSTANCE);
		assertAlertRule(monitor.getAlertRules(), MEMORY_UTILIZATION_PARAMETER, Severity.ALARM, memoryUtilizationAlarmConditions, AlertRuleType.INSTANCE);

		
	}
}
