package com.sentrysoftware.matrix.common.helpers;

import java.util.regex.Pattern;

public class HardwareConstants {

	private HardwareConstants() {
	}

	public static final String FULL_DUPLEX_MODE = "Full";
	public static final String HALF_DUPLEX_MODE = "Half";
	public static final String N_A = "N/A";
	public static final String NEW_LINE = "\n";
	public static final String TAB = "\t";
	public static final String WHITE_SPACE = " ";
	public static final String WHITE_SPACE_TAB = WHITE_SPACE + TAB;
	public static final String WHITE_SPACE_REPEAT_REGEX = "[ \t]+";
	public static final String EMPTY = "";
	public static final String ID_SEPARATOR = "_";
	public static final String BLADE_ENCLOSURE = "Blade Enclosure";
	public static final String ENCLOSURE = "Enclosure";
	public static final String COMPUTER = "Computer";
	public static final String STORAGE = "Storage";
	public static final String SWITCH = "Switch";
	public static final String SEMICOLON = ";";
	public static final String TABLE_SEP = SEMICOLON;
	public static final String COLUMN_REGEXP = "^\\s*column\\((\\d+)\\)\\s*$";
	public static final String LOCALHOST = "localhost";
	public static final String REMOTE = "remote";
	public static final String COLON_DOUBLE_SLASH = "://";

	///////////////////////////////////////////////////////////////////////////////
	//                                                                           //
	//                              PARAMETER NAMES                              //
	//                                                                           //
	//                                 .i;;;;i.                                  //
	//                               iYcviii;vXY:                                //
	//                             .YXi       .i1c.                              //
	//                            .YC.     .    in7.                             //
	//                           .vc.   ......   ;1c.                            //
	//                           i7,   ..        .;1.                            //
	//                          i7,   .. ...      .Y1i                           //
	//                         ,7v     .6MMM@;     .YX,                          //
	//                        .7;.   ..IMMMMMM1     :t7.                         //
	//                       .;Y.     ;$MMMMMM9.     :tc.                        //
	//                       vY.   .. .nMMM@MMU.      ;1v.                       //
	//                      i7i   ...  .#MM@M@C. .....:71i                       //
	//                     it:   ....   $MMM@9;.,i;;;i,;tti                      //
	//                    :t7.  .....   0MMMWv.,iii:::,,;St.                     //
	//                   .nC.   .....   IMMMQ..,::::::,.,czX.                    //
	//                  .ct:   ....... .ZMMMI..,:::::::,,:76Y.                   //
	//                  c2:   ......,i..Y$M@t..:::::::,,..inZY                   //
	//                 vov   ......:ii..c$MBc..,,,,,,,,,,..iI9i                  //
	//                i9Y   ......iii:..7@MA,..,,,,,,,,,....;AA:                 //
	//               iIS.  ......:ii::..;@MI....,............;Ez.                //
	//              .I9.  ......:i::::...8M1..................C0z.               //
	//             .z9;  ......:i::::,.. .i:...................zWX.              //
	//             vbv  ......,i::::,,.      ................. :AQY              //
	//            c6Y.  .,...,::::,,..:t0@@QY. ................ :8bi             //
	//           :6S. ..,,...,:::,,,..EMMMMMMI. ............... .;bZ,            //
	//          :6o,  .,,,,..:::,,,..i#MMMMMM#v.................  YW2.           //
	//         .n8i ..,,,,,,,::,,,,.. tMMMMM@C:.................. .1Wn           //
	//         7Uc. .:::,,,,,::,,,,..   i1t;,..................... .UEi          //
	//         7C...::::::::::::,,,,..        ....................  vSi.         //
	//         ;1;...,,::::::,.........       ..................    Yz:          //
	//          v97,.........                                     .voC.          //
	//           izAotX7777777777777777777777777777777777777777Y7n92:            //
	//             .;CoIIIIIUAA666666699999ZZZZZZZZZZZZZZZZZZZZ6ov.              //
	//                                                                           //
	//       PLEASE NOTE: parameter names MUST be written using camelCase        //
	//                                                                           //
	///////////////////////////////////////////////////////////////////////////////

