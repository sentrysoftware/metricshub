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
public class GetUntilPrompt extends Step {

	private static final long serialVersionUID = 56690800276839618L;

	private Long timeout;

	@Builder
	public GetUntilPrompt(Integer index, Long timeout) {

		super(index);
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

		addNonNull(stringJoiner, "- timeout=", timeout);

		return stringJoiner.toString();

	}

}
