package com.sentrysoftware.matrix.common.meta.monitor;

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
import com.sentrysoftware.matrix.model.parameter.ParameterState;
import com.sentrysoftware.matrix.model.parameter.StatusParam;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DEVICE_ID;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DOMAIN;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.IDENTIFYING_INFORMATION;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.IP_ADDRESS;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_SHARE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_SOURCE_ID;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.STATUS_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.STATUS_WARN_CONDITION;

public class Vm implements IMetaMonitor {

	private static final List<String> METADATA = List.of(DEVICE_ID, POWER_SHARE, DOMAIN, IP_ADDRESS, IDENTIFYING_INFORMATION);

	public static final MetaParameter POWER_STATE = MetaParameter.builder()
			.basicCollect(true)
			.name(HardwareConstants.POWER_STATE_PARAMETER)
			.unit(HardwareConstants.POWER_STATE_PARAMETER_UNIT)
			.type(ParameterType.STATUS)
			.build();

	public static final AlertRule STATUS_WARN_ALERT_RULE = new AlertRule(Vm::checkStatusWarnCondition,
			STATUS_WARN_CONDITION,
			ParameterState.WARN);
	public static final AlertRule STATUS_ALARM_ALERT_RULE = new AlertRule(Vm::checkStatusAlarmCondition,
			STATUS_ALARM_CONDITION,
			ParameterState.ALARM);

	private static final Map<String, MetaParameter> META_PARAMETERS;
	private static final Map<String, List<AlertRule>> ALERT_RULES;

	static {
		final Map<String, MetaParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(HardwareConstants.STATUS_PARAMETER, STATUS);
		map.put(HardwareConstants.PRESENT_PARAMETER, PRESENT);
		map.put(HardwareConstants.POWER_STATE_PARAMETER, POWER_STATE);

		META_PARAMETERS = Collections.unmodifiableMap(map);

		final Map<String, List<AlertRule>> alertRulesMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		alertRulesMap .put(HardwareConstants.STATUS_PARAMETER, List.of(STATUS_WARN_ALERT_RULE, STATUS_ALARM_ALERT_RULE));

		ALERT_RULES = Collections.unmodifiableMap(alertRulesMap);
	}

	/**
	 * Check condition when the monitor status is in WARN state.
	 * 
	 * @param monitor    The monitor we wish to check its status
	 * @param conditions The conditions used to detect abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkStatusWarnCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<StatusParam> assertedStatus = monitor.assertStatusParameter(HardwareConstants.STATUS_PARAMETER, conditions);
		if (assertedStatus.isAbnormal()) {

			return AlertDetails.builder()
				.problem("This VM is degraded and is not fully available for normal operations." + IMetaMonitor.getStatusInformationMessage(assertedStatus.getParameter()))
				.consequence("If this VM is part of a distributed server farm, it may be unable to handle the load and may affect the performance of other VMs.")
				.recommendedAction("Check why this VM is degraded and try to bring it fully online. Otherwise, try distributing the load to other VMs.")
				.build();
		}
		return null;
	}

	/**
	 * Check condition when the monitor status is in ALARM state.
	 * 
	 * @param monitor    The monitor we wish to check its status
	 * @param conditions The conditions used to detect abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkStatusAlarmCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<StatusParam> assertedStatus = monitor.assertStatusParameter(HardwareConstants.STATUS_PARAMETER, conditions);
		if (assertedStatus.isAbnormal()) {

			return AlertDetails.builder()
				.problem("This VM has failed." + IMetaMonitor.getStatusInformationMessage(assertedStatus.getParameter()))
				.consequence("If this VM is part of a distributed server farm, it may be unable to handle the load and probably affects the performance of other VMs.")
				.recommendedAction("Check why this VM is not working and try to bring it online. Otherwise, try distributing the load to other VMs.")
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
		return MonitorType.VM;
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