package com.sentrysoftware.matrix.connector.model.common.telnet.step;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GetUntilPrompt extends Step {

	private static final long serialVersionUID = 56690800276839618L;

	private Long timeout;

	@Builder
	public GetUntilPrompt(boolean capture, boolean telnetOnly, Long timeout) {

		super(capture, telnetOnly);
		this.timeout = timeout;
	}

}
