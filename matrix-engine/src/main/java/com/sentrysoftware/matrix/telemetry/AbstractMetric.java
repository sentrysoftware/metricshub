package com.sentrysoftware.matrix.telemetry;

import lombok.Data;

import java.util.Map;

@Data
public abstract class AbstractMetric {

	private String name;
	private long collectTime;
	private long previousCollectTime;
	private Map<String, String> attributes;
}
