package com.sentrysoftware.matrix.common.meta.monitor;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.common.meta.parameter.MetaParameter;
import com.sentrysoftware.matrix.common.meta.parameter.ParameterType;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.strategy.IMonitorVisitor;

public class Memory implements IMetaMonitor {

	public static final MetaParameter ERROR_COUNT = MetaParameter.builder()
			.basicCollect(true)
			.name(HardwareConstants.ERROR_COUNT_PARAMETER)
			.unit(HardwareConstants.ERROR_COUNT_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final MetaParameter ERROR_STATUS = MetaParameter.builder()
			.basicCollect(true)
			.name(HardwareConstants.ERROR_STATUS_PARAMETER)
			.unit(HardwareConstants.ERROR_STATUS_PARAMETER_UNIT)
			.type(ParameterType.STATUS)
			.build();

	public static final MetaParameter PREDICTED_FAILURE = MetaParameter.builder()
			.basicCollect(true)
			.name(HardwareConstants.PREDICTED_FAILURE_PARAMETER)
			.unit(HardwareConstants.PREDICTED_FAILURE_PARAMETER_UNIT)
			.type(ParameterType.STATUS)
			.build();

	private static final Map<String, MetaParameter> META_PARAMETERS;

	static {
		final Map<String, MetaParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(HardwareConstants.STATUS_PARAMETER, STATUS);
		map.put(HardwareConstants.ERROR_COUNT_PARAMETER, ERROR_COUNT);
		map.put(HardwareConstants.ERROR_STATUS_PARAMETER, ERROR_STATUS);
		map.put(HardwareConstants.PREDICTED_FAILURE_PARAMETER, PREDICTED_FAILURE);
		map.put(HardwareConstants.PRESENT_PARAMETER, PRESENT);

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
		return MonitorType.MEMORY;
	}
}