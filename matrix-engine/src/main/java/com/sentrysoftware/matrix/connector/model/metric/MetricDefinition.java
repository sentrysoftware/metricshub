package com.sentrysoftware.matrix.connector.model.metric;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.EMPTY;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MetricDefinition implements Serializable {

	private static final long serialVersionUID = 1L;

	@Default
	private String unit = EMPTY;

	@Default
	private String desciption = EMPTY;

	@Default
	private IMetricType type = MetricType.GAUGE;

}
