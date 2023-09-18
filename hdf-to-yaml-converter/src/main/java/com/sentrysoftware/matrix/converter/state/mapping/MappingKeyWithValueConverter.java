package com.sentrysoftware.matrix.converter.state.mapping;

import java.util.function.UnaryOperator;
import lombok.Getter;

public class MappingKeyWithValueConverter extends MappingKey {

	public MappingKeyWithValueConverter(String key, UnaryOperator<String> valueConverter) {
		super(key);
		this.valueConverter = valueConverter;
	}

	@Getter
	private final UnaryOperator<String> valueConverter;
}
