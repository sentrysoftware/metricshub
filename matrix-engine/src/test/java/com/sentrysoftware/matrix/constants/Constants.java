package com.sentrysoftware.matrix.constants;

import java.util.List;

public class Constants {

	// Global constants

	public static final String USERNAME = "testUser";
	public static final String PASSWORD = "testPassword";
	public static final String WBEM_NAMESPACE = "testWbemNamespace";
	public static final String WINRM_NAMESPACE = "testWinRmNamespace";
	public static final String WBEM_VCENTER = "testWbemVCenter";
	public static final byte[] BMC_KEY = new byte[]{0x06, 0x66};
	public static final Long SSH_CONFIGURATION_TIMEOUT = 50L;
	public static final Long STRATEGY_TIMEOUT = 100L;
	public static final Long RETRY_DELAY = 30L;
	public static final String SSH_SUDO_COMMAND = "sudo pwd";
	public final static String DETECTION_FOLDER = "src/test/resources/test-files/connector/detection";
	public final static String CONNECTOR_YAML = "connector.yaml";
	public final static String IPMI_SUCCESS_MESSAGE = "System description;";
	public final static String IPMI_FAILURE_MESSAGE = "No result";
	public static final String HTTP_GET = "GET";
	public static final String MY_CONNECTOR_1_NAME = "myConnector1";
	public final static String NO_TEST_WILL_BE_PERFORMED_MESSAGE = "Process presence check: No test will be performed.";
	public final static String NO_TEST_WILL_BE_PERFORMED_UNKNOWN_OS_MESSAGE = "Process presence check: OS unknown, no test will be performed.";
	public final static String NO_TEST_WILL_BE_PERFORMED_AIX_MESSAGE = "Process presence check: No tests will be performed for OS: aix.";
	public final static String NO_TEST_WILL_BE_PERFORMED_REMOTELY_MESSAGE = "Process presence check: No test will be performed remotely.";
	public final static String PROCESS_CRITERION_COMMAND_LINE = "MBM[5-9]\\.exe";
	public final static String WMI_QUERY_EMPTY_VALUE_MESSAGE = "WMI query \"SELECT ProcessId,Name,ParentProcessId,CommandLine FROM Win32_Process\" " +
			"returned empty value.";
	public final static String WBEM_QUERY = "SELECT ProcessId,Name,ParentProcessId,CommandLine FROM Win32_Process";
	public final static String WMI_NAMESPACE = "root\\cimv2";
	public final static String RUNNING_PROCESS_MATCH_REGEX_MESSAGE = "One or more currently running processes match the following regular expression:\n- " +
			"Regexp (should match with the command-line): MBM[5-9]\\.exe";
	public final static String NO_RUNNING_PROCESS_MATCH_REGEX_MESSAGE = "No currently running processes match the following regular expression:\n" +
			"- Regexp (should match with the command-line): MBM[5-9]\\.exe\n" +
			"- Currently running process list:\n" +
			"1;ps;root;0;ps -A -o pid,comm,ruser,ppid,args\n" +
			"10564;eclipse.exe;user;11068;\"C:\\Users\\huan\\eclipse\\eclipse.exe\"";
	public final static List<List<String>> EXCUTE_WMI_RESULT = List.of(
			List.of("0", "System Idle Process", "0", ""),
			List.of("2", "MBM6.exe", "0", "MBM6.exe arg1 arg2"),
			List.of("10564", "eclipse.exe", "11068", "\"C:\\Users\\huan\\eclipse\\eclipse.exe\""));
	public final static List<List<String>> LIST_ALL_LINUX_PROCESSES_RESULT = List.of(
			List.of("1", "ps", "root", "0", "ps -A -o pid,comm,ruser,ppid,args"),
			List.of("10564", "eclipse.exe", "user", "11068", "\"C:\\Users\\huan\\eclipse\\eclipse.exe\""));
	public final static String OID = "1.3.6.1.4.1.674.10893.1.20";
	public static final String EMPTY = "";

	// Yaml test file name
	public final static String YAML_TEST_FILE_NAME = "AAC";

	// Host information
	public static final String LOCALHOST = "localhost";
	public final static String HOST_ID = "PC-120";
	public static final String MANAGEMENT_CARD_HOST = "management-card-host";

