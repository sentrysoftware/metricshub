package com.sentrysoftware.matrix.connector.model.monitor.job.source.compute;

import com.sentrysoftware.matrix.engine.strategy.source.compute.IComputeVisitor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExtractPropertyFromWbemPath implements Compute {

	private static final long serialVersionUID = -1223955587166569350L;

	private String propertyName;

	@Override
	public void accept(final IComputeVisitor computeVisitor) {
		computeVisitor.visit(this);
	}
}
