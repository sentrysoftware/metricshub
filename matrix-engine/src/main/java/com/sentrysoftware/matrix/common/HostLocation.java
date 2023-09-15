package com.sentrysoftware.matrix.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum HostLocation {
	LOCAL("local"),
	REMOTE("remote");

	private String key;
}
