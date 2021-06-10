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
	private Boolean https = false;

	@Default
	private Integer port = 8080;

	@Default
	private Long timeout = 120L;

	private String username;
	private char[] password;
}
