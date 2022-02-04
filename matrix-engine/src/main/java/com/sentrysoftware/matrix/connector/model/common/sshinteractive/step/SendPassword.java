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
public class SendPassword extends Step {

	private static final long serialVersionUID = -7797324854055894092L;

	@Builder
	public SendPassword(Integer index) {

		super(index);
	}

	@Override
	public void accept(final IStepVisitor visitor) throws StepException {
		visitor.visit(this);
	}

	@Override
	public SendPassword copy() {

		final SendPassword sendPassword =  new SendPassword(index);
		sendPassword.setCapture(capture);
		sendPassword.setIgnored(ignored);

		return sendPassword;
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		// For now nothing to update
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
