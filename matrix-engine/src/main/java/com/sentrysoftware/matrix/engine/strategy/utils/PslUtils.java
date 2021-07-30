package com.sentrysoftware.matrix.engine.strategy.utils;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.CARET;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.CLOSING_SQUARE_BRACKET;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.COMMA;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DOUBLE_BACKSLASH;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.EMPTY;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.NEW_LINE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.OPENING_PARENTHESIS;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.OPENING_SQUARE_BRACKET;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PLUS;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.WHITE_SPACE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.engine.strategy.source.SourceTable;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PslUtils {

	private static final String SPECIAL_CHARACTERS = "^$.*+?[]\\";
	private static final String BACKSLASH_B = "\\b";
	private static final String SEPARATORS_SPECIAL_CHARACTERS = "([()\\[\\]{}\\\\^\\-$|?*+.])";
	private static final String ESCAPED_FIRST_MATCHING_GROUP = "\\\\$1";
	private static final String FIRST_MATCHING_GROUP = "$1";
	private static final String CLOSING_OPENING_PARENTHESIS = ")(";
	private static final String CLOSING_PARENTHESIS_PLUS = ")+";
	private static final char BACKSLASH_CHAR = '\\';
	private static final char OPENING_PARENTHESIS_CHAR = '(';
	private static final char CLOSING_PARENTHESIS_CHAR = ')';
	private static final char PIPE_CHAR = '|';
	private static final char OPENING_CURLY_BRACKET_CHAR = '{';
	private static final char CLOSING_CURLY_BRACKET_CHAR = '}';
	private static final char OPENING_SQUARE_BRACKET_CHAR = '[';
	private static final char CLOSING_SQUARE_BRACKET_CHAR = ']';
	private static final char LOWER_THAN_CHAR = '<';
	private static final char GREATER_THAN_CHAR = '>';
	private static final char DASH_CHAR = '-';

	private PslUtils() { }

	/**
	 * Converts a PSL regex into its Java equivalent.
	 * Method shamelessly taken from somewhere else.
	 * <p>
	 * @param pslRegex Regular expression as used in PSL's grep() function.
	 * @return Regular expression that can be used in Java's Pattern.compile.
	 */
	public static String psl2JavaRegex(String pslRegex) {

		if (pslRegex == null || pslRegex.isEmpty()) {
			return EMPTY;
		}

		// We 're going to build the regex char by char
		StringBuilder javaRegex = new StringBuilder();

		// Parse the PSL regex char by char (the very last char will be added unconditionally)
		int i;
		boolean inRange = false;
		for (i = 0; i < pslRegex.length(); i++) {

			char c = pslRegex.charAt(i);

			if (c == BACKSLASH_CHAR && i < pslRegex.length() - 1) {

				i = handleBackSlash(pslRegex, inRange, i, javaRegex);

			} else if (c == OPENING_PARENTHESIS_CHAR || c == CLOSING_PARENTHESIS_CHAR || c == PIPE_CHAR
				|| c == OPENING_CURLY_BRACKET_CHAR || c == CLOSING_CURLY_BRACKET_CHAR) {

				javaRegex.append(BACKSLASH_CHAR).append(c);

			} else if (c == OPENING_SQUARE_BRACKET_CHAR) {

				// Regex ranges have a different escaping system, so we need to
				// know when we're inside a [a-z] range or not
				javaRegex.append(c);
				inRange = true;

			} else if (c == CLOSING_SQUARE_BRACKET_CHAR) {

				// Getting out of a [] range
				javaRegex.append(c);
				inRange = false;

			} else {

				// Other cases
				javaRegex.append(c);
			}
		}

		return javaRegex.toString();
	}

	/**
	 * Properly converts a backslash (present in the given PSL regular expression)
	 * to its Java regular expression version,
	 * and appends the result to the given Java regular expression {@link StringBuilder}.
	 *
	 * @param pslRegex	Regular expression as used in PSL's grep() function.
	 * @param inRange	Indicate whether the backslash is within a range (i.e. between '[' and ']').
	 * @param i			The index of the backslash in the regular expression.
	 * @param javaRegex	The {@link StringBuilder} holding the resulting Java regular expression.
	 *
	 * @return			The new value of the index
	 */
	private static int handleBackSlash(String pslRegex, boolean inRange, int i, StringBuilder javaRegex) {

		int result = i;

		if (inRange) {

			// Escape works differently in [] ranges
			// We simply need to double backslashes
			javaRegex.append(DOUBLE_BACKSLASH);

		} else {

			// Backslashes in PSL regex are utterly broken
			// We need to handle all cases here to convert to proper regex in Java
			char nextChar = pslRegex.charAt(i + 1);
			if (nextChar == LOWER_THAN_CHAR || nextChar == GREATER_THAN_CHAR) {

				// Replace \< and \> with \b
				javaRegex.append(BACKSLASH_B);
				result++;

			} else if (SPECIAL_CHARACTERS.indexOf(nextChar) > -1) {

				// Append the backslash and what it's protecting, as is
				javaRegex.append(BACKSLASH_CHAR).append(nextChar);
				result++;

			} else {

				// Append the next character
				javaRegex.append(nextChar);
				result++;
			}
		}

		return result;
	}

	/**
	 * @param text				The text that should be parsed.
	 * @param selectColumns		The list/range(s) of columns that should be extracted from the text.
	 * @param separators		The set of characters used to split the given text.
	 * @param resultSeparator	The separator used to join the resulting elements.
	 *
	 * @return					The nth group in the given text,
	 * 							as formatted according to the given separators and column numbers.
	 */
	public static String nthArgf(String text, String selectColumns, String separators, String resultSeparator) {

		return nthArgCommon(text, selectColumns, separators, resultSeparator, false);
	}

	/**
	 * @param text				The text that should be parsed.
	 * @param selectColumns		The list/range(s) of columns that should be extracted from the text.
	 * @param separators		The set of characters used to split the given text.
	 * @param resultSeparator	The separator used to join the resulting elements.
	 *
	 * @return					The nth group in the given text,
	 * 							as formatted according to the given separators and column numbers.
	 */
	public static String nthArg(String text, String selectColumns, String separators, String resultSeparator) {

		return nthArgCommon(text, selectColumns, separators, resultSeparator, true);
	}

	/**
	 * @param text				The text that should be parsed.
	 * @param selectColumns		The list/range(s) of columns that should be extracted from the text.
	 * @param separators		The set of characters used to split the given text.
	 * @param resultSeparator	The separator used to join the resulting elements.
	 * @param isNthArg			Indicates whether an <em>nthArg</em> operation should be performed
	 *                          (as opposed to a <em>nthArgf</em> operation).
	 *
	 * @return					The nth group in the given text,
	 * 							as formatted according to the given separators and column numbers.
	 */
	private static String nthArgCommon(String text, String selectColumns, String separators, String resultSeparator,
									   boolean isNthArg) {

		// If any arg is null, then return empty String
		if (text == null || selectColumns == null || separators == null
			|| text.isEmpty() || selectColumns.isEmpty() || separators.isEmpty()) {

			return EMPTY;
		}

		// Replace special chars with their literal equivalents
		String separatorsRegExp = OPENING_SQUARE_BRACKET
			+ separators.replaceAll(SEPARATORS_SPECIAL_CHARACTERS, ESCAPED_FIRST_MATCHING_GROUP)
			+ CLOSING_SQUARE_BRACKET;

		if (isNthArg) {

			// Remove redundant separators
			text = text.replaceAll(OPENING_PARENTHESIS
				+ separatorsRegExp
				+ CLOSING_OPENING_PARENTHESIS
				+ separatorsRegExp
				+ CLOSING_PARENTHESIS_PLUS,
				FIRST_MATCHING_GROUP);

			// Remove leading separators
			text = text.replaceAll(CARET + separatorsRegExp + PLUS, EMPTY);
		}

		// Check the result separator
		if (resultSeparator == null) {
			resultSeparator = WHITE_SPACE;
		}

		// The list holding the final result
		List<String> finalResult = new ArrayList<>();

		// Split the text value using the new line separator
		String [] textArray = text.split(NEW_LINE);
		for (String line : textArray) {
			processText(line, selectColumns, separatorsRegExp, resultSeparator, finalResult, isNthArg);
		}

		// Alright, we did it!
		return finalResult.stream().collect(Collectors.joining(resultSeparator));
	}

	/**
	 * Process the given text value and update the finalResult {@link List}
	 * 
	 * @param text             The text we wish to process
	 * @param selectColumns    The columns to select. E.g. <em>1-2</em> <em>1,2,3</em> <em>1-</em> <em>-4</em>
	 * @param separatorsRegExp The separator used to split the text value
	 * @param resultSeparator  The separator of the final result
	 * @param finalResult      The final result as {@link List}
	 * @param isNthArg         Indicate whether we want ntharg or nthargf.
	 */
	static void processText(final String text,
			final String selectColumns,
			final String separatorsRegExp,
			final String resultSeparator,
			final List<String> finalResult,
			final boolean isNthArg) {

		// Split the input text into a String array thanks to the separatorsRegExp
		final String[] splitText = text.split(separatorsRegExp, -1);

		// The user can specify several columns.
		// Split the selectColumns into an array too.
		final String[] columnsArray = selectColumns.split(COMMA);

		// So, for each columns group requested
		String result = null;
		int[] columnsRange;
		int fromColumnNumber;
		int toColumnNumber;
		for (String columns : columnsArray) {

			// Get the columns range
			columnsRange = getColumnsRange(columns, splitText.length);
			fromColumnNumber = columnsRange[0];
			toColumnNumber = columnsRange[1];

			// If we have valid fromColumnNumber and toColumnNumber, then retrieve these columns
			// which are actually items in the splitText array
			if (fromColumnNumber > 0 && fromColumnNumber <= toColumnNumber) {

				result = Arrays
					.stream(splitText, fromColumnNumber - 1, toColumnNumber)
					.filter(value -> !isNthArg || !value.trim().isEmpty())
					.collect(Collectors.joining(resultSeparator));

				finalResult.add(result);
			}
		}
	}

	/**
	 * @param columns       The {@link String} denoting the range of columns, in one of the following forms:
	 *                      "m-n", "m-" or "-n".
	 * @param columnCount	The total number of columns.
	 *
	 * @return				A 2-element array A with:
	 * 						<ul>
	 * 							<li>A[0] being the start of the range, inclusive</li>
	 * 							<li>A[1] being the end of the range, inclusive</li>
	 * 						</ul>
	 */
	private static int[] getColumnsRange(String columns, int columnCount) {

		int fromColumnNumber;
		int toColumnNumber;

		try {

			int dashIndex = columns.indexOf(DASH_CHAR);
			int columnsLength = columns.length();

			// If it is a simple number, we'll retrieve only that column number
			if (dashIndex == -1) {

				fromColumnNumber = Integer.parseInt(columns);
				toColumnNumber = fromColumnNumber;
			}

			// If it is "-n", then we'll retrieve all columns til number n
			else if (dashIndex == 0) {

				fromColumnNumber = 1;
				toColumnNumber = Integer.parseInt(columns.substring(1));
			}

			// If it is "n-", then we'll retrieve all columns starting from n
			else if (dashIndex == columnsLength - 1) {

				fromColumnNumber = Integer.parseInt(columns.substring(0, columnsLength - 1));
				toColumnNumber = columnCount;
			}

			// Else, if it is "m-n", we'll retrieve all columns starting from m til n
			else {

				fromColumnNumber = Integer.parseInt(columns.substring(0, dashIndex));
				toColumnNumber = Integer.parseInt(columns.substring(dashIndex + 1));
				if (toColumnNumber > columnCount) {
					toColumnNumber = columnCount;
				}
			}

			if (fromColumnNumber > columnCount || toColumnNumber > columnCount) {

				log.warn("getColumnRange: Invalid range for a {}-length array: [{}-{}]",
					columnCount, fromColumnNumber, toColumnNumber);

				fromColumnNumber = 0;
				toColumnNumber = 0;
			}

		} catch (NumberFormatException e) {

			log.warn("getColumnRange: Could not determine the range denoted by {}: {}", columns, e.getMessage());

			fromColumnNumber = 0;
			toColumnNumber = 0;
		}

		return new int[]{fromColumnNumber, toColumnNumber};
	}

	/**
	 * Converts an entry and its result into an extended JSON format:
	 * 	{
	 * 		"Entry":{
	 * 			"Full":"<entry>",
	 * 			"Column(1)":"<1st field value>",
	 * 	    	"Column(2)":"<2nd field value>",
	 * 			"Column(3)":"<3rd field value>",
	 * 			"Value":<result> <- Result must be properly formatted (either "result" or {"property":"value"}
	 * 		}
	 * 	}
	 *
	 * @param entry The row of values.
	 * @param tableResult The output returned by the SourceVisitor.
	 * @return
	 */
	public static String formatExtendedJSON(@NonNull String row, @NonNull SourceTable tableResult)
			throws IllegalArgumentException{
		if (row.isEmpty()) {
			throw new IllegalArgumentException("Empty row of values");
		}

		String rawData = tableResult.getRawData();
		if (rawData == null || rawData.isEmpty()) {
			throw new IllegalArgumentException("Empty SourceTable data: " + tableResult);
		}

		StringBuilder jsonContent = new StringBuilder();
		jsonContent.append("{\n\"Entry\":{\n\"Full\":\"")
		.append(row)
		.append("\",\n");

		int i = 1;

		for (String value : row.split(",")) {
			jsonContent
			.append("\"Column(")
			.append(i)
			.append(")\":\"")
			.append(value)
			.append("\",\n");
			i++;
		}

		jsonContent.append("\"Value\":").append(tableResult.getRawData()).append("\n}\n}");

		return jsonContent.toString();
	}

	/**
	 * Process what we got from the IPMI WMI provider and return a pretty table.
	 * @param wmiComputerSystem
	 * @param wmiNumericSensors
	 * @param wmiDiscreteSensors
	 * @return
	 */
	public static List<List<String>> ipmiTranslateFromWmi(@NonNull List<List<String>> wmiComputerSystem,
			@NonNull List<List<String>> wmiNumericSensors,
			@NonNull List<List<String>> wmiDiscreteSensors) {
		List<List<String>> ipmiTable = new ArrayList<>();

		// Process compute system data
		if (!wmiComputerSystem.isEmpty()) {
			List<String> wmiComputerSystemLine = wmiComputerSystem.get(0);

			if (wmiComputerSystemLine.size() > 2) {
				ipmiTable.add(
						Arrays.asList(
								"FRU",
								wmiComputerSystemLine.get(2),
								wmiComputerSystemLine.get(1),
								wmiComputerSystemLine.get(0)));
			}
		}

		// Process numeric sensors
		// BaseUnits,CurrentReading,Description,LowerThresholdCritical,LowerThresholdNonCritical,SensorType,UnitModifier,UpperThresholdCritical,UpperThresholdNonCritical
		for (List<String> line : wmiNumericSensors) {
			// Process the 'Description' field which contains everything about how to identify the sensor
			String description = line.get(2);
			String[] sensorSplit = description.split("\\(");
			String sensorId = sensorSplit[1].split("\\)")[0];
			String sensorName = sensorSplit[0];
			description = description.split(":")[1];

			String deviceId;

			int lookupIndex = description.indexOf(" for ");
			if (lookupIndex != -1) {
				deviceId = description.substring(lookupIndex + 5);
			} else {
				deviceId = "";
			}

			String baseUnit = line.get(0);
			int unitModifier;

			try {
				unitModifier = Integer.parseInt(line.get(6));
			} catch (Exception e) {
				unitModifier = 0;
			}

			String sensorType = line.get(5);

			double currentValue;
			try {
				currentValue = Double.parseDouble(line.get(1));
			} catch (Exception e) {
				currentValue = 0;
			}

			String threshold1;
			String threshold2;

			double threshold1Double = 0D;
			double threshold2Double = 0D;

			// Temperature
			if (sensorType.equals("2")) {
				// Check the unit (must be Celsius degrees)
				// 2 -> Celsius, 3 -> Farenheit, 4 -> Kelvin
				if (currentValue != 0 && (baseUnit.equals("2") || baseUnit.equals("3") || baseUnit.equals("4"))) {
					// Convert values based on unitModifier
					currentValue = currentValue * Math.pow(10, unitModifier);

					threshold1 = line.get(8);
					threshold2 = line.get(7);

					if (baseUnit.equals("4")) {
						currentValue = convertFromKelvinToCelsius(currentValue);
					} else if (baseUnit.equals("3")) {
						currentValue = convertFromFahrenheitToCelsius(currentValue);
					}

					if (isNumeric(threshold1)) {
						threshold1Double = Double.parseDouble(threshold1) * Math.pow(10, unitModifier);

						if (baseUnit.equals("4")) {
							threshold1Double = convertFromKelvinToCelsius(threshold1Double);
						} else if (baseUnit.equals("3")) {
							threshold1Double = convertFromFahrenheitToCelsius(threshold1Double);
						}

						threshold1 = String.valueOf(threshold1Double);
					} else {
						threshold1 = "";
					}

					if (isNumeric(threshold2)) {
						threshold2Double = Double.parseDouble(threshold2) * Math.pow(10, unitModifier);

						if (baseUnit.equals("4")) {
							threshold2Double = convertFromKelvinToCelsius(threshold2Double);
						} else if (baseUnit.equals("3")) {
							threshold2Double = convertFromFahrenheitToCelsius(threshold2Double);
						}

						threshold2 = String.valueOf(threshold2Double);
					} else {
						threshold2 = "";
					}

					ipmiTable.add(
							Arrays.asList(
									"Temperature",
									sensorId,
									sensorName,
									deviceId,
									String.valueOf(currentValue),
									threshold1,
									threshold2));
				}
			} else if (sensorType.equals("5") && baseUnit.equals("19")) { // Fans
				threshold1 = line.get(4);
				threshold2 = line.get(3);

				if (currentValue != 0) {
					// Convert values based on unitModifier
					currentValue = currentValue * Math.pow(10, unitModifier);
					if (isNumeric(threshold1)) { 
						threshold1 = String.valueOf(Double.parseDouble(threshold1) * Math.pow(10, unitModifier)); 
					} else { 
						threshold1 = ""; 
					}

					if (isNumeric(threshold2)) {
						threshold2 = String.valueOf(Double.parseDouble(threshold2) * Math.pow(10, unitModifier)); 
					} else { 
						threshold2 = ""; 
					}

					ipmiTable.add(
							Arrays.asList(
									"Fan",
									sensorId,
									sensorName,
									deviceId,
									String.valueOf(currentValue),
									threshold1,
									threshold2));
				}
			} else if (sensorType.equals("3") && baseUnit.equals("5")) { // Voltages
				threshold1 = line.get(4);
				if (!isNumeric(threshold1)) {
					threshold1 = line.get(3);
				}
				threshold2 = line.get(8);
				if (!isNumeric(threshold2)) {
					threshold2 = line.get(7);
				}

				if (currentValue != 0) {
					// Convert values based on unitModifier and then from Volts to milliVolts
					currentValue = currentValue * Math.pow(10, unitModifier) * 1000;

					if (isNumeric(threshold1)) {
						threshold1Double = Double.parseDouble(threshold1) * Math.pow(10, unitModifier) * 1000;
						threshold1 = String.valueOf(threshold1Double);
					} else {
						threshold1 = "";
					}

					if (isNumeric(threshold2)) {
						threshold2Double = Double.parseDouble(threshold2) * Math.pow(10, unitModifier) * 1000;
						threshold2 = String.valueOf(threshold2Double);
					} else {
						threshold2 = "";
					}

					// Add the sensor to the table
					ipmiTable.add(
							Arrays.asList(
									"Voltage",
									sensorId,
									sensorName,
									deviceId,
									String.valueOf(currentValue),
									threshold1,
									threshold2));
				}
			} else if (sensorType.equals("4") && baseUnit.equals("6") && currentValue != 0) { // Current
				// Convert values based on unitModifier
				currentValue = currentValue * Math.pow(10, unitModifier);
				// Add the sensor to the table
				ipmiTable.add(
						Arrays.asList(
								"Current",
								sensorId,
								sensorName,
								deviceId,
								String.valueOf(currentValue)));
			} else if (sensorType.equals("1") && (baseUnit.equals("7") || baseUnit.equals("8"))) { // Power consumption.
				threshold1 = line.get(8);
				threshold2 = line.get(7);

				if (isNumeric(threshold1)) {
					threshold1Double = Double.parseDouble(threshold1) * Math.pow(10, unitModifier);
					threshold1 = String.valueOf(threshold1Double);
				} else {
					threshold1 = "";
				}

				if (isNumeric(threshold2)) {
					threshold2Double = Double.parseDouble(threshold2) * Math.pow(10, unitModifier);
					threshold2 = String.valueOf(threshold2Double);
				} else {
					threshold2 = "";
				}

				// Convert values based on unitModifier
				currentValue = currentValue * Math.pow(10, unitModifier);

				// Depending on the unit, convert it to watts or not
				if (baseUnit.equals("7")) {
					ipmiTable.add(
							Arrays.asList(
									"PowerConsumption",
									sensorId,
									sensorName,
									deviceId,
									String.valueOf(currentValue),
									threshold1,
									threshold2));
				} else {
					currentValue = currentValue / 3600000;
					ipmiTable.add(
							Arrays.asList(
									"EnergyUsage",
									sensorId,
									sensorName,
									deviceId,
									String.valueOf(currentValue)));
				}
			}
		}

		List<List<String>> deviceList = new ArrayList<>();
		List<String> deviceSensorList = new ArrayList<>();

		for (List<String> line : wmiDiscreteSensors) {
			String description = line.get(1);
			String sensorName = description.split("\\(")[0];
			description = description.split(":")[1];

			String entityId;

			int lookupIndex = description.indexOf(" for ");
			if (lookupIndex == -1) {
				continue;
			}

			entityId = description.substring(lookupIndex + 5);
			lookupIndex = entityId.lastIndexOf(HardwareConstants.WHITE_SPACE);
			if (lookupIndex == -1) {
				continue;
			}

			String deviceType = entityId.substring(0, lookupIndex);
			String deviceId = entityId.substring(lookupIndex + 1);

			String state = line.get(0);
			if (state == null || state.isEmpty() || "N/A".equals(state)) {
				continue;
			}

			if (state.length() > 18 && state.startsWith("OEM State,Value=")) {
				// Reverse the bytes of the WORD value
				state = "0x" + state.substring(18, 20) + state.substring(16, 18);
			}

			boolean entityIdAlreadyRegistered = false;

			state = state.replace(HardwareConstants.DOT + HardwareConstants.COMMA, HardwareConstants.PIPE + sensorName + HardwareConstants.EQUAL);

			// Get the line of this device
			for (int i = 0; i < deviceList.size(); i++) {
				List<String> deviceLine = deviceList.get(i);
				if (entityId.equals(deviceLine.get(2))) {
					entityIdAlreadyRegistered = true;

					// Remove it from the list (we'll then update the line and re-add it to the list)
					deviceList.remove(i);

					// Add the list of states of this sensor to the device
					deviceLine.set(6, deviceLine.get(6) + 
							HardwareConstants.PIPE
							+ sensorName
							+ HardwareConstants.EQUAL
							+ state);

					// Re-add that to the device list
					deviceList.add(deviceLine);
					break;
				}
			}

			if (!entityIdAlreadyRegistered) {
				// This is the first time we find this device ID
				deviceSensorList = Arrays.asList(
						deviceType,
						deviceId,
						entityId,
						HardwareConstants.EMPTY,
						HardwareConstants.EMPTY,
						HardwareConstants.EMPTY,
						sensorName 
						+ HardwareConstants.EQUAL 
						+ state);
				deviceList.add(deviceSensorList);
			}

			// Now, remove devices that are marked as "removed" or "absent"
			deviceList.removeIf(column -> column.get(6).contains("=Device Removed/Device Absent"));
			
			for (int i = 0; i < deviceList.size(); i++) {
				List<String> deviceLine = deviceList.get(i);
				// Replace "State Asserted" and "State Deasserted" by 1 and 0
				deviceLine.set(6, deviceLine.get(6)
						.replace("=State Asserted", "=1")
						.replace("=State Deasserted", "=0")
						.replace("=Deasserted", "=0"));
				deviceList.set(i, deviceLine);
			}
		}

		// Add that to the ipmiTable
		if (!deviceList.isEmpty()) {
			ipmiTable.addAll(deviceList);
		}

		return ipmiTable;
	}

	public static boolean isNumeric(String str) {
		try {
			Double.parseDouble(str);
			return true;
		} catch(NumberFormatException e) {
			return false;
		}
	}

	public static double convertFromFahrenheitToCelsius(double fahrenheit) {
		return (fahrenheit - 32) / 1.8;
	}

	public static double convertFromKelvinToCelsius(double kelvin) {
		return kelvin - 273.15;
	}
}
