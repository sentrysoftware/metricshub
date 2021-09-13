package com.sentrysoftware.matrix.engine.strategy.collect;

import com.sentrysoftware.matrix.common.helpers.ArrayHelper;
import com.sentrysoftware.matrix.common.helpers.NumberHelper;
import com.sentrysoftware.matrix.common.meta.monitor.Battery;
import com.sentrysoftware.matrix.common.meta.monitor.Blade;
import com.sentrysoftware.matrix.common.meta.monitor.Cpu;
import com.sentrysoftware.matrix.common.meta.monitor.CpuCore;
import com.sentrysoftware.matrix.common.meta.monitor.DiskController;
import com.sentrysoftware.matrix.common.meta.monitor.Enclosure;
import com.sentrysoftware.matrix.common.meta.monitor.Fan;
import com.sentrysoftware.matrix.common.meta.monitor.IMetaMonitor;
import com.sentrysoftware.matrix.common.meta.monitor.Led;
import com.sentrysoftware.matrix.common.meta.monitor.LogicalDisk;
import com.sentrysoftware.matrix.common.meta.monitor.Lun;
import com.sentrysoftware.matrix.common.meta.monitor.Memory;
import com.sentrysoftware.matrix.common.meta.monitor.MetaConnector;
import com.sentrysoftware.matrix.common.meta.monitor.NetworkCard;
import com.sentrysoftware.matrix.common.meta.monitor.OtherDevice;
import com.sentrysoftware.matrix.common.meta.monitor.PhysicalDisk;
import com.sentrysoftware.matrix.common.meta.monitor.PowerSupply;
import com.sentrysoftware.matrix.common.meta.monitor.Robotics;
import com.sentrysoftware.matrix.common.meta.monitor.TapeDrive;
import com.sentrysoftware.matrix.common.meta.monitor.Target;
import com.sentrysoftware.matrix.common.meta.monitor.Temperature;
import com.sentrysoftware.matrix.common.meta.monitor.Voltage;
import com.sentrysoftware.matrix.common.meta.parameter.MetaParameter;
import com.sentrysoftware.matrix.common.meta.parameter.ParameterType;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.strategy.IMonitorVisitor;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.parameter.IParameterValue;
import com.sentrysoftware.matrix.model.parameter.ParameterState;
import com.sentrysoftware.matrix.model.parameter.StatusParam;
import com.sentrysoftware.matrix.model.parameter.TextParam;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION1;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION2;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION3;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ALARM_ON_COLOR;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.AVAILABLE_PATH_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.AVAILABLE_PATH_INFORMATION_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.BANDWIDTH_UTILIZATION_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.BATTERY_STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.BLINKING_STATUS;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.BYTES_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.BYTES_RATE_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.CHARGE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.COLOR_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.CONTROLLER_STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.CORRECTED_ERROR_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.CURRENT_SPEED_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DUPLEX_MODE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DUPLEX_MODE_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENDURANCE_REMAINING_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_USAGE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_COUNT_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_PERCENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.INTRUSION_STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.LAST_ERROR_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.LED_INDICATOR_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.LINK_SPEED_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.LINK_STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MODEL;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MOUNT_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MOUNT_COUNT_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MOVE_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MOVE_COUNT_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.NEEDS_CLEANING_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.NEW_LINE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.OFF_STATUS;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ON_STATUS;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PACKETS_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PACKETS_RATE_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PERCENT_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_CONSUMPTION_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_STATE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_SUPPLY_POWER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PREDICTED_FAILURE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PRESENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.RECEIVED_BYTES_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.RECEIVED_BYTES_RATE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.RECEIVED_PACKETS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.RECEIVED_PACKETS_RATE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SPACE_GB_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SPEED_MBITS_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SPEED_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SPEED_PERCENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STARTING_ERROR_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STATUS_INFORMATION_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STATUS_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TEMPERATURE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TEMPERATURE_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TEST_REPORT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TIME_LEFT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TIME_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TOTAL_PACKETS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TRANSMITTED_BYTES_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TRANSMITTED_BYTES_RATE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TRANSMITTED_PACKETS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TRANSMITTED_PACKETS_RATE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.UNALLOCATED_SPACE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.UNMOUNT_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.UNMOUNT_COUNT_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.USAGE_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.USAGE_REPORT_RECEIVED_BYTES_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.USAGE_REPORT_RECEIVED_PACKETS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.USAGE_REPORT_TRANSMITTED_BYTES_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.USAGE_REPORT_TRANSMITTED_PACKETS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.USED_CAPACITY_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.USED_PERCENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.USED_TIME_PERCENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.USED_WATTS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.VALUE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.VOLTAGE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.VOLTAGE_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.WARNING_ON_COLOR;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ZERO_BUFFER_CREDIT_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ZERO_BUFFER_CREDIT_COUNT_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ZERO_BUFFER_CREDIT_PERCENT_PARAMETER;

@Slf4j
public class MonitorCollectVisitor implements IMonitorVisitor {

	private static final String MONITOR_TYPE_CANNOT_BE_NULL = "monitorType cannot be null";
	private static final String VALUE_TABLE_CANNOT_BE_NULL = "valueTable cannot be null";
	private static final String DATA_CANNOT_BE_NULL = "row cannot be null.";
	private static final String MONITOR_COLLECT_INFO_CANNOT_BE_NULL = "monitorCollectInfo cannot be null.";
	private static final String CONNECTOR_NAME_CANNOT_BE_NULL = "connectorName cannot be null.";
	private static final String HOST_MONITORING_CANNOT_BE_NULL = "hostMonitoring cannot be null.";
	private static final String HOSTNAME_CANNOT_BE_NULL = "hostname cannot be null.";
	private static final String MAPPING_CANNOT_BE_NULL = "mapping cannot be null.";
	private static final String MONITOR_CANNOT_BE_NULL = "monitor cannot be null.";
	private static final String COLLECT_TIME_CANNOT_BE_NULL = "collectTime cannot be null.";
	private static final String UNKNOWN_STATUS_CANNOT_BE_NULL = "unknownStatus cannot be null.";

	private MonitorCollectInfo monitorCollectInfo;

	private static final Map<String, Function<ParameterState, String>> STATUS_INFORMATION_MAP;

	static {

		final Map<String, Function<ParameterState, String>> map = new HashMap<>();
		map.put(INTRUSION_STATUS_PARAMETER, MonitorCollectVisitor::getIntrusionStatusInformation);
		STATUS_INFORMATION_MAP = Collections.unmodifiableMap(map);
	}

	public MonitorCollectVisitor(MonitorCollectInfo monitorCollectInfo) {
		Assert.notNull(monitorCollectInfo, MONITOR_COLLECT_INFO_CANNOT_BE_NULL);
		checkCollectInfo(monitorCollectInfo);
		this.monitorCollectInfo = monitorCollectInfo;
	}
	

	private void checkCollectInfo(MonitorCollectInfo monitorCollectInfo) {
		Assert.notNull(monitorCollectInfo.getMonitor(), MONITOR_CANNOT_BE_NULL);
		Assert.notNull(monitorCollectInfo.getConnectorName(), CONNECTOR_NAME_CANNOT_BE_NULL);
		Assert.notNull(monitorCollectInfo.getRow(), DATA_CANNOT_BE_NULL);
		Assert.notNull(monitorCollectInfo.getHostMonitoring(), HOST_MONITORING_CANNOT_BE_NULL);
		Assert.notNull(monitorCollectInfo.getHostname(), HOSTNAME_CANNOT_BE_NULL);
		Assert.notNull(monitorCollectInfo.getMapping(), MAPPING_CANNOT_BE_NULL);
		Assert.notNull(monitorCollectInfo.getValueTable(), VALUE_TABLE_CANNOT_BE_NULL);
		Assert.notNull(monitorCollectInfo.getCollectTime(), COLLECT_TIME_CANNOT_BE_NULL);
		Assert.notNull(monitorCollectInfo.getUnknownStatus(), UNKNOWN_STATUS_CANNOT_BE_NULL);
	}

	@Override
	public void visit(MetaConnector metaConnector) {
		collectBasicParameters(metaConnector);

		appendValuesToStatusParameter(TEST_REPORT_PARAMETER);
	}

	@Override
	public void visit(Target target) {
		// Not implemented yet
	}

	@Override
	public void visit(Battery battery) {

		collectBasicParameters(battery);

		collectBatteryCharge();
		collectBatteryTimeLeft();

		appendValuesToStatusParameter(
			PRESENT_PARAMETER,
			CHARGE_PARAMETER,
			TIME_LEFT_PARAMETER);
	}

