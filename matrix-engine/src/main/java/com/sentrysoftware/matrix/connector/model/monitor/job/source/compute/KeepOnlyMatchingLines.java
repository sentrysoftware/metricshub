package com.sentrysoftware.matrix.connector.model.monitor.job.source.compute;

import java.util.List;
import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KeepOnlyMatchingLines implements Compute {

	private static final long serialVersionUID = 5853378552607445344L;

	private Integer column;
	private String regExp;
	@Default
	private List<String> valueList = new ArrayList<>();
}
