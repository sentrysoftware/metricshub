package com.sentrysoftware.matrix.connector.model.monitor.job.source.compute;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExcludeMatchingLines implements Compute {

	private static final long serialVersionUID = -3662198961800729320L;

	private Integer column;
	private String regExp;
	@Default
	private List<String> valueList = new ArrayList<>();
}