	@Override
	public void visit(Blade blade) {
		collectBasicParameters(blade);

		appendValuesToStatusParameter(
				POWER_STATE_PARAMETER,
				PRESENT_PARAMETER);
	}

	@Override
	public void visit(Cpu cpu) {
		collectBasicParameters(cpu);

		appendValuesToStatusParameter(
				CORRECTED_ERROR_COUNT_PARAMETER,
				CURRENT_SPEED_PARAMETER,
				PREDICTED_FAILURE_PARAMETER,
				PRESENT_PARAMETER);
	}

	@Override
	public void visit(CpuCore cpuCore) {

		collectBasicParameters(cpuCore);

		collectCpuCoreUsedTimePercent();

		appendValuesToStatusParameter(
				CURRENT_SPEED_PARAMETER,
				USED_TIME_PERCENT_PARAMETER,
				PRESENT_PARAMETER);
	}

	@Override
	public void visit(DiskController diskController) {
		collectBasicParameters(diskController);

		appendValuesToStatusParameter(
				PRESENT_PARAMETER,
				BATTERY_STATUS_PARAMETER,
				CONTROLLER_STATUS_PARAMETER);

		estimateDiskControllerPowerConsumption();
	}

	@Override
	public void visit(Enclosure enclosure) {
		collectBasicParameters(enclosure);

		collectPowerConsumption();

		appendValuesToStatusParameter(
				PRESENT_PARAMETER,
				INTRUSION_STATUS_PARAMETER,
				ENERGY_USAGE_PARAMETER,
				POWER_CONSUMPTION_PARAMETER);
	}

	@Override
	public void visit(Fan fan) {
		collectBasicParameters(fan);

		appendValuesToStatusParameter(
				SPEED_PARAMETER,
				PRESENT_PARAMETER,
				SPEED_PERCENT_PARAMETER);

		estimateFanPowerConsumption();
	}

	@Override
	public void visit(Led led) {

		collectBasicParameters(led);
		collectLedColor();
		collectLedStatus();

		appendValuesToStatusParameter(
				COLOR_PARAMETER,
				LED_INDICATOR_PARAMETER);
	}

	@Override
	public void visit(LogicalDisk logicalDisk) {
		collectBasicParameters(logicalDisk);

		collectErrorCount();
		collectLogicalDiskUnallocatedSpace();
		
		appendValuesToStatusParameter(
				ERROR_COUNT_PARAMETER,
				UNALLOCATED_SPACE_PARAMETER,
				LAST_ERROR_PARAMETER);
	}

	@Override
	public void visit(Lun lun) {
		collectBasicParameters(lun);

		appendValuesToStatusParameter(
				AVAILABLE_PATH_COUNT_PARAMETER,
				AVAILABLE_PATH_INFORMATION_PARAMETER);
	}

	@Override
	public void visit(Memory memory) {
		collectBasicParameters(memory);

		collectErrorCount();

		appendValuesToStatusParameter(
				ERROR_COUNT_PARAMETER,
				ERROR_STATUS_PARAMETER,
				PREDICTED_FAILURE_PARAMETER,
				PRESENT_PARAMETER,
				LAST_ERROR_PARAMETER);

		estimateMemoryPowerConsumption();
	}

	@Override
	public void visit(NetworkCard networkCard) {
		collectBasicParameters(networkCard);
		
		final Double duplexMode = collectNetworkCardDuplexMode();
		final Double linkSpeed = collectNetworkCardLinkSpeed();
		final Double receivedBytesRate = collectNetworkCardBytesRate(
			RECEIVED_BYTES_PARAMETER,
			RECEIVED_BYTES_RATE_PARAMETER,
			USAGE_REPORT_RECEIVED_BYTES_PARAMETER
		);
		final Double transmittedBytesRate = collectNetworkCardBytesRate(
			TRANSMITTED_BYTES_PARAMETER,
			TRANSMITTED_BYTES_RATE_PARAMETER,
			USAGE_REPORT_TRANSMITTED_BYTES_PARAMETER
		);

		collectNetworkCardBandwidthUtilization(duplexMode, linkSpeed, receivedBytesRate,transmittedBytesRate);

		final Double receivedPackets = collectNetworkCardPacketsRate(
			RECEIVED_PACKETS_PARAMETER,
			RECEIVED_PACKETS_RATE_PARAMETER,
			USAGE_REPORT_RECEIVED_PACKETS_PARAMETER
		);
		final Double transmittedPackets = collectNetworkCardPacketsRate(
			TRANSMITTED_PACKETS_PARAMETER,
			TRANSMITTED_PACKETS_RATE_PARAMETER,
			USAGE_REPORT_TRANSMITTED_PACKETS_PARAMETER
		);
		collectNetworkCardErrorPercent(receivedPackets, transmittedPackets);
		collectNetworkCardZeroBufferCreditPercent();

		appendValuesToStatusParameter(
				PRESENT_PARAMETER, 
				BANDWIDTH_UTILIZATION_PARAMETER,
				ERROR_COUNT_PARAMETER,
				ERROR_PERCENT_PARAMETER, 
				LINK_SPEED_PARAMETER, 
				LINK_STATUS_PARAMETER, 
				RECEIVED_BYTES_RATE_PARAMETER, 
				RECEIVED_PACKETS_RATE_PARAMETER, 
				TRANSMITTED_BYTES_RATE_PARAMETER, 
				TRANSMITTED_PACKETS_RATE_PARAMETER, 
				ZERO_BUFFER_CREDIT_PERCENT_PARAMETER);

		estimateNetworkCardPowerConsumption();

	}

	@Override
	public void visit(OtherDevice otherDevice) {
		collectBasicParameters(otherDevice);

		appendValuesToStatusParameter(
				PRESENT_PARAMETER,
				USAGE_COUNT_PARAMETER,
				VALUE_PARAMETER);
	}

	@Override
	public void visit(PhysicalDisk physicalDisk) {
		collectBasicParameters(physicalDisk);

		collectPhysicalDiskParameters();

		collectErrorCount();

		appendValuesToStatusParameter(
				PRESENT_PARAMETER,
				USAGE_COUNT_PARAMETER,
				INTRUSION_STATUS_PARAMETER,
				ENDURANCE_REMAINING_PARAMETER,
				ERROR_COUNT_PARAMETER,
				PREDICTED_FAILURE_PARAMETER);

		estimatePhysicalDiskPowerConsumption();
	}

	@Override
	public void visit(PowerSupply powerSupply) {
		collectBasicParameters(powerSupply);
		
		collectPowerSupplyUsedCapacity();

		appendValuesToStatusParameter(
				USED_CAPACITY_PARAMETER,
				PRESENT_PARAMETER,
				MOVE_COUNT_PARAMETER,
				ERROR_COUNT_PARAMETER);
	}
	
	@Override
	public void visit(Robotics robotics) {
		collectBasicParameters(robotics);

		collectIncrementCount(MOVE_COUNT_PARAMETER, MOVE_COUNT_PARAMETER_UNIT);

		collectErrorCount();

		appendValuesToStatusParameter(
				ERROR_COUNT_PARAMETER,
				MOVE_COUNT_PARAMETER);

		estimateRoboticsPowerConsumption();
	}
	
	@Override
	public void visit(TapeDrive tapeDrive) {
		collectBasicParameters(tapeDrive);

		collectIncrementCount(MOUNT_COUNT_PARAMETER, MOUNT_COUNT_PARAMETER_UNIT);

		collectIncrementCount(UNMOUNT_COUNT_PARAMETER, UNMOUNT_COUNT_PARAMETER_UNIT);

		collectErrorCount();

		appendValuesToStatusParameter(
				PRESENT_PARAMETER,
				ERROR_COUNT_PARAMETER, 
				MOUNT_COUNT_PARAMETER, 
				NEEDS_CLEANING_PARAMETER,
				UNMOUNT_COUNT_PARAMETER);

		estimateTapeDrivePowerConsumption();
	}

	@Override
	public void visit(Temperature temperature) {
		collectBasicParameters(temperature);

		collectTemperature();

		appendValuesToStatusParameter(TEMPERATURE_PARAMETER);
	}

	@Override
	public void visit(Voltage voltage) {
		collectBasicParameters(voltage);

		collectVoltage();

		appendValuesToStatusParameter(VOLTAGE_PARAMETER);
	}

