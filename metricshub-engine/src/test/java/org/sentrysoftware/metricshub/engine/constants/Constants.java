package org.sentrysoftware.metricshub.engine.constants;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import org.sentrysoftware.metricshub.engine.common.helpers.StringHelper;

public class Constants {

	// Global constants

	public static final String USERNAME = "testUser";
	public static final String PASSWORD = "testPassword";
	public static final String WINRM_NAMESPACE = "testWinRmNamespace";
	public static final Long SSH_CONFIGURATION_TIMEOUT = 50L;
	public static final Long STRATEGY_TIMEOUT = 100L;
	public static final Long STRATEGY_TIME = 100L;

	public static final Long RETRY_DELAY = 30L;
	public static final String SSH_SUDO_COMMAND = "sudo pwd";
	public static final String DETECTION_FOLDER = "src/test/resources/test-files/connector/detection";
	public static final String IPMI_SUCCESS_MESSAGE = "System description;";
	public static final String IPMI_FAILURE_MESSAGE = "No result";
	public static final String MY_CONNECTOR_1_NAME = "myConnector1";
	public static final String NO_TEST_WILL_BE_PERFORMED_MESSAGE = "Process presence check: No test will be performed.";
	public static final String NO_TEST_WILL_BE_PERFORMED_UNKNOWN_OS_MESSAGE =
		"Process presence check: OS unknown, no test will be performed.";
	public static final String NO_TEST_WILL_BE_PERFORMED_AIX_MESSAGE =
		"Process presence check: No tests will be performed for OS: aix.";
	public static final String NO_TEST_WILL_BE_PERFORMED_REMOTELY_MESSAGE =
		"Process presence check: No test will be performed remotely.";
	public static final String PROCESS_CRITERION_COMMAND_LINE = "MBM[5-9]\\.exe";
	public static final String WMI_QUERY_EMPTY_VALUE_MESSAGE =
		"WMI query \"SELECT ProcessId,Name,ParentProcessId,CommandLine FROM Win32_Process\" " + "returned empty value.";
	public static final String WBEM_QUERY = "SELECT ProcessId,Name,ParentProcessId,CommandLine FROM Win32_Process";
	public static final String WEBM_CRITERION_SUCCESS_EXPECTED_RESULT = "^Some Res[aeiouy]lt";
	public static final String WEBM_CRITERION_FAILURE_EXPECTED_RESULT = "^S Res[aeiouy]lt";
	public static final String WMI_NAMESPACE = "root\\cimv2";
	public static final String RUNNING_PROCESS_MATCH_REGEX_MESSAGE =
		"One or more currently running processes match the following regular expression:\n- " +
		"Regexp (should match with the command-line): MBM[5-9]\\.exe";
	public static final String NO_RUNNING_PROCESS_MATCH_REGEX_MESSAGE =
		"No currently running processes match the following regular expression:\n" +
		"- Regexp (should match with the command-line): MBM[5-9]\\.exe\n" +
		"- Currently running process list:\n" +
		"1;ps;root;0;ps -A -o pid,comm,ruser,ppid,args\n" +
		"10564;eclipse.exe;user;11068;\"C:\\Users\\huan\\eclipse\\eclipse.exe\"";
	public static final String WMI_CRITERION_TEST_SUCCEED_MESSAGE =
		"""
		WmiCriterion test succeeded:
		- WQL Query: SELECT ProcessId,Name,ParentProcessId,CommandLine FROM Win32_Process
		- Namespace: root\\cimv2
		- Expected Result: MBM[5-9]\\.exe

		Result: MBM6.exe""";
	public static final List<List<String>> EXECUTE_WMI_RESULT = List.of(
		List.of("0", "System Idle Process", "0", ""),
		List.of("2", "MBM6.exe", "0", "MBM6.exe arg1 arg2"),
		List.of("10564", "eclipse.exe", "11068", "\"C:\\Users\\huan\\eclipse\\eclipse.exe\"")
	);
	public static final List<List<String>> EXCUTE_WBEM_RESULT = List.of(List.of("some result"));
	public static final String WBEM_CRITERION_UNEXPECTED_RESULT_MESSAGE = "WbemCriterion test ran but failed";
	public static final String WBEM_MALFORMED_CRITERION_MESSAGE = "Malformed criterion. Cannot perform detection";
	public static final String WBEM_CRITERION_NO_RESULT_MESSAGE = "No result.";
	public static final String WBEM_CREDENTIALS_NOT_CONFIGURED = "The WBEM credentials are not configured for this host.";
	public static final List<List<String>> LIST_ALL_LINUX_PROCESSES_RESULT = List.of(
		List.of("1", "ps", "root", "0", "ps -A -o pid,comm,ruser,ppid,args"),
		List.of("10564", "eclipse.exe", "user", "11068", "\"C:\\Users\\huan\\eclipse\\eclipse.exe\"")
	);

