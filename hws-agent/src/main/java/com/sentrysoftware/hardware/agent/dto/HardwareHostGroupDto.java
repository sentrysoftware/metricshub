package com.sentrysoftware.hardware.agent.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.hardware.agent.deserialization.HostTypeDeserializer;
import com.sentrysoftware.hardware.agent.deserialization.HostnamesDeserializer;
import com.sentrysoftware.matrix.engine.host.HostType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HardwareHostGroupDto {

	@NonNull
	@JsonDeserialize(using = HostnamesDeserializer.class)
	private IHostnames hostnames;
	@JsonDeserialize(using = HostTypeDeserializer.class)
	private HostType type;
}
