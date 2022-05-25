package com.sentrysoftware.hardware.agent.service.opentelemetry;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.EMPTY;
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

import com.sentrysoftware.hardware.agent.dto.metric.DynamicIdentifyingAttribute;
import com.sentrysoftware.hardware.agent.dto.metric.IIdentifyingAttribute;
import com.sentrysoftware.hardware.agent.dto.metric.MetricInfo;
import com.sentrysoftware.hardware.agent.dto.metric.StaticIdentifyingAttribute;
import com.sentrysoftware.matrix.common.helpers.LocalOsHandler;
import com.sentrysoftware.matrix.common.helpers.LocalOsHandler.ILocalOs;
import com.sentrysoftware.matrix.common.meta.parameter.state.IState;
import com.sentrysoftware.matrix.common.helpers.StringHelper;
import com.sentrysoftware.matrix.engine.target.TargetType;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.parameter.DiscreteParam;
import com.sentrysoftware.matrix.model.parameter.IParameter;
import com.sentrysoftware.matrix.model.parameter.NumberParam;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdkBuilder;
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

	static final Map<ILocalOs, String> LOCAL_OS_TO_OTEL_OS_TYPE = Map.of(
			LocalOsHandler.WINDOWS, OS_TYPE_WINDOWS,
			LocalOsHandler.LINUX, OS_TYPE_LINUX,
			LocalOsHandler.SUN, OS_TYPE_SUN,
			LocalOsHandler.HP, OS_TYPE_HP_UX,
			LocalOsHandler.SOLARIS, OS_TYPE_SOLARIS,
			LocalOsHandler.FREE_BSD, OS_TYPE_FREE_BSD,
			LocalOsHandler.NET_BSD, OS_TYPE_NET_BSD,
			LocalOsHandler.OPEN_BSD, OS_TYPE_OPEN_BSD,
			LocalOsHandler.MAC_OS_X, OS_TYPE_MAC_OS_X,
			LocalOsHandler.AIX, OS_TYPE_AIX);

	/**
	 * Initializes an OpenTelemetry SDK with a Resource and an instance of
	 * IntervalMetricReader.
	 *
	 * @param resource             the resource used for the SdkMeterProvider
	 * @param otelSdkConfiguration configuration for the OpenTelemetry SDK.
	 * @return a ready-to-use {@link AutoConfiguredOpenTelemetrySdk} instance
	 */
	public static AutoConfiguredOpenTelemetrySdk initOpenTelemetrySdk(@NonNull final Resource resource,
			@NonNull final Map<String, String> otelSdkConfiguration) {

		final AutoConfiguredOpenTelemetrySdkBuilder sdkBuilder = AutoConfiguredOpenTelemetrySdk.builder();

		// Properties
		sdkBuilder.addPropertiesSupplier(() -> otelSdkConfiguration);

		// Resource
		sdkBuilder.addResourceCustomizer((r, c) -> resource);

		// Control the registration of a shutdown hook to shut down the SDK when
		// appropriate. By default, the shutdown hook is registered.
		sdkBuilder.registerShutdownHook(false);

		// We are not instrumenting our code execution and because this method is called
		// for each target, we must disable the global result
		sdkBuilder.setResultAsGlobal(false);

		return sdkBuilder.build();
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
				.put("agent.host.name", AGENT_HOSTNAME);

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
		final Optional<ILocalOs> localOs = LocalOsHandler.getOs();
		if (localOs.isPresent()) {
			return LOCAL_OS_TO_OTEL_OS_TYPE.getOrDefault(localOs.get(), UNKNOWN);
		}
		return UNKNOWN;
	}

	/**
	 * Extract the identifying attribute from the given {@link MetricInfo}. The
	 * identifying attribute is a key value pair which could be static or dynamic
	 * i.e. fetched from the monitor's metadata.
	 * 
	 * @param metricInfo The metric information
	 * @param monitor    The monitor used to fetch the attribute value
	 * @return {@link Optional} of {@link String} array including the key at the
	 *         first position and the value in the second one.
	 */
	public static Optional<String[]> extractIdentifyingAttribute(final MetricInfo metricInfo, final Monitor monitor) {
		final IIdentifyingAttribute identifyingAttribute = metricInfo.getIdentifyingAttribute();
		if (identifyingAttribute != null) {
			// Simple key-value
			if (identifyingAttribute instanceof StaticIdentifyingAttribute) {
				return Optional.of(new String[] { identifyingAttribute.getKey(), identifyingAttribute.getValue() });
			} else if (identifyingAttribute instanceof DynamicIdentifyingAttribute) {
				// The value is dynamic extracted from the metadata collection
				return Optional.of(new String[] { identifyingAttribute.getKey(),
						StringHelper.getValue(() -> monitor.getMetadata(identifyingAttribute.getValue()).trim().toLowerCase(), EMPTY) });
			} else {
				throw new IllegalStateException("Unhandled identifying attribute: " + identifyingAttribute.getClass().getSimpleName());
			}
		}

		return Optional.empty();
	}

	/**
	 * Get the parameter from the monitor instance then if this parameter is a
	 * {@link DiscreteParam} then apply the {@link MetricInfo} predicate to decide
	 * if we should return 1 (true) or 0 (false).<br> If we deal with a
	 * {@link NumberParam} then simply return the parameter's value converted using
	 * the metric information factor.
	 *
	 * @param metricInfo    The metric information
	 * @param monitor       The monitor from which we extract the parameter value
	 * @param parameterName The parameter name we want to extract from the given
	 *                      monitor instance
	 * @return {@link Number} value
	 */
	public static double getMetricValue(@NonNull final MetricInfo metricInfo, @NonNull final Monitor monitor, @NonNull  final String parameterName) {

		// Extract the parameter from this monitor
		final IParameter parameter = monitor.getParameters().get(parameterName);

		if (parameter instanceof DiscreteParam && metricInfo.isBooleanMetric()) {
			final IState state = ((DiscreteParam) parameter).getState();
			return metricInfo.getPredicate().test(state) ? 1 : 0;
		}

		// Return the number value for other parameters
		return parameter.numberValue().doubleValue() * metricInfo.getFactor();
	}
}
