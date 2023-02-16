package com.sentrysoftware.matrix.connector.model.metric;

import static com.fasterxml.jackson.annotation.Nulls.SKIP;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.EMPTY;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonSetter;

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
	@JsonSetter(nulls = SKIP)
	private String unit = EMPTY;

	@Default
	@JsonSetter(nulls = SKIP)
	private String description = EMPTY;

	@Default
	@JsonSetter(nulls = SKIP)
	private IMetricType type = MetricType.GAUGE;

}
