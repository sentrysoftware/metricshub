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
public class WBEMProtocol implements IProtocolConfiguration {

	WBEMProtocols protocol;

	@Default
	private Integer port = 5989;

	@Default
	private String namespace = "root/cimv2";

	@Default
	private Long timeout = 120L;

	String username;

	char[] password;

	public enum WBEMProtocols {
		HTTP, HTTPS;

		/**
		 * Convert to upper case in order to manage case sensitivity
		 * @param label
		 * @return
		 */
		public static WBEMProtocols getValue(final String label) {
			return WBEMProtocols.valueOf(label.toUpperCase());
		}
	}
}
