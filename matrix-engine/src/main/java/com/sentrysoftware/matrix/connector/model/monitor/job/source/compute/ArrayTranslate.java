package com.sentrysoftware.matrix.connector.model.monitor.job.source.compute;

import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.util.StringJoiner;

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
public class ArrayTranslate extends Compute {

	private static final long serialVersionUID = -4184949025683323864L;

	private Integer column;
	private TranslationTable translationTable;
	private String arraySeparator;
	private String resultSeparator;

	@Builder
	public ArrayTranslate(Integer index, Integer column, TranslationTable translationTable, String arraySeparator,
			String resultSeparator) {
		super(index);
		this.column = column;
		this.translationTable = translationTable;
		this.arraySeparator = arraySeparator;
		this.resultSeparator = resultSeparator;
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
		addNonNull(stringJoiner, "- translationTable=", translationTable != null ? translationTable.getName() : null);
		addNonNull(stringJoiner, "- arraySeparator=", arraySeparator);
		addNonNull(stringJoiner, "- resultSeparator=", resultSeparator);

		return stringJoiner.toString();
	}

}
