package com.sentrysoftware.metricshub.agent.helper;

import java.nio.file.Path;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AgentConstants {

	// Configuration file
	public static final String PRODUCT_CODE = "metricshub";
	public static final String CONFIG_EXAMPLE_FILENAME = PRODUCT_CODE + "-example.yaml";
	public static final String DEFAULT_CONFIG_FILENAME = PRODUCT_CODE + ".yaml";
	public static final String DEFAULT_OTEL_CONFIG_FILENAME = "otel-config.yaml";
	public static final String DEFAULT_OTEL_CRT_FILENAME = "otel.crt";
	public static final String LOG_DIRECTORY_NAME = "logs";
	public static final String OTEL_DIRECTORY_NAME = "otel";
	public static final String CONFIG_DIRECTORY_NAME = "config";
	public static final String SECURITY_DIRECTORY_NAME = "security";
	public static final Path DEFAULT_OUTPUT_DIRECTORY = ConfigHelper.getDefaultOutputDirectory();
	public static final String FILE_PATH_FORMAT = "%s/%s";

	// Agent attribute information constants
	public static final String AGENT_INFO_OTEL_VERSION_NUMBER_ATTRIBUTE_KEY = "otel_version";
	public static final String AGENT_INFO_HC_VERSION_NUMBER_ATTRIBUTE_KEY = "hc_version";
	public static final String AGENT_INFO_BUILD_DATE_NUMBER_ATTRIBUTE_KEY = "build_date";
	public static final String AGENT_INFO_BUILD_NUMBER_ATTRIBUTE_KEY = "build_number";
	public static final String AGENT_INFO_VERSION_ATTRIBUTE_KEY = "version";
	public static final String AGENT_INFO_NAME_ATTRIBUTE_KEY = "name";
}
