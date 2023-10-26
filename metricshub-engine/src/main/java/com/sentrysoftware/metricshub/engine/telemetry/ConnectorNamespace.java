package com.sentrysoftware.metricshub.engine.telemetry;

import com.sentrysoftware.metricshub.engine.strategy.source.SourceTable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConnectorNamespace {

	@Default
	private Map<String, SourceTable> sourceTables = new HashMap<>();

	private String automaticWmiNamespace;
	private String automaticWbemNamespace;
	private boolean isStatusOk;

	@Default
	private ReentrantLock forceSerializationLock = new ReentrantLock(true);

	/**
	 * Add a source in the current sourceTables map
	 *
	 * @param key sourceTable key
	 * @param sourceTable sourceTable instance
	 */
	public void addSourceTable(@NonNull String key, @NonNull SourceTable sourceTable) {
		sourceTables.put(key, sourceTable);
	}

	/**
	 * Get the {@link SourceTable} identified with the given key
	 *
	 * @param key sourceTable key
	 * @return return existing {@link SourceTable} object
	 */
	public SourceTable getSourceTable(@NonNull String key) {
		return sourceTables.get(key);
	}
}