	/**
	 * Collect the Status of the current {@link Monitor} instance
	 * 
	 * @param monitorType   The type of the monitor we currently collect
	 * @param parameterName The name of the status parameter to collect
	 * @param unit          The unit to set in the {@link IParameterValue} instance
	 */
	void collectStatusParameter(final MonitorType monitorType, final String parameterName, final String unit) {

		Assert.notNull(monitorType, MONITOR_TYPE_CANNOT_BE_NULL);

		checkCollectInfo(monitorCollectInfo);

		final Monitor monitor = monitorCollectInfo.getMonitor();
		final List<String> row = monitorCollectInfo.getRow();
		final Map<String, String> mapping = monitorCollectInfo.getMapping();
		final String hostname = monitorCollectInfo.getHostname();
		final String valueTable = monitorCollectInfo.getValueTable();
		final ParameterState unknownStatus = monitorCollectInfo.getUnknownStatus();
		final Long collectTime = monitorCollectInfo.getCollectTime();

		// Get the status raw value
		final String status = CollectHelper.getValueTableColumnValue(valueTable,
				parameterName,
				monitorType,
				row,
				mapping.get(parameterName));

		// Translate the status raw value
		final ParameterState state = CollectHelper.translateStatus(status,
				unknownStatus,
				monitor.getId(),
				hostname,
				parameterName);

		if (state == null) {
			log.warn("Could not collect {} for monitor id {}. Hostname {}", parameterName, monitor.getId(), hostname);
			return;
		}

		String statusInformation = null;

		// Get the status information
		if (STATUS_PARAMETER.equals(parameterName)) {
			statusInformation = CollectHelper.getValueTableColumnValue(valueTable,
					STATUS_INFORMATION_PARAMETER,
					monitorType,
					row,
					mapping.get(STATUS_INFORMATION_PARAMETER));
		}

		// Otherwise simply set the state name OK, WARN or ALARM
		if (statusInformation == null || statusInformation.trim().isEmpty()) {
			// Is there any specific implementation for the status information field
			if (STATUS_INFORMATION_MAP.containsKey(parameterName)) {
				statusInformation = STATUS_INFORMATION_MAP.get(parameterName).apply(state);
			} else {
				statusInformation = state.name();
			}
		}

		CollectHelper.updateStatusParameter(monitor, parameterName, unit, collectTime, state, statusInformation);

	}

	/**
	 * Append the given parameter information to the status information
	 * 
	 * @param statusParam The {@link StatusParam} we wish to update its statusInformation field value
	 * @param parameter   The parameter we wish to append its value
	 */
	static void appendToStatusInformation(final StatusParam statusParam, final IParameterValue parameter) {
		if (statusParam == null || parameter == null) {
			return;
		}

		final String value = parameter.formatValueAsString();

		if (value == null) {
			return;
		}

		String existingStatusInformation = statusParam.getStatusInformation();

		if (existingStatusInformation == null) {
			existingStatusInformation = "";
		} else {
			existingStatusInformation += NEW_LINE;
		}

		final StringBuilder builder = new StringBuilder(existingStatusInformation)
				.append(value);

		statusParam.setStatusInformation(builder.toString());
	}

	/**
	 * Get the parameter identified by the given name from the current monitor then append the values to the StatusInformation fiend of the
	 * Status parameter
	 * 
	 * @param parameterNames The name of the parameters we wish to append in the StatusInformation of the Status parameter
	 */
	void appendValuesToStatusParameter(final String... parameterNames) {

		final Monitor monitor = monitorCollectInfo.getMonitor();
		Assert.notNull(monitor, MONITOR_CANNOT_BE_NULL);

		// Cannot be null
		final Map<String, IParameterValue> parameters = monitor.getParameters();

		final StatusParam statusParam = (StatusParam) parameters.get(STATUS_PARAMETER);
		if (statusParam == null) {
			// Nothing to append
			return;
		}

		for (String parameterName : parameterNames) {
			appendToStatusInformation(statusParam, parameters.get(parameterName));
		}
	}

	/**
	 * Collect a number parameter
	 * 
	 * @param monitorType   The type of the monitor we currently collect
	 * @param parameterName The name of the status parameter to collect
	 * @param unit          The unit to set in the {@link IParameterValue} instance
	 */
	void collectNumberParameter(final MonitorType monitorType, final String parameterName, final String unit) {

		Assert.notNull(monitorType, MONITOR_TYPE_CANNOT_BE_NULL);

		checkCollectInfo(monitorCollectInfo);

		final Monitor monitor = monitorCollectInfo.getMonitor();
		final Long collectTime = monitorCollectInfo.getCollectTime();


		final Double value = extractParameterValue(monitorType, parameterName);
		if (value != null) {
			CollectHelper.updateNumberParameter(
				monitor,
				parameterName,
				unit,
				collectTime,
				value,
				value
			);
		}

	}

	/**
	 * Collect the parameter string from the current value
	 * 
	 * @param monitorType   The type of the monitor
	 * @param parameterName The unique name of the parameter
	 * @return {@link String} value
	 */
	void collectTextParameter(@NonNull final String parameterName, final String value) {

		if (value == null) {
			return;
		}

		final Monitor monitor = monitorCollectInfo.getMonitor();

		// Create a text parameter and update the value and the collect time
		final TextParam textParam = TextParam.builder()
				.name(parameterName)
				.value(value)
				.collectTime(monitorCollectInfo.getCollectTime())
				.build();

		monitor.collectParameter(textParam);
	}

	/**
	 * Extract the parameter value from the current row
	 * 
	 * @param monitorType   The type of the monitor
	 * @param parameterName The unique name of the parameter
	 * @return {@link Double} value
	 */
	Double extractParameterValue(@NonNull final MonitorType monitorType, final String parameterName) {

		checkCollectInfo(monitorCollectInfo);

		final Monitor monitor = monitorCollectInfo.getMonitor();
		final List<String> row = monitorCollectInfo.getRow();
		final Map<String, String> mapping = monitorCollectInfo.getMapping();
		final String hostname = monitorCollectInfo.getHostname();
		final String valueTable = monitorCollectInfo.getValueTable();

		// Get the number value as string from the current row
		final String stringValue = CollectHelper.getValueTableColumnValue(valueTable,
				parameterName,
				monitorType,
				row,
				mapping.get(parameterName));


		if (stringValue == null) {
			log.debug("No {} to collect for monitor id {}. Hostname {}", parameterName, monitor.getId(), hostname);
			return null;
		}

		try {
			return Double.parseDouble(stringValue);
		} catch(NumberFormatException e) {
			log.warn("Cannot parse the {} value '{}' for monitor id {}. {} won't be collected",
					parameterName, stringValue, monitor.getId(), parameterName);
		}

		return null;
	}
	
	/**
	 * Extract the parameter string from the current row
	 * 
	 * @param monitorType   The type of the monitor
	 * @param parameterName The unique name of the parameter
	 * @return {@link String} value
	 */
	String extractParameterStringValue(@NonNull final MonitorType monitorType, final String parameterName) {

		checkCollectInfo(monitorCollectInfo);

		// Get the number value as string from the current row
		return CollectHelper.getValueTableColumnValue(
				monitorCollectInfo.getValueTable(),
				parameterName,
				monitorType,
				monitorCollectInfo.getRow(),
				monitorCollectInfo.getMapping().get(parameterName));
	}

	/**
	 * @param parameterState {@link ParameterState#OK}, {@link ParameterState#WARN} or {@link ParameterState#ALARM}
	 * @return a phrase for the intrusion status value
	 */
	static String getIntrusionStatusInformation(final ParameterState parameterState) {
		switch (parameterState) {
		case OK:
			return "No Intrusion Detected";
		case ALARM:
			return "Intrusion Detected";
		default: 
			return "Unexpected Intrusion Status";
		}
	}

	/**
	 * Collect the basic parameters as defined by the given {@link IMetaMonitor}
	 * 
	 * @param metaMonitor Defines all the meta information of the parameters to collect (name, type, unit and basic or not)
	 */
	private void collectBasicParameters(final IMetaMonitor metaMonitor) {

		metaMonitor.getMetaParameters()
		.values()
		.stream()
		.filter(metaParam -> metaParam.isBasicCollect() && ParameterType.STATUS.equals(metaParam.getType()))
		.sorted(new StatusParamFirstComparator())
		.forEach(metaParam -> collectStatusParameter(
			metaMonitor.getMonitorType(),
			metaParam.getName(), metaParam.getUnit()
		));

		metaMonitor.getMetaParameters()
		.values()
		.stream()
		.filter(metaParam -> metaParam.isBasicCollect() && ParameterType.NUMBER.equals(metaParam.getType()))
		.forEach(metaParam -> collectNumberParameter(
			metaMonitor.getMonitorType(), 
			metaParam.getName(),
			metaParam.getUnit()
		));

		metaMonitor.getMetaParameters()
		.values()
		.stream()
		.filter(metaParam -> metaParam.isBasicCollect() && ParameterType.TEXT.equals(metaParam.getType()))
		.forEach(metaParam -> collectTextParameter(
			metaParam.getName(),
			extractParameterStringValue(metaMonitor.getMonitorType(), metaParam.getName())
		));
	}

