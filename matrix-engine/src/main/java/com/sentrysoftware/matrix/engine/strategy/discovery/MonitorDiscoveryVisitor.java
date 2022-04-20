package com.sentrysoftware.matrix.engine.strategy.discovery;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.common.helpers.NumberHelper;
import com.sentrysoftware.matrix.common.helpers.NetworkHelper;
import com.sentrysoftware.matrix.common.meta.monitor.Battery;
import com.sentrysoftware.matrix.common.meta.monitor.Blade;
import com.sentrysoftware.matrix.common.meta.monitor.Cpu;
import com.sentrysoftware.matrix.common.meta.monitor.CpuCore;
import com.sentrysoftware.matrix.common.meta.monitor.DiskController;
import com.sentrysoftware.matrix.common.meta.monitor.Enclosure;
import com.sentrysoftware.matrix.common.meta.monitor.Fan;
import com.sentrysoftware.matrix.common.meta.monitor.Gpu;
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
import com.sentrysoftware.matrix.common.meta.monitor.Vm;
import com.sentrysoftware.matrix.common.meta.monitor.Voltage;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.strategy.IMonitorVisitor;
import com.sentrysoftware.matrix.engine.strategy.collect.CollectHelper;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.Map;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ATTACHED_TO_DEVICE_ID;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ATTACHED_TO_DEVICE_TYPE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DEVICE_ID;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TARGET_FQDN;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TYPE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.AVAILABLE_PATH_WARNING;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.EXPECTED_PATH_COUNT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_PERCENT_WARNING_THRESHOLD;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_PERCENT_ALARM_THRESHOLD;

@Slf4j
public class MonitorDiscoveryVisitor implements IMonitorVisitor {

	private static final String CANNOT_CREATE_MONITOR_NULL_NAME_MSG = "Hostname {} - Cannot create monitor {} with null name. Connector {}";
	private static final String HOSTNAME_CANNOT_BE_NULL = "hostname cannot be null.";
	private static final String HOST_MONITORING_CANNOT_BE_NULL = "hostMonitoring cannot be null.";
	private static final String CONNECTOR_NAME_CANNOT_BE_NULL = "connectorName cannot be null.";
	private static final String MONITOR_CANNOT_BE_NULL = "monitor cannot be null.";
	private static final String TARGET_ID_CANNOT_BE_NULL = "target id cannot be null.";
	private static final String TARGET_MONITOR_CANNOT_BE_NULL = "targetMonitor cannot be null.";
	private static final String TARGET_TYPE_CANNOT_BE_NULL = "targetType cannot be null.";
	private static final String MONITOR_TYPE_CANNOT_BE_NULL = "monitorType cannot be null.";
	public static final String METADATA_CANNOT_BE_NULL = "metadata cannot be null.";
	private static final String CANNOT_CREATE_MONITOR_ERROR_MSG = "Hostname {} - Cannot create {} with deviceId {}. Connector {}";

	private final MonitorBuildingInfo monitorBuildingInfo;

	public MonitorDiscoveryVisitor(@NonNull MonitorBuildingInfo monitorBuildingInfo) {
		checkBuildingInfo(monitorBuildingInfo);
		this.monitorBuildingInfo = monitorBuildingInfo;
	}

	@Override
	public void visit(MetaConnector metaConnector) {

		final Monitor monitor = monitorBuildingInfo.getMonitor();

		// Add the monitor
		monitorBuildingInfo.getHostMonitoring().addMonitor(monitor);

	}

	@Override
	public void visit(Target target) {

		final Monitor monitor = monitorBuildingInfo.getMonitor();

		// Add the monitor
		monitorBuildingInfo.getHostMonitoring().addMonitor(monitor);

	}

	@Override
	public void visit(Battery battery) {

		createMonitor(MonitorNameBuilder.buildBatteryName(monitorBuildingInfo), null);
	}

