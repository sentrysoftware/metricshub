package com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tablejoin;

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
public class TableJoinSource extends Source {

	private static final long serialVersionUID = -7311795243465084164L;

	private String leftTable;
	private String rightTable;
	private Integer leftKeyColumn;
	private Integer rightKeyColumn;
	private List<String> defaultRightLine;
	private String keyType;

	@Builder
	public TableJoinSource(List<Compute> computes, boolean forceSerialization, String leftTable,
			String rightTable, Integer leftKeyColumn, Integer rightKeyColumn, List<String> defaultRightLine,
			String keyType, int index, String key) {

		super(computes, forceSerialization, index, key);
		this.leftTable = leftTable;
		this.rightTable = rightTable;
		this.leftKeyColumn = leftKeyColumn;
		this.rightKeyColumn = rightKeyColumn;
		this.defaultRightLine = defaultRightLine;
		this.keyType = keyType;
	}

}
