package com.sentrysoftware.matrix.connector.model.monitor;

import com.sentrysoftware.matrix.connector.model.monitor.task.AllAtOnce;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AllAtOnceMonitorJob implements MonitorJob {

	private static final long serialVersionUID = 1L;

	private AllAtOnce allAtOnce;

}
