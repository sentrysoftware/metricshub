package com.sentrysoftware.matrix.connector.model.monitor.job.source.compute;

import java.util.ArrayList;
import java.util.List;

import com.sentrysoftware.matrix.engine.strategy.source.compute.IComputeVisitor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KeepColumns extends Compute {

	private static final long serialVersionUID = 8346789196215087296L;

	@Default
	private List<Integer> columnNumbers = new ArrayList<>();

	@Override
	public void accept(final IComputeVisitor computeVisitor) {
		computeVisitor.visit(this);
	}
}
