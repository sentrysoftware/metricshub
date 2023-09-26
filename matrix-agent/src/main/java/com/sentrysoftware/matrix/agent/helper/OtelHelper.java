package com.sentrysoftware.matrix.agent.helper;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Map;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OtelHelper {

	/**
	 * Create a Service resource
	 *
	 * @param attributeMap key-value pairs of attributes that describe the resource.
	 * @return Resource capturing identifying information about the service
	 */
	public static Resource createServiceResource(@NonNull final Map<String, String> attributeMap) {
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
			.map(entry -> Attributes.of(AttributeKey.stringKey(entry.getKey()), entry.getValue()))
			.reduce((attributes1, attributes2) -> Attributes.builder().putAll(attributes1).putAll(attributes2).build())
			.orElseGet(() -> Attributes.builder().build());
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
}
