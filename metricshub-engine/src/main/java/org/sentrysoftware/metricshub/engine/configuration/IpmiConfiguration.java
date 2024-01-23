package org.sentrysoftware.metricshub.engine.configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IpmiConfiguration implements IConfiguration {

	@Builder.Default
	private final Long timeout = 120L;

	private String username;
	private char[] password;
	private byte[] bmcKey;
	private boolean skipAuth;

	@Override
	public String toString() {
		String description = "IPMI";
		if (username != null) {
			description = description + " as " + username;
		}
		return description;
	}
}
