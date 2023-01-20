package com.sentrysoftware.matrix.connector.model.monitor.task.source;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.EMPTY;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.NEW_LINE;
import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;

import com.sentrysoftware.matrix.connector.model.common.ExecuteForEachEntry;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Compute;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public abstract class Source implements Serializable {

	private static final long serialVersionUID = 1L;

	protected String type;
	private List<Compute> computes;
	protected boolean forceSerialization;
	protected String key;
	protected ExecuteForEachEntry executeForEachEntry;

	protected Source(
		String type,
		List<Compute> computes,
		boolean forceSerialization,
		String key,
		ExecuteForEachEntry executeForEachEntry
	) {

		this.type = type;
		this.computes = computes == null ? new ArrayList<>() : computes;
		this.forceSerialization = forceSerialization;
		this.key = key;
		this.executeForEachEntry = executeForEachEntry;
	}

	public abstract Source copy();

	public abstract void update(UnaryOperator<String> updater);

	@Override
	public String toString() {

		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

		stringJoiner.add(new StringBuilder("- ").append(key).append(".type=").append(this.getClass().getSimpleName()));

		addNonNull(stringJoiner, "- forceSerialization=", forceSerialization);
		// A small trick here because the executeForEachEntry.toString value is already
		// formatted that's why we don't need a prefix for the string value of the nested executeForEachEntry
		addNonNull(stringJoiner, EMPTY, executeForEachEntry != null ? executeForEachEntry.toString() : null);

		return stringJoiner.toString();

	}

	/**
	 * Whether the {@link ExecuteForEachEntry} is present in the {@link Source} or
	 * not
	 * 
	 * @return <code>true</code> if {@link ExecuteForEachEntry} is present otherwise
	 *         <code>false</code>
	 */
	public boolean isExecuteForEachEntry() {
		return executeForEachEntry != null && executeForEachEntry.getOf() != null
				&& !executeForEachEntry.getOf().isBlank();
	}
}
