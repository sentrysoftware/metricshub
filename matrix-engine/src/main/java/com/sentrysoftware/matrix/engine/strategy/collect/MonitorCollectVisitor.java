package com.sentrysoftware.matrix.engine.strategy.collect;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.util.Assert;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.Battery;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.Blade;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.ConcreteConnector;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.Cpu;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.CpuCore;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.DiskController;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.DiskEnclosure;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.Enclosure;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.Fan;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.Led;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.LogicalDisk;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.Lun;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.Memory;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.NetworkCard;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.OtherDevice;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.PhysicalDisk;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.PowerSupply;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.Robotic;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.TapeDrive;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.Target;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.Temperature;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.Voltage;
import com.sentrysoftware.matrix.engine.strategy.IMonitorVisitor;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.parameter.IParameterValue;
import com.sentrysoftware.matrix.model.parameter.NumberParam;
import com.sentrysoftware.matrix.model.parameter.ParameterState;
import com.sentrysoftware.matrix.model.parameter.StatusParam;

import lombok.extern.slf4j.Slf4j;

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
		map.put(HardwareConstants.INTRUSION_STATUS_PARAMETER, MonitorCollectVisitor::getIntrusionStatusInformation);
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
	public void visit(ConcreteConnector concreteConnector) {
		// Not implemented yet
	}

	@Override
	public void visit(Target device) {
		// Not implemented yet
	}

	@Override
	public void visit(Battery battery) {
		collectStatusParameter(MonitorType.BATTERY, HardwareConstants.STATUS_PARAMETER, HardwareConstants.STATUS_PARAMETER_UNIT);
	}

	@Override
	public void visit(Blade blade) {
		collectStatusParameter(MonitorType.BLADE, HardwareConstants.STATUS_PARAMETER, HardwareConstants.STATUS_PARAMETER_UNIT);
	}

	@Override
	public void visit(Cpu cpu) {
		collectStatusParameter(MonitorType.CPU, HardwareConstants.STATUS_PARAMETER, HardwareConstants.STATUS_PARAMETER_UNIT);
	}

	@Override
	public void visit(CpuCore cpuCore) {
		collectStatusParameter(MonitorType.CPU_CORE, HardwareConstants.STATUS_PARAMETER, HardwareConstants.STATUS_PARAMETER_UNIT);
	}

	@Override
	public void visit(DiskController diskController) {
		collectStatusParameter(MonitorType.DISK_CONTROLLER, HardwareConstants.STATUS_PARAMETER, HardwareConstants.STATUS_PARAMETER_UNIT);
	}

	@Override
	public void visit(DiskEnclosure diskEnclosure) {
		collectStatusParameter(MonitorType.DISK_ENCLOSURE, HardwareConstants.STATUS_PARAMETER, HardwareConstants.STATUS_PARAMETER_UNIT);
	}

	@Override
	public void visit(Enclosure enclosure) {
		// Status
		collectStatusParameter(MonitorType.ENCLOSURE, HardwareConstants.STATUS_PARAMETER, HardwareConstants.STATUS_PARAMETER_UNIT);

		// IntrusionStatus
		collectStatusParameter(MonitorType.ENCLOSURE, HardwareConstants.INTRUSION_STATUS_PARAMETER,
				HardwareConstants.INTRUSION_STATUS_PARAMETER_UNIT);

		// EnergyUsage in Joules, to get the power consumption in Watts we need to compute the volts / amperes
		// Means [ Delta Joules ] / [ Delta Time in seconds ]
		collectNumberParameter(MonitorType.ENCLOSURE, HardwareConstants.ENERGY_USAGE_PARAMETER,
				HardwareConstants.ENERGY_USAGE_PARAMETER_UNIT);

		appendValuesToStatusParameter(HardwareConstants.INTRUSION_STATUS_PARAMETER, HardwareConstants.ENERGY_USAGE_PARAMETER);
	}

	@Override
	public void visit(Fan fan) {
		collectStatusParameter(MonitorType.FAN, HardwareConstants.STATUS_PARAMETER, HardwareConstants.STATUS_PARAMETER_UNIT);
	}

	@Override
	public void visit(Led led) {
		collectStatusParameter(MonitorType.LED, HardwareConstants.STATUS_PARAMETER, HardwareConstants.STATUS_PARAMETER_UNIT);
	}

	@Override
	public void visit(LogicalDisk logicalDisk) {
		collectStatusParameter(MonitorType.LOGICAL_DISK, HardwareConstants.STATUS_PARAMETER, HardwareConstants.STATUS_PARAMETER_UNIT);
	}

	@Override
	public void visit(Lun lun) {
		collectStatusParameter(MonitorType.LUN, HardwareConstants.STATUS_PARAMETER, HardwareConstants.STATUS_PARAMETER_UNIT);
	}

	@Override
	public void visit(Memory memory) {
		collectStatusParameter(MonitorType.MEMORY, HardwareConstants.STATUS_PARAMETER, HardwareConstants.STATUS_PARAMETER_UNIT);
	}

	@Override
	public void visit(NetworkCard networkCard) {
		collectStatusParameter(MonitorType.NETWORK_CARD, HardwareConstants.STATUS_PARAMETER, HardwareConstants.STATUS_PARAMETER_UNIT);
	}

	@Override
	public void visit(OtherDevice otherDevice) {
		collectStatusParameter(MonitorType.OTHER_DEVICE, HardwareConstants.STATUS_PARAMETER, HardwareConstants.STATUS_PARAMETER_UNIT);
	}

	@Override
	public void visit(PhysicalDisk physicalDisk) {
		collectStatusParameter(MonitorType.DISK_CONTROLLER, HardwareConstants.STATUS_PARAMETER, HardwareConstants.STATUS_PARAMETER_UNIT);
	}

	@Override
	public void visit(PowerSupply powerSupply) {
		collectStatusParameter(MonitorType.POWER_SUPPLY, HardwareConstants.STATUS_PARAMETER, HardwareConstants.STATUS_PARAMETER_UNIT);
	}

	@Override
	public void visit(TapeDrive tapeDrive) {
		collectStatusParameter(MonitorType.TAPE_DRIVE, HardwareConstants.STATUS_PARAMETER, HardwareConstants.STATUS_PARAMETER_UNIT);
	}

	@Override
	public void visit(Temperature temperature) {
		collectStatusParameter(MonitorType.TEMPERATURE, HardwareConstants.STATUS_PARAMETER, HardwareConstants.STATUS_PARAMETER_UNIT);
	}

	@Override
	public void visit(Voltage voltage) {
		collectStatusParameter(MonitorType.VOLTAGE, HardwareConstants.STATUS_PARAMETER, HardwareConstants.STATUS_PARAMETER_UNIT);
	}

	@Override
	public void visit(Robotic robotic) {
		collectStatusParameter(MonitorType.ROBOTIC, HardwareConstants.STATUS_PARAMETER, HardwareConstants.STATUS_PARAMETER_UNIT);
	}

	/**
	 * Collect the Status of the current {@link Monitor} instance
	 * 
	 * @param monitorType   The type of the monitor we currently collect
	 * @param parameterName The name of the status parameter to collect
	 * @param unit          The unit to set in the {@link IParameterValue} instance
	 */
	protected void collectStatusParameter(final MonitorType monitorType, final String parameterName, final String unit) {

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
		if (HardwareConstants.STATUS_PARAMETER.equals(parameterName)) {
			statusInformation = CollectHelper.getValueTableColumnValue(valueTable,
					HardwareConstants.STATUS_INFORMATION_PARAMETER,
					monitorType,
					row,
					mapping.get(HardwareConstants.STATUS_INFORMATION_PARAMETER));
		}

		// Otherwise simply set the state name OK, WARN or ALARM
		if (statusInformation == null
				|| statusInformation.trim().isEmpty()) {
			// Is there any specific implementation for the status information field
			if (STATUS_INFORMATION_MAP.containsKey(parameterName)) {
				statusInformation = STATUS_INFORMATION_MAP.get(parameterName).apply(state);
			} else {
				statusInformation = state.name();
			}
		}

		final StatusParam statusParam = StatusParam
				.builder()
				.name(parameterName)
				.collectTime(collectTime)
				.state(state)
				.unit(unit)
				.statusInformation(buildStatusInformation(
						parameterName,
						state.ordinal(),
						statusInformation))
				.build();

		monitor.addParameter(statusParam);

	}

	/**
	 * Build the status information text value
	 * 
	 * @param parameterName The name of the parameter e.g. intrusionStatus, status
	 * @param ordinal       The numeric value of the status (0, 1, 2)
	 * @param value         The text value of the status information
	 * @return {@link String} value
	 */
	protected String buildStatusInformation(final String parameterName, final int ordinal, final String value) {
		return new StringBuilder()
				.append(parameterName)
				.append(HardwareConstants.COLON)
				.append(HardwareConstants.WHITE_SPACE)
				.append(ordinal)
				.append(HardwareConstants.WHITE_SPACE)
				.append(HardwareConstants.OPENING_PARENTHESIS)
				.append(value)
				.append(HardwareConstants.CLOSING_PARENTHESIS)
				.toString();
	}

	/**
	 * Append the given parameter information to the status information
	 * 
	 * @param statusParam The {@link StatusParam} we wish to update its statusInformation field value
	 * @param parameter   The parameter we wish to append its value
	 */
	protected static void appendToStatusInformation(final StatusParam statusParam, final IParameterValue parameter) {
		if (statusParam == null || parameter == null) {
			return;
		}

		String existingStatusInformation = statusParam.getStatusInformation();

		if (existingStatusInformation == null) {
			existingStatusInformation = HardwareConstants.EMPTY;
		} else {
			existingStatusInformation += HardwareConstants.NEW_LINE;
		}

		final StringBuilder builder = new StringBuilder(existingStatusInformation)
				.append(parameter.formatValueAsString());

		statusParam.setStatusInformation(builder.toString());
	}

	/**
	 * Get the parameter identified by the given name from the current monitor then append the values to the StatusInformation fiend of the
	 * Status parameter
	 * 
	 * @param parameterNames The name of the parameters we wish to append in the StatusInformation of the Status parameter
	 */
	protected void appendValuesToStatusParameter(final String... parameterNames) {

		final Monitor monitor = monitorCollectInfo.getMonitor();
		Assert.notNull(monitor, MONITOR_CANNOT_BE_NULL);

		// Cannot be null
		final Map<String, IParameterValue> parameters = monitor.getParameters();

		final StatusParam statusParam = (StatusParam) parameters.get(HardwareConstants.STATUS_PARAMETER);
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
	protected void collectNumberParameter(final MonitorType monitorType, final String parameterName, final String unit) {

		Assert.notNull(monitorType, MONITOR_TYPE_CANNOT_BE_NULL);

		checkCollectInfo(monitorCollectInfo);

		final Monitor monitor = monitorCollectInfo.getMonitor();
		final List<String> row = monitorCollectInfo.getRow();
		final Map<String, String> mapping = monitorCollectInfo.getMapping();
		final String hostname = monitorCollectInfo.getHostname();
		final String valueTable = monitorCollectInfo.getValueTable();
		final Long collectTime = monitorCollectInfo.getCollectTime();

		// Get the number value as string from the current row
		final String stringValue = CollectHelper.getValueTableColumnValue(valueTable,
				parameterName,
				monitorType,
				row,
				mapping.get(parameterName));


		if (stringValue == null) {
			log.debug("No {} to collect for monitor id {}. Hostname {}", parameterName, monitor.getId(), hostname);
			return;
		}

		final Double value;
		try {
			value = Double.parseDouble(stringValue);
		} catch(NumberFormatException e) {
			log.error("Cannot parse the {} value {} for monitor id {}. {} won't be collected",
					parameterName, stringValue, monitor.getId(), parameterName);
			log.error("Parsing Error", e);
			return;
		}

		final NumberParam numberParam = NumberParam
				.builder()
				.name(parameterName)
				.collectTime(collectTime)
				.unit(unit)
				.value(value)
				.build();

		monitor.addParameter(numberParam);

	}

	/**
	 * @param paramerState {@link ParameterState#OK}, {@link ParameterState#WARN} or {@link ParameterState#ALARM}
	 * @return a phrase for the intrusion status value
	 */
	protected static String getIntrusionStatusInformation(final ParameterState paramerState) {
		switch (paramerState) {
		case OK:
			return "No Intrusion Detected";
		case ALARM:
			return "Intrusion Detected";
		default: 
			return "Unexpected Intrusion Status";
		}
	}
}
