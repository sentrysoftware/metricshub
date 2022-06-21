package com.sentrysoftware.matrix.engine.strategy.discovery;

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
import com.sentrysoftware.matrix.common.meta.monitor.Host;
import com.sentrysoftware.matrix.common.meta.monitor.Temperature;
import com.sentrysoftware.matrix.common.meta.monitor.Vm;
import com.sentrysoftware.matrix.common.meta.monitor.Voltage;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.strategy.IMonitorVisitor;
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
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.HOST_FQDN;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TYPE;

@Slf4j
public class MonitorDiscoveryVisitor implements IMonitorVisitor {

	private static final String CANNOT_CREATE_MONITOR_NULL_NAME_MSG = "Hostname {} - Cannot create monitor {} with null name. Connector {}";
	private static final String HOSTNAME_CANNOT_BE_NULL = "hostname cannot be null.";
	private static final String HOST_MONITORING_CANNOT_BE_NULL = "hostMonitoring cannot be null.";
	private static final String CONNECTOR_NAME_CANNOT_BE_NULL = "connectorName cannot be null.";
	private static final String MONITOR_CANNOT_BE_NULL = "monitor cannot be null.";
	private static final String HOST_ID_CANNOT_BE_NULL = "host id cannot be null.";
	private static final String HOST_MONITOR_CANNOT_BE_NULL = "hostMonitor cannot be null.";
	private static final String HOST_TYPE_CANNOT_BE_NULL = "hostType cannot be null.";
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
	public void visit(Host host) {

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
		final Monitor hostMonitor = monitorBuildingInfo.getHostMonitor();
		Assert.notNull(hostMonitor, HOST_MONITOR_CANNOT_BE_NULL);

		final String id = hostMonitor.getId();
		Assert.notNull(id, HOST_ID_CANNOT_BE_NULL);

		Monitor enclosureMonitor = createMonitor(MonitorNameBuilder.buildEnclosureName(monitorBuildingInfo), null);

		// Check if this enclosure is really created.
		if (enclosureMonitor != null) {
			discoverEnclosureIpAddress(enclosureMonitor);
		}
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

		createMonitor(MonitorNameBuilder.buildLunName(monitorBuildingInfo), null);
	}

	@Override
	public void visit(Memory memory) {

		createMonitor(MonitorNameBuilder.buildMemoryName(monitorBuildingInfo), null);
	}

	@Override
	public void visit(NetworkCard networkCard) {

		createMonitor(MonitorNameBuilder.buildNetworkCardName(monitorBuildingInfo), null);
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
		final Monitor hostMonitor = monitorBuildingInfo.getHostMonitor();
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
		monitor.setHostId(hostMonitor.getId());
		monitor.setExtendedType(extendedType);
		monitor.addMetadata(HOST_FQDN, hostMonitor.getFqdn());

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
		Assert.isTrue(hasHostMonitor(monitorBuildingInfo), HOST_MONITOR_CANNOT_BE_NULL);
		Assert.notNull(monitorBuildingInfo.getHostType(), HOST_TYPE_CANNOT_BE_NULL);
		Assert.notNull(monitorBuildingInfo.getHostMonitoring(), HOST_MONITORING_CANNOT_BE_NULL);
		Assert.notNull(monitorBuildingInfo.getHostname(), HOSTNAME_CANNOT_BE_NULL);
	}

	/**
	 * Check if the given building information embeds the host monitor excluding the host monitor itself.
	 * 
	 * @param monitorBuildingInfo Wraps all the required field used to discover a monitor
	 * @return <code>true</code> if the host is found otherwise <code>false</code>
	 */
	static boolean hasHostMonitor(final MonitorBuildingInfo monitorBuildingInfo) {
		return isHostType(monitorBuildingInfo) || monitorBuildingInfo.getHostMonitor() != null;
	}

	/**
	 * Check if the given building information embeds the connector name excluding the host monitor.
	 * 
	 * @param monitorBuildingInfo Wraps all the required field used to discover a monitor
	 * @return <code>true</code> if the connector name is found otherwise <code>false</code>
	 */
	static boolean hasConnectorName(final MonitorBuildingInfo monitorBuildingInfo) {
		return isHostType(monitorBuildingInfo) || monitorBuildingInfo.getConnectorName() != null;
	}

	/**
	 * Check if the given {@link MonitorBuildingInfo} are defined for a Host monitor
	 * 
	 * @param monitorBuildingInfo Wraps all the required field used to discover a monitor
	 * @return <code>true</code> if the monitor is a Host otherwise <code>false</code>
	 */
	static boolean isHostType(final MonitorBuildingInfo monitorBuildingInfo) {
		Assert.notNull(monitorBuildingInfo.getMonitorType(), MONITOR_TYPE_CANNOT_BE_NULL);
		return monitorBuildingInfo.getMonitorType().equals(MonitorType.HOST);
	}

}
