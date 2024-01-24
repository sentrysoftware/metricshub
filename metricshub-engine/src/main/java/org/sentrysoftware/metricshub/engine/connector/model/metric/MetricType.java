package org.sentrysoftware.metricshub.engine.connector.model.metric;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub Engine
 * ჻჻჻჻჻჻
 * Copyright 2023 - 2024 Sentry Software
 * ჻჻჻჻჻჻
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱
 */

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
