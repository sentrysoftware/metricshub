package com.sentrysoftware.metricshub.engine.connector.deserializer.custom;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class BooleanDeserializer extends JsonDeserializer<Boolean> {

	private static final Map<String, Boolean> BOOLEAN_MAP = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

	static {
		BOOLEAN_MAP.put("0", Boolean.FALSE);
		BOOLEAN_MAP.put("1", Boolean.TRUE);
		BOOLEAN_MAP.put("true", Boolean.TRUE);
		BOOLEAN_MAP.put("false", Boolean.FALSE);
	}

	@Override
	public Boolean deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
		if (jsonParser == null) {
			return null;
		}

		final String key = jsonParser.getCurrentName();

		final String str = jsonParser.getValueAsString();
		if (str == null) {
			return null;
		}
		Boolean booleanValue = BOOLEAN_MAP.get(str);
		if (booleanValue == null) {
			throw new InvalidFormatException(
				jsonParser,
				String.format("Invalid boolean value encountered for property '%s'. Value:%s", key, str),
				str,
				Boolean.class
			);
		}
		return booleanValue;
	}
}
