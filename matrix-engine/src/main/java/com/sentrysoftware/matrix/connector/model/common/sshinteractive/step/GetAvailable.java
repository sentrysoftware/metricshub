package com.sentrysoftware.matrix.connector.model.common.sshinteractive.step;

import com.sentrysoftware.matrix.common.exception.StepException;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GetAvailable extends Step {

	private static final long serialVersionUID = 5233581194028858077L;

	@Builder
	public GetAvailable(Integer index) {

		super(index);
	}

	@Override
	public void accept(final IStepVisitor visitor) throws StepException {
		visitor.visit(this);
	}

	@Override
	public String toString() {
		return super.toString();
	}

}
