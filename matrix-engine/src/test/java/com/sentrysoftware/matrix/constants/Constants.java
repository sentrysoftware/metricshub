package com.sentrysoftware.matrix.constants;

public class Constants {

	// Global constants

	public static final String USERNAME = "testUser";
	public static final String PASSWORD = "testPassword";
	public static final String WBEM_NAMESPACE = "testWbemNamespace";
	public static final String WINRM_NAMESPACE = "testWinRmNamespace";
	public static final String WMI_NAMESPACE = "testWmiNamespace";
	public static final String WBEM_VCENTER = "testWbemVCenter";
	public static final byte[] BMC_KEY = new byte[]{0x06, 0x66};
	public static final Long SSH_CONFIGURATION_TIMEOUT = 50L;
	public static final Long STRATEGY_TIMEOUT = 100L;
	public static final Long RETRY_DELAY = 30L;
	public static final String SSH_SUDO_COMMAND = "sudo pwd";
	public static final String HTTP_GET = "GET";
	public static final String MY_CONNECTOR_1_NAME = "myConnector1";

	// Yaml test file name
	public final static String YAML_TEST_FILE_NAME = "AAC";

	// Host information
	public static final String LOCALHOST = "localhost";
	public final static String HOST_ID = "PC-120";

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

	public final static String DETECTION_FOLDER = "src/test/resources/test-files/connector/detection";
	public final static String CONNECTOR_YAML = "connector.yaml";

	public final static String RESULT = "result";
	public final static String TEST = "test";
	public final static String TEST_BODY = "test_body";
	public final static String ERROR = "error";
	public final static String SUCCESSFUL_OS_DETECTION = "Successful OS detection operation";
	public final static String FAILED_OS_DETECTION = "Failed OS detection operation";
	public final static String CONFIGURED_OS_NT_MESSAGE = "Configured OS type : NT";
	public final static String CONFIGURED_OS_SOLARIS_MESSAGE = "Configured OS type : SOLARIS";
}
