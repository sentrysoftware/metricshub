package com.sentrysoftware.hardware.cli.component.cli.protocols;

import com.sentrysoftware.matrix.engine.protocol.IProtocolConfiguration;

public interface IProtocolConfig {
	public IProtocolConfiguration toProtocol(String defaultUsername, char[] defaultPassword);
}
