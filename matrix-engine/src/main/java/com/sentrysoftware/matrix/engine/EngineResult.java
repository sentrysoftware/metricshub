package com.sentrysoftware.matrix.engine;

import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EngineResult {

	private OperationStatus operationStatus;
	private IHostMonitoring hostMonitoring;
}
