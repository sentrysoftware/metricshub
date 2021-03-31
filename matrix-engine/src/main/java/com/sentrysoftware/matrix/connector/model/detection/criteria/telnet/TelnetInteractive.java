package com.sentrysoftware.matrix.connector.model.detection.criteria.telnet;

import java.util.ArrayList;
import java.util.List;

import com.sentrysoftware.matrix.connector.model.common.telnet.step.Step;
import com.sentrysoftware.matrix.connector.model.detection.criteria.Criteria;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TelnetInteractive extends Criteria {

	private static final long serialVersionUID = 8279611415738510282L;

	private Integer port;
	private String expectedResult;
	private List<Step> steps = new ArrayList<>();

	@Builder
	public TelnetInteractive(boolean forceSerialization, Integer port, String expectedResult, List<Step> steps) {

		super(forceSerialization);
		this.port = port;
		this.expectedResult = expectedResult;
		this.steps = steps;
	}

}
