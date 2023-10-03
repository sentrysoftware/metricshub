package com.sentrysoftware.matrix;

import com.sentrysoftware.matrix.delegate.IPostExecutionService;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
public class HardwareEnergyPostExecutionService implements IPostExecutionService {

	private TelemetryManager telemetryManager;

	@Override
	public void run() {
		// TODO
	}
}
