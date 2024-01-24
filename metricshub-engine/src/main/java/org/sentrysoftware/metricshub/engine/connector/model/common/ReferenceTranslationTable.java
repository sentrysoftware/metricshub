package org.sentrysoftware.metricshub.engine.connector.model.common;

import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants;

/**
 * Represents a translation table based on a reference in connector models.
 * It implements the {@link ITranslationTable} interface for mapping key-value pairs during data transformation.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReferenceTranslationTable implements ITranslationTable {

	private static final long serialVersionUID = 1L;

	/**
	 * The reference associated with the translation table.
	 */
	private String reference;

	/**
	 * The unique identifier for the translation table.
	 */
	private String tableId;

	/**
	 * Constructs a {@link ReferenceTranslationTable} with the given reference.
	 *
	 * @param reference The reference used to create the translation table.
	 */
	public ReferenceTranslationTable(final String reference) {
		this.reference = reference;
		final Matcher matcher = MetricsHubConstants.TRANSLATION_REF_PATTERN.matcher(reference);
		if (matcher.find()) {
			tableId = matcher.group(1);
		} else {
			tableId = reference;
		}
	}

	@Override
	public ReferenceTranslationTable copy() {
		return new ReferenceTranslationTable(reference);
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		reference = updater.apply(reference);
		tableId = updater.apply(tableId);
	}
}
