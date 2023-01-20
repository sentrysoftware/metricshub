package com.sentrysoftware.matrix.connector.model.monitor.task.source.compute;

import java.io.Serializable;
import java.util.function.UnaryOperator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Compute implements Serializable {

	private static final long serialVersionUID = 1L;

	protected String type;

	public abstract Compute copy();

	public abstract void update(UnaryOperator<String> updater);

	@Override
	public String toString() {
		return new StringBuilder("- type=")
			.append(this.getClass().getSimpleName())
			.toString();
	}
}
