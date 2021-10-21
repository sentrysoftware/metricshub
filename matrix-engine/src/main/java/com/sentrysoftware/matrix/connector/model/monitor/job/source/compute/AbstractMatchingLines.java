package com.sentrysoftware.matrix.connector.model.monitor.job.source.compute;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Set;
import java.util.TreeSet;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class AbstractMatchingLines extends Compute {

	private static final long serialVersionUID = 3125914016586965365L;

	private Integer column;
	private String regExp;
	private Set<String> valueSet = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

	protected AbstractMatchingLines(Integer index, Integer column, String regExp, Set<String> valueSet) {

		super(index);

		this.column = column;
		this.regExp = regExp;
		this.valueSet = valueSet == null ? new TreeSet<>(String.CASE_INSENSITIVE_ORDER) : valueSet;
	}
}
