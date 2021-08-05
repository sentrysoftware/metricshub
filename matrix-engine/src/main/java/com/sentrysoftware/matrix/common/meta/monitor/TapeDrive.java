package com.sentrysoftware.matrix.common.meta.monitor;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION1;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION2;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION3;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DEVICE_ID;
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

public class TapeDrive implements IMetaMonitor {

	public static final MetaParameter MOUNT_COUNT = MetaParameter.builder()
			.basicCollect(true)
			.name(HardwareConstants.MOUNT_COUNT_PARAMETER)
			.unit(HardwareConstants.MOUNT_COUNT_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final MetaParameter NEEDS_CLEANING = MetaParameter.builder()
			.basicCollect(true)
			.name(HardwareConstants.NEEDS_CLEANING_PARAMETER)
			.unit(HardwareConstants.NEEDS_CLEANING_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final MetaParameter UNMOUNT_COUNT = MetaParameter.builder()
			.basicCollect(true)
			.name(HardwareConstants.UNMOUNT_COUNT_PARAMETER)
			.unit(HardwareConstants.UNMOUNT_COUNT_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	private static final List<String> METADATA = List.of(DEVICE_ID, SERIAL_NUMBER, VENDOR, MODEL, ADDITIONAL_INFORMATION1,
			ADDITIONAL_INFORMATION2, ADDITIONAL_INFORMATION3);

	private static final Map<String, MetaParameter> META_PARAMETERS;

	static {
		final Map<String, MetaParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(HardwareConstants.STATUS_PARAMETER, STATUS);
		map.put(HardwareConstants.PRESENT_PARAMETER, PRESENT);
		map.put(HardwareConstants.ERROR_COUNT_PARAMETER, ERROR_COUNT);
		map.put(HardwareConstants.MOUNT_COUNT_PARAMETER, MOUNT_COUNT);
		map.put(HardwareConstants.NEEDS_CLEANING_PARAMETER, NEEDS_CLEANING);
		map.put(HardwareConstants.UNMOUNT_COUNT_PARAMETER, UNMOUNT_COUNT);

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
		return MonitorType.TAPE_DRIVE;
	}

	@Override
	public List<String> getMetadata() {
		return METADATA;
	}
}