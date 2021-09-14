package com.sentrysoftware.matrix.connector.model.monitor.job.source.compute;

import java.util.ArrayList;
import java.util.List;

import com.sentrysoftware.matrix.engine.strategy.source.compute.IComputeVisitor;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TABLE_SEP;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Json2CSV extends Compute {

	private static final long serialVersionUID = -4481018666787412274L;

	private String entryKey;
	private List<String> properties = new ArrayList<>();
	private String separator = TABLE_SEP;

	@Builder
	public Json2CSV(Integer index, String entryKey, List<String> properties, String separator) {
		super(index);
		this.entryKey = entryKey;
		this.properties = properties == null ? new ArrayList<>() : properties;
		this.separator = separator == null ? TABLE_SEP : separator;
	}
	@Override
	public void accept(final IComputeVisitor computeVisitor) {
		computeVisitor.visit(this);
	}

}
