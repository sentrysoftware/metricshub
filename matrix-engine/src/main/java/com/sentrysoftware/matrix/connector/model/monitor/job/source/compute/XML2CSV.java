package com.sentrysoftware.matrix.connector.model.monitor.job.source.compute;

import java.util.ArrayList;
import java.util.List;

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
public class XML2CSV extends Compute {

	private static final long serialVersionUID = 6561437878414249082L;

	private String recordTag;
	private List<String> properties = new ArrayList<>();
	private String separator;

	@Builder
	public XML2CSV(Integer index, String recordTag, List<String> properties, String separator) {
		super(index);
		this.recordTag = recordTag;
		this.properties = properties == null ? new ArrayList<>() : properties;
		this.separator = separator;
	}

	@Override
	public void accept(final IComputeVisitor computeVisitor) {
		computeVisitor.visit(this);
	}

}
