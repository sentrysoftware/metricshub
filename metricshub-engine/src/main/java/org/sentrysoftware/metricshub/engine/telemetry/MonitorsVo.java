package org.sentrysoftware.metricshub.engine.telemetry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Value Object (VO) class representing a collection of monitors with additional metadata.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MonitorsVo {

	private int total;

	@Default
	private List<Monitor> monitors = new ArrayList<>();

	/**
	 * Ingest all the given monitors
	 *
	 * @param monitors Collection of {@link Monitor} instances
	 */
	public void addAll(final Collection<Monitor> monitors) {
		if (this.monitors.addAll(monitors)) {
			total = this.monitors.size();
		}
	}
}
