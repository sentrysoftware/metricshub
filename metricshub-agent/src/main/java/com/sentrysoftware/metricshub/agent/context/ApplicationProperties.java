package com.sentrysoftware.metricshub.agent.context;

/**
 * This record is used to map the contents of the application.yaml file into a
 * structured Java object.<br>
 * It contains fields that correspond to the data in the YAML file.
 *
 * @param project     Project informations
 * @param buildNumber Application build number
 * @param buildDate   Application build date
 * @param hcVersion   Hardware Connector version
 * @param otelVersion OpenTelemetry version
 */
public record ApplicationProperties(
	Project project,
	String buildNumber,
	String buildDate,
	String hcVersion,
	String otelVersion
) {
	/**
	 * Record representing project information.
	 *
	 * @param name    Project name.
	 * @param version Project version.
	 */
	public record Project(String name, String version) {}
}
