package com.sentrysoftware.matrix.common.helpers;

public class HardwareConstants {

	private HardwareConstants() {
	}

	public static final String NEW_LINE = "\n";
	public static final String WHITE_SPACE = " ";
	public static final String DASH = "-";
	public static final String EMPTY = "";
	public static final String ID_SEPARATOR = "_";
	public static final String ENCLOSURE = "Enclosure";
	public static final String COMPUTER = "Computer";
	public static final String STORAGE = "Storage";
	public static final String PARENTHESIS_EMPTY = "()";
	public static final String OPENING_PARENTHESIS = "(";
	public static final String CLOSING_PARENTHESIS = ")";
	public static final String OPENING_SQUARE_BRACKET = "[";
	public static final String CLOSING_SQUARE_BRACKET = "]";
	public static final String SEMICOLON = ";";
	public static final String COLUMN_REGEXP = "^\\s*column\\((\\d+)\\)\\s*$";
	public static final String LOCALHOST = "localhost";
	public static final String REMOTE = "remote";
	public static final String COMMA = ",";
	public static final String COLON = ":";
	public static final String DOT = ".";
	public static final String BACKSLASH = "\\";
	public static final String DOUBLE_BACKSLASH = BACKSLASH + BACKSLASH;

	public static final String STATUS_PARAMETER = "status";
	public static final String TEST_REPORT_PARAMETER = "testReport";
	public static final String STATUS_INFORMATION_PARAMETER = "statusInformation";
	public static final String BATTERY_STATUS_PARAMETER = "batteryStatus";
	public static final String CONTROLLER_STATUS_PARAMETER = "controllerStatus";
	public static final String INTRUSION_STATUS_PARAMETER = "intrusionStatus";
	public static final String ENERGY_USAGE_PARAMETER = "energyUsage";
	public static final String POWER_CONSUMPTION_PARAMETER = "powerConsumption";
	public static final String POWER_STATE_PARAMETER = "powerState";
	public static final String SPEED_PARAMETER = "speed";
	public static final String CURRENT_SPEED_PARAMETER = "currentSpeed";
	public static final String SPEED_PERCENT_PARAMETER = "speedPercent";
	public static final String VOLTAGE_PARAMETER = "voltage";
	public static final String TEMPERATURE_PARAMETER = "temperature";
	public static final String ERROR_COUNT_PARAMETER = "errorCount";
	public static final String HARD_ERROR_COUNT_PARAMETER = "hardErrorCount";
	public static final String ILLEGAL_REQUEST_ERROR_COUNT_PARAMETER = "illegalRequestErrorCount";
	public static final String MEDIA_ERROR_COUNT_PARAMETER = "mediaErrorCount";
	public static final String NO_DEVICE_ERROR_COUNT_PARAMETER = "noDeviceErrorCount";
	public static final String DEVICE_NOT_READY_ERROR_COUNT_PARAMETER = "deviceNotReadyErrorCount";
	public static final String CORRECTED_ERROR_COUNT_PARAMETER = "correctedErrorCount";
	public static final String RECOVERABLE_ERROR_COUNT_PARAMETER = "recoverableErrorCount";
	public static final String TRANSPORT_ERROR_COUNT_PARAMETER = "transportErrorCount";
	public static final String ERROR_STATUS_PARAMETER = "errorStatus";
	public static final String PREDICTED_FAILURE_PARAMETER = "predictedFailure";
	public static final String PRESENT_PARAMETER = "present";
	public static final String CHARGE_PARAMETER = "charge";
	public static final String COLOR_PARAMETER = "color";
	public static final String LED_INDICATOR_PARAMETER = "LEDIndicator";
	public static final String UNALLOCATED_SPACE_PARAMETER = "unallocatedSpace";
	public static final String AVAILABLE_PATH_COUNT_PARAMETER = "availablePathCount";
	public static final String AVAILABLE_PATH_INFORMATION_PARAMETER = "availablePathInformation";
	public static final String BANDWIDTH_UTILIZATION_INFORMATION_PARAMETER = "bandwidthUtilization";
	public static final String DUPLEX_MODE_PARAMETER = "duplexMode";
	public static final String ERROR_PERCENT_PARAMETER = "errorPercent";
	public static final String LINK_SPEED_PARAMETER = "linkSpeed";
	public static final String LINK_STATUS_PARAMETER = "linkSTatus";
	public static final String RECEIVED_BYTES_RATE_PARAMETER = "ReceivedBytesRate";
	public static final String RECEIVED_PACKETS_RATE_PARAMETER = "ReceivedPacketsRate";
	public static final String TRANSMITTED_BYTES_RATE_PARAMETER = "TransmittedBytesRate";
	public static final String TRANSMITTED_PACKETS_RATE_PARAMETER = "TransmittedPacketsRate";
	public static final String ZERO_BUFFER_CREDIT_PERCENT_PARAMETER = "ZeroBufferCreditPercent";
	public static final String USAGE_COUNT_PARAMETER = "usageCount";
	public static final String VALUE_PARAMETER = "value";
	public static final String ENDURANCE_REMAINING_PARAMETER = "enduranceRemaining";
	public static final String USED_CAPACITY_PARAMETER = "usedCapacity";
	public static final String MOVE_COUNT_PARAMETER = "moveCount";
	public static final String MOUNT_COUNT_PARAMETER = "mountCount";
	public static final String UNMOUNT_COUNT_PARAMETER = "unmountCount";
	public static final String NEEDS_CLEANING_PARAMETER = "needsCleaning";
	public static final String USED_TIME_PERCENT_PARAMETER = "usedTimePercent";


