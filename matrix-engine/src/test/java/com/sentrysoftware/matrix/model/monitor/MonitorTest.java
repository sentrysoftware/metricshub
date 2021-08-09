package com.sentrysoftware.matrix.model.monitor;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SPEED_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STATUS_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.PRESENT_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.SPEED_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.SPEED_WARN_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.STATUS_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.STATUS_WARN_CONDITION;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.common.meta.monitor.Fan;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.model.alert.AlertCondition;
import com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder;
import com.sentrysoftware.matrix.model.alert.AlertRule;
import com.sentrysoftware.matrix.model.alert.AlertRule.AlertRuleState;
import com.sentrysoftware.matrix.model.monitor.Monitor.AssertedParameter;
import com.sentrysoftware.matrix.model.parameter.NumberParam;
import com.sentrysoftware.matrix.model.parameter.ParameterState;
import com.sentrysoftware.matrix.model.parameter.PresentParam;
import com.sentrysoftware.matrix.model.parameter.StatusParam;


class MonitorTest {

	@Test
	void testAddAlertRules() {
		final Monitor monitor = Monitor.builder().build();
		final List<AlertRule> alertRulesToAdd = MonitorType.FAN
				.getMetaMonitor()
				.getStaticAlertRules()
				.get(SPEED_PARAMETER)
				.stream().map(AlertRule::copy).collect(Collectors.toList());
		monitor.addAlertRules(SPEED_PARAMETER, alertRulesToAdd);

		List<AlertRule> lastAlertRules = monitor.getAlertRules().get(SPEED_PARAMETER);
		assertEquals(lastAlertRules, alertRulesToAdd);

		// The alert rule shape changed ?
		AlertRule alarmOrigin = lastAlertRules.stream().filter(rule -> rule.getConditions().equals(SPEED_ALARM_CONDITION))
				.findFirst().orElse(null) ;
		// change its state to check later it hasn't been erased
		alarmOrigin.setActive(AlertRuleState.ACTIVE);

		final AlertRule sameSpeedAlert = new AlertRule((mo, conditions) -> 
			Fan.checkZeroSpeedCondition(mo, HardwareConstants.SPEED_PARAMETER, conditions),
			SPEED_ALARM_CONDITION, // Same condition
			ParameterState.ALARM);

		final Set<AlertCondition> warnConditions = AlertConditionsBuilder.newInstance().gt(0D).lte(300D).build();
		final AlertRule newWarnAlert = new AlertRule((mo, conditions) -> 
			Fan.checkOutOfRangeSpeedCondition(mo, HardwareConstants.SPEED_PARAMETER, conditions),
			warnConditions, // Original is 500.0
			ParameterState.WARN);

		monitor.addAlertRules(SPEED_PARAMETER, Arrays.asList(sameSpeedAlert, newWarnAlert));

		assertNotEquals(lastAlertRules, alertRulesToAdd);
		AlertRule warnAlertRule = lastAlertRules.stream().filter(rule -> rule.getConditions().equals(warnConditions))
				.findFirst().orElse(null);
		AlertRule alarmAlertRule = lastAlertRules.stream().filter(rule -> rule.getConditions().equals(SPEED_ALARM_CONDITION))
				.findFirst().orElse(null);
		assertEquals(newWarnAlert, warnAlertRule);
		assertEquals(alarmOrigin, alarmAlertRule);
	}

	@Test
	void testAssertPresentParameter() {
		{
			final Monitor monitor = Monitor.builder()
					.monitorType(MonitorType.FAN)
					.build();
			// Collect
			PresentParam present = PresentParam.present();
			monitor.addParameter(present);

			final AssertedParameter<PresentParam> assertedPresentParameter = monitor.assertPresentParameter(PRESENT_ALARM_CONDITION);

			assertEquals(present, assertedPresentParameter.getParameter());
			assertFalse(assertedPresentParameter.isAbnormal());
		}

		{
			final Monitor monitor = Monitor.builder()
					.monitorType(MonitorType.FAN)
					.build();
			// Collect
			PresentParam present = PresentParam.missing();
			monitor.addParameter(present);

			final AssertedParameter<PresentParam> assertedPresentParameter = monitor.assertPresentParameter(PRESENT_ALARM_CONDITION);

			assertEquals(present, assertedPresentParameter.getParameter());
			assertTrue(assertedPresentParameter.isAbnormal());
		}

		{
			// Enclosure cannot be missing but let's check the condition on it to validate we don't break things...
			final Monitor monitor = Monitor.builder()
					.monitorType(MonitorType.ENCLOSURE)
					.build();

			final AssertedParameter<PresentParam> assertedPresentParameter = monitor.assertPresentParameter(PRESENT_ALARM_CONDITION);

			assertNull(assertedPresentParameter.getParameter());
			assertFalse(assertedPresentParameter.isAbnormal());
		}
	}

