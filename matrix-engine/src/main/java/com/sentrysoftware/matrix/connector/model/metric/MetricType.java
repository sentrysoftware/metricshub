package com.sentrysoftware.matrix.connector.model.metric;

public enum MetricType implements IMetricType {

	GAUGE,
	COUNTER,
	UP_DOWN_COUNTER,;

	@Override
	public MetricType get() {
		return this;
	}

}
