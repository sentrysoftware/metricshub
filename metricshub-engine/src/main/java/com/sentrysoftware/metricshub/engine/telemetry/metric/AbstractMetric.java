package com.sentrysoftware.metricshub.engine.telemetry.metric;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes(
	{
		@JsonSubTypes.Type(value = StateSetMetric.class, name = StateSetMetric.STATE_SET_METRIC_TYPE),
		@JsonSubTypes.Type(value = NumberMetric.class, name = NumberMetric.NUMBER_METRIC_TYPE)
	}
)
public abstract class AbstractMetric {

	private String name;
	private Long collectTime;
	private Long previousCollectTime;
	private Map<String, String> attributes = new HashMap<>();
	private boolean resetMetricTime;

	AbstractMetric(final String name, final Long collectTime, final Map<String, String> attributes) {
		this.name = name;
		this.collectTime = collectTime;
		this.attributes = attributes == null ? new HashMap<>() : attributes;
	}

	/**
	 * Set the collect time as previous collect time
	 */
	public void save() {
		previousCollectTime = collectTime;
	}

	/**
	 * Whether the metric is updated or not
	 *
	 * @return <code>true</code> if the collect time is different from the
	 * previous collect time otherwise <code>false</code>
	 */
	public boolean isUpdated() {
		if (collectTime == null) {
			return false;
		}

		return !collectTime.equals(previousCollectTime);
	}

	/**
	 * Get the metric type as String value
	 *
	 * @return {@link String} value
	 */
	public abstract String getType();

	/**
	 * Get the metric value
	 */
	public abstract <T> T getValue();
}
