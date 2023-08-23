package com.sentrysoftware.matrix.connector.model.monitor.mapping;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MappingResource implements Serializable {

	private static final long serialVersionUID = 1L;

	private String type;
	@Builder.Default
	private Map<String, String> attributes = new HashMap<>();
}