	/**
	 * Collect the power consumption. <br>
	 * <ol>
	 * <li>If the energyUsage is collected by the connector, we compute the delta energyUsage (Joules) and then the powerConsumption (Watts) based on the
	 * collected delta energyUsage and the collect time.</li>
	 * <li>If the connector collects the powerConsumption, we directly collect the power consumption (Watts) then we compute the energy usage based on the
	 * collected power consumption and the delta collect time</li>
	 * </ol>
	 */
	void collectPowerConsumption() {

		checkCollectInfo(monitorCollectInfo);

		final Monitor monitor = monitorCollectInfo.getMonitor();
		final Long collectTime = monitorCollectInfo.getCollectTime();
		final String hostname = monitorCollectInfo.getHostname();

		// When the connector collects the energy usage,
		// the power consumption will be computed based on the collected energy usage value
		final Double energyUsageRaw = extractParameterValue(monitor.getMonitorType(), ENERGY_USAGE_PARAMETER);
		if (energyUsageRaw != null && energyUsageRaw >= 0) {

			CollectHelper.collectPowerFromEnergyUsage(monitor, collectTime, energyUsageRaw, hostname);
			return;
		}

		// based on the power consumption compute the energy usage
		final Double powerConsumption = extractParameterValue(monitor.getMonitorType(),
				POWER_CONSUMPTION_PARAMETER);
		if (powerConsumption != null && powerConsumption >= 0) {
			CollectHelper.collectEnergyUsageFromPower(monitor, collectTime, powerConsumption, hostname);
		}

	}

	public static class StatusParamFirstComparator implements Comparator<MetaParameter> {

		@Override
		public int compare(final MetaParameter metaParam1, final MetaParameter metaParam2) {
			// Status first
			if (STATUS_PARAMETER.equalsIgnoreCase(metaParam1.getName())) {
				return -1;
			}

			return metaParam1.getName().compareTo(metaParam2.getName());
		}
	}

	/**
	 * Collects the percentage of charge, if the current {@link Monitor} is a {@link Battery}.
	 */
	void collectBatteryCharge() {

		final Monitor monitor = monitorCollectInfo.getMonitor();

		final Double chargeRaw = extractParameterValue(monitor.getMonitorType(), CHARGE_PARAMETER);
		if (chargeRaw != null) {

			CollectHelper.updateNumberParameter(
				monitor,
				CHARGE_PARAMETER,
				PERCENT_PARAMETER_UNIT,
				monitorCollectInfo.getCollectTime(),
				Math.min(chargeRaw, 100.0), // In case the raw value is greater than 100%
				chargeRaw
			);
		}
	}

	/**
	 * Collects the remaining time, in seconds, before the {@link Battery} runs out of power.
	 */
	void collectBatteryTimeLeft() {

		final Monitor monitor = monitorCollectInfo.getMonitor();

		final Double timeLeftRaw = extractParameterValue(monitor.getMonitorType(),
			TIME_LEFT_PARAMETER);

		if (timeLeftRaw != null) {

			CollectHelper.updateNumberParameter(
				monitor,
				TIME_LEFT_PARAMETER,
				TIME_PARAMETER_UNIT,
				monitorCollectInfo.getCollectTime(),
				timeLeftRaw * 60.0, // minutes to seconds
				timeLeftRaw
			);
		}
	}

	/**
	 * Collects the percentage of used time, if the current {@link Monitor} is a {@link CpuCore}.
	 */
	void collectCpuCoreUsedTimePercent() {

		final Monitor monitor = monitorCollectInfo.getMonitor();

		// Getting the current value
		final Double usedTimePercentRaw = extractParameterValue(monitor.getMonitorType(),
			USED_TIME_PERCENT_PARAMETER);

		if (usedTimePercentRaw == null) {
			return;
		}

		// Getting the current value's collect time
		Long collectTime = monitorCollectInfo.getCollectTime();

		// Getting the previous value
		Double usedTimePercentPrevious = CollectHelper.getNumberParamRawValue(monitor,
			USED_TIME_PERCENT_PARAMETER, true);

		if (usedTimePercentPrevious == null) {

			// Setting the current raw value so that it becomes the previous raw value when the next collect occurs
			CollectHelper.updateNumberParameter(
				monitor,
				USED_TIME_PERCENT_PARAMETER,
				PERCENT_PARAMETER_UNIT,
				collectTime,
				null,
				usedTimePercentRaw
			);

			return;
		}

		// Getting the previous value's collect time
		final Double collectTimePrevious = CollectHelper.getNumberParamCollectTime(monitor,
			USED_TIME_PERCENT_PARAMETER, true);

		if (collectTimePrevious == null) {

			// This should never happen
			log.warn("Found previous usedTimePercent value, but could not find previous collect time.");

			return;
		}

		// Computing the value delta
		final Double usedTimePercentDelta = CollectHelper.subtract(USED_TIME_PERCENT_PARAMETER,
			usedTimePercentRaw, usedTimePercentPrevious);

		// Computing the time delta
		final double timeDeltaInSeconds = CollectHelper.subtract(USED_TIME_PERCENT_PARAMETER,
			collectTime.doubleValue(), collectTimePrevious) / 1000.0;

		if (timeDeltaInSeconds == 0.0) {
			return;
		}

		// Setting the parameter
		CollectHelper.updateNumberParameter(monitor,
			USED_TIME_PERCENT_PARAMETER,
			PERCENT_PARAMETER_UNIT,
			collectTime,
			100.0 * usedTimePercentDelta / timeDeltaInSeconds,
			usedTimePercentRaw);
	}

	/**
	 * Collect the voltage value, if the current {@link Monitor} is a {@link Voltage}.
	 */
	void collectVoltage() {
		final Monitor monitor = monitorCollectInfo.getMonitor();

		// Getting the current value
		final Double voltageValue = extractParameterValue(monitor.getMonitorType(),
				VOLTAGE_PARAMETER);

		final Double computedVoltage = (voltageValue != null && voltageValue >= -100000 && voltageValue <= 450000) ? voltageValue : null;

		if (computedVoltage != null ) {
			CollectHelper.updateNumberParameter(
				monitor,
				VOLTAGE_PARAMETER,
				VOLTAGE_PARAMETER_UNIT,
				monitorCollectInfo.getCollectTime(),
				computedVoltage,
				voltageValue
			);
		}
	}

	/**
	 * Collects the error counts
	 */
	void collectErrorCount() {

		final Monitor monitor = monitorCollectInfo.getMonitor();

		Double rawErrorCount = extractParameterValue(monitor.getMonitorType(),
			ERROR_COUNT_PARAMETER);

		if (rawErrorCount != null) {
			double errorCount = 0.0;
			final Double startingErrorCount = CollectHelper.getNumberParamRawValue(
					monitor, STARTING_ERROR_COUNT_PARAMETER, true);

			if (startingErrorCount != null) {
				// Remove existing error count from the current value
				errorCount = rawErrorCount - startingErrorCount;

				// If we obtain a negative number, that's impossible: set everything to 0
				if (errorCount < 0) {
					errorCount = 0.0;

					// Reset the starting error count
					CollectHelper.updateNumberParameter(
							monitor,
							STARTING_ERROR_COUNT_PARAMETER,
							ERROR_COUNT_PARAMETER_UNIT,
							monitorCollectInfo.getCollectTime(),
							0.0,
							0.0
					);
				} else {
					// Copy the last startingErrorCount
					CollectHelper.updateNumberParameter(
							monitor,
							STARTING_ERROR_COUNT_PARAMETER,
							ERROR_COUNT_PARAMETER_UNIT,
							monitorCollectInfo.getCollectTime(),
							startingErrorCount,
							startingErrorCount
					);
				}
			} else {
				// First polling, we're going to pretend that everything is alright and save the existing number of errors
				if (rawErrorCount < 0) {
					rawErrorCount = 0.0;
				}
				CollectHelper.updateNumberParameter(
						monitor,
						STARTING_ERROR_COUNT_PARAMETER,
						ERROR_COUNT_PARAMETER_UNIT,
						monitorCollectInfo.getCollectTime(),
						rawErrorCount,
						rawErrorCount
				);
			}

			CollectHelper.updateNumberParameter(
					monitor,
					ERROR_COUNT_PARAMETER,
					ERROR_COUNT_PARAMETER_UNIT,
					monitorCollectInfo.getCollectTime(),
					errorCount,
					errorCount
			);
		}

	}

