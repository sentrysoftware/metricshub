package com.sentrysoftware.matrix.engine.strategy.collect;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.util.Assert;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.common.meta.monitor.Battery;
import com.sentrysoftware.matrix.common.meta.monitor.Blade;
import com.sentrysoftware.matrix.common.meta.monitor.Cpu;
import com.sentrysoftware.matrix.common.meta.monitor.CpuCore;
import com.sentrysoftware.matrix.common.meta.monitor.DiskController;
import com.sentrysoftware.matrix.common.meta.monitor.DiskEnclosure;
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
	public void visit(MetaConnector metaConnector) {
		// Not implemented yet
	}

	@Override
	public void visit(Target target) {
		// Not implemented yet
	}

	@Override
	public void visit(Battery battery) {
		collectBasicParameters(battery);
	}

	@Override
	public void visit(Blade blade) {
		collectBasicParameters(blade);
	}

	@Override
	public void visit(Cpu cpu) {
		collectBasicParameters(cpu);
		
		appendValuesToStatusParameter(HardwareConstants.CORRECTED_ERROR_COUNT_PARAMETER, 
				HardwareConstants.CURRENT_SPEED_PARAMETER,
				HardwareConstants.PREDICTED_FAILURE_PARAMETER,
				HardwareConstants.PRESENT_PARAMETER);
	}

	@Override
	public void visit(CpuCore cpuCore) {
		collectBasicParameters(cpuCore);
	}

	@Override
	public void visit(DiskController diskController) {
		collectBasicParameters(diskController);
	}

	@Override
	public void visit(DiskEnclosure diskEnclosure) {
		collectBasicParameters(diskEnclosure);
	}

	@Override
	public void visit(Enclosure enclosure) {
		collectBasicParameters(enclosure);

		appendValuesToStatusParameter(HardwareConstants.INTRUSION_STATUS_PARAMETER, HardwareConstants.ENERGY_USAGE_PARAMETER);
	}

	@Override
	public void visit(Fan fan) {
		collectBasicParameters(fan);

		appendValuesToStatusParameter(HardwareConstants.SPEED_PARAMETER, HardwareConstants.SPEED_PERCENT_PARAMETER);
	}

	@Override
	public void visit(Led led) {
		collectBasicParameters(led);
	}

	@Override
	public void visit(LogicalDisk logicalDisk) {
		collectBasicParameters(logicalDisk);
	}

	@Override
	public void visit(Lun lun) {
		collectBasicParameters(lun);
	}

	@Override
	public void visit(Memory memory) {
		collectBasicParameters(memory);
		
		appendValuesToStatusParameter(HardwareConstants.ERROR_COUNT_PARAMETER, HardwareConstants.ERROR_STATUS_PARAMETER,
				HardwareConstants.PREDICTED_FAILURE_PARAMETER, HardwareConstants.PRESENT_PARAMETER);
	}

	@Override
	public void visit(NetworkCard networkCard) {
		collectBasicParameters(networkCard);
	}

	@Override
	public void visit(OtherDevice otherDevice) {
		collectBasicParameters(otherDevice);
	}

	@Override
	public void visit(PhysicalDisk physicalDisk) {
		collectBasicParameters(physicalDisk);
	}

	@Override
	public void visit(PowerSupply powerSupply) {
		collectBasicParameters(powerSupply);
	}

	@Override
	public void visit(TapeDrive tapeDrive) {
		collectBasicParameters(tapeDrive);
	}

	@Override
	public void visit(Temperature temperature) {
		collectBasicParameters(temperature);

		appendValuesToStatusParameter(HardwareConstants.TEMPERATURE_PARAMETER);
	}

	@Override
	public void visit(Voltage voltage) {
		collectBasicParameters(voltage);

		appendValuesToStatusParameter(HardwareConstants.VOLTAGE_PARAMETER);
	}

	@Override
	public void visit(Robotic robotic) {
		collectBasicParameters(robotic);
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
		if (HardwareConstants.STATUS_PARAMETER.equals(parameterName)) {
			statusInformation = CollectHelper.getValueTableColumnValue(valueTable,
					HardwareConstants.STATUS_INFORMATION_PARAMETER,
					monitorType,
					row,
					mapping.get(HardwareConstants.STATUS_INFORMATION_PARAMETER));
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
	String buildStatusInformation(final String parameterName, final int ordinal, final String value) {
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
	static void appendToStatusInformation(final StatusParam statusParam, final IParameterValue parameter) {
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
	void appendValuesToStatusParameter(final String... parameterNames) {

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
	void collectNumberParameter(final MonitorType monitorType, final String parameterName, final String unit) {

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
	static String getIntrusionStatusInformation(final ParameterState paramerState) {
		switch (paramerState) {
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

	public static class StatusParamFirstComparator implements Comparator<MetaParameter> {

		@Override
		public int compare(final MetaParameter metaParam1, final MetaParameter metaParam2) {
			// Status first
			if (HardwareConstants.STATUS_PARAMETER.equalsIgnoreCase(metaParam1.getName())) {
				return -1;
			}

			return metaParam1.getName().compareTo(metaParam2.getName());
		}
	}

}
