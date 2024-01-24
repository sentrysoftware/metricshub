package org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute;

import static com.fasterxml.jackson.annotation.Nulls.FAIL;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.NEW_LINE;
import static org.sentrysoftware.metricshub.engine.common.helpers.StringHelper.addNonNull;

import com.fasterxml.jackson.annotation.JsonSetter;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * An abstract class representing a computation operation for concatenation. Subclasses should provide specific
 * implementations for column and value settings. This class extends the {@link Compute} class and includes
 * settings for the column index and a value to concatenate.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractConcat extends Compute {

	private static final long serialVersionUID = 1L;

	/**
	 * The column index for concatenation.
	 */
	@NonNull
	@JsonSetter(nulls = FAIL)
	protected Integer column;

	/**
	 * The value to concatenate.
	 */
	@NonNull
	@JsonSetter(nulls = FAIL)
	protected String value;

	/**
	 * Returns a string representation of the AbstractConcat instance, including the type, column index, and value.
	 *
	 * @return A string representation of the object.
	 */
	protected AbstractConcat(String type, Integer column, String value) {
		super(type);
		this.column = column;
		this.value = value;
	}

	@Override
	public String toString() {
		final StringJoiner valueJoiner = new StringJoiner(NEW_LINE);

		valueJoiner.add(super.toString());

		addNonNull(valueJoiner, "- column=", column);
		addNonNull(valueJoiner, "- value=", value);

		return valueJoiner.toString();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		value = updater.apply(value);
	}
}
