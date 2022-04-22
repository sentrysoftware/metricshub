package com.sentrysoftware.matrix.engine.protocol;

import java.nio.file.Path;
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
public class WinRMProtocol implements IProtocolConfiguration {
	
	private String username;
	private char[] password;
	private String namespace;
	private String command;
	private String workingDirectory;
	private Integer port;
	private String protocol; // HTTP or HTTPS
	private Path ticketCache;
	private List<AuthenticationEnum> authentications;
	@Default
	private Long timeout = 120L;
	private List<String> localFileToCopyList;

	@Override
	public String toString() {
		String desc = "WinRM";
		if (username != null) {
			desc = desc + " as " + username;
		}
		return desc;
	}
}
