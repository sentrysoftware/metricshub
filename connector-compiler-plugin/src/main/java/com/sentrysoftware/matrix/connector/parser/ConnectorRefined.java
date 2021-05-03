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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.sentrysoftware.matrix.connector.model.common.EmbeddedFile;
import com.sentrysoftware.matrix.connector.model.common.TranslationTable;
import org.springframework.util.Assert;

import lombok.Data;

/**
 * Based on HDV Maven Plugin HardwareConnector Bertrand's implementation.
 * The aim of this bean is to provide a refined Connector content to be processed by the {@link ConnectorParser}
 * 
 * @author Nassim BOUTEKEDJIRET
 *
 */
@Data
public class ConnectorRefined {

	private String compiledFilename;
	private Map<String, String> codeMap = new LinkedHashMap<>();
	private Map<Integer, EmbeddedFile> embeddedFiles = new HashMap<>();
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
	 * Pattern to detect source types
	 */
	private static final Pattern SOURCE_PATTERN = Pattern.compile(".*\\.source\\([0-9]+\\)\\.type$");

	/**
	 * Pattern to detect commands that require sudo
	 */
	private static final Pattern SUDO_PATTERN = Pattern.compile("sudo\\([0-9]+\\)\\.command");

	/**
	 * Pattern to detect discovered objects
	 */
	private static final Pattern DISCOVERY_PATTERN = Pattern.compile("^[a-z]+\\.discovery\\.instancetable$");

	/**
	 * Pattern to detect discovered properties
	 */
	private static final Pattern PROPERTY_PATTERN = Pattern.compile("^[a-z]+\\.discovery\\.instance\\.[a-z0-9]+$");

	/**
	 * Pattern to detect collected parameters
	 */
	private static final Pattern PARAMETER_PATTERN = Pattern.compile("^[a-z]+\\.collect\\.[a-z]+$");

	/**
	 * Pattern to detect parameters that are activated dynamically (programmatically)
	 */
	private static final Pattern DYNAMIC_PARAMETER_PATTERN = Pattern.compile("^[a-z]+\\.discovery.instance.parameteractivation.[a-z]+$");

	/**
	 * Pattern to detect DetectionOperation.Criteria(n).Type
	 * group(1): detection.criteria(n)
	 */
	private static final  Pattern DETECTIONCRITERIA_PATTERN = Pattern.compile("^(detection\\.criteria\\([0-9]+\\))\\.type$");


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
	public void load(File hdfFile) throws IOException {

		// Sanity check
		Assert.notNull(hdfFile, "Specified file is <null>");
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
	public void load(String hdfFilename, File hdfParentDirectory, InputStream hdfStream) throws IOException {

		// Sanity check
		Assert.notNull(hdfStream, "Specified stream is <null>");
		Assert.notNull(hdfParentDirectory, "Specified parent directory is <null>");
		Assert.notNull(hdfFilename, "Specified connector filename is <null>");

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
			value = value.replaceAll("\\\\t", "\t").replaceAll("\\\\n", "\n").replaceAll("\"\"", "\"");

			codeMap.put(codeMatcher.group(1).toLowerCase(), value);
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
				.collect(Collectors.toMap(Function.identity(), translationTableName -> Pattern.compile(
						"^\\s*" + translationTableName + "(.*?)\\s*=\\s*(.*?)\\s*$",
						Pattern.CASE_INSENSITIVE | Pattern.MULTILINE)));

		for (Entry<String, Pattern> entry : translationTablePatterns.entrySet()) {
			final String translationTableName = entry.getKey();
			final Pattern translationTablePattern = entry.getValue();
			final Matcher translationTableMatcher = translationTablePattern.matcher(rawCode);
			final StringBuffer tempRawCode = new StringBuffer();
			final TranslationTable translationTable = TranslationTable.builder().name(translationTableName).build();

			while (translationTableMatcher.find()) {
				translationTable.getTranslations().put(translationTableMatcher.group(1).replace("\"", "").trim(),
						translationTableMatcher.group(2).replace("\"", "").trim());
				translationTableMatcher.appendReplacement(tempRawCode, "");
			}
			translationTables.put(translationTableName, translationTable);
			translationTableMatcher.appendTail(tempRawCode);
			rawCode = tempRawCode.toString();

		}

		return rawCode;
	}

	private String processIncludeDirectives(File hdfParentDirectory, String hdfSourceFilename,
			String rawCode) throws IOException {

		// We will process "recursively" the #include directives, that is: we will find
		// the #include
		// and replace with the content of the included file, and then look again on the
		// resulting
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
			rawCode = rawCode.substring(0, includeMatcher.start(1)) + includeStringBuilder.toString()
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

			// Protect the defined value as it's going to be a replacement string in regex
			// functions below
			// backslash and dollars needs to be protected as they are normally used to
			// reference groups, etc.
			// Replacing backslashes with double backslashes with double protection means
			// (gasp) 8 backslashes! :-D
			String defineValue = defineEntry.getValue().replaceAll("\\\\", "\\\\\\\\").replaceAll("\\$", "\\\\\\$");

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

		return problemList.toArray(new String[problemList.size()]);
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

		codeMap.put(key, value);
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
			return "null.connector";
		}

		if (hdfFilename.length() < 5) {
			return "invalid.connector";
		}

		return hdfFilename.substring(0, hdfFilename.length() - 5) + ".connector";
	}
}
