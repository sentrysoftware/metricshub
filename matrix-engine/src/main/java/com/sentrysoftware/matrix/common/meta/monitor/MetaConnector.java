package com.sentrysoftware.matrix.common.meta.monitor;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DESCRIPTION;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DISPLAY_NAME;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.FILE_NAME;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.STATUS_ALARM_CONDITION;

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
import com.sentrysoftware.matrix.model.parameter.ParameterState;
import com.sentrysoftware.matrix.model.parameter.StatusParam;

public class MetaConnector implements IMetaMonitor {

	public static final MetaParameter TEST_REPORT = MetaParameter.builder()
			.basicCollect(false)
			.name(HardwareConstants.TEST_REPORT_PARAMETER)
			.type(ParameterType.TEXT)
			.build();

	private static final List<String> METADATA = List.of(DISPLAY_NAME, FILE_NAME, DESCRIPTION);

	public static final AlertRule STATUS_ALARM_ALERT_RULE = new AlertRule(MetaConnector::checkStatusAlarmCondition,
			STATUS_ALARM_CONDITION,
			ParameterState.ALARM);

	private static final Map<String, MetaParameter> META_PARAMETERS;
	private static final Map<String, List<AlertRule>> ALERT_RULES;

	static {
		final Map<String, MetaParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(HardwareConstants.STATUS_PARAMETER, STATUS);
		map.put(HardwareConstants.TEST_REPORT_PARAMETER, TEST_REPORT);

		META_PARAMETERS = Collections.unmodifiableMap(map);

		final Map<String, List<AlertRule>> alertRulesMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		alertRulesMap.put(HardwareConstants.STATUS_PARAMETER, Collections.singletonList(STATUS_ALARM_ALERT_RULE));

		ALERT_RULES = Collections.unmodifiableMap(alertRulesMap);
	}

	/**
	 * Check condition when the monitor status is in ALARM state.
	 * 
	 * @param monitor    The monitor we wish to check its status
	 * @param conditions The conditions used to determine abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkStatusAlarmCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<StatusParam> assertedStatus = monitor.assertStatusParameter(HardwareConstants.STATUS_PARAMETER, conditions);
		if (assertedStatus.isAbnormal()) {

			return AlertDetails.builder()
					.problem("This connector is no longer working." +  IMetaMonitor.getStatusInformationMessage(assertedStatus.getParameter()))
					.consequence("All of the components that were monitored through this connector will no longer be monitored.")
					.recommendedAction("Check the TestReport parameter of this connector. It will help understand why this connector is no longer working.")
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
		return MonitorType.CONNECTOR;
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