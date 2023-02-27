package com.sentrysoftware.matrix.connector.model.metric;

import com.fasterxml.jackson.annotation.JsonAlias;

public enum MetricType implements IMetricType {

	@JsonAlias("gauge")
	GAUGE,
	@JsonAlias("counter")
	COUNTER,
	@JsonAlias(value = { "upDownCounter", "up_down_counter" })
	UP_DOWN_COUNTER,;

	@Override
	public MetricType get() {
		return this;
	}

}
