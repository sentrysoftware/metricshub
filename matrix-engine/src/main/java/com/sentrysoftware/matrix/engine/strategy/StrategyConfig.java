package com.sentrysoftware.matrix.engine.strategy;

import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StrategyConfig {

	private IHostMonitoring hostMonitoring;
	private EngineConfiguration engineConfiguration;
}
