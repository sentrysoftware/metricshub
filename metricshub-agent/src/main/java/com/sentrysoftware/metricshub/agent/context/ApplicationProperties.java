package com.sentrysoftware.metricshub.agent.context;

/**
 * This record is used to map the contents of the application.yaml file into a structured Java object.<br>
 * It contains fields that correspond to the data in the YAML file.
 *
 */
public record ApplicationProperties(
	Project project,
	String buildNumber,
	String buildDate,
	String hcVersion,
	String otelVersion
) {
	public record Project(String name, String version) {}
}
