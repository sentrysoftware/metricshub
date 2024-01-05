package com.sentrysoftware.metricshub.engine.awk;

import java.text.ParseException;
import java.util.concurrent.ConcurrentHashMap;
import org.sentrysoftware.jawk.intermediate.AwkTuples;

/**
 * Execute AWK Scripts
 */
public class AwkExecutor {

	/**
	 * Map of the scripts that have already been transformed to intermediate code
	 */
	static ConcurrentHashMap<String, AwkTuples> awkCodeMap = new ConcurrentHashMap<>();

	public static String executeAwk(final String awkScript, final String awkInput) throws AwkException {
		// We're using our ConcurrentHashMap to cache the intermediate
		// code, so we don't "compile" it every time.
		// This saves a lot of CPU.
		final AwkTuples tuples = awkCodeMap.computeIfAbsent(
			awkScript,
			code -> {
				try {
					return Awk.getIntermediateCode(code);
				} catch (ParseException e) {
					// Through a RuntimeException so the e.getMessage() can be passed
					// through the call stack
					throw new RuntimeException(e.getMessage());
				}
			}
		);

		if (tuples == null) {
			throw new AwkException("Failed to interpret the AWK script below:\n" + awkScript);
		}

		final String result = Awk.interpret(awkInput, tuples);

		if (result == null) {
			throw new AwkException(
				"null result for the script below on the specified input:\n" + awkScript + "\n\nInput:\n" + awkInput
			);
		}

		return result;
	}

	/**
	 * Clear the {@link ConcurrentHashMap} <code>awkCodeMap</code>
	 */
	public static void resetCache() {
		awkCodeMap.clear();
	}
}
