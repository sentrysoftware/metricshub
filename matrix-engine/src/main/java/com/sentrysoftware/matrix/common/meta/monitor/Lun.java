package com.sentrysoftware.matrix.common.meta.monitor;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.common.meta.parameter.MetaParameter;
import com.sentrysoftware.matrix.common.meta.parameter.ParameterType;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.strategy.IMonitorVisitor;

public class Lun implements IMetaMonitor {

	public static final MetaParameter AVAILABLE_PATH_COUNT = MetaParameter.builder()
			.basicCollect(true)
			.name(HardwareConstants.AVAILABLE_PATH_COUNT_PARAMETER)
			.unit(HardwareConstants.PATHS_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final MetaParameter AVAILABLE_PATH_INFORMATION = MetaParameter.builder()
			.basicCollect(true)
			.name(HardwareConstants.AVAILABLE_PATH_INFORMATION_PARAMETER)
			.type(ParameterType.TEXT)
			.build();

	private static final Map<String, MetaParameter> META_PARAMETERS;

	static {
		final Map<String, MetaParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(HardwareConstants.STATUS_PARAMETER, STATUS);
		map.put(HardwareConstants.AVAILABLE_PATH_COUNT_PARAMETER, AVAILABLE_PATH_COUNT);
		map.put(HardwareConstants.AVAILABLE_PATH_INFORMATION_PARAMETER, AVAILABLE_PATH_INFORMATION);

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
		return MonitorType.LUN;
	}
}