	/**
	 * Collects the incremental parameters, namely 
	 * {@link TapeDrive} unmount, mount & {@link Robotics} move count.
	 * 
	 * @param countParameter		The name of the count parameter, like mountCount
	 * @param countParameterUnit	The unit of the count parameter, like mounts
	 */
	void collectIncrementCount(final String countParameter, final String countParameterUnit) {

		final Monitor monitor = monitorCollectInfo.getMonitor();
		final Double rawCount  = extractParameterValue(monitor.getMonitorType(), countParameter);

		if (rawCount != null) {

			// Getting the previous value
			Double previousRawCount = CollectHelper.getNumberParamRawValue(monitor, countParameter, true);
			
			CollectHelper.updateNumberParameter(
				monitor,
				countParameter,
				countParameterUnit,
				monitorCollectInfo.getCollectTime(),
				(previousRawCount != null && previousRawCount < rawCount) ?  (rawCount - previousRawCount) : 0,
				rawCount
			);
		}
	}
	
	/**
	 * Collects the used capacity of {@link PowerSupply}.
	 */
	void collectPowerSupplyUsedCapacity() {

		final Monitor monitor = monitorCollectInfo.getMonitor();

		// Getting the used percent
		Double usedPercent = null;
		final Double usedPercentRaw = extractParameterValue(monitor.getMonitorType(),
			USED_PERCENT_PARAMETER);

		if (usedPercentRaw == null) {
		
			// Getting the used capacity
			final Double powerSupplyUsedWatts = extractParameterValue(monitor.getMonitorType(),
				USED_WATTS_PARAMETER);
			
			// Getting the power
			final Double power = extractParameterValue(monitor.getMonitorType(),
				POWER_SUPPLY_POWER);

			if (powerSupplyUsedWatts  != null && power != null && power > 0) {
				usedPercent = 100.0 * powerSupplyUsedWatts / power;
			}

		} else {
			usedPercent = usedPercentRaw;
		}

		// Update the used capacity, if the usedPercent is valid
		if (usedPercent != null && usedPercent >= 0.0 && usedPercent <= 100.0) {
			CollectHelper.updateNumberParameter(
				monitor,
				USED_CAPACITY_PARAMETER,
				PERCENT_PARAMETER_UNIT,
				monitorCollectInfo.getCollectTime(),
				usedPercent,
				usedPercentRaw
			);
		}
	}
	
	/**
	 * Collects the unallocated space in GB for {@link LogicalDisk}.
	 */
	void collectLogicalDiskUnallocatedSpace() {

		final Monitor monitor = monitorCollectInfo.getMonitor();

		final Double unallocatedSpaceRaw = extractParameterValue(monitor.getMonitorType(),
			UNALLOCATED_SPACE_PARAMETER);

		if (unallocatedSpaceRaw != null) {

			CollectHelper.updateNumberParameter(
				monitor,
				UNALLOCATED_SPACE_PARAMETER,
				SPACE_GB_PARAMETER_UNIT,
				monitorCollectInfo.getCollectTime(),
				unallocatedSpaceRaw / (1024.0 * 1024.0 * 1024.0), // Bytes to GB
				unallocatedSpaceRaw
			);
		}
	}

	/**
	 * Collect the temperature value, if the current {@link Monitor} is a {@link Temperature}.
	 */
	void collectTemperature() {
		final Monitor monitor = monitorCollectInfo.getMonitor();

		// Getting the current value
		final Double temperatureValue = extractParameterValue(monitor.getMonitorType(),
				TEMPERATURE_PARAMETER);

		if (temperatureValue != null && temperatureValue >= -100 && temperatureValue <= 200) {
			CollectHelper.updateNumberParameter(
				monitor,
				TEMPERATURE_PARAMETER,
				TEMPERATURE_PARAMETER_UNIT,
				monitorCollectInfo.getCollectTime(),
				temperatureValue,
				temperatureValue
			);
		}
	}

	/**
	 * Set the power consumption (15W by default for disk controllers) Source:
	 * https://forums.servethehome.com/index.php?threads/raid-controllers-power-consumption.9189/
	 */
	void estimateDiskControllerPowerConsumption() {

		CollectHelper.collectEnergyUsageFromPower(
			monitorCollectInfo.getMonitor(),
			monitorCollectInfo.getCollectTime(),
			15.0,
			monitorCollectInfo.getHostname()
		);
	}

	/**
	 * Estimated power consumption: 4W 
	 * Source: https://www.buildcomputers.net/power-consumption-of-pc-components.html
	 */
	void estimateMemoryPowerConsumption() {
		CollectHelper.collectEnergyUsageFromPower(
			monitorCollectInfo.getMonitor(),
			monitorCollectInfo.getCollectTime(),
			4.0,
			monitorCollectInfo.getHostname()
		);
	}

	/**
	 * Estimates the power dissipation of a network card, based on some characteristics Inspired by:
	 * https://www.cl.cam.ac.uk/~acr31/pubs/sohan-10gbpower.pdf
	 */
	void estimateNetworkCardPowerConsumption() {
		final Monitor monitor = monitorCollectInfo.getMonitor();

		// Network card name
		final String lowerCaseName = monitor.getName().toLowerCase();

		// Link status
		final ParameterState linkStatus = CollectHelper.getStatusParamState(monitor, LINK_STATUS_PARAMETER);

		// Link speed
		final Double linkSpeed = CollectHelper.getNumberParamValue(monitor, LINK_SPEED_PARAMETER);

		// Bandwidth utilization
		final Double bandwidthUtilization = CollectHelper.getNumberParamValue(monitor, BANDWIDTH_UTILIZATION_PARAMETER);

		final double powerConsumption;

		// Virtual or WAN card: 0 (it's not physical)
		if (lowerCaseName.indexOf("wan") >= 0 || lowerCaseName.indexOf("virt") >= 0) {
			powerConsumption = 0.0;
		}

		// Unplugged, means not much
		else if (ParameterState.WARN.equals(linkStatus)) {
			// 1W for an unplugged card
			powerConsumption = 1.0;
		}

		// (0.5 + 0.5 * bandwidthUtilization) * 5 * log10(linkSpeed)
		else if (CollectHelper.isValidPercentage(bandwidthUtilization)) {
			if (CollectHelper.isValidPositive(linkSpeed) && linkSpeed > 10) {
				powerConsumption = (0.5 + 0.5 * bandwidthUtilization / 100.0) * 5.0 * Math.log10(linkSpeed);
			} else {
				powerConsumption = (0.5 + 0.5 * bandwidthUtilization / 100.0) * 5.0;
			}
		}

		// If we have the link speed, we'll go with 0.75 * 5 * log10(linkSpeed)
		else if (CollectHelper.isValidPositive(linkSpeed)) {
			if (linkSpeed > 10) {
				powerConsumption = 0.75 * 5.0 * Math.log10(linkSpeed);
			} else {
				powerConsumption = 2.0;
			}

		} else {
			// Some default value (what about 10W ? wet finger...)
			powerConsumption = 10.0;
		}

		CollectHelper.collectEnergyUsageFromPower(monitor,
				monitorCollectInfo.getCollectTime(),
				NumberHelper.round(powerConsumption, 2, RoundingMode.HALF_UP),
				monitorCollectInfo.getHostname());
	}

	/**
	 * Estimates the power dissipation of a physical disk, based on its characteristics: vendor, model, location, type, etc. all mixed up
	 * Inspiration: https://outervision.com/power-supply-calculator
	 */
	void estimatePhysicalDiskPowerConsumption() {
		// Physical disk
		final Monitor monitor = monitorCollectInfo.getMonitor();

		// Physical disk characteristics
		final List<String> dataList = new ArrayList<>();
		dataList.add(monitor.getName());
		dataList.add(monitor.getMetadata(MODEL));
		dataList.add(monitor.getMetadata(ADDITIONAL_INFORMATION1));
		dataList.add(monitor.getMetadata(ADDITIONAL_INFORMATION2));
		dataList.add(monitor.getMetadata(ADDITIONAL_INFORMATION3));

		final Monitor parent = monitorCollectInfo.getHostMonitoring().findById(monitor.getParentId());
		if (parent != null) {
			dataList.add(parent.getName());
		} else {
			log.error("No parent found for the physical disk identified by: {}. Physical disk name: {}",
					monitor.getId(), monitor.getName());
		}

		final String[] data = dataList.toArray(new String[dataList.size()]);

		final double powerConsumption;

		// SSD
		if (ArrayHelper.anyMatchLowerCase(str -> str.contains("ssd") || str.contains("solid"), data)) {
			powerConsumption = estimateSsdPowerConsumption(data);
		}

		// HDD (non-SSD), depending on the interface
		// SAS
		else if (ArrayHelper.anyMatchLowerCase(str -> str.contains("sas"), data)) {
			powerConsumption = estimateSasPowerConsumption(data);
		}

		// SCSI and IDE
		else if (ArrayHelper.anyMatchLowerCase(str -> str.contains("scsi") || str.contains("ide"), data)) {
			powerConsumption = estimateScsiAndIde(data);
		}

		// SATA (and unknown, we'll assume it's the most common case)
		else {
			powerConsumption  = estimateSataOrDefault(data);
		}

		CollectHelper.collectEnergyUsageFromPower(monitor,
				monitorCollectInfo.getCollectTime(),
				powerConsumption,
				monitorCollectInfo.getHostname());
	}

