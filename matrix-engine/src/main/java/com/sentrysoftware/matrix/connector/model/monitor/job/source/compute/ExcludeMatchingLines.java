package com.sentrysoftware.matrix.connector.model.monitor.job.source.compute;

import java.util.ArrayList;
import java.util.List;

import com.sentrysoftware.matrix.engine.strategy.source.compute.IComputeVisitor;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ExcludeMatchingLines extends Compute {

	private static final long serialVersionUID = -3662198961800729320L;

	private Integer column;
	private String regExp;
	private List<String> valueList = new ArrayList<>();

	@Builder
	public ExcludeMatchingLines(Integer index, Integer column, String regExp, List<String> valueList) {
		super(index);
		this.column = column;
		this.regExp = regExp;
		this.valueList = valueList == null ? new ArrayList<>() : valueList;
	}

	@Override
	public void accept(final IComputeVisitor computeVisitor) {
		computeVisitor.visit(this);
	}

}
