package com.sentrysoftware.matrix.agent.helper;

import java.nio.file.Path;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AgentConstants {

	public static final String PRODUCT_CODE = "matrix";
	public static final String CONFIG_EXAMPLE_FILENAME = PRODUCT_CODE + "-config-example.yaml";
	public static final String DEFAULT_CONFIG_FILENAME = PRODUCT_CODE + "-config.yaml";
	public static final String DEFAULT_OTEL_CONFIG_FILENAME = "otel-config.yaml";
	public static final String DEFAULT_OTEL_CRT_FILENAME = "otel.crt";
	public static final String LOG_DIRECTORY_NAME = "logs";
	public static final String OTEL_DIRECTORY_NAME = "otel";
	public static final String CONFIG_DIRECTORY_NAME = "config";
	public static final String SECURITY_DIRECTORY_NAME = "security";
	public static final Path DEFAULT_OUTPUT_DIRECTORY = ConfigHelper.getDefaultOutputDirectory();
	public static final String FILE_PATH_FORMAT = "%s/%s";
}
