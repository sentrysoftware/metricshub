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
import com.sentrysoftware.matrix.common.helpers.LocalOSHandler.Aix;
import com.sentrysoftware.matrix.common.helpers.LocalOSHandler.FreeBSD;
import com.sentrysoftware.matrix.common.helpers.LocalOSHandler.Hp;
import com.sentrysoftware.matrix.common.helpers.LocalOSHandler.ILocalOS;
import com.sentrysoftware.matrix.common.helpers.LocalOSHandler.ILocalOSVisitor;
import com.sentrysoftware.matrix.common.helpers.LocalOSHandler.Linux;
import com.sentrysoftware.matrix.common.helpers.LocalOSHandler.MacOSX;
import com.sentrysoftware.matrix.common.helpers.LocalOSHandler.NetBSD;
import com.sentrysoftware.matrix.common.helpers.LocalOSHandler.OpenBSD;
import com.sentrysoftware.matrix.common.helpers.LocalOSHandler.Solaris;
import com.sentrysoftware.matrix.common.helpers.LocalOSHandler.Sun;
import com.sentrysoftware.matrix.common.helpers.LocalOSHandler.Windows;
import com.sentrysoftware.matrix.common.helpers.StringHelper;
import com.sentrysoftware.matrix.engine.target.TargetType;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.MetricReaderFactory;
import io.opentelemetry.sdk.resources.Resource;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access =  AccessLevel.PRIVATE)
public class OtelHelper {

	private static final String RESOURCE_HOST_NAME_PROP = "host.name";

	static final String AGENT_HOSTNAME = StringHelper
			.getValue(() -> InetAddress.getLocalHost().getCanonicalHostName(), "unknown");

	private static final Map<TargetType, String> TARGET_TYPE_TO_OTEL_OS_TYPE = Map.of(
			HP_OPEN_VMS, "openvms",
			HP_TRU64_UNIX, "true64",
			HP_UX, "hpux",
			IBM_AIX, "aix",
			LINUX, "linux",
			MGMT_CARD_BLADE_ESXI, "management",
			MS_WINDOWS, "windows",
			NETWORK_SWITCH, "network",
			STORAGE, "storage",
			SUN_SOLARIS, "solaris");

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

		// The host resource type used for both host.type and os.type attributes
		final String hostType = TARGET_TYPE_TO_OTEL_OS_TYPE.getOrDefault(targetType,
				targetType.getDisplayName().toLowerCase());

		// Build attributes
		final AttributesBuilder builder = Attributes.builder()
				.put("host.id", id)
				.put(RESOURCE_HOST_NAME_PROP, hostname)
				.put("host.type", hostType)
				.put("os.type", hostType)
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
				.put("host.type", osType)
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
			final OpenTelemetryAgentOsTypeVisitor visitor = new OpenTelemetryAgentOsTypeVisitor();
			localOs.get().accept(visitor);
			final String osType = visitor.getOsType();
			if (osType != null) {
				return osType;
			}
		}
		return "unknown";
	}

	@NoArgsConstructor
	static class OpenTelemetryAgentOsTypeVisitor implements ILocalOSVisitor {

		@Getter
		private String osType;

		@Override
		public void visit(Windows os) {
			osType = "windows";
		}

		@Override
		public void visit(Linux os) {
			osType = "linux";
		}

		@Override
		public void visit(Sun os) {
			osType = "sun";
		}

		@Override
		public void visit(Hp os) {
			osType = "hpux";
		}

		@Override
		public void visit(Solaris os) {
			osType = "solaris";
		}

		@Override
		public void visit(Aix os) {
			osType = "aix";
		}

		@Override
		public void visit(FreeBSD os) {
			osType = "freebsd";
		}

		@Override
		public void visit(OpenBSD os) {
			osType = "openbsd";
		}

		@Override
		public void visit(NetBSD os) {
			osType = "netbsd";
		}

		@Override
		public void visit(MacOSX os) {
			osType = "macosx";
		}

	}
}
