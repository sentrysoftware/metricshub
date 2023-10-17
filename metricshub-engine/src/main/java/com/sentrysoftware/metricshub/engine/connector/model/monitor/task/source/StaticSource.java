package com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source;

import static com.fasterxml.jackson.annotation.Nulls.FAIL;
import static com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.NEW_LINE;
import static com.sentrysoftware.metricshub.engine.common.helpers.StringHelper.addNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.sentrysoftware.metricshub.engine.connector.model.common.ExecuteForEachEntryOf;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Compute;
import com.sentrysoftware.metricshub.engine.strategy.source.ISourceProcessor;
import com.sentrysoftware.metricshub.engine.strategy.source.SourceTable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StaticSource extends Source {

	private static final long serialVersionUID = 1L;

	@NonNull
	@JsonSetter(nulls = FAIL)
	private String value;

	@Builder
	@JsonCreator
	public StaticSource(
		@JsonProperty("type") String type,
		@JsonProperty("computes") List<Compute> computes,
		@JsonProperty("forceSerialization") boolean forceSerialization,
		@JsonProperty(value = "value", required = true) @NonNull String value,
		@JsonProperty("key") String key,
		@JsonProperty("executeForEachEntryOf") ExecuteForEachEntryOf executeForEachEntryOf
	) {
		super("static", computes, forceSerialization, key, executeForEachEntryOf);
		this.value = value;
	}

	public StaticSource copy() {
		return StaticSource
			.builder()
			.type(type)
			.key(key)
			.forceSerialization(forceSerialization)
			.computes(getComputes() != null ? new ArrayList<>(getComputes()) : null)
			.executeForEachEntryOf(executeForEachEntryOf != null ? executeForEachEntryOf.copy() : null)
			.value(value)
			.build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		value = updater.apply(value);
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- value=", value);

		return stringJoiner.toString();
	}

	@Override
	public SourceTable accept(final ISourceProcessor sourceProcessor) {
		return sourceProcessor.process(this);
	}
}
