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
}
