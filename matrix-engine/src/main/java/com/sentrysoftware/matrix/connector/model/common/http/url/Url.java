package com.sentrysoftware.matrix.connector.model.common.http.url;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.HOSTNAME_MACRO;

import java.io.Serializable;

import lombok.NonNull;

public class Url implements Serializable {

	private static final long serialVersionUID = -1498191184731181688L;

	/**
	 * Generate the full URL
	 * 
	 * @param hostname 	hostname of the target
	 * @param port		port of the request
	 * @param url 		url of the request
	 * @param protocol	protocol used by the request
	 */
	public static String getContent(@NonNull final String hostname, @NonNull final Integer port, @NonNull final String url, @NonNull final String protocol) {

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