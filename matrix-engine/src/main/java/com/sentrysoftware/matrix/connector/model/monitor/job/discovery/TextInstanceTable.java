package com.sentrysoftware.matrix.connector.model.monitor.job.discovery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TextInstanceTable implements InstanceTable {

	private static final long serialVersionUID = -2445856307331794107L;

	private String text;
}
