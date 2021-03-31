package com.sentrysoftware.matrix.connector.model.monitor.job.source.compute;

import java.util.List;
import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class XML2CSV implements Compute {

	private static final long serialVersionUID = 6561437878414249082L;

	private String recordTag;
	@Default
	private List<String> properties = new ArrayList<>();
	private String separator;
}
