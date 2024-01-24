package org.sentrysoftware.metricshub.agent.helper;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.sentrysoftware.metricshub.agent.deserialization.PostConfigDeserializer;

/**
 * Helper class for adding post-deserialization support to an {@link ObjectMapper}.
 * It provides a utility method to register a {@link PostConfigDeserializer} for post-processing
 * the deserialization of certain types.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PostConfigDeserializeHelper {

	/**
	 * Add the {@link PostConfigDeserializer} as Post deserializer to the given
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
					return new PostConfigDeserializer(originalDeserializer);
				}
			}
		);

		objectMapper.registerModule(module);

		return objectMapper;
	}
}