	public static final String EMPTY = "";
	public static final String SINGLE_SPACE = " ";
	public static final String ROOT = "root";
	public static final String AUTOMATIC = "Automatic";
	public static final String HOSTNAME_MACRO = "%{HOSTNAME}";
	public static final String CMD = "cmd";
	public static final String ID = "id";
	public static final String BAT = "bat";
	public static final String ECHO_OS = "ECHO %OS%";
	public static final String ARCCONF_PATH = "/[opt|usr]/StorMan/arcconf";
	public static final String PWD_COMMAND = "pwd";
	public static final String AGENT_REV_RESULT = "Agent Rev:";
	public static final String NAVISECCLI_COMMAND =
		"%{SUDO:naviseccli} naviseccli -User %{USERNAME} -Password %{PASSWORD} -Address %{HOSTNAME} -Scope 1 getagent";
	public static final String ECHO_HELLO_WORLD = "echo Hello World";
	public static final String WINDOWS_NT_HELLO_WORLD = "Windows_NT\nHello World";
	public static final String END_OF_LINE = "\n";
	public static final String END_OF_LINE_IN_BRACKETS = "[\r\n]";
	public static final String TEXT = "text";
	public static final String KEY = "key";
	public static final String SPACE_KEY = SINGLE_SPACE + KEY;
	public static final String SUDO_KEY = "%{SUDO:key} key";
	public static final String SUDO_KEY_RESULT = "sudo key\nsudo key";
	public static final String PAUSE = "PAUSE";
	public static final String NAVISECCLI_CAMEL_CASE = "NaviSecCli";
	public static final String SLEEP_5 = "sleep 5";
	public static final String ECHO_TEST_UPPER_CASE = "ECHO Test";
	public static final String ECHO_TEST_LOWER_CASE = "echo Test";
	public static final String TEST_RESULT = "Test";
	public static final String RAIDCTL_PATH = "/usr/sbin/raidctl";
	public static final String Q_HOST = "(?i)\\QHost\\E";
	public static final String Q_USERNAME = "(?i)\\Q%{UserName}\\E";
	public static final String Q_HOSTNAME = "(?i)\\Q%{HOSTNAME}\\E";
	public static final String PERCENT_USERNAME = "%{UserName}";
	public static final String HARD_DRIVE = "Hard drive";
	public static final String ECS1_01 = "ecs1-01";
	public static final String VALUE_VAL1 = "val1";
	public static final String VALUE_VAL2 = "val2";
	public static final String VALUE_VAL3 = "val3";
	public static final String VALUE_A1 = "a1";
	public static final String VALUE_B1 = "b1";
	public static final String VALUE_C1 = "c1";
	public static final String DOLLAR_3 = "$3";
	public static final String DOLLAR_4 = "$4";
	public static final String URL = "my/url";
	public static final String ENCLOSURE_COLLECT_SOURCE_1 = "${source::monitors.enclosure.collect.sources.source1}";
	public static final String EXPECTED_VAL_1 = "expectedVal1";
	public static final String EXPECTED_VAL_2 = "expectedVal2";
	public static final String EXPECTED_VAL_1_AND_2 = "expectedVal1\nexpectedVal2";
	public static final String EXPECTED_VAL_1_AND_2_ARRAY = "[expectedVal1,\nexpectedVal2]";
	public static final String EXPECTED_RESULT =
		"[{\n" +
		"\"Entry\":{\n" +
		"\"Full\":\"val1,val2,val3\",\n" +
		"\"Column(1)\":\"val1\",\n" +
		"\"Column(2)\":\"val2\",\n" +
		"\"Column(3)\":\"val3\",\n" +
		"\"Value\":expectedVal1\n" +
		"}\n" +
		"},\n" +
		"{\n" +
		"\"Entry\":{\n" +
		"\"Full\":\"a1,b1,c1\",\n" +
		"\"Column(1)\":\"a1\",\n" +
		"\"Column(2)\":\"b1\",\n" +
		"\"Column(3)\":\"c1\",\n" +
		"\"Value\":expectedVal2\n" +
		"}\n" +
		"}]";
	public static final String OID = "1.3.6.1.4.1.674.10893.1.20";
	public static final List<List<String>> EXPECTED_SNMP_TABLE_DATA = Arrays.asList(
		Arrays.asList("1", "PowerEdge R630", "FSJR3N2", "34377965102")
	);
	public static final String SNMP_SELECTED_COLUMNS = "ID, 9, 11, 49";
	public static final List<String> SNMP_SELECTED_COLUMNS_LIST = Arrays.asList("ID", "9", "11", "49");
	public static final String TAB1_REF = "${source::monitors.cpu.discovery.sources.tab1}";
	public static final String TABLE_SEP = ";";

