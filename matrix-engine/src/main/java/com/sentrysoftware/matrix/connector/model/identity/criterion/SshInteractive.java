package com.sentrysoftware.matrix.connector.model.identity.criterion;

import java.util.ArrayList;
import java.util.List;

import com.sentrysoftware.matrix.connector.model.common.sshstep.Step;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SshInteractive extends Criterion {

	private static final long serialVersionUID = 1L;

	private Integer port;
	private String expectedResult;
	private List<Step> steps = new ArrayList<>();

	@Builder
	public SshInteractive(String type, boolean forceSerialization, Integer port, String expectedResult, List<Step> steps) {

		super(type, forceSerialization);
		this.port = port;
		this.expectedResult = expectedResult;
		this.steps = steps;
	}

}
