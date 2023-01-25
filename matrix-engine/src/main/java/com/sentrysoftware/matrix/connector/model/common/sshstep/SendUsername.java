package com.sentrysoftware.matrix.connector.model.common.sshstep;

import java.util.function.UnaryOperator;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SendUsername extends Step {

	private static final long serialVersionUID = 1L;

	@Builder
	public SendUsername(String type, Boolean capture, boolean ignored) {

		super(type, capture, ignored);
	}

	@Override
	public SendUsername copy() {

		return SendUsername.builder()
			.type(type)
			.capture(capture)
			.ignored(ignored)
			.build();
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
