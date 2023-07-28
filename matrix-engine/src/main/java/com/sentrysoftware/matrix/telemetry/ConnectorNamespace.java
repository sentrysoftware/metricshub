package com.sentrysoftware.matrix.telemetry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConnectorNamespace {

	private Map<String, SourceTable> sourceTables;
	private String automaticWmiNamespace;
	private String automaticWbemNamespace;
	private ReentrantLock forceSerializationLock;
}
