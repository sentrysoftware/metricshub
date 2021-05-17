package com.sentrysoftware.matrix.connector.model.monitor.job.source.compute;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class AbstractConcat extends Compute {

	private static final long serialVersionUID = -4171343812982964238L;

	private Integer column;
	private String string;

	protected AbstractConcat(Integer index, Integer column, String string) {

		super(index);

		this.column = column;
		this.string = string;
	}
}
