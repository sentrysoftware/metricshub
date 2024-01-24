package org.sentrysoftware.metricshub.engine.strategy.utils;

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

import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.sentrysoftware.metricshub.engine.connector.model.Connector;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DetectionHelper {

	/**
	 * Checks whether the given includeConnectorTags set defined in HostConfiguration or defined using the CLI contains at least one of a given connector's tags
	 * @param includeConnectorTags tags defined by the user and stored in HostConfiguration
	 * @param connector a given connector
	 * @return boolean
	 */
	public static boolean hasAtLeastOneTagOf(final Set<String> includeConnectorTags, final Connector connector) {
		if (includeConnectorTags == null || includeConnectorTags.isEmpty()) {
			return true;
		}
		final Set<String> connectorTags = connector.getConnectorIdentity().getDetection().getTags();
		if (connectorTags == null) {
			return false;
		}
		return connectorTags.stream().anyMatch(tag -> includeConnectorTags.contains(tag));
	}
}