	// Embedded files
	public static final String TEMP_EMBEDDED_1 = "/tmp/SEN_Embedded_1.bat";
	public static final String TEMP_EMBEDDED_2 = "/tmp/SEN_Embedded_2";
	public static final String AWK_EMBEDDED_CONTENT_PERCENT_SUDO =
		"# Awk (or nawk)\n" +
		"if [ -f /usr/xpg4/bin/awk ]; then\n" +
		"	AWK=\"/usr/xpg4/bin/awk\";\n" +
		"elif [ -f /usr/bin/nawk ]; then\n" +
		"	AWK=\"/usr/bin/nawk\";\n" +
		"else\n" +
		"	AWK=\"awk\";\n" +
		"fi\n" +
		"if [ -f /opt/StorMan/arcconf ]; then\n" +
		"       STORMAN=\"/opt/StorMan\";\n" +
		"elif [ -f /usr/StorMan/arcconf ]; then\n" +
		"       STORMAN=\"/usr/StorMan\";\n" +
		"else\n" +
		"	echo No Storman Installed; exit;\n" +
		"fi\n" +
		"DEVICES=`%{SUDO:/[opt|usr]/StorMan/arcconf} $STORMAN/arcconf getversion | $AWK '($1 ~ /Controller/ && $2 ~ /#[0-9]/) {controller=$2;gsub(/#/,\"\",controller);print(controller)}'`\n" +
		"for CTRL in $DEVICES\n" +
		"                do\n" +
		"                echo MSHWController $CTRL\n" +
		"                %{SUDO:/[opt|usr]/StorMan/arcconf} $STORMAN/arcconf getconfig $CTRL PD\n" +
		"                done";
	public static final String AWK_EMBEDDED_CONTENT_SUDO =
		"# Awk (or nawk)\n" +
		"if [ -f /usr/xpg4/bin/awk ]; then\n" +
		"	AWK=\"/usr/xpg4/bin/awk\";\n" +
		"elif [ -f /usr/bin/nawk ]; then\n" +
		"	AWK=\"/usr/bin/nawk\";\n" +
		"else\n" +
		"	AWK=\"awk\";\n" +
		"fi\n" +
		"if [ -f /opt/StorMan/arcconf ]; then\n" +
		"       STORMAN=\"/opt/StorMan\";\n" +
		"elif [ -f /usr/StorMan/arcconf ]; then\n" +
		"       STORMAN=\"/usr/StorMan\";\n" +
		"else\n" +
		"	echo No Storman Installed; exit;\n" +
		"fi\n" +
		"DEVICES=`sudo $STORMAN/arcconf getversion | $AWK '($1 ~ /Controller/ && $2 ~ /#[0-9]/) {controller=$2;gsub(/#/,\"\",controller);print(controller)}'`\n" +
		"for CTRL in $DEVICES\n" +
		"                do\n" +
		"                echo MSHWController $CTRL\n" +
		"                sudo $STORMAN/arcconf getconfig $CTRL PD\n" +
		"                done";
	public static final String SH_EMBEDDED_FILE_1 = "/bin/sh ${file::EmbeddedFile(1)}";
	public static final String EMBEDDED_FILE_1_REF = "${file::EmbeddedFile(1)}";
	public static final String EMBEDDED_FILE_2_REF = "${file::EmbeddedFile(2)}";
	public static final String EMBEDDED_FILE_1_COPY_COMMAND_LINE =
		"copy ${file::EmbeddedFile(1)} ${file::EmbeddedFile(1)}.bat > NUL & ${file::EmbeddedFile(1)}.bat %{USERNAME} %{PASSWORD} %{HOSTNAME} & del /F /Q ${file::EmbeddedFile(1)}.bat & del /F /Q ${file::EmbeddedFile(2)}.bat ";
	public static final String CMD_COMMAND = "CMD.EXE /C cmd";
	public static final String NO_PASSWORD_COMMAND =
		" naviseccli -User testUser -Password ******** -Address host -Scope 1 getagent";
	public static final String CLEAR_PASSWORD_COMMAND =
		" naviseccli -User testUser -Password pwd -Address host -Scope 1 getagent";
	public static final String COMMAND_TO_UPDATE =
		"copy ${file::EmbeddedFile(2)} ${file::EmbeddedFile(2)}.bat > NUL" +
		" & ${file::EmbeddedFile(1)}" +
		" & ${file::EmbeddedFile(2)}.bat" +
		" & del /F /Q ${file::EmbeddedFile(1)}" +
		" & del /F /Q ${file::EmbeddedFile(2)}.bat";
	public static final String UPDATED_COMMAND =
		"copy /tmp/SEN_Embedded_2 /tmp/SEN_Embedded_2.bat > NUL" +
		" & /tmp/SEN_Embedded_1.bat" +
		" & /tmp/SEN_Embedded_2.bat" +
		" & del /F /Q /tmp/SEN_Embedded_1.bat" +
		" & del /F /Q /tmp/SEN_Embedded_2.bat";
	public static final String RAIDCTL_COMMAND = "/usr/sbin/raidctl -S";
	public static final String SUDO_RAIDCTL_COMMAND = "%{SUDO:/usr/sbin/raidctl} /usr/sbin/raidctl -S";
	public static final String SUDO_NAVISECCLI_COMMAND =
		"%{Sudo:NaviSecCli} NaviSecCli -User %{USERNAME} -Password %{PASSWORD} -Address host -Scope 1 getagent";
	public static final String SEN_EMBEDDED_0001_PATH = "/tmp/SEN_Embedded_0001";
	public static final String SH_SEN_EMBEDDED_0001_PATH = "/bin/sh /tmp/SEN_Embedded_0001";

