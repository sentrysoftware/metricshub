package com.sentrysoftware.hardware.agent.service.opentelemetry;

import org.springframework.util.Assert;

import com.sentrysoftware.matrix.model.monitor.Monitor;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.MetricReaderFactory;
import io.opentelemetry.sdk.resources.Resource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access =  AccessLevel.PRIVATE)
public class OtelHelper {

	/**
	 * Initializes a Metrics SDK with a Resource and an instance of IntervalMetricReader.
	 *
	 * @param resource the resource used for the SdkMeterProvider
	 * @param periodicReaderFactory the periodic reader running the metrics collect then the OTLP metrics export
	 * @return a ready-to-use {@link SdkMeterProvider} instance
	 */
	public static SdkMeterProvider initOpenTelemetryMetrics(@NonNull final Resource resource,
			@NonNull final MetricReaderFactory periodicReaderFactory) {

		return SdkMeterProvider.builder()
				.setResource(resource)
				.registerMetricReader(periodicReaderFactory)
				.buildAndRegisterGlobal();

	}

	/**
	 * Create the target resource using the given target monitor
	 * 
	 * @param target   The monitor used create the OpenTelemetry {@link Resource}
	 * @param hostType The host type which goes in host.type
	 * @return Resource capturing identifying information about the target for which
	 *         signals are reported.
	 */
	public static Resource createHostResource(@NonNull final Monitor target, @NonNull final String hostType) {
		Assert.notNull(target.getId(), "Target id cannot be null.");
		Assert.notNull(target.getName(), "Target name cannot be null.");
		Assert.notNull(target.getFqdn(), "Target fqdn cannot be null.");

		final Attributes resourceAttributes = Attributes.builder()
				.put("host.id", target.getId())
				.put("host.name", target.getName())
				.put("host.type", hostType)
				.put("fqdn", target.getFqdn())
				.build();

		return Resource.create(resourceAttributes);

	}

	/**
	 * Create a Service resource
	 * 
	 * @param serviceName The name of the service, identified as service.name in the resulting resource
	 * @return Resource capturing identifying information about the service
	 */
	public static Resource createServiceResource(@NonNull final String serviceName) {
		final Attributes resourceAttributes = Attributes.builder()
				.put("service.name", serviceName)
				.build();

		return Resource.create(resourceAttributes);

	}
}
