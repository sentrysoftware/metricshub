package com.sentrysoftware.matrix.connector.model.monitor.job.source.type.ipmi;

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
public class IPMI extends Source {

	private static final long serialVersionUID = 2314585274202787684L;

	@Builder
	public IPMI(List<Compute> computes, boolean forceSerialization) {

		super(computes, forceSerialization);
	}

	
}
