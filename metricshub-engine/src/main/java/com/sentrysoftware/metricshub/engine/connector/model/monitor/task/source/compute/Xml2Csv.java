package com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute;

import static com.fasterxml.jackson.annotation.Nulls.SKIP;
import static com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.NEW_LINE;
import static com.sentrysoftware.metricshub.engine.common.helpers.StringHelper.addNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.sentrysoftware.metricshub.engine.strategy.source.compute.IComputeProcessor;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Xml2Csv extends Compute {

	private static final long serialVersionUID = 1L;

	@JsonSetter(nulls = SKIP)
	private String recordTag = "/";

	private String properties;

	@Builder
	@JsonCreator
	public Xml2Csv(
		@JsonProperty("type") String type,
		@JsonProperty("recordTag") String recordTag,
		@JsonProperty("properties") String properties
	) {
		super(type);
		this.recordTag = recordTag == null ? "/" : recordTag;
		this.properties = properties;
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- recordTag=", recordTag);
		addNonNull(stringJoiner, "- properties=", properties);

		return stringJoiner.toString();
	}

	@Override
	public Xml2Csv copy() {
		return Xml2Csv.builder().type(type).recordTag(recordTag).properties(properties).build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		recordTag = updater.apply(recordTag);
		properties = updater.apply(properties);
	}

	@Override
	public void accept(IComputeProcessor computeProcessor) {
		computeProcessor.process(this);
	}
}