package com.sentrysoftware.matrix.telemetry;

import com.sentrysoftware.matrix.connector.model.monitor.task.Mapping;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MappingProcessor {
	private TelemetryManager telemetryManager;
	private Mapping connectorMapping;
	private String id;
	private String hostname;
}