	public static final String SERIAL_NUMBER = "serialNumber";
	public static final String MODEL = "model";
	public static final String VENDOR = "vendor";
	public static final String TYPE = "type";
	public static final String DEVICE_ID = "deviceId";
	public static final String ID_COUNT = "idCount";
	public static final String DISPLAY_ID = "displayId";
	public static final String LOCATION = "location";
	public static final String CONNECTOR = "connector";
	public static final String OPERATING_SYSTEM_TYPE = "operatingSystemType";
	public static final String ATTACHED_TO_DEVICE_ID = "attachedToDeviceId";
	public static final String ATTACHED_TO_DEVICE_TYPE = "attachedToDeviceType";

	public static final String STATUS_PARAMETER_UNIT = "{0 = OK ; 1 = Degraded ; 2 = Failed}";
	public static final String LED_INDICATOR_PARAMETER_UNIT = "{0 = Off ; 1 = Blinking ; 2 = On}";
	public static final String INTRUSION_STATUS_PARAMETER_UNIT = "{0 = OK ; 2 = Intrusion Detected}";
	public static final String ENERGY_USAGE_PARAMETER_UNIT = "Joules";
	public static final String POWER_CONSUMPTION_PARAMETER_UNIT = "Watts";
	public static final String POWER_STATE_PARAMETER_UNIT = "{0 = Off ; 2 = On}";
	public static final String LINK_STATUS_PARAMETER_UNIT = "{0 = Plugged ; 1 = Unplugged}";
	public static final String SPEED_PARAMETER_UNIT = "RPM";
	public static final String CURRENT_SPEED_PARAMETER_UNIT = "MHz";
	public static final String SPEED_PERCENT_PARAMETER_UNIT = "% of maximum speed";
	public static final String PERCENT_PARAMETER_UNIT = "%";
	public static final String VOLTAGE_PARAMETER_UNIT = "mV";
	public static final String TEMPERATURE_PARAMETER_UNIT = "degrees Celsius";
	public static final String ERROR_COUNT_PARAMETER_UNIT = "errors";
	public static final String ERROR_STATUS_PARAMETER_UNIT = "{0 = No Errors ; 1 = Detected Errors ; 2 = Too Many Errors}";
	public static final String PREDICTED_FAILURE_PARAMETER_UNIT  = "{0 = OK ; 1 = Failure Predicted}";
	public static final String PRESENT_PARAMETER_UNIT  = "{0 = Missing ; 1 = Present}";
	public static final String SPACE_GB_PARAMETER_UNIT = "GB";
	public static final String SPEED_MBITS_PARAMETER_UNIT = "Mbits/s";
	public static final String BYTES_RATE_PARAMETER_UNIT = "MB/s";
	public static final String PATHS_PARAMETER_UNIT = "paths";
	public static final String USAGE_COUNT_PARAMETER_UNIT = "times";
	public static final String MOVE_COUNT_PARAMETER_UNIT = "moves";
	public static final String MOUNT_COUNT_PARAMETER_UNIT = "mounts";
	public static final String UNMOUNT_COUNT_PARAMETER_UNIT = "unmounts";
	public static final String PACKETS_RATE_PARAMETER_UNIT = "packets/s";
	public static final String DUPLEX_MODE_PARAMETER_UNIT = "{0 =  Half-duplex ; 1 = Full-duplex}";
	public static final String NEEDS_CLEANING_PARAMETER_UNIT = "{0 =  OK ; 1 = Cleaning Needed ; 2 = Cleaning Needed Immediately}";

	public static final String ONE = "1";
	public static final String ZERO = "0";

}
