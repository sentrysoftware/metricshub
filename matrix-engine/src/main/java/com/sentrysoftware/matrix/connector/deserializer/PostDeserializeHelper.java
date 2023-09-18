package com.sentrysoftware.matrix.connector.deserializer;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.sentrysoftware.matrix.connector.deserializer.custom.CustomDeserializer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PostDeserializeHelper {

	/**
	 * Add the {@link CustomDeserializer} as Post deserializer to the given
	 * {@link ObjectMapper}
	 *
	 * @param objectMapper provides functionality for reading and writing JSON
	 * @return the updated {@link ObjectMapper}
	 */
	public static ObjectMapper addPostDeserializeSupport(final ObjectMapper objectMapper) {
		final SimpleModule module = new SimpleModule();
		module.setDeserializerModifier(
			new BeanDeserializerModifier() {
				@Override
				public JsonDeserializer<?> modifyDeserializer(
					DeserializationConfig config,
					BeanDescription beanDescription,
					JsonDeserializer<?> originalDeserializer
				) {
					return new CustomDeserializer(originalDeserializer);
				}
			}
		);

		objectMapper.registerModule(module);

		return objectMapper;
	}
}
