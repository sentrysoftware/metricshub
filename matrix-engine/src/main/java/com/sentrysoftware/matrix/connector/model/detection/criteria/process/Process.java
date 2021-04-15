package com.sentrysoftware.matrix.connector.model.detection.criteria.process;

import com.sentrysoftware.matrix.connector.model.detection.criteria.Criterion;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Process extends Criterion {

	private static final long serialVersionUID = 4418210555494869095L;

	private String processCommandLine;

	@Builder
	public Process(boolean forceSerialization, String processCommandLine, int index) {

		super(forceSerialization, index);
		this.processCommandLine = processCommandLine;
	}

	
}
