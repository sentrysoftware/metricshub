package org.sentrysoftware.metricshub.engine.connector.deserializer;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.sentrysoftware.metricshub.engine.connector.deserializer.custom.CustomDeserializer;

/**
 * Helper class for adding post-deserialization support to an {@link ObjectMapper}.
 * This class provides a method to add a {@link CustomDeserializer} as a post deserializer
 * for handling special deserialization requirements.
 */
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