	@Test
	void testAssertStatusParameter() {
		{
			final Monitor monitor = Monitor.builder()
					.monitorType(MonitorType.FAN)
					.build();
			// Collect
			StatusParam status = StatusParam.builder()
					.name(STATUS_PARAMETER)
					.state(ParameterState.OK)
					.statusInformation("OK")
					.unit(STATUS_PARAMETER_UNIT)
					.collectTime(new Date().getTime())
					.build();
			monitor.addParameter(status);

			final AssertedParameter<StatusParam> assertedPresentParameter = monitor.assertStatusParameter(STATUS_PARAMETER, STATUS_ALARM_CONDITION);

			assertEquals(status, assertedPresentParameter.getParameter());
			assertFalse(assertedPresentParameter.isAbnormal());
		}

		{
			final Monitor monitor = Monitor.builder()
					.monitorType(MonitorType.FAN)
					.build();
			// Collect
			StatusParam status = StatusParam.builder()
					.name(STATUS_PARAMETER)
					.state(ParameterState.ALARM)
					.statusInformation("ALARM")
					.unit(STATUS_PARAMETER_UNIT)
					.collectTime(new Date().getTime())
					.build();
			monitor.addParameter(status);

			final AssertedParameter<StatusParam> assertedPresentParameter = monitor.assertStatusParameter(STATUS_PARAMETER, STATUS_ALARM_CONDITION);

			assertEquals(status, assertedPresentParameter.getParameter());
			assertTrue(assertedPresentParameter.isAbnormal());
		}

		{
			final Monitor monitor = Monitor.builder()
					.monitorType(MonitorType.FAN)
					.build();
			// Collect
			StatusParam status = StatusParam.builder()
					.name(STATUS_PARAMETER)
					.state(ParameterState.WARN)
					.statusInformation("WARN")
					.unit(STATUS_PARAMETER_UNIT)
					.collectTime(new Date().getTime())
					.build();
			monitor.addParameter(status);

			final AssertedParameter<StatusParam> assertedPresentParameter = monitor.assertStatusParameter(STATUS_PARAMETER, STATUS_ALARM_CONDITION);

			assertEquals(status, assertedPresentParameter.getParameter());
			assertFalse(assertedPresentParameter.isAbnormal());
		}

		{
			final Monitor monitor = Monitor.builder()
					.monitorType(MonitorType.FAN)
					.build();
			// Collect
			StatusParam status = StatusParam.builder()
					.name(STATUS_PARAMETER)
					.state(ParameterState.WARN)
					.statusInformation("WARN")
					.unit(STATUS_PARAMETER_UNIT)
					.collectTime(new Date().getTime())
					.build();
			monitor.addParameter(status);

			final AssertedParameter<StatusParam> assertedPresentParameter = monitor.assertStatusParameter(STATUS_PARAMETER, STATUS_WARN_CONDITION);

			assertEquals(status, assertedPresentParameter.getParameter());
			assertTrue(assertedPresentParameter.isAbnormal());
		}

		{
			final Monitor monitor = Monitor.builder()
					.monitorType(MonitorType.TEMPERATURE)
					.build();

			final AssertedParameter<StatusParam> assertedPresentParameter = monitor.assertStatusParameter(STATUS_PARAMETER, STATUS_ALARM_CONDITION);

			assertNull(assertedPresentParameter.getParameter());
			assertFalse(assertedPresentParameter.isAbnormal());
		}
	}

