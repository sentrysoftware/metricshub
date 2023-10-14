package com.sentrysoftware.matrix.telemetry;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.PRESENT_STATUS;

import com.sentrysoftware.matrix.alert.AlertRule;
import com.sentrysoftware.matrix.common.helpers.KnownMonitorType;
import com.sentrysoftware.matrix.common.helpers.MatrixConstants;
import com.sentrysoftware.matrix.telemetry.metric.AbstractMetric;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Monitor {

	@Default
	private Map<String, AbstractMetric> metrics = new HashMap<>();

	@Default
	private Map<String, String> attributes = new HashMap<>();

	@Default
	private Map<String, String> conditionalCollection = new HashMap<>();

	@Default
	private Map<String, String> legacyTextParameters = new HashMap<>();

	@Default
	private Map<String, List<AlertRule>> alertRules = new HashMap<>();

	private Resource resource;
	private long discoveryTime;
	private String type;
	private String id;
	private boolean endpoint;

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

	/***
	 * Add a new metric in the map of metrics
	 *
	 * @param metricName The unique name of the metric
	 * @param metric     The metric instance to add
	 */
	public void addMetric(final String metricName, final AbstractMetric metric) {
		metrics.put(metricName, metric);
	}

	/**
	 * Add the given attributes to the current map of attributes
	 *
	 * @param attributes Map of key-pair values to be added to the current map of attributes
	 */
	public void addAttributes(@NonNull final Map<String, String> attributes) {
		this.attributes.putAll(attributes);
	}

	/**
	 * Add the given conditionalCollection map to the current map of conditionalCollection
	 *
	 * @param conditionalCollection Map of key-pair values to be added to the current map of conditionalCollection
	 */
	public void addConditionalCollection(Map<String, String> conditionalCollection) {
		this.conditionalCollection.putAll(conditionalCollection);
	}

	/**
	 * Add the given legacyTextParameters map to the current map of legacyTextParameters
	 *
	 * @param legacyTextParameters Map of key-pair values to be added to the current map of legacyTextParameters
	 */
	public void addLegacyParameters(Map<String, String> legacyTextParameters) {
		this.legacyTextParameters.putAll(legacyTextParameters);
	}

	/**
	 * This method adds a new attribute to the current monitor
	 * @param key attribute key
	 * @param value attribute value
	 */
	public void addAttribute(final String key, final String value) {
		attributes.put(key, value);
	}

	/**
	 * This method gets an attribute from the current monitor using its key
	 * @param key attribute key
	 * @return attribute value
	 */
	public String getAttribute(final String key) {
		return attributes.get(key);
	}

	/**
	 * This method returns whether a metric is deactivated
	 * @param key metric key
	 * @return boolean
	 */
	public boolean isMetricDeactivated(final String key) {
		return MatrixConstants.EMPTY.equals(conditionalCollection.get(key));
	}

	/**
	 * Set the current monitor as missing
	 */
	public void setAsMissing(final String hostname) {
		new MetricFactory(hostname).collectNumberMetric(this, String.format(PRESENT_STATUS, type), 0.0, discoveryTime);
	}

	/**
	 * Set the current monitor as present
	 */
	public void setAsPresent(final String hostname) {
		new MetricFactory(hostname).collectNumberMetric(this, String.format(PRESENT_STATUS, type), 1.0, discoveryTime);
	}

	/**
	 * This method checks if the monitor type is {@code host} and represents an endpoint.
	 *
	 * @return {@code true} if the monitor is of type "host" and is an endpoint; {@code false} otherwise.
	 */
	public boolean isEndpointHost() {
		return KnownMonitorType.HOST.getKey().equals(type) && isEndpoint();
	}
}
