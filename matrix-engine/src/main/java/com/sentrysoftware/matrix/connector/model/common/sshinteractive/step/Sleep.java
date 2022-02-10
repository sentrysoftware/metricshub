package com.sentrysoftware.matrix.connector.model.common.sshinteractive.step;

import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.util.StringJoiner;
import java.util.function.UnaryOperator;

import com.sentrysoftware.matrix.common.exception.StepException;
import com.sentrysoftware.matrix.common.helpers.HardwareConstants;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Sleep extends Step {

	private static final long serialVersionUID = -6722571717607709100L;

	private Long duration;

	@Builder
	public Sleep(Integer index, Long duration) {

		super(index);
		this.duration = duration;
	}

	@Override
	public void accept(final IStepVisitor visitor) throws StepException {
		visitor.visit(this);
	}

	@Override
	public Sleep copy() {

		final Sleep sleep =  new Sleep(index, duration);
		sleep.setCapture(capture);
		sleep.setIgnored(ignored);

		return sleep;
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		// For now nothing to update
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(HardwareConstants.NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- duration=", duration);

		return stringJoiner.toString();
	}

}