	@Test
	void testAssertNumberParameter() {
		{
			final Monitor monitor = Monitor.builder()
					.monitorType(MonitorType.FAN)
					.build();
			// Collect
			NumberParam speed = NumberParam.builder()
					.name(SPEED_PARAMETER)
					.value(3500D)
					.collectTime(new Date().getTime())
					.build();
			monitor.addParameter(speed);

			final AssertedParameter<NumberParam> assertedPresentParameter = monitor.assertNumberParameter(SPEED_PARAMETER, SPEED_WARN_CONDITION); // ]0 - 500]

			assertEquals(speed, assertedPresentParameter.getParameter());
			assertFalse(assertedPresentParameter.isAbnormal());
		}

		{
			final Monitor monitor = Monitor.builder()
					.monitorType(MonitorType.FAN)
					.build();
			// Collect
			NumberParam speed = NumberParam.builder()
					.name(SPEED_PARAMETER)
					.value(10D)
					.collectTime(new Date().getTime())
					.build();
			monitor.addParameter(speed);

			final AssertedParameter<NumberParam> assertedPresentParameter = monitor.assertNumberParameter(SPEED_PARAMETER, SPEED_WARN_CONDITION); // ]0 - 500]

			assertEquals(speed, assertedPresentParameter.getParameter());
			assertTrue(assertedPresentParameter.isAbnormal());
		}

		{
			final Monitor monitor = Monitor.builder()
					.monitorType(MonitorType.FAN)
					.build();
			// Collect
			NumberParam speed = NumberParam.builder()
					.name(SPEED_PARAMETER)
					.value(10D)
					.collectTime(new Date().getTime())
					.build();
			monitor.addParameter(speed);

			final AssertedParameter<NumberParam> assertedPresentParameter = monitor.assertNumberParameter(SPEED_PARAMETER, SPEED_ALARM_CONDITION); // [0]

			assertEquals(speed, assertedPresentParameter.getParameter());
			assertFalse(assertedPresentParameter.isAbnormal());
		}

		{
			final Monitor monitor = Monitor.builder()
					.monitorType(MonitorType.FAN)
					.build();
			// Collect
			NumberParam speed = NumberParam.builder()
					.name(SPEED_PARAMETER)
					.value(0D)
					.collectTime(new Date().getTime())
					.build();
			monitor.addParameter(speed);

			final AssertedParameter<NumberParam> assertedPresentParameter = monitor.assertNumberParameter(SPEED_PARAMETER, SPEED_ALARM_CONDITION); // [0]

			assertEquals(speed, assertedPresentParameter.getParameter());
			assertTrue(assertedPresentParameter.isAbnormal());
		}

		{
			final Monitor monitor = Monitor.builder()
					.monitorType(MonitorType.FAN)
					.build();

			final AssertedParameter<NumberParam> assertedPresentParameter = monitor.assertNumberParameter(SPEED_PARAMETER, SPEED_ALARM_CONDITION); // [0]

			assertNull(assertedPresentParameter.getParameter());
			assertFalse(assertedPresentParameter.isAbnormal());
		}
	}

	@Test
	void testCollectParameter() {
		final Monitor monitor = Monitor.builder()
				.monitorType(MonitorType.FAN)
				.build();

		// Discovery
		final List<AlertRule> alertRulesToAdd = MonitorType.FAN
				.getMetaMonitor()
				.getStaticAlertRules()
				.get(SPEED_PARAMETER)
				.stream().map(AlertRule::copy).collect(Collectors.toList());
		monitor.addAlertRules(SPEED_PARAMETER, alertRulesToAdd);

		// Collect
		NumberParam speed = NumberParam.builder()
				.name(SPEED_PARAMETER)
				.value(10D) // 10.0 RPM
				.collectTime(new Date().getTime())
				.build();
		monitor.collectParameter(speed);

		final AlertRule warnAlert = alertRulesToAdd.stream().filter(a -> a.getSeverity().equals(ParameterState.WARN)).findFirst().orElse(null);
		assertNotNull(warnAlert);
		assertTrue(warnAlert.isActive()); // YEAH :)

		final AlertRule alarmAlert = alertRulesToAdd.stream().filter(a -> a.getSeverity().equals(ParameterState.ALARM)).findFirst().orElse(null);
		assertNotNull(alarmAlert);
		assertFalse(alarmAlert.isActive()); // Not yet alarm, alarm is at 0 RPM
	}
}
