package com.sentrysoftware.matrix.connector.model.common.sshstep;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.NEW_LINE;
import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.util.StringJoiner;
import java.util.function.UnaryOperator;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WaitFor extends Step {

	private static final long serialVersionUID = 1L;

	private String text;
	private Long timeout;

	@Builder
	public WaitFor(String type, Boolean capture, boolean ignored, String text, Long timeout) {

		super(type, capture, ignored);
		this.text = text;
		this.timeout = timeout;
	}

	@Override
	public WaitFor copy() {

		return WaitFor
			.builder()
			.type(type)
			.capture(capture)
			.ignored(ignored)
			.text(text)
			.timeout(timeout)
			.build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		text = updater.apply(text);
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- text=", text);
		addNonNull(stringJoiner, "- timeout=", timeout);

		return stringJoiner.toString();
	}
}
