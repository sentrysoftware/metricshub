package org.sentrysoftware.metricshub.agent.config.exporter;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub Agent
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

import static com.fasterxml.jackson.annotation.Nulls.SKIP;

import com.fasterxml.jackson.annotation.JsonSetter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sentrysoftware.metricshub.agent.helper.ConfigHelper;

/**
 * Configuration class for the OpenTelemetry (OTEL) Protocol (OTLP) exporter.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OtlpExporterConfig {

	@Default
	@JsonSetter(nulls = SKIP)
	private String endpoint = "https://localhost:4317";

	private String trustedCertificatesFile;

	@Default
	private Map<String, char[]> headers = new HashMap<>(
		Map.of(
			"Authorization",
			new char[] {
				'B',
				'a',
				's',
				'i',
				'c',
				' ',
				'a',
				'H',
				'd',
				'z',
				'O',
				'l',
				'N',
				'l',
				'b',
				'n',
				'R',
				'y',
				'e',
				'V',
				'N',
				'v',
				'Z',
				'n',
				'R',
				'3',
				'Y',
				'X',
				'J',
				'l',
				'M',
				'S',
				'E',
				'='
			}
		)
	);

	/**
	 * Convert the <em>headers</em> map to key-value pairs separated by commas to match the format expected by the OTLP Exporter.
	 *
	 * @return {@link Optional} of string value. <em>Optional.empty</em> if the
	 *         <em>headers</em> map is null or empty.
	 */
	public Optional<String> getHeadersInOtlpFormat() {
		if (headers == null || headers.isEmpty()) {
			return Optional.empty();
		}

		final String result = headers
			.entrySet()
			.stream()
			.filter(entry -> Objects.nonNull(entry.getKey()) && Objects.nonNull(entry.getValue()))
			// The header can be encrypted, if not encrypted then decrypt(...) returns the
			// original header
			.map(entry -> String.format("%s=%s", entry.getKey(), String.valueOf(ConfigHelper.decrypt(entry.getValue()))))
			.collect(Collectors.joining(","));

		return Optional.of(result);
	}

	/**
	 * Whether the OTLP <code>endpoint</code> is defined or not.
	 *
	 * @return <code>true</code> if the <code>endpoint</code> is not
	 *         <code>null</code> and not blank
	 */
	public boolean hasEndpoint() {
		return endpoint != null && !endpoint.isBlank();
	}
}
