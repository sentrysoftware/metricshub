package com.sentrysoftware.hardware.agent.dto.application;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationConfigDto {

	private ProjectDto project;
	private String buildNumber;
	private String buildDate;
	private String hcVersion;
	private String otelVersion;
	 
}
