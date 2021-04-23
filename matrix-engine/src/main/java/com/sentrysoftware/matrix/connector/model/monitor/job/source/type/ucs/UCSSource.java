package com.sentrysoftware.matrix.connector.model.monitor.job.source.type.ucs;

import java.util.ArrayList;
import java.util.List;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Compute;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UCSSource extends Source {

	private static final long serialVersionUID = 2010036387689462346L;

	private List<String> queries = new ArrayList<>();
	private String excludeRegExp;
	private String keepOnlyRegExp;
	private List<String> selectColumns = new ArrayList<>();

	@Builder
	public UCSSource(List<Compute> computes, boolean forceSerialization, List<String> queries,
			String excludeRegExp, String keepOnlyRegExp, List<String> selectColumns, int index, String key) {

		super(computes, forceSerialization, index, key);
		this.queries = queries;
		this.excludeRegExp = excludeRegExp;
		this.keepOnlyRegExp = keepOnlyRegExp;
		this.selectColumns = selectColumns;
	}

}
