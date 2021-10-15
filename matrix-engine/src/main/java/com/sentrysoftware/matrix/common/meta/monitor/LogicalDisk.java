package com.sentrysoftware.matrix.common.meta.monitor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.sentrysoftware.matrix.common.meta.parameter.MetaParameter;
import com.sentrysoftware.matrix.common.meta.parameter.SimpleParamType;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.strategy.IMonitorVisitor;
import com.sentrysoftware.matrix.model.alert.AlertCondition;
import com.sentrysoftware.matrix.model.alert.AlertDetails;
import com.sentrysoftware.matrix.model.alert.AlertRule;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitor.Monitor.AssertedParameter;
import com.sentrysoftware.matrix.model.parameter.DiscreteParam;
import com.sentrysoftware.matrix.model.parameter.NumberParam;
import com.sentrysoftware.matrix.model.alert.Severity;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.IDENTIFYING_INFORMATION;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DEVICE_ID;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_COUNT_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.LAST_ERROR_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.RAID_LEVEL;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SIZE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SPACE_GB_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.UNALLOCATED_SPACE_PARAMETER;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.ERROR_COUNT_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.STATUS_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.STATUS_WARN_CONDITION;

public class LogicalDisk implements IMetaMonitor {

	public static final MetaParameter UNALLOCATED_SPACE = MetaParameter.builder()
			.basicCollect(false)
			.name(UNALLOCATED_SPACE_PARAMETER)
			.unit(SPACE_GB_PARAMETER_UNIT)
			.type(SimpleParamType.NUMBER)
			.build();
	
	public static final MetaParameter ERROR_COUNT = MetaParameter.builder()
			.basicCollect(false)
			.name(ERROR_COUNT_PARAMETER)
			.unit(ERROR_COUNT_PARAMETER_UNIT)
			.type(SimpleParamType.NUMBER)
			.build();

	public static final MetaParameter LAST_ERROR = MetaParameter.builder()
			.basicCollect(true)
			.name(LAST_ERROR_PARAMETER)
			.type(SimpleParamType.TEXT)
			.build();

	private static final List<String> METADATA = List.of(DEVICE_ID, RAID_LEVEL, SIZE, IDENTIFYING_INFORMATION);

	public static final AlertRule STATUS_WARN_ALERT_RULE = new AlertRule(LogicalDisk::checkStatusWarnCondition,
			STATUS_WARN_CONDITION,
			Severity.WARN);
	public static final AlertRule STATUS_ALARM_ALERT_RULE = new AlertRule(LogicalDisk::checkStatusAlarmCondition,
			STATUS_ALARM_CONDITION,
			Severity.ALARM);
	public static final AlertRule ERROR_COUNT_ALERT_RULE = new AlertRule(LogicalDisk::checkErrorCountCondition,
			ERROR_COUNT_ALARM_CONDITION,
			Severity.ALARM);

	private static final Map<String, MetaParameter> META_PARAMETERS;
	private static final Map<String, List<AlertRule>> ALERT_RULES;

	static {
		final Map<String, MetaParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(STATUS_PARAMETER, STATUS);
		map.put(ERROR_COUNT_PARAMETER, ERROR_COUNT);
		map.put(UNALLOCATED_SPACE_PARAMETER, UNALLOCATED_SPACE);
		map.put(LAST_ERROR_PARAMETER, LAST_ERROR);

		META_PARAMETERS = Collections.unmodifiableMap(map);

		final Map<String, List<AlertRule>> alertRulesMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		alertRulesMap.put(STATUS_PARAMETER, List.of(STATUS_WARN_ALERT_RULE, STATUS_ALARM_ALERT_RULE));
		alertRulesMap.put(ERROR_COUNT_PARAMETER, Collections.singletonList(ERROR_COUNT_ALERT_RULE));

		ALERT_RULES = Collections.unmodifiableMap(alertRulesMap);

	}

	/**
	 * Check condition when the errorCount is in Alarm state.
	 * 
	 * @param monitor    The monitor we wish to check its errorCount
	 * @param conditions The condition used to determine if the alarm is reached
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkErrorCountCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<NumberParam> correctedErrorCountAsserted = monitor.assertNumberParameter(ERROR_COUNT_PARAMETER, conditions);
		if (correctedErrorCountAsserted.isAbnormal()) {

			return AlertDetails.builder()
					.problem(String.format("The logical disk encountered errors (%f).", correctedErrorCountAsserted.getParameter().getValue()))
					.consequence("The integrity of the data stored on this logical disk may be in jeopardy.")
					.recommendedAction("Check whether a physical disk is in degraded state or predicts failure, and if so, replace it.")
					.build();

		}
		return null;
	}

	/**
	 * Check condition when there are too many errors.
	 * 
	 * @param monitor    The monitor we wish to check its errorCount
	 * @param conditions The conditions used to determine if the alarm is reached
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkHighErrorCountCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<NumberParam> correctedErrorCountAsserted = monitor.assertNumberParameter(ERROR_COUNT_PARAMETER, conditions);
		if (correctedErrorCountAsserted.isAbnormal()) {

			return AlertDetails.builder()
					.problem(String.format("The logical disk encountered too many errors (%f).", correctedErrorCountAsserted.getParameter().getValue()))
					.consequence("The integrity of the data stored on this logical disk is affected (possible data corruption).")
					.recommendedAction("Check whether a physical disk is in degraded state or predicts failure, and if so, replace it.")
					.build();

		}
		return null;
	}

	/**
	 * Check condition when the monitor status is in WARN state.
	 * 
	 * @param monitor    The monitor we wish to check its status
	 * @param conditions The conditions used to check the parameter value
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkStatusWarnCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<DiscreteParam> assertedStatus = monitor.assertStatusParameter(STATUS_PARAMETER, conditions);
		if (assertedStatus.isAbnormal()) {

			return AlertDetails.builder()
					.problem("Although still working and available, this logical disk is degraded." + IMetaMonitor.getStatusInformationMessage(monitor))
					.consequence("The performance of the system may be affected.")
					.recommendedAction("If the RAID controller is not already handling this problem, check if you need to replace a physical disk or assign a hot spare to this logical disk.")
					.build();

		}
		return null;
	}

	/**
	 * Check condition when the monitor status is in ALARM state.
	 * 
	 * @param monitor    The monitor we wish to check its status
	 * @param conditions The conditions used to check the parameter value
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkStatusAlarmCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<DiscreteParam> assertedStatus = monitor.assertStatusParameter(STATUS_PARAMETER, conditions);
		if (assertedStatus.isAbnormal()) {

			String problem = "This logical disk is in critical state." +  IMetaMonitor.getStatusInformationMessage(monitor);
			return AlertDetails.builder()
					.problem(problem)
					.consequence("This means that one or more filesystems are no longer available (possible data loss).")
					.recommendedAction("Check whether the RAID controller is able to repair the logical disk. If not, you may have to re-create this logical disk and restore its data from a backup image.")
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
		return MonitorType.LOGICAL_DISK;
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