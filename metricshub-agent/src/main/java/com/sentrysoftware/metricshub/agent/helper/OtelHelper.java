package com.sentrysoftware.metricshub.agent.helper;

import static com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.HOST_NAME;

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
	 * Fully Qualified Domain Name (FQDN) attribute key
	 */
	public static final String FQDN_ATTRIBUTE_KEY = "fqdn";

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
	 *
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
	 * Create the host resource based on the given attributes
	 *
	 * @param hostAttributes        Host attributes: host.id, host.name, os.type, host.type, etc
	 *                              collected by the engine
	 * @param userAttributes        User configured attributes
	 * @param resolveHostnameToFqdn Whether we must resolve the hostname of the host to a
	 *                              Fully Qualified Domain Name (FQDN)
	 * @return OTEL {@link Resource} instance
	 */
	public static Resource createHostResource(
		@NonNull final Map<String, String> hostAttributes,
		@NonNull final Map<String, String> userAttributes,
		final boolean resolveHostnameToFqdn
	) {
		// Get the resource host.name attribute value
		final String hostname = resolveResourceHostname(
			hostAttributes.get(HOST_NAME),
			userAttributes.get(HOST_NAME),
			resolveHostnameToFqdn,
			userAttributes.get(FQDN_ATTRIBUTE_KEY)
		);

		// Prepare the resource attributes
		final Map<String, String> attributes = new HashMap<>();
		attributes.putAll(hostAttributes);
		attributes.put(HOST_NAME, hostname);

		// Add user attributes
		userAttributes.entrySet().stream().forEach(entry -> attributes.putIfAbsent(entry.getKey(), entry.getValue()));

		return createOpenTelemetryResource(attributes);
	}

	/**
	 * Resolve the resource hostname.
	 *
	 * Priority
	 * <ol>
	 *   <li>User's attribute <code>fqdn</code> with <code>resolveHostnameToFqdn=true</code></li>
	 *   <li>Collected <code>fqdn</code> when the <code>resolveHostnameToFqdn=true</code></li>
	 *   <li>Configured host name attribute <code>host.name</code></li>
	 * </ol>
	 *
	 * @param collectedFqdn         Collected fqdn
	 * @param configuredHostname    Configured host's host.name
	 * @param resolveHostnameToFqdn Whether we must resolve host.name value to a fully qualified domain name.
	 * @param userFqdn              FQDN attribute value
	 * @return String value
	 */
	public static String resolveResourceHostname(
		final String collectedFqdn,
		final String configuredHostname,
		final boolean resolveHostnameToFqdn,
		final String userFqdn
	) {
		if (resolveHostnameToFqdn) {
			// User's Fqdn?
			if (userFqdn != null) {
				return userFqdn;
			} else {
				// Collected Fqdn
				return collectedFqdn;
			}
		}

		// Finally we keep the configured host.name
		return configuredHostname;
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
