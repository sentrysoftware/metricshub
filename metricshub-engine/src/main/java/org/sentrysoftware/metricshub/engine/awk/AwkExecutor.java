package org.sentrysoftware.metricshub.engine.awk;

import java.text.ParseException;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.sentrysoftware.jawk.intermediate.AwkTuples;

/**
 * Execute AWK Scripts
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AwkExecutor {

	/**
	 * Map of the scripts that have already been transformed to intermediate code
	 */
	static ConcurrentHashMap<String, AwkTuples> awkCodeMap = new ConcurrentHashMap<>();

	/**
	 * Execute the given <code>awkScript</code> on the <code>awkInput</code>
	 *
	 * @param awkScript The AWK script we wish to process and interpret
	 * @param awkInput  The input we wish to modify via the AWK script
	 * @return String value
	 * @throws AwkException
	 */
	public static String executeAwk(final String awkScript, final String awkInput) throws AwkException {
		// We're using our ConcurrentHashMap to cache the intermediate
		// code, so we don't "compile" it every time.
		// This saves a lot of CPU.
		final AwkTuples tuples;
		try {
			tuples =
				awkCodeMap.computeIfAbsent(
					awkScript,
					code -> {
						try {
							return Awk.getIntermediateCode(code);
						} catch (ParseException e) {
							// Throw a RuntimeException so the e.getMessage() can be passed
							// through the call stack
							throw new RuntimeException(e.getMessage());
						}
					}
				);
		} catch (Exception e) {
			throw new AwkException("Failed to get intermediate code.", e);
		}

		if (tuples == null) {
			throw new AwkException("Failed to interpret the AWK script below:\n" + awkScript);
		}

		final String result = Awk.interpret(awkInput, tuples);

		if (result == null) {
			throw new AwkException(
				String.format("Null result for the script below on the specified input:\n%s\n\nInput:\n%s", awkScript, awkInput)
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
