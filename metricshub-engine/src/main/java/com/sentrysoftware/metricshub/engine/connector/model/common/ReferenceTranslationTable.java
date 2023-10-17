package com.sentrysoftware.metricshub.engine.connector.model.common;

import com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReferenceTranslationTable implements ITranslationTable {

	private static final long serialVersionUID = 1L;

	private String reference;

	private String tableId;

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
