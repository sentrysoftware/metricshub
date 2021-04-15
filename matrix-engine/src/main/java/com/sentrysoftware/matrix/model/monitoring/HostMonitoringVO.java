package com.sentrysoftware.matrix.model.monitoring;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HostMonitoringVO { 
	private int total;

	@Builder.Default
	private List<HostMonitorsVO> hostMonitors = new ArrayList<>();

	public void  addHostMonitor(HostMonitorsVO hostMonitorsVO) {
		if (hostMonitors.add(hostMonitorsVO)) {
			total += hostMonitorsVO.getNumberOfMonitors();
		}
	}
}
