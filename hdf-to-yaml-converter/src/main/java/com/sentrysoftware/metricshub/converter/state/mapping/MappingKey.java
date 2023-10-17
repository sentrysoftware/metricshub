package com.sentrysoftware.metricshub.converter.state.mapping;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class MappingKey implements IMappingKey {

	@Getter
	private final String key;
}
