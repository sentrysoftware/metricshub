package com.sentrysoftware.matrix.connector.model.monitor.job.source.compute;

import java.util.ArrayList;
import java.util.List;

import com.sentrysoftware.matrix.connector.model.common.EmbeddedFile;
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
public class Awk extends Compute {

	private static final long serialVersionUID = -60202574277309621L;

	private EmbeddedFile awkScript;
	private String excludeRegExp;
	private String keepOnlyRegExp;
	private String separators;
	private List<String> selectColumns = new ArrayList<>();

	@Builder
	public Awk(Integer index, EmbeddedFile awkScript, String excludeRegExp, String keepOnlyRegExp, String separators,
			List<String> selectColumns) {
		super(index);
		this.awkScript = awkScript;
		this.excludeRegExp = excludeRegExp;
		this.keepOnlyRegExp = keepOnlyRegExp;
		this.separators = separators;
		this.selectColumns = selectColumns == null ? new ArrayList<>() : selectColumns;
	}

	@Override
	public void accept(final IComputeVisitor computeVisitor) {
		computeVisitor.visit(this);
	}

}
