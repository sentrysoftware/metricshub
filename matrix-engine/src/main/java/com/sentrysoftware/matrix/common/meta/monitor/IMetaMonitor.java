package com.sentrysoftware.matrix.common.meta.monitor;

import java.util.List;
import java.util.Map;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.common.meta.parameter.MetaParameter;
import com.sentrysoftware.matrix.common.meta.parameter.ParameterType;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.strategy.IMonitorVisitor;
import com.sentrysoftware.matrix.model.alert.AlertRule;
import com.sentrysoftware.matrix.model.parameter.StatusParam;

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

	MetaParameter ENERGY = MetaParameter.builder()
		.basicCollect(true)
		.name(HardwareConstants.ENERGY_PARAMETER)
		.unit(HardwareConstants.ENERGY_PARAMETER_UNIT)
		.type(ParameterType.NUMBER)
		.build();

	MetaParameter HEATING_MARGIN = MetaParameter.builder()
		.basicCollect(true)
		.name(HardwareConstants.HEATING_MARGIN_PARAMETER)
		.unit(HardwareConstants.HEATING_MARGIN_PARAMETER_UNIT)
		.type(ParameterType.NUMBER)
		.build();

	MetaParameter AMBIENT_TEMPERATURE = MetaParameter.builder()
		.basicCollect(false)
		.name(HardwareConstants.AMBIENT_TEMPERATURE_PARAMETER)
		.unit(HardwareConstants.TEMPERATURE_PARAMETER_UNIT)
		.type(ParameterType.NUMBER)
		.build();

	MetaParameter CPU_TEMPERATURE = MetaParameter.builder()
		.basicCollect(false)
		.name(HardwareConstants.CPU_TEMPERATURE_PARAMETER)
		.unit(HardwareConstants.TEMPERATURE_PARAMETER_UNIT)
		.type(ParameterType.NUMBER)
		.build();

	MetaParameter CPU_THERMAL_DISSIPATION_RATE = MetaParameter.builder()
		.basicCollect(false)
		.name(HardwareConstants.CPU_THERMAL_DISSIPATION_RATE_PARAMETER)
		.unit(HardwareConstants.EMPTY)
		.type(ParameterType.NUMBER)
		.build();

	void accept(IMonitorVisitor monitorVisitor);

	Map<String, MetaParameter> getMetaParameters();

	MonitorType getMonitorType();

	List<String> getMetadata();

	Map<String, List<AlertRule>> getStaticAlertRules();

	default boolean hasPresentParameter() {

		return getMetaParameters().containsKey(HardwareConstants.PRESENT_PARAMETER);
	}

	/**
	 * 
	 * @param status {@link StatusParam} instance we wish to extract its status information
	 * @return {@link String} value if the status information is available otherwise null
	 */
	/**
	 * 
	 * @param status {@link StatusParam} instance we wish to extract its status information
	 * @return {@link String} value if the status information is available otherwise null
	 */
	static String getStatusInformationMessage(StatusParam status) {
		String statusInformation = status.getStatusInformation();
		if (statusInformation != null && statusInformation.isBlank()) {
			return String.format(" Reported status: %s.", statusInformation);
		}
		return "";
	}

}