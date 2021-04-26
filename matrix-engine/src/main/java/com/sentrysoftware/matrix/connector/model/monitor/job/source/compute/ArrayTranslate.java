package com.sentrysoftware.matrix.connector.model.monitor.job.source.compute;

import com.sentrysoftware.matrix.connector.model.common.TranslationTable;
import com.sentrysoftware.matrix.engine.strategy.source.compute.IComputeVisitor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArrayTranslate implements Compute {

	private static final long serialVersionUID = -4184949025683323864L;

	private Integer column;
	private TranslationTable translationTable;
	private String arraySeparator;
	private String resultSeparator;

	@Override
	public void accept(final IComputeVisitor computeVisitor) {
		computeVisitor.visit(this);
	}
}
