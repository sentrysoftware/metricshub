package org.sentrysoftware.metricshub.engine.connector.model.monitor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.Simple;

/**
 * Represents a monitor job associated with a simple monitoring task.
 *
 * <p>
 * This class implements the {@link MonitorJob} interface and is used to encapsulate the configuration and details of a monitoring job that involves a
 * simple monitoring task.
 * </p>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SimpleMonitorJob implements MonitorJob {

	private static final long serialVersionUID = 1L;

	/**
	 * The simple task associated with this monitor job.
	 */
	private Simple simple;
}
