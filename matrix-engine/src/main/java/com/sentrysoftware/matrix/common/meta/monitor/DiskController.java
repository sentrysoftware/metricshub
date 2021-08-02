package com.sentrysoftware.matrix.common.meta.monitor;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION1;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION2;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION3;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.BIOS_VERSION;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DEVICE_ID;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DRIVER_VERSION;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.FIRMWARE_VERSION;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MODEL;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SERIAL_NUMBER;
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

public class DiskController implements IMetaMonitor {

	public static final MetaParameter BATTERY_STATUS = MetaParameter.builder()
			.basicCollect(true)
			.name(HardwareConstants.BATTERY_STATUS_PARAMETER)
			.unit(HardwareConstants.STATUS_PARAMETER_UNIT)
			.type(ParameterType.STATUS)
			.build();

	public static final MetaParameter CONTROLLER_STATUS = MetaParameter.builder()
			.basicCollect(true)
			.name(HardwareConstants.CONTROLLER_STATUS_PARAMETER)
			.unit(HardwareConstants.STATUS_PARAMETER_UNIT)
			.type(ParameterType.STATUS)
			.build();

	private static final List<String> METADATA = List.of(DEVICE_ID, SERIAL_NUMBER, VENDOR, MODEL, BIOS_VERSION,
			FIRMWARE_VERSION, DRIVER_VERSION, ADDITIONAL_INFORMATION1, ADDITIONAL_INFORMATION2, ADDITIONAL_INFORMATION3);

	private static final Map<String, MetaParameter> META_PARAMETERS;

	static {
		final Map<String, MetaParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(HardwareConstants.STATUS_PARAMETER, STATUS);
		map.put(HardwareConstants.PRESENT_PARAMETER, PRESENT);
		map.put(HardwareConstants.BATTERY_STATUS_PARAMETER, BATTERY_STATUS);
		map.put(HardwareConstants.CONTROLLER_STATUS_PARAMETER, CONTROLLER_STATUS);

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
		return MonitorType.DISK_CONTROLLER;
	}

	@Override
	public List<String> getMetadata() {
		return METADATA;
	}
}