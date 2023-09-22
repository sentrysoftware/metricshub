package com.sentrysoftware.matrix.agent.config.protocols;

import static com.fasterxml.jackson.annotation.Nulls.SKIP;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.matrix.agent.deserialization.TimeDeserializer;
import com.sentrysoftware.matrix.configuration.IConfiguration;
import com.sentrysoftware.matrix.configuration.TransportProtocols;
import com.sentrysoftware.matrix.configuration.WinRmConfiguration;
import com.sentrysoftware.matsya.winrm.service.client.auth.AuthenticationEnum;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class WinRmProtocolConfig extends AbstractProtocolConfig {

	private String username;

	private char[] password;

	private String namespace;

	@Default
	private Integer port = 5985;

	@Default
	@JsonSetter(nulls = SKIP)
	private TransportProtocols protocol = TransportProtocols.HTTP;

	private List<AuthenticationEnum> authentications;

	@Default
	@JsonDeserialize(using = TimeDeserializer.class)
	private Long timeout = 120L;

	/**
	 * Create a new {@link WinRmConfiguration} instance based on the current members
	 *
	 * @return The {@link WinRmConfiguration} instance
	 */
	@Override
	public IConfiguration toConfigurartion() {
		return WinRmConfiguration
			.builder()
			.username(username)
			.password(super.decrypt(password))
			.namespace(namespace)
			.port(port)
			.protocol(protocol)
			.authentications(authentications)
			.timeout(timeout)
			.build();
	}

	@Override
	public String toString() {
		String desc = "WinRM";
		if (username != null) {
			desc = desc + " as " + username;
		}
		return desc;
	}
}
