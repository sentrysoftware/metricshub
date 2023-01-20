package com.sentrysoftware.matrix.connector.model.common.sshstep;

import java.util.function.UnaryOperator;


import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GetAvailable extends Step {

	private static final long serialVersionUID = 1L;

	@Builder
	public GetAvailable(String type, Boolean capture, boolean ignored) {

		super(type, capture, ignored);
	}

	@Override
	public GetAvailable copy() {

		return GetAvailable
			.builder()
			.type(type)
			.capture(capture)
			.ignored(ignored)
			.build();
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
