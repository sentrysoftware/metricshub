package com.sentrysoftware.hardware.agent.dto;

public class HostContext {

	private HostContext() {}

	private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();

	public static void setHostId(String hostId) {
		CONTEXT.set(hostId);
	}

	public static String getHostId() {
		return CONTEXT.get();
	}

	public static void clear() {
		CONTEXT.remove();
	}
}