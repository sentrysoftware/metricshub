package com.sentrysoftware.matrix.connector.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.util.Assert;

import com.sentrysoftware.matrix.connector.model.common.EmbeddedFile;
import com.sentrysoftware.matrix.connector.model.common.TranslationTable;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

/**
 * Based on HDF Maven Plugin HardwareConnector Bertrand's implementation.
 * The aim of this bean is to provide a refined Connector content to be processed by the {@link ConnectorParser}
 *
 * @author Nassim BOUTEKEDJIRET
 *
 */
public class ConnectorRefined {

	@Getter
	private String compiledFilename;
	@Getter
	private Map<String, String> codeMap = new LinkedHashMap<>();
	@Getter
	private Map<Integer, EmbeddedFile> embeddedFiles = new HashMap<>();
	@Getter
	private Map<String, TranslationTable> translationTables = new HashMap<>();
	private ArrayList<String> problemList = new ArrayList<>();

	/**
	 * Pattern to detect <code>#include</code> statements
	 * <li>group(1): whole statement
	 * <li>group(2): included file
	 */
	private static final Pattern INCLUDE_PATTERN = Pattern.compile("^(\\s*#include\\s+\"?(\\S+?)\"?\\s*)$", Pattern.MULTILINE);

	/**
	 * Pattern to detect <code>#define</code> statements
	 * <li>group(1): constant name
	 * <li>group(2): constant value
	 */
	private static final Pattern DEFINE_PATTERN = Pattern.compile("^\\s*#define\\s+(\\w+)\\s+(.+?)\\s*$", Pattern.MULTILINE);

