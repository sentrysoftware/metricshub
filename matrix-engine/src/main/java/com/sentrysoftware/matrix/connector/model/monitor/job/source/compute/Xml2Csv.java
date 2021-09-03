package com.sentrysoftware.matrix.connector.model.monitor.job.source.compute;

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
public class Xml2Csv extends Compute {

	private static final long serialVersionUID = 6561437878414249082L;

	private String recordTag;
	private String properties;

	@Builder
	public Xml2Csv(Integer index, String recordTag, String properties) {
		super(index);
		this.recordTag = recordTag;
		this.properties = properties;
	}

	@Override
	public void accept(final IComputeVisitor computeVisitor) {
		computeVisitor.visit(this);
	}

}