	@Override
	public void visit(Blade blade) {

		createMonitor(MonitorNameBuilder.buildBladeName(monitorBuildingInfo), null);
	}

	@Override
	public void visit(Cpu cpu) {

		createMonitor(MonitorNameBuilder.buildCpuName(monitorBuildingInfo), null);
	}

	@Override
	public void visit(CpuCore cpuCore) {

		createMonitor(MonitorNameBuilder.buildCpuCoreName(monitorBuildingInfo), null);
	}

	@Override
	public void visit(DiskController diskController) {

		createMonitor(MonitorNameBuilder.buildDiskControllerName(monitorBuildingInfo), null);
	}

	@Override
	public void visit(Enclosure enclosure) {
		final Monitor targetMonitor = monitorBuildingInfo.getTargetMonitor();
		Assert.notNull(targetMonitor, TARGET_MONITOR_CANNOT_BE_NULL);

		final String id = targetMonitor.getId();
		Assert.notNull(id, TARGET_ID_CANNOT_BE_NULL);

		Monitor enclosureMonitor = createMonitor(MonitorNameBuilder.buildEnclosureName(monitorBuildingInfo), null);

		discoverEnclosureIpAddress(enclosureMonitor);
	}
	
	private void discoverEnclosureIpAddress(Monitor enclosureMonitor) {
		final String enclosureHostname = enclosureMonitor.getMetadata("DeviceHostname");
		String ipAddress = NetworkHelper.resolveDns(enclosureHostname);
		if (ipAddress != null) {
			enclosureMonitor.addMetadata("ipAddress", ipAddress);
		}
	}

	@Override
	public void visit(Fan fan) {

		createMonitor(MonitorNameBuilder.buildFanName(monitorBuildingInfo), null);
	}

	@Override
	public void visit(Gpu gpu) {

		createMonitor(MonitorNameBuilder.buildGpuName(monitorBuildingInfo), null);
	}

	@Override
	public void visit(Led led) {

		createMonitor(MonitorNameBuilder.buildLedName(monitorBuildingInfo), null);
	}

	@Override
	public void visit(LogicalDisk logicalDisk) {

		createMonitor(MonitorNameBuilder.buildLogicalDiskName(monitorBuildingInfo), null);
	}

	@Override
	public void visit(Lun lun) {

		final Monitor lunMonitor = createMonitor(MonitorNameBuilder.buildLunName(monitorBuildingInfo), null);

		if (lunMonitor != null) {
			setExpectedPathCount(lunMonitor);
		}

	}

	@Override
	public void visit(Memory memory) {

		createMonitor(MonitorNameBuilder.buildMemoryName(monitorBuildingInfo), null);
	}

	@Override
	public void visit(NetworkCard networkCard) {

		final Monitor networkCardMonitor = createMonitor(MonitorNameBuilder.buildNetworkCardName(monitorBuildingInfo), null);

		if (networkCardMonitor != null) {
			normalizeErrorPercentThresholds(networkCardMonitor);
		}
	}

	@Override
	public void visit(OtherDevice otherDevice) {

		createMonitor(MonitorNameBuilder.buildOtherDeviceName(monitorBuildingInfo), null);
	}

	@Override
	public void visit(PhysicalDisk physicalDisk) {

		createMonitor(MonitorNameBuilder.buildPhysicalDiskName(monitorBuildingInfo), null);
	}

	@Override
	public void visit(PowerSupply powerSupply) {

		createMonitor(MonitorNameBuilder.buildPowerSupplyName(monitorBuildingInfo), null);
	}

	@Override
	public void visit(Robotics robotics) {

		createMonitor(MonitorNameBuilder.buildRoboticsName(monitorBuildingInfo), null);
	}

	@Override
	public void visit(Vm vm) {

		createMonitor(MonitorNameBuilder.buildVmName(monitorBuildingInfo), null);
	}

