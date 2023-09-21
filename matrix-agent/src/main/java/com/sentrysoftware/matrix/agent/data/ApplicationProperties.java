package com.sentrysoftware.matrix.agent.data;

public record ApplicationProperties(
	Project project,
	String buildNumber,
	String buildDate,
	String hcVersion,
	String otelVersion
) {
	public record Project(String name, String version) {}
}
