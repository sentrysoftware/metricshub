package com.sentrysoftware.matrix.connector.model.common.telnet.step;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GetAvailable extends Step {

	private static final long serialVersionUID = 5233581194028858077L;

	@Builder
	public GetAvailable(boolean capture, boolean telnetOnly) {

		super(capture, telnetOnly);
	}

}
