package com.sentrysoftware.matrix.engine.protocol;

public interface IWqlProtocol extends IProtocolConfiguration {
	String getNamespace();
	String getUsername();
	Long getTimeout();
}
