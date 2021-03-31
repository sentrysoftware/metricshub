package com.sentrysoftware.matrix.connector.model.monitor.job.source.compute;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExtractPropertyFromWbemPath implements Compute {

	private static final long serialVersionUID = -1223955587166569350L;

	private String propertyName;
}
