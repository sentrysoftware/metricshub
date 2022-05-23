package com.sentrysoftware.hardware.agent.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.hardware.agent.deserialization.HostTypeDeserializer;

import com.sentrysoftware.matrix.engine.host.HardwareHost;
import com.sentrysoftware.matrix.engine.host.HostType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HardwareHostDto {

	private String id;
	private String hostname;
	@JsonDeserialize(using = HostTypeDeserializer.class)
	private HostType type;

	/**
	 * Create a new {@link HardwareHost} instance based on the current members
	 * 
	 * @return The {@link HardwareHost} instance
	 */
	public HardwareHost toHardwareHost() {
		return HardwareHost
				.builder()
				.id(id)
				.hostname(hostname)
				.type(type)
				.build();
	}
}