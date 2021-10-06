package com.sentrysoftware.matrix.common.meta.monitor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.sentrysoftware.matrix.common.meta.parameter.DiscreteParamType;
import com.sentrysoftware.matrix.common.meta.parameter.MetaParameter;
import com.sentrysoftware.matrix.common.meta.parameter.SimpleParamType;
import com.sentrysoftware.matrix.common.meta.parameter.state.ErrorStatus;
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
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_USAGE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_COUNT_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_STATUS_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.LAST_ERROR_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MODEL;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_CONSUMPTION_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PREDICTED_FAILURE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PRESENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SERIAL_NUMBER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SIZE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TYPE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.VENDOR;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.ERROR_COUNT_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.PRESENT_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.STATUS_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.STATUS_WARN_CONDITION;

public class Memory implements IMetaMonitor {

	private static final String RECOMMENDED_ACTION_FOR_BAD_MEMORY = "Replace this memory module as soon as possible to prevent a system crash or data corruption.";

	public static final MetaParameter ERROR_STATUS = MetaParameter.builder()
			.basicCollect(true)
			.name(ERROR_STATUS_PARAMETER)
			.unit(ERROR_STATUS_PARAMETER_UNIT)
			.type(DiscreteParamType
					.builder()
					.interpreter(ErrorStatus::interpret)
					.build()
			)
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

	private static final List<String> METADATA = List.of(DEVICE_ID, SERIAL_NUMBER, VENDOR, MODEL, TYPE, SIZE,
			IDENTIFYING_INFORMATION);

	public static final AlertRule PRESENT_ALERT_RULE = new AlertRule(Memory::checkMissingCondition,
			PRESENT_ALARM_CONDITION,
			Severity.ALARM);
	public static final AlertRule STATUS_WARN_ALERT_RULE = new AlertRule(Memory::checkStatusWarnCondition,
			STATUS_WARN_CONDITION,
			Severity.WARN);
	public static final AlertRule STATUS_ALARM_ALERT_RULE = new AlertRule(Memory::checkStatusAlarmCondition,
			STATUS_ALARM_CONDITION,
			Severity.ALARM);
	public static final AlertRule ERROR_STATUS_WARN_ALERT_RULE = new AlertRule(Memory::checkErrorStatusWarnCondition,
			STATUS_WARN_CONDITION,
			Severity.WARN);
	public static final AlertRule ERROR_STATUS_ALARM_ALERT_RULE = new AlertRule(Memory::checkErrorStatusAlarmCondition,
			STATUS_ALARM_CONDITION,
			Severity.ALARM);
	public static final AlertRule ERROR_COUNT_ALERT_RULE = new AlertRule(Memory::checkErrorCountCondition,
			ERROR_COUNT_ALARM_CONDITION,
			Severity.ALARM);
	public static final AlertRule PREDICTED_FAILURE_ALERT_RULE = new AlertRule(Memory::checkPredictedFailureWarnCondition,
			STATUS_WARN_CONDITION,
			Severity.WARN);

	private static final Map<String, MetaParameter> META_PARAMETERS;
	private static final Map<String, List<AlertRule>> ALERT_RULES;

	static {
		final Map<String, MetaParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(STATUS_PARAMETER, STATUS);
		map.put(ERROR_COUNT_PARAMETER, ERROR_COUNT);
		map.put(ERROR_STATUS_PARAMETER, ERROR_STATUS);
		map.put(PREDICTED_FAILURE_PARAMETER, PREDICTED_FAILURE);
		map.put(PRESENT_PARAMETER, PRESENT);
		map.put(ENERGY_PARAMETER, ENERGY);
		map.put(ENERGY_USAGE_PARAMETER, ENERGY_USAGE);
		map.put(POWER_CONSUMPTION_PARAMETER, POWER_CONSUMPTION);
		map.put(LAST_ERROR_PARAMETER, LAST_ERROR);

		META_PARAMETERS = Collections.unmodifiableMap(map);

		final Map<String, List<AlertRule>> alertRulesMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		alertRulesMap.put(PRESENT_PARAMETER, Collections.singletonList(PRESENT_ALERT_RULE));
		alertRulesMap.put(STATUS_PARAMETER, List.of(STATUS_WARN_ALERT_RULE, STATUS_ALARM_ALERT_RULE));
		alertRulesMap.put(ERROR_STATUS_PARAMETER, List.of(ERROR_STATUS_WARN_ALERT_RULE, ERROR_STATUS_ALARM_ALERT_RULE));
		alertRulesMap.put(ERROR_COUNT_PARAMETER, Collections.singletonList(ERROR_COUNT_ALERT_RULE));
		alertRulesMap.put(PREDICTED_FAILURE_PARAMETER, Collections.singletonList(PREDICTED_FAILURE_ALERT_RULE));

		ALERT_RULES = Collections.unmodifiableMap(alertRulesMap);
	}

