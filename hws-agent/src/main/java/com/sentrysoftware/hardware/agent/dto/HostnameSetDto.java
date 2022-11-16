package com.sentrysoftware.hardware.agent.dto;

import static com.fasterxml.jackson.annotation.Nulls.SKIP;

import java.util.HashSet;
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
public class HostnameSetDto implements IHostnames {
	@Default
	@JsonSetter(nulls = SKIP)
	private Set<String> set = new HashSet<>();

	@Override
	public Set<String> getEntries() {
		return set;
	}

	@Override
	public Optional<HostnameInfoDto> getHostnameInfo(String hostname) {
		return Optional.empty();
	}
}
