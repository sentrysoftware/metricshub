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
public class Sleep extends Step {

	private static final long serialVersionUID = -6722571717607709100L;

	private Long duration;

	@Builder
	public Sleep(boolean capture, boolean telnetOnly, Long duration) {

		super(capture, telnetOnly);
		this.duration = duration;
	}

}