	// Configuration toString output
	public static final String HTTP_CONFIGURATION_TO_STRING = "HTTPS/443 as testUser";
	public static final String IMPI_CONFIGURATION_TO_STRING = "IPMI as testUser";

	public static final String WBEM_CONFIGURATION_TO_STRING = "https/5989 as testUser";
	public static final String WINRM_CONFIGURATION_TO_STRING = "WinRM as testUser";
	public static final String OS_COMMAND_CONFIGURATION_TO_STRING = "Local Commands";
	public static final String WMI_CONFIGURATION_TO_STRING = "WMI as testUser";
	public static final String SSH_CONFIGURATION_TO_STRING = "SSH as testUser";
	public static final String SNMP_CONFIGURATION_V1_TO_STRING = "SNMP v1 (public)";
	public static final String SNMP_CONFIGURATION_V2C_TO_STRING = "SNMP v2c (public)";
	public static final String SNMP_CONFIGURATION_ENCRYPTED_TO_STRING = "SNMP v3 with MD5 auth (AES-encrypted)";
	public static final String SNMP_CONFIGURATION_NO_PRIVACY_TO_STRING = "SNMP v3 with MD5 auth";
	public static final String SNMP_CONFIGURATION_NO_PRIVACY_WITH_USERNAME_TO_STRING = "SNMP v3 with MD5 auth as testUser";
	public static final String HOST_CONFIGURATION_TO_STRING = "HostConfiguration(hostname=localhost," +
			" hostId=localhost, hostType=LINUX, strategyTimeout=100, selectedConnectors=null, excludedConnectors=null, sequential=false," +
			" alertTrigger=null, retryDelay=30, connectorVariables=null, configurations=null)";
	public static final String IPMI_RESULT_EXAMPLE = "Device ID                 : 3\r\n" + "Device Revision           : 3\r\n"
			+ "Firmware Revision         : 4.10\r\n" + "IPMI Version              : 2.0\r\n"
			+ "Manufacturer ID           : 10368\r\n" + "Manufacturer Name         : Fujitsu Siemens\r\n"
			+ "Product ID                : 790 (0x0316)\r\n" + "Product Name              : Unknown (0x316)";
	public static final String SUDO_KEYWORD = "sudo";
	public static final String INVALID_SSH_RESPONSE = "Wrong result";

	public static final String OOB_NULL_RESULT_MESSAGE = "Received <null> result after connecting to the IPMI BMC chip with the IPMI-over-LAN interface.";
	public static final String SYSTEM_POWER_UP_MESSAGE = "System power state is up";
	public static final String IPMI_CONNECTION_SUCCESS_WITH_IMPI_OVER_LAN_MESSAGE = "Successfully connected to the IPMI BMC chip with the IPMI-over-LAN " +
			"interface.";
	public static final String IPMI_CONNECTION_SUCCESS_WITH_IN_BAND_DRIVER_MESSAGE = "Successfully connected to the IPMI BMC chip with the in-band driver" +
			" interface.";
	public static final String PATH = "PATH";
	public static final String NO_OS_CONFIGURATION_MESSAGE = " - No OS command configuration for this host. Returning an empty result";
	public static final String IPMI_TOOL_COMMAND = "ipmitoolCommand ";
	public static final String BMC = "bmc";
	public static final String LIPMI = "lipmi";
	public static final String LINUX_BUILD_IPMI_COMMAND = "PATH=$PATH:/usr/local/bin:/usr/sfw/bin;export PATH;ipmitool -I open bmc info";
	public static final String TWGIPC = "TWGIPC";
	public static final String SERVICE_NAME_NOT_SPECIFIED_MESSAGE = "Service name is not specified";

	// Exception messages

	public static final String INVALID_PROTOCOL_EXCEPTION_MESSAGE = "Invalid protocol value: ";
	public static final String INVALID_SNMP_VERSION = "Invalid SNMP version: ";
	public final static String INVALID_PRIVACY_VALUE_EXCEPTION_MESSAGE = " Invalid Privacy value: ";

