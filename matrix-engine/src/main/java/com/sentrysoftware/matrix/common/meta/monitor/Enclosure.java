package com.sentrysoftware.matrix.common.meta.monitor;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.BIOS_VERSION;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DEVICE_ID;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_USAGE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.IDENTIFYING_INFORMATION;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.INTRUSION_STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.INTRUSION_STATUS_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MODEL;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_CONSUMPTION_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PRESENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SERIAL_NUMBER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TYPE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.VENDOR;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.PRESENT_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.STATUS_ALARM_CONDITION;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.sentrysoftware.matrix.common.meta.parameter.DiscreteParamType;
import com.sentrysoftware.matrix.common.meta.parameter.MetaParameter;
import com.sentrysoftware.matrix.common.meta.parameter.state.IntrusionStatus;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.strategy.IMonitorVisitor;
import com.sentrysoftware.matrix.model.alert.AlertCondition;
import com.sentrysoftware.matrix.model.alert.AlertDetails;
import com.sentrysoftware.matrix.model.alert.AlertRule;
import com.sentrysoftware.matrix.model.alert.Severity;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitor.Monitor.AssertedParameter;
import com.sentrysoftware.matrix.model.parameter.DiscreteParam;

public class Enclosure implements IMetaMonitor {

	public static final MetaParameter INTRUSION_STATUS = MetaParameter.builder()
			.basicCollect(true)
			.name(INTRUSION_STATUS_PARAMETER)
			.unit(INTRUSION_STATUS_PARAMETER_UNIT)
			.type(new DiscreteParamType(IntrusionStatus::interpret))
			.build();

	private static final List<String> METADATA = List.of(DEVICE_ID, SERIAL_NUMBER, VENDOR, MODEL, BIOS_VERSION, TYPE,
			IDENTIFYING_INFORMATION);

	public static final AlertRule PRESENT_ALERT_RULE = new AlertRule(Enclosure::checkMissingCondition,
			PRESENT_ALARM_CONDITION,
			Severity.ALARM);
	public static final AlertRule INTRUSION_STATUS_ALERT_RULE = new AlertRule(Enclosure::checkIntrusionStatusAlarmCondition,
			STATUS_ALARM_CONDITION,
			Severity.ALARM);

	private static final Map<String, MetaParameter> META_PARAMETERS;
	private static final Map<String, List<AlertRule>> ALERT_RULES;

	static {
		final Map<String, MetaParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(INTRUSION_STATUS_PARAMETER, INTRUSION_STATUS);
		map.put(ENERGY_USAGE_PARAMETER, ENERGY_USAGE);
		map.put(ENERGY_PARAMETER, ENERGY);
		map.put(POWER_CONSUMPTION_PARAMETER, POWER_CONSUMPTION);
		map.put(STATUS_PARAMETER, STATUS);
		map.put(PRESENT_PARAMETER, PRESENT);

		META_PARAMETERS = Collections.unmodifiableMap(map);

		final Map<String, List<AlertRule>> alertRulesMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		alertRulesMap.put(PRESENT_PARAMETER, Collections.singletonList(PRESENT_ALERT_RULE));
		alertRulesMap.put(INTRUSION_STATUS_PARAMETER, Collections.singletonList(INTRUSION_STATUS_ALERT_RULE));

		ALERT_RULES = Collections.unmodifiableMap(alertRulesMap);

	}

	/**
	 * Check missing enclosure condition
	 *
	 * @param monitor    The monitor we wish to check
	 * @param conditions The conditions used to determine the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkMissingCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<DiscreteParam> assertedPresent = monitor.assertPresentParameter(conditions);
		if (assertedPresent.isAbnormal()) {

			return AlertDetails.builder()
					.problem("This enclosure is not detected anymore.")
					.consequence("This is probably a problem between Hardware Sentry and the hardware agent.")
					.recommendedAction("Check that the connectors are running properly.")
					.build();

		}

		return null;
	}

	/**
	 * Check condition when the monitor intrusion status is in ALARM state.
	 *
	 * @param monitor The monitor we wish to check
	 * @param conditions The conditions used to check the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkIntrusionStatusAlarmCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<DiscreteParam> assertedStatus = monitor.assertStatusParameter(INTRUSION_STATUS_PARAMETER, conditions);
		if (assertedStatus.isAbnormal()) {

			return AlertDetails.builder()
					.problem("The enclosure is open or has been removed.")
					.consequence("This could mean that somebody is accessing the hardware components in the enclosure, including the harddisks which may contain private information.")
					.recommendedAction("Make sure the enclosure has been opened by authorized personnel only and close it as soon as possible.")
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
		return MonitorType.ENCLOSURE;
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