package com.sentrysoftware.matrix.connector.model.monitor.task.source.compute;

import static com.fasterxml.jackson.annotation.Nulls.FAIL;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.NEW_LINE;
import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonSetter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractMatchingLines extends Compute {

	private static final long serialVersionUID = 1L;

	@NonNull
	@JsonSetter(nulls = FAIL)
	protected Integer column;

	protected String regExp;
	protected Set<String> valueList = new TreeSet<>(String.CASE_INSENSITIVE_ORDER); // NOSONAR TreeSet is Serializable

	protected AbstractMatchingLines(String type, Integer column, String regExp, Set<String> valueList) {

		super(type);

		this.column = column;
		this.regExp = regExp;
		this.valueList = valueList == null ? new TreeSet<>(String.CASE_INSENSITIVE_ORDER) : valueList;
	}

	@Override
	public String toString() {

		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- column=", column);
		addNonNull(stringJoiner, "- regExp=", regExp);
		addNonNull(stringJoiner, "- valueList=", valueList);

		return stringJoiner.toString();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		regExp = updater.apply(regExp);
		if (valueList != null && !valueList.isEmpty()) {
			valueList = valueList
				.stream()
				.map(updater::apply)
				.collect(Collectors
					.toCollection(() -> new TreeSet<>(String.CASE_INSENSITIVE_ORDER))
				);
		}
	}
}
