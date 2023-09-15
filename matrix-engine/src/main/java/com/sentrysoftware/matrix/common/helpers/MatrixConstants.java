package com.sentrysoftware.matrix.common.helpers;

import com.sentrysoftware.matrix.connector.model.common.DeviceKind;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.regex.Pattern;

import static com.sentrysoftware.matrix.connector.model.common.DeviceKind.AIX;
import static com.sentrysoftware.matrix.connector.model.common.DeviceKind.HPUX;
import static com.sentrysoftware.matrix.connector.model.common.DeviceKind.LINUX;
import static com.sentrysoftware.matrix.connector.model.common.DeviceKind.OOB;
import static com.sentrysoftware.matrix.connector.model.common.DeviceKind.SOLARIS;
import static com.sentrysoftware.matrix.connector.model.common.DeviceKind.TRU64;
import static com.sentrysoftware.matrix.connector.model.common.DeviceKind.VMS;
import static com.sentrysoftware.matrix.connector.model.common.DeviceKind.WINDOWS;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MatrixConstants {

	/**
	 * Table separators, tabulations, new line characters
	 **/
	public static final String SEMICOLON = ";";
	public static final String COMMA = ",";
	public static final String TABLE_SEP = SEMICOLON;
	public static final String EMPTY = "";
	public static final String WHITE_SPACE = " ";
	public static final String NEW_LINE = "\n";
	public static final String TAB = "\t";
	public static final String UNDERSCORE = "_";

	/**
	 * Monitors information
	 **/
	public static final String MONITOR_ATTRIBUTE_ID = "id";
	public static final String MONITOR_ATTRIBUTE_NAME = "name";
	public static final String MONITOR_ATTRIBUTE_CONNECTOR_ID = "connector_id";
	public static final String MONITOR_ATTRIBUTE_APPLIES_TO_OS = "applies_to_os";
	public static final String IS_ENDPOINT = "is_endpoint";

	/**
	 * Macros
	 **/

	public static final String USERNAME_MACRO = "%{USERNAME}";
	public static final String PASSWORD_MACRO = "%{PASSWORD}";
	public static final String HOSTNAME_MACRO = "%{HOSTNAME}";

	/**
	 * Threads and timeout
	 **/

	public static final long DEFAULT_JOB_TIMEOUT = 5 * 60L;
	public static final int DEFAULT_LOCK_TIMEOUT = 2 * 60; // 2 minutes
	public static final int MAX_THREADS_COUNT = 50;
	public static final long THREAD_TIMEOUT = 15 * 60L; // 15 minutes

	/**
	 * Matrix reloaded / OpenTelemetry mappings
	 **/
	public static final String STORAGE = "storage";
	public static final String HOST_TYPE_COMPUTE = "compute";
	public static final String NETWORK = "network";

	public static final Map<DeviceKind, String> HOST_TYPE_TO_OTEL_HOST_TYPE = Map.of(
			VMS, HOST_TYPE_COMPUTE,
			TRU64, HOST_TYPE_COMPUTE,
			HPUX, HOST_TYPE_COMPUTE,
			AIX, HOST_TYPE_COMPUTE,
			LINUX, HOST_TYPE_COMPUTE,
			OOB, HOST_TYPE_COMPUTE,
			WINDOWS, HOST_TYPE_COMPUTE,
			DeviceKind.NETWORK, NETWORK,
			DeviceKind.STORAGE, STORAGE,
			SOLARIS, HOST_TYPE_COMPUTE);

	public static final Map<DeviceKind, String> HOST_TYPE_TO_OTEL_OS_TYPE = Map.of(
			VMS, "openvms",
			TRU64, "true64",
			HPUX, "hpux",
			AIX, "aix",
			LINUX, "linux",
			OOB, "management",
			WINDOWS, "windows",
			DeviceKind.NETWORK, NETWORK,
			DeviceKind.STORAGE, STORAGE,
			SOLARIS, "solaris");

	/**
	 * Metrics
	 **/
	public static final String CONNECTOR_STATUS_METRIC_KEY = "mtx_sentry.connector.status";
	public static final String STATE_SET_METRIC_OK = "ok";
	public static final String STATE_SET_METRIC_FAILED = "failed";

	/**
	 * Host information
	 **/
	public static final String LOCALHOST = "localhost";
	public static final String HOST_NAME = "host.name";
	public static final String HOSTNAME_EXCEPTION_MESSAGE = "Hostname {} - Exception: ";

	/**
	 * Engine properties file
	 **/
	public static final String ENGINE_PROPERTIES_FILE_NAME = "engine.properties";
	public static final String ENGINE_VERSION_PROPERTY = "engine.version";

	/**
	 * Criteria and detection
	 **/
	public static final String SUCCESSFUL_OS_DETECTION_MESSAGE = "Successful OS detection operation";

	public static final String WMI_PROCESS_QUERY = "SELECT ProcessId,Name,ParentProcessId,CommandLine FROM Win32_Process";
	public static final String WMI_DEFAULT_NAMESPACE = "root\\cimv2";

	/**
	 * Files
	 **/
	public static final String CANT_FIND_EMBEDDED_FILE = "Can't find embedded file: ";

	//A compiled representation of a file converter. We attempt to match input like ${file::path} // NOSONAR on comment
	public static final Pattern FILE_PATTERN = Pattern.compile("\\$\\{file::(.*?)\\}", Pattern.CASE_INSENSITIVE);
	public static final Pattern SOURCE_REF_PATTERN = Pattern.compile("\\$\\{source::([^\\s]+)\\}");
	public static final Pattern COLUMN_PATTERN = Pattern.compile("^\\s*\\$(\\d+)\\s*$");
	public static final Pattern DOUBLE_PATTERN = Pattern.compile("\\d+(\\.\\d+)?");

}
