package com.sentrysoftware.hardware.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
public class UserConfiguration {

	@NonNull
	private MultiHostsConfigurationDTO multiHostsConfigurationDTO;
	@NonNull
	private HostConfigurationDTO hostConfigurationDTO;
}
