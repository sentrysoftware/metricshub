package com.sentrysoftware.matrix.converter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access =  AccessLevel.PRIVATE)
public class ConverterConstants {

	public static final String EMPTY_STRING = "";
	public static final String COMMA = ",";
	public static final String SEMICOLON = ";";
	public static final String COLON = ":";
	public static final String SPACE = " ";
	public static final String COLON_SPACE = COLON + SPACE;
	public static final String DOT = ".";
	public static final String OPENING_PARENTHESIS = "(";
	public static final String CLOSING_PARENTHESIS = ")";

	public static final String TRUE = "true";
	public static final String ONE = "1";
	public static final String PERCENT = "%";

	public static final String DOUBLE_QUOTES_REGEX_REPLACEMENT = "^\\s*\"(.*)\"\\s*$";
	public static final String SOURCE_REFERENCE_REGEX_REPLACEMENT = "^\\s*%(.*)%\\s*$";

	public static final String INTEGER_REGEX = "^[1-9]\\d*$";
	public static final String EMBEDDED_FILE_REGEX = "^\\s*embeddedfile\\(([1-9]\\d*)\\)\\s*$";

	public static final String CONNECTOR = "connector";

	public static final String DETECTION = "detection";
	public static final String CRITERIA = "criteria";

	public static final String DISCOVERY = "discovery";
	public static final String COLLECT = "collect";

	public static final String MONO_INSTANCE_CAMEL_CASE = "monoInstance";
	public static final String MULTI_INSTANCE_CAMEL_CASE = "multiInstance";

	public static final String MONO_INSTANCE = MONO_INSTANCE_CAMEL_CASE.toLowerCase();
	public static final String MULTI_INSTANCE = MULTI_INSTANCE_CAMEL_CASE.toLowerCase();

	public static final String TYPE = "type";
	public static final String VALUE_TABLE = "valuetable";

	public static final String DETECTION_DOT_CRITERIA = "detection.criteria";
	public static final String DOT_COMPUTE = ".compute";

	public static final String SET_COLUMN = "setColumn";
	public static final String SET_EXPECTED_RESULT = "setExpectedResult";
	public static final String SET_ERROR_MESSAGE = "setErrorMessage";
	public static final String SET_WBEM_QUERY = "setWbemQuery";
	public static final String SET_WBEM_NAMESPACE = "setWbemNamespace";
	public static final String SET_TIMEOUT = "setTimeout";
	public static final String SET_TEXT = "setText";

	public static final String DEFAULT = "default";

	public static final String IPMI_TOOL = "ipmitool";

	public static final String SOURCES = "sources";
	public static final String MONITORS = "monitors";
	public static final String COMPUTES = "computes";
	public static final String MAPPING = "mapping";
	public static final String ATTRIBUTES = "attributes";
	public static final String SOURCE = "source";

	// Monitor types
	public static final String HDF_BATTERY = "battery";
	public static final String HDF_BLADE = "blade";
	public static final String HDF_CPU = "cpu";
	public static final String HDF_CPU_CORE = "cpucore";
	public static final String HDF_DISK_CONTROLLER = "diskcontroller";
	public static final String HDF_ENCLOSURE = "enclosure";
	public static final String HDF_FAN = "fan";
	public static final String HDF_GPU = "gpu";
	public static final String HDF_LED = "led";
	public static final String HDF_LOGICAL_DISK = "logicaldisk";
	public static final String HDF_LUN = "lun";
	public static final String HDF_MEMORY = "memory";
	public static final String HDF_NETWORK_CARD = "networkcard";
	public static final String HDF_OTHER_DEVICE = "otherdevice";
	public static final String HDF_PHYSICAL_DISK = "physicaldisk";
	public static final String HDF_POWER_SUPPLY = "powersupply";
	public static final String HDF_ROBOTIC = "robotic";
	public static final String HDF_TAPEDRIVE = "tapedrive";
	public static final String HDF_TEMPERATURE = "temperature";
	public static final String HDF_VM = "vm";
	public static final String HDF_VOLTAGE = "voltage";

	public static final String YAML_BATTERY = HDF_BATTERY;
	public static final String YAML_BLADE = HDF_BLADE;
	public static final String YAML_CPU = HDF_CPU;
	public static final String YAML_CPU_CORE = "cpu_core";
	public static final String YAML_DISK_CONTROLLER = "disk_controller";
	public static final String YAML_ENCLOSURE = HDF_ENCLOSURE;
	public static final String YAML_FAN = HDF_FAN;
	public static final String YAML_GPU = HDF_GPU;
	public static final String YAML_LED = HDF_LED;
	public static final String YAML_LOGICAL_DISK = "logical_disk";
	public static final String YAML_LUN = HDF_LUN;
	public static final String YAML_MEMORY = HDF_MEMORY;
	public static final String YAML_NETWORK = "network";
	public static final String YAML_OTHER_DEVICE = "other_device";
	public static final String YAML_PHYSICAL_DISK = "physical_disk";
	public static final String YAML_POWER_SUPPLY = "power_supply";
	public static final String YAML_ROBOTICS = "robotics";
	public static final String YAML_TAPEDRIVE = "tape_drive";
	public static final String YAML_TEMPERATURE = HDF_TEMPERATURE;
	public static final String YAML_VM = HDF_VM;
	public static final String YAML_VOLTAGE = HDF_VOLTAGE;

	// HDF properties
	public static final String HDF_CHEMISTRY = "chemistry";
	public static final String HDF_TYPE = "type";
	public static final String HDF_MODEL = "model";
	public static final String HDF_VENDOR = "vendor";
	public static final String HDF_DISPLAY_ID = "displayid";
	public static final String HDF_DEVICE_ID = "deviceid";

	// YAML attributes
	public static final String YAML_TYPE = HDF_TYPE;
	public static final String YAML_MODEL = HDF_MODEL;
	public static final String YAML_VENDOR = HDF_VENDOR;
	public static final String YAML_DISPLAY_ID = "__display_id";
	public static final String YAML_ID = "id";
	public static final String YAML_NAME = "name";
	public static final String YAML_HW_PARENT_ID = "hw.parent.id";
	public static final String YAML_HW_PARENT_TYPE = "hw.parent.type";
	public static final String YAML_CHEMISTRY = HDF_CHEMISTRY;
}