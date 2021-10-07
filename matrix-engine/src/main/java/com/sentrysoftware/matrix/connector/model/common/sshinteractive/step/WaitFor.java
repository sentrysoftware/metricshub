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
}
