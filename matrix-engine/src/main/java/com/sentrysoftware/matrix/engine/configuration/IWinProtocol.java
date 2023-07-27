package com.sentrysoftware.matrix.engine.configuration;

/**
 * This interface manages Windows protocols
 */
public interface IWinProtocol extends IConfiguration {
	String getNamespace();

	String getUsername();

	Long getTimeout();

	char[] getPassword();
}
