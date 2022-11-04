package com.sentrysoftware.hardware.agent.dto;

import java.util.Optional;
import java.util.Set;

public interface IHostnames {

	Set<String> getEntries();

	Optional<HostnameInfoDto> getHostnameInfo(String hostname);

}
