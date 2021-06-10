package com.sentrysoftware.matrix.engine.protocol;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WMIProtocol implements IProtocolConfiguration {

	private String username;
	private char[] password;
	@Default
	private String namespace = "root/cimv2";
	@Default
	private Long timeout = 120L;
}
