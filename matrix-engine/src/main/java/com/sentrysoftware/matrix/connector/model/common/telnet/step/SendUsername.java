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
public class SendUsername extends Step {

	private static final long serialVersionUID = 7932662970835172835L;

	@Builder
	public SendUsername(boolean capture, boolean telnetOnly) {

		super(capture, telnetOnly);
	}
}
