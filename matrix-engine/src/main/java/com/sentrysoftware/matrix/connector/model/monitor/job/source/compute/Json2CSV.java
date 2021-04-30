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
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Json2CSV extends Compute {

	private static final long serialVersionUID = -4481018666787412274L;

	private String entryKey;
	@Default
	private List<String> properties = new ArrayList<>();
	private String separator;

	@Override
	public void accept(final IComputeVisitor computeVisitor) {
		computeVisitor.visit(this);
	}
}
