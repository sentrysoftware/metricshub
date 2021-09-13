package com.sentrysoftware.matrix.common.meta.monitor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.sentrysoftware.matrix.common.meta.parameter.MetaParameter;
import com.sentrysoftware.matrix.common.meta.parameter.ParameterType;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.strategy.IMonitorVisitor;
import com.sentrysoftware.matrix.model.alert.AlertCondition;
import com.sentrysoftware.matrix.model.alert.AlertDetails;
import com.sentrysoftware.matrix.model.alert.AlertRule;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitor.Monitor.AssertedParameter;
import com.sentrysoftware.matrix.model.parameter.NumberParam;
import com.sentrysoftware.matrix.model.parameter.ParameterState;
import com.sentrysoftware.matrix.model.parameter.PresentParam;
import com.sentrysoftware.matrix.model.parameter.StatusParam;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION1;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION2;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION3;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DEVICE_ID;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_USAGE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_COUNT_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MODEL;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MOUNT_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MOUNT_COUNT_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.NEEDS_CLEANING_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.NEEDS_CLEANING_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_CONSUMPTION_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PRESENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SERIAL_NUMBER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.UNMOUNT_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.UNMOUNT_COUNT_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.VENDOR;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.ERROR_COUNT_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.PRESENT_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.STATUS_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.STATUS_WARN_CONDITION;

public class TapeDrive implements IMetaMonitor {

	static final String TAPE_LIBRARY_CONSEQUENCE = "The tape library may no longer be able to perform backups.";

