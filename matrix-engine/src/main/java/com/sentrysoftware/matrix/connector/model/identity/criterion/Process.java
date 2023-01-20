package com.sentrysoftware.matrix.connector.model.identity.criterion;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Process extends Criterion {

	private static final long serialVersionUID = 1L;

	private String commandLine;

	@Builder
	public Process(String type, boolean forceSerialization, String commandLine, int index) {

		super(type, forceSerialization);
		this.commandLine = commandLine;
	}
	
}
