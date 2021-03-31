package com.sentrysoftware.matrix.engine.target;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HardwareTarget {

	private String id;
	private String hostname;
	private TargetType type;
}