	public static final MetaParameter MOUNT_COUNT = MetaParameter.builder()
			.basicCollect(false)
			.name(MOUNT_COUNT_PARAMETER)
			.unit(MOUNT_COUNT_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final MetaParameter NEEDS_CLEANING = MetaParameter.builder()
			.basicCollect(true)
			.name(NEEDS_CLEANING_PARAMETER)
			.unit(NEEDS_CLEANING_PARAMETER_UNIT)
			.type(ParameterType.STATUS)
			.build();

	public static final MetaParameter UNMOUNT_COUNT = MetaParameter.builder()
			.basicCollect(false)
			.name(UNMOUNT_COUNT_PARAMETER)
			.unit(UNMOUNT_COUNT_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();
	
	public static final MetaParameter ERROR_COUNT = MetaParameter.builder()
			.basicCollect(false)
			.name(ERROR_COUNT_PARAMETER)
			.unit(ERROR_COUNT_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	private static final List<String> METADATA = List.of(DEVICE_ID, SERIAL_NUMBER, VENDOR, MODEL, ADDITIONAL_INFORMATION1,
			ADDITIONAL_INFORMATION2, ADDITIONAL_INFORMATION3);

	public static final AlertRule PRESENT_ALERT_RULE = new AlertRule(TapeDrive::checkMissingCondition,
			PRESENT_ALARM_CONDITION,
			ParameterState.ALARM);
	public static final AlertRule STATUS_WARN_ALERT_RULE = new AlertRule(TapeDrive::checkStatusWarnCondition,
			STATUS_WARN_CONDITION,
			ParameterState.WARN);
	public static final AlertRule STATUS_ALARM_ALERT_RULE = new AlertRule(TapeDrive::checkStatusAlarmCondition,
			STATUS_ALARM_CONDITION,
			ParameterState.ALARM);
	public static final AlertRule ERROR_COUNT_ALERT_RULE = new AlertRule(TapeDrive::checkErrorCountCondition,
			ERROR_COUNT_ALARM_CONDITION,
			ParameterState.ALARM);
	public static final AlertRule NEEDS_CLEANING_WARN_ALERT_RULE = new AlertRule(TapeDrive::checkNeedsCleaningWarnCondition,
			STATUS_WARN_CONDITION,
			ParameterState.WARN);
	public static final AlertRule NEEDS_CLEANING_ALARM_ALERT_RULE = new AlertRule(TapeDrive::checkNeedsCleaningAlarmCondition,
			STATUS_ALARM_CONDITION,
			ParameterState.ALARM);

	private static final Map<String, MetaParameter> META_PARAMETERS;
	private static final Map<String, List<AlertRule>> ALERT_RULES;

	static {
		final Map<String, MetaParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(STATUS_PARAMETER, STATUS);
		map.put(PRESENT_PARAMETER, PRESENT);
		map.put(ERROR_COUNT_PARAMETER, ERROR_COUNT);
		map.put(MOUNT_COUNT_PARAMETER, MOUNT_COUNT);
		map.put(NEEDS_CLEANING_PARAMETER, NEEDS_CLEANING);
		map.put(UNMOUNT_COUNT_PARAMETER, UNMOUNT_COUNT);
		map.put(ENERGY_PARAMETER, ENERGY);
		map.put(ENERGY_USAGE_PARAMETER, ENERGY_USAGE);
		map.put(POWER_CONSUMPTION_PARAMETER, POWER_CONSUMPTION);

		META_PARAMETERS = Collections.unmodifiableMap(map);

		final Map<String, List<AlertRule>> alertRulesMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		alertRulesMap.put(PRESENT_PARAMETER, Collections.singletonList(PRESENT_ALERT_RULE));
		alertRulesMap.put(STATUS_PARAMETER, List.of(STATUS_WARN_ALERT_RULE, STATUS_ALARM_ALERT_RULE));
		alertRulesMap.put(ERROR_COUNT_PARAMETER, Collections.singletonList(ERROR_COUNT_ALERT_RULE));
		alertRulesMap.put(NEEDS_CLEANING_PARAMETER,  List.of(NEEDS_CLEANING_WARN_ALERT_RULE, NEEDS_CLEANING_ALARM_ALERT_RULE));

		ALERT_RULES = Collections.unmodifiableMap(alertRulesMap);
	}

	/**
	 * Check missing TapeDrive condition.
	 * 
	 * @param monitor    The monitor we wish to check
	 * @param conditions The conditions used to determine the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkMissingCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<PresentParam> assertedPresent = monitor.assertPresentParameter(conditions);
		if (assertedPresent.isAbnormal()) {

			return AlertDetails.builder()
					.problem("This tape drive is no longer detected.")
					.consequence(TAPE_LIBRARY_CONSEQUENCE)
					.recommendedAction("Check that the tape drive is still present and online.")
					.build();
		}

		return null;
	}

	/**
	 * Check condition when the monitor status is in WARN state.
	 * 
	 * @param monitor    The monitor we wish to check its status
	 * @param conditions The conditions used to detect the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkStatusWarnCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<StatusParam> assertedStatus = monitor.assertStatusParameter(STATUS_PARAMETER, conditions);
		if (assertedStatus.isAbnormal()) {

			String problem = "This tape drive may be running slowly, dirty or has encountered too many errors."
					+ IMetaMonitor.getStatusInformationMessage(assertedStatus.getParameter());
			return AlertDetails.builder()
					.problem(problem)
					.consequence(TAPE_LIBRARY_CONSEQUENCE)
					.recommendedAction("Check the faulty tape drive for any visible problem and replace it if necessary.")
					.build();

		}
		return null;
	}

	/**
	 * Check condition when the monitor status is in ALARM state.
	 * 
	 * @param monitor    The monitor we wish to check its status
	 * @param conditions The conditions used to detect the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkStatusAlarmCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<StatusParam> assertedStatus = monitor.assertStatusParameter(STATUS_PARAMETER, conditions);
		if (assertedStatus.isAbnormal()) {

			String problem = "This tape drive may be jammed, dirty, mechanically failed or broken." +  IMetaMonitor.getStatusInformationMessage(assertedStatus.getParameter());
			return AlertDetails.builder()
					.problem(problem)
					.consequence(TAPE_LIBRARY_CONSEQUENCE)
					.recommendedAction("Quickly replace the faulty tape drive.")
					.build();

		}
		return null;
	}

	/**
	 * Check condition when the monitor needs cleaning status is in WARN state.
	 * 
	 * @param monitor    The monitor we wish to check its needs cleaning status
	 * @param conditions The conditions used to detect the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkNeedsCleaningWarnCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<StatusParam> assertedStatus = monitor.assertStatusParameter(NEEDS_CLEANING_PARAMETER, conditions);
		if (assertedStatus.isAbnormal()) {

			return AlertDetails.builder()
					.problem("This tape drives needs to be cleaned. Either the tape drive exceeds internal preset error thresholds in the drive or the tape drive exceeds the maximum recommended time between cleaning.")
					.consequence("Regular tape drive cleaning helps in long-term reliability, prevents read/write errors and should be conducted on a scheduled cycle as well as when requested by the drive.")
					.recommendedAction("Wait for any running operation to finish, eject the tape and clean the drive.")
					.build();

		}
		return null;
	}

	/**
	 * Check condition when the monitor needs cleaning status is in ALARM state.
	 * 
	 * @param monitor    The monitor we wish to check its needs cleaning status
	 * @param conditions The conditions used to detect the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkNeedsCleaningAlarmCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<StatusParam> assertedStatus = monitor.assertStatusParameter(NEEDS_CLEANING_PARAMETER, conditions);
		if (assertedStatus.isAbnormal()) {

				return AlertDetails.builder()
					.problem("This tape drives requires to be cleaned immediately. Either the tape drive exceeds internal preset error thresholds in the drive or the tape drive exceeds the maximum recommended time between cleaning.")
					.consequence("Regular tape drive cleaning helps in long-term reliability, prevents read/write errors and should be conducted on a scheduled cycle as well as when requested by the drive.")
					.recommendedAction("Wait for any running operation to finish, eject the tape and clean the drive.")
					.build();

		}
		return null;
	}

	/**
	 * Check condition when the monitor error count parameter is abnormal.
	 * 
	 * @param monitor    The monitor we wish to check its error count
	 * @param conditions The condition used to check the error count parameter value
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkErrorCountCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<NumberParam> assertedErrorCount = monitor.assertNumberParameter(ERROR_COUNT_PARAMETER, conditions);
		if (assertedErrorCount.isAbnormal()) {

			String serialNumber = monitor.getMetadata(SERIAL_NUMBER);
			return AlertDetails.builder()
					.problem(String.format("The tape drive encountered errors (%f).", assertedErrorCount.getParameter().getValue()))
					.consequence("The tape drive may not be able to read or write data to the tape.")
					.recommendedAction("Check what is causing these errors on the tape drive, whether it is caused by the tape itself, the drive needing to be cleaned, or a data transport error."
							+ ((serialNumber != null) ? String.format(" Please note this tape drive's serial number: %s.", serialNumber) : ""))
					.build();
		}

		return null;
	}

	/**
	 * Check condition when the monitor error count parameter too high.
	 * 
	 * @param monitor    The monitor we wish to check its error count
	 * @param conditions The condition used to check the error count parameter value
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkHighErrorCountCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<NumberParam> assertedErrorCount = monitor.assertNumberParameter(ERROR_COUNT_PARAMETER, conditions);
		if (assertedErrorCount.isAbnormal()) {

			String serialNumber = monitor.getMetadata(SERIAL_NUMBER);
			return AlertDetails.builder()
					.problem(String.format("The tape drive encountered too many errors (%f).", assertedErrorCount.getParameter().getValue()))
					.consequence("The tape drive may not be able to read or write data to the tape.")
					.recommendedAction("Check what is causing these errors on the tape drive, whether it is caused by the tape itself, the drive needing to be cleaned, or a data transport error."
							+ ((serialNumber != null) ? String.format("Please note this tape drive's serial number: %s.", serialNumber) : ""))
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
		return MonitorType.TAPE_DRIVE;
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