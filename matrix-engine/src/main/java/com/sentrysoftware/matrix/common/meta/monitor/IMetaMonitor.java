package com.sentrysoftware.matrix.common.meta.monitor;

import java.util.List;
import java.util.Map;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.common.meta.parameter.MetaParameter;
import com.sentrysoftware.matrix.common.meta.parameter.ParameterType;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.strategy.IMonitorVisitor;

public interface IMetaMonitor {

	MetaParameter STATUS = MetaParameter.builder()
			.basicCollect(true)
			.name(HardwareConstants.STATUS_PARAMETER)
			.unit(HardwareConstants.STATUS_PARAMETER_UNIT)
			.type(ParameterType.STATUS).build();

	MetaParameter PRESENT = MetaParameter.builder()
			.basicCollect(false)
			.name(HardwareConstants.PRESENT_PARAMETER)
			.unit(HardwareConstants.PRESENT_PARAMETER_UNIT)
			.type(ParameterType.PRESENT).build();

	MetaParameter PREDICTED_FAILURE = MetaParameter.builder()
			.basicCollect(true)
			.name(HardwareConstants.PREDICTED_FAILURE_PARAMETER)
			.unit(HardwareConstants.PREDICTED_FAILURE_PARAMETER_UNIT)
			.type(ParameterType.STATUS)
			.build();

	MetaParameter ERROR_COUNT = MetaParameter.builder()
			.basicCollect(true)
			.name(HardwareConstants.ERROR_COUNT_PARAMETER)
			.unit(HardwareConstants.ERROR_COUNT_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();


	void accept(IMonitorVisitor monitorVisitor);

	Map<String, MetaParameter> getMetaParameters();

	MonitorType getMonitorType();

	List<String> getMetadata();

	default boolean hasPresentParameter() {

		return getMetaParameters().containsKey(HardwareConstants.PRESENT_PARAMETER);
	}
}