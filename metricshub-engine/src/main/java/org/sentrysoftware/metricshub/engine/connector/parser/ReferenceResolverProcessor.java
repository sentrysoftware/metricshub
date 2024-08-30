package org.sentrysoftware.metricshub.engine.connector.parser;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub Engine
 * ჻჻჻჻჻჻
 * Copyright 2023 - 2024 Sentry Software
 * ჻჻჻჻჻჻
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱
 */

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReferenceResolverProcessor extends AbstractNodeProcessor {

	private static final String SOURCE_REF_FORMAT = "${source::%s}";

	private static final Pattern REGEX_SOURCE_REF_MONITORS = Pattern.compile(
		"\\$\\{source::(?!((?i)monitors\\.([\\w()\\.-]+)\\.(discovery|collect|simple)\\.sources\\.([\\w()\\.-]+)\\}|(?i)pre\\.([\\w()\\.-]+)\\}))([\\w()\\.-]+)\\}"
	);
	private static final Pattern REGEX_SOURCE_REF_PRE = Pattern.compile(
		"\\$\\{source::(?!((?i)beforeAll\\.([\\w()\\.-]+)\\}))([\\w()\\.-]+)\\}"
	);

	@Builder
	public ReferenceResolverProcessor(final AbstractNodeProcessor next) {
		super(next);
	}

	private static final Set<String> JOBS = Set.of("discovery", "collect", "simple");

	/**
	 * Replaces relative source references with the full source references
	 *
	 * @param valueToUpdate The value to be replaced
	 * @param context The path in the JSON tree from the starting JsonNode having the key "monitors" or "pre"
	 * @return A string representing the updated value
	 */
	private String updateSourceReferences(String valueToUpdate, final String context) {
		if (valueToUpdate == null || valueToUpdate.isEmpty() || context == null || context.isEmpty()) {
			return valueToUpdate;
		}

		final var parts = context.split("\\.");

		valueToUpdate =
			REGEX_SOURCE_REF_MONITORS
				.matcher(valueToUpdate)
				.replaceAll(match -> {
					// If the context starts with the string "monitors", perform the corresponding replacement
					if (
						// CHECKSTYLE:OFF
						parts.length >= 4 &&
						parts[0].equals("monitors") &&
						JOBS.contains(parts[2]) &&
						(parts[3].equals("sources") || parts[3].equals("mapping"))
						// CHECKSTYLE:ON
					) {
						return Matcher.quoteReplacement(
							String.format(
								SOURCE_REF_FORMAT,
								Stream.of(parts[0], parts[1], parts[2], "sources", match.group(6)).collect(Collectors.joining("."))
							)
						);
					}

					return Matcher.quoteReplacement(match.group());
				});

		return REGEX_SOURCE_REF_PRE
			.matcher(valueToUpdate)
			.replaceAll(match -> {
				if (parts.length >= 2 && parts[0].equals("beforeAll")) {
					return Matcher.quoteReplacement(
						String.format(SOURCE_REF_FORMAT, Stream.of(parts[0], match.group(3)).collect(Collectors.joining(".")))
					);
				}

				return Matcher.quoteReplacement(match.group());
			});
	}

	/**
	 * Processes the given JsonNode by calling {@link JsonNodeUpdater}
	 *
	 * @param node The JsonNode to be processed.
	 * @return The processed node as a {@link JsonNode} instance
	 * @throws IOException thrown by the super class method
	 */
	@Override
	protected JsonNode processNode(final JsonNode node) throws IOException {
		JsonNodeContextUpdater
			.jsonNodeContextUpdaterBuilder()
			.withJsonNode(node)
			.withPredicate(Objects::nonNull)
			.withUpdater(this::updateSourceReferences)
			.build()
			.update();
		return node;
	}
}
