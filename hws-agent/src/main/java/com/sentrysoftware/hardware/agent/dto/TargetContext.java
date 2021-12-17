package com.sentrysoftware.hardware.agent.dto;

public class TargetContext {

	private TargetContext() {}

	private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();

	public static void setTargetId(String targetId) {
		CONTEXT.set(targetId);
	}

	public static String getTargetId() {
		return CONTEXT.get();
	}

	public static void clear() {
		CONTEXT.remove();
	}
}