	/**
	 * Estimate SATA physical disk power dissipation. Default is 11W.
	 * 
	 * @param data the physical disk information
	 * @return double value
	 */
	double estimateSataOrDefault(final String[] data) {

		// Factor in the rotational speed
		if (ArrayHelper.anyMatchLowerCase(str -> str.contains("10k"), data)) {
			return 27.0;
		} else if (ArrayHelper.anyMatchLowerCase(str -> str.contains("15k"), data)) {
			return 32.0;
		} else if (ArrayHelper.anyMatchLowerCase(str -> str.contains("5400") || str.contains("5.4"), data)) {
			return 7.0;
		}

		// Default for 7200-RPM disks
		return 11.0;

	}

	/**
	 * Estimate SCSI and IDE physical disk power dissipation
	 * 
	 * @param data the physical disk information
	 * @return double value
	 */
	double estimateScsiAndIde(final String[] data) {
		// SCSI and IDE
		// Factor in the rotational speed
		if (ArrayHelper.anyMatchLowerCase(str -> str.contains("10k"), data)) {
			// Only SCSI supports 10k
			return 32.0;
		} else if (ArrayHelper.anyMatchLowerCase(str -> str.contains("15k"), data)) {
			// Only SCSI supports 15k
			return 35.0;
		} else if (ArrayHelper.anyMatchLowerCase(str -> str.contains("5400") || str.contains("5.4"), data)) {
			// Likely to be cheap IDE
			return 19;
		}

		// Default for 7200-rpm disks, SCSI or IDE, who knows?
		// SCSI is 31 watts, IDE is 21...
		return 30.0;
	}

	/**
	 * Estimate SAS physical disk power dissipation
	 * 
	 * @param data the physical disk information
	 * @return double value
	 */
	double estimateSasPowerConsumption(final String[] data) {
		// Factor in the rotationnal speed
		if (ArrayHelper.anyMatchLowerCase(str -> str.contains("15k"), data)) {
			return 17.0;
		}
		// Default for 10k-rpm disks (rarely lower than that anyway)
		return 12.0;
	}

	/**
	 * Estimate SSD physical disk power dissipation
	 * 
	 * @param data the physical disk information
	 * @return double value
	 */
	double estimateSsdPowerConsumption(final String[] data) {
		if (ArrayHelper.anyMatchLowerCase(str -> str.contains("pcie"), data)) {
			return 18.0;
		} else if (ArrayHelper.anyMatchLowerCase(str -> str.contains("nvm"), data)) {
			return  6.0;
		}
		return 3.0;
	}

	/**
	 * Calculate the approximate power consumption of the media changer.<br>
	 * If it moved, 154W, if not, 48W Source:
	 * https://docs.oracle.com/en/storage/tape-storage/sl4000/slklg/calculate-total-power-consumption.html
	 */
	void estimateRoboticsPowerConsumption() {
		final Monitor monitor = monitorCollectInfo.getMonitor();

		final Double moveCount = CollectHelper.getNumberParamValue(monitor, MOVE_COUNT_PARAMETER);

		final double powerConsumption;
		if (moveCount != null && moveCount > 0.0) {
			powerConsumption = 154.0;
		} else {
			powerConsumption = 48.0;
		}

		CollectHelper.collectEnergyUsageFromPower(monitor,
				monitorCollectInfo.getCollectTime(),
				powerConsumption,
				monitorCollectInfo.getHostname());
	}

	/**
	 * Estimates the power consumed by a tape drive for its operation, based on some of its characteristics and activity Inspiration:
	 * https://docs.oracle.com/en/storage/tape-storage/sl4000/slklg/calculate-total-power-consumption.html
	 * https://www.ibm.com/support/knowledgecenter/STQRQ9/com.ibm.storage.ts4500.doc/ts4500_power_consumption_and_cooling_requirements.html
	 */
	void estimateTapeDrivePowerConsumption() {
		final Monitor monitor = monitorCollectInfo.getMonitor();

		Double mountCount = CollectHelper.getNumberParamValue(monitor, MOUNT_COUNT_PARAMETER);
		mountCount = mountCount != null ? mountCount : 0.0;

		Double unmountCount = CollectHelper.getNumberParamValue(monitor, UNMOUNT_COUNT_PARAMETER);
		unmountCount = unmountCount != null ? unmountCount : 0.0;

		final boolean active = mountCount + unmountCount > 0;
		final String lowerCaseName = monitor.getName().toLowerCase();

		final double powerConsumption = estimateTapeDrivePowerConsumption(active, lowerCaseName);

		CollectHelper.collectEnergyUsageFromPower(monitor,
				monitorCollectInfo.getCollectTime(),
				powerConsumption,
				monitorCollectInfo.getHostname());
	}

	/**
	 * Estimate the tape drive power consumption based on its name and its activity
	 * 
	 * @param active        Whether the tape drive is active or not
	 * @param lowerCaseName The name of the tape drive in lower case
	 * @return double value
	 */
	double estimateTapeDrivePowerConsumption(final boolean active, final String lowerCaseName) {

		if (lowerCaseName.indexOf("lto") >= 0) {
			return active ? 46 : 30;
		} else if (lowerCaseName.indexOf("t10000d") >= 0) {
			return active ? 127 : 64;
		} else if (lowerCaseName.indexOf("t10000") >= 0) {
			return active ? 93 : 61;
		} else if (lowerCaseName.indexOf("ts") >= 0) {
			return active ? 53 : 35;
		}

		return active ? 80 : 55;
	}

	/**
	 * Collects the power consumption from {@link Fan} speed.
	 */
	void estimateFanPowerConsumption() {

		final Monitor monitor = monitorCollectInfo.getMonitor();

		// Approximately 5 Watt for standard fan
		double powerConsumption = 5.0;

		final Double fanSpeed = extractParameterValue(monitor.getMonitorType(),
			SPEED_PARAMETER);

		if (fanSpeed != null) {
			// 1000 RPM = 1 Watt
			powerConsumption = fanSpeed / 1000.0;
		} else {
			final Double fanSpeedPercent = extractParameterValue(monitor.getMonitorType(),
					SPEED_PERCENT_PARAMETER);
			
			if (fanSpeedPercent != null) {
				// Approximately 5 Watt for 100%
				powerConsumption = fanSpeedPercent * 0.05;
			}
		}

		CollectHelper.collectEnergyUsageFromPower(monitor,
				monitorCollectInfo.getCollectTime(),
				NumberHelper.round(powerConsumption, 2, RoundingMode.HALF_UP),
				monitorCollectInfo.getHostname());
	}

	/**
	 * Collect the physical disks specific parameters.
	 */
	void collectPhysicalDiskParameters() {
		final Monitor monitor = monitorCollectInfo.getMonitor();

		// Getting the endurance remaining current value
		final Double rawEnduranceRemaining = extractParameterValue(monitor.getMonitorType(),
				ENDURANCE_REMAINING_PARAMETER);

		if (rawEnduranceRemaining != null && rawEnduranceRemaining >= 0 && rawEnduranceRemaining <= 100) {
			CollectHelper.updateNumberParameter(
				monitor,
				ENDURANCE_REMAINING_PARAMETER,
				PERCENT_PARAMETER_UNIT,
				monitorCollectInfo.getCollectTime(),
				rawEnduranceRemaining,
				rawEnduranceRemaining
			);
		}
	}

