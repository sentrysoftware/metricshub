package org.sentrysoftware.metricshub.engine.connector.model.metric;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;

/**
 * Represents an interface for metric types.
 *
 * <p>
 * This interface is designed to be implemented by various metric type classes, providing a common way to retrieve
 * the metric type.
 * </p>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION, defaultImpl = MetricType.class)
@JsonSubTypes(@JsonSubTypes.Type(value = StateSet.class))
public interface IMetricType extends Serializable {
	/**
	 * Gets the metric type.
	 *
	 * @return The metric type.
	 */
	MetricType get();
}