	public static final String MONITOR_ID_ATTRIBUTE_VALUE = "anyMonitorId";

	public static final String UNKNOWN = "unknown";
	public static final String AGENT_HOSTNAME_VALUE = StringHelper.getValue(
		() -> InetAddress.getLocalHost().getCanonicalHostName(),
		UNKNOWN
	);
	public static final String LOCATION = "location";
	public static final String COMPUTE = "compute";
	public static final String HOST_ID_ATTRIBUTE = "host.id";
	public static final String HOST_TYPE = "host.type";
	public static final String OS_TYPE = "os.type";
	public static final String HOST_NAME = "host.name";
	public static final String[] STATE_SET = { "ok", "degraded", "failed" };
	public static final String HARDCODED_SOURCE = "Hardcoded Source";
	public static final String DISK_CONTROLLER = "disk_controller";
	public static final String PHYSICAL_DISK = "physical_disk";
	public static final String CONNECTOR = "connector";
	public static final String ENCLOSURE = "enclosure";
	public static final String DISCOVERY_MAPPING_NAME = "name";
	public static final String DISCOVERY_MAPPING_VENDOR = "vendor";
	public static final String DISCOVERY_MAPPING_MODEL = "model";

	// Yaml test files
	public static final String AAC_CONNECTOR_ID = "AAC";
	public static final String TEST_CONNECTOR_ID = "TestConnector";
	public static final String COMMAND_FILE_ABSOLUTE_PATH =
		"${file::src\\test\\resources\\test-files\\embedded\\connector2\\command.txt}";
	public static final String EMBEDDED_TEMP_FILE_PREFIX = "SEN_Embedded_";
	public static final String BAT_FILE_EXTENSION = "\\w+\\.bat";

