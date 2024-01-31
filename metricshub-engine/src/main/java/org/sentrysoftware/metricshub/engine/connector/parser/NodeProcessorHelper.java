package org.sentrysoftware.metricshub.engine.connector.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Path;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NodeProcessorHelper {

	/**
	 * Creates a new {@link ConstantsProcessor}
	 *
	 * @return new {@link ConstantsProcessor}
	 */
	private static AbstractNodeProcessor constantsProcessor() {
		return new ConstantsProcessor();
	}

	/**
	 * Create a {@link ExtendsProcessor} with {@link ConstantsProcessor} destination
	 *
	 * @param connectorDirectory the connectors yaml files directory
	 * @param mapper object mapper
	 * @return new {@link ExtendsProcessor} instance
	 */
	public static AbstractNodeProcessor withExtendsAndConstantsProcessor(
		final Path connectorDirectory,
		final ObjectMapper mapper
	) {
		return ExtendsProcessor
			.builder()
			.connectorDirectory(connectorDirectory)
			.mapper(mapper)
			.next(ReferenceResolverProcessor.builder().next(constantsProcessor()).build())
			.build();
	}

	/**
	 * Create a {@link ExtendsProcessor} with {@link TemplateVariableProcessor} destination that redirects to {@link ConstantsProcessor}
	 *
	 * @param connectorDirectory the connectors yaml files directory
	 * @param mapper object mapper
	 * @return new {@link TemplateVariableProcessor} instance
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
				TemplateVariableProcessor
					.builder()
					.connectorVariables(connectorVariables)
					.next(ReferenceResolverProcessor.builder().next(constantsProcessor()).build())
					.build()
			)
			.mapper(mapper)
			.build();
	}
}
