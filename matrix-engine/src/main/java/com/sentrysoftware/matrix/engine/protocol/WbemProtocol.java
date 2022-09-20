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
public class WbemProtocol implements IProtocolConfiguration {

	@Default
	TransportProtocols protocol = TransportProtocols.HTTPS;

	@Default
	private Integer port = 5989;

	private String namespace;

	@Default
	private Long timeout = 120L;

	String username;

	char[] password;
	
	String vCenter;

	@Override
	public String toString() {
		String desc = protocol + "/" + port;
		if (username != null) {
			desc = desc + " as " + username;
		}
		return desc;
	}
}
