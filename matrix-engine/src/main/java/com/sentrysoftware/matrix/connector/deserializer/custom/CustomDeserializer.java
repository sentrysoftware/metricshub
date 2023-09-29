package com.sentrysoftware.matrix.connector.deserializer.custom;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.DelegatingDeserializer;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.Source;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Class implementing the functionality of the Post deserialization
 */
public class CustomDeserializer extends DelegatingDeserializer {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	/**
	 * Source reference pattern
	 */
	private static final Pattern REFERENCE_PATTERN = Pattern.compile(
		"\\s*(\\$\\{source::((monitors)\\.(.*)\\.(.*)\\.sources\\.(.*))|(pre\\.(.*))\\})\\s*",
		Pattern.MULTILINE | Pattern.CASE_INSENSITIVE
	);

	private static final long serialVersionUID = 1L;

	public CustomDeserializer(JsonDeserializer<?> delegate) {
		super(delegate);
	}

	@Override
	protected JsonDeserializer<?> newDelegatingInstance(JsonDeserializer<?> newDelegatee) {
		return new CustomDeserializer(newDelegatee);
	}

	@Override
	public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		final Object deserializedObject = super.deserialize(p, ctxt);

		callPostDeserialize(deserializedObject);

		return deserializedObject;
	}

	/**
	 * Post deserialization of the given object
	 *
	 * @param deserializedObject
	 */
	private void callPostDeserialize(final Object deserializedObject) {
		if (deserializedObject instanceof Source source) {
			final Set<String> refs = new HashSet<>();

			references(
				OBJECT_MAPPER.convertValue(deserializedObject, JsonNode.class),
				refs,
				val -> REFERENCE_PATTERN.matcher(val).find()
			);

			source.setReferences(refs);
		}
	}

	/**
	 * Traverse the given {@link JsonNode} and update the given reference collection
	 *
	 * @param jsonNode  node we wish to traverse
	 * @param refs      references collection to update
	 * @param predicate predicate function used to check the reference value
	 */
	private void references(final JsonNode jsonNode, final Collection<String> refs, final Predicate<String> predicate) {
		if (jsonNode == null) {
			return;
		}

		// If object? continue the traversal
		if (jsonNode.isObject()) {
			jsonNode.fields().forEachRemaining(entry -> references(entry.getValue(), refs, predicate));
		} else if (jsonNode.isArray()) {
			// if array? traverse each value in the array
			for (int i = 0; i < jsonNode.size(); i++) {
				references(jsonNode.get(i), refs, predicate);
			}
		} else {
			// If the value is not null and predicted, add it to the refs collection
			if (!jsonNode.isNull()) {
				final String value = jsonNode.asText();

				if (predicate.test(value)) {
					refs.add(value);
				}
			}
		}
	}
}
