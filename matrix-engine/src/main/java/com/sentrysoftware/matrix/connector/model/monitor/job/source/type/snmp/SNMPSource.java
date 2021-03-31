package com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp;

import java.util.List;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Compute;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class SNMPSource extends Source {

	private static final long serialVersionUID = 4987906836798379805L;

	private String oid;

	public SNMPSource(List<Compute> computes, boolean forceSerialization, String oid) {

		super(computes, forceSerialization);
		this.oid = oid;
	}
}
