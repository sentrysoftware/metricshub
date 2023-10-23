package com.sentrysoftware.metricshub.engine.common.helpers;

import static com.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind.AIX;
import static com.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind.HPUX;
import static com.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind.LINUX;
import static com.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind.OOB;
import static com.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind.SOLARIS;
import static com.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind.TRU64;
import static com.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind.VMS;
import static com.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind.WINDOWS;

import com.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MetricsHubConstants {

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
	public static final String VERTICAL_BAR = "|";

	/**
	 * Monitors information
	 **/
	public static final String MONITOR_ATTRIBUTE_ID = "id";
	public static final String MONITOR_ATTRIBUTE_NAME = "name";
	public static final String MONITOR_ATTRIBUTE_CONNECTOR_ID = "connector_id";
	public static final String MONITOR_ATTRIBUTE_APPLIES_TO_OS = "applies_to_os";
	public static final String IS_ENDPOINT = "is_endpoint";

	public static final String OTHER_MONITOR_JOB_TYPES = "otherMonitorJobTypes";

	// Map monitor job types to their priorities
	// @formatter:off
	public static final Map<String, Integer> MONITOR_JOBS_PRIORITY = Map.of(
		KnownMonitorType.HOST.getKey(), 1,
		KnownMonitorType.ENCLOSURE.getKey(), 2,
		KnownMonitorType.BLADE.getKey(), 3,
		KnownMonitorType.DISK_CONTROLLER.getKey(), 4,
		KnownMonitorType.CPU.getKey(), 5,
		OTHER_MONITOR_JOB_TYPES, 6
	);
	// @formatter:on

	public static final String LOG_COMPUTE_KEY_SUFFIX_TEMPLATE = "%s -> computes[%d]";

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
	 * MetricsHub / OpenTelemetry mappings
	 **/
	public static final String STORAGE = "storage";
	public static final String HOST_TYPE_COMPUTE = "compute";
	public static final String NETWORK = "network";

	// @formatter:off
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
		SOLARIS, HOST_TYPE_COMPUTE
	);
	// @formatter:on

	public static final String OTEL_HPUX_OS_TYPE = "hpux";
	public static final String OTEL_TRUE64_OS_TYPE = "true64";
	public static final String OTEL_OPENVMS_OS_TYPE = "openvms";
	public static final String OTEL_NETWORK_OS_TYPE = NETWORK;
	public static final String OTEL_STORAGE_OS_TYPE = STORAGE;
	public static final String OTEL_SOLARIS_OS_TYPE = "solaris";
	public static final String OTEL_WINDOWS_OS_TYPE = "windows";
	public static final String OTEL_MANAGEMENT_OS_TYPE = "management";
	public static final String OTEL_LINUX_OS_TYPE = "linux";
	public static final String OTEL_AIX_OS_TYPE = "aix";
	public static final String OTEL_MAC_OS_X_OS_TYPE = "macosx";
	public static final String OTEL_OPEN_BSD_OS_TYPE = "openbsd";
	public static final String OTEL_NET_BSD_OS_TYPE = "netbsd";
	public static final String OTEL_FREE_BSD_OS_TYPE = "freebsd";
	public static final String OTEL_SUN_OS_TYPE = "sun";

	// @formatter:off
	public static final Map<DeviceKind, String> HOST_TYPE_TO_OTEL_OS_TYPE = Map.of(
		VMS, OTEL_OPENVMS_OS_TYPE,
		TRU64, OTEL_TRUE64_OS_TYPE,
		HPUX, OTEL_HPUX_OS_TYPE,
		AIX, OTEL_AIX_OS_TYPE,
		LINUX, OTEL_LINUX_OS_TYPE,
		OOB, OTEL_MANAGEMENT_OS_TYPE,
		WINDOWS, OTEL_WINDOWS_OS_TYPE,
		DeviceKind.NETWORK, OTEL_NETWORK_OS_TYPE,
		DeviceKind.STORAGE, OTEL_STORAGE_OS_TYPE,
		SOLARIS, OTEL_SOLARIS_OS_TYPE
	);
	// @formatter:on

	/**
	 * Metrics
	 **/
	public static final String CONNECTOR_STATUS_METRIC_KEY = "metricshub.connector.status";
	public static final String STATE_SET_METRIC_OK = "ok";
	public static final String STATE_SET_METRIC_FAILED = "failed";
	public static final String PRESENT_STATUS = "hw.status{hw.type=\"%s\", state=\"present\"}";
	public static final String HW_HOST_CPU_THERMAL_DISSIPATION_RATE = "__hw.host.cpu.thermal_dissipation_rate";

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

	public static final String AUTOMATIC_NAMESPACE = "automatic";

	/**
	 * Files
	 **/
	public static final String CANT_FIND_EMBEDDED_FILE = "Can't find embedded file: ";

	//A compiled representation of a file converter. We attempt to match input like ${file::path} // NOSONAR on comment
	public static final Pattern FILE_PATTERN = Pattern.compile("\\$\\{file::(.*?)\\}", Pattern.CASE_INSENSITIVE);
	public static final Pattern SOURCE_REF_PATTERN = Pattern.compile("\\$\\{source::([^\\s\\}]+)\\}");
	public static final Pattern COLUMN_PATTERN = Pattern.compile("^\\s*\\$(\\d+)\\s*$");
	public static final Pattern DOUBLE_PATTERN = Pattern.compile("\\d+(\\.\\d+)?");
	public static final Pattern TRANSLATION_REF_PATTERN = Pattern.compile("\\$\\{translation::([^\\s]+)\\}");
	public static final Pattern HEXA_PATTERN = Pattern.compile("^[0-9A-Fa-f]+$");

	/**
	 * Translations
	 **/
	public static final String DEFAULT = "default";
}
