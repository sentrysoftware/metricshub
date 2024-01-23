package org.sentrysoftware.metricshub.engine.configuration;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sentrysoftware.winrm.service.client.auth.AuthenticationEnum;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WinRmConfiguration implements IWinConfiguration {

	private String username;
	private char[] password;
	private String namespace;

	@Builder.Default
	private final Integer port = 5985;

	@Builder.Default
	private final TransportProtocols protocol = TransportProtocols.HTTP;

	private List<AuthenticationEnum> authentications;

	@Builder.Default
	private final Long timeout = 120L;

	@Override
	public String toString() {
		String description = "WinRM";
		if (username != null) {
			description = description + " as " + username;
		}
		return description;
	}
}
