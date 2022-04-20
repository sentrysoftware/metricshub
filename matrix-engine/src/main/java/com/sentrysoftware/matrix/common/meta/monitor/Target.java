package com.sentrysoftware.matrix.common.meta.monitor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.sentrysoftware.matrix.common.meta.parameter.DiscreteParamType;
import com.sentrysoftware.matrix.common.meta.parameter.MetaParameter;
import com.sentrysoftware.matrix.common.meta.parameter.SimpleParamType;
import com.sentrysoftware.matrix.common.meta.parameter.state.Up;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.strategy.IMonitorVisitor;
import com.sentrysoftware.matrix.model.alert.AlertCondition;
import com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder;
import com.sentrysoftware.matrix.model.alert.AlertDetails;
import com.sentrysoftware.matrix.model.alert.AlertRule;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitor.Monitor.AssertedParameter;
import com.sentrysoftware.matrix.model.parameter.DiscreteParam;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.AMBIENT_TEMPERATURE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.CPU_TEMPERATURE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.CPU_THERMAL_DISSIPATION_RATE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_USAGE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.HEATING_MARGIN_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.LOCATION;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_CONSUMPTION_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PRESENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TEMPERATURE_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SNMP_UP_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.WBEM_UP_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SSH_UP_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.WMI_UP_PARAMETER;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.UP_PARAMETER_UNIT;

public class Target implements IMetaMonitor {

	private static final List<String> METADATA = Collections.singletonList(LOCATION);

	public static final MetaParameter AMBIENT_TEMPERATURE = MetaParameter.builder()
			.basicCollect(false)
			.name(AMBIENT_TEMPERATURE_PARAMETER)
			.unit(TEMPERATURE_PARAMETER_UNIT)
			.type(SimpleParamType.NUMBER)
			.build();

	public static final MetaParameter CPU_TEMPERATURE = MetaParameter.builder()
			.basicCollect(false)
			.name(CPU_TEMPERATURE_PARAMETER)
			.unit(TEMPERATURE_PARAMETER_UNIT)
			.type(SimpleParamType.NUMBER)
			.build();

	public static final MetaParameter CPU_THERMAL_DISSIPATION_RATE = MetaParameter.builder()
			.basicCollect(false)
			.name(CPU_THERMAL_DISSIPATION_RATE_PARAMETER)
			.unit("")
			.type(SimpleParamType.NUMBER)
			.build();

	public static final MetaParameter SNMP_UP = MetaParameter.builder()
			.basicCollect(false)
			.name(SNMP_UP_PARAMETER)
			.unit(UP_PARAMETER_UNIT)
			.type(new DiscreteParamType(Up::interpret))
			.build();

	public static final MetaParameter WBEM_UP = MetaParameter.builder()
			.basicCollect(false)
			.name(WBEM_UP_PARAMETER)
			.unit(UP_PARAMETER_UNIT)
			.type(new DiscreteParamType(Up::interpret))
			.build();

	public static final MetaParameter SSH_UP = MetaParameter.builder()
			.basicCollect(false)
			.name(SSH_UP_PARAMETER)
			.unit(UP_PARAMETER_UNIT)
			.type(new DiscreteParamType(Up::interpret))
			.build();

	public static final MetaParameter WMI_UP = MetaParameter.builder()
			.basicCollect(false)
			.name(WMI_UP_PARAMETER)
			.unit(UP_PARAMETER_UNIT)
			.type(new DiscreteParamType(Up::interpret))
			.build();

	public static final Set<AlertCondition> ALERT_CONDITIONS = AlertConditionsBuilder.newInstance().lt(1D).build();

	private static final Map<String, MetaParameter> META_PARAMETERS;

	static {
		final Map<String, MetaParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(STATUS_PARAMETER, STATUS);
		map.put(HEATING_MARGIN_PARAMETER, HEATING_MARGIN);
		map.put(AMBIENT_TEMPERATURE_PARAMETER, AMBIENT_TEMPERATURE);
		map.put(CPU_TEMPERATURE_PARAMETER, CPU_TEMPERATURE);
		map.put(CPU_THERMAL_DISSIPATION_RATE_PARAMETER, CPU_THERMAL_DISSIPATION_RATE);
		map.put(ENERGY_PARAMETER, ENERGY);
		map.put(ENERGY_USAGE_PARAMETER, ENERGY_USAGE);
		map.put(POWER_CONSUMPTION_PARAMETER, POWER_CONSUMPTION);
		map.put(PRESENT_PARAMETER, PRESENT);
		map.put(SNMP_UP_PARAMETER, SNMP_UP);
		map.put(WBEM_UP_PARAMETER, WBEM_UP);
		map.put(SSH_UP_PARAMETER, SSH_UP);
		map.put(WMI_UP_PARAMETER, WMI_UP);

		META_PARAMETERS = Collections.unmodifiableMap(map);
	}

	/**
	 * Check condition when the monitor status is in ALARM state.
	 * 
	 * @param monitor    The monitor we wish to check its status
	 * @param conditions The conditions used to detect abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkSnmpStatusAlarmCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<DiscreteParam> assertedStatus = monitor.assertStatusParameter(SNMP_UP_PARAMETER,
				conditions);
		if (assertedStatus.isAbnormal()) {

			String problem = "SNMP Connection has been lost.";
			return AlertDetails.builder()
					.problem(problem)
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
	public static AlertDetails checkWbemStatusAlarmCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<DiscreteParam> assertedStatus = monitor.assertStatusParameter(WBEM_UP_PARAMETER,
				conditions);
		if (assertedStatus.isAbnormal()) {

			String problem = "WBEM Connection has been lost.";
			return AlertDetails.builder()
					.problem(problem)
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
	public static AlertDetails checkSshStatusAlarmCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<DiscreteParam> assertedStatus = monitor.assertStatusParameter(WBEM_UP_PARAMETER,
				conditions);
		if (assertedStatus.isAbnormal()) {

			String problem = "SSH Connection has been lost.";
			return AlertDetails.builder()
					.problem(problem)
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
	public static AlertDetails checkWmiStatusAlarmCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<DiscreteParam> assertedStatus = monitor.assertStatusParameter(WMI_UP_PARAMETER,
				conditions);
		if (assertedStatus.isAbnormal()) {

			String problem = "WMI Connection has been lost.";
			return AlertDetails.builder()
					.problem(problem)
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
		return MonitorType.TARGET;
	}

	@Override
	public List<String> getMetadata() {
		return METADATA;
	}

	@Override
	public Map<String, List<AlertRule>> getStaticAlertRules() {
		return Collections.emptyMap();
	}
}