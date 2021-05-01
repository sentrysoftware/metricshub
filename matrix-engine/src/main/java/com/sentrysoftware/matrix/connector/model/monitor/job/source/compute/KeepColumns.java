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
public class KeepColumns extends Compute {

	private static final long serialVersionUID = 8346789196215087296L;

	private List<Integer> columnNumbers = new ArrayList<>();

	@Builder
	public KeepColumns(Integer index, List<Integer> columnNumbers) {
		super(index);
		this.columnNumbers = columnNumbers == null ? new ArrayList<>() : columnNumbers;
	}

	@Override
	public void accept(final IComputeVisitor computeVisitor) {
		computeVisitor.visit(this);
	}

}
