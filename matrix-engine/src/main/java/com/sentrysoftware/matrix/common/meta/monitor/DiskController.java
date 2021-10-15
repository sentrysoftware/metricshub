package com.sentrysoftware.matrix.common.meta.monitor;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.BATTERY_STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.BIOS_VERSION;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.CONTROLLER_STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DEVICE_ID;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DRIVER_VERSION;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_USAGE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.FIRMWARE_VERSION;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.IDENTIFYING_INFORMATION;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MODEL;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_CONSUMPTION_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PRESENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SERIAL_NUMBER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STATUS_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.VENDOR;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.PRESENT_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.STATUS_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.STATUS_WARN_CONDITION;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.sentrysoftware.matrix.common.meta.parameter.DiscreteParamType;
import com.sentrysoftware.matrix.common.meta.parameter.MetaParameter;
import com.sentrysoftware.matrix.common.meta.parameter.state.Status;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.strategy.IMonitorVisitor;
import com.sentrysoftware.matrix.model.alert.AlertCondition;
import com.sentrysoftware.matrix.model.alert.AlertDetails;
import com.sentrysoftware.matrix.model.alert.AlertRule;
import com.sentrysoftware.matrix.model.alert.Severity;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitor.Monitor.AssertedParameter;
import com.sentrysoftware.matrix.model.parameter.DiscreteParam;

public class DiskController implements IMetaMonitor {

	public static final MetaParameter BATTERY_STATUS = MetaParameter.builder()
			.basicCollect(true)
			.name(BATTERY_STATUS_PARAMETER)
			.unit(STATUS_PARAMETER_UNIT)
			.type(new DiscreteParamType(Status::interpret))
			.build();

	public static final MetaParameter CONTROLLER_STATUS = MetaParameter.builder()
			.basicCollect(true)
			.name(CONTROLLER_STATUS_PARAMETER)
			.unit(STATUS_PARAMETER_UNIT)
			.type(new DiscreteParamType(Status::interpret))
			.build();

	private static final List<String> METADATA = List.of(DEVICE_ID, SERIAL_NUMBER, VENDOR, MODEL, BIOS_VERSION,
			FIRMWARE_VERSION, DRIVER_VERSION, IDENTIFYING_INFORMATION);

	public static final AlertRule PRESENT_ALERT_RULE = new AlertRule(DiskController::checkMissingCondition,
			PRESENT_ALARM_CONDITION,
			Severity.ALARM);
	public static final AlertRule BATTERY_STATUS_WARN_ALERT_RULE = new AlertRule(DiskController:: checkBatteryStatusWarnCondition,
			STATUS_WARN_CONDITION,
			Severity.WARN);
	public static final AlertRule BATTERY_STATUS_ALARM_ALERT_RULE = new AlertRule(DiskController::checkBatteryStatusAlarmCondition,
			STATUS_ALARM_CONDITION,
			Severity.ALARM);
	public static final AlertRule CONTROLLER_STATUS_WARN_ALERT_RULE = new AlertRule(DiskController::checkControllerStatusWarnCondition,
			STATUS_WARN_CONDITION,
			Severity.WARN);
	public static final AlertRule CONTROLLER_STATUS_ALARM_ALERT_RULE = new AlertRule(DiskController::checkControllerStatusAlarmCondition,
			STATUS_ALARM_CONDITION,
			Severity.ALARM);

	private static final Map<String, MetaParameter> META_PARAMETERS;
	private static final Map<String, List<AlertRule>> ALERT_RULES;

	static {
		final Map<String, MetaParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(STATUS_PARAMETER, STATUS);
		map.put(PRESENT_PARAMETER, PRESENT);
		map.put(BATTERY_STATUS_PARAMETER, BATTERY_STATUS);
		map.put(CONTROLLER_STATUS_PARAMETER, CONTROLLER_STATUS);
		map.put(ENERGY_PARAMETER, ENERGY);
		map.put(ENERGY_USAGE_PARAMETER, ENERGY_USAGE);
		map.put(POWER_CONSUMPTION_PARAMETER, POWER_CONSUMPTION);

		META_PARAMETERS = Collections.unmodifiableMap(map);

		final Map<String, List<AlertRule>> alertRulesMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		alertRulesMap.put(PRESENT_PARAMETER, Collections.singletonList(PRESENT_ALERT_RULE));
		alertRulesMap.put(BATTERY_STATUS_PARAMETER, List.of(BATTERY_STATUS_WARN_ALERT_RULE, BATTERY_STATUS_ALARM_ALERT_RULE));
		alertRulesMap.put(CONTROLLER_STATUS_PARAMETER, List.of(CONTROLLER_STATUS_WARN_ALERT_RULE, CONTROLLER_STATUS_ALARM_ALERT_RULE));

		ALERT_RULES = Collections.unmodifiableMap(alertRulesMap);

	}

