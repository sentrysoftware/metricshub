package com.sentrysoftware.matrix.connector.model.common.telnet.step;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WaitForPrompt extends Step {

	private static final long serialVersionUID = -1362469305560698770L;

	private Long timeout;

	@Builder
	public WaitForPrompt(boolean capture, boolean telnetOnly, Long timeout) {

		super(capture, telnetOnly);
		this.timeout = timeout;
	}

}
