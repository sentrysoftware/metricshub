package com.sentrysoftware.hardware.agent.dto;

import java.util.Optional;
import java.util.Set;

public interface IHostnames {

	/**
	 * 
	 * @return hostname entries
	 */
	Set<String> getEntries();

	/**
	 * Contains extra labels. Is optional to handle hostGroups, as these will not necessarily have extraLabels.
	 * 
	 * @param hostname
	 * @return 
	 */
	Optional<HostnameInfoDto> getHostnameInfo(String hostname);

}
