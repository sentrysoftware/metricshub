package com.sentrysoftware.matrix.connector.model.common.sshinteractive.step;

import com.sentrysoftware.matrix.common.exception.StepException;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
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
}
