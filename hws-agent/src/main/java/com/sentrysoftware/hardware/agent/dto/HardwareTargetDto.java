package com.sentrysoftware.hardware.agent.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.hardware.agent.deserialization.TargetTypeDeserializer;
import com.sentrysoftware.matrix.engine.target.HardwareTarget;
import com.sentrysoftware.matrix.engine.target.TargetType;

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
	private TargetType type;

	/**
	 * Create a new {@link HardwareTarget} instance based on the current members
	 * 
	 * @return The {@link HardwareTarget} instance
	 */
	public HardwareTarget toHardwareTarget() {
		return HardwareTarget
				.builder()
				.id(id)
				.hostname(hostname)
				.type(type)
				.build();
	}
}