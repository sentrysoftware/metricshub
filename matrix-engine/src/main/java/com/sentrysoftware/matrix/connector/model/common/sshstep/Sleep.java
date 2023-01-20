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
public class Sleep extends Step {

	private static final long serialVersionUID = 1L;

	private Long duration;

	@Builder
	public Sleep(String type, Boolean capture, boolean ignored, Long duration) {

		super(type, capture, ignored);
		this.duration = duration;
	}


	@Override
	public Sleep copy() {

		return Sleep
			.builder()
			.type(type)
			.capture(capture)
			.ignored(ignored)
			.duration(duration)
			.build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		// For now nothing to update
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- duration=", duration);

		return stringJoiner.toString();
	}

}
