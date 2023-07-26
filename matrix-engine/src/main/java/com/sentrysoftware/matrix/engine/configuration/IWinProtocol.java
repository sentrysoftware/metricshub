package com.sentrysoftware.matrix.engine.configuration;

public interface IWinProtocol extends IConfiguration {
	String getNamespace();

	String getUsername();

	Long getTimeout();

	char[] getPassword();
}
