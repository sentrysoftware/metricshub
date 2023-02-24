package com.sentrysoftware.matrix.connector.deserializer.custom;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.sentrysoftware.matrix.connector.model.monitor.task.AbstractCollect;
import com.sentrysoftware.matrix.connector.model.monitor.task.MonoCollect;
import com.sentrysoftware.matrix.connector.model.monitor.task.MultiCollect;

public class CollectDeserializer extends JsonDeserializer<AbstractCollect> {

	@Override
	public AbstractCollect deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
		if (parser == null)
			return null;

		final String key = parser.getCurrentName();
		if (MultiCollect.class.getSimpleName().equalsIgnoreCase(key)) {
			return parser.readValueAs(MultiCollect.class);
		}

		if (MonoCollect.class.getSimpleName().equalsIgnoreCase(key)) {
			return parser.readValueAs(MonoCollect.class);
		}

		throw new IOException(String
				.format("Invalid job name %s. Accepted jobs: [ discovery, multiCollect, monoCollect, allAtOnce ].", key));
	}

}
