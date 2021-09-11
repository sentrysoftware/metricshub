package com.sentrysoftware.matrix.model.monitoring;

import java.util.Map;
import java.util.TreeMap;

import com.sentrysoftware.matrix.engine.strategy.source.SourceTable;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class ConnectorNamespace {

	@Default
	private Map<String, SourceTable> sourceTables = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

	private String automaticWmiNamespace;

	private String automaticWbemNamespace;

	public void addSourceTable(@NonNull String key, @NonNull SourceTable sourceTable) {
		sourceTables.put(key, sourceTable);
	}

	public SourceTable getSourceTable(@NonNull String key) {
		return sourceTables.get(key);
	}

}
