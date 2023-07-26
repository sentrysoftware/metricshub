package com.sentrysoftware.matrix.engine.configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WbemConfiguration implements IConfiguration {

	@Builder.Default
	private final TransportProtocols protocol = TransportProtocols.HTTPS;

	@Builder.Default
	private final Integer port = 5989;

	private String namespace;

	@Builder.Default
	private final Long timeout = 120L;

	String username;

	char[] password;

	String vCenter;

	@Override
	public String toString() {
		String description = protocol + "/" + port;
		if (username != null) {
			description = description + " as " + username;
		}
		return description;
	}
}
