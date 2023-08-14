package com.sentrysoftware.matrix.connector.model.monitor.mapping;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MappingResource {
	private String type;
	private Map<String, String> attributes = new HashMap<>();
}
