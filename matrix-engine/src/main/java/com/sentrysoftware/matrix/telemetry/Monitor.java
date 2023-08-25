package com.sentrysoftware.matrix.telemetry;

import com.sentrysoftware.matrix.alert.AlertRule;
import com.sentrysoftware.matrix.telemetry.metric.AbstractMetric;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Monitor {

	private Map<String, AbstractMetric> metrics = new HashMap<>();
	private Map<String, String> attributes = new HashMap<>();
	private Map<String, List<AlertRule>> alertRules = new HashMap<>();
	private Resource resource;
	private long discoveryTime;
	private String type;

	/**
	 * Get a metric by type
	 *
	 * @param metricName The unique name of the metric
	 * @param type          The type of the metric
	 * @return {@link AbstractMetric} instance
	 */
	public <T extends AbstractMetric> T getMetric(final String metricName, final Class<T> type) {
		return type.cast(metrics.get(metricName));
	}

	/**
	 * Returns whether the monitor contains a given metric
	 * @param metricName
	 * @return true or false
	 */
	public boolean hasMetric(final String metricName){
		return metrics.containsKey(metricName);
	}
}
