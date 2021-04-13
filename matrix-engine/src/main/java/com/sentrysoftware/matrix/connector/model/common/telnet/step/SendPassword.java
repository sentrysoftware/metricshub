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
public class SendPassword extends Step {

	private static final long serialVersionUID = -7797324854055894092L;

	@Builder
	public SendPassword(boolean capture, boolean telnetOnly) {

		super(capture, telnetOnly);
	}
}