	// Protocols
	public final static String INVALID_PROTOCOL = "SFTPST";
	public final static String HTTP = "HTTP";
	public final static String HTTPS = "HTTPS";
	public final static String HTTPS_WITH_PORT = "HTTPS/443";
	public final static String SSH = "SSH";
	public final static String WMI = "WMI";
	public final static String WINRM = "WinRM";
	public final static String WBEM_HTTPS = "https/5989";
	public final static String IPMI = "IPMI";
	public final static String SNMP_VERSION = "2.4.6";
	public final static String EXECUTE_SNMP_GET_RESULT = "CMC DELL";
	public final static String EXPECTED_SNMP_RESULT = "CMC";
	public final static String SNMP_CONFIGURATION_COMMUNITY = "public";
	public final static String SNMP_GET_EXPECTED_RESULT_MATCHES_MESSAGE = "Hostname host-win - Successful SNMP Get of 1.3.6.1.4.1.674.10893.1.20. " +
			"Returned result: CMC DELL";
	public final static String SNMP_GET_EXPECTED_RESULT_NOT_MATCHES_MESSAGE = "Hostname host-win - SNMP test failed - " +
			"SNMP Get of 1.3.6.1.4.1.674.10893.1.20 was successful but the value of the returned " +
			"OID did not match with the expected result. Expected value: 2.4.6 - returned value CMC DELL.";
	public final static String SNMP_GET_SUCCESS_WITH_NO_EXPECTED_RESULT_MESSAGE = "Hostname host-win - Successful SNMP Get " +
			"of 1.3.6.1.4.1.674.10893.1.20. Returned result: CMC DELL.";
	public final static String SNMP_GET_EMPTY_RESULT_MESSAGE = "Hostname host-win - SNMP test failed - SNMP Get " +
			"of 1.3.6.1.4.1.674.10893.1.20 was unsuccessful due to an empty result.";
	public final static String SNMP_GET_NULL_RESULT_MESSAGE = "Hostname host-win - SNMP test failed - SNMP Get of 1.3.6.1.4.1.674.10893.1.20 was " +
			"unsuccessful due to a null result";
	public final static String SNMP_GET_EXCEPTION_MESSAGE = "Hostname host-win - SNMP test failed - SNMP Get of 1.3.6.1.4.1.674.10893.1.20 was unsuccessful " +
			"due to an exception. " +
			"Message: SNMPGet timeout";
	public final static String SNMP_GET_TIMEOUT_MESSAGE = "SNMPGet timeout";
	// Encryption

	public final static String AES = "aes";
	public final static String DES = "des";
	public final static String NO = "no";
	public final static String NONE = "none";
	public final static String INVALID_PRIVACY_VALUE = "sha-256";

	// Thread sleep duration in milliseconds
	public final static long THREAD_SLEEP_DURATION = 350000L;

	// OS
	public final static String LINUX = "LINUX";
	public final static String WINDOWS = "WINDOWS";
	public final static String HOST_WIN = "host-win";
	public static final String HOST_LINUX = "host-linux";
	public static final String VALID_SOLARIS_VERSION_TEN = "5.10";
	public static final String VALID_SOLARIS_VERSION_NINE = "5.9";
	public static final String OLD_SOLARIS_VERSION = "4.1.1B";
	public static final String INVALID_SOLARIS_VERSION = "invalid";
	public static final String OLD_SOLARIS_VERSION_MESSAGE = "Solaris version (4.1.1B) is too old";
	public static final String UNKNOWN_SOLARIS_VERSION = "Unknown Solaris version";
	public static final String SOLARIS_VERSION_NOT_IDENTIFIED_MESSAGE_TOKEN = "Could not";
	public final static String RESULT = "result";
	public final static String TEST = "test";
	public final static String TEST_BODY = "test_body";
	public final static String ERROR = "error";
	public final static String SUCCESSFUL_OS_DETECTION = "Successful OS detection operation";
	public final static String FAILED_OS_DETECTION = "Failed OS detection operation";
	public final static String CONFIGURED_OS_NT_MESSAGE = "Configured OS type : NETWORK";
	public final static String CONFIGURED_OS_SOLARIS_MESSAGE = "Configured OS type : SOLARIS";
	public static final String NEITHER_WMI_NOR_WINRM_ERROR = "Neither WMI nor WinRM credentials are configured for this host.";
	public static final String HOST_OS_IS_NOT_WINDOWS_MESSAGE = "Host OS is not Windows";
}
