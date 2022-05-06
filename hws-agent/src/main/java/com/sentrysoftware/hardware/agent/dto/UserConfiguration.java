package com.sentrysoftware.hardware.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
public class UserConfiguration {

	@NonNull
	private MultiHostsConfigurationDto multiHostsConfigurationDto;
	@NonNull
	private HostConfigurationDto hostConfigurationDto;
}
