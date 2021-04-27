package com.sentrysoftware.matrix.connector.model.monitor.job.source.compute;

import java.io.Serializable;

import com.sentrysoftware.matrix.engine.strategy.source.compute.IComputeVisitor;

public interface Compute extends Serializable {

	public void accept(final IComputeVisitor computeVisitor);

}
