package com.sentrysoftware.matrix.agent.config.protocols;

import static com.fasterxml.jackson.annotation.Nulls.SKIP;

import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProtocolsConfig {

	@JsonSetter(nulls = SKIP)
	private SnmpProtocolConfig snmp;

	@JsonSetter(nulls = SKIP)
	private IpmiProtocolConfig ipmi;

	@JsonSetter(nulls = SKIP)
	private SshProtocolConfig ssh;

	@JsonSetter(nulls = SKIP)
	private WbemProtocolConfig wbem;

	@JsonSetter(nulls = SKIP)
	private WmiProtocolConfig wmi;

	@JsonSetter(nulls = SKIP)
	private HttpProtocolConfig http;

	@JsonSetter(nulls = SKIP)
	private OsCommandProtocolConfig osCommand;

	@JsonSetter(nulls = SKIP)
	private WinRmProtocolConfig winRm;
}