	/**
	 * Check missing Memory condition.
	 * 
	 * @param monitor    The monitor we wish to check
	 * @param conditions The conditions used to determine the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkMissingCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<DiscreteParam> assertedPresent = monitor.assertPresentParameter(conditions);
		if (assertedPresent.isAbnormal()) {

			return AlertDetails.builder()
					.problem("This memory module is not detected anymore.")
					.consequence("This could mean that the memory module has been disabled by BIOS, or that it has been deallocated for this system (in case of a dynamically reconfigurable machine). The total memory size has been reduced and this may impact the overall performance of this computer.")
					.recommendedAction("Check whether the memory module has been intentionally removed by authorized personnel or whether the memory module failed. In the latter case, replace the faulty memory module.")
					.build();
		}

		return null;
	}

	/**
	 * Check condition when the monitor status is in WARN state.
	 * 
	 * @param monitor    The monitor we wish to check its status
	 * @param conditions The conditions used to determine the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkStatusWarnCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<DiscreteParam> assertedStatus = monitor.assertStatusParameter(STATUS_PARAMETER, conditions);
		if (assertedStatus.isAbnormal()) {

			return AlertDetails.builder()
					.problem("This memory module is degraded or about to fail." + IMetaMonitor.getStatusInformationMessage(monitor))
					.consequence("If degraded, this memory module may not be fully operational and may impact the overall performance of the system. If about to fail, this may soon lead to a system crash or data corruption.")
					.recommendedAction(RECOMMENDED_ACTION_FOR_BAD_MEMORY)
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
		final AssertedParameter<DiscreteParam> assertedStatus = monitor.assertStatusParameter(STATUS_PARAMETER, conditions);
		if (assertedStatus.isAbnormal()) {

			return AlertDetails.builder()
					.problem("This memory module has failed." +  IMetaMonitor.getStatusInformationMessage(monitor))
					.consequence("This memory module is not operational. This probably impacts the overall performance of the system.")
					.recommendedAction("Replace this memory module as soon as possible to prevent a system overload.")
					.build();
		}

		return null;
	}

	/**
	 * Check condition when the monitor error status is in WARN state.
	 * 
	 * @param monitor    The monitor we wish to check its errorCount
	 * @param conditions The conditions used to determine the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkErrorStatusWarnCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<DiscreteParam> assertedStatus = monitor.assertStatusParameter(ERROR_STATUS_PARAMETER, conditions);
		if (assertedStatus.isAbnormal()) {

			return AlertDetails.builder()
					.problem("This memory module has encountered one or several errors." + IMetaMonitor.getStatusInformationMessage(monitor))
					.consequence("This memory module is getting unstable and may cause a system crash or data corruption.")
					.recommendedAction(RECOMMENDED_ACTION_FOR_BAD_MEMORY)
					.build();
		}

		return null;
	}

	/**
	 * Check condition when the monitor error status is in ALARM state.
	 * 
	 * @param monitor    The monitor we wish to check its errorStatus
	 * @param conditions The conditions used to determine the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkErrorStatusAlarmCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<DiscreteParam> assertedStatus = monitor.assertStatusParameter(ERROR_STATUS_PARAMETER, conditions);
		if (assertedStatus.isAbnormal()) {

			return AlertDetails.builder()
					.problem("This memory module has encountered too many errors." + IMetaMonitor.getStatusInformationMessage(monitor))
					.consequence("This memory module is unstable and will probably cause a system crash or data corruption.")
					.recommendedAction(RECOMMENDED_ACTION_FOR_BAD_MEMORY)
					.build();
		}

		return null;
	}

	/**
	 * Check condition when the monitor error count is in an abnormal state.
	 * 
	 * @param monitor    The monitor we wish to check its error count
	 * @param conditions The conditions used to determine the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkErrorCountCondition(Monitor monitor, Set<AlertCondition> conditions) {
		AssertedParameter<NumberParam> errorCountAsserted = monitor.assertNumberParameter(ERROR_COUNT_PARAMETER, conditions);
		if (errorCountAsserted.isAbnormal()) {

			return AlertDetails.builder()
					.problem(String.format("This memory module encountered a few internal errors (%f).", errorCountAsserted.getParameter().getValue()))
					.consequence("The stability of the system may be affected. A system crash or data corruption is likely to occur soon.")
					.recommendedAction("Replace this memory module as soon as possible to prevent a system overload.")
					.build();
		}

		return null;
	}

	/**
	 * Check condition when the monitor error count is too high.
	 * 
	 * @param monitor    The monitor we wish to check its error count
	 * @param conditions The conditions used to determine the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkHighErrorCountCondition(Monitor monitor, Set<AlertCondition> conditions) {
		AssertedParameter<NumberParam> errorCountAsserted = monitor.assertNumberParameter(ERROR_COUNT_PARAMETER, conditions);
		if (errorCountAsserted.isAbnormal()) {

			return AlertDetails.builder()
					.problem(String.format("This memory module encountered a high number of internal errors (%f).", errorCountAsserted.getParameter().getValue()))
					.consequence("The stability of the system may be critically affected. A system crash or data corruption is very likely to occur soon.")
					.recommendedAction("Replace this memory module as soon as possible to prevent a system crash.")
					.build();
		}

		return null;
	}

	/**
	 * Check condition when the monitor predictedFailure is in WARN state.
	 * 
	 * @param monitor    The monitor we wish to check its predictedFailure
	 * @param conditions The conditions used to determine the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkPredictedFailureWarnCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<DiscreteParam> assertedPredictedFailure = monitor.assertStatusParameter(PREDICTED_FAILURE_PARAMETER, conditions);
		if (assertedPredictedFailure.isAbnormal()) {

			return AlertDetails.builder()
					.problem("An imminent failure is predicted on this memory module.")
					.consequence("A system crash or data corruption is very likely to occur soon.")
					.recommendedAction("Replace this memory module as soon as possible to prevent a system crash.")
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
		return MonitorType.MEMORY;
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