package com.sentrysoftware.hardware.agent.service.opentelemetry;

import static com.sentrysoftware.matrix.engine.target.TargetType.HP_OPEN_VMS;
import static com.sentrysoftware.matrix.engine.target.TargetType.HP_TRU64_UNIX;
import static com.sentrysoftware.matrix.engine.target.TargetType.HP_UX;
import static com.sentrysoftware.matrix.engine.target.TargetType.IBM_AIX;
import static com.sentrysoftware.matrix.engine.target.TargetType.LINUX;
import static com.sentrysoftware.matrix.engine.target.TargetType.MGMT_CARD_BLADE_ESXI;
import static com.sentrysoftware.matrix.engine.target.TargetType.MS_WINDOWS;
import static com.sentrysoftware.matrix.engine.target.TargetType.NETWORK_SWITCH;
import static com.sentrysoftware.matrix.engine.target.TargetType.STORAGE;
import static com.sentrysoftware.matrix.engine.target.TargetType.SUN_SOLARIS;

import java.net.InetAddress;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.sentrysoftware.matrix.common.helpers.LocalOSHandler;
import com.sentrysoftware.matrix.common.helpers.LocalOSHandler.ILocalOS;
import com.sentrysoftware.matrix.common.helpers.StringHelper;
import com.sentrysoftware.matrix.engine.target.TargetType;

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

	// OS Types
	static final String OS_TYPE_STORAGE = "storage";
	static final String OS_TYPE_NETWORK = "network";
	static final String OS_TYPE_SOLARIS = "solaris";
	static final String OS_TYPE_WINDOWS = "windows";
	static final String OS_TYPE_MANAGEMENT = "management";
	static final String OS_TYPE_LINUX = "linux";
	static final String OS_TYPE_AIX = "aix";
	static final String OS_TYPE_HP_UX = "hpux";
	static final String OS_TYPE_TRUE64 = "true64";
	static final String OS_TYPE_OPEN_VMS = "openvms";
	static final String OS_TYPE_MAC_OS_X = "macosx";
	static final String OS_TYPE_OPEN_BSD = "openbsd";
	static final String OS_TYPE_NET_BSD = "netbsd";
	static final String OS_TYPE_FREE_BSD = "freebsd";
	static final String OS_TYPE_SUN = "sun";

	// Host Types
	static final String HOST_TYPE_STORAGE = "storage";
	static final String HOST_TYPE_NETWORK = "network";
	static final String HOST_TYPE_COMPUTE = "compute";

	static final String UNKNOWN = "unknown";

	static final String RESOURCE_HOST_NAME_PROP = "host.name";

	static final String AGENT_HOSTNAME = StringHelper
			.getValue(() -> InetAddress.getLocalHost().getCanonicalHostName(), UNKNOWN);

	static final Map<TargetType, String> TARGET_TYPE_TO_OTEL_OS_TYPE = Map.of(
			HP_OPEN_VMS, OS_TYPE_OPEN_VMS,
			HP_TRU64_UNIX, OS_TYPE_TRUE64,
			HP_UX, OS_TYPE_HP_UX,
			IBM_AIX, OS_TYPE_AIX,
			LINUX, OS_TYPE_LINUX,
			MGMT_CARD_BLADE_ESXI, OS_TYPE_MANAGEMENT,
			MS_WINDOWS, OS_TYPE_WINDOWS,
			NETWORK_SWITCH, OS_TYPE_NETWORK,
			STORAGE, OS_TYPE_STORAGE,
			SUN_SOLARIS, OS_TYPE_SOLARIS);

	static final Map<TargetType, String> TARGET_TYPE_TO_OTEL_HOST_TYPE = Map.of(
			HP_OPEN_VMS, HOST_TYPE_COMPUTE,
			HP_TRU64_UNIX, HOST_TYPE_COMPUTE,
			HP_UX, HOST_TYPE_COMPUTE,
			IBM_AIX, HOST_TYPE_COMPUTE,
			LINUX, HOST_TYPE_COMPUTE,
			MGMT_CARD_BLADE_ESXI, HOST_TYPE_COMPUTE,
			MS_WINDOWS, HOST_TYPE_COMPUTE,
			NETWORK_SWITCH, HOST_TYPE_NETWORK,
			STORAGE, HOST_TYPE_STORAGE,
			SUN_SOLARIS, HOST_TYPE_COMPUTE);

	static final Map<ILocalOS, String> LOCAL_OS_TO_OTEL_OS_TYPE = Map.of(
			LocalOSHandler.WINDOWS, OS_TYPE_WINDOWS,
			LocalOSHandler.LINUX, OS_TYPE_LINUX,
			LocalOSHandler.SUN, OS_TYPE_SUN,
			LocalOSHandler.HP, OS_TYPE_HP_UX,
			LocalOSHandler.SOLARIS, OS_TYPE_SOLARIS,
			LocalOSHandler.FREE_BSD, OS_TYPE_FREE_BSD,
			LocalOSHandler.NET_BSD, OS_TYPE_NET_BSD,
			LocalOSHandler.OPEN_BSD, OS_TYPE_OPEN_BSD,
			LocalOSHandler.MAC_OS_X, OS_TYPE_MAC_OS_X,
			LocalOSHandler.AIX, OS_TYPE_AIX);

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
	 * @param targetType            Target type
	 * @param fqdn                  Collected fqdn
	 * @param resolveHostnameToFqdn Whether we should resolve the hostname to Fqdn
	 * @param extraLabels           Extra labels configured on the target
	 * @param globalExtraLabels     Global configured extra labels
	 * @return Resource capturing identifying information about the target for which
	 *         signals are reported.
	 */
	public static Resource createHostResource(
			@NonNull final String id,
			@NonNull String hostname,
			@NonNull final TargetType targetType,
			@NonNull final String fqdn,
			final boolean resolveHostnameToFqdn,
			@NonNull final Map<String, String> extraLabels,
			@NonNull final Map<String, String> globalExtraLabels) {

		// Which hostname?
		hostname = getResourceHostname(
				hostname,
				fqdn,
				resolveHostnameToFqdn,
				extraLabels
		);

		// The host resource os.type
		final String osType = TARGET_TYPE_TO_OTEL_OS_TYPE.getOrDefault(targetType,
				targetType.getDisplayName().toLowerCase());

		// The host resource host.type
		final String hostType = TARGET_TYPE_TO_OTEL_HOST_TYPE.getOrDefault(targetType,
				targetType.getDisplayName().toLowerCase());

		// Build attributes
		final AttributesBuilder builder = Attributes.builder()
				.put("host.id", id)
				.put(RESOURCE_HOST_NAME_PROP, hostname)
				.put("host.type", hostType)
				.put("os.type", osType)
				.put("agent.host.name", AGENT_HOSTNAME)
				.put("fqdn", fqdn);

		// Global extra attributes? Ok let's override them here
		globalExtraLabels
			.keySet()
			.stream()
			.filter(key -> !RESOURCE_HOST_NAME_PROP.equals(key)) // host.name has a special handling
			.filter(key -> Objects.nonNull(globalExtraLabels.get(key)))
			.forEach(key -> builder.put(key, globalExtraLabels.get(key)));

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
	 * @param globalExtraLabels Configured global extra labels
	 * @return Resource capturing identifying information about the service
	 */
	public static Resource createServiceResource(@NonNull final String serviceName,
			@NonNull final Map<String, String> globalExtraLabels) {

		final String osType = getAgentOsType();
		final AttributesBuilder builder = Attributes.builder()
				.put("service.name", serviceName)
				.put("host.id", AGENT_HOSTNAME)
				.put(RESOURCE_HOST_NAME_PROP, AGENT_HOSTNAME)
				.put("agent.host.name", AGENT_HOSTNAME)
				.put("host.type", HOST_TYPE_COMPUTE)
				.put("os.type", osType);

		// Extra attributes? put them all!
		globalExtraLabels
			.keySet()
			.stream()
			.filter(key -> Objects.nonNull(globalExtraLabels.get(key)))
			.forEach(key -> builder.put(key, globalExtraLabels.get(key)));

		return Resource.create(builder.build());

	}

	/**
	 * Get the detected OS then return the value as specified by OpenTelemetry
	 * 
	 * @return String value
	 */
	static String getAgentOsType() {
		final Optional<ILocalOS> localOs = LocalOSHandler.getOS();
		if (localOs.isPresent()) {
			return LOCAL_OS_TO_OTEL_OS_TYPE.getOrDefault(localOs.get(), UNKNOWN);
		}
		return UNKNOWN;
	}

}