	// Host information
	public static final String LOCALHOST = "localhost";
	public static final String HOST_ID = "PC-120";
	public static final String MANAGEMENT_CARD_HOST = "management-card-host";
	public static final String HOST = "host";
	public static final String HOSTNAME = "hostname";
	public static final String HOST_CAMEL_CASE = "Host";

	// Configuration toString output
	public static final String WINRM_CONFIGURATION_TO_STRING = "WinRM as testUser";
	public static final String OS_COMMAND_CONFIGURATION_TO_STRING = "Local Commands";
	public static final String WMI_CONFIGURATION_TO_STRING = "WMI as testUser";
	public static final String SSH_CONFIGURATION_TO_STRING = "SSH as testUser";
	public static final String HOST_CONFIGURATION_TO_STRING =
		"HostConfiguration(hostname=localhost, hostId=localhost," +
		" hostType=LINUX, strategyTimeout=100, connectors=null," +
		" sequential=false, alertTrigger=null," +
		" retryDelay=30, connectorVariables=null, configurations={}, configuredConnectorId=null)";
	public static final String IPMI_RESULT_EXAMPLE =
		"Device ID                 : 3\r\n" +
		"Device Revision           : 3\r\n" +
		"Firmware Revision         : 4.10\r\n" +
		"IPMI Version              : 2.0\r\n" +
		"Manufacturer ID           : 10368\r\n" +
		"Manufacturer Name         : Fujitsu Siemens\r\n" +
		"Product ID                : 790 (0x0316)\r\n" +
		"Product Name              : Unknown (0x316)";
	public static final String SUDO_KEYWORD = "sudo";
	public static final String INVALID_SSH_RESPONSE = "Wrong result";
	public static final String SYSTEM_POWER_UP_MESSAGE = "System power state is up";
	public static final String IPMI_CONNECTION_SUCCESS_WITH_IMPI_OVER_LAN_MESSAGE =
		"Successfully connected to the IPMI BMC chip with the IPMI-over-LAN " + "interface.";
	public static final String IPMI_CONNECTION_SUCCESS_WITH_IN_BAND_DRIVER_MESSAGE =
		"Successfully connected to the IPMI BMC chip with the in-band driver" + " interface.";
	public static final String PATH = "PATH";
	public static final String NO_OS_CONFIGURATION_MESSAGE =
		" - No OS command configuration for this host. Returning an empty result";
	public static final String IPMI_TOOL_COMMAND = "ipmitoolCommand ";
	public static final String BMC = "bmc";
	public static final String LIPMI = "lipmi";
	public static final String LINUX_BUILD_IPMI_COMMAND =
		"PATH=$PATH:/usr/local/bin:/usr/sfw/bin;export PATH;ipmitool -I open bmc info";
	public static final String TWGIPC = "TWGIPC";
	public static final String SERVICE_NAME_NOT_SPECIFIED_MESSAGE = "Service name is not specified";

	// Exception messages

	public static final String INVALID_PROTOCOL_EXCEPTION_MESSAGE = "Invalid protocol value: ";
	public static final String ERROR_IN_FILE1 = "error in file1";

	// Protocols
	public static final String INVALID_PROTOCOL = "SFTPST";
	public static final String HTTP = "HTTP";
	public static final String HTTPS = "HTTPS";
	public static final String SSH = "SSH";
	public static final String WMI = "WMI";
	public static final String WINRM = "WinRM";
	public static final String CRITERION_WMI_NAMESPACE = "root\\cimv2";
	public static final String WMI_EXCEPTION_OTHER_MESSAGE = "other";
	public static final String SNMP_TABLE = "snmpTable";

