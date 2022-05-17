package com.sentrysoftware.hardware.agent.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.hardware.agent.deserialization.TargetTypeDeserializer;

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
public class HardwareTargetDto {

	private String id;
	private String hostname;
	@JsonDeserialize(using = TargetTypeDeserializer.class)
	private HostType type;

	/**
	 * Create a new {@link HardwareHost} instance based on the current members
	 * 
	 * @return The {@link HardwareHost} instance
	 */
	public HardwareHost toHardwareTarget() {
		return HardwareHost
				.builder()
				.id(id)
				.hostname(hostname)
				.type(type)
				.build();
	}
}