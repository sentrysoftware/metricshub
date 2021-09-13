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
import com.sentrysoftware.matrix.model.parameter.StatusParam;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION1;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION2;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION3;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ARRAY_NAME;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.AVAILABLE_PATH_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.AVAILABLE_PATH_INFORMATION_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DEVICE_ID;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.EXPECTED_PATH_COUNT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.LOCAL_DEVICE_NAME;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PATHS_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.REMOTE_DEVICE_NAME;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.WWN;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.AVAILABLE_PATH_COUNT_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.STATUS_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.STATUS_WARN_CONDITION;

public class Lun implements IMetaMonitor {


	private static final String RECOMMENDED_ACTION_FOR_BAD_LUN = 
			"Verify the status of the underlying HBA and its connectivity. Verify the reachability of the storage system and whether any configuration change has been made to the corresponding storage volume.";

	public static final MetaParameter AVAILABLE_PATH_COUNT = MetaParameter.builder()
			.basicCollect(true)
			.name(AVAILABLE_PATH_COUNT_PARAMETER)
			.unit(PATHS_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final MetaParameter AVAILABLE_PATH_INFORMATION = MetaParameter.builder()
			.basicCollect(true)
			.name(AVAILABLE_PATH_INFORMATION_PARAMETER)
			.type(ParameterType.TEXT)
			.build();

	private static final List<String> METADATA = List.of(DEVICE_ID, LOCAL_DEVICE_NAME, REMOTE_DEVICE_NAME, ARRAY_NAME, WWN,
			EXPECTED_PATH_COUNT, ADDITIONAL_INFORMATION1, ADDITIONAL_INFORMATION2, ADDITIONAL_INFORMATION3);

	public static final AlertRule STATUS_WARN_ALERT_RULE = new AlertRule(Lun::checkStatusWarnCondition,
			STATUS_WARN_CONDITION,
			ParameterState.WARN);
	public static final AlertRule STATUS_ALARM_ALERT_RULE = new AlertRule(Lun::checkStatusAlarmCondition,
			STATUS_ALARM_CONDITION,
			ParameterState.ALARM);
	public static final AlertRule AVAILABLE_PATH_COUNT_ALERT_RULE = new AlertRule(Lun::checkAvailablePathCountCondition, 
			AVAILABLE_PATH_COUNT_ALARM_CONDITION,
			ParameterState.ALARM);

	private static final Map<String, MetaParameter> META_PARAMETERS;
	private static final Map<String, List<AlertRule>> ALERT_RULES;

	static {
		final Map<String, MetaParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(STATUS_PARAMETER, STATUS);
		map.put(AVAILABLE_PATH_COUNT_PARAMETER, AVAILABLE_PATH_COUNT);
		map.put(AVAILABLE_PATH_INFORMATION_PARAMETER, AVAILABLE_PATH_INFORMATION);

		META_PARAMETERS = Collections.unmodifiableMap(map);

		final Map<String, List<AlertRule>> alertRulesMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		alertRulesMap.put(STATUS_PARAMETER, List.of(STATUS_WARN_ALERT_RULE, STATUS_ALARM_ALERT_RULE));
		alertRulesMap.put(AVAILABLE_PATH_COUNT_PARAMETER, Collections.singletonList(AVAILABLE_PATH_COUNT_ALERT_RULE));

		ALERT_RULES = Collections.unmodifiableMap(alertRulesMap);
	}

	/**
	 * Check condition when the monitor status is in WARN state.
	 * 
	 * @param monitor    The monitor we wish to check its status
	 * @param conditions The conditions used to determine the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkStatusWarnCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<StatusParam> assertedStatus = monitor.assertStatusParameter(STATUS_PARAMETER, conditions);
		if (assertedStatus.isAbnormal()) {

			return AlertDetails.builder()
					.problem("Although still working and available, this LUN is degraded." + IMetaMonitor.getStatusInformationMessage(assertedStatus.getParameter()))
					.consequence("The performance of the system may be affected and redundancy could be lost.")
					.recommendedAction(RECOMMENDED_ACTION_FOR_BAD_LUN)
					.build();
		}

		return null;
	}

	/**
	 * Check condition when the monitor status is in ALARM state.
	 * 
	 * @param monitor    The monitor we wish to check its status
	 * @param conditions The conditions used to determine the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkStatusAlarmCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<StatusParam> assertedStatus = monitor.assertStatusParameter(STATUS_PARAMETER, conditions);
		if (assertedStatus.isAbnormal()) {

			String problem = "This LUN is in critical state." +  IMetaMonitor.getStatusInformationMessage(assertedStatus.getParameter());
			return AlertDetails.builder()
					.problem(problem)
					.consequence("One or more filesystems are no longer available (possible data loss).")
					.recommendedAction(RECOMMENDED_ACTION_FOR_BAD_LUN)
					.build();
		}

		return null;
	}

	/**
	 * Check condition when the availablePathCount is abnormal.
	 * 
	 * @param monitor    The monitor we wish to check its availablePathCount
	 * @param conditions The conditions used to determine the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkAvailablePathCountCondition(Monitor monitor, Set<AlertCondition> conditions) {
		AssertedParameter<NumberParam> assertedParam = monitor.assertNumberParameter(AVAILABLE_PATH_COUNT_PARAMETER, conditions);
		if (assertedParam.isAbnormal()) {

			return AlertDetails.builder()
					.problem("The LUN cannot be accessed anymore because no path is available.")
					.consequence("One or more filesystems are no longer available (possible data loss).")
					.recommendedAction("Verify on the SAN switches which links are broken (link down, or zone exclusion, etc.). Check the mapping and masking configuration of the corresponding storage volume in the storage system.")
					.build();
		}

		return null;
	}

	/**
	 * Check condition when the availablePathCount is lower than previously detected.
	 * 
	 * @param monitor    The monitor we wish to check its availablePathCount
	 * @param conditions The conditions used to determine if the alarm is reached
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkLowerAvailablePathCountCondition(Monitor monitor, Set<AlertCondition> conditions) {
		AssertedParameter<NumberParam> assertedParam = monitor.assertNumberParameter(AVAILABLE_PATH_COUNT_PARAMETER, conditions);
		if (assertedParam.isAbnormal()) {

			return AlertDetails.builder()
					.problem(String.format("Although the LUN is still working and available, the number of paths to the LUN is lower than previously detected (%f).", assertedParam.getParameter().getValue()))
					.consequence("The performance of the system may be affected and redundancy could be lost.")
					.recommendedAction("Verify on the SAN switches which links are broken (link down, or zone exclusion, etc.). Check the mapping and masking configuration of the corresponding storage volume in the storage system.")
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
		return MonitorType.LUN;
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