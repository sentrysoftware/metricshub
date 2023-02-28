package com.sentrysoftware.matrix.connector.parser;

import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentrysoftware.matrix.common.helpers.JsonHelper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NodeProcessorHelper {

	/**
	 * Creates a new {@link ConstantsProcessor}
	 * 
	 * @param mapper
	 * @return new {@link ConstantsProcessor}
	 */
	private static NodeProcessor constantsProcessor(ObjectMapper mapper) {
		return new ConstantsProcessor();
	}

	/**
	 * Create a {@link ExtendsProcessor} with {@link ConstantsProcessor} destination
	 * 
	 * @param connectorDirectory
	 * @return new {@link ExtendsProcessor} instance
	 */
	public static NodeProcessor withExtendsAndConstantsProcessor(Path connectorDirectory) {
		return ExtendsProcessor.builder()
			.connectorDirectory(connectorDirectory)
			.destination(constantsProcessor(JsonHelper.buildYamlMapper()))
			.build();
	}
}
