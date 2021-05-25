package com.sentrysoftware.matrix.common.meta.monitor;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.common.meta.parameter.MetaParameter;
import com.sentrysoftware.matrix.common.meta.parameter.ParameterType;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.strategy.IMonitorVisitor;

public class NetworkCard implements IMetaMonitor {

	public static final MetaParameter BANDWIDTH_UTILIZATION_INFORMATION = MetaParameter.builder()
			.basicCollect(true)
			.name(HardwareConstants.BANDWIDTH_UTILIZATION_INFORMATION_PARAMETER)
			.unit(HardwareConstants.PERCENT_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final MetaParameter DUPLEX_MODE = MetaParameter.builder()
			.basicCollect(false)
			.name(HardwareConstants.DUPLEX_MODE_PARAMETER)
			.unit(HardwareConstants.DUPLEX_MODE_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final MetaParameter ERROR_PERCENT = MetaParameter.builder()
			.basicCollect(true)
			.name(HardwareConstants.ERROR_PERCENT_PARAMETER)
			.unit(HardwareConstants.PERCENT_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final MetaParameter LINK_SPEED = MetaParameter.builder()
			.basicCollect(true)
			.name(HardwareConstants.LINK_SPEED_PARAMETER)
			.unit(HardwareConstants.SPEED_MBITS_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final MetaParameter LINK_STATUS = MetaParameter.builder()
			.basicCollect(true)
			.name(HardwareConstants.LINK_STATUS_PARAMETER)
			.unit(HardwareConstants.LINK_STATUS_PARAMETER_UNIT)
			.type(ParameterType.STATUS)
			.build();

	public static final MetaParameter RECEIVED_BYTES_RATE = MetaParameter.builder()
			.basicCollect(false)
			.name(HardwareConstants.RECEIVED_BYTES_RATE_PARAMETER)
			.unit(HardwareConstants.BYTES_RATE_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final MetaParameter RECEIVED_PACKETS_RATE = MetaParameter.builder()
			.basicCollect(false)
			.name(HardwareConstants.RECEIVED_PACKETS_RATE_PARAMETER)
			.unit(HardwareConstants.PACKETS_RATE_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final MetaParameter TRANSMITTED_BYTES_RATE = MetaParameter.builder()
			.basicCollect(false)
			.name(HardwareConstants.TRANSMITTED_BYTES_RATE_PARAMETER)
			.unit(HardwareConstants.BYTES_RATE_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final MetaParameter TRANSMITTED_PACKETS_RATE = MetaParameter.builder()
			.basicCollect(false)
			.name(HardwareConstants.TRANSMITTED_PACKETS_RATE_PARAMETER)
			.unit(HardwareConstants.PACKETS_RATE_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final MetaParameter ZERO_BUFFER_CREDIT_PERCENT = MetaParameter.builder()
			.basicCollect(true)
			.name(HardwareConstants.ZERO_BUFFER_CREDIT_PERCENT_PARAMETER)
			.unit(HardwareConstants.PERCENT_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();


	private static final Map<String, MetaParameter> META_PARAMETERS;

	static {
		final Map<String, MetaParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(HardwareConstants.STATUS_PARAMETER, STATUS);
		map.put(HardwareConstants.PRESENT_PARAMETER, PRESENT);
		map.put(HardwareConstants.BANDWIDTH_UTILIZATION_INFORMATION_PARAMETER, BANDWIDTH_UTILIZATION_INFORMATION);
		map.put(HardwareConstants.DUPLEX_MODE_PARAMETER, DUPLEX_MODE);
		map.put(HardwareConstants.ERROR_PERCENT_PARAMETER, ERROR_PERCENT);
		map.put(HardwareConstants.LINK_SPEED_PARAMETER, LINK_SPEED);
		map.put(HardwareConstants.LINK_STATUS_PARAMETER, LINK_STATUS);
		map.put(HardwareConstants.RECEIVED_BYTES_RATE_PARAMETER, RECEIVED_BYTES_RATE);
		map.put(HardwareConstants.RECEIVED_PACKETS_RATE_PARAMETER, RECEIVED_PACKETS_RATE);
		map.put(HardwareConstants.TRANSMITTED_BYTES_RATE_PARAMETER, TRANSMITTED_BYTES_RATE);
		map.put(HardwareConstants.TRANSMITTED_PACKETS_RATE_PARAMETER, TRANSMITTED_PACKETS_RATE);
		map.put(HardwareConstants.ZERO_BUFFER_CREDIT_PERCENT_PARAMETER, ZERO_BUFFER_CREDIT_PERCENT);
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
		return MonitorType.NETWORK_CARD;
	}
}