	public static final String STATUS_PARAMETER = "status";
	public static final String TEST_REPORT_PARAMETER = "testReport";
	public static final String STATUS_INFORMATION_PARAMETER = "statusInformation";
	public static final String BATTERY_STATUS_PARAMETER = "batteryStatus";
	public static final String CONTROLLER_STATUS_PARAMETER = "controllerStatus";
	public static final String INTRUSION_STATUS_PARAMETER = "intrusionStatus";
	public static final String ENERGY_USAGE_PARAMETER = "energyUsage";
	public static final String ENERGY_PARAMETER = "energy";
	public static final String POWER_CONSUMPTION_PARAMETER = "powerConsumption";
	public static final String POWER_STATE_PARAMETER = "powerState";
	public static final String SPEED_PARAMETER = "speed";
	public static final String CURRENT_SPEED_PARAMETER = "currentSpeed";
	public static final String SPEED_PERCENT_PARAMETER = "speedPercent";
	public static final String VOLTAGE_PARAMETER = "voltage";
	public static final String TEMPERATURE_PARAMETER = "temperature";
	public static final String AMBIENT_TEMPERATURE_PARAMETER = "ambientTemperature";
	public static final String CPU_TEMPERATURE_PARAMETER = "cpuTemperature";
	public static final String CPU_THERMAL_DISSIPATION_RATE_PARAMETER = "cpuThermalDissipationRate";
	public static final String HEATING_MARGIN_PARAMETER = "heatingMargin";
	public static final String ERROR_COUNT_PARAMETER = "errorCount";
	public static final String CORRECTED_ERROR_COUNT_PARAMETER = "correctedErrorCount";
	public static final String STARTING_ERROR_COUNT_PARAMETER = "startingErrorCount";
	public static final String STARTING_CORRECTED_ERROR_COUNT_PARAMETER = "startingCorrectedErrorCount";
	public static final String ERROR_STATUS_PARAMETER = "errorStatus";
	public static final String PREDICTED_FAILURE_PARAMETER = "predictedFailure";
	public static final String PRESENT_PARAMETER = "present";
	public static final String CHARGE_PARAMETER = "charge";
	public static final String TIME_LEFT_PARAMETER = "timeLeft";
	public static final String COLOR_PARAMETER = "color";
	public static final String LED_INDICATOR_PARAMETER = "ledIndicator";
	public static final String UNALLOCATED_SPACE_PARAMETER = "unallocatedSpace";
	public static final String AVAILABLE_PATH_COUNT_PARAMETER = "availablePathCount";
	public static final String MAX_AVAILABLE_PATH_COUNT_PARAMETER = "maxAvailablePathCount";
	public static final String AVAILABLE_PATH_INFORMATION_PARAMETER = "availablePathInformation";
	public static final String BANDWIDTH_UTILIZATION_PARAMETER = "bandwidthUtilization";
	public static final String DUPLEX_MODE_PARAMETER = "duplexMode";
	public static final String ERROR_PERCENT_PARAMETER = "errorPercent";
	public static final String LINK_SPEED_PARAMETER = "linkSpeed";
	public static final String LINK_STATUS_PARAMETER = "linkStatus";
	public static final String RECEIVED_BYTES_RATE_PARAMETER = "receivedBytesRate";
	public static final String USAGE_REPORT_RECEIVED_BYTES_PARAMETER = "usageReportReceivedBytes";
	public static final String RECEIVED_PACKETS_RATE_PARAMETER = "receivedPacketsRate";
	public static final String USAGE_REPORT_RECEIVED_PACKETS_PARAMETER = "usageReportReceivedPackets";
	public static final String TRANSMITTED_BYTES_RATE_PARAMETER = "transmittedBytesRate";
	public static final String USAGE_REPORT_TRANSMITTED_BYTES_PARAMETER = "usageReportTransmittedBytes";
	public static final String TRANSMITTED_PACKETS_RATE_PARAMETER = "transmittedPacketsRate";
	public static final String USAGE_REPORT_TRANSMITTED_PACKETS_PARAMETER = "usageReportTransmittedPackets";
	public static final String ZERO_BUFFER_CREDIT_PERCENT_PARAMETER = "zeroBufferCreditPercent";
	public static final String USAGE_COUNT_PARAMETER = "usageCount";
	public static final String VALUE_PARAMETER = "value";
	public static final String ENDURANCE_REMAINING_PARAMETER = "enduranceRemaining";
	public static final String USED_CAPACITY_PARAMETER = "usedCapacity";
	public static final String MOVE_COUNT_PARAMETER = "moveCount";
	public static final String MOUNT_COUNT_PARAMETER = "mountCount";
	public static final String UNMOUNT_COUNT_PARAMETER = "unmountCount";
	public static final String NEEDS_CLEANING_PARAMETER = "needsCleaning";
	public static final String USED_TIME_PARAMETER = "usedTime";
	public static final String USED_TIME_PERCENT_PARAMETER = "usedTimePercent";
	public static final String RECEIVED_BYTES_PARAMETER = "receivedBytes";
	public static final String TRANSMITTED_BYTES_PARAMETER = "transmittedBytes";
	public static final String TRANSMITTED_PACKETS_PARAMETER = "transmittedPackets";
	public static final String RECEIVED_PACKETS_PARAMETER = "receivedPackets";
	public static final String ZERO_BUFFER_CREDIT_COUNT_PARAMETER = "zeroBufferCreditCount";
	public static final String TOTAL_PACKETS_PARAMETER = "totalPackets";
	public static final String CONNECTED_PORTS_COUNT_PARAMETER = "connectedPortsCount";
	public static final String TOTAL_BANDWIDTH_PARAMETER = "totalBandwidth";
	public static final String USED_WATTS_PARAMETER = "usedWatts";
	public static final String USED_PERCENT_PARAMETER = "usedPercent";
	public static final String LAST_ERROR_PARAMETER = "lastError";
	public static final String MEMORY_UTILIZATION_PARAMETER = "memoryUtilization";
	public static final String ENCODER_USED_TIME_PARAMETER = "encoderUsedTime";
	public static final String ENCODER_USED_TIME_PERCENT_PARAMETER = "encoderUsedTimePercent";
	public static final String DECODER_USED_TIME_PARAMETER = "decoderUsedTime";
	public static final String DECODER_USED_TIME_PERCENT_PARAMETER = "decoderUsedTimePercent";
	public static final String POWER_SHARE_PARAMETER = "powerShare";
	public static final String POWER_SHARE_PERCENT_PARAMETER = "powerSharePercent";
	public static final String SNMP_UP_PARAMETER = "snmpUp";
	public static final String SSH_UP_PARAMETER = "sshUp";
	public static final String WBEM_UP_PARAMETER = "wbemUp";
	public static final String WMI_UP_PARAMETER = "wmiUp";
	public static final String HTTP_UP_PARAMETER = "httpUp";
	public static final String IPMI_UP_PARAMETER = "ipmiUp";

