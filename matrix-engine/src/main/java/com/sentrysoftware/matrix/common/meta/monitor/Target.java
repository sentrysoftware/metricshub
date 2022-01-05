package com.sentrysoftware.matrix.common.meta.monitor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sentrysoftware.matrix.common.meta.parameter.MetaParameter;
import com.sentrysoftware.matrix.common.meta.parameter.SimpleParamType;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.strategy.IMonitorVisitor;
import com.sentrysoftware.matrix.model.alert.AlertRule;

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

		META_PARAMETERS = Collections.unmodifiableMap(map);
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