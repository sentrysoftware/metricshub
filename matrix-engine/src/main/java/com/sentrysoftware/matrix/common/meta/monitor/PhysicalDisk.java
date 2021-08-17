package com.sentrysoftware.matrix.common.meta.monitor;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION1;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION2;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION3;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DEVICE_ID;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.FIRMWARE_VERSION;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MODEL;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SERIAL_NUMBER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SIZE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.VENDOR;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.ENDURANCE_REMAINING_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.ENDURANCE_REMAINING_WARN_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.ERROR_COUNT_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.PRESENT_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.STATUS_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.STATUS_WARN_CONDITION;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
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

public class PhysicalDisk implements IMetaMonitor {

	public static final MetaParameter INTRUSION_STATUS = MetaParameter.builder()
			.basicCollect(true)
			.name(HardwareConstants.INTRUSION_STATUS_PARAMETER)
			.unit(HardwareConstants.INTRUSION_STATUS_PARAMETER_UNIT)
			.type(ParameterType.STATUS)
			.build();

	public static final MetaParameter DEVICE_NOT_READY_ERROR_COUNT = MetaParameter.builder()
			.basicCollect(true)
			.name(HardwareConstants.DEVICE_NOT_READY_ERROR_COUNT_PARAMETER)
			.unit(HardwareConstants.ERROR_COUNT_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final MetaParameter ENDURANCE_REMAINING = MetaParameter.builder()
			.basicCollect(true)
			.name(HardwareConstants.ENDURANCE_REMAINING_PARAMETER)
			.unit(HardwareConstants.PERCENT_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final MetaParameter HARD_ERROR_COUNT= MetaParameter.builder()
			.basicCollect(true)
			.name(HardwareConstants.HARD_ERROR_COUNT_PARAMETER)
			.unit(HardwareConstants.ERROR_COUNT_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final MetaParameter ILLEGAL_REQUEST_ERROR_COUNT= MetaParameter.builder()
			.basicCollect(true)
			.name(HardwareConstants.ILLEGAL_REQUEST_ERROR_COUNT_PARAMETER)
			.unit(HardwareConstants.ERROR_COUNT_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final MetaParameter MEDIA_ERROR_COUNT= MetaParameter.builder()
			.basicCollect(true)
			.name(HardwareConstants.MEDIA_ERROR_COUNT_PARAMETER)
			.unit(HardwareConstants.ERROR_COUNT_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final MetaParameter NO_DEVICE_ERROR_COUNT = MetaParameter.builder()
			.basicCollect(true)
			.name(HardwareConstants.NO_DEVICE_ERROR_COUNT_PARAMETER)
			.unit(HardwareConstants.ERROR_COUNT_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final MetaParameter RECOVERABLE_ERROR_COUNT = MetaParameter.builder()
			.basicCollect(true)
			.name(HardwareConstants.RECOVERABLE_ERROR_COUNT_PARAMETER)
			.unit(HardwareConstants.ERROR_COUNT_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final MetaParameter TRANSPORT_ERROR_COUNT = MetaParameter.builder()
			.basicCollect(true)
			.name(HardwareConstants.TRANSPORT_ERROR_COUNT_PARAMETER)
			.unit(HardwareConstants.ERROR_COUNT_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	private static final List<String> METADATA = List.of(DEVICE_ID, SERIAL_NUMBER, VENDOR, MODEL, FIRMWARE_VERSION, SIZE,
			ADDITIONAL_INFORMATION1, ADDITIONAL_INFORMATION2, ADDITIONAL_INFORMATION3);

	public static final AlertRule PRESENT_ALERT_RULE = new AlertRule(PhysicalDisk::checkMissingCondition,
			PRESENT_ALARM_CONDITION,
			ParameterState.ALARM);
	public static final AlertRule STATUS_WARN_ALERT_RULE = new AlertRule(PhysicalDisk::checkStatusWarnCondition,
			STATUS_WARN_CONDITION,
			ParameterState.WARN);
	public static final AlertRule STATUS_ALARM_ALERT_RULE = new AlertRule(PhysicalDisk::checkStatusAlarmCondition,
			STATUS_ALARM_CONDITION,
			ParameterState.ALARM);
	public static final AlertRule ERROR_COUNT_ALERT_RULE = new AlertRule((monitor, conditions) -> 
			checkErrorCountCondition(monitor, HardwareConstants.ERROR_COUNT_PARAMETER, conditions),
			ERROR_COUNT_ALARM_CONDITION,
			ParameterState.ALARM);
	public static final AlertRule TRANSPORT_ERROR_COUNT_ALERT_RULE = new AlertRule((monitor, conditions) -> 
			checkErrorCountCondition(monitor, HardwareConstants.TRANSPORT_ERROR_COUNT_PARAMETER, conditions),
			ERROR_COUNT_ALARM_CONDITION,
			ParameterState.ALARM);

	public static final AlertRule HARD_ERROR_COUNT_ALERT_RULE = new AlertRule((monitor, conditions) -> 
			checkErrorCountCondition(monitor, HardwareConstants.HARD_ERROR_COUNT_PARAMETER, conditions),
			ERROR_COUNT_ALARM_CONDITION,
			ParameterState.ALARM);
	public static final AlertRule NO_DEVICE_ERROR_COUNT_ALERT_RULE = new AlertRule((monitor, conditions) -> 
			checkErrorCountCondition(monitor, HardwareConstants.NO_DEVICE_ERROR_COUNT_PARAMETER, conditions),
			ERROR_COUNT_ALARM_CONDITION,
			ParameterState.ALARM);
	public static final AlertRule MEDIA_ERROR_COUNT_ALERT_RULE = new AlertRule((monitor, conditions) -> 
			checkErrorCountCondition(monitor, HardwareConstants.MEDIA_ERROR_COUNT_PARAMETER, conditions),
			ERROR_COUNT_ALARM_CONDITION,
			ParameterState.ALARM);
	public static final AlertRule DEVICE_NOT_READY_ERROR_COUNT_ALERT_RULE = new AlertRule((monitor, conditions) -> 
			checkErrorCountCondition(monitor, HardwareConstants.DEVICE_NOT_READY_ERROR_COUNT_PARAMETER, conditions),
			ERROR_COUNT_ALARM_CONDITION,
			ParameterState.ALARM);
	public static final AlertRule PREDICTED_FAILURE_ALERT_RULE = new AlertRule(PhysicalDisk::checkPredictedFailureCondition,
			STATUS_WARN_CONDITION,
			ParameterState.WARN);
	public static final AlertRule ENDURANCE_REMAINING_WARN_ALERT_RULE = new AlertRule(PhysicalDisk::checkEnduranceRemainingWarnCondition, 
			ENDURANCE_REMAINING_WARN_CONDITION,
			ParameterState.WARN);
	public static final AlertRule ENDURANCE_REMAINING_ALARM_ALERT_RULE = new AlertRule(PhysicalDisk::checkEnduranceRemainingAlarmCondition, 
			ENDURANCE_REMAINING_ALARM_CONDITION,
			ParameterState.ALARM);

	private static final Map<String, MetaParameter> META_PARAMETERS;
	private static final Map<String, List<AlertRule>> ALERT_RULES;

	static {
		final Map<String, MetaParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(HardwareConstants.STATUS_PARAMETER, STATUS);
		map.put(HardwareConstants.PRESENT_PARAMETER, PRESENT);
		map.put(HardwareConstants.INTRUSION_STATUS_PARAMETER, INTRUSION_STATUS);
		map.put(HardwareConstants.DEVICE_NOT_READY_ERROR_COUNT_PARAMETER, DEVICE_NOT_READY_ERROR_COUNT);
		map.put(HardwareConstants.ENDURANCE_REMAINING_PARAMETER, ENDURANCE_REMAINING);
		map.put(HardwareConstants.ERROR_COUNT_PARAMETER, ERROR_COUNT);
		map.put(HardwareConstants.HARD_ERROR_COUNT_PARAMETER, HARD_ERROR_COUNT);
		map.put(HardwareConstants.ILLEGAL_REQUEST_ERROR_COUNT_PARAMETER, ILLEGAL_REQUEST_ERROR_COUNT);
		map.put(HardwareConstants.MEDIA_ERROR_COUNT_PARAMETER, MEDIA_ERROR_COUNT);
		map.put(HardwareConstants.NO_DEVICE_ERROR_COUNT_PARAMETER, NO_DEVICE_ERROR_COUNT);
		map.put(HardwareConstants.PREDICTED_FAILURE_PARAMETER, PREDICTED_FAILURE);
		map.put(HardwareConstants.RECOVERABLE_ERROR_COUNT_PARAMETER, RECOVERABLE_ERROR_COUNT);
		map.put(HardwareConstants.TRANSPORT_ERROR_COUNT_PARAMETER, TRANSPORT_ERROR_COUNT);

		META_PARAMETERS = Collections.unmodifiableMap(map);

		final Map<String, List<AlertRule>> alertRulesMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		alertRulesMap.put(HardwareConstants.PRESENT_PARAMETER, Collections.singletonList(PRESENT_ALERT_RULE));
		alertRulesMap.put(HardwareConstants.STATUS_PARAMETER, List.of(STATUS_WARN_ALERT_RULE, STATUS_ALARM_ALERT_RULE));
		alertRulesMap.put(HardwareConstants.ERROR_COUNT_PARAMETER, Collections.singletonList(ERROR_COUNT_ALERT_RULE));
		alertRulesMap.put(HardwareConstants.TRANSPORT_ERROR_COUNT_PARAMETER, Collections.singletonList(TRANSPORT_ERROR_COUNT_ALERT_RULE));
		alertRulesMap.put(HardwareConstants.HARD_ERROR_COUNT_PARAMETER, Collections.singletonList(HARD_ERROR_COUNT_ALERT_RULE));
		alertRulesMap.put(HardwareConstants.NO_DEVICE_ERROR_COUNT_PARAMETER, Collections.singletonList(NO_DEVICE_ERROR_COUNT_ALERT_RULE));
		alertRulesMap.put(HardwareConstants.MEDIA_ERROR_COUNT_PARAMETER, Collections.singletonList(MEDIA_ERROR_COUNT_ALERT_RULE));
		alertRulesMap.put(HardwareConstants.DEVICE_NOT_READY_ERROR_COUNT_PARAMETER, Collections.singletonList(DEVICE_NOT_READY_ERROR_COUNT_ALERT_RULE));
		alertRulesMap.put(HardwareConstants.PREDICTED_FAILURE_PARAMETER, Collections.singletonList(PREDICTED_FAILURE_ALERT_RULE));
		alertRulesMap.put(HardwareConstants.ENDURANCE_REMAINING_PARAMETER, List.of(ENDURANCE_REMAINING_WARN_ALERT_RULE, ENDURANCE_REMAINING_ALARM_ALERT_RULE));

		ALERT_RULES = Collections.unmodifiableMap(alertRulesMap);

	}

	/**
	 * Missing PhysicalDisk condition
	 * 
	 * @param monitor    The monitor we wish to check
	 * @param conditions The conditions used to determine the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkMissingCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<PresentParam> assertedPresent = monitor.assertPresentParameter(conditions);
		if (assertedPresent.isAbnormal()) {

			return AlertDetails.builder()
					.problem("This physical disk is not detected anymore.")
					.consequence("If part of a RAID subsystem, a missing disk will affect the overall performance, but filesystems should still be up and running. If not part of a RAID, the filesystems of this disk will no longer be available (data loss).")
					.recommendedAction("Check if the physical disk is really missing. The non-detection may be due to a dead disk or an unplugged cable.")
					.build();
		}

		return null;
	}

	/**
	 * Condition when the monitor status is in WARN state
	 * 
	 * @param monitor    The monitor we wish to check its status
	 * @param conditions The conditions used to detect the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkStatusWarnCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<StatusParam> assertedStatus = monitor.assertStatusParameter(HardwareConstants.STATUS_PARAMETER, conditions);
		if (assertedStatus.isAbnormal()) {

			final String serialNumber = monitor.getMetadata(HardwareConstants.SERIAL_NUMBER);
			return AlertDetails.builder()
					.problem("Although still working and available, this physical disk is degraded or about to fail."
							+ IMetaMonitor.getStatusInformationMessage(assertedStatus.getParameter()))
					.consequence("If degraded, the performance of the system may be affected. If about to fail, the disk may crash soon, possibly causing a loss of data.")
					.recommendedAction(buildRecommendedActionString("You may need to replace this physical disk.", serialNumber))
					.build();
		}

		return null;
	}

	/**
	 * Condition when the monitor status is in ALARM state
	 * 
	 * @param monitor    The monitor we wish to check its status
	 * @param conditions The conditions used to detect the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkStatusAlarmCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<StatusParam> assertedStatus = monitor.assertStatusParameter(HardwareConstants.STATUS_PARAMETER, conditions);
		if (assertedStatus.isAbnormal()) {

			String serialNumber = monitor.getMetadata(HardwareConstants.SERIAL_NUMBER);
			return AlertDetails.builder()
					.problem("This physical disk is in critical/unrecoverable state." + IMetaMonitor.getStatusInformationMessage(assertedStatus.getParameter()))
					.consequence("If part of a RAID subsystem, a missing disk affects the performance but filesystems should still be up and running. Otherwise, the filesystems of this disk are no longer available (data loss).")
					.recommendedAction(buildRecommendedActionString("Replace this physical disk as soon as possible.", serialNumber))
					.build();
		}

		return null;
	}

	/**
	 * Condition to be used to detect too many errors
	 * 
	 * @param monitor              The monitor we wish to check its error count
	 * @param errorCountParamName  The name of the error count parameter
	 * @param conditions           The conditions used to check the error count parameter value
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkHighErrorCountCondition(Monitor monitor, String errorCountParamName, Set<AlertCondition> conditions) {
		final AssertedParameter<NumberParam> assertedErrorCount = monitor.assertNumberParameter(errorCountParamName, conditions);
		if (assertedErrorCount.isAbnormal()) {

			String serialNumber = monitor.getMetadata(HardwareConstants.SERIAL_NUMBER);
			return AlertDetails.builder()
					.problem(String.format("The physical disk encountered too many errors (%f).", assertedErrorCount.getParameter().getValue()))
					.consequence("The integrity of the data stored on this physical disk may be in jeopardy.")
					.recommendedAction(buildRecommendedActionString("Replace this physical disk as soon as possible to avoid data corruption.", serialNumber))
					.build();
		}

		return null;
	}

	/**
	 * Condition when the monitor error count is abnormal
	 * 
	 * @param monitor              The monitor we wish to check its error count
	 * @param errorCountParamName  The name of the error count parameter
	 * @param conditions           The condition used to check the error count parameter value
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkErrorCountCondition(Monitor monitor, String errorCountParamName, Set<AlertCondition> conditions) {
		final AssertedParameter<NumberParam> assertedErrorCount = monitor.assertNumberParameter(errorCountParamName, conditions);
		if (assertedErrorCount.isAbnormal()) {

			String serialNumber = monitor.getMetadata(HardwareConstants.SERIAL_NUMBER);
			return AlertDetails.builder()
					.problem(String.format("The physical disk encountered errors (%f).", assertedErrorCount.getParameter().getValue()))
					.consequence("The integrity of the data stored on this physical disk is not assured.")
					.recommendedAction(buildRecommendedActionString("Replace this physical disk as soon as possible to avoid data corruption.", serialNumber))
					.build();
		}

		return null;
	}

	/**
	 * Condition when the Disk predicted failure is in WARN state
	 * 
	 * @param monitor    The Disk we wish to check its predicted failure
	 * @param conditions The conditions used to detect the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkPredictedFailureCondition(Monitor monitor, Set<AlertCondition> conditions) {
		AssertedParameter<StatusParam> assertedPredictedFailure = monitor.assertStatusParameter(HardwareConstants.PREDICTED_FAILURE_PARAMETER, conditions);
		if (assertedPredictedFailure.isAbnormal()) {

			String serialNumber = monitor.getMetadata(HardwareConstants.SERIAL_NUMBER);
			return AlertDetails.builder()
					.problem("An imminent failure is predicted on this physical disk (SMART report).")
					.consequence("A disk crash or data corruption is very likely to occur soon.")
					.recommendedAction(
							buildRecommendedActionString("Replace this physical disk as soon as possible to prevent a disk crash or data corruption.",
									serialNumber))
					.build();

		}

		return null;
	}

	/**
	 * Condition when the Disk endurance remaining is abnormal (WARN)
	 * 
	 * @param monitor   The monitor we wish to check its endurance remaining parameter
	 * @param conditions The conditions used to check the endurance remaining parameter value
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkEnduranceRemainingWarnCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<NumberParam> assertedEnduranceRemaining = monitor.assertNumberParameter(HardwareConstants.ENDURANCE_REMAINING_PARAMETER, conditions);
		if (assertedEnduranceRemaining.isAbnormal()) {

			return AlertDetails.builder()
					.problem("The physical disk is reaching its maximum usage rating and is to be considered worn out.")
					.consequence("The disk will soon stop responding and may become incapable of writing data.")
					.recommendedAction("Replace the physical disk as soon as possible.")
					.build();
		}

		return null;
	}

	/**
	 * Condition when the Disk endurance remaining is abnormal (ALARM)
	 * 
	 * @param monitor    The monitor we wish to check its endurance remaining parameter
	 * @param conditions The conditions used to check the endurance remaining parameter value
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkEnduranceRemainingAlarmCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<NumberParam> assertedEnduranceRemaining = monitor.assertNumberParameter(HardwareConstants.ENDURANCE_REMAINING_PARAMETER, conditions);
		if (assertedEnduranceRemaining.isAbnormal()) {

			return AlertDetails.builder()
					.problem("The physical disk is nearing wearing out and getting to its end of life.")
					.consequence("As the disk reaches its maximum usage limit, it may become incapable of writing data.")
					.recommendedAction("Consider replacing the physical disk.")
					.build();
		}

		return null;
	}

	/**
	 * Concatenate the given action to the physical disk serial number text information
	 * 
	 * @param action       The action text our sentence starts with
	 * @param serialNumber The serial number to include in the final string result
	 * @return String result
	 */
	private static String buildRecommendedActionString(final String action, final String serialNumber) {
		return action + ((serialNumber != null) ? String.format(" Please note this physical disk's serial number: %s.", serialNumber) : "");
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
		return MonitorType.PHYSICAL_DISK;
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