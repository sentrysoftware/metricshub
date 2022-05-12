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
public class WmiProtocol implements IProtocolConfiguration {

	private String username;
	private char[] password;
	private String namespace;
	@Default
	private Long timeout = 120L;

	@Override
	public String toString() {
		String desc = "WMI";
		if (username != null) {
			desc = desc + " as " + username;
		}
		return desc;
	}
}