	@Override
	public void visit(TapeDrive tapeDrive) {

		createMonitor(MonitorNameBuilder.buildTapeDriveName(monitorBuildingInfo), null);
	}

	@Override
	public void visit(Temperature temperature) {

		createMonitor(MonitorNameBuilder.buildTemperatureName(monitorBuildingInfo), null);
	}

	@Override
	public void visit(Voltage voltage) {

		createMonitor(MonitorNameBuilder.buildVoltageName(monitorBuildingInfo), null);
	}

	/**
	 * Create the monitor with given <code>monitorName</code> and <code>parentId</code>
	 * @param monitorName The name of the monitor instance provided in the {@link MonitorBuildingInfo}.
	 *                    If <code>null</code> the monitor is not created
	 * @param parentId    The parent identifier of the current monitor. If <code>null</code> then {@link HostMonitoring}
	 *                    will try to detect and build the parent id.
	 * @return Created {@link Monitor} instance
	 */
	Monitor createMonitor(final String monitorName, final String parentId) {

		checkBuildingInfo(monitorBuildingInfo);

		final Monitor monitor = monitorBuildingInfo.getMonitor();
		final String connectorName = monitorBuildingInfo.getConnectorName();
		final Monitor targetMonitor = monitorBuildingInfo.getTargetMonitor();
		final IHostMonitoring hostMonitoring = monitorBuildingInfo.getHostMonitoring();
		final MonitorType monitorType = monitorBuildingInfo.getMonitorType();
		final String hostname = monitorBuildingInfo.getHostname();

		final Map<String, String> metadata = monitor.getMetadata();
		Assert.notNull(metadata, METADATA_CANNOT_BE_NULL);

		if (monitorName == null) {
			log.error(CANNOT_CREATE_MONITOR_NULL_NAME_MSG,
					hostname, 
					monitorType.getNameInConnector(),
					connectorName);
			return null;
		}

		// Get the id metadata value which is going to be used to create the
		// monitor
		final String id = metadata.get(DEVICE_ID);
		if (!checkNotBlankDataValue(id)) {
			log.error(CANNOT_CREATE_MONITOR_ERROR_MSG,
					hostname,
					monitorType.getNameInConnector(),
					id,
					connectorName);
			return null;
		}

		final String extendedType = getTextDataValueOrElse(metadata.get(TYPE), monitorType.getNameInConnector());
		final String attachedToDeviceId = getTextDataValueOrElse(metadata.get(ATTACHED_TO_DEVICE_ID), null);
		final String attachedToDeviceType = getTextDataValueOrElse(metadata.get(ATTACHED_TO_DEVICE_TYPE), null);

		monitor.setName(monitorName);
		monitor.setParentId(parentId);
		monitor.setTargetId(targetMonitor.getId());
		monitor.setExtendedType(extendedType);
		monitor.addMetadata(TARGET_FQDN, targetMonitor.getFqdn());

		// Finally we can add the monitor
		return hostMonitoring.addMonitor(
					monitor,
					id,
					connectorName,
					monitorType,
					attachedToDeviceId,
					attachedToDeviceType
				);

	}

	private static String getTextDataValueOrElse(final String data, final String other) {
		return checkNotBlankDataValue(data) ? data : other;
	}

	private static boolean checkNotBlankDataValue(final String data) {
		return data != null && !data.trim().isEmpty();
	}


