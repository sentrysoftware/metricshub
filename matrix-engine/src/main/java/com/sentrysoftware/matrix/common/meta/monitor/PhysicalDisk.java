package com.sentrysoftware.matrix.common.meta.monitor;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION1;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION2;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION3;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DEVICE_ID;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.FIRMWARE_VERSION;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MODEL;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SERIAL_NUMBER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SIZE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.VENDOR;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.common.meta.parameter.MetaParameter;
import com.sentrysoftware.matrix.common.meta.parameter.ParameterType;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.strategy.IMonitorVisitor;

public class PhysicalDisk implements IMetaMonitor {

	public static final MetaParameter INTRUSION_STATUS = MetaParameter.builder()
			.basicCollect(true)
			.name(HardwareConstants.INTRUSION_STATUS_PARAMETER)
			.unit(HardwareConstants.INTRUSION_STATUS_PARAMETER_UNIT)
			.type(ParameterType.STATUS)
			.build();

	public static final MetaParameter DEVICE_NOT_READY_ERROR_COUNT = MetaParameter.builder()
			.basicCollect(true)
			.name(HardwareConstants.DEVICE_NOT_READY_ERROR_COUNT_PARAMETER)
			.unit(HardwareConstants.ERROR_COUNT_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final MetaParameter ENDURANCE_REMAINING = MetaParameter.builder()
			.basicCollect(true)
			.name(HardwareConstants.ENDURANCE_REMAINING_PARAMETER)
			.unit(HardwareConstants.PERCENT_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final MetaParameter HARD_ERROR_COUNT= MetaParameter.builder()
			.basicCollect(true)
			.name(HardwareConstants.HARD_ERROR_COUNT_PARAMETER)
			.unit(HardwareConstants.ERROR_COUNT_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final MetaParameter ILLEGAL_REQUEST_ERROR_COUNT= MetaParameter.builder()
			.basicCollect(true)
			.name(HardwareConstants.ILLEGAL_REQUEST_ERROR_COUNT_PARAMETER)
			.unit(HardwareConstants.ERROR_COUNT_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final MetaParameter MEDIA_ERROR_COUNT= MetaParameter.builder()
			.basicCollect(true)
			.name(HardwareConstants.MEDIA_ERROR_COUNT_PARAMETER)
			.unit(HardwareConstants.ERROR_COUNT_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final MetaParameter NO_DEVICE_ERROR_COUNT = MetaParameter.builder()
			.basicCollect(true)
			.name(HardwareConstants.NO_DEVICE_ERROR_COUNT_PARAMETER)
			.unit(HardwareConstants.ERROR_COUNT_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final MetaParameter RECOVERABLE_ERROR_COUNT = MetaParameter.builder()
			.basicCollect(true)
			.name(HardwareConstants.RECOVERABLE_ERROR_COUNT_PARAMETER)
			.unit(HardwareConstants.ERROR_COUNT_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final MetaParameter TRANSPORT_ERROR_COUNT = MetaParameter.builder()
			.basicCollect(true)
			.name(HardwareConstants.TRANSPORT_ERROR_COUNT_PARAMETER)
			.unit(HardwareConstants.ERROR_COUNT_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	private static final List<String> METADATA = List.of(DEVICE_ID, SERIAL_NUMBER, VENDOR, MODEL, FIRMWARE_VERSION, SIZE,
			ADDITIONAL_INFORMATION1, ADDITIONAL_INFORMATION2, ADDITIONAL_INFORMATION3);

	private static final Map<String, MetaParameter> META_PARAMETERS;

	static {
		final Map<String, MetaParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(HardwareConstants.STATUS_PARAMETER, STATUS);
		map.put(HardwareConstants.PRESENT_PARAMETER, PRESENT);
		map.put(HardwareConstants.INTRUSION_STATUS_PARAMETER, INTRUSION_STATUS);
		map.put(HardwareConstants.DEVICE_NOT_READY_ERROR_COUNT_PARAMETER, DEVICE_NOT_READY_ERROR_COUNT);
		map.put(HardwareConstants.ENDURANCE_REMAINING_PARAMETER, ENDURANCE_REMAINING);
		map.put(HardwareConstants.ERROR_COUNT_PARAMETER, ERROR_COUNT);
		map.put(HardwareConstants.HARD_ERROR_COUNT_PARAMETER, HARD_ERROR_COUNT);
		map.put(HardwareConstants.ILLEGAL_REQUEST_ERROR_COUNT_PARAMETER, ILLEGAL_REQUEST_ERROR_COUNT);
		map.put(HardwareConstants.MEDIA_ERROR_COUNT_PARAMETER, MEDIA_ERROR_COUNT);
		map.put(HardwareConstants.NO_DEVICE_ERROR_COUNT_PARAMETER, NO_DEVICE_ERROR_COUNT);
		map.put(HardwareConstants.PREDICTED_FAILURE_PARAMETER, PREDICTED_FAILURE);
		map.put(HardwareConstants.RECOVERABLE_ERROR_COUNT_PARAMETER, RECOVERABLE_ERROR_COUNT);
		map.put(HardwareConstants.TRANSPORT_ERROR_COUNT_PARAMETER, TRANSPORT_ERROR_COUNT);
		 
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
		return MonitorType.PHYSICAL_DISK;
	}

	@Override
	public List<String> getMetadata() {
		return METADATA;
	}
}