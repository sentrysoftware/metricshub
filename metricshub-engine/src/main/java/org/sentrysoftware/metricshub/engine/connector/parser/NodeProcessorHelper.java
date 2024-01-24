package org.sentrysoftware.metricshub.engine.connector.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Path;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Helper class for creating instances of NodeProcessor implementations.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NodeProcessorHelper {

	/**
	 * Creates a new {@link ConstantsProcessor}.
	 *
	 * @return A new {@link ConstantsProcessor} instance.
	 */
	private static AbstractNodeProcessor constantsProcessor() {
		return new ConstantsProcessor();
	}

	/**
	 * Creates a {@link ExtendsProcessor} with a {@link ConstantsProcessor} destination.
	 *
	 * @param connectorDirectory The directory containing connectors YAML files.
	 * @param mapper             The object mapper.
	 * @return A new {@link ExtendsProcessor} instance.
	 */
	public static AbstractNodeProcessor withExtendsAndConstantsProcessor(
		final Path connectorDirectory,
		final ObjectMapper mapper
	) {
		return ExtendsProcessor
			.builder()
			.connectorDirectory(connectorDirectory)
			.mapper(mapper)
			.next(constantsProcessor())
			.build();
	}

	/**
	 * Creates a {@link ExtendsProcessor} with a {@link TemplateVariableProcessor} destination
	 * that redirects to {@link ConstantsProcessor}.
	 *
	 * @param connectorDirectory   The directory containing connectors YAML files.
	 * @param mapper               The object mapper.
	 * @param connectorVariables   The map of connector variables.
	 * @return A new {@link TemplateVariableProcessor} instance.
	 */
	public static AbstractNodeProcessor withExtendsAndTemplateVariableProcessor(
		final Path connectorDirectory,
		final ObjectMapper mapper,
		final Map<String, String> connectorVariables
	) {
		return ExtendsProcessor
			.builder()
			.connectorDirectory(connectorDirectory)
			.next(
				TemplateVariableProcessor.builder().connectorVariables(connectorVariables).next(constantsProcessor()).build()
			)
			.mapper(mapper)
			.build();
	}
}
