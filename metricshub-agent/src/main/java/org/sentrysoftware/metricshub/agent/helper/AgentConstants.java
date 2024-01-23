package org.sentrysoftware.metricshub.agent.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Path;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * AgentConstants contains constant values and configurations used across the MetricsHub agent.
 * It includes file names, directory names, default output directory, file path format, and attribute information constants.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AgentConstants {

	// Application YAML properties
	/**
	 * Application YAML file name
	 */
	public static final String APPLICATION_YAML_FILE_NAME = "application.yaml";

	// Configuration file
	/**
	 * Product code
	 */
	public static final String PRODUCT_CODE = "metricshub";
	/**
	 * Configuration example filename
	 */
	public static final String CONFIG_EXAMPLE_FILENAME = PRODUCT_CODE + "-example.yaml";
	/**
	 * Default configuration filename
	 */
	public static final String DEFAULT_CONFIG_FILENAME = PRODUCT_CODE + ".yaml";
	/**
	 * Default Open Telemetry configuration filename
	 */
	public static final String DEFAULT_OTEL_CONFIG_FILENAME = "otel-config.yaml";
	/**
	 * Default Open Telemetry (.crt) filename
	 */
	public static final String DEFAULT_OTEL_CRT_FILENAME = "otel.crt";
	/**
	 * Log directory name
	 */
	public static final String LOG_DIRECTORY_NAME = "logs";
	/**
	 * Open Telemetry directory name
	 */
	public static final String OTEL_DIRECTORY_NAME = "otel";
	/**
	 * Configuration directory name
	 */
	public static final String CONFIG_DIRECTORY_NAME = "config";
	/**
	 * Security directory name
	 */
	public static final String SECURITY_DIRECTORY_NAME = "security";
	/**
	 * Default output directory
	 */
	public static final Path DEFAULT_OUTPUT_DIRECTORY = ConfigHelper.getDefaultOutputDirectory();
	/**
	 * File path format
	 */
	public static final String FILE_PATH_FORMAT = "%s/%s";

	// Agent attribute information constants
	/**
	 * Agent information Open Telemetry version number attribute key
	 */
	public static final String AGENT_INFO_OTEL_VERSION_NUMBER_ATTRIBUTE_KEY = "otel_version";
	/**
	 * Agent info hc version number attribute key
	 */
	public static final String AGENT_INFO_HC_VERSION_NUMBER_ATTRIBUTE_KEY = "hc_version";
	/**
	 * Agent info build date number attribute key
	 */
	public static final String AGENT_INFO_BUILD_DATE_NUMBER_ATTRIBUTE_KEY = "build_date";
	/**
	 * Agent info build number attribute key
	 */
	public static final String AGENT_INFO_BUILD_NUMBER_ATTRIBUTE_KEY = "build_number";
	/**
	 * Agent info version attribute key
	 */
	public static final String AGENT_INFO_VERSION_ATTRIBUTE_KEY = "version";
	/**
	 * Agent info name attribute key
	 */
	public static final String AGENT_INFO_NAME_ATTRIBUTE_KEY = "name";

	// Object Mapper
	/**
	 * Object mapper
	 */
	public static final ObjectMapper OBJECT_MAPPER = ConfigHelper.newObjectMapper();
}
