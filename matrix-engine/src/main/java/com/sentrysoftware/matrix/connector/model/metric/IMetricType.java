package com.sentrysoftware.matrix.connector.model.metric;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION, defaultImpl = MetricType.class) 
@JsonSubTypes(@JsonSubTypes.Type(value = StateSet.class))
public interface IMetricType extends Serializable {

	MetricType get();
}
