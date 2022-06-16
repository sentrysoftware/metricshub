package com.sentrysoftware.matrix.connector.model.common.sshinteractive.step;

import java.io.Serializable;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;

import com.sentrysoftware.matrix.common.exception.StepException;
import com.sentrysoftware.matrix.common.helpers.HardwareConstants;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
public abstract class Step implements Serializable {

	private static final long serialVersionUID = 1631362528155294870L;

	@NonNull
	protected Integer index;

	protected Boolean capture;

	/** If true this step is not processed. (Mainly when parameter TelnetOnly is set) */
	protected boolean ignored;

	public abstract void accept(final IStepVisitor visitor) throws StepException;

	public abstract Step copy();

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(HardwareConstants.NEW_LINE);

		stringJoiner.add(new StringBuilder("- type=").append(this.getClass().getSimpleName()));

		addNonNull(stringJoiner, "- index=", index);
		addNonNull(stringJoiner, "- capture=", capture);
		addNonNull(stringJoiner, "- ignored=", ignored);

		return stringJoiner.toString();
	}

	public abstract void update(UnaryOperator<String> updater);
}
