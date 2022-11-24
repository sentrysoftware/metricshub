package com.sentrysoftware.hardware.agent.dto;

import static com.fasterxml.jackson.annotation.Nulls.SKIP;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonSetter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HostnameMapDto implements IHostnames {
	@Default
	@JsonSetter(nulls = SKIP)
	private Map<String, HostnameInfoDto> map = new HashMap<>();

	@Override
	public Set<String> getEntries() {
		return map.keySet();
	}

	@Override
	public Optional<HostnameInfoDto> getHostnameInfo(String hostname) {
		return Optional.ofNullable(map.get(hostname));
	}
}
