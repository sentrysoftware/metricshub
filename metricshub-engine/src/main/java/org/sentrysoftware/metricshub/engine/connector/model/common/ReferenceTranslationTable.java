package org.sentrysoftware.metricshub.engine.connector.model.common;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub Engine
 * ჻჻჻჻჻჻
 * Copyright 2023 - 2024 Sentry Software
 * ჻჻჻჻჻჻
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱
 */

import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants;

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
