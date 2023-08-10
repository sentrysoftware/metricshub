package com.sentrysoftware.matrix.configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HttpConfiguration implements IConfiguration {

	@Builder.Default
	private final Boolean https = true;

	@Builder.Default
	private final Integer port = 443;

	@Builder.Default
	private final Long timeout = 120L;

	private String username;
	private char[] password;

	@Override
	public String toString() {
		return String.format(
				"%s/%d%s",
				https ? "HTTPS" : "HTTP",
				port,
				username != null ? " as " + username : ""
		);
	}
}