package com.sentrysoftware.matrix.connector.model.monitor.job.source.compute;

import java.util.ArrayList;
import java.util.List;

import com.sentrysoftware.matrix.connector.model.common.TranslationTable;
import com.sentrysoftware.matrix.engine.strategy.source.compute.IComputeVisitor;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
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

}
