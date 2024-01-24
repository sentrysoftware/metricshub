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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import java.util.function.UnaryOperator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sentrysoftware.metricshub.engine.strategy.source.compute.IComputeProcessor;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
@JsonSubTypes(
	{
		@JsonSubTypes.Type(value = Add.class, name = "add"),
		@JsonSubTypes.Type(value = And.class, name = "and"),
		@JsonSubTypes.Type(value = ArrayTranslate.class, name = "arrayTranslate"),
		@JsonSubTypes.Type(value = Awk.class, name = "awk"),
		@JsonSubTypes.Type(value = Convert.class, name = "convert"),
		@JsonSubTypes.Type(value = Divide.class, name = "divide"),
		@JsonSubTypes.Type(value = DuplicateColumn.class, name = "duplicateColumn"),
		@JsonSubTypes.Type(value = ExcludeMatchingLines.class, name = "excludeMatchingLines"),
		@JsonSubTypes.Type(value = Extract.class, name = "extract"),
		@JsonSubTypes.Type(value = ExtractPropertyFromWbemPath.class, name = "extractPropertyFromWbemPath"),
		@JsonSubTypes.Type(value = Json2Csv.class, name = "json2Csv"),
		@JsonSubTypes.Type(value = KeepColumns.class, name = "keepColumns"),
		@JsonSubTypes.Type(value = KeepOnlyMatchingLines.class, name = "keepOnlyMatchingLines"),
		@JsonSubTypes.Type(value = LeftConcat.class, name = "leftConcat"),
		@JsonSubTypes.Type(value = Multiply.class, name = "multiply"),
		@JsonSubTypes.Type(value = PerBitTranslation.class, name = "perBitTranslation"),
		@JsonSubTypes.Type(value = Replace.class, name = "replace"),
		@JsonSubTypes.Type(value = RightConcat.class, name = "rightConcat"),
		@JsonSubTypes.Type(value = Subtract.class, name = "subtract"),
		@JsonSubTypes.Type(value = Substring.class, name = "substring"),
		@JsonSubTypes.Type(value = Translate.class, name = "translate"),
		@JsonSubTypes.Type(value = Xml2Csv.class, name = "xml2Csv")
	}
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Compute implements Serializable {

	private static final long serialVersionUID = 1L;

	protected String type;

	public abstract Compute copy();

	public abstract void update(UnaryOperator<String> updater);

	@Override
	public String toString() {
		return new StringBuilder("- type=").append(this.getClass().getSimpleName()).toString();
	}

	public abstract void accept(IComputeProcessor computeProcessor);
}
