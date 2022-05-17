package com.sentrysoftware.matrix.engine.host;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HardwareHost {

	private String id;
	private String hostname;
	private HostType type;
}
