package com.sentrysoftware.matrix.engine.strategy.collect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;
import com.sentrysoftware.matrix.model.parameter.ParameterState;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonitorCollectInfo {

	private Monitor monitor;
	private String connectorName;
	private IHostMonitoring hostMonitoring;
	private String hostname;
	@Default
	private List<String> row = new ArrayList<>();
	private String valueTable;
	@Default
	private Map<String, String> mapping = new HashMap<>();

	private Long collectTime;

	private ParameterState unknownStatus;
}