package com.sentrysoftware.matrix.connector.model.monitor.task.source.compute;

import static com.fasterxml.jackson.annotation.Nulls.FAIL;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.NEW_LINE;
import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import com.fasterxml.jackson.annotation.JsonSetter;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractConcat extends Compute {

	private static final long serialVersionUID = 1L;

	@NonNull
	@JsonSetter(nulls = FAIL)
	protected Integer column;

	@NonNull
	@JsonSetter(nulls = FAIL)
	protected String value;

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
