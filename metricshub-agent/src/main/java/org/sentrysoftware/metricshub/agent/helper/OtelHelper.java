package org.sentrysoftware.metricshub.agent.helper;

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

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Helper class providing methods related to OpenTelemetry (OTEL) configuration.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OtelHelper {

	/**
	 * Create an OTEL Resource
	 *
	 * @param attributeMap key-value pairs of attributes that describe the resource.
	 * @return Resource capturing identifying information about service, host, etc.
	 */
	public static Resource createOpenTelemetryResource(@NonNull final Map<String, String> attributeMap) {
		return Resource.create(buildOtelAttributesFromMap(attributeMap));
	}

	/**
	 * Build OpenTelemetry attributes from the given map
	 *
	 * @param attributeMap key-value pairs of attributes
	 * @return OTEL {@link Attributes} instance
	 */
	public static Attributes buildOtelAttributesFromMap(@NonNull final Map<String, String> attributeMap) {
		return attributeMap
			.entrySet()
			.stream()
			.filter(entry -> Objects.nonNull(entry.getValue()))
			.filter(entry -> isAcceptedKey(entry.getKey()))
			.map(entry -> Attributes.of(AttributeKey.stringKey(entry.getKey()), entry.getValue()))
			.reduce((attributes1, attributes2) -> Attributes.builder().putAll(attributes1).putAll(attributes2).build())
			.orElseGet(() -> Attributes.builder().build());
	}

	/**
	 * Whether this key should be accepted or not.<br>
	 * If the key starts with '__' then it is not accepted.
	 * @param key OTEL key to test
	 *
	 * @return boolean value
	 */
	public static boolean isAcceptedKey(final String key) {
		return !key.startsWith("__");
	}

	/**
	 * Initializes an OpenTelemetry SDK with a Resource and an instance of
	 * IntervalMetricReader.
	 *
	 * @param resource             the resource used for the SdkMeterProvider
	 * @param otelSdkConfiguration configuration for the OpenTelemetry SDK.
	 * @return a ready-to-use {@link AutoConfiguredOpenTelemetrySdk} instance
	 */
	public static AutoConfiguredOpenTelemetrySdk initOpenTelemetrySdk(
		@NonNull final Resource resource,
		@NonNull final Map<String, String> otelSdkConfiguration
	) {
		return AutoConfiguredOpenTelemetrySdk
			.builder()
			.addPropertiesSupplier(() -> otelSdkConfiguration)
			.addResourceCustomizer((r, c) -> resource)
			// Control the registration of a shutdown hook to shut down the SDK when
			// appropriate. By default, the shutdown hook is registered.
			.disableShutdownHook()
			.build();
	}

	/**
	 * Create the host resource based on the given attributes.
	 *
	 * @param computedHostResourceAttributes Host Resource attributes: host.id, host.name, os.type, host.type, etc
	 *                                       collected by the engine.
	 * @param userAttributes                 User configured attributes.
	 * @return OTEL {@link Resource} instance representing the host.
	 */
	public static Resource createHostResource(
		@NonNull final Map<String, String> computedHostResourceAttributes,
		@NonNull final Map<String, String> userAttributes
	) {
		// Prepare the resource attributes
		final Map<String, String> attributes = new HashMap<>(computedHostResourceAttributes);

		// Add user attributes to the resource attributes
		// host.name is managed by the engine based on the resolveHostnameToFqdn flag, so we shouldn't override it here.
		// host.type is managed by the engine using a set of rules to determine the host.type in OTEL format so we shouldn't override it here.
		// The other attributes are user-defined so we can override them.
		userAttributes
			.entrySet()
			.stream()
			.filter(keyValue -> {
				final String key = keyValue.getKey();
				return !key.equals("host.name") && !key.equals("host.type");
			})
			.forEach(entry -> attributes.put(entry.getKey(), entry.getValue()));

		return createOpenTelemetryResource(attributes);
	}

	/**
	 * Merge the given OTEL SDK attributes
	 *
	 *
	 * @param firstAttributes  First {@link Attributes} instance to merge
	 * @param secondAttributes Second  {@link Attributes} instance to merge
	 *
	 * @return new {@link Attributes} instance
	 */
	public static Attributes mergeOtelAttributes(final Attributes firstAttributes, final Attributes secondAttributes) {
		return Attributes.builder().putAll(firstAttributes).putAll(secondAttributes).build();
	}
}
