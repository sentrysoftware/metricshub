package com.sentrysoftware.matrix.connector.model.monitor;

import java.io.Serializable;

import com.sentrysoftware.matrix.connector.model.monitor.job.MonitorJob;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HardwareMonitor implements Serializable {

	private static final long serialVersionUID = -1334219640435515973L;

	private MonitorType type;
	private MonitorJob discovery;
	private MonitorJob collect;
}
