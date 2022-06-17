package com.sentrysoftware.matrix.engine.protocol;

public interface IWinProtocol extends IProtocolConfiguration {
	String getNamespace();
	String getUsername();
	Long getTimeout();
	char[] getPassword();
}
