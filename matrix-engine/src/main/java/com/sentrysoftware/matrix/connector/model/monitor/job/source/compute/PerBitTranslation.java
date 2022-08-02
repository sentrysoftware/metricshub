package com.sentrysoftware.matrix.connector.model.monitor.job.source.compute;

import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.connector.model.common.TranslationTable;
import com.sentrysoftware.matrix.engine.strategy.source.compute.IComputeVisitor;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PerBitTranslation extends Compute {

	private static final long serialVersionUID = -7456243256660647476L;

	private Integer column;
	private List<Integer> bitList = new ArrayList<>();
	private TranslationTable bitTranslationTable;

	@Builder
	public PerBitTranslation(Integer index, Integer column, List<Integer> bitList,
			TranslationTable bitTranslationTable) {
		super(index);
		this.column = column;
		this.bitList = bitList == null ? new ArrayList<>() : bitList;
		this.bitTranslationTable = bitTranslationTable;
	}

	@Override
	public void accept(final IComputeVisitor computeVisitor) {
		computeVisitor.visit(this);
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(HardwareConstants.NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- column=", column);
		addNonNull(stringJoiner, "- bitList=", bitList);
		addNonNull(stringJoiner, "- bitTranslationTable=",
				bitTranslationTable != null ? bitTranslationTable.getName() : null);

		return stringJoiner.toString();

	}

	@Override
	public PerBitTranslation copy() {
		return PerBitTranslation
			.builder()
			.index(index)
			.column(column)
			.bitList(new ArrayList<>(bitList))
			.bitTranslationTable(bitTranslationTable != null  ? bitTranslationTable.copy() : null)
			.build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		if (bitTranslationTable != null) {
			bitTranslationTable.update(updater);
		}
	}
}
