package com.sentrysoftware.metricshub.engine.awk;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import org.sentrysoftware.jawk.ExitException;
import org.sentrysoftware.jawk.backend.AVM;
import org.sentrysoftware.jawk.frontend.AwkParser;
import org.sentrysoftware.jawk.frontend.AwkSyntaxTree;
import org.sentrysoftware.jawk.util.AwkSettings;
import org.sentrysoftware.jawk.util.ScriptSource;
import org.sentrysoftware.jawk.intermediate.AwkTuples;

public class Awk {

	/**
	 * Generates the "Awk Tuples", i.e. the intermediate Awk code that can be
	 * interpreted afterward.
	 *
	 * @param script Awk script source code to be converted to intermediate code
	 * @return The actual AwkTuples to be interpreted
	 * @throws ParseException when the Awk script is wrong
	 */
	public static AwkTuples getIntermediateCode(String script) throws ParseException {
		// All scripts need to be prefixed with an extra statement that sets the Record
		// Separator (RS)
		// to the "normal" end-of-line (\n), because Jawk uses line.separator System
		// property, which
		// is \r\n on Windows, thus preventing it from splitting lines properly.
		ScriptSource awkHeader = new ScriptSource("Header", new StringReader("BEGIN { RS = \"\\n\"; }"), false);
		ScriptSource awkSource = new ScriptSource("Body", new StringReader(script), false);
		ArrayList<ScriptSource> sourceList = new ArrayList<ScriptSource>();
		sourceList.add(awkHeader);
		sourceList.add(awkSource);

		// Awk Setup
		AwkSettings settings = new AwkSettings();
		settings.setCatchIllegalFormatExceptions(false);
		settings.setUseStdIn(false);

		// Parse the Awk script
		AwkTuples tuples = new AwkTuples();
		AwkParser parser = new AwkParser(false, false, false, Collections.emptyMap());
		AwkSyntaxTree ast;
		try {
			ast = parser.parse(sourceList);

			// Produce the intermediate code
			if (ast != null) {
				ast.semanticAnalysis();
				ast.semanticAnalysis();
				if (ast.populateTuples(tuples) != 0) {
					throw new ParseException("Syntax problem with the Awk script", 0);
				}
				tuples.postProcess();
				parser.populateGlobalVariableNameToOffsetMappings(tuples);
			}
		} catch (IOException e) {
			throw new ParseException(e.getMessage(), 0);
		} catch (Exception e) {
			throw new ParseException(e.getMessage(), 0);
		}

		return tuples;
	}

	/**
	 * Interprets the specified Awk intermediate code against an input string
	 *
	 * @param input            The text input to be parsed by the Awk script
	 * @param intermediateCode The Awk intermediate code
	 * @return The result of the Awk script (i.e. what has been printed by the
	 *         script)
	 * @throws RuntimeException when something goes wrong with the interpretation of
	 *                          the code
	 */
	public static String interpret(String input, AwkTuples intermediateCode) throws RuntimeException {
		// Configure the InputStream
		AwkSettings settings = new AwkSettings();
		try {
			settings.setInput(new ByteArrayInputStream(input.getBytes("ISO-8859-1"))); // UTF-8")));
		} catch (UnsupportedEncodingException e) {
			// Impossible
			return null;
		}
		// settings.setFieldSeparator(" +");

		// Create the OutputStream
		ByteArrayOutputStream resultBytesStream = new ByteArrayOutputStream();
		PrintStream resultStream = new PrintStream(resultBytesStream);
		settings.setOutputStream(resultStream);

		// We don't want to see error messages because of formatting issues
		settings.setCatchIllegalFormatExceptions(true);

		// We force \n as the Record Separator (RS) because even if running on Windows
		// we're passing Java strings, where end of lines are simple \n
		settings.setDefaultRS("\n");

		// Interpret
		AVM avm = new AVM(settings, Collections.emptyMap());
		try {
			avm.interpret(intermediateCode);
		} catch (ExitException e) {
			throw new RuntimeException(e.getMessage());
		}

		// Result
		return resultBytesStream.toString();
	}

	/**
	 * Interprets the specified Awk script against an input string
	 *
	 * @param input  The text input to be parsed by the Awk script
	 * @param script The Awk script to interpret
	 * @return The result of the Awk script (i.e. what has been printed by the
	 *         script)
	 * @throws ParseException   when the Awk script is wrong
	 * @throws RuntimeException when something goes wrong with the interpretation of
	 *                          the code
	 */
	public static String interpret(String input, String script) throws RuntimeException, ParseException {
		// Get the intermediate code
		AwkTuples intermediateCode = getIntermediateCode(script);

		// Interpret
		return interpret(input, intermediateCode);
	}
}