	/**
	 * Check {@link MonitorBuildingInfo} required fields
	 * @param monitorBuildingInfo Wraps all the required field used to discover a monitor
	 */
	static void checkBuildingInfo(final MonitorBuildingInfo monitorBuildingInfo) {

		Assert.notNull(monitorBuildingInfo.getMonitor(), MONITOR_CANNOT_BE_NULL);
		Assert.notNull(monitorBuildingInfo.getMonitorType(), MONITOR_TYPE_CANNOT_BE_NULL);
		Assert.isTrue(hasConnectorName(monitorBuildingInfo), CONNECTOR_NAME_CANNOT_BE_NULL);
		Assert.isTrue(hasTargetMonitor(monitorBuildingInfo), TARGET_MONITOR_CANNOT_BE_NULL);
		Assert.notNull(monitorBuildingInfo.getTargetType(), TARGET_TYPE_CANNOT_BE_NULL);
		Assert.notNull(monitorBuildingInfo.getHostMonitoring(), HOST_MONITORING_CANNOT_BE_NULL);
		Assert.notNull(monitorBuildingInfo.getHostname(), HOSTNAME_CANNOT_BE_NULL);
	}

	/**
	 * Check if the given building information embeds the target monitor excluding the target monitor itself.
	 * 
	 * @param monitorBuildingInfo Wraps all the required field used to discover a monitor
	 * @return <code>true</code> if the target is found otherwise <code>false</code>
	 */
	static boolean hasTargetMonitor(final MonitorBuildingInfo monitorBuildingInfo) {
		return isTargetType(monitorBuildingInfo) || monitorBuildingInfo.getTargetMonitor() != null;
	}

	/**
	 * Check if the given building information embeds the connector name excluding the target monitor.
	 * 
	 * @param monitorBuildingInfo Wraps all the required field used to discover a monitor
	 * @return <code>true</code> if the connector name is found otherwise <code>false</code>
	 */
	static boolean hasConnectorName(final MonitorBuildingInfo monitorBuildingInfo) {
		return isTargetType(monitorBuildingInfo) || monitorBuildingInfo.getConnectorName() != null;
	}

	/**
	 * Check if the given {@link MonitorBuildingInfo} are defined for a Target monitor
	 * 
	 * @param monitorBuildingInfo Wraps all the required field used to discover a monitor
	 * @return <code>true</code> if the monitor is a Target otherwise <code>false</code>
	 */
	static boolean isTargetType(final MonitorBuildingInfo monitorBuildingInfo) {
		Assert.notNull(monitorBuildingInfo.getMonitorType(), MONITOR_TYPE_CANNOT_BE_NULL);
		return monitorBuildingInfo.getMonitorType().equals(MonitorType.TARGET);
	}

	/**
	 * Set the Expected Path Count metadata
	 * 
	 * @param lun the monitor we wish to update its expectedPathCount metadata
	 */
	void setExpectedPathCount(@NonNull final Monitor lun) {
		// For now, it has been decided that expectedPathCount = availablePathWarning + 1
		final Double availablePathWarning = NumberHelper.parseDouble(lun.getMetadata(AVAILABLE_PATH_WARNING), null);

		if (availablePathWarning != null) {
			lun.addMetadata(EXPECTED_PATH_COUNT, Double.toString(availablePathWarning + 1));
		}
	}

	/**
	 * Normalize the network card error percent thresholds
	 * 
	 * @param networkCard The monitor we wish to update its {@value HardwareConstants#ERROR_PERCENT_WARNING_THRESHOLD}
	 *                    and {@value HardwareConstants#ERROR_PERCENT_ALARM_THRESHOLD} metadata
	 */
	void normalizeErrorPercentThresholds(@NonNull final Monitor networkCard) {
		final Double errorPercentWarningThreshold = NumberHelper.parseDouble(networkCard.getMetadata(ERROR_PERCENT_WARNING_THRESHOLD), null);
		final Double errorPercentAlarmThreshold = NumberHelper.parseDouble(networkCard.getMetadata(ERROR_PERCENT_ALARM_THRESHOLD), null);

		if (!CollectHelper.isValidPercentage(errorPercentWarningThreshold) || !CollectHelper.isValidPercentage(errorPercentAlarmThreshold)) {
			networkCard.addMetadata(ERROR_PERCENT_WARNING_THRESHOLD, "20");
			networkCard.addMetadata(ERROR_PERCENT_ALARM_THRESHOLD, "30");
		}
	}
}
