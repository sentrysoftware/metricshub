package org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute;

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

import static com.fasterxml.jackson.annotation.Nulls.FAIL;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.NEW_LINE;
import static org.sentrysoftware.metricshub.engine.common.helpers.StringHelper.addNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.sentrysoftware.metricshub.engine.strategy.source.compute.IComputeProcessor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ExtractPropertyFromWbemPath extends Compute {

	private static final long serialVersionUID = 1L;

	@NonNull
	@JsonSetter(nulls = FAIL)
	private String property;

	@NonNull
	@JsonSetter(nulls = FAIL)
	private Integer column;

	@Builder
	@JsonCreator
	public ExtractPropertyFromWbemPath(
		@JsonProperty("type") String type,
		@JsonProperty(value = "column", required = true) @NonNull Integer column,
		@JsonProperty(value = "property", required = true) @NonNull String property
	) {
		super(type);
		this.property = property;
		this.column = column;
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- column=", column);
		addNonNull(stringJoiner, "- property=", property);

		return stringJoiner.toString();
	}

	@Override
	public ExtractPropertyFromWbemPath copy() {
		return ExtractPropertyFromWbemPath.builder().type(type).property(property).column(column).build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		property = updater.apply(property);
	}

	@Override
	public void accept(IComputeProcessor computeProcessor) {
		computeProcessor.process(this);
	}
}
