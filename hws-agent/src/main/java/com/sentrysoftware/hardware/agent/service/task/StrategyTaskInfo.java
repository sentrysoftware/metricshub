package com.sentrysoftware.hardware.agent.service.task;

import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StrategyTaskInfo {

	@NonNull
	private IHostMonitoring hostMonitoring;

	private int discoveryCycle;
	private String loggerLevel;
	private String outputDirectory;
}
