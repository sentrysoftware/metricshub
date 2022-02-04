package com.sentrysoftware.matrix.connector.model.monitor.job.source;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.EMPTY;
import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;
import static org.springframework.util.Assert.isTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.connector.model.common.EntryConcatMethod;
import com.sentrysoftware.matrix.connector.model.common.ExecuteForEachEntry;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Compute;
import com.sentrysoftware.matrix.engine.strategy.source.ISourceVisitor;
import com.sentrysoftware.matrix.engine.strategy.source.SourceTable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public abstract class Source implements Serializable {

	private static final long serialVersionUID = 4765209445308968001L;

	private List<Compute> computes;
	protected boolean forceSerialization;
	protected Integer index;
	protected String key;
	protected ExecuteForEachEntry executeForEachEntry;

	protected Source(List<Compute> computes, boolean forceSerialization, Integer index, String key,
			ExecuteForEachEntry executeForEachEntry) {

		this.computes = computes == null ? new ArrayList<>() : computes;
		this.forceSerialization = forceSerialization;
		this.index = index;
		this.key = key;
		this.executeForEachEntry = executeForEachEntry;
	}

	public void setIndex(int index) {

		isTrue(index > 0, "Invalid index: " + index);
		this.index = index;
	}

	public abstract SourceTable accept(final ISourceVisitor sourceVisitor);

	public abstract Source copy();

	public abstract void update(UnaryOperator<String> updater);

	/**
	 * Get the {@link ExecuteForEachEntry} instance from the current {@link Source} or
	 * create a new one
	 * 
	 * @return {@link ExecuteForEachEntry} instance. Never <code>null</code>
	 */
	private ExecuteForEachEntry getOrCreateExecuteForEachEntry() {

		// Create a new instance of ExecuteForEachEntry if it is missing
		if (executeForEachEntry == null) {
			executeForEachEntry = new ExecuteForEachEntry();
		}

		return executeForEachEntry;
	}

	@Override
	public String toString() {

		final StringJoiner stringJoiner = new StringJoiner(HardwareConstants.NEW_LINE);

		stringJoiner.add(new StringBuilder("- ").append(key).append(".type=").append(this.getClass().getSimpleName()));

		addNonNull(stringJoiner, "- index=", index);
		addNonNull(stringJoiner, "- forceSerialization=", forceSerialization);
		// A small trick here because the executeForEachEntry.toString value is already
		// formatted that's why we don't need a prefix for the string value of the nested executeForEachEntry
		addNonNull(stringJoiner, EMPTY, executeForEachEntry != null ? executeForEachEntry.toString() : null);

		return stringJoiner.toString();

	}

	/**
	 * Set the {@link EntryConcatMethod} value
	 * 
	 * @param entryConcatMethod
	 */
	public void setEntryConcatMethod(final EntryConcatMethod entryConcatMethod) {
		getOrCreateExecuteForEachEntry().setEntryConcatMethod(entryConcatMethod);
	}

	/**
	 * Set the entryConcatStart string value
	 * 
	 * @param entryConcatStart
	 */
	public void setEntryConcatStart(final String entryConcatStart) {
		getOrCreateExecuteForEachEntry().setEntryConcatStart(entryConcatStart);
	}

	/**
	 * Set the entryConcatEnd string value
	 * 
	 * @param entryConcatEnd
	 */
	public void setEntryConcatEnd(final String entryConcatEnd) {
		getOrCreateExecuteForEachEntry().setEntryConcatEnd(entryConcatEnd);
	}

	/**
	 * Set the executeForEachEntryOf string value
	 * 
	 * @param executeForEachEntryOf
	 */
	public void setExecuteForEachEntryOf(final String executeForEachEntryOf) {
		getOrCreateExecuteForEachEntry().setExecuteForEachEntryOf(executeForEachEntryOf);
	}

	/**
	 * Get the {@link EntryConcatMethod} value
	 * 
	 * @return {@link EntryConcatMethod} enum value
	 */
	public EntryConcatMethod getEntryConcatMethod() {
		return executeForEachEntry != null ? executeForEachEntry.getEntryConcatMethod() : null;
	}

	/**
	 * Get the entryConcatStart string value
	 * 
	 * @return String value
	 */
	public String getEntryConcatStart() {
		return executeForEachEntry != null ? executeForEachEntry.getEntryConcatStart() : null;
	}

	/**
	 * Get the entryConcatEnd string value
	 * 
	 * @return String value
	 */
	public String getEntryConcatEnd() {
		return executeForEachEntry != null ? executeForEachEntry.getEntryConcatEnd() : null;
	}

	/**
	 * Get the executeForEachEntryOf string value
	 * 
	 * @return String value
	 */
	public String getExecuteForEachEntryOf() {
		return executeForEachEntry != null ? executeForEachEntry.getExecuteForEachEntryOf() : null;
	}

	/**
	 * Whether the {@link ExecuteForEachEntry} is present in the {@link Source} or
	 * not
	 * 
	 * @return <code>true</code> if {@link ExecuteForEachEntry} is present otherwise
	 *         <code>false</code>
	 */
	public boolean isExecuteForEachEntry() {
		return executeForEachEntry != null && executeForEachEntry.getExecuteForEachEntryOf() != null
				&& !executeForEachEntry.getExecuteForEachEntryOf().isBlank();
	}
}
