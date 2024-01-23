package org.sentrysoftware.metricshub.engine.client.http;

import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.HOSTNAME_MACRO;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Url {

	/**
	 * Generate the full URL
	 *
	 * @param hostname    hostname
	 * @param port        port of the request
	 * @param requestPath request path of the request
	 * @param protocol    protocol used by the request
	 * @return formatted String
	 */
	public static String format(
		@NonNull final String hostname,
		@NonNull final Integer port,
		@NonNull final String requestPath,
		@NonNull final String protocol
	) {
		String fullUrl = String.format(
			"%s://%s:%d%s%s",
			protocol,
			hostname,
			port,
			requestPath.startsWith("/") ? "" : "/",
			requestPath
		);

		return fullUrl.replace(HOSTNAME_MACRO, hostname);
	}
}
