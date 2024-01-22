package org.sentrysoftware.metricshub.engine.connector.model.common;

import java.io.Serializable;
import java.util.function.UnaryOperator;

public interface ITranslationTable extends Serializable {
	/**
	 * Copy the {@link ITranslationTable} instance
	 *
	 * @return {@link ITranslationTable} deep copy
	 */
	ITranslationTable copy();

	/**
	 * Update the given translation table
	 *
	 * @param updater An operation on a single operand that produces a result of the
	 *                same type as its operand.
	 */
	void update(UnaryOperator<String> updater);
}
