package com.sentrysoftware.matrix.telemetry;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Resource {

	private String type;
	private Map<String, String> attributes;
}
