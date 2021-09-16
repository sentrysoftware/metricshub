package com.sentrysoftware.matrix.common.helpers;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LoggerHelper {

	private LoggerHelper() {

	}

	private static final Set<String> NO_LOGGING_STRINGS = Set.of("PASS", "TOKEN");

	public static boolean canBeLogged(String string) {

		if (string == null || string.isBlank()) {
			return true;
		}

		for (String noLoggingString : NO_LOGGING_STRINGS) {

			if (string.toUpperCase().contains(noLoggingString)) {
				return false;
			}
		}

		return true;
	}

	public static boolean canBeLogged(Collection<String> collection) {

		if (collection == null || collection.isEmpty()) {
			return true;
		}

		for (String item : collection) {

			if (!canBeLogged(item)) {
				return false;
			}
		}

		return true;
	}

	public static boolean canBeLogged(List<List<String>> table) {

		if (table == null || table.isEmpty()) {
			return true;
		}

		for (List<String> row : table) {

			if (!canBeLogged(row)) {
				return false;
			}
		}

		return true;
	}

	public static boolean canBeLogged(Map<String, String> map) {

		if (map == null || map.isEmpty()) {
			return true;
		}

		for (Map.Entry<String, String> entry : map.entrySet()) {

			if (!canBeLogged(entry.getKey()) || !canBeLogged(entry.getValue())) {
				return false;
			}
		}

		return true;
	}
}
