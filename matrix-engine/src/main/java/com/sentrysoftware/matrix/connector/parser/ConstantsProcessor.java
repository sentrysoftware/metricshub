package com.sentrysoftware.matrix.connector.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@AllArgsConstructor
@Builder
@Data
public class ConstantsProcessor implements NodeProcessor {

	@NonNull
	private ObjectMapper mapper;

	@Override
	public JsonNode process(JsonNode node) {

		// TODO Perform Constants replacements

		// TODO Build a new node
		return node;
	}

}
