package com.sentrysoftware.matrix.model.monitoring;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sentrysoftware.matrix.model.monitor.Monitor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HostMonitorsVO { 
	private int numberOfMonitors;
	@Builder.Default
	private Map<String, List<Monitor>> monitors = new HashMap<>();
}
