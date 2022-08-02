package com.sentrysoftware.matrix.connector.model.monitor.job.source.compute;

import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.util.StringJoiner;
import java.util.function.UnaryOperator;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractConcat extends Compute {

	private static final long serialVersionUID = -4171343812982964238L;

	protected Integer column;
	protected String string;

	protected AbstractConcat(Integer index, Integer column, String string) {

		super(index);

		this.column = column;
		this.string = string;
	}

	@Override
	public String toString() {

		final StringJoiner stringJoiner = new StringJoiner(HardwareConstants.NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- column=", column);
		addNonNull(stringJoiner, "- string=", string);

		return stringJoiner.toString();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		string = updater.apply(string);
	}
}
