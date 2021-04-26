package com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp;

import java.util.ArrayList;
import java.util.List;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Compute;
import com.sentrysoftware.matrix.engine.strategy.source.ISourceVisitor;
import com.sentrysoftware.matrix.engine.strategy.source.SourceTable;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SNMPGetTableSource extends SNMPSource {

	private static final long serialVersionUID = -8516718446207060520L;

	private List<String> snmpTableSelectColumns = new ArrayList<>();

	@Builder
	public SNMPGetTableSource(List<Compute> computes, boolean forceSerialization, String oid,
			List<String> snmpTableSelectColumns, int index, String key) {

		super(computes, forceSerialization, oid, index, key);
		this.snmpTableSelectColumns = snmpTableSelectColumns;
	}

	@Override
	public SourceTable accept(ISourceVisitor sourceVisitor) {
		return sourceVisitor.visit(this);
	}

}
