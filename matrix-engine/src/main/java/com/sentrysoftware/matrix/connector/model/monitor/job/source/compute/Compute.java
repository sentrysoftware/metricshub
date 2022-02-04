package com.sentrysoftware.matrix.connector.model.monitor.job.source.compute;

import java.io.Serializable;

import com.sentrysoftware.matrix.engine.strategy.source.compute.IComputeVisitor;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public abstract class Compute implements Serializable {

	private static final long serialVersionUID = -7887124289220448847L;

	protected Integer index;

	protected Compute(Integer index) {
		this.index = index;
	}

	public abstract void accept(final IComputeVisitor computeVisitor);

	@Override
	public String toString() {
		return new StringBuilder("- type=").append(this.getClass().getSimpleName())
				.append("\n- index=").append(index)
				.toString();
	}
}