	/**
	 * Collects the color status for a {@link Led}.
	 */
	void collectLedColor() {

		// Getting the raw color from the current row
		final String colorRaw = CollectHelper.getValueTableColumnValue(monitorCollectInfo.getValueTable(),
			COLOR_PARAMETER,
			MonitorType.LED,
			monitorCollectInfo.getRow(),
			monitorCollectInfo.getMapping().get(COLOR_PARAMETER));

		if (colorRaw != null) {

			final Monitor monitor = monitorCollectInfo.getMonitor();

			// Getting the color status
			Map<String, String> metadata = monitor.getMetadata();
			String warningOnColor = metadata.get(WARNING_ON_COLOR);
			String alarmOnColor = metadata.get(ALARM_ON_COLOR);

			String colorStatus;
			if (warningOnColor != null && warningOnColor.toUpperCase().contains(colorRaw.toUpperCase())) {
				colorStatus = "1";
			} else if (alarmOnColor != null && alarmOnColor.toUpperCase().contains(colorRaw.toUpperCase())) {
				colorStatus = "2";
			} else {
				colorStatus = "0";
			}

			// Translating the color status
			ParameterState colorState = CollectHelper.translateStatus(colorStatus,
				monitorCollectInfo.getUnknownStatus(), monitor.getId(), monitorCollectInfo.getHostname(),
				COLOR_PARAMETER);

			// colorState is never null here
			CollectHelper.updateStatusParameter(monitor,
				COLOR_PARAMETER,
				STATUS_PARAMETER_UNIT,
				monitorCollectInfo.getCollectTime(),
				colorState,
				colorState.name());
		}
	}

	/**
	 * Collects the status for a {@link Led}.
	 */
	void collectLedStatus() {

		// Getting the raw status from the current row
		final String statusRaw = CollectHelper.getValueTableColumnValue(monitorCollectInfo.getValueTable(),
			STATUS_PARAMETER,
			MonitorType.LED,
			monitorCollectInfo.getRow(),
			monitorCollectInfo.getMapping().get(STATUS_PARAMETER));

		if (statusRaw != null) {

			final Monitor monitor = monitorCollectInfo.getMonitor();

			// Translating the status
			Map<String, String> metadata = monitor.getMetadata();

			String preTranslatedStatus;
			switch (statusRaw.toUpperCase()) {
				case "ON":
					preTranslatedStatus = metadata.get(ON_STATUS);
					break;
				case "BLINKING":
					preTranslatedStatus = metadata.get(BLINKING_STATUS);
					break;
				case "OFF":
				default:
					preTranslatedStatus = metadata.get(OFF_STATUS);
			}

			ParameterState translatedStatus = CollectHelper.translateStatus(preTranslatedStatus,
				monitorCollectInfo.getUnknownStatus(), monitor.getId(), monitorCollectInfo.getHostname(),
				STATUS_PARAMETER);

			if (translatedStatus != null) {

				CollectHelper.updateStatusParameter(monitor,
					STATUS_PARAMETER,
					STATUS_PARAMETER_UNIT,
					monitorCollectInfo.getCollectTime(),
					translatedStatus,
					translatedStatus.name());
			}
		}
	}
	
	/**
	 * Collect the {@link NetworkCard} duplex mode parameter.
	 */
	Double collectNetworkCardDuplexMode() {
		final Monitor monitor = monitorCollectInfo.getMonitor();

		// Getting the duplex mode
		final String duplexModeRaw = extractParameterStringValue(monitor.getMonitorType(),
				DUPLEX_MODE_PARAMETER);

		if (duplexModeRaw != null) {

			final Double duplexMode = (duplexModeRaw.equalsIgnoreCase("yes") || 
					duplexModeRaw.equalsIgnoreCase("full") || duplexModeRaw.equalsIgnoreCase("1")) ? 1D : 0D;
			CollectHelper.updateNumberParameter(
					monitor,
					DUPLEX_MODE_PARAMETER,
					DUPLEX_MODE_PARAMETER_UNIT,
					monitorCollectInfo.getCollectTime(),
					duplexMode,
					duplexMode
			);

			return duplexMode;
		}

		CollectHelper.updateNumberParameter(
				monitor,
				DUPLEX_MODE_PARAMETER,
				DUPLEX_MODE_PARAMETER_UNIT,
				monitorCollectInfo.getCollectTime(),
				0D,
				0D
		);

		return null;
	}
	
	/**
	 * Collect the {@link NetworkCard} link speed parameter.
	 */
	Double collectNetworkCardLinkSpeed() {
		final Monitor monitor = monitorCollectInfo.getMonitor();

		// Getting the link speed
		final Double linkSpeed = extractParameterValue(monitor.getMonitorType(),
				LINK_SPEED_PARAMETER);

		if (linkSpeed != null && linkSpeed >= 0) {
			CollectHelper.updateNumberParameter(
					monitor,
					LINK_SPEED_PARAMETER,
					SPEED_MBITS_PARAMETER_UNIT,
					monitorCollectInfo.getCollectTime(),
					linkSpeed,
					linkSpeed
			);
		}

		return linkSpeed;
	}
	
	/**
	 * Collects the {@link NetworkCard} bytes rate and usage.
	 * 
	 * @param bytesParameterName       The name of the bytes parameter where the raw value is collected
	 * @param byteRateParameterName    The name of the byte rate parameter to be calculated
	 * @param usageReportParameterName The name of the usage report parameter to be calculated
	 * 
	 * @return bytesRate               Calculated byte rate in MB/s
	 */
	Double collectNetworkCardBytesRate(final String bytesParameterName, final String byteRateParameterName, final String usageReportParameterName) {

		final Monitor monitor = monitorCollectInfo.getMonitor();

		// Getting the current value
		final Double bytesValue = extractParameterValue(monitor.getMonitorType(), bytesParameterName);
		if (bytesValue == null) {
			return null;
		}

		// Getting the current value's collect time
		Long collectTime = monitorCollectInfo.getCollectTime();

		// Setting the bytes parameter
		CollectHelper.updateNumberParameter(
				monitor,
				bytesParameterName,
				BYTES_PARAMETER_UNIT,
				collectTime,
				bytesValue,
				bytesValue
		);
		
		// Getting the previous value
		Double lastBytesValue = CollectHelper.getNumberParamRawValue(monitor, bytesParameterName, true);
		if (lastBytesValue == null) {
			log.warn("No last bytes value to calculate the byte rate or usage.");
			return null;
		}

		// Getting the previous value's collect time
		final Double collectTimePrevious = CollectHelper.getNumberParamCollectTime(monitor, bytesParameterName, true);
		if (collectTimePrevious == null) {
			// This should never happen
			log.warn("Found previous bytes value, but could not find previous collect time.");
			return null;
		}

		// Computing the value delta (in MBytes)
		final Double bytesDelta = CollectHelper.subtract(bytesParameterName, bytesValue, lastBytesValue);
		if (bytesDelta == null) {
			log.warn("Found decreasing bytes count - must have been reset.");
			return null;
		}
		final double bytesDeltaMb = bytesDelta / 1048576.0;

		// Byte rate
		Double bytesRate = null;

		// Computing the time delta (in seconds)
		final Double timeDeltaMs = CollectHelper.subtract(bytesParameterName, collectTime.doubleValue(), collectTimePrevious);
		if (timeDeltaMs == null || timeDeltaMs == 0.0) {
			log.warn("No denominator for collect time difference to calculate the byte rate.");
		} else {
			final double timeDelta = timeDeltaMs / 1000.0;
	
			// Setting the byte rate (in MB/s)
			bytesRate = bytesDeltaMb / timeDelta;
			CollectHelper.updateNumberParameter(monitor,
					byteRateParameterName,
					BYTES_RATE_PARAMETER_UNIT,
					collectTime,
					bytesRate,
					bytesRate
			);
		}

		// Setting the usage (in GB), even if it is zero
		final double bytesDeltaGb = bytesDeltaMb / 1024.0;
		CollectHelper.updateNumberParameter(
				monitor,
				usageReportParameterName,
				SPACE_GB_PARAMETER_UNIT,
				collectTime,
				bytesDeltaGb,
				bytesDeltaGb
		);

		return bytesRate;
	}
	
