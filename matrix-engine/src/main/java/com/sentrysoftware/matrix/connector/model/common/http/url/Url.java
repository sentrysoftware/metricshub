package com.sentrysoftware.matrix.connector.model.common.http.url;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.HOSTNAME_MACRO;

public class Url {

	/**
	 * Generate the full URL
	 * 
	 * @param hostname 	hostname of the target
	 * @param port		port of the request
	 * @param url 		url of the request
	 * @param protocol	protocol used by the request
	 */
	public static String getContent(String hostname, int port, String url, String protocol) {

		String fullUrl = String.format(
				"%s://%s:%d%s%s",
				protocol,
				hostname,
				port,
				url.startsWith("/") ? "" : "/",
				url
		);

		return fullUrl.replace(HOSTNAME_MACRO, hostname);
	}
}