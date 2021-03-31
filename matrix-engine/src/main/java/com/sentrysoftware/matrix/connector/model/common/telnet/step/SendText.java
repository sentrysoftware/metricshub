package com.sentrysoftware.matrix.connector.model.common.telnet.step;

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
	public SendText(boolean capture, boolean telnetOnly, String text) {

		super(capture, telnetOnly);
		this.text = text;
	}

}
