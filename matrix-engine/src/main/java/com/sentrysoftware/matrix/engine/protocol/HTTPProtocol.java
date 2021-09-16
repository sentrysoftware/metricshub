package com.sentrysoftware.matrix.engine.protocol;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HTTPProtocol implements IProtocolConfiguration {

	@Default
	private Boolean https = true;

	@Default
	private Integer port = 443;

	@Default
	private Long timeout = 120L;

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
