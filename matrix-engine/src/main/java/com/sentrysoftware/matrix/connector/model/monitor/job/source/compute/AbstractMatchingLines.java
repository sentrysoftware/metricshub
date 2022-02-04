package com.sentrysoftware.matrix.connector.model.monitor.job.source.compute;

import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractMatchingLines extends Compute {

	private static final long serialVersionUID = 3125914016586965365L;

	private Integer column;
	private String regExp;
	private Set<String> valueSet = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

	protected AbstractMatchingLines(Integer index, Integer column, String regExp, Set<String> valueSet) {

		super(index);

		this.column = column;
		this.regExp = regExp;
		this.valueSet = valueSet == null ? new TreeSet<>(String.CASE_INSENSITIVE_ORDER) : valueSet;
	}

	@Override
	public String toString() {

		final StringJoiner stringJoiner = new StringJoiner(HardwareConstants.NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- column=", column);
		addNonNull(stringJoiner, "- regExp=", regExp);
		addNonNull(stringJoiner, "- valueSet=", valueSet);

		return stringJoiner.toString();
	}
}
