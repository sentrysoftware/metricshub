package com.sentrysoftware.metricshub.engine.connector.model.metric;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION, defaultImpl = MetricType.class)
@JsonSubTypes(@JsonSubTypes.Type(value = StateSet.class))
public interface IMetricType extends Serializable {
	MetricType get();
}
