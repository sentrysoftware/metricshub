package com.sentrysoftware.matrix.connector.parser;

import java.nio.file.Path;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@AllArgsConstructor
@Builder
@Data
public class ExtendsProcessor implements NodeProcessor {

	@NonNull
	private Path connectorDirectory;
	@NonNull
	private NodeProcessor destination;

	@Override
	public JsonNode process(JsonNode node) {

		// TODO Perform pre processing. Extends management

		// Call next processor
		return destination.process(node);
	}

	
}
