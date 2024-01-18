package com.sentrysoftware.metricshub.agent.config.protocols;

import static com.fasterxml.jackson.annotation.Nulls.SKIP;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.metricshub.agent.deserialization.TimeDeserializer;
import com.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import com.sentrysoftware.metricshub.engine.configuration.TransportProtocols;
import com.sentrysoftware.metricshub.engine.configuration.WinRmConfiguration;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.sentrysoftware.winrm.service.client.auth.AuthenticationEnum;

/**
 * Configuration class for the WinRm (Windows Remote Management) protocol.
 * Extends {@link AbstractProtocolConfig}.
 */
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
	public IConfiguration toConfiguration() {
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
