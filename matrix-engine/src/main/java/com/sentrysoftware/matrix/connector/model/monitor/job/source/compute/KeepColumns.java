package com.sentrysoftware.matrix.connector.model.monitor.job.source.compute;

import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.engine.strategy.source.compute.IComputeVisitor;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
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

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(HardwareConstants.NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- columnNumbers=", columnNumbers);

		return stringJoiner.toString();
	}

	@Override
	public KeepColumns copy() {
		return KeepColumns
			.builder()
			.index(index)
			.columnNumbers(columnNumbers != null ? new ArrayList<>(columnNumbers) : null)
			.build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		// Not implemented because this class doesn't define string members
	}
}