	///////////////////////////////////////////////////////////////////////////////
	//                                                                           //
	//                              METADATA NAMES                               //
	//                                                                           //
	//                                 .i;;;;i.                                  //
	//                               iYcviii;vXY:                                //
	//                             .YXi       .i1c.                              //
	//                            .YC.     .    in7.                             //
	//                           .vc.   ......   ;1c.                            //
	//                           i7,   ..        .;1.                            //
	//                          i7,   .. ...      .Y1i                           //
	//                         ,7v     .6MMM@;     .YX,                          //
	//                        .7;.   ..IMMMMMM1     :t7.                         //
	//                       .;Y.     ;$MMMMMM9.     :tc.                        //
	//                       vY.   .. .nMMM@MMU.      ;1v.                       //
	//                      i7i   ...  .#MM@M@C. .....:71i                       //
	//                     it:   ....   $MMM@9;.,i;;;i,;tti                      //
	//                    :t7.  .....   0MMMWv.,iii:::,,;St.                     //
	//                   .nC.   .....   IMMMQ..,::::::,.,czX.                    //
	//                  .ct:   ....... .ZMMMI..,:::::::,,:76Y.                   //
	//                  c2:   ......,i..Y$M@t..:::::::,,..inZY                   //
	//                 vov   ......:ii..c$MBc..,,,,,,,,,,..iI9i                  //
	//                i9Y   ......iii:..7@MA,..,,,,,,,,,....;AA:                 //
	//               iIS.  ......:ii::..;@MI....,............;Ez.                //
	//              .I9.  ......:i::::...8M1..................C0z.               //
	//             .z9;  ......:i::::,.. .i:...................zWX.              //
	//             vbv  ......,i::::,,.      ................. :AQY              //
	//            c6Y.  .,...,::::,,..:t0@@QY. ................ :8bi             //
	//           :6S. ..,,...,:::,,,..EMMMMMMI. ............... .;bZ,            //
	//          :6o,  .,,,,..:::,,,..i#MMMMMM#v.................  YW2.           //
	//         .n8i ..,,,,,,,::,,,,.. tMMMMM@C:.................. .1Wn           //
	//         7Uc. .:::,,,,,::,,,,..   i1t;,..................... .UEi          //
	//         7C...::::::::::::,,,,..        ....................  vSi.         //
	//         ;1;...,,::::::,.........       ..................    Yz:          //
	//          v97,.........                                     .voC.          //
	//           izAotX7777777777777777777777777777777777777777Y7n92:            //
	//             .;CoIIIIIUAA666666699999ZZZZZZZZZZZZZZZZZZZZ6ov.              //
	//                                                                           //
	//        PLEASE NOTE: metadata names MUST be written using camelCase        //
	//                                                                           //
	///////////////////////////////////////////////////////////////////////////////

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
	public static final String DESCRIPTION = "description";
	public static final String COMPILED_FILE_NAME = "compiledFileName";
	public static final String DISPLAY_NAME = "displayName";
	public static final String FAN_TYPE = "fanType";
	public static final String VOLTAGE_TYPE = "voltageType";
	public static final String TEMPERATURE_TYPE = "temperatureType";
	public static final String ROBOTIC_TYPE = "roboticType";
	public static final String POWER_SUPPLY_TYPE = "powerSupplyType";
	public static final String POWER_SUPPLY_POWER = "powerSupplyPower";
	public static final String BIOS_VERSION = "biosVersion";
	public static final String DEVICE_TYPE = "deviceType";
	public static final String LOGICAL_ADDRESS = "logicalAddress";
	public static final String PHYSICAL_ADDRESS = "physicalAddress";
	public static final String BANDWIDTH = "bandwidth";
	public static final String REMOTE_PHYSICAL_ADDRESS = "remotePhysicalAddress";
	public static final String WWN = "wwn";
	public static final String ARRAY_NAME = "arrayName";
	public static final String REMOTE_DEVICE_NAME = "remoteDeviceName";
	public static final String SIZE = "size";
	public static final String RAID_LEVEL = "raidLevel";
	public static final String LOCAL_DEVICE_NAME = "localDeviceName";
	public static final String EXPECTED_PATH_COUNT = "expectedPathCount";
	public static final String NAME = "Name";
	public static final String DRIVER_VERSION = "driverVersion";
	public static final String FIRMWARE_VERSION = "firmwareVersion";
	public static final String MAXIMUM_SPEED = "maximumSpeed";
	public static final String BLADE_NAME = "bladeName";
	public static final String ADDITIONAL_INFORMATION3 = "additionalInformation3";
	public static final String ADDITIONAL_INFORMATION2 = "additionalInformation2";
	public static final String ADDITIONAL_INFORMATION1 = "additionalInformation1";
	public static final String CHEMISTRY = "chemistry";
	public static final String POWER = "power";
	public static final String CORRECTED_ERROR_WARNING_THRESHOLD = "correctedErrorWarningThreshold";
	public static final String CORRECTED_ERROR_ALARM_THRESHOLD = "correctedErrorAlarmThreshold";
	public static final String WARNING_THRESHOLD = "warningThreshold";
	public static final String ALARM_THRESHOLD = "alarmThreshold";
	public static final String PERCENT_WARNING_THRESHOLD = "percentWarningThreshold";
	public static final String PERCENT_ALARM_THRESHOLD = "percentAlarmThreshold";
	public static final String ERROR_COUNT_WARNING_THRESHOLD =  "errorCountWarningThreshold";
	public static final String ERROR_COUNT_ALARM_THRESHOLD =  "errorCountAlarmThreshold";
	public static final String AVAILABLE_PATH_WARNING = "availablePathWarning";
	public static final String ERROR_PERCENT_WARNING_THRESHOLD = "errorPercentWarningThreshold";
	public static final String ERROR_PERCENT_ALARM_THRESHOLD ="errorPercentAlarmThreshold";
	public static final String VALUE_WARNING_THRESHOLD = "valueWarningThreshold";
	public static final String VALUE_ALARM_THRESHOLD = "valueAlarmThreshold";
	public static final String USAGE_COUNT_WARNING_THRESHOLD = "usageCountWarningThreshold";
	public static final String USAGE_COUNT_ALARM_THRESHOLD = "usageCountAlarmThreshold";
	public static final String UPPER_THRESHOLD = "upperThreshold";
	public static final String LOWER_THRESHOLD= "lowerThreshold";
	public static final String DISK_CONTROLLER_NUMBER = "controllerNumber";
	public static final String COLOR = "color";
	public static final String ADDITIONAL_LABEL = "additionalLabel";
	public static final String IS_CPU_SENSOR = "isCpuSensor";
	public static final String AVERAGE_CPU_TEMPERATURE_WARNING = "averageCpuTemperatureWarning";
	public static final String WARNING_ON_COLOR = "warningOnColor";
	public static final String ALARM_ON_COLOR = "alarmOnColor";
	public static final String ON_STATUS = "onStatus";
	public static final String OFF_STATUS = "offStatus";
	public static final String BLINKING_STATUS = "blinkingStatus";
	public static final String POWER_CONSUMPTION = "powerConsumption";
	public static final String IDENTIFYING_INFORMATION = "identifyingInformation";
	public static final String POWER_SOURCE_ID = "powerSourceId";
	public static final String DOMAIN = "domain";
	public static final String HOSTNAME = "hostname";
	public static final String POWER_METER = "powerMeter";
	public static final String APPLIES_TO_OS = "appliesToOS";
	public static final String IP_ADDRESS = "ipAddress";

