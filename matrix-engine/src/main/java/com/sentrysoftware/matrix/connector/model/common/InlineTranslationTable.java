package com.sentrysoftware.matrix.connector.model.common;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InlineTranslationTable implements ITranslationTable {

	private static final long serialVersionUID = 1652447044297886578L;

	private JsonNode translationsNode;

	@Override
	public InlineTranslationTable copy() {
		return InlineTranslationTable.builder().translationsNode(translationsNode).build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {}

	@Override
	public Map<String, String> getTranslations() {
		final Map<String, String> translationsMap = new HashMap<>();
		translationsNode.fields().forEachRemaining(entry -> translationsMap.put(entry.getKey(), entry.getValue().asText()));
		return translationsMap;
	}
}
