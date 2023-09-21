package com.sentrysoftware.matrix.connector.deserializer.custom;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.TRANSLATION_REF_PATTERN;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.sentrysoftware.matrix.connector.model.common.ITranslationTable;
import com.sentrysoftware.matrix.connector.model.common.InlineTranslationTable;
import com.sentrysoftware.matrix.connector.model.common.ReferenceTranslationTable;
import java.io.IOException;
import java.util.regex.Matcher;

public class TranslationTableDeserializer extends JsonDeserializer<ITranslationTable> {

	@Override
	public ITranslationTable deserialize(JsonParser parser, DeserializationContext ctxt)
		throws IOException, JacksonException {
		if (parser == null) {
			return null;
		}

		final String str = parser.getValueAsString();
		if (str == null) {
			return null;
		}

		final Matcher matcher = TRANSLATION_REF_PATTERN.matcher(str);

		// In case of a ReferenceTranslationTable, the value is like: ${translation::<translationTableName>}
		if (matcher.find()) {
			return ReferenceTranslationTable.builder().name(matcher.group(1)).build();
		} else { // In case of a InlineTranslationTable, the value is a JsonNode
			return InlineTranslationTable.builder().translationsNode(parser.readValueAs(JsonNode.class)).build();
		}
	}
}
