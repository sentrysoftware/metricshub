package com.sentrysoftware.matrix.common.helpers;

public class HardwareConstants {

	private HardwareConstants() {
	}

	public static final String NEW_LINE = "\n";
	public static final String WHITE_SPACE = " ";
	public static final String EMPTY = "";
	public static final String ID_SEPARATOR = "_";
	public static final String ENCLOSURE = "Enclosure";
	public static final String COMPUTER = "Computer";
	public static final String STORAGE = "Storage";
	public static final String PARENTHESIS_EMPTY = "()";
	public static final String OPENING_PARENTHESIS = "(";
	public static final String CLOSING_PARENTHESIS = ")";
	public static final String SEMICOLON = ";";
	public static final String COLUMN_REGEXP = "^\\s*column\\((\\d+)\\)\\s*$";
	public static final String LOCALHOST = "localhost";
	public static final String REMOTE = "remote";
	public static final String COMMA = ",";
	public static final String COLON = ":";

	public static final String STATUS_PARAMETER = "status";
	public static final String TEST_REPORT_PARAMETER = "testReport";
	public static final String STATUS_INFORMATION_PARAMETER = "statusInformation";
	public static final String INTRUSION_STATUS_PARAMETER = "intrusionStatus";
	public static final String ENERGY_USAGE_PARAMETER = "energyUsage";
	
	public static final String SERIAL_NUMBER = "serialNumber";
	public static final String MODEL = "model";
	public static final String VENDOR = "vendor";
	public static final String TYPE = "type";
	public static final String DEVICE_ID = "deviceId";
	public static final String ID_COUNT = "idCount";
	public static final String DISPLAY_ID = "displayId";
	public static final String LOCATION = "Location";
	public static final String OPERATING_SYSTEM_TYPE = "operatingSystemType";
	public static final String ATTACHED_TO_DEVICE_ID = "attachedToDeviceId";
	public static final String ATTACHED_TO_DEVICE_TYPE = "attachedToDeviceType";

	public static final String STATUS_PARAMETER_UNIT = "{0 = OK ; 1 = Degraded ; 2 = Failed}";
	public static final String INTRUSION_STATUS_PARAMETER_UNIT = "{0 = OK ; 2 = Intrusion Detected}";
	public static final String ENERGY_USAGE_PARAMETER_UNIT = "Joules";

}