	public static final String WMI_EXCEPTION_WBEM_E_NOT_FOUND_MESSAGE = "WBEM_E_NOT_FOUND";
	public static final String WMI_EXCEPTION_WBEM_E_INVALID_NAMESPACE_MESSAGE = "WBEM_E_INVALID_NAMESPACE";
	public static final String WMI_EXCEPTION_WBEM_E_INVALID_CLASS_MESSAGE = "WBEM_E_INVALID_CLASS";
	public static final String EXCUTE_WBEM_RESULT_ELEMENT = "some result";
	public static final String RESULT_MESSAGE_SHOULD_CONTAIN_RESULT = "Result message must contain the query result";
	public static final String WMI_CRITERION_UNEXPECTED_RESULT_MESSAGE = "WmiCriterion test ran but failed";
	public static final String TIMEOUT_EXCEPTION = "TimeoutException";
	public static final String WMI_COM_EXCEPTION_MESSAGE = "WBEM_E_INVALID_NAMESPACE";
	public static final String WEBM_CRITERION_NOT_MATCHING_EXPECTED_RESULT = "^Some Res[^aeiouy]lt";
	public static final String WMI_CREDENTIALS_NOT_CONFIGURED =
		"Neither WMI nor WinRM credentials are configured for this host.";
	public static final String FORCED_NAMESPACE = "forced";
	public static final String WBEM_NAMESPACE_TIMEOUT_MESSAGE = "very long";
	public static final String WBEM_NAMESPACE_TIMEOUT_ERROR_MESSAGE =
		"Error message must contain the cause of the problem";
	public static final String MATSYA_NO_RESPONSE_EXCEPTION_MESSAGE = "no response";
	public static final String FIRST_NAMESPACE = "namespace1";
	public static final String SECOND_NAMESPACE = "namespace2";
	public static final String INTEROP_NAMESPACE = "interop";
	public static final String DUPLICATE_COLUMN = "duplicateColumn";
	public static final String TRANSLATE = "translate";
	public static final String SNMP_CRITERION_TYPE = "snmpGetNext";
	public static final String HW_PARENT_TYPE = "hw.parent.type";
	public static final String DISK_CONTROLLER_AWK_COMMAND = "${awk::sprintf(\"Disk Controller: %s (%s)\", $2, $3)}";
	public static final String PHYSICAL_DISK_AWK_COMMAND =
		"${awk::sprintf(\"%s (%s - %s)\", $1, $4, bytes2HumanFormatBase10($6))}";
	public static final String SOURCE = "source(1)";

	// OS
	public static final String LINUX = "LINUX";
	public static final String WINDOWS = "WINDOWS";
	public static final String HOST_WIN = "host-win";
	public static final String HOST_LINUX = "host-linux";
	public static final String VALID_SOLARIS_VERSION_TEN = "5.10";
	public static final String VALID_SOLARIS_VERSION_NINE = "5.9";
	public static final String OLD_SOLARIS_VERSION = "4.1.1B";
	public static final String INVALID_SOLARIS_VERSION = "invalid";
	public static final String OLD_SOLARIS_VERSION_MESSAGE = "Solaris version (4.1.1B) is too old";
	public static final String UNKNOWN_SOLARIS_VERSION = "Unknown Solaris version";
	public static final String SOLARIS_VERSION_NOT_IDENTIFIED_MESSAGE_TOKEN = "Could not";
	public static final String RESULT = "result";
	public static final String TEST = "test";
	public static final String TEST_BODY = "test_body";
	public static final String ERROR = "error";
	public static final String SUCCESSFUL_OS_DETECTION = "Successful OS detection operation";
	public static final String FAILED_OS_DETECTION = "Failed OS detection operation";
	public static final String CONFIGURED_OS_NT_MESSAGE = "Configured OS type : NETWORK";
	public static final String CONFIGURED_OS_SOLARIS_MESSAGE = "Configured OS type : SOLARIS";

	// Version Number
	public static final String LOW_VERSION_NUMBER = "0.0.1";
	public static final String HIGH_VERSION_NUMBER = "1000.0.1";
	public static final String NEITHER_WMI_NOR_WINRM_ERROR =
		"Neither WMI nor WinRM credentials are configured for this host.";
	public static final String HOST_OS_IS_NOT_WINDOWS_MESSAGE = "Host OS is not Windows";

	public static final String HEALTHY = "healthy";
	public static final String STATUS_INFORMATION = "StatusInformation";
}
