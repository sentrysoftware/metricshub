package org.sentrysoftware.metricshub.engine.connector.parser;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReferenceResolverProcessor extends AbstractNodeProcessor {

	@Builder
	public ReferenceResolverProcessor(final AbstractNodeProcessor next) {
		super(next);
	}

	private static final Pattern REGEX_SOURCE_REF_MONITORS = Pattern.compile(
		"\\$\\{source::(?!((?i)monitors\\.(\\w+)\\.(discovery|collect|simple)\\.(sources|mapping)\\.(\\w+)\\}))([\\w()]+)\\}"
	);
	private static final Pattern REGEX_SOURCE_REF_PRE = Pattern.compile(
		"\\$\\{source::(?!((?i)pre\\.(\\w+)\\}))([\\w()]+)\\}"
	);

	private static final Set<String> JOBS = Set.of("discovery", "collect", "simple");

	/**
	 * Calls the method {@link #processNode(JsonNode)}
	 * @param node a given json node
	 * @return JsonNode
	 * @throws IOException thrown by the super class method
	 */
	@Override
	public JsonNode process(JsonNode node) throws IOException {
		return super.process(node);
	}

	/**
	 * Processes a given Json node by calling {@link JsonNodeUpdater}
	 * @param node The JsonNode to be processed.
	 * @return {@link JsonNode} instance
	 * @throws IOException
	 */
	@Override
	protected JsonNode processNode(JsonNode node) throws IOException {
		@NonNull
		final BinaryOperator<String> updater = (valueToUpdate, context) -> {
			var monitorsRefMatcher = REGEX_SOURCE_REF_MONITORS.matcher(valueToUpdate);
			var preRefMatcher = REGEX_SOURCE_REF_PRE.matcher(valueToUpdate);
			var parts = context.split("\\.");

			String sourceName;
			while (monitorsRefMatcher.find()) {
				if (
					// CHECKSTYLE:OFF
					parts.length >= 4 &&
					parts[0].equals("monitors") &&
					JOBS.contains(parts[2]) &&
					(parts[3].equals("sources") || parts[3].equals("mapping"))
					// CHECKSTYLE:ON
				) {
					sourceName = monitorsRefMatcher.group(6);
					valueToUpdate =
						valueToUpdate.replace(
							sourceName,
							Stream.of(parts[0], parts[1], parts[2], "sources", sourceName).collect(Collectors.joining("."))
						);
				}
			}
			while (preRefMatcher.find()) {
				if (parts.length >= 2 && parts[0].equals("pre")) {
					sourceName = preRefMatcher.group(3);
					valueToUpdate =
						valueToUpdate.replace(sourceName, Stream.of(parts[0], sourceName).collect(Collectors.joining(".")));
				}
			}
			return valueToUpdate;
		};
		JsonNodeContextUpdater
			.jsonNodeContextUpdaterBuilder()
			.withJsonNode(node)
			.withPredicate(Objects::nonNull)
			.withUpdater(updater)
			.build()
			.update();
		return node;
	}
}
