package com.sentrysoftware.metricshub.engine.connector.model.metric;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum MetricType implements IMetricType {
	@JsonAlias("gauge")
	GAUGE(new Gauge()),
	@JsonAlias("counter")
	COUNTER(new Counter()),
	@JsonAlias(value = { "upDownCounter", "up_down_counter" })
	UP_DOWN_COUNTER(new UpDownCounter());

	@Getter
	private IMetricKeyType metricKeyType;

	@Override
	public MetricType get() {
		return this;
	}

	public interface IMetricKeyType {
		/**
		 * Accept the given visitor
		 *
		 * @param visitor visitor class having its specific business logic
		 */
		void accept(IMetricTypeVisitor visitor);
	}

	public static class Gauge implements IMetricKeyType {

		@Override
		public void accept(final IMetricTypeVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class Counter implements IMetricKeyType {

		@Override
		public void accept(final IMetricTypeVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class UpDownCounter implements IMetricKeyType {

		@Override
		public void accept(final IMetricTypeVisitor visitor) {
			visitor.visit(this);
		}
	}

	/**
	 * Defines the visit of each metric type
	 *
	 */
	public interface IMetricTypeVisitor {
		/**
		 * Visit the given Gauge type
		 * @param gauge
		 */
		void visit(Gauge gauge);

		/**
		 * Visit the given Counter type
		 * @param counter
		 */
		void visit(Counter counter);

		/**
		 * Visit the given UpDownCounter type
		 * @param upDownCounter
		 */
		void visit(UpDownCounter upDownCounter);
	}
}
