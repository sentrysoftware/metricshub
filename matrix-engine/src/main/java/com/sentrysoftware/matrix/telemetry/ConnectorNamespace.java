package com.sentrysoftware.matrix.telemetry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Builder.Default;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import com.sentrysoftware.matrix.strategy.source.SourceTable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConnectorNamespace {

	@Default
	private Map<String, SourceTable> sourceTables = new HashMap<>();
	private String automaticWmiNamespace;
	private String automaticWbemNamespace;
	private ReentrantLock forceSerializationLock;

	/**
	 * Add a source in the current sourceTables map
	 * 
	 * @param key
	 * @param sourceTable
	 */
	public void addSourceTable(@NonNull String key, @NonNull SourceTable sourceTable) {
		sourceTables.put(key, sourceTable);
	}

	/**
	 * Get the {@link SourceTable} identified with the given key
	 * 
	 * @param key
	 * @return return existing {@link SourceTable} object
	 */
	public SourceTable getSourceTable(@NonNull String key) {
		return sourceTables.get(key);
	}
}