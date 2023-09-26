package com.sentrysoftware.matrix.agent.helper;

import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OtelTestHelper {

	/**
	 * Initializes a Metrics SDK with a Resource and an instance of {@link InMemoryMetricReader}.
	 *
	 * @param resource the resource used for the SdkMeterProvider
	 * @param inMemoryReader the periodic reader running the metrics collect then the OTLP metrics export
	 * @return a ready-to-use {@link SdkMeterProvider} instance
	 */
	public static SdkMeterProvider initOpenTelemetryMetrics(
		@NonNull final Resource resource,
		@NonNull final InMemoryMetricReader inMemoryReader
	) {
		return SdkMeterProvider.builder().setResource(resource).registerMetricReader(inMemoryReader).build();
	}
}