	/**
	 * Check missing disk controller condition.
	 * 
	 * @param monitor    The monitor we wish to check
	 * @param conditions The conditions used to determine the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkMissingCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<DiscreteParam> assertedPresent = monitor.assertPresentParameter(conditions);
		if (assertedPresent.isAbnormal()) {

			return AlertDetails.builder()
					.problem("This disk controller is not detected anymore.")
					.consequence("The disks attached to this controller are no longer available to the operating system. This will certainly cause serious problems to applications that were using these disks.")
					.recommendedAction("Check whether the controller was intentionally removed from the system or whether it has been automatically disabled by the BIOS of the computer.")
					.build();

		}

		return null;
	}

	/**
	 * Check condition when the monitor battery status is in WARN state.
	 * 
	 * @param monitor    The monitor we wish to check
	 * @param conditions The conditions used to determine the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkBatteryStatusWarnCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<DiscreteParam> assertedStatus = monitor.assertStatusParameter(BATTERY_STATUS_PARAMETER, conditions);
		if (assertedStatus.isAbnormal()) {

			return AlertDetails.builder()
					.problem("The battery of this disk controller is degraded." +  IMetaMonitor.getStatusInformationMessage(monitor))
					.consequence("A degraded battery in a disk controller with write cache enabled means that a power outage will certainly lead to data loss and/or corruption.")
					.recommendedAction("Replace the battery of this disk controller to avoid potential data loss and/or corruption.")
					.build();

		}
		return null;
	}

	/**
	 * Check condition when the monitor battery status is in ALARM state.
	 * 
	 * @param monitor    The monitor we wish to check
	 * @param conditions The conditions used to determine the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkBatteryStatusAlarmCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<DiscreteParam> assertedStatus = monitor.assertStatusParameter(BATTERY_STATUS_PARAMETER, conditions);
		if (assertedStatus.isAbnormal()) {

			return AlertDetails.builder()
					.problem("The battery of this disk controller has failed." +  IMetaMonitor.getStatusInformationMessage(monitor))
					.consequence("A failed battery in a disk controller with write cache enabled, means that a power outage will certainly lead to data loss and/or corruption.")
					.recommendedAction("Replace the battery of this disk controller to avoid potential data loss and/or corruption.")
					.build();

		}
		return null;
	}

	/**
	 * Check condition when the monitor controller status is in WARN state.
	 * 
	 * @param monitor    The monitor we wish to check
	 * @param conditions The conditions used to determine the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkControllerStatusWarnCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<DiscreteParam> assertedStatus = monitor.assertStatusParameter(CONTROLLER_STATUS_PARAMETER, conditions);
		if (assertedStatus.isAbnormal()) {

			String problem = "This disk controller is degraded or about to fail." +  IMetaMonitor.getStatusInformationMessage(monitor);
			return AlertDetails.builder()
					.problem(problem)
					.consequence("A degraded disk controller could have a serious performance impact on its attached disks. A controller about to fail means that its attached disks will no longer be available to the operating system. This may lead to data loss and/or corruption.")
					.recommendedAction("Replace this disk controller as soon as possible to avoid performance issues and possible data loss and corruption.")
					.build();

		}
		return null;
	}

	/**
	 * Check condition when the monitor controller status is in ALARM state.
	 * 
	 * @param monitor    The monitor we wish to check
	 * @param conditions The conditions used to determine the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkControllerStatusAlarmCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<DiscreteParam> assertedStatus = monitor.assertStatusParameter(CONTROLLER_STATUS_PARAMETER, conditions);
		if (assertedStatus.isAbnormal()) {

			return AlertDetails.builder()
					.problem("This disk controller has failed." +  IMetaMonitor.getStatusInformationMessage(monitor))
					.consequence("This disk controller is no longer working. Its attached disks are probably no longer available to the operating system which means data loss or even corruption.")
					.recommendedAction("Replace this disk controller immediately to have a chance to recover the data on the attached disks.")
					.build();

		}
		return null;
	}

	@Override
	public void accept(IMonitorVisitor monitorVisitor) {
		monitorVisitor.visit(this);
	}

	@Override
	public Map<String, MetaParameter> getMetaParameters() {
		return META_PARAMETERS;
	}

	@Override
	public MonitorType getMonitorType() {
		return MonitorType.DISK_CONTROLLER;
	}

	@Override
	public List<String> getMetadata() {
		return METADATA;
	}

	@Override
	public Map<String, List<AlertRule>> getStaticAlertRules() {
		return ALERT_RULES;
	}
}