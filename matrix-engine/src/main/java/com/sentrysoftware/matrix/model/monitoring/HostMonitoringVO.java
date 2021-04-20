package com.sentrysoftware.matrix.model.monitoring;

import java.util.ArrayList;
import java.util.List;

import com.sentrysoftware.matrix.model.monitor.Monitor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HostMonitoringVO {
	private int total;

	@Default
	private List<Monitor> monitors = new ArrayList<>();

	public void addAll(List<Monitor> monitorList) {
		if (monitors.addAll(monitorList)) {
			total += monitorList.size();
		}
	}

}
