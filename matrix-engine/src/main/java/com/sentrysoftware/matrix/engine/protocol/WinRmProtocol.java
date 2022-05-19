package com.sentrysoftware.matrix.engine.protocol;

import java.util.List;

import com.sentrysoftware.matsya.winrm.service.client.auth.AuthenticationEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WinRmProtocol implements IWqlProtocol {

	private String username;
	private char[] password;
	private String namespace;
	@Default
	private Integer port = 5985;
	@Default
	private boolean https = false;
	private List<AuthenticationEnum> authentications;
	@Default
	private Long timeout = 120L;

	@Override
	public String toString() {
		String desc = "WinRM";
		if (username != null) {
			desc = desc + " as " + username;
		}
		return desc;
	}
}
