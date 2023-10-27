package com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute;

import static com.fasterxml.jackson.annotation.Nulls.FAIL;
import static com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.NEW_LINE;
import static com.sentrysoftware.metricshub.engine.common.helpers.StringHelper.addNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.metricshub.engine.connector.deserializer.custom.TranslationTableDeserializer;
import com.sentrysoftware.metricshub.engine.connector.model.common.ITranslationTable;
import com.sentrysoftware.metricshub.engine.strategy.source.compute.IComputeProcessor;
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
public class ArrayTranslate extends Compute {

	private static final long serialVersionUID = 1L;

	@NonNull
	@JsonSetter(nulls = FAIL)
	private Integer column;

	@NonNull
	@JsonSetter(nulls = FAIL)
	@JsonDeserialize(using = TranslationTableDeserializer.class)
	private ITranslationTable translationTable;

	private String arraySeparator;
	private String resultSeparator;

	@Builder
	@JsonCreator
	public ArrayTranslate(
		@JsonProperty("type") String type,
		@JsonProperty(value = "column", required = true) @NonNull Integer column,
		@JsonProperty(value = "translationTable", required = true) @NonNull ITranslationTable translationTable,
		@JsonProperty("arraySeparator") String arraySeparator,
		@JsonProperty("resultSeparator") String resultSeparator
	) {
		super(type);
		this.column = column;
		this.translationTable = translationTable;
		this.arraySeparator = arraySeparator;
		this.resultSeparator = resultSeparator;
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- column=", column);
		addNonNull(stringJoiner, "- translationTable=", translationTable);
		addNonNull(stringJoiner, "- arraySeparator=", arraySeparator);
		addNonNull(stringJoiner, "- resultSeparator=", resultSeparator);

		return stringJoiner.toString();
	}

	@Override
	public ArrayTranslate copy() {
		return ArrayTranslate
			.builder()
			.type(type)
			.column(column)
			.translationTable(translationTable)
			.arraySeparator(arraySeparator)
			.resultSeparator(resultSeparator)
			.build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		arraySeparator = updater.apply(arraySeparator);
		resultSeparator = updater.apply(resultSeparator);
		translationTable.update(updater);
	}

	@Override
	public void accept(IComputeProcessor computeProcessor) {
		computeProcessor.process(this);
	}
}