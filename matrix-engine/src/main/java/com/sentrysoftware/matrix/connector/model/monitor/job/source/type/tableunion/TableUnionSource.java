package com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tableunion;

import java.util.ArrayList;
import java.util.List;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Compute;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TableUnionSource extends Source {

	private static final long serialVersionUID = -348604258888047116L;

	private List<String> tables = new ArrayList<>();

	@Builder
	public TableUnionSource(List<Compute> computes, boolean forceSerialization, List<String> tables) {

		super(computes, forceSerialization);
		this.tables = tables;
	}

}
