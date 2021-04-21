package com.sentrysoftware.matrix.connector.model.monitor.job.source.type.reference;

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
public class ReferenceSource extends Source {

	private static final long serialVersionUID = -4192645639386266586L;

	private Source reference;

	@Builder
	public ReferenceSource(List<Compute> computes, boolean forceSerialization, Source reference, int index) {

		super(computes, forceSerialization, index);
		this.reference = reference;
	}

	
}
