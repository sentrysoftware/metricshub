package com.sentrysoftware.matrix.converter.state.mapping;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.UnaryOperator;


public interface IMappingKey {

	/**
	 * Create an entry indicating where to put the new mapping key
	 * 
	 * @param where where the property key must be set. This is the key of the node located under the mapping node
	 * @param newKey the key used to build the {@link MappingKey} instance (metric or attribute key).
	 * @return key-value pair of String value and {@link MappingKey} instance
	 */
	static Entry<String, IMappingKey> of(final String where, final String newKey) {
		return Map.entry(where, new MappingKey(newKey));
	}

	/**
	 * Create an entry indicating where to put the new mapping key and how to
	 * convert its value
	 * 
	 * @param where where the property key must be set. This is the key of the node located under the mapping node
	 * @param newKey the key used to build the {@link MappingKeyWithValueConverter} instance (metric or attribute key).
	 * @param valueConverter function used to build the
	 * {@link MappingKeyWithValueConverter} that indicates how to convert the value
	 * @return  key-value pair of String value and {@link MappingKeyWithValueConverter} instance
	 */
	static Entry<String, IMappingKey> of(final String where, final String newKey, final UnaryOperator<String> valueConverter) {
		return Map.entry(where, new MappingKeyWithValueConverter(newKey, valueConverter));
	}
}
