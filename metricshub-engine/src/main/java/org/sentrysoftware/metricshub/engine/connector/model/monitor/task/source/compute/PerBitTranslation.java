package org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute;

import static com.fasterxml.jackson.annotation.Nulls.FAIL;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.NEW_LINE;
import static org.sentrysoftware.metricshub.engine.common.helpers.StringHelper.addNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.sentrysoftware.metricshub.engine.connector.deserializer.custom.TranslationTableDeserializer;
import org.sentrysoftware.metricshub.engine.connector.model.common.ITranslationTable;
import org.sentrysoftware.metricshub.engine.strategy.source.compute.IComputeProcessor;

/**
 * Represents a PerBitTranslation computation task for monitoring.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PerBitTranslation extends Compute {

	private static final long serialVersionUID = 1L;

	/**
	 * The column index used in the PerBitTranslation computation.
	 */
	@NonNull
	@JsonSetter(nulls = FAIL)
	private Integer column;

	/**
	 * The bit list associated with the PerBitTranslation task.
	 */
	@NonNull
	@JsonSetter(nulls = FAIL)
	private String bitList;

	/**
	 * The translation table used for the PerBitTranslation computation.
	 */
	@NonNull
	@JsonSetter(nulls = FAIL)
	@JsonDeserialize(using = TranslationTableDeserializer.class)
	private ITranslationTable translationTable;

	/**
	 * PerBitTranslation constructor using the Builder pattern.
	 *
	 * @param type             The type of the computation task.
	 * @param column           The column index used in the computation.
	 * @param bitList          The bit list associated with the PerBitTranslation task.
	 * @param translationTable The translation table used for the PerBitTranslation computation.
	 */
	@Builder
	@JsonCreator
	public PerBitTranslation(
		@JsonProperty("type") String type,
		@JsonProperty(value = "column", required = true) @NonNull Integer column,
		@JsonProperty(value = "bitList", required = true) @NonNull String bitList,
		@JsonProperty(value = "translationTable", required = true) @NonNull ITranslationTable translationTable
	) {
		super(type);
		this.column = column;
		this.bitList = bitList;
		this.translationTable = translationTable;
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- column=", column);
		addNonNull(stringJoiner, "- bitList=", bitList);
		addNonNull(stringJoiner, "- translationTable=", translationTable);

		return stringJoiner.toString();
	}

	@Override
	public PerBitTranslation copy() {
		return PerBitTranslation
			.builder()
			.type(type)
			.column(column)
			.bitList(bitList)
			.translationTable(translationTable)
			.build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		translationTable.update(updater);
	}

	@Override
	public void accept(IComputeProcessor computeProcessor) {
		computeProcessor.process(this);
	}
}
