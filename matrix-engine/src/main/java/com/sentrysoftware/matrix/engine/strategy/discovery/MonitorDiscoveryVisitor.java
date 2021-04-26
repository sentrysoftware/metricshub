package com.sentrysoftware.matrix.engine.strategy.discovery;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ATTACHED_TO_DEVICE_ID_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.COMPUTER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DEVICE_ID_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MODEL_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STORAGE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TYPE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.*;

import java.util.Map;

import org.springframework.util.Assert;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.Battery;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.Blade;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.ConcreteConnector;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.Cpu;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.CpuCore;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.Target;
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
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.Temperature;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.Voltage;
import com.sentrysoftware.matrix.engine.strategy.IMonitorVisitor;
import com.sentrysoftware.matrix.engine.target.TargetType;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;
import com.sentrysoftware.matrix.model.parameter.BooleanParam;
import com.sentrysoftware.matrix.model.parameter.IParameterValue;
import com.sentrysoftware.matrix.model.parameter.TextParam;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MonitorDiscoveryVisitor implements IMonitorVisitor {

	private static final String UNKNOWN_COMPUTER = "Unknown computer";
	private static final String HP_TRU64_COMPUTER = "HP Tru64 computer";
	private static final String LINUX_COMPUTER = "Linux computer";
	private static final String HP_OPEN_VMS_COMPUTER = "HP Open-VMS computer";
	private static final String WINDOWS_COMPUTER = "Windows computer";
	private static final String LOCALHOST_ENCLOSURE = "Localhost Enclosure";
	private static final String CANNOT_CREATE_MONITOR_NULL_NAME_MSG = "Cannot create monitor {} with null name. Connector {}. System {}.";
	private static final String NAME_SEPARATOR = ": ";
	private static final String ID_COUNT_PARAM_CANNOT_BE_NULL = "idCountParam cannot be null.";
	private static final String HOSTNAME_CANNOT_BE_NULL = "hostname cannot be null.";
	private static final String HOST_MONITORING_CANNOT_BE_NULL = "hostMonitoring cannot be null.";
	private static final String CONNECTOR_NAME_CANNOT_BE_NULL = "connectorName cannot be null.";
	private static final String MONITOR_CANNOT_BE_NULL = "monitor cannot be null.";
	private static final String TARGET_ID_CANNOT_BE_NULL = "target id cannot be null.";
	private static final String TARGET_MONITOR_CANNOT_BE_NULL = "targetMonitor cannot be null.";
	private static final String TARGET_TYPE_CANNOT_BE_NULL = "targetType cannot be null.";
	private static final String MONITOR_TYPE_CANNOT_BE_NULL = "monitorType cannot be null.";
	private static final String PARAMETERS_CANNOT_BE_NULL = "parameters cannot be null.";
	private static final String MONITOR_BUILDING_INFO_CANNOT_BE_NULL = "monitorBuildingInfo cannot be null.";
	private static final String CANNOT_CREATE_MONITOR_ERROR_MSG = "Cannot create {} with deviceId {}. Connector {}. System {}";

	private MonitorBuildingInfo monitorBuildingInfo;

	private static final String TWO_STRINGS_FORMAT = "%s: %s";

	public MonitorDiscoveryVisitor(MonitorBuildingInfo monitorBuildingInfo) {
		Assert.notNull(monitorBuildingInfo, MONITOR_BUILDING_INFO_CANNOT_BE_NULL);
		checkBuildingInfo(monitorBuildingInfo);
		this.monitorBuildingInfo = monitorBuildingInfo;
	}

	@Override
	public void visit(ConcreteConnector concreteConnector) {
		// No implementation provided
	}

	@Override
	public void visit(Target device) {
		// No implementation provided
	}

	@Override
	public void visit(Battery battery) {
		createMonitor(buildGenericName(), null);
	}

	@Override
	public void visit(Blade blade) {
		createMonitor(buildGenericName(), null);
	}

	@Override
	public void visit(Cpu cpu) {
		createMonitor(buildGenericName(), null);
	}

	@Override
	public void visit(CpuCore cpuCore) {
		createMonitor(buildGenericName(), null);
	}

	@Override
	public void visit(DiskController diskController) {
		createMonitor(buildGenericName(), null);
	}

	@Override
	public void visit(DiskEnclosure diskEnclosure) {
		createMonitor(buildGenericName(), null);
	}

	@Override
	public void visit(Enclosure enclosure) {
		final Monitor targetMonitor = monitorBuildingInfo.getTargetMonitor();
		Assert.notNull(targetMonitor, TARGET_MONITOR_CANNOT_BE_NULL);

		final String id = targetMonitor.getId();
		Assert.notNull(id, TARGET_ID_CANNOT_BE_NULL);

		createMonitor(buildEnclosureName(), id);
	}

	@Override
	public void visit(Fan fan) {
		createMonitor(buildGenericName(), null);
	}

	@Override
	public void visit(Led led) {
		createMonitor(buildGenericName(), null);
	}

	@Override
	public void visit(LogicalDisk logicalDisk) {
		createMonitor(buildGenericName(), null);
	}

	@Override
	public void visit(Lun lun) {
		createMonitor(buildGenericName(), null);
	}

	@Override
	public void visit(Memory memory) {
		createMonitor(buildGenericName(), null);
	}

	@Override
	public void visit(NetworkCard networkCard) {
		createMonitor(buildGenericName(), null);
	}

	@Override
	public void visit(OtherDevice otherDevice) {
		createMonitor(buildGenericName(), null);
	}

	@Override
	public void visit(PhysicalDisk physicalDisk) {
		createMonitor(buildGenericName(), null);
	}

	@Override
	public void visit(PowerSupply powerSupply) {
		createMonitor(buildGenericName(), null);
	}

	@Override
	public void visit(TapeDrive tapeDrive) {
		createMonitor(buildGenericName(), null);
	}

	@Override
	public void visit(Temperature concreteTemperature) {
		createMonitor(buildGenericName(), null);
	}

	@Override
	public void visit(Voltage concreteVoltage) {
		createMonitor(buildGenericName(), null);
	}

	@Override
	public void visit(Robotic concreteRobotic) {
		createMonitor(buildGenericName(), null);
	}

	/**
	 * Create the monitor with given <code>monitorName</code> and <code>parentId</code>
	 * @param monitorName The name of the monitor instance provided in the {@link MonitorBuildingInfo}.
	 *                    If <code>null</code> the monitor is not created
	 * @param parentId    The parent identifier of the current monitor. If <code>null</code> then {@link HostMonitoring}
	 *                    will try to detect and build the parent id.
	 */
	protected void createMonitor(final String monitorName, final String parentId) {

		checkBuildingInfo(monitorBuildingInfo);

		final Monitor monitor = monitorBuildingInfo.getMonitor();
		final String connectorName = monitorBuildingInfo.getConnectorName();
		final Monitor targetMonitor = monitorBuildingInfo.getTargetMonitor();
		final IHostMonitoring hostMonitoring = monitorBuildingInfo.getHostMonitoring();
		final MonitorType monitorType = monitorBuildingInfo.getMonitorType();
		final String hostname = monitorBuildingInfo.getHostname();

		final Map<String, IParameterValue> parameters = monitor.getParameters();
		Assert.notNull(parameters, PARAMETERS_CANNOT_BE_NULL);

		if (monitorName == null) {
			log.error(CANNOT_CREATE_MONITOR_NULL_NAME_MSG,
					monitorType.getName(),
					connectorName,
					hostname);
			return;
		}

		// Get the id parameter value which is going to be used to create the
		// enclosure monitor
		final TextParam idParam = (TextParam) parameters.get(DEVICE_ID_PARAMETER);
		if (!checkNotBlankParameterValue(idParam)) {
			log.error(CANNOT_CREATE_MONITOR_ERROR_MSG,
					monitorType.getName(),
					idParam,
					connectorName,
					hostname);
			return;
		}

		final String extendedType = getTextParamValueOrElse((TextParam) parameters.get(TYPE_PARAMETER), monitorType.getName());
		final String attachedToDeviceId = getTextParamValueOrElse((TextParam) parameters.get(ATTACHED_TO_DEVICE_ID_PARAMETER), null);
		final String attachedToDeviceType = getTextParamValueOrElse((TextParam) parameters.get(ATTACHED_TO_DEVICE_ID_PARAMETER), null);

		monitor.setName(monitorName);
		monitor.setParentId(parentId);
		monitor.setTargetId(targetMonitor.getId());
		monitor.setExtendedType(extendedType);

		// Finally we can add the monitor
		hostMonitoring.addMonitor(monitor,
				idParam.getValue(),
				connectorName,
				monitorType,
				attachedToDeviceId,
				attachedToDeviceType);
	}

	public static String getTextParamValueOrElse(final TextParam textParam, final String other) {
		return (textParam != null) ? textParam.getValueOrElse(other) : other;
	}

	protected static boolean checkNotBlankParameterValue(final TextParam textParam) {
		return textParam != null && textParam.getValue() != null && !textParam.getValue().trim().isEmpty();
	}

	/**
	 * Build the enclosure name based on the current implementation in Hardware Sentry KM
	 * 
	 * @return {@link String} value
	 */
	protected String buildEnclosureName() {

		final TargetType targetType = monitorBuildingInfo.getTargetType();
		Assert.notNull(targetType, TARGET_TYPE_CANNOT_BE_NULL);

		final Monitor targetMonitor = monitorBuildingInfo.getTargetMonitor();
		Assert.notNull(targetMonitor, TARGET_MONITOR_CANNOT_BE_NULL);

		final Map<String, IParameterValue> parameters = monitorBuildingInfo.getMonitor().getParameters();
		Assert.notNull(parameters, PARAMETERS_CANNOT_BE_NULL);

		final String type = getTextParamValueOrElse((TextParam) parameters.get(TYPE_PARAMETER), ENCLOSURE);
		final String vendor = getTextParamValueOrElse((TextParam) parameters.get(VENDOR_PARAMETER), null);
		final String model = getTextParamValueOrElse((TextParam) parameters.get(MODEL_PARAMETER), null);
		final String enclosureDisplayId = getTextParamValueOrElse((TextParam) parameters.get(DISPLAY_ID_PARAMETER), null);
		final String enclosureIdCount = getTextParamValueOrElse((TextParam) parameters.get(ID_COUNT_PARAMETER), null);

		final StringBuilder nameBuilder = new StringBuilder(type).append(NAME_SEPARATOR);

		// If enclosureDisplayID is specified, use it and put the rest in parenthesis
		if (enclosureDisplayId != null) {
			nameBuilder.append(enclosureDisplayId).append(" (");
		}

		// Add model and vendor... or if none, some basic architecture information
		if (vendor != null && model != null) {
			if (model.toLowerCase().indexOf(vendor.toLowerCase()) != -1) {
				nameBuilder.append(model);
			} else {
				nameBuilder.append(vendor).append(" ").append(model);
			}
		} else if (vendor != null) {
			nameBuilder.append(vendor);
		} else if (model != null) {
			nameBuilder.append(model);
		} else if (COMPUTER.equalsIgnoreCase(type)) {
			nameBuilder.append(handleComputerDisplayName(targetMonitor, targetType));
		} else if (STORAGE.equalsIgnoreCase(type) && enclosureDisplayId == null) {
			nameBuilder.append(" (").append(enclosureIdCount).append(CLOSING_PARENTHESIS);
		}

		// At the end, if we specified enclosureDisplayId, then close the parenthesis
		if (enclosureDisplayId != null) {
			nameBuilder.append(")");
		}

		return nameBuilder.toString().replace(PARENTHESIS_EMPTY, EMPTY).trim();

	}

	/**
	 * Build a Generic name to be set in the {@link Monitor} name
	 * <br>Try to get the display id otherwise check the DeviceID parameter or the idCount parameter
	 * <br>Refine the result before returning the final generic name result
	 * @return {@link String} value
	 */
	protected String buildGenericName() {
		final Map<String, IParameterValue> parameters = monitorBuildingInfo.getMonitor().getParameters();
		Assert.notNull(parameters, PARAMETERS_CANNOT_BE_NULL);

		final MonitorType monitorType = monitorBuildingInfo.getMonitorType();
		Assert.notNull(monitorType, MONITOR_TYPE_CANNOT_BE_NULL);

		final TextParam idParam = (TextParam) parameters.get(DEVICE_ID_PARAMETER);
		final TextParam displayIdParam = (TextParam) parameters.get(DISPLAY_ID_PARAMETER);
		final TextParam idCountParam = (TextParam) parameters.get(ID_COUNT_PARAMETER);
		Assert.notNull(idCountParam, ID_COUNT_PARAM_CANNOT_BE_NULL);
		String name = null;
		if (checkNotBlankParameterValue(displayIdParam)) {
			name = displayIdParam.getValue();
		} else if (checkNotBlankParameterValue(idParam)) {
			name = idParam.getValue();
		} else if (checkNotBlankParameterValue(idCountParam)){
			name = idCountParam.getValue();
		}

		if (name != null && name.toLowerCase().indexOf(monitorType.getName().toLowerCase()) != -1) {
			name = name.replaceAll("(?i)\\s*"+monitorType.getName()+"\\s*", "");
		}

		if (name == null) {
			return null;
		}

		return String.format(TWO_STRINGS_FORMAT, monitorType.getName(), name);
	}

	/**
	 * Handle the computer display name based on the target location. I.e. local or remote
	 * @param targetMonitor Monitor with type {@link MonitorType#TARGET}
	 * @param targetType    The type of the target monitor
	 * @return {@link String} value to append with the full monitor name
	 */
	protected static String handleComputerDisplayName(final Monitor targetMonitor, final TargetType targetType) {
		Assert.notNull(targetMonitor, TARGET_MONITOR_CANNOT_BE_NULL);
		Assert.notNull(targetType, TARGET_TYPE_CANNOT_BE_NULL);

		if (isLocalhost(targetMonitor.getParameters())) {
			// TODO Handle localhost machine type, processor architecture detection
			return LOCALHOST_ENCLOSURE;
		} else {
			switch (targetType) {
			case MS_WINDOWS:
				return WINDOWS_COMPUTER;
			case HP_OPEN_VMS:
				return HP_OPEN_VMS_COMPUTER;
			case LINUX:
				return LINUX_COMPUTER;
			case HP_TRU64_UNIX:
				return HP_TRU64_COMPUTER;
			default:
				return UNKNOWN_COMPUTER;
			}
		}
	}

	/**
	 * Try to get the {@value HardwareConstants#IS_LOCALHOST_PARAMETER} parameter and return its boolean value
	 * Note: {@value HardwareConstants#IS_LOCALHOST_PARAMETER} is computed on {@link MonitorType#TARGET} in the detection operation
	 * @param parameters
	 * @return {@link boolean} value
	 */
	protected static boolean isLocalhost(final Map<String, IParameterValue> parameters) {
		if (parameters != null) {
			final BooleanParam isLocalhostParam = (BooleanParam) parameters.get(IS_LOCALHOST_PARAMETER);
			if (isLocalhostParam != null) {
				return isLocalhostParam.isValue();
			}
		}
		return false;
	}

	/**
	 * Check {@link MonitorBuildingInfo} required fields
	 * @param monitorBuildingInfo Wraps all the required field used to create a monitor
	 */
	private static void checkBuildingInfo(final MonitorBuildingInfo monitorBuildingInfo) {

		Assert.notNull(monitorBuildingInfo.getMonitor(), MONITOR_CANNOT_BE_NULL);
		Assert.notNull(monitorBuildingInfo.getConnectorName(), CONNECTOR_NAME_CANNOT_BE_NULL);
		Assert.notNull(monitorBuildingInfo.getTargetMonitor(), TARGET_MONITOR_CANNOT_BE_NULL);
		Assert.notNull(monitorBuildingInfo.getHostMonitoring(), HOST_MONITORING_CANNOT_BE_NULL);
		Assert.notNull(monitorBuildingInfo.getMonitorType(), MONITOR_TYPE_CANNOT_BE_NULL);
		Assert.notNull(monitorBuildingInfo.getHostname(), HOSTNAME_CANNOT_BE_NULL);
		Assert.notNull(monitorBuildingInfo.getTargetType(), TARGET_TYPE_CANNOT_BE_NULL);
	}
}
