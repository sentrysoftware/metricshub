package com.sentrysoftware.matrix.connector.model.common.sshstep;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.NEW_LINE;
import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.util.StringJoiner;
import java.util.function.UnaryOperator;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.matrix.connector.deserializer.custom.TimeoutDeserializer;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GetUntilPrompt extends Step {

	private static final long serialVersionUID = 1L;

	@JsonDeserialize(using = TimeoutDeserializer.class)
	private Long timeout;

	@Builder
	public GetUntilPrompt(String type, Boolean capture, boolean ignored, Long timeout) {

		super(type, capture, ignored);
		this.timeout = timeout;
	}

	@Override
	public GetUntilPrompt copy() {

		return GetUntilPrompt
			.builder()
			.type(type)
			.capture(capture)
			.ignored(ignored)
			.timeout(timeout)
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

		addNonNull(stringJoiner, "- timeout=", timeout);

		return stringJoiner.toString();

	}

}
