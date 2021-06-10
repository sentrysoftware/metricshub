package com.sentrysoftware.matrix.engine.protocol;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SNMPProtocol implements IProtocolConfiguration {

	@Default
	private SNMPVersion version = SNMPVersion.V1;
	@Default
	private String community = "public";
	@Default
	private Integer port = 161;
	@Default
	private Long timeout = 120L;
	private Privacy privacy;
	private String privacyPassword;
	private String username;
	private String password;

	@AllArgsConstructor
	public enum SNMPVersion {
		V1(1, null), V2C(2, null), V3_NO_AUTH(3, null), V3_MD5(3, "MD5"), V3_SHA(3, "SHA");
		@Getter
		private int intVersion;
		@Getter
		private String authType;
	}

	public enum Privacy {
		NO_ENCRYPTION, AES, DES;
	}

}
