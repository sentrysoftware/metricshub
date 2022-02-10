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
public class SendText extends Step {

	private static final long serialVersionUID = -289762217581958237L;

	private String text;

	@Builder
	public SendText(Integer index, String text) {

		super(index);
		this.text = text;
	}

	@Override
	public void accept(final IStepVisitor visitor) throws StepException {
		visitor.visit(this);
	}

	@Override
	public SendText copy() {

		final SendText sendText =  new SendText(index, text);
		sendText.setCapture(capture);
		sendText.setIgnored(ignored);

		return sendText;
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		text = updater.apply(text);
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(HardwareConstants.NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- text=", text);

		return stringJoiner.toString();
	}
}
