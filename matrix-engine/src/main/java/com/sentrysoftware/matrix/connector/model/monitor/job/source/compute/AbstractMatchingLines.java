package com.sentrysoftware.matrix.connector.model.monitor.job.source.compute;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class AbstractMatchingLines extends Compute {

	private static final long serialVersionUID = 3125914016586965365L;

	protected Integer column;
	protected String regExp;
	protected List<String> valueList = new ArrayList<>();

	protected AbstractMatchingLines(Integer index, Integer column, String regExp, List<String> valueList) {

		super(index);

		this.column = column;
		this.regExp = regExp;
		this.valueList = valueList == null ? new ArrayList<>() : valueList;
	}
}
