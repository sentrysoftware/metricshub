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
public class WaitForPrompt extends Step {

	private static final long serialVersionUID = -1362469305560698770L;

	private Long timeout;

	@Builder
	public WaitForPrompt(Integer index, Long timeout) {

		super(index);
		this.timeout = timeout;
	}

	@Override
	public void accept(final IStepVisitor visitor) throws StepException {
		visitor.visit(this);
	}

	@Override
	public WaitForPrompt copy() {

		final WaitForPrompt waitForPrompt =  new WaitForPrompt(index, timeout);
		waitForPrompt.setCapture(capture);
		waitForPrompt.setIgnored(ignored);

		return waitForPrompt;
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		// For now nothing to update
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(HardwareConstants.NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- timeout=", timeout);

		return stringJoiner.toString();
	}

}
