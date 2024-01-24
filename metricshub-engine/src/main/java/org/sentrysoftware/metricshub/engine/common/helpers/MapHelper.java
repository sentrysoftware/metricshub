package org.sentrysoftware.metricshub.engine.common.helpers;

import java.util.Map;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Helper class for operations related to maps.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MapHelper {

	/**
	 * Check if the given maps are deeply the same.
	 *
	 * @param first  The first map to compare.
	 * @param second The second map to compare.
	 * @return {@code true} if the maps are deeply equal, otherwise {@code false}.
	 */
	public static boolean areEqual(final Map<String, String> first, final Map<String, String> second) {
		if (Objects.equals(first, second)) {
			return true;
		}

		if (first == null || second == null) {
			return false;
		}

		if (first.size() != second.size()) {
			return false;
		}

		return first.entrySet().stream().allMatch(e -> e.getValue().equals(second.get(e.getKey())));
	}
}
