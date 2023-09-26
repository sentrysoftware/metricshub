package com.sentrysoftware.matrix.agent.context;

import static com.sentrysoftware.matrix.agent.helper.AgentConstants.AGENT_INFO_BUILD_DATE_NUMBER_ATTRIBUTE_KEY;
import static com.sentrysoftware.matrix.agent.helper.AgentConstants.AGENT_INFO_BUILD_NUMBER_ATTRIBUTE_KEY;
import static com.sentrysoftware.matrix.agent.helper.AgentConstants.AGENT_INFO_HC_VERSION_NUMBER_ATTRIBUTE_KEY;
import static com.sentrysoftware.matrix.agent.helper.AgentConstants.AGENT_INFO_NAME_ATTRIBUTE_KEY;
import static com.sentrysoftware.matrix.agent.helper.AgentConstants.AGENT_INFO_OTEL_VERSION_NUMBER_ATTRIBUTE_KEY;
import static com.sentrysoftware.matrix.agent.helper.AgentConstants.AGENT_INFO_VERSION_ATTRIBUTE_KEY;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.HOST_NAME;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.OTEL_AIX_OS_TYPE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.OTEL_FREE_BSD_OS_TYPE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.OTEL_HPUX_OS_TYPE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.OTEL_LINUX_OS_TYPE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.OTEL_MAC_OS_X_OS_TYPE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.OTEL_NET_BSD_OS_TYPE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.OTEL_OPEN_BSD_OS_TYPE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.OTEL_SOLARIS_OS_TYPE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.OTEL_SUN_OS_TYPE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.OTEL_WINDOWS_OS_TYPE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentrysoftware.matrix.agent.context.ApplicationProperties.Project;
import com.sentrysoftware.matrix.agent.helper.ConfigHelper;
import com.sentrysoftware.matrix.common.helpers.JsonHelper;
import com.sentrysoftware.matrix.common.helpers.LocalOsHandler;
import com.sentrysoftware.matrix.common.helpers.LocalOsHandler.ILocalOs;
import com.sentrysoftware.matrix.common.helpers.StringHelper;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

@Slf4j
public class AgentInfo {

	public static final ObjectMapper OBJECT_MAPPER = ConfigHelper.newObjectMapper();

	public static final String METRICS_HUB_AGENT_METRIC_NAME = "metricshub.agent.info";

	private static final String APPLICATION_YAML_FILE_NAME = "application.yaml";

	// @formatter:off
	// Map of local OS type that can be detected by the engine to the OTEL OS type
	private static final Map<ILocalOs, String> LOCAL_OS_TO_OTEL_OS_TYPE = Map.of(
		LocalOsHandler.WINDOWS, OTEL_WINDOWS_OS_TYPE,
		LocalOsHandler.LINUX, OTEL_LINUX_OS_TYPE,
		LocalOsHandler.SUN, OTEL_SUN_OS_TYPE,
		LocalOsHandler.HP, OTEL_HPUX_OS_TYPE,
		LocalOsHandler.SOLARIS, OTEL_SOLARIS_OS_TYPE,
		LocalOsHandler.FREE_BSD, OTEL_FREE_BSD_OS_TYPE,
		LocalOsHandler.NET_BSD, OTEL_NET_BSD_OS_TYPE,
		LocalOsHandler.OPEN_BSD, OTEL_OPEN_BSD_OS_TYPE,
		LocalOsHandler.MAC_OS_X, OTEL_MAC_OS_X_OS_TYPE,
		LocalOsHandler.AIX, OTEL_AIX_OS_TYPE
	);
	// @formatter:on

	private static final String UNKNOWN = "unknown";

	private static final String AGENT_HOSTNAME = StringHelper.getValue(
		() -> InetAddress.getLocalHost().getCanonicalHostName(),
		UNKNOWN
	);

	private static final String OTEL_LOCAL_OS_TYPE = StringHelper.getValue(AgentInfo::getLocalOsTypeForOtel, UNKNOWN);

	@Getter
	private ApplicationProperties applicationProperties;

	@Getter
	private Map<String, String> metricAttributes = new HashMap<>();

	@Getter
	private Map<String, String> resourceAttributes = new HashMap<>();

	public AgentInfo() {
		// Read the application.yaml file
		final ClassPathResource classPathResource = new ClassPathResource(APPLICATION_YAML_FILE_NAME);
		try {
			applicationProperties =
				JsonHelper.deserialize(OBJECT_MAPPER, classPathResource.getInputStream(), ApplicationProperties.class);
		} catch (IOException e) {
			log.error("Cannot read internal application configuration file: {}", classPathResource.getPath());
			log.debug("Exception: ", e);
			throw new IllegalStateException("Cannot read application.yaml file.", e);
		}

		final Project project = applicationProperties.project();

		// Update metric attributes
		// @formatter:off
		metricAttributes =
			Map.of(
				AGENT_INFO_NAME_ATTRIBUTE_KEY, project.name(),
				AGENT_INFO_VERSION_ATTRIBUTE_KEY, project.version(),
				AGENT_INFO_BUILD_NUMBER_ATTRIBUTE_KEY, applicationProperties.buildNumber(),
				AGENT_INFO_BUILD_DATE_NUMBER_ATTRIBUTE_KEY, applicationProperties.buildDate(),
				AGENT_INFO_HC_VERSION_NUMBER_ATTRIBUTE_KEY, applicationProperties.hcVersion(),
				AGENT_INFO_OTEL_VERSION_NUMBER_ATTRIBUTE_KEY, applicationProperties.otelVersion()
			);
		// @formatter:on

		// Update resource attributes
		// @formatter:off
		resourceAttributes =
			Map.of(
				"service.name", project.name(),
				"host.id", AGENT_HOSTNAME,
				HOST_NAME, AGENT_HOSTNAME,
				"agent.host.name", AGENT_HOSTNAME,
				"host.type", "compute",
				"os.type", OTEL_LOCAL_OS_TYPE
			);
		// @formatter:on
	}

	/**
	 * Get the detected OS then return the value as specified by OpenTelemetry
	 *
	 * @return String value
	 */
	private static String getLocalOsTypeForOtel() {
		final Optional<ILocalOs> localOs = LocalOsHandler.getOS();
		if (localOs.isPresent()) {
			return LOCAL_OS_TO_OTEL_OS_TYPE.getOrDefault(localOs.get(), UNKNOWN);
		}
		return UNKNOWN;
	}
}
