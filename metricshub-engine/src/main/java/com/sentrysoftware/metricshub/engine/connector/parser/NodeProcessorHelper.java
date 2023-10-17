package com.sentrysoftware.metricshub.engine.connector.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
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
	 * @param mapper
	 * @return new {@link ExtendsProcessor} instance
	 */
	public static NodeProcessor withExtendsAndConstantsProcessor(
		final Path connectorDirectory,
		final ObjectMapper mapper
	) {
		return ExtendsProcessor
			.builder()
			.connectorDirectory(connectorDirectory)
			.destination(constantsProcessor())
			.mapper(mapper)
			.build();
	}
}
