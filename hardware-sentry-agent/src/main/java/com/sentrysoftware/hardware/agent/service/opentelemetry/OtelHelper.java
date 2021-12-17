package com.sentrysoftware.hardware.agent.service.opentelemetry;

import java.util.Map;
import java.util.Objects;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.MetricReaderFactory;
import io.opentelemetry.sdk.resources.Resource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access =  AccessLevel.PRIVATE)
public class OtelHelper {

	private static final String RESOURCE_HOST_NAME_PROP = "host.name";

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
					.build();
	}

	/**
	 * Create host resource using the given information
	 * 
	 * @param id                    Target id
	 * @param hostname              Target configured hostname
	 * @param hostType              Target type
	 * @param fqdn                  Collected fqdn
	 * @param resolveHostnameToFqdn Whether we should resolve the hostname to Fqdn
	 * @param extraLabels           Extra labels configured on the target
	 * @return Resource capturing identifying information about the target for which
	 *         signals are reported.
	 */
	public static Resource createHostResource(
			@NonNull final String id,
			@NonNull String hostname,
			@NonNull final String hostType,
			@NonNull final String fqdn,
			final boolean resolveHostnameToFqdn,
			@NonNull final Map<String, String> extraLabels) {

		// Which hostname?
		hostname = getResourceHostname(
				hostname,
				fqdn,
				resolveHostnameToFqdn,
				extraLabels
		);

		// Build attributes
		final AttributesBuilder builder = Attributes.builder()
				.put("host.id", id)
				.put(RESOURCE_HOST_NAME_PROP, hostname)
				.put("host.type", hostType)
				.put("fqdn", fqdn);

		// Extra attributes? Ok let's override them here
		extraLabels
			.keySet()
			.stream()
			.filter(key -> !RESOURCE_HOST_NAME_PROP.equals(key)) // host.name has a special handling
			.filter(key -> Objects.nonNull(extraLabels.get(key)))
			.forEach(key -> builder.put(key, extraLabels.get(key)));

		return Resource.create(builder.build());

	}


	/**
	 * Order
	 * <ol>
	 *   <li>User's extra label <code>fqdn</code> with <code>resolveHostnameToFqdn=true</code></li>
	 *   <li>User's extra label <code>host.name</code> value</li>
	 *   <li>Collected <code>fqdn</code> when the <code>resolveHostnameToFqdn=true</code></li>
	 *   <li>Configured target's <code>hostname</code></li>
	 * </ol>
	 * 
	 * @param hostname              Configured target's hostname
	 * @param collectedFqdn         Collected fqdn
	 * @param resolveHostnameToFqdn global configuration property to tell the agent
	 *                              resolve host.name as Fqdn
	 * @param extraLabels           Configured extra labels
	 * @return String value
	 */
	private static String getResourceHostname(final String hostname, final String collectedFqdn,
			final boolean resolveHostnameToFqdn, final Map<String, String> extraLabels) {

		// Extra Fqdn, WTF? who knows! ok let's be consistent
		// Should we resolve hostname to the overridden fqdn?
		final String extraFqdn = extraLabels.get("fqdn");
		if (resolveHostnameToFqdn && extraFqdn != null) {
			return extraFqdn;
		}

		final String extraHostname = extraLabels.get(RESOURCE_HOST_NAME_PROP);
		// Priority to extra label host.name
		if (extraHostname != null) {
			return extraHostname;
		}

		// Should we resolve hostname to the collected fqdn?
		if (resolveHostnameToFqdn) {
			return collectedFqdn;
		}

		// Finally we keep the configured target's hostname
		return hostname;
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
