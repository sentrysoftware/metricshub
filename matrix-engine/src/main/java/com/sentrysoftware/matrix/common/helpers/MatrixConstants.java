package com.sentrysoftware.matrix.common.helpers;

import com.sentrysoftware.matrix.connector.model.common.DeviceKind;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.net.InetAddress;
import java.util.Map;
import java.util.regex.Pattern;

import static com.sentrysoftware.matrix.connector.model.common.DeviceKind.AIX;
import static com.sentrysoftware.matrix.connector.model.common.DeviceKind.HPUX;
import static com.sentrysoftware.matrix.connector.model.common.DeviceKind.LINUX;
import static com.sentrysoftware.matrix.connector.model.common.DeviceKind.NETWORK;
import static com.sentrysoftware.matrix.connector.model.common.DeviceKind.OOB;
import static com.sentrysoftware.matrix.connector.model.common.DeviceKind.SOLARIS;
import static com.sentrysoftware.matrix.connector.model.common.DeviceKind.STORAGE;
import static com.sentrysoftware.matrix.connector.model.common.DeviceKind.TRU64;
import static com.sentrysoftware.matrix.connector.model.common.DeviceKind.VMS;
import static com.sentrysoftware.matrix.connector.model.common.DeviceKind.WINDOWS;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MatrixConstants {

	public static final String N_A = "N/A";
	public static final String SEMICOLON = ";";
	public static final String TABLE_SEP = SEMICOLON;
	public static final String EMPTY = "";
	public static final String WHITE_SPACE = " ";
	public static final String NEW_LINE = "\n";
	public static final String TAB = "\t";
	public static final String WHITE_SPACE_TAB = WHITE_SPACE + TAB;
	public static final String YAML_CONNECTOR_KEY = "connector";
	public static final String YAML_DISPLAY_NAME_KEY = "displayName";
	public static final long DEFAULT_JOB_TIMEOUT = 5 * 60L;
	public static final String CONNECTOR_STATUS_METRIC_KEY = "mtx_sentry.connector.status";
	public static final String STATE_SET_METRIC_OK = "ok";
	public static final String STATE_SET_METRIC_FAILED = "failed";
	public static final String MONITOR_ATTRIBUTE_ID = "id";
	public static final String MONITOR_ATTRIBUTE_NAME = "name";
	public static final String MONITOR_ATTRIBUTE_CONNECTOR_ID = "connector_id";
	public static final String MONITOR_ATTRIBUTE_APPLIES_TO_OS = "applies_to_os";
	public static final String MONITOR_ATTRIBUTE_DETECTION = "detection";
	public static final String MONITOR_ATTRIBUTE_PARENT = "parent";
	public static final String USERNAME_MACRO = "%{USERNAME}";
	public static final String AUTHENTICATION_TOKEN_MACRO = "%{AUTHENTICATIONTOKEN}";
	public static final String PASSWORD_MACRO = "%{PASSWORD}";
	public static final String PASSWORD_BASE64_MACRO = "%{PASSWORD_BASE64}";
	public static final String BASIC_AUTH_BASE64_MACRO = "%{BASIC_AUTH_BASE64}";
	public static final String HOSTNAME_MACRO = "%{HOSTNAME}";
	public static final String SHA256_AUTH_MACRO = "%{SHA256_AUTH}";
	public static final int DEFAULT_LOCK_TIMEOUT = 2 * 60; // 2 minutes
	public static final String NEITHER_WMI_NOR_WINRM_ERROR = "Neither WMI nor WinRM credentials are configured for this host.";
	public static final String ALTERNATE_COLUMN_SEPARATOR = ",";
	public static final String IPMI_TOOL_SUDO_COMMAND = "PATH=$PATH:/usr/local/bin:/usr/sfw/bin;export PATH;%{SUDO:ipmitool}ipmitool -I ";
	public static final String IPMI_TOOL_SUDO_MACRO = "%{SUDO:ipmitool}";
	public static final String IPMI_DETECTION_FAILURE_MESSAGE = "Hostname %s - Failed to perform IPMI detection. %s is an unsupported OS for IPMI.";
	public static final String WMI_QUERY = "SELECT Description FROM ComputerSystem";
	public static final String WMI_NAMESPACE = "root\\hardware";
	public static final String EXPECTED_VALUE_RETURNED_VALUE = "Expected value: %s - returned value %s.";
	public static final String FAILED_OS_DETECTION_MESSAGE = "Failed OS detection operation";
	public static final String SUCCESSFUL_OS_DETECTION_MESSAGE = "Successful OS detection operation";
	public static final String CONFIGURE_OS_TYPE_MESSAGE = "Configured OS type : ";
	public static final String MALFORMED_CRITERION_MESSAGE = "Hostname {} - Malformed DeviceType criterion {}." +
			" Cannot process DeviceType criterion detection.";
	public static final String IPMI_TOOL_COMMAND = "PATH=$PATH:/usr/local/bin:/usr/sfw/bin;export PATH;ipmitool -I ";
	public static final String SOLARIS_VERSION_COMMAND = "/usr/bin/uname -r";
	public static final String IPMI_VERSION = "IPMI Version";
	public static final String IPMI_SOLARIS_VERSION_NOT_IDENTIFIED = "Hostname %s - Could not identify Solaris version %s. Exception: %s";
	public static final String OPEN_IPMI_INTERFACE_DRIVER = "open";
	public static final String END_OF_IPMI_COMMAND = " bmc info";
	public static final String OLD_SOLARIS_VERSION_MESSAGE = "Solaris version (%s) is too old for the host: %s IPMI cannot be " +
			"executed. Returning an empty result.";
	public static final String SOLARIS_VERSION_NOT_IDENTIFIED_MESSAGE_TOKEN = "Could not identify Solaris version as a valid one.\nThe 'uname -r' " +
			"command returned: ";
	public static final String BMC = "bmc";
	public static final String LIPMI = "lipmi";
	public static final String VERSION_REGEX_DELIMITER = "\\.";
	public static final String VERSION_REGEX_REPLACEMENT = "[^\\d\\.]";
	public static final String ENGINE_PROPERTIES_FILE_NAME = "engine.properties";
	public static final String ENGINE_VERSION_PROPERTY = "engine.version";
	public static final String MALFORMED_SERVICE_CRITERION_MESSAGE = "Malformed Service criterion.";
	public static final String SERVICE_NAME_NOT_SPECIFIED = "Service name is not specified. Skipping this test.";
	public static final String HOST_OS_IS_NOT_WINDOWS_SKIP_MESSAGE = "Host OS is not Windows. Skipping this test.";
	public static final String LOCAL_OS_IS_NOT_WINDOWS_SKIP_MESSAGE = "Local OS is not Windows. Skipping this test.";
	public static final String CRITERION_WMI_QUERY = "SELECT Name, State FROM Win32_Service WHERE Name = '%s'";
	public static final String CRITERION_WMI_NAMESPACE = "root\\cimv2";
	public static final String WINDOWS_IS_RUNNING_MESSAGE = "The %s Windows Service is currently running.";
	public static final String RUNNING = "running";
	public static final String WINDOWS_IS_NOT_RUNNING_MESSAGE = "The %s Windows Service is not reported as running:\n%s";
	public static final String MALFORMED_PROCESS_CRITERION_MESSAGE = "Hostname {} - Malformed process criterion {}. Cannot process process detection.";
	public static final String EMPTY_PROCESS_COMMAND_LINE_MESSAGE = "Hostname {} - Process Criterion, Process Command Line is empty.";
	public static final String NO_TEST_WILL_BE_PERFORMED_MESSAGE = "Process presence check: No test will be performed.";
	public static final String NO_TEST_WILL_BE_PERFORMED_UNKNOWN_OS_MESSAGE = "Process presence check: OS unknown, no test will be performed.";
	public static final String NO_TEST_WILL_BE_PERFORMED_AIX_MESSAGE = "Process presence check: No tests will be performed for OS: aix.";
	public static final String NO_TEST_WILL_BE_PERFORMED_REMOTELY_MESSAGE = "Process presence check: No test will be performed remotely.";
	public static final String REMOTE_PROCESS_MESSAGE = "Hostname {} - Process criterion, not localhost.";
	public static final String UNKNOWN_LOCAL_OS_MESSAGE = "Hostname {} - Process criterion, unknown local OS.";
	public static final String CRITERION_PROCESSOR_VISITOR_LOG_MESSAGE = "Hostname {} - Process Criterion, {}";
	public static final String NO_TEST_FOR_OS_MESSAGE = "Process presence check: No tests will be performed for OS: %s.";
	public static final String RUNNING_PROCESS_MATCH_REGEX_MESSAGE = "One or more currently running processes match the following regular expression:\n- " +
			"Regexp (should match with the command-line): %s";
	public static final String NO_RUNNING_PROCESS_MATCH_REGEX_MESSAGE = 
		"""
		No currently running processes match the following regular expression:
		- Regexp (should match with the command-line): %s
		- Currently running process list:
		%s""";
	public static final String CRITERION_PROCESSOR_VISITOR_QUERY = "SELECT ProcessId,Name,ParentProcessId,CommandLine FROM Win32_Process";
	public static final String CRITERION_PROCESSOR_VISITOR_NAMESPACE = "root\\cimv2";
	public static final String WQL_DETECTION_HELPER_NULL_MESSAGE = "wqlDetectionHelper cannot be null.";
	public static final String LOCALHOST = "localhost";
	public static final String MALFORMED_SNMP_GET_CRITERION_MESSAGE = "Hostname {} - Malformed SNMP Get criterion {}. Cannot process SNMP Get detection.";
	public static final String SNMP_CREDENTIALS_NOT_CONFIGURED_MESSAGE = "Hostname {} - The SNMP credentials are not configured. Cannot process SNMP " +
			"detection {}.";
	public static final String SNMP_FAILED_WITH_EXCEPTION_MESSAGE = "Hostname %s - SNMP test failed - SNMP Get of %s was" +
			" unsuccessful due to an exception. Message: %s";
	public static final String SNMP_OID_NOT_MATCH_MESSAGE = "Hostname %s - SNMP test failed - SNMP Get of %s was successful " +
			"but the value of the returned OID did not match with the" +
			" expected result. ";
	public static final String SUCCESSFUL_SNMP_MESSAGE = "Hostname %s - Successful SNMP Get of %s. Returned result: %s";
	public static final String SNMP_FAILED_NULL_RESULT_MESSAGE = "Hostname %s - SNMP test failed - SNMP Get of %s was unsuccessful due to a null result";
	public static final String SNMP_FAILED_EMPTY_RESULT_MESSAGE = "Hostname %s - SNMP test failed - SNMP Get of %s was unsuccessful due to an empty result.";
	public static final String SNMP_VALUE_CHECK_SUCCESSFUL_MESSAGE = "Hostname %s - Successful SNMP Get of %s. Returned result: %s.";
	public static final String AUTOMATIC_NAMESPACE = "automatic";
	public static final String WBEM_MALFORMED_CRITERION_MESSAGE = "Malformed criterion. Cannot perform detection.";
	public static final String WBEM_CREDENTIALS_NOT_CONFIGURED_MESSAGE = "The WBEM credentials are not configured for this host.";
	public static final Pattern SNMP_GETNEXT_RESULT_REGEX = Pattern.compile("\\w+\\s+\\w+\\s+(.*)");
	public static final String MALFORMED_SNMP_GET_NEXT_CRITERION_MESSAGE = "Hostname {} - Malformed SNMP GetNext criterion {}. " +
			"Cannot process SNMP GetNext detection.";
	public static final String SNMP_GETNEXT_FAILED_NULL_RESULT_MESSAGE = "Hostname %s - SNMP test failed - SNMP GetNext of %s was unsuccessful due to a null" +
			" result.";
	public static final String SNMP_GETNEXT_FAILED_EMPTY_RESULT_MESSAGE = "Hostname %s - SNMP test failed - SNMP GetNext of %s was unsuccessful due to an " +
			"empty result.";
	public static final String SNMP_GETNEXT_SUCCESSFUL_MESSAGE = "Hostname %s - Successful SNMP GetNext of %s. Returned result: %s.";

	public static final String SNMP_GETNEXT_FAILED_OID_NOT_UNDER_SAME_TREE_MESSAGE = "Hostname %s - SNMP test failed - SNMP GetNext of %s was successful " +
			"but the returned OID is not under the same tree. Returned OID: %s.";
	public static final String SNMP_GETNEXT_FAILED_OID_NOT_MATCHING_MESSAGE = "Hostname %s - SNMP test failed - SNMP GetNext of %s was successful but " +
			"the value of the returned OID did not match with the expected result. ";
	public static final String SNMP_GETNEXT_EXPECTED_RETURNED_VALUES_MESSAGE = "Expected value: %s - returned value %s.";
	public static final String SNMP_GETNEXT_CANNOT_EXTRACT_VALUE_MESSAGE = "Hostname %s - SNMP test failed - SNMP GetNext of %s was successful but the " +
			"value cannot be extracted. ";
	public static final String SNMP_GETNEXT_RETURNED_RESULT_MESSAGE = "Returned result: %s.";
	public static final String SNMP_GETNEXT_FAILED_WITH_EXCEPTION_MESSAGE = "Hostname %s - SNMP test failed - SNMP GetNext of %s was unsuccessful " +
			"due to an exception. Message: %s";
	public static final String MALFORMED_WMI_CRITERION_MESSAGE = "Malformed criterion. Cannot perform detection.";
	public static final String HOSTNAME_EXCEPTION_MESSAGE = "Hostname {} - Exception: ";
	public static final String UNKNOWN = "unknown";
	public static final String HOST_TYPE_STORAGE = "storage";
	public static final String HOST_TYPE_NETWORK = "network";
	public static final String HOST_TYPE_COMPUTE = "compute";
	public static final String OS_TYPE_STORAGE = "storage";
	public static final String OS_TYPE_NETWORK = "network";
	public static final String OS_TYPE_SOLARIS = "solaris";
	public static final String OS_TYPE_WINDOWS = "windows";
	public static final String OS_TYPE_MANAGEMENT = "management";
	public static final String OS_TYPE_LINUX = "linux";
	public static final String OS_TYPE_AIX = "aix";
	public static final String OS_TYPE_HP_UX = "hpux";
	public static final String OS_TYPE_TRUE64 = "true64";
	public static final String OS_TYPE_OPEN_VMS = "openvms";
	public static final Map<DeviceKind, String> HOST_TYPE_TO_OTEL_HOST_TYPE = Map.of(
			VMS, HOST_TYPE_COMPUTE,
			TRU64, HOST_TYPE_COMPUTE,
			HPUX, HOST_TYPE_COMPUTE,
			AIX, HOST_TYPE_COMPUTE,
			LINUX, HOST_TYPE_COMPUTE,
			OOB, HOST_TYPE_COMPUTE,
			WINDOWS, HOST_TYPE_COMPUTE,
			NETWORK, HOST_TYPE_NETWORK,
			STORAGE, HOST_TYPE_STORAGE,
			SOLARIS, HOST_TYPE_COMPUTE);

	public static final Map<DeviceKind, String> HOST_TYPE_TO_OTEL_OS_TYPE = Map.of(
			VMS, OS_TYPE_OPEN_VMS,
			TRU64, OS_TYPE_TRUE64,
			HPUX, OS_TYPE_HP_UX,
			AIX, OS_TYPE_AIX,
			LINUX, OS_TYPE_LINUX,
			OOB, OS_TYPE_MANAGEMENT,
			WINDOWS, OS_TYPE_WINDOWS,
			NETWORK, OS_TYPE_NETWORK,
			STORAGE, OS_TYPE_STORAGE,
			SOLARIS, OS_TYPE_SOLARIS);
	public static final String AGENT_HOSTNAME_VALUE = StringHelper
			.getValue(() -> InetAddress.getLocalHost().getCanonicalHostName(), UNKNOWN);
	public static final String OPENING_BRACKET = "{";
	public static final String CLOSING_BRACKET = "}";
	public static final String EQUALS_OPERATOR = "=";
	public static final String COMMA = ",";
	public static final String HOST_ID = "host.id";
	public static final String HOST_TYPE = "host.type";
	public static final String HOST_NAME = "host.name";
	public static final String OS_TYPE = "os.type";
	public static final String AGENT_HOST_NAME = "agent.host.name";
	public static final String HOST = "host";
	public static final String LOCATION = "location";
	public static final String HOST_CREATION_MESSAGE = "Hostname {} - Created host ID: {} ";

}
