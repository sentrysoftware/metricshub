package com.sentrysoftware.matrix.connector.model.monitor.job.source.compute;

import com.sentrysoftware.matrix.connector.model.common.ConversionType;
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
public class Convert extends Compute {

	private static final long serialVersionUID = -4813127390503515508L;

	private Integer column;
	private ConversionType conversionType;

	@Builder
	public Convert(Integer index, Integer column, ConversionType conversionType) {
		super(index);
		this.column = column;
		this.conversionType = conversionType;
	}

	@Override
	public void accept(final IComputeVisitor computeVisitor) {
		computeVisitor.visit(this);
	}


}
