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
public class SendUsername extends Step {

	private static final long serialVersionUID = 7932662970835172835L;

	@Builder
	public SendUsername(Integer index) {

		super(index);
	}

	@Override
	public void accept(final IStepVisitor visitor) throws StepException {
		visitor.visit(this);
	}

	@Override
	public SendUsername copy() {

		final SendUsername sendUsername =  new SendUsername(index);
		sendUsername.setCapture(capture);
		sendUsername.setIgnored(ignored);

		return sendUsername;
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
