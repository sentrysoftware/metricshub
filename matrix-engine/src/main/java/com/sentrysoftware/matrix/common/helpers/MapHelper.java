package com.sentrysoftware.matrix.common.helpers;

import java.util.Map;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MapHelper {

	/**
	 * Check if the given maps are deeply the same
	 *
	 * @param first
	 * @param second
	 * @return <code>true</code> if the maps are deeply equal otherwise <code>false</code>
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
