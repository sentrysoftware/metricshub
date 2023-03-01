package com.sentrysoftware.matrix.connector.parser;

import java.nio.file.Path;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NodeProcessorHelper {

	/**
	 * Creates a new {@link ConstantsProcessor}
	 * 
	 * @return new {@link ConstantsProcessor}
	 */
	private static NodeProcessor constantsProcessor() {
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
			.destination(constantsProcessor())
			.build();
	}
}
