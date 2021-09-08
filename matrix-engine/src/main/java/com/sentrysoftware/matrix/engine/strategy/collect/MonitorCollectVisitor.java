package com.sentrysoftware.matrix.engine.strategy.collect;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.util.Assert;

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
import com.sentrysoftware.matrix.common.meta.monitor.Robotic;
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
import com.sentrysoftware.matrix.model.parameter.NumberParam;
import com.sentrysoftware.matrix.model.parameter.ParameterState;
import com.sentrysoftware.matrix.model.parameter.StatusParam;

import lombok.extern.slf4j.Slf4j;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.AVAILABLE_PATH_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.AVAILABLE_PATH_INFORMATION_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.BANDWIDTH_UTILIZATION_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.BATTERY_STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.CHARGE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.COLOR_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.CONTROLLER_STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.CORRECTED_ERROR_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.CURRENT_SPEED_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DUPLEX_MODE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENDURANCE_REMAINING_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_USAGE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_USAGE_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_COUNT_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_PERCENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.INTRUSION_STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.LED_INDICATOR_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.LINK_SPEED_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.LINK_STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.LOGICAL_DISK_LAST_ERROR;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MEMORY_LAST_ERROR;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MOUNT_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MOUNT_COUNT_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MOVE_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MOVE_COUNT_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.NEEDS_CLEANING_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.NEW_LINE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PERCENT_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_CONSUMPTION_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_CONSUMPTION_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_STATE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_SUPPLY_POWER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_SUPPLY_USED_PERCENT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_SUPPLY_USED_WATTS;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PREDICTED_FAILURE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PRESENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PREVIOUS_ERROR_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.RECEIVED_BYTES_RATE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.RECEIVED_PACKETS_RATE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SPACE_GB_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SPEED_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SPEED_PERCENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STARTING_ERROR_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STATUS_INFORMATION_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TEMPERATURE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TEMPERATURE_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TEST_REPORT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TIME_LEFT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TIME_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TRANSMITTED_BYTES_RATE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TRANSMITTED_PACKETS_RATE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.UNALLOCATED_SPACE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.UNMOUNT_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.UNMOUNT_COUNT_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.USAGE_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.USED_CAPACITY_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.USED_TIME_PERCENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.VALUE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.VOLTAGE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.VOLTAGE_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.WHITE_SPACE;
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

		collectFanPowerConsumption();

		appendValuesToStatusParameter(
				SPEED_PARAMETER,
				PRESENT_PARAMETER,
				SPEED_PERCENT_PARAMETER);
	}

	@Override
	public void visit(Led led) {
		collectBasicParameters(led);

		appendValuesToStatusParameter(
				COLOR_PARAMETER,
				LED_INDICATOR_PARAMETER);
	}

	@Override
	public void visit(LogicalDisk logicalDisk) {
		collectBasicParameters(logicalDisk);

		collectErrorCount();
		updateAdditionalStatusInformation(LOGICAL_DISK_LAST_ERROR);
		collectLogicalDiskUnallocatedSpace();
		
		appendValuesToStatusParameter(
				ERROR_COUNT_PARAMETER,
				UNALLOCATED_SPACE_PARAMETER);
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
		updateAdditionalStatusInformation(MEMORY_LAST_ERROR);
		
		appendValuesToStatusParameter(
				ERROR_COUNT_PARAMETER,
				ERROR_STATUS_PARAMETER,
				PREDICTED_FAILURE_PARAMETER,
				PRESENT_PARAMETER);
	}

	@Override
	public void visit(NetworkCard networkCard) {
		collectBasicParameters(networkCard);

		appendValuesToStatusParameter(
				PRESENT_PARAMETER,
				BANDWIDTH_UTILIZATION_PARAMETER,
				DUPLEX_MODE_PARAMETER,
				ERROR_COUNT_PARAMETER,
				ERROR_PERCENT_PARAMETER,
				LINK_SPEED_PARAMETER,
				LINK_STATUS_PARAMETER,
				RECEIVED_BYTES_RATE_PARAMETER,
				RECEIVED_PACKETS_RATE_PARAMETER,
				TRANSMITTED_BYTES_RATE_PARAMETER,
				TRANSMITTED_PACKETS_RATE_PARAMETER,
				ZERO_BUFFER_CREDIT_PERCENT_PARAMETER);

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
	public void visit(Robotic robotic) {
		collectBasicParameters(robotic);
		
		collectIncrementCount(MOVE_COUNT_PARAMETER, MOVE_COUNT_PARAMETER_UNIT);
		collectErrorCount();
		
		appendValuesToStatusParameter(
				ERROR_COUNT_PARAMETER,
				MOVE_COUNT_PARAMETER);
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

		updateStatusParameter(monitor, parameterName, unit, collectTime, state, statusInformation);

	}

	/**
	 * Build the status information text value
	 * 
	 * @param parameterName The name of the parameter e.g. intrusionStatus, status
	 * @param ordinal       The numeric value of the status (0, 1, 2)
	 * @param value         The text value of the status information
	 * @return {@link String} value
	 */
	static String buildStatusInformation(final String parameterName, final int ordinal, final String value) {
		return new StringBuilder()
				.append(parameterName)
				.append(":")
				.append(WHITE_SPACE)
				.append(ordinal)
				.append(WHITE_SPACE)
				.append("(")
				.append(value)
				.append(")")
				.toString();
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
			updateNumberParameter(monitor, parameterName, unit, collectTime, value, value);
		}

	}

	/**
	 * Extract the parameter value from the current row
	 * 
	 * @param monitorType   The type of the monitor
	 * @param parameterName The unique name of the parameter
	 * @return {@link Double} value
	 */
	Double extractParameterValue(final MonitorType monitorType, final String parameterName) {
		Assert.notNull(monitorType, MONITOR_TYPE_CANNOT_BE_NULL);

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
	 * Update the number parameter value identified by <code>parameterName</code> in the given {@link Monitor} instance
	 * 
	 * @param monitor       The monitor we wish to collect the number parameter value
	 * @param parameterName The unique name of the parameter
	 * @param unit          The unit of the parameter
	 * @param collectTime   The collect time for this parameter
	 * @param value         The value to set on the {@link NumberParam} instance
	 * @param rawValue      The raw value to set as it is needed when computing delta and rates
	 */
	static void updateNumberParameter(final Monitor monitor, final String parameterName, final String unit, final Long collectTime,
			final Double value, final Double rawValue) {

		// GET the existing number parameter and update the value and the collect time
		NumberParam numberParam = monitor.getParameter(parameterName, NumberParam.class);

		// The parameter is not present then create it
		if (numberParam == null) {
			numberParam = NumberParam
					.builder()
					.name(parameterName)
					.unit(unit)
					.build();

		}

		numberParam.setValue(value);
		numberParam.setCollectTime(collectTime);
		numberParam.setRawValue(rawValue);

		monitor.collectParameter(numberParam);
	}

	/**
	 * Update the status parameter value identified by <code>parameterName</code> in the given {@link Monitor} instance
	 * 
	 * @param monitor           The monitor we wish to collect the status parameter value
	 * @param parameterName     The unique name of the parameter
	 * @param unit              The unit of the parameter
	 * @param collectTime       The collect time for this parameter
	 * @param state             The {@link ParameterState} (OK, WARN, ALARM) used to build the {@link StatusParam}
	 * @param statusInformation The status information
	 */
	static void updateStatusParameter(final Monitor monitor, final String parameterName, final String unit, final Long collectTime,
			final ParameterState state, final String statusInformation) {

		StatusParam statusParam = monitor.getParameter(parameterName, StatusParam.class);

		if (statusParam == null) {
			statusParam = StatusParam
					.builder()
					.name(parameterName)
					.unit(unit)
					.build();
		}

		statusParam.setState(state);
		statusParam.setStatus(state.ordinal());
		statusParam.setStatusInformation(buildStatusInformation(
				parameterName,
				state.ordinal(),
				statusInformation));
		statusParam.setCollectTime(collectTime);

		monitor.collectParameter(statusParam);
	}

	/**
	 * Update status information with additional information as suffix.
	 * 
	 * @param additionalInformation The name of the field containing the additional information
	 */
	void updateAdditionalStatusInformation(final String additionalInformation) {

		final Monitor monitor = monitorCollectInfo.getMonitor();
		final String additionalInfo = CollectHelper.getValueTableColumnValue(
				monitorCollectInfo.getValueTable(),
				additionalInformation,
				monitor.getMonitorType(),
				monitorCollectInfo.getRow(),
				monitorCollectInfo.getMapping().get(additionalInformation));

		if (additionalInfo != null) {

			StatusParam statusParam = monitor.getParameter(STATUS_PARAMETER, StatusParam.class);
			statusParam.setStatusInformation(statusParam.getStatusInformation() + " - " + additionalInfo);
		}
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
		.forEach(metaParam -> collectStatusParameter(metaMonitor.getMonitorType(), metaParam.getName(), metaParam.getUnit()));

		metaMonitor.getMetaParameters()
		.values()
		.stream()
		.filter(metaParam -> metaParam.isBasicCollect() && ParameterType.NUMBER.equals(metaParam.getType()))
		.forEach(metaParam -> collectNumberParameter(metaMonitor.getMonitorType(), metaParam.getName(), metaParam.getUnit()));
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

			collectPowerFromEnergyUsage(monitor, collectTime, energyUsageRaw, hostname);
			return;
		}

		// based on the power consumption compute the energy usage
		final Double powerConsumption = extractParameterValue(monitor.getMonitorType(),
				POWER_CONSUMPTION_PARAMETER);
		if (powerConsumption != null && powerConsumption >= 0) {
			collectEnergyUsageFromPower(monitor, collectTime, powerConsumption, hostname);
		}

	}

	/**
	 * Collect the energy usage based on the power consumption
	 * 
	 * @param monitor          The monitor instance we wish to collect
	 * @param collectTime      The current collect time
	 * @param powerConsumption The power consumption value. Never null
	 * @param hostname         The system host name used for debug purpose
	 */
	static void collectEnergyUsageFromPower(final Monitor monitor, final Long collectTime, final Double powerConsumption, String hostname) {

		updateNumberParameter(monitor,
				POWER_CONSUMPTION_PARAMETER,
				POWER_CONSUMPTION_PARAMETER_UNIT,
				collectTime,
				powerConsumption,
				powerConsumption);

		final Double collectTimePrevious = CollectHelper.getNumberParamCollectTime(monitor, POWER_CONSUMPTION_PARAMETER, true);

		Double deltaTime = CollectHelper.subtract(POWER_CONSUMPTION_PARAMETER,
				collectTime.doubleValue(), collectTimePrevious);

		// Convert deltaTime from milliseconds (ms) to seconds
		if (deltaTime != null) {
			deltaTime /= 1000.0; 
		}

		// Calculate energy usage from Power Consumption: E = P * T
		final Double energyUsage = CollectHelper.multiply(POWER_CONSUMPTION_PARAMETER,
				powerConsumption, deltaTime);

		if (energyUsage != null) {

			// The energy will start from the energy usage delta
			Double energy = energyUsage;

			// The previous value is needed to get the total energy in joules
			Double previousEnergy = CollectHelper.getNumberParamRawValue(monitor,
				ENERGY_PARAMETER, true);

			// Ok, we have the previous energy value ? sum the previous energy and the current delta energy usage
			if (previousEnergy != null) {
				energy +=  previousEnergy;
			}

			// Everything is good update the energy parameter in the HostMonitoring
			updateNumberParameter(monitor,
				ENERGY_PARAMETER,
				ENERGY_PARAMETER_UNIT,
				collectTime,
				energy,
				energy);

			// Update the energy usage delta parameter in the HostMonitoring
			updateNumberParameter(monitor,
				ENERGY_USAGE_PARAMETER,
				ENERGY_USAGE_PARAMETER_UNIT,
				collectTime,
				energyUsage,
				energyUsage);

		} else {
			log.debug("Cannot compute energy usage for monitor {} on system {}. Current power consumption {}, current time {}, previous time {}",
					monitor.getId(), hostname, powerConsumption, collectTime, collectTimePrevious);
		}
	}

	/**
	 * Collect the power consumption based on the energy usage Power Consumption = Delta(energyUsageRaw) - Delta(CollectTime)
	 * @param monitor           The monitor instance we wish to collect
	 * @param collectTime       The current collect time
	 * @param energyUsageRaw    The energy usage value. Never null
	 * @param hostname          The system host name used for debug purpose
	 */
	static void collectPowerFromEnergyUsage(final Monitor monitor, final Long collectTime, final Double energyUsageRaw, final String hostname) {

		updateNumberParameter(monitor,
				ENERGY_USAGE_PARAMETER,
				ENERGY_USAGE_PARAMETER_UNIT,
				collectTime,
				null,
				energyUsageRaw);

		// Previous raw value
		final Double energyUsageRawPrevious = CollectHelper.getNumberParamRawValue(monitor, ENERGY_USAGE_PARAMETER, true);

		// Time
		final Double collectTimeDouble = collectTime.doubleValue();
		final Double collectTimePrevious = CollectHelper.getNumberParamCollectTime(monitor, ENERGY_USAGE_PARAMETER, true);

		// Compute the rate value: delta(raw energy usage) / delta (time)
		Double powerConsumption = CollectHelper.rate(POWER_CONSUMPTION_PARAMETER,
				energyUsageRaw, energyUsageRawPrevious,
				collectTimeDouble, collectTimePrevious);

		// Compute the delta to get the energy usage value
		final Double energyUsage = CollectHelper.subtract(ENERGY_USAGE_PARAMETER, energyUsageRaw, energyUsageRawPrevious);

		if (energyUsage != null) {
			updateNumberParameter(monitor,
					ENERGY_USAGE_PARAMETER,
					ENERGY_USAGE_PARAMETER_UNIT,
					collectTime,
					energyUsage * 1000 * 3600, // kW-hours to Joules
					energyUsageRaw);
		} else {
			log.debug("Cannot compute energy usage for monitor {} on system {}. Current raw energy usage {}, previous raw energy usage {}",
					monitor.getId(), hostname, energyUsageRaw, energyUsageRawPrevious);
		}

		if (powerConsumption != null) {
			// powerConsumption = (delta kwatt-hours) / delta (time in milliseconds)
			// powerConsumption = rate * 1000 (1Kw = 1000 Watts) * (1000 * 3600  To milliseconds convert to hours) 
			powerConsumption = powerConsumption * 1000 * (1000 * 3600);

			updateNumberParameter(monitor,
					POWER_CONSUMPTION_PARAMETER,
					POWER_CONSUMPTION_PARAMETER_UNIT,
					collectTime,
					powerConsumption,
					powerConsumption);
		} else {
			log.debug("Cannot compute power consumption for monitor {} on system {}.\n"
					+ "Current raw energy usage {}, previous raw energy usage {}, current time {}, previous time {}",
					monitor.getId(), hostname, energyUsageRaw, energyUsageRawPrevious, collectTimeDouble, collectTimePrevious);
		}

		// Updating the monitor's energy parameter
		updateNumberParameter(monitor,
			ENERGY_PARAMETER,
			ENERGY_PARAMETER_UNIT,
			collectTime,
			energyUsageRaw * 3600 * 1000, // value
			energyUsageRaw); // raw value
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

			updateNumberParameter(monitor,
				CHARGE_PARAMETER,
				PERCENT_PARAMETER_UNIT,
				monitorCollectInfo.getCollectTime(),
				Math.min(chargeRaw, 100.0), // In case the raw value is greater than 100%
				chargeRaw);
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

			updateNumberParameter(monitor,
				TIME_LEFT_PARAMETER,
				TIME_PARAMETER_UNIT,
				monitorCollectInfo.getCollectTime(),
				timeLeftRaw * 60.0, // minutes to seconds
				timeLeftRaw);
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
			updateNumberParameter(monitor,
				USED_TIME_PERCENT_PARAMETER,
				PERCENT_PARAMETER_UNIT,
				collectTime,
				null,
				usedTimePercentRaw);

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
		updateNumberParameter(monitor,
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
			updateNumberParameter(monitor,
					VOLTAGE_PARAMETER,
					VOLTAGE_PARAMETER_UNIT,
					monitorCollectInfo.getCollectTime(),
					computedVoltage,
					voltageValue);
		}
	}
	
	/**
	 * Collects the error counts in {@link Robotic} & {@link TapeDrive}.
	 */
	void collectErrorCount() {

		final Monitor monitor = monitorCollectInfo.getMonitor();
		Double errorCount = null;

		Double rawErrorCount = extractParameterValue(monitor.getMonitorType(),
			ERROR_COUNT_PARAMETER);

		if (rawErrorCount != null) {

			// Getting the previous error count
			Double previousErrorCount = extractParameterValue(monitor.getMonitorType(),
				PREVIOUS_ERROR_COUNT_PARAMETER);

			// Getting the starting error count
			Double startingErrorCount = extractParameterValue(monitor.getMonitorType(),
				STARTING_ERROR_COUNT_PARAMETER);
			
			if (startingErrorCount != null) {
				
				// Remove existing error count from the current value
				errorCount = rawErrorCount - startingErrorCount;

				// If we obtain a negative number, that's impossible: set everything to 0
				if (errorCount < 0)
				{
					errorCount = 0.0;

					// Reset the starting error count
					updateNumberParameter(monitor,
						STARTING_ERROR_COUNT_PARAMETER,
						ERROR_COUNT_PARAMETER_UNIT,
						monitorCollectInfo.getCollectTime(),
						0.0,
						0.0);
				} 

			} else {
				
				// First polling
				errorCount = 0.0;
				
				if (rawErrorCount < 0.0) {
					rawErrorCount = 0.0;
				}
				
				// Record as the starting error count
				updateNumberParameter(monitor,
					STARTING_ERROR_COUNT_PARAMETER,
					ERROR_COUNT_PARAMETER_UNIT,
					monitorCollectInfo.getCollectTime(),
					rawErrorCount,
					rawErrorCount);
				
				// Record the previous error count
				previousErrorCount = rawErrorCount;
			}
			
			// Update the previous error count
			updateNumberParameter(monitor,
				PREVIOUS_ERROR_COUNT_PARAMETER,
				ERROR_COUNT_PARAMETER_UNIT,
				monitorCollectInfo.getCollectTime(),
				previousErrorCount,
				previousErrorCount);

			// Update the error count
			updateNumberParameter(monitor,
				ERROR_COUNT_PARAMETER,
				ERROR_COUNT_PARAMETER_UNIT,
				monitorCollectInfo.getCollectTime(),
				errorCount,
				rawErrorCount);
		}
	}
	
	/**
	 * Collects the incremental parameters, namely 
	 * {@link TapeDrive} unmount, mount & {@link Robotic} move count.
	 * 
	 * @param parameterName The name of the count parameter, like mountCount
	 * @param parameterName The unit of the count parameter, like mounts
	 */
	void collectIncrementCount(final String countParameter, final String countParameterUnit) {

		final Monitor monitor = monitorCollectInfo.getMonitor();
		final Double rawCount  = extractParameterValue(monitor.getMonitorType(), countParameter);

		if (rawCount != null) {

			// Getting the previous value
			Double previousRawCount = CollectHelper.getNumberParamRawValue(monitor, countParameter, true);
			
			updateNumberParameter(
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
			POWER_SUPPLY_USED_PERCENT);

		if (usedPercentRaw == null) {
		
			// Getting the used capacity
			final Double powerSupplyUsedWatts = extractParameterValue(monitor.getMonitorType(),
				POWER_SUPPLY_USED_WATTS);
			
			// Getting the the power
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
			updateNumberParameter(monitor,
				USED_CAPACITY_PARAMETER,
				PERCENT_PARAMETER_UNIT,
				monitorCollectInfo.getCollectTime(),
				usedPercent,
				usedPercentRaw);
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

			updateNumberParameter(monitor,
				UNALLOCATED_SPACE_PARAMETER,
				SPACE_GB_PARAMETER_UNIT,
				monitorCollectInfo.getCollectTime(),
				unallocatedSpaceRaw / (1024.0 * 1024.0 * 1024.0), // Bytes to GB
				unallocatedSpaceRaw);
		}
	}
	
	/**
	 * Collects the power consumption from {@link Fan} speed.
	 */
	void collectFanPowerConsumption() {

		final Monitor monitor = monitorCollectInfo.getMonitor();
		
		// Approximately 5 Watt for standard fan
		Double powerConsumption = 5.0;

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
			
		updateNumberParameter(monitor,
			POWER_CONSUMPTION_PARAMETER,
			POWER_CONSUMPTION_PARAMETER_UNIT,
			monitorCollectInfo.getCollectTime(),
			powerConsumption,
			powerConsumption);
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
			updateNumberParameter(monitor,
					TEMPERATURE_PARAMETER,
					TEMPERATURE_PARAMETER_UNIT,
					monitorCollectInfo.getCollectTime(),
					temperatureValue,
					temperatureValue);
		}
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
			updateNumberParameter(monitor,
					ENDURANCE_REMAINING_PARAMETER,
					PERCENT_PARAMETER_UNIT,
					monitorCollectInfo.getCollectTime(),
					rawEnduranceRemaining,
					rawEnduranceRemaining);
		}
	}
}
