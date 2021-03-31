package com.sentrysoftware.matrix.connector.model.common.telnet.step;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WaitFor extends Step {

	private static final long serialVersionUID = 3018513050308002112L;

	private String text;
	private Long timeout;

	@Builder
	public WaitFor(boolean capture, boolean telnetOnly, String text, Long timeout) {

		super(capture, telnetOnly);
		this.text = text;
		this.timeout = timeout;
	}

}
