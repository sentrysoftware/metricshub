package com.sentrysoftware.matrix.telemetry;

import com.sentrysoftware.matrix.alert.AlertRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Monitor {

	private Map<String, AbstractMetric> metrics;
	private Map<String, String> attributes;
	private Map<String, List<AlertRule>> alertRules;
	private Resource resource;
	private long discoveryTime;
}
