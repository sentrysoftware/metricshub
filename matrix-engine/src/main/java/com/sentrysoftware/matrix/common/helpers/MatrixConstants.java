package com.sentrysoftware.matrix.common.helpers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

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
	public static final String METRICS_KEY = "mtx_sentry.connector.status";
	public static final String STATE_SET_METRIC_OK = "ok";
	public static final String STATE_SET_METRIC_FAILED = "failed";
	public static final String MONITOR_ATTRIBUTE_ID = "id";
	public static final String MONITOR_ATTRIBUTE_NAME = "name";
	public static final String MONITOR_ATTRIBUTE_CONNECTOR_ID = "connector_id";
	public static final String MONITOR_ATTRIBUTE_APPLIES_TO_OS = "applies_to_os";
	public static final String MONITOR_ATTRIBUTE_DETECTION = "detection";
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
	public static final String EXPECTED_VALUE_RETURNED_VALUE = "Expected value: %s - returned value %s.";
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
}
