package com.sentrysoftware.matrix.connector.model.common.sshinteractive.step;

import java.util.function.UnaryOperator;

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
	public GetAvailable copy() {

		final GetAvailable getAvailable =  new GetAvailable(index);
		getAvailable.setCapture(capture);
		getAvailable.setIgnored(ignored);

		return getAvailable;
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		// Nothing to update
	}

	@Override
	public String toString() {
		return super.toString();
	}

}
