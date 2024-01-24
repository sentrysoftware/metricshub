package org.sentrysoftware.metricshub.engine.connector.model.monitor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.AbstractCollect;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.Discovery;

/**
 * Represents a standard monitor job.
 *
 * <p>
 * This class implements the {@link MonitorJob} interface and is designed for standard monitor jobs that include both
 * discovery and collection tasks.
 * </p>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StandardMonitorJob implements MonitorJob {

	private static final long serialVersionUID = 1L;

	/**
	 * The discovery task associated with this standard monitor job.
	 */
	private Discovery discovery;
	/**
	 * The collection task associated with this standard monitor job.
	 */
	private AbstractCollect collect;
}
