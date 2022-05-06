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
	WBEMProtocols protocol = WBEMProtocols.HTTPS;

	@Default
	private Integer port = 5989;

	private String namespace;

	@Default
	private Long timeout = 120L;

	String username;

	char[] password;

	@Override
	public String toString() {
		String desc = protocol + "/" + port;
		if (username != null) {
			desc = desc + " as " + username;
		}
		return desc;
	}

	/**
	 * Represents the transport protocol for WBEM: HTTP or HTTPS
	 */
	public enum WBEMProtocols {

		HTTP,
		HTTPS;

		/**
		 * Interpret the specified name and returns corresponding value.
		 *
		 * @param label	String to be interpreted
		 *
		 * @return Corresponding {@link WBEMProtocols} value
		 */
		public static WBEMProtocols interpretValueOf(final String label) {

			if ("http".equalsIgnoreCase(label)) {
				return HTTP;
			}

			if ("https".equalsIgnoreCase(label)) {
				return HTTPS;
			}

			throw new IllegalArgumentException("Invalid protocol value: " + label);
		}

		@Override
		public String toString() {
			if (this == HTTP) {
				return "http";
			}
			return "https";
		}
	}
}
