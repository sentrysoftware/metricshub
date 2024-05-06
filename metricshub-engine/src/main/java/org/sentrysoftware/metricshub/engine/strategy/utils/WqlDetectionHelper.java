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

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.sentrysoftware.metricshub.engine.strategy.detection.CriterionTestResult;
import org.sentrysoftware.wbem.javax.wbem.WBEMException;

import java.util.Set;

/**
 * Helper class for WBEM/WMI namespace detection using WQL queries.
 * This class provides methods to find possible namespaces, detect suitable namespaces,
 * and perform WQL detection tests.
 */
@Slf4j
public class WqlDetectionHelper {

	/**
	 * Assess whether an exception (or any of its causes) is simply an error saying that the
	 * requested namespace of class doesn't exist, which is considered okay.
	 * <br>
	 *
	 * @param t Exception to verify
	 * @return whether specified exception is acceptable while performing namespace detection
	 */
	public static boolean isAcceptableException(Throwable t) {
		if (t == null) {
			return false;
		}

		if (t instanceof WBEMException wbemException) {
			final int cimErrorType = wbemException.getID();
			return isAcceptableWbemError(cimErrorType);
		} else if (
			// CHECKSTYLE:OFF
			t instanceof org.sentrysoftware.wbem.client.exceptions.WqlQuerySyntaxException ||
			t instanceof org.sentrysoftware.winrm.exceptions.WqlQuerySyntaxException
			// CHECKSTYLE:ON
		) {
			return true;
		}

		// Now check recursively the cause
		return isAcceptableException(t.getCause());
	}

	/**
	 * Whether this error id is an acceptable WBEM error.
	 *
	 * @param errorId integer value representing the id of the WBEM exception
	 * @return boolean value
	 */
	private static boolean isAcceptableWbemError(final int errorId) {
		// CHECKSTYLE:OFF
		return (
			errorId == WBEMException.CIM_ERR_INVALID_NAMESPACE ||
			errorId == WBEMException.CIM_ERR_INVALID_CLASS ||
			errorId == WBEMException.CIM_ERR_NOT_FOUND
		);
		// CHECKSTYLE:ON
	}

	/**
	 * Data class representing the result of querying for possible namespaces.
	 * Provides information about the possible namespaces, success status, and an error message if applicable.
	 */
	@Data
	@Builder
	public static class PossibleNamespacesResult {

		private Set<String> possibleNamespaces;
		private boolean success;
		private String errorMessage;
	}

	/**
	 * Data class representing the result for a specific namespace.
	 * Contains information about the namespace itself and a CriterionTestResult.
	 */
	@Data
	@Builder
	public static class NamespaceResult {

		private String namespace;
		private CriterionTestResult result;
	}
}
