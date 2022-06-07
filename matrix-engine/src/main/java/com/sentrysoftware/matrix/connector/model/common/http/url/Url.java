package com.sentrysoftware.matrix.connector.model.common.http.url;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.HOSTNAME_MACRO;

import lombok.NonNull;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access =  AccessLevel.PRIVATE)
public class Url {

	/**
	 * Generate the full URL
	 * 
	 * @param hostname    hostname
	 * @param port        port of the request
	 * @param requestPath request path of the request
	 * @param protocol    protocol used by the request
	 */
	public static String format(@NonNull final String hostname, @NonNull final Integer port, @NonNull final String requestPath, @NonNull final String protocol) {

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