	/////////////////////
	//                 //
	// PARAMETER UNITS //
	//                 //
	/////////////////////

	public static final String STATUS_PARAMETER_UNIT = "{ 0 = OK ; 1 = Degraded ; 2 = Failed }";
	public static final String LED_INDICATOR_PARAMETER_UNIT = "{ 0 = Off ; 1 = Blinking ; 2 = On }";
	public static final String INTRUSION_STATUS_PARAMETER_UNIT = "{ 0 = Closed ; 1 = Open }";
	public static final String ENERGY_USAGE_PARAMETER_UNIT = "Joules";
	public static final String ENERGY_PARAMETER_UNIT = "Joules";
	public static final String POWER_CONSUMPTION_PARAMETER_UNIT = "Watts";
	public static final String POWER_STATE_PARAMETER_UNIT = "{ 0 = Off ; 1 = Suspended ; 2 = On }";
	public static final String LINK_STATUS_PARAMETER_UNIT = "{ 0 = Plugged ; 1 = Unplugged }";
	public static final String SPEED_PARAMETER_UNIT = "RPM";
	public static final String CURRENT_SPEED_PARAMETER_UNIT = "MHz";
	public static final String SPEED_PERCENT_PARAMETER_UNIT = "% of maximum speed";
	public static final String PERCENT_PARAMETER_UNIT = "%";
	public static final String VOLTAGE_PARAMETER_UNIT = "mV";
	public static final String TEMPERATURE_PARAMETER_UNIT = "degrees Celsius";
	public static final String HEATING_MARGIN_PARAMETER_UNIT = "degrees Celsius";
	public static final String ERROR_COUNT_PARAMETER_UNIT = "errors";
	public static final String ERROR_STATUS_PARAMETER_UNIT = "{ 0 = No Errors ; 1 = Detected Errors ; 2 = Too Many Errors }";
	public static final String PREDICTED_FAILURE_PARAMETER_UNIT  = "{ 0 = OK ; 1 = Failure Predicted }";
	public static final String PRESENT_PARAMETER_UNIT  = "{ 0 = Missing ; 1 = Present }";
	public static final String SPACE_GB_PARAMETER_UNIT = "GB";
	public static final String SPEED_MBITS_PARAMETER_UNIT = "Mbits/s";
	public static final String BYTES_RATE_PARAMETER_UNIT = "MB/s";
	public static final String PATHS_PARAMETER_UNIT = "paths";
	public static final String USAGE_COUNT_PARAMETER_UNIT = "times";
	public static final String MOVE_COUNT_PARAMETER_UNIT = "moves";
	public static final String MOUNT_COUNT_PARAMETER_UNIT = "mounts";
	public static final String UNMOUNT_COUNT_PARAMETER_UNIT = "unmounts";
	public static final String PACKETS_RATE_PARAMETER_UNIT = "packets/s";
	public static final String DUPLEX_MODE_PARAMETER_UNIT = "{ 0 =  Half-duplex ; 1 = Full-duplex }";
	public static final String NEEDS_CLEANING_PARAMETER_UNIT = "{ 0 =  OK ; 1 = Cleaning Needed ; 2 = Cleaning Needed Immediately }";
	public static final String TIME_PARAMETER_UNIT = "seconds";
	public static final String BYTES_PARAMETER_UNIT = "Bytes";
	public static final String PACKETS_PARAMETER_UNIT = "Packets";
	public static final String ZERO_BUFFER_CREDIT_COUNT_PARAMETER_UNIT = "buffer credits";
	public static final String CONNECTED_PORTS_PARAMETER_UNIT = "ports";
	public static final String COLOR_PARAMETER_UNIT = "{ 0 = OK ; 1 = Degraded ; 2 = Failed }";
	public static final String UP_PARAMETER_UNIT  = "{ 0 = Down ; 1 = Up }";
	public static final String DEFAULT = "default";

	public static final String USERNAME_MACRO = "%{USERNAME}";
	public static final String AUTHENTICATION_TOKEN_MACRO = "%{AUTHENTICATIONTOKEN}";
	public static final String PASSWORD_MACRO = "%{PASSWORD}";
	public static final String PASSWORD_BASE64_MACRO = "%{PASSWORD_BASE64}";
	public static final String BASIC_AUTH_BASE64_MACRO = "%{BASIC_AUTH_BASE64}";
	public static final String HOSTNAME_MACRO = "%{HOSTNAME}";

	public static final String AUTOMATIC_NAMESPACE = "automatic";

	public static final String FQDN = "fqdn";
	public static final String TARGET_FQDN = "targetFqdn";

	public static final int ID_MAXLENGTH = 10;

	public static final String ECHO_SSH_UP_TEST = "echo SSH_UP_TEST";
	public static final String SSH_UP_TEST_RESPONSE = "SSH_UP_TEST";

	/**
	 * Pattern to remove MS_HW_ from the compiled file name
	 */
	public static final Pattern REMOVE_MS_HW_PATTERN = Pattern.compile("^(MS_HW_)(.*)$", Pattern.CASE_INSENSITIVE);

}
