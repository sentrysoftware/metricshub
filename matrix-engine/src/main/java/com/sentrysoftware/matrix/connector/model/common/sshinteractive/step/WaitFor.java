package com.sentrysoftware.matrix.connector.model.common.sshinteractive.step;

import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.util.StringJoiner;

import com.sentrysoftware.matrix.common.exception.StepException;
import com.sentrysoftware.matrix.common.helpers.HardwareConstants;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WaitFor extends Step {

	private static final long serialVersionUID = 3018513050308002112L;

	private String text;
	private Long timeout;

	@Builder
	public WaitFor(Integer index, String text, Long timeout) {

		super(index);
		this.text = text;
		this.timeout = timeout;
	}

	@Override
	public void accept(final IStepVisitor visitor) throws StepException {
		visitor.visit(this);
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(HardwareConstants.NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- text=", text);
		addNonNull(stringJoiner, "- timeout=", timeout);

		return stringJoiner.toString();
	}
}
