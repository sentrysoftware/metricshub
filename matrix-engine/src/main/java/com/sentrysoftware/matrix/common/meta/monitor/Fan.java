package com.sentrysoftware.matrix.common.meta.monitor;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.common.meta.parameter.MetaParameter;
import com.sentrysoftware.matrix.common.meta.parameter.ParameterType;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.strategy.IMonitorVisitor;

public class Fan implements IMetaMonitor {

	public static final MetaParameter SPEED = MetaParameter.builder()
			.basicCollect(true)
			.name(HardwareConstants.SPEED_PARAMETER)
			.unit(HardwareConstants.SPEED_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final MetaParameter SPEED_PERCENT = MetaParameter.builder()
			.basicCollect(true)
			.name(HardwareConstants.SPEED_PERCENT_PARAMETER)
			.unit(HardwareConstants.SPEED_PERCENT_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	private static final Map<String, MetaParameter> META_PARAMETERS;

	static {
		final Map<String, MetaParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(HardwareConstants.STATUS_PARAMETER, STATUS);
		map.put(HardwareConstants.PRESENT_PARAMETER, PRESENT);
		map.put(HardwareConstants.SPEED_PARAMETER, SPEED);
		map.put(HardwareConstants.SPEED_PERCENT_PARAMETER, SPEED_PERCENT);

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
		return MonitorType.FAN;
	}
}