	/**
	 * Collects the {@link NetworkCard} packets rate and usage.
	 * 
	 * @param packetsParameterName       The name of the packets parameter where the raw value is collected
	 * @param packetRateParameterName    The name of the packets rate parameter to be calculated
	 * @param usageReportParameterName   The name of the usage report parameter to be calculated
	 * 
	 * @return packetsValue              Number of packets
	 */
	Double collectNetworkCardPacketsRate(final String packetsParameterName, final String packetRateParameterName, final String usageReportParameterName) {

		final Monitor monitor = monitorCollectInfo.getMonitor();

		// Getting the current value
		final Double packetsValue = extractParameterValue(monitor.getMonitorType(),
			packetsParameterName);

		if (packetsValue == null) {
			return null;
		}

		// Getting the current value's collect time
		Long collectTime = monitorCollectInfo.getCollectTime();
		
		// Setting the packets parameter
		CollectHelper.updateNumberParameter(
				monitor,
				packetsParameterName,
				PACKETS_PARAMETER_UNIT,
				collectTime,
				packetsValue,
				packetsValue
		);

		// Getting the previous value
		Double lastPacketsValue = CollectHelper.getNumberParamRawValue(monitor, packetsParameterName, true);
		if (lastPacketsValue == null) {
			return packetsValue;
		}

		// Getting the previous value's collect time
		final Double collectTimePrevious = CollectHelper.getNumberParamCollectTime(monitor, packetsParameterName, true);

		if (collectTimePrevious == null) {
			// This should never happen
			log.warn("Found previous packets value, but could not find previous collect time.");
			return packetsValue;
		}

		// Computing the packets delta
		final Double packetsDelta = CollectHelper.subtract(packetsParameterName, packetsValue, lastPacketsValue);
		if (packetsDelta == null) {
			log.warn("Found decreasing packets count - must have been reset.");
			return packetsValue;
		}

		// Computing the time delta (in seconds)
		Double timeDelta = CollectHelper.subtract(packetsParameterName, collectTime.doubleValue(), collectTimePrevious);
		if (timeDelta == null || timeDelta == 0.0) {
			return packetsValue;
		}

		timeDelta /= 1000.0;

		// Setting the usage in packets
		CollectHelper.updateNumberParameter(
				monitor,
				usageReportParameterName,
				PACKETS_PARAMETER_UNIT,
				collectTime,
				packetsDelta,
				packetsValue
		);

		// Setting the packets rate
		final Double packetsRate = packetsDelta / timeDelta;
		CollectHelper.updateNumberParameter(
				monitor,
				packetRateParameterName,
				PACKETS_RATE_PARAMETER_UNIT,
				collectTime,
				packetsRate,
				packetsValue
		);

		return packetsValue;
	}
	
	/**
	 * Collect the {@link NetworkCard} bandwidth utilization.
	 */
	void collectNetworkCardBandwidthUtilization(final Double duplexMode, final Double linkSpeed, Double receivedBytesRate, Double transmittedBytesRate) {

		// No rate => no bandwidth
		if (receivedBytesRate == null && transmittedBytesRate == null) {
			return;
		}

		final Monitor monitor = monitorCollectInfo.getMonitor();

		if (linkSpeed != null && linkSpeed >= 0) {

			if (receivedBytesRate == null) {
				receivedBytesRate = 0D;
			}
			if (transmittedBytesRate == null) {
				transmittedBytesRate = 0D;
			}

			double bandwidthUtilization;
			if (duplexMode == null || duplexMode == 1D)  {
				// Full-duplex mode, or unknown mode, in which case, we assume full-duplex.
				// In full-duplex mode, consider bandwidth as the maximum usage while receiving or transmitting.
				bandwidthUtilization = Math.max(transmittedBytesRate, receivedBytesRate) * 8 * 100 / linkSpeed;
			} else {
				// Half-duplex mode
				bandwidthUtilization = (transmittedBytesRate + receivedBytesRate) * 8 * 100 / linkSpeed;
			}

			CollectHelper.updateNumberParameter(
					monitor,
					BANDWIDTH_UTILIZATION_PARAMETER,
					PERCENT_PARAMETER_UNIT,
					monitorCollectInfo.getCollectTime(),
					bandwidthUtilization,
					bandwidthUtilization
			);
		}
	}
	
	/**
	 * Collect the {@link NetworkCard} error count/percentage.
	 */
	void collectNetworkCardErrorPercent(final Double receivedPackets, final Double tranmittedPackets) {
		
		if (receivedPackets == null || tranmittedPackets == null) {
			return;
		}
		
		final Monitor monitor = monitorCollectInfo.getMonitor();

		// Getting the current error count
		final Double errorCount = extractParameterValue(monitor.getMonitorType(),
			ERROR_COUNT_PARAMETER);

		if (errorCount == null) {
			return;
		}

		// Setting the error count
		CollectHelper.updateNumberParameter(
				monitor,
				ERROR_COUNT_PARAMETER,
				ERROR_COUNT_PARAMETER_UNIT,
				monitorCollectInfo.getCollectTime(),
				errorCount,
				errorCount
		);
		
		// Setting the total packets
		final Double totalPackets = receivedPackets + tranmittedPackets;
		CollectHelper.updateNumberParameter(
				monitor,
				TOTAL_PACKETS_PARAMETER,
				PACKETS_PARAMETER_UNIT,
				monitorCollectInfo.getCollectTime(),
				totalPackets,
				totalPackets
		);

		// Getting the previous error count
		final Double lastErrorCount = CollectHelper.getNumberParamRawValue(monitor,
			ERROR_COUNT_PARAMETER, true);
		
		if (lastErrorCount == null) {
			return;
		}

		// Getting the previous total packets count
		final Double lastTotalPackets = CollectHelper.getNumberParamRawValue(monitor,
				TOTAL_PACKETS_PARAMETER, true);
		
		if (lastTotalPackets == null) {
			return;
		}

		// Computing the total packets delta
		final Double totalPacketsDelta = CollectHelper.subtract(TOTAL_PACKETS_PARAMETER,
				totalPackets, lastTotalPackets);
		
		// Setting the error percent
		if (totalPacketsDelta != null && totalPacketsDelta > 10) {
			
			// Computing the error count delta
			final Double errorCountDelta = CollectHelper.subtract(ERROR_COUNT_PARAMETER,
				errorCount, lastErrorCount);
			
			if (errorCountDelta != null) {
				// Computing the error percent
				final Double errorPercent = Math.min(100 * errorCountDelta / totalPacketsDelta, 100);
			
				CollectHelper.updateNumberParameter(
						monitor,
						ERROR_PERCENT_PARAMETER,
						PERCENT_PARAMETER_UNIT,
						monitorCollectInfo.getCollectTime(),
						errorPercent,
						errorPercent
				);
			}
		}
	}
	
	/**
	 * Collect the {@link NetworkCard} zero credit buffer count/percent
	 */
	void collectNetworkCardZeroBufferCreditPercent() {
		final Monitor monitor = monitorCollectInfo.getMonitor();

		// Getting the current zero buffer credit count
		final Double zeroBufferCreditCount = extractParameterValue(monitor.getMonitorType(),
				ZERO_BUFFER_CREDIT_COUNT_PARAMETER);
		
		// Getting the previous zero buffer credit count
		final Double lastZeroBufferCreditCount = CollectHelper.getNumberParamRawValue(monitor,
			ZERO_BUFFER_CREDIT_COUNT_PARAMETER, true);
		
		// Setting the zero buffer credit count
		CollectHelper.updateNumberParameter(
				monitor,
				ZERO_BUFFER_CREDIT_COUNT_PARAMETER,
				ZERO_BUFFER_CREDIT_COUNT_PARAMETER_UNIT,
				monitorCollectInfo.getCollectTime(),
				zeroBufferCreditCount,
				zeroBufferCreditCount
		);

		if (zeroBufferCreditCount == null || lastZeroBufferCreditCount == null) {
			return;
		}
		
		// Getting the transmitted packets since last collect
		final Double transmittedPacketsSinceLastCollect = CollectHelper.getNumberParamValue(monitor,
				USAGE_REPORT_TRANSMITTED_PACKETS_PARAMETER);
		
		if (transmittedPacketsSinceLastCollect == null) {
			return;
		}

		// Computing the zero buffer credit delta delta
		final Double zeroBufferCreditDelta = CollectHelper.subtract(ZERO_BUFFER_CREDIT_COUNT_PARAMETER,
				zeroBufferCreditCount, lastZeroBufferCreditCount);
		
		if (zeroBufferCreditDelta != null) {
			// Setting the zero buffer credit percent
			final Double lastZeroBufferCreditPercent = 100 * zeroBufferCreditDelta / (zeroBufferCreditDelta + transmittedPacketsSinceLastCollect);
			CollectHelper.updateNumberParameter(
					monitor,
					ZERO_BUFFER_CREDIT_PERCENT_PARAMETER,
					PERCENT_PARAMETER_UNIT,
					monitorCollectInfo.getCollectTime(),
					lastZeroBufferCreditPercent,
					lastZeroBufferCreditPercent
			);
		}
	}
}
