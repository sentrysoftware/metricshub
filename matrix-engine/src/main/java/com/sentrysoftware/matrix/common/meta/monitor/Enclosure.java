package com.sentrysoftware.matrix.common.meta.monitor;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.common.meta.parameter.MetaParameter;
import com.sentrysoftware.matrix.common.meta.parameter.ParameterType;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.strategy.IMonitorVisitor;

public class Enclosure implements IMetaMonitor {

	public static final MetaParameter INTRUSION_STATUS = MetaParameter.builder()
			.basicCollect(true)
			.name(HardwareConstants.INTRUSION_STATUS_PARAMETER)
			.unit(HardwareConstants.INTRUSION_STATUS_PARAMETER_UNIT)
			.type(ParameterType.STATUS)
			.build();

	public static final MetaParameter ENERGY_USAGE = MetaParameter.builder()
			.basicCollect(false)
			.name(HardwareConstants.ENERGY_USAGE_PARAMETER)
			.unit(HardwareConstants.ENERGY_USAGE_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final MetaParameter POWER_CONSUMPTION = MetaParameter.builder()
			.basicCollect(false)
			.name(HardwareConstants.POWER_CONSUMPTION_PARAMETER)
			.unit(HardwareConstants.POWER_CONSUMPTION_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final MetaParameter ENERGY = MetaParameter
		.builder()
		.basicCollect(false)
		.name(HardwareConstants.ENERGY_PARAMETER)
		.unit(HardwareConstants.ENERGY_PARAMETER_UNIT)
		.type(ParameterType.NUMBER)
		.build();

	private static final Map<String, MetaParameter> META_PARAMETERS;

	static {
		final Map<String, MetaParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(HardwareConstants.INTRUSION_STATUS_PARAMETER, INTRUSION_STATUS);
		map.put(HardwareConstants.ENERGY_USAGE_PARAMETER, ENERGY_USAGE);
		map.put(HardwareConstants.ENERGY_PARAMETER, ENERGY);
		map.put(HardwareConstants.POWER_CONSUMPTION_PARAMETER, POWER_CONSUMPTION);
		map.put(HardwareConstants.STATUS_PARAMETER, STATUS);

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
		return MonitorType.ENCLOSURE;
	}

}