package com.sentrysoftware.matrix.common.meta.monitor;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.common.meta.parameter.MetaParameter;
import com.sentrysoftware.matrix.common.meta.parameter.ParameterType;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.strategy.IMonitorVisitor;

public class Led implements IMetaMonitor {

	public static final MetaParameter COLOR = MetaParameter.builder()
			.basicCollect(true)
			.name(HardwareConstants.COLOR_PARAMETER)
			.unit(HardwareConstants.STATUS_PARAMETER_UNIT)
			.type(ParameterType.STATUS)
			.build();

	public static final MetaParameter LED_INDICATOR = MetaParameter.builder()
			.basicCollect(true)
			.name(HardwareConstants.LED_INDICATOR_PARAMETER)
			.unit(HardwareConstants.LED_INDICATOR_PARAMETER_UNIT)
			.type(ParameterType.STATUS)
			.build();


	private static final Map<String, MetaParameter> META_PARAMETERS;

	static {
		final Map<String, MetaParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(HardwareConstants.STATUS_PARAMETER, STATUS);
//		map.put(HardwareConstants.PRESENT_PARAMETER, PRESENT);
		map.put(HardwareConstants.COLOR_PARAMETER, COLOR);
		map.put(HardwareConstants.LED_INDICATOR_PARAMETER, LED_INDICATOR);

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
		return MonitorType.LED;
	}
}