	/**
	 * Pattern to detect <code>EmbeddedFile(n):</code> blocks
	 * <li>group(1): embedded file number
	 * <li>group(2): embedded file content
	 */
	private static final Pattern EMBEDDEDFILE_PATTERN = Pattern.compile("\\n\\s*EmbeddedFile\\(([0-9]+)\\):\\s*\\n(.*)\\n\\s*EmbeddedFile\\(\\1\\)\\.End", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

	/**
	 * Pattern to detect broken <code>EmbeddedFile</code> blocks
	 * <li>group(1): mismatching embedded file reference
	 */
	private static final Pattern MISMATCHING_EMBEDDEDFILE_PATTERN = Pattern.compile("\\n\\s*(EmbeddedFile\\([0-9]+\\))(:|\\.End)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

	/**
	 * Pattern to remove comments (see https://regex101.com/r/vI2iW5/1)
	 * <p>
	 * Usage:
	 * {@code REMOVE_COMMENTS_PATTERN.matcher(code).replaceAll("$1")}
	 */
	private static final Pattern REMOVE_COMMENTS_PATTERN = Pattern.compile("((['\"])(?:(?!\\2|\\\\).|\\\\.)*\\2)|\\/\\/[^\\n]*|\\/\\*(?:[^*]|\\*(?!\\/))*\\*\\/");

	/**
	 * Pattern to detect {@code <entry>=<value>} entries
	 * <li>group(1): entry name
	 * <li>group(2): entry value
	 */
	private static final Pattern CODE_PATTERN = Pattern.compile("^\\s*(.*?)\\s*=\\s*(.*?)\\s*$", Pattern.MULTILINE);

	/**
	 * Pattern to detect translation table names
	 */
	private static final Pattern TRANSLATION_TABLE_NAME_PATTERN = Pattern.compile(".*\\.(translationtable|bittranslationtable)=\\s*(.*?)\\s*$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);



	/**
	 * Loads the specified Connector, parses it and populates the map with all values
	 * (also does some pre-processing)
	 *
	 * @param filePath Path to the Connector file
	 * @throws IllegalArgumentException when specified File instance is null or the
	 *                                  file does not exist
	 * @throws IOException              when there is a problem while reading the
	 *                                  Connector file
	 */
	public void load(String filePath) throws IOException {

		load(new File(filePath));
	}

	/**
	 * Loads the specified Connector, parses it and populates the map with all values
	 * (also does some pre-processing)
	 *
	 * @param hdfFile File instance to the .HDF file to be parsed
	 * @throws IllegalArgumentException when specified File instance is null or the
	 *                                  file does not exist
	 * @throws IOException              when there is a problem while reading the
	 *                                  Connector file
	 */
	public void load(@NonNull File hdfFile) throws IOException {

		// Sanity check
		Assert.isTrue(hdfFile.exists(),
				() -> String.format("Specified file %s does not exist", hdfFile.getAbsolutePath()));

		// Get an InputStream from the specified File, and load
		load(hdfFile.getName(), hdfFile.getParentFile(), new FileInputStream(hdfFile));
	}

	/**
	 * Loads the specified Connector, parses it and populates the map with all values
	 * (also does some pre-processing)
	 *
	 * @param hdfStream          Stream to the connector file to load
	 * @param hdfParentDirectory Directory that contains the .hhdf files that may be
	 *                           included by the connector
	 * @throws IllegalArgumentException when specified File instance is null or the
	 *                                  file does not exist
	 * @throws IOException              when there is a problem while reading the
	 *                                  Connector file
	 */
	public void load(@NonNull String hdfFilename, @NonNull File hdfParentDirectory, @NonNull InputStream hdfStream)
			throws IOException {

		compiledFilename = getCompiledFilename(hdfFilename);

		String rawCode = buildRawCode(hdfStream);

		// Clear the problem list while loading (should be empty already, but in case
		// someone loads the connector several times...)
		problemList.clear();

		// Now process #include directives
		rawCode = processIncludeDirectives(hdfParentDirectory, hdfFilename, rawCode);

		// Process the #define directives
		rawCode = processDefineDirectives(rawCode);

		// Now process the embedded files
		rawCode = processEmbeddedFiles(rawCode);

		// Process Translation tables
		rawCode = processTranslationTables(rawCode);

		// Do we still have EmbeddedFile references? (which would mean we have
		// mismatching EmbeddedFile(x))
		detectRemainingEmbeddedFiles(rawCode);

		// Now remove comments
		rawCode = removeComments(rawCode);

		// Now, build the code map
		processCode(rawCode);

		codeMap = sortCodeMap(codeMap);
	}

	private String buildRawCode(InputStream hdfStream) throws IOException {

		try (BufferedReader sourceReader = new BufferedReader(new InputStreamReader(hdfStream))) {
			return sourceReader.lines().collect(Collectors.joining("\n"));
		}
	}

	private void processCode(String rawCode) {

		Matcher codeMatcher = CODE_PATTERN.matcher(rawCode);

		while (codeMatcher.find()) {
			String value = codeMatcher.group(2).trim();

			if (value.startsWith("\"")) {
				value = value.substring(1);
			}

			if (value.endsWith("\"")) {
				value = value.substring(0, value.length() - 1);
			}
			value = value.replace("\\\\t", "\t").replace("\\\\n", "\n").replace("\"\"", "\"");

			put(codeMatcher.group(1), value);
		}
	}

	private String removeComments(String rawCode) {

		return REMOVE_COMMENTS_PATTERN.matcher(rawCode).replaceAll("$1");
	}

	private void detectRemainingEmbeddedFiles(String rawCode) {

		Matcher remainingEmbeddedFileMatcher = MISMATCHING_EMBEDDEDFILE_PATTERN.matcher(rawCode);

		while (remainingEmbeddedFileMatcher.find()) {
			problemList.add("Found mismatching EmbeddedFile reference: " + remainingEmbeddedFileMatcher.group(1));
		}
	}

	private String processEmbeddedFiles(String rawCode) {

		Matcher embeddedFileMatcher = EMBEDDEDFILE_PATTERN.matcher(rawCode);
		StringBuffer tempRawCode = new StringBuffer();

		while (embeddedFileMatcher.find()) {
			String embeddedFileContent = embeddedFileMatcher.group(2);
			Integer embeddedFileIndex = Integer.valueOf(embeddedFileMatcher.group(1).trim());
			// EmbeddedFiles index is the key
			embeddedFiles.put(embeddedFileIndex , EmbeddedFile.builder().content(embeddedFileContent).build());
			embeddedFileMatcher.appendReplacement(tempRawCode, "");
		}
		embeddedFileMatcher.appendTail(tempRawCode);
		rawCode = tempRawCode.toString();
		return rawCode;
	}

	private String processTranslationTables(String rawCode) {

		Set<String> translationTableNames = new HashSet<>();

		Matcher translationTableNameMatcher = TRANSLATION_TABLE_NAME_PATTERN.matcher(rawCode);

		while (translationTableNameMatcher.find()) {
			translationTableNames.add(translationTableNameMatcher.group(2).replace("\"", "").trim());
		}

		Map<String, Pattern> translationTablePatterns = translationTableNames.stream()
				.collect(Collectors.toMap(
						Function.identity(),
						translationTableName -> Pattern.compile(
								"^\\s*" + translationTableName + "\\((.*?)\\)\\s*=\\s*(.*?)\\s*$",
								Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
						)
				));

		for (Entry<String, Pattern> entry : translationTablePatterns.entrySet()) {
			final String translationTableName = entry.getKey();
			final Pattern translationTablePattern = entry.getValue();
			final Matcher translationTableMatcher = translationTablePattern.matcher(rawCode);
			final StringBuffer tempRawCode = new StringBuffer();
			final TranslationTable translationTable = TranslationTable.builder().name(translationTableName).build();

			while (translationTableMatcher.find()) {

				String key = translationTableMatcher
					.group(1)
					.replaceAll(ConnectorParserConstants.DOUBLE_QUOTES_REGEX_REPLACEMENT, "$1")
					.trim()
					.toLowerCase();

				translationTable.getTranslations().put(
						key,
						translationTableMatcher
							.group(2)
							.replaceAll(ConnectorParserConstants.DOUBLE_QUOTES_REGEX_REPLACEMENT, "$1")
							.trim());

				translationTableMatcher.appendReplacement(tempRawCode, "");
			}
			translationTables.put(translationTableName, translationTable);
			translationTableMatcher.appendTail(tempRawCode);
			rawCode = tempRawCode.toString();

		}

		return rawCode;
	}

	private String processIncludeDirectives(File hdfParentDirectory, String hdfSourceFilename, String rawCode)
			throws IOException {

		// We will process "recursively" the #include directives, that is: we will find the #include
		// and replace with the content of the included file, and then look again on the resulting
		// code with there are still new #include directives
		int ttl = 100;

		while (ttl > 0) {
			Matcher includeMatcher = INCLUDE_PATTERN.matcher(rawCode);

			if (!includeMatcher.find()) {
				break;
			}
			String includeHdfName = includeMatcher.group(2);

			// But first, we want to make sure we have an actual parent directory to look
			// for .hhdf files
			if (hdfParentDirectory == null) {
				throw new IOException(hdfSourceFilename + " wants to include " + includeHdfName
						+ " but no parent directory was specified.");
			}

			if (!hdfParentDirectory.exists()) {
				throw new IOException(
						hdfSourceFilename + " wants to include " + includeHdfName + " but specified parent directory '"
								+ hdfParentDirectory.getAbsolutePath() + "' does not exist.");
			}

			// Read the #include file
			File includeHdfFile = new File(hdfParentDirectory, includeHdfName);
			StringBuilder includeStringBuilder = new StringBuilder();

			try (BufferedReader includeReader = new BufferedReader(new FileReader(includeHdfFile))) {
				String includeLine;

				while ((includeLine = includeReader.readLine()) != null) {
					includeStringBuilder.append(includeLine).append("\n");
				}
			}
			// Replace the #include statement with the actual content
			rawCode = rawCode.substring(0, includeMatcher.start(1))
					+ includeStringBuilder.toString()
					+ rawCode.substring(includeMatcher.end(1));

			ttl--;
		}

		// If TTL = 0, means we still have #include statements to process! (which is
		// weird)
		if (ttl == 0) {
			throw new IOException("Probable recursive #include statements in " + hdfSourceFilename);
		}
		return rawCode;
	}

	private String processDefineDirectives(String rawCode) {

		Map<String, String> defineMap = new HashMap<>();
		Matcher defineMatcher = DEFINE_PATTERN.matcher(rawCode);
		StringBuffer tempRawCode = new StringBuffer();

		while (defineMatcher.find()) {
			defineMap.put(defineMatcher.group(1), defineMatcher.group(2));
			defineMatcher.appendReplacement(tempRawCode, "");
		}
		defineMatcher.appendTail(tempRawCode);
		rawCode = tempRawCode.toString();

		for (Entry<String, String> defineEntry : defineMap.entrySet()) {

			// Protect the defined value as it's going to be a replacement string in regex functions below
			// backslash and dollars needs to be protected as they are normally used to reference groups, etc.
			// Replacing backslashes with double backslashes with double protection means (gasp) 8 backslashes! :-D
			String defineValue = defineEntry.getValue().replace("\\\\", "\\\\\\\\").replace("\\$", "\\\\\\$");

			// Usual regex replacement code loop
			tempRawCode = new StringBuffer();
			Pattern defineEntryPattern = Pattern.compile("\\b" + defineEntry.getKey() + "\\b");
			Matcher defineEntryMatcher = defineEntryPattern.matcher(rawCode);

			while (defineEntryMatcher.find()) {
				defineEntryMatcher.appendReplacement(tempRawCode, defineValue);
			}
			defineEntryMatcher.appendTail(tempRawCode);
			rawCode = tempRawCode.toString();
		}
		return rawCode;
	}

	/**
	 * Get the list of problems encountered while loading the connector
	 *
	 * @return The list of problems as an array of String
	 */
	public String[] getProblemList() {

		return problemList.toArray(new String[0]);
	}

	/**
	 * Return the value of the specified key in the Connector
	 *
	 * @param key
	 *            The key whose's value is to be retrieved (e.g.:
	 *            "detection.criteria(1).type")
	 *
	 * @return The value of the specified key
	 */
	public String get(String key) {

		return codeMap.get(key.toLowerCase());
	}

	/**
	 * Return the value of the specified key in the Connector, or the specified
	 * default value if not found
	 *
	 * @param key
	 *                     The key whose's value is to be retrieved (e.g.:
	 *                     "detection.criteria(1).type")
	 * @param defaultValue
	 *                     The default value to be returned if the specified key is
	 *                     not
	 *                     found
	 *
	 * @return The value of the specified key, or the specified default value if
	 *         not found
	 */
	public String getOrDefault(String key, String defaultValue) {

		return codeMap.getOrDefault(key.toLowerCase(), defaultValue);
	}

	/**
	 * Sets the specified key with the specified value in the Connector
	 *
	 * @param key
	 *              The key to set (e.g.: "hdf.builddate")
	 * @param value
	 *              The value to set for the specified key
	 */
	public void put(String key, String value) {

		codeMap.put(key.toLowerCase(), value);
	}

	/**
	 * Returns the entire code map as a set of entries (pairs of key and value)
	 *
	 * @return a the entire code map
	 */
	public Set<Entry<String, String>> entrySet() {

		return codeMap.entrySet();
	}

	/**
	 * Return the compiled Connector file name corresponding to the specified connector
	 * file name
	 *
	 * @param hdfFilename The connector file name (ending with .hdfs)
	 * @return The corresponding HTML page filename
	 */
	public static String getCompiledFilename(String hdfFilename) {

		// Sanity check (we're supposed to be provided with an .hdfs filename
		if (hdfFilename == null) {
			return "null";
		}

		if (hdfFilename.length() < 5) {
			return "invalid";
		}

		return ConnectorParser.normalizeConnectorName(hdfFilename);

	}

	/**
	 * <p>Sort the Code Map with this order:
	 * <li>Hdf</li>
	 * 		<ul><li>original position in map</li></ul>
	 * <li>Detection</li>
	 * 		<ul>
	 * 			<li>Criteria index</li>
	 * 			<li>Criteria type</li>
	 * 			<li>Criteria parameters in original position in map</li>
	 * 			<li>Criteria Step index</li>
	 * 			<li>Criteria Step type</li>
	 * 			<li>Criteria Step parameters in original position in map</li>
	 * 		</ul>
	 * <li>Monitor type like (Enclosure, Battery, ...) in the connector order</li>
	 * 		<ul>
	 * 			<li>Discovery</li>
	 * 				<ul>
	 * 					<li>Source index</li>
	 * 					<li>Source type</li>
	 * 					<li>Source parameters in original position in map</li>
	 * 					<li>Source Step index</li>
	 * 					<li>Source Step type</li>
	 * 					<li>Source Step parameters in original position in map</li>
	 * 					<li>Source Compute index</li>
	 * 					<li>Source Compute type</li>
	 * 					<li>Source Compute parameters in original position in map</li>
	 * 					<li>Instance in original position in map</li>
	 * 				</ul>
	 * 		<li>Collect</li>
	 * 				<ul>
	 * 					<li>Source Mono or Multi Instance type</li>
	 * 					<li>Source index</li>
	 * 					<li>Source reference</li>
	 * 					<li>Source type</li>
	 * 					<li>Source parameters in original position in map</li>
	 * 					<li>Source Step index</li>
	 * 					<li>Source Step type</li>
	 * 					<li>Source Step parameters in original position in map</li>
	 * 					<li>Source Compute index</li>
	 * 					<li>Source Compute type</li>
	 * 					<li>Source Compute parameters in original position in map</li>
	 * 					<li>Instance in original position in map</li>
	 * 				</ul>
	 * 		</ul>
	 * <li>Default original position in map</li>
	 * </p>
	 * @param codeMap The key-value entry in the connector
	 * @return The sorted code map.
	 */
	static Map<String, String> sortCodeMap(final Map<String, String> codeMap) {

		if (codeMap == null || codeMap.isEmpty()) {
			return codeMap;
		}

		final Map<String, List<ConnectorEntry>> connectorEntries = new LinkedHashMap<>();

		final String[] keys = codeMap.keySet().toArray(new String[0]);
		
		// Group all by the entry types.
		IntStream.range(0, keys.length)
		.mapToObj(index -> new ConnectorEntry(index, keys[index], codeMap.get(keys[index])))
		.forEach(connectorEntry -> 
				connectorEntries.computeIfAbsent(connectorEntry.extractEntryType(), key -> new ArrayList<>())
				.add(connectorEntry));

		// Sort the code map with the keys sorted inside the entry type groups
		return connectorEntries.entrySet().stream()
				.flatMap(entry -> entry.getValue().stream().sorted(ConnectorRefined::compareCodeMap))				
				.collect(Collectors.toMap(
						connectorEntry -> connectorEntry.getKey(),
						connectorEntry -> connectorEntry.getValue(),
						(connectorEntry1, connectorEntry2) -> connectorEntry1,
						LinkedHashMap::new));
	}

	private static final int IS_BEFORE = -1;
	private static final int IS_EQUAL = 0;
	private static final int IS_AFTER = 1;

	/**
	 * <p>Compare Two connector entries. Order them by:
	 * <li>Hdf in original position in map</li>
	 * <li>Detection</li>
	 * 		<ul>
	 * 			<li>Criteria index</li>
	 * 			<li>Criteria type</li>
	 * 			<li>Criteria parameters in original position in map</li>
	 * 			<li>Criteria Step index</li>
	 * 			<li>Criteria Step type</li>
	 * 			<li>Criteria Step parameters in original position in map</li>
	 * 		</ul>
	 * <li>Discovery</li>
	 * 		<ul>
	 * 			<li>Source index</li>
	 * 			<li>Source type</li>
	 * 			<li>Source parameters in original position in map</li>
	 * 			<li>Source Step index</li>
	 * 			<li>Source Step type</li>
	 * 			<li>Source Step parameters in original position in map</li>
	 * 			<li>Source Compute index</li>
	 * 			<li>Source Compute type</li>
	 * 			<li>Source Compute parameters in original position in map</li>
	 * 			<li>Instance in original position in map</li>
	 * 		</ul>
	 * <li>Collect</li>
	 * 		<ul>
	 * 			<li>Source Mono or Multi Instance type</li>
	 * 			<li>Source index</li>
	 * 			<li>Source reference</li>
	 * 			<li>Source type</li>
	 * 			<li>Source parameters in original position in map</li>
	 * 			<li>Source Step index</li>
	 * 			<li>Source Step type</li>
	 * 			<li>Source Step parameters in original position in map</li>
	 * 			<li>Source Compute index</li>
	 * 			<li>Source Compute type</li>
	 * 			<li>Source Compute parameters in original position in map</li>
	 * 			<li>Instance in original position in map</li>
	 * 		</ul>
	 * <li>Default original position in map</li>
	 * </p>
	 * @param connectorEntry1
	 * @param connectorEntry2
	 * @return 
	 * 		<li>Inferior to zero if connectorEntry1 is before connectorEntry2</li>
	 * 		<li>Superior to zero if connectorEntry1 is after connectorEntry2</li>
	 * 		<li>Zero if both are equals</li>
	 */
	static int compareCodeMap(final ConnectorEntry connectorEntry1, final ConnectorEntry connectorEntry2) {

		if (connectorEntry1 == null && connectorEntry2 == null) {
			return IS_EQUAL;
		}
		if (connectorEntry2 == null) {
			return IS_BEFORE;
		}
		if (connectorEntry1 == null) {
			return IS_AFTER;
		}

		// Case Hdf
		if (connectorEntry1.hasHdf() && connectorEntry2.hasHdf()) {
			return Integer.valueOf(connectorEntry1.getOriginalIndex()).compareTo(connectorEntry2.getOriginalIndex());
		}
		if (connectorEntry1.hasHdf()) {
			return IS_BEFORE;
		}
		if (connectorEntry2.hasHdf()) {
			return IS_AFTER;
		}

		// Case Detection
		final OptionalInt maybeDetection = compareDetectionConnectorEntries(connectorEntry1, connectorEntry2);
		return maybeDetection.isPresent() ?
				maybeDetection.getAsInt() :
				// Case Source
				compareSourceConnectorEntries(connectorEntry1, connectorEntry2);
	}

	/**
	 * <p>Compare Two connector entries. If it's a Detection entry type, order them by:
	 * <li>Criteria index</li>
	 * <li>Criteria type</li>
	 * 	<li>Criteria parameters in original position in map</li>
	 * 	<li>Criteria Step index</li>
	 * 	<li>Criteria Step type</li>
	 * 	<li>Criteria Step parameters in original position in map</li>
	 * <li>Default original position in map</li>
	 * </p>
	 * @param connectorEntry1
	 * @param connectorEntry2
	 * @return
	 * 		<li>If it's a Detection entry type, an optional with :</li>
	 * 		<ul>
	 * 			<li>Inferior to zero if connectorEntry1 is before connectorEntry2</li>
	 * 			<li>Superior to zero if connectorEntry1 is after connectorEntry2</li>
	 * 			<li>Zero if both are equals</li>
	 * 		</ul>
	 * 		<li>An Empty optional otherwise if it's not a Detection entry type</li>
	 */
	static OptionalInt compareDetectionConnectorEntries(
			final ConnectorEntry connectorEntry1,
			final ConnectorEntry connectorEntry2) {

		final OptionalInt maybeDetection1 = connectorEntry1.extractDetectionIndex();
		final OptionalInt maybeDetection2 = connectorEntry2.extractDetectionIndex();

		if (maybeDetection1.isEmpty() && maybeDetection2.isEmpty()) {
			return OptionalInt.empty();
		}
		if (maybeDetection1.isPresent() && maybeDetection2.isEmpty()) {
			return OptionalInt.of(IS_BEFORE);
		}
		if (maybeDetection1.isEmpty() && maybeDetection2.isPresent()) {
			return OptionalInt.of(IS_AFTER);
		}

		// criteria index different => compare criteria index
		if (maybeDetection1.getAsInt() != maybeDetection2.getAsInt()) {
			return OptionalInt.of(Integer.valueOf(maybeDetection1.getAsInt()).compareTo(maybeDetection2.getAsInt()));
		}

		final OptionalInt maybeDetectionStep1 = connectorEntry1.extractDetectionStepIndex();
		final OptionalInt maybeDetectionStep2 = connectorEntry2.extractDetectionStepIndex();

		final boolean hasType1 = connectorEntry1.hasType();
		final boolean hasType2 = connectorEntry2.hasType();

		//type before
		if (hasType1 && maybeDetectionStep1.isEmpty() &&
				hasType2 && maybeDetectionStep2.isEmpty()) {
			return OptionalInt.of(Integer.valueOf(connectorEntry1.getOriginalIndex())
					.compareTo(connectorEntry2.getOriginalIndex()));
		}
		if (hasType1 && maybeDetectionStep1.isEmpty()) {
			return OptionalInt.of(IS_BEFORE);
		}
		if (hasType2 && maybeDetectionStep2.isEmpty()) {
			return OptionalInt.of(IS_AFTER);
		}

		if (maybeDetectionStep1.isPresent() && maybeDetectionStep2.isPresent()) {

			// step index different => compare step index
			if (maybeDetectionStep1.getAsInt() != maybeDetectionStep2.getAsInt()) {
				return OptionalInt.of(Integer.valueOf(maybeDetectionStep1.getAsInt())
						.compareTo(maybeDetectionStep2.getAsInt()));
			}

			//type before
			if (hasType1 ^ hasType2) {
				return hasType1 ? OptionalInt.of(IS_BEFORE) : OptionalInt.of(IS_AFTER);
			}

			// same criteria index and same step index and   => compare original map index
			return OptionalInt.of(Integer.valueOf(connectorEntry1.getOriginalIndex())
					.compareTo(connectorEntry2.getOriginalIndex()));
		}
		if (maybeDetectionStep1.isPresent() ^ maybeDetectionStep2.isPresent()) {
			return maybeDetectionStep1.isPresent() ? OptionalInt.of(IS_AFTER) : OptionalInt.of(IS_BEFORE);
		}

		// same criteria index, no step index => compare original map index
		return OptionalInt.of(Integer.valueOf(connectorEntry1.getOriginalIndex())
				.compareTo(connectorEntry2.getOriginalIndex()));
	}

	/**
	 * <p>Compare Two connector entries of Source type. Order them by:
	 * <li>Source Mono or Multi Instance type</li>
	 * 	<li>Source index</li>
	 * 	<li>Source reference</li>
	 * 	<li>Source type</li>
	 * 	<li>Source parameters in original position in map</li>
	 * 	<li>Source Step index</li>
	 * 	<li>Source Step type</li>
	 * 	<li>Source Step parameters in original position in map</li>
	 * 	<li>Source Compute index</li>
	 * 	<li>Source Compute type</li>
	 * 	<li>Source Compute parameters in original position in map</li>
	 * 	<li>Instance in original position in map</li>
	 * <li>Default original position in map</li>
	 * </p>
	 * @param connectorEntry1
	 * @param connectorEntry2
	 * @return 
	 * 		<li>Inferior to zero if connectorEntry1 is before connectorEntry2</li>
	 * 		<li>Superior to zero if connectorEntry1 is after connectorEntry2</li>
	 * 		<li>Zero if both are equals</li>
	 */
	static int compareSourceConnectorEntries(
			final ConnectorEntry connectorEntry1,
			final ConnectorEntry connectorEntry2) {

		final boolean hasDiscovery1 = connectorEntry1.hasDiscovery();
		final boolean hasDiscovery2 = connectorEntry2.hasDiscovery();
		final boolean hasCollect1 = connectorEntry1.hasCollect();
		final boolean hasCollect2 = connectorEntry2.hasCollect();

		// Discovery before Collect
		if (hasDiscovery1 && hasCollect2) {
			return IS_BEFORE;
		}
		if (hasCollect1 && hasDiscovery2) {
			return IS_AFTER;
		}

		final OptionalInt maybeSource1 = connectorEntry1.extractSourceIndex();
		final OptionalInt maybeSource2 = connectorEntry2.extractSourceIndex();

		final boolean hasType1 = connectorEntry1.hasType();
		final boolean hasType2 = connectorEntry2.hasType();
		
		// Other sources like instances
		if (maybeSource1.isEmpty() && maybeSource2.isEmpty()) {
			return Integer.valueOf(connectorEntry1.getOriginalIndex()).compareTo(connectorEntry2.getOriginalIndex());
		}
		if (maybeSource1.isEmpty()) {
			// Collect multi-instance type first.
			return hasCollect1 && hasType1 ? IS_BEFORE : IS_AFTER;
		}
		if (maybeSource2.isEmpty()) {
			// Collect multi-instance type first.
			return hasCollect2 && hasType2 ? IS_AFTER : IS_BEFORE;
		}

		// source index different => compare source index
		if (maybeSource1.getAsInt() != maybeSource2.getAsInt()) {
			return Integer.valueOf(maybeSource1.getAsInt()).compareTo(maybeSource2.getAsInt());
		}

		final OptionalInt maybeSourceStep1 = connectorEntry1.extractSourceStepIndex();
		final OptionalInt maybeSourceStep2 = connectorEntry2.extractSourceStepIndex();

		final OptionalInt maybeCompute1 = connectorEntry1.extractComputeIndex();
		final OptionalInt maybeCompute2 = connectorEntry2.extractComputeIndex();

		// Only source
		if (maybeSourceStep1.isEmpty() && maybeSourceStep2.isEmpty() &&
				maybeCompute1.isEmpty() && maybeCompute2.isEmpty()) {

			// Reference before
			final boolean hasSourceReference1 = connectorEntry1.hasSourceReference();
			final boolean hasSourceReference2 = connectorEntry2.hasSourceReference();
			if (hasSourceReference1 ^ hasSourceReference2) {
				return hasSourceReference1 ? IS_BEFORE : IS_AFTER;
			}

			// Type before
			if (hasType1 ^ hasType2) {
				return hasType1 ? IS_BEFORE : IS_AFTER;
			}

			return Integer.valueOf(connectorEntry1.getOriginalIndex()).compareTo(connectorEntry2.getOriginalIndex());

		}
		if (maybeSourceStep1.isEmpty() && maybeCompute1.isEmpty()) {
			return IS_BEFORE;
		}
		if (maybeSourceStep2.isEmpty() && maybeCompute2.isEmpty()) {
			return IS_AFTER;
		}

		// Source Step
		if (maybeSourceStep1.isPresent() && maybeSourceStep2.isPresent()) {

			// step index different => compare step index
			if (maybeSourceStep1.getAsInt() != maybeSourceStep2.getAsInt()) {
				return Integer.valueOf(maybeSourceStep1.getAsInt()).compareTo(maybeSourceStep2.getAsInt());
			}

			// Type before
			if (hasType1 ^ hasType2) {
				return hasType1? IS_BEFORE : IS_AFTER;
			}

			return Integer.valueOf(connectorEntry1.getOriginalIndex()).compareTo(connectorEntry2.getOriginalIndex());

		}
		if (maybeSourceStep1.isPresent()) {
			return IS_BEFORE;
		}
		if (maybeSourceStep2.isPresent()) {
			return IS_AFTER;
		}

		// Source Compute	

		// compute index different => compare compute index
		if (maybeCompute1.getAsInt() != maybeCompute2.getAsInt()) {
			return Integer.valueOf(maybeCompute1.getAsInt()).compareTo(maybeCompute2.getAsInt());
		}

		// Type before
		if (hasType1 ^ hasType2) {
			return hasType1 ? IS_BEFORE : IS_AFTER;
		}

		return Integer.valueOf(connectorEntry1.getOriginalIndex()).compareTo(connectorEntry2.getOriginalIndex());		
	}

	@Data
	static class ConnectorEntry {

		private final int originalIndex;
		private final String key;
		private final String value;

		private static final Pattern HDF_PATTERN = Pattern.compile(
				"^\\s*Hdf\\..+\\s*$",
				Pattern.CASE_INSENSITIVE);

		private static final Pattern DISCOVERY_PATTERN = Pattern.compile(
				"^\\s*.+\\.Discovery\\..+\\s*$",
				Pattern.CASE_INSENSITIVE);

		private static final Pattern COLLECT_PATTERN = Pattern.compile(
				"^\\s*.+\\.Collect\\..+\\s*$",
				Pattern.CASE_INSENSITIVE);

		private static final Pattern TYPE_PATTERN = Pattern.compile(
				"^\\s*.+\\.Type\\s*$",
				Pattern.CASE_INSENSITIVE);

		private static final Pattern DETECTION_PATTERN = Pattern.compile(
				"^\\s*Detection\\.Criteria\\(([1-9]\\d*)\\).*\\s*$",
				Pattern.CASE_INSENSITIVE);

		private static final Pattern DETECTION_STEP_PATTERN = Pattern.compile(
				"^\\s*Detection\\.Criteria\\([1-9]\\d*\\)\\.Step\\(([1-9]\\d*)\\).*\\s*$",
				Pattern.CASE_INSENSITIVE);

		private static final Pattern SOURCE_PATTERN = Pattern.compile(
				"^\\s*.*\\.Source\\(([1-9]\\d*)\\).*\\s*$",
				Pattern.CASE_INSENSITIVE);

		private static final Pattern SOURCE_REFERENCE_PATTERN = Pattern.compile(
				"^\\s*.*\\.Source\\([1-9]\\d*\\)\\s*$",
				Pattern.CASE_INSENSITIVE);

		private static final Pattern SOURCE_STEP_PATTERN = Pattern.compile(
				"^\\s*.*\\.Source\\([1-9]\\d*\\)\\.Step\\(([1-9]\\d*)\\).*\\s*$",
				Pattern.CASE_INSENSITIVE);

		private static final Pattern SOURCE_COMPUTE_PATTERN = Pattern.compile(
				"^\\s*.*\\.Source\\([1-9]\\d*\\)\\.Compute\\(([1-9]\\d*)\\).*\\s*$",
				Pattern.CASE_INSENSITIVE);

		/**
		 * Extract the entry type like hdf, detection, enclosure , battery ... from the key.
		 * @return
		 */
		String extractEntryType() {
			final int firstDotIndex = key.indexOf('.');
			final String entryType = firstDotIndex > 0 ? key.substring(0, firstDotIndex) : key;
			return entryType.toLowerCase();
		}

		/**
		 * Indicate that the key has hdf tag.
		 * @return
		 */
		boolean hasHdf() {
			return matches(HDF_PATTERN);
		}

		/**
		 * Indicate that the key has Type tag.
		 * @return
		 */
		boolean hasType() {
			return matches(TYPE_PATTERN);
		}

		/**
		 * Indicate that the key has Discovery tag.
		 * @return
		 */
		boolean hasDiscovery() {
			return matches(DISCOVERY_PATTERN);
		}

		/**
		 * Indicate that the key has Collect tag.
		 * @return
		 */
		boolean hasCollect() {
			return matches(COLLECT_PATTERN);
		}

		/**
		 * Indicate that the key has a source reference.
		 * @return
		 */
		boolean hasSourceReference() {
			return matches(SOURCE_REFERENCE_PATTERN);
		}

		/**
		 * Extract the detection index from the key.
		 * @return An Optional with the detection index. Optional empty if the index has not been found.
		 */
		OptionalInt extractDetectionIndex() {
			return extractIndex(DETECTION_PATTERN);
		}

		/**
		 * Extract the detection step index from the key.
		 * @return An Optional with the detection step index. Optional empty if the index has not been found.
		 */
		OptionalInt extractDetectionStepIndex() {
			return extractIndex(DETECTION_STEP_PATTERN);
		}
	
		/**
		 * Extract the source index from the key.
		 * @return An Optional with the source index. Optional empty if the index has not been found.
		 */
		OptionalInt extractSourceIndex() {
			return extractIndex(SOURCE_PATTERN);
		}
		
		/**
		 * Extract the source step index from the key.
		 * @return An Optional with the source step index. Optional empty if the index has not been found.
		 */
		OptionalInt extractSourceStepIndex() {
			return extractIndex(SOURCE_STEP_PATTERN);
		}
		
		/**
		 * Extract the compute index from the key.
		 * @return An Optional with the compute index. Optional empty if the index has not been found.
		 */
		OptionalInt extractComputeIndex() {
			return extractIndex(SOURCE_COMPUTE_PATTERN);
		}

		/**
		 * Match the key with the pattern
		 * @param pattern The pattern
		 * @return
		 */
		private boolean matches(final Pattern pattern) {
			return pattern.matcher(key).matches();
		}

		/**
		 * Extract the index from the key.
		 * @return An Optional with the index. Optional empty if the index has not been found.
		 */
		private OptionalInt extractIndex(final Pattern pattern) {
			final Matcher matcher =  pattern.matcher(key);
			return matcher.find() ?
					OptionalInt.of(Integer.parseInt(matcher.group(1))) :
					OptionalInt.empty();
		}
	}
}
