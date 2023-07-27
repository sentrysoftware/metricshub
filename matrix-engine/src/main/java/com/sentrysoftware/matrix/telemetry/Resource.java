package com.sentrysoftware.matrix.telemetry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Resource {

	private String type;
	private Map<String, String> attributes;
}
