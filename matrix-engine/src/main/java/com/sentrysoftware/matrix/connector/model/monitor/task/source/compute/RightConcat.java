package com.sentrysoftware.matrix.connector.model.monitor.task.source.compute;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RightConcat extends AbstractConcat {

	private static final long serialVersionUID = 1L;

	@Builder
	public RightConcat(String type, Integer column, String value) {
		super(type, column, value);
	}


	@Override
	public String toString() {
		return super.toString();
	}

	@Override
	public RightConcat copy() {
		return RightConcat
			.builder()
			.type(type)
			.column(column)
			.value(value)
			.build();
	}
}
