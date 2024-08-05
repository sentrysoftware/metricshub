package org.sentrysoftware.metricshub.extension.jawk;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub Jawk Extension
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

import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.FILE_PATTERN;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.sentrysoftware.jawk.backend.AVM;
import org.sentrysoftware.jawk.frontend.AwkParser;
import org.sentrysoftware.jawk.frontend.AwkSyntaxTree;
import org.sentrysoftware.jawk.intermediate.AwkTuples;
import org.sentrysoftware.jawk.util.AwkParameters;
import org.sentrysoftware.jawk.util.AwkSettings;
import org.sentrysoftware.jawk.util.ScriptSource;
import org.sentrysoftware.metricshub.engine.awk.UniformPrintStream;
import org.sentrysoftware.metricshub.engine.connector.model.common.EmbeddedFile;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.JawkSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.Source;
import org.sentrysoftware.metricshub.engine.extension.IJawkExtension;
import org.sentrysoftware.metricshub.engine.strategy.source.SourceProcessor;
import org.sentrysoftware.metricshub.engine.strategy.source.SourceTable;
import org.sentrysoftware.metricshub.engine.strategy.utils.EmbeddedFileHelper;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

/**
 * This class implements the {@link IJawkExtension} contract, reports the supported features,
 * processes HTTP sources and criteria.
 */
@Slf4j
public class JawkExtension implements IJawkExtension {

	@Override
	public boolean isValidSource(Source source) {
		return source instanceof JawkSource;
	}

	@Override
	public SourceTable processSource(
		final Source source,
		final String connectorId,
		final TelemetryManager telemetryManager,
		final SourceProcessor sourceProcessor
	) {
		final String hostname = telemetryManager.getHostname();

		if (source == null) {
			log.warn("Hostname {} - Jawk Source cannot be null, the Jawk operation will return an empty result.", hostname);
			return SourceTable.empty();
		}

		if (!(source instanceof JawkSource jawkSource)) {
			log.warn("Hostname {} - Jawk Source is invalid, the Jawk operation will return an empty result.", hostname);
			return SourceTable.empty();
		}

		final String script = jawkSource.getScript();

		// An Awk Script is supposed to be only the reference to the EmbeddedFile, so the map contains only one item which is our EmbeddedFile
		final Optional<EmbeddedFile> maybeEmbeddedFile;

		if (!FILE_PATTERN.matcher(script).find()) {
			maybeEmbeddedFile = Optional.of(EmbeddedFile.fromString(script));
		} else {
			try {
				maybeEmbeddedFile =
					EmbeddedFileHelper.findEmbeddedFile(
						script,
						telemetryManager.getEmbeddedFiles(connectorId),
						hostname,
						connectorId
					);
			} catch (Exception exception) {
				log.warn("Hostname {} - Jawk Operation script {} has not been set correctly.", hostname, script);
				return SourceTable.empty();
			}
		}

		if (maybeEmbeddedFile.isEmpty()) {
			log.warn("Hostname {} - Jawk Operation script {} embedded file can't be found.", hostname, script);
			return SourceTable.empty();
		}

		final EmbeddedFile embeddedFile = maybeEmbeddedFile.get();
		final String awkScript = embeddedFile.getContentAsString();

		log.debug("Hostname {} - Jawk Operation. AWK Script:\n{}\n", hostname, awkScript);

		System.setProperty("jawk.extensions", MetricsHubAwk.class.getName());

		AwkSettings settings = AwkParameters.parseCommandLineArguments(new String[] { awkScript });
		settings.setUserExtensions(true);

		// Create the OutputStream
		final ByteArrayOutputStream resultBytesStream = new ByteArrayOutputStream();
		final UniformPrintStream resultStream = new UniformPrintStream(resultBytesStream);
		settings.setOutputStream(resultStream);

		// We don't want to see error messages because of formatting issues
		settings.setCatchIllegalFormatExceptions(true);

		// We force \n as the Record Separator (RS) because even if running on Windows
		// we're passing Java strings, where end of lines are simple \n
		settings.setDefaultRS("\n");

		MetricsHubAwk metricsHubAwk = MetricsHubAwk
			.builder()
			.telemetryManager(telemetryManager)
			.connectorId(connectorId)
			.sourceProcessor(sourceProcessor)
			.build();

		Map<String, org.sentrysoftware.jawk.ext.JawkExtension> extensionMap = Arrays
			.stream(metricsHubAwk.extensionKeywords())
			.collect(Collectors.toMap(key -> key, key -> metricsHubAwk));

		// Interpret
		final AVM avm = new AVM(settings, extensionMap);

		try {
			avm.interpret(getIntermediateCode(awkScript));
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}

		// Result
		final SourceTable sourceTable = new SourceTable();
		sourceTable.setRawData(resultBytesStream.toString());
		sourceTable.setTable(SourceTable.csvToTable(resultBytesStream.toString(), ";"));
		return sourceTable;
	}

	/**
	 * Generates the "Awk Tuples", i.e. the intermediate Awk code
	 * that can be interpreted afterward.
	 *
	 * @param script Awk script source code to be converted to intermediate code
	 * @return The actual AwkTuples to be interpreted
	 * @throws ParseException when the Awk script is wrong
	 */
	public static AwkTuples getIntermediateCode(final String script) throws ParseException {
		// All scripts need to be prefixed with an extra statement that sets the Record Separator (RS)
		// to the "normal" end-of-line (\n), because Jawk uses line.separator System property, which
		// is \r\n on Windows, thus preventing it from splitting lines properly.
		final ScriptSource awkHeader = new ScriptSource("Header", new StringReader("BEGIN { RS = \"\\n\"; }"), false);
		final ScriptSource awkSource = new ScriptSource("Body", new StringReader(script), false);
		final List<ScriptSource> sourceList = new ArrayList<>();
		sourceList.add(awkHeader);
		sourceList.add(awkSource);

		// Awk Setup
		final AwkSettings settings = new AwkSettings();
		settings.setCatchIllegalFormatExceptions(false);
		settings.setUseStdIn(false);

		// Parse the Awk script
		final AwkTuples tuples = new AwkTuples();
		final AwkParser parser = new AwkParser(false, false, false, Collections.emptyMap());
		final AwkSyntaxTree ast;
		try {
			ast = parser.parse(sourceList);

			// Produce the intermediate code
			if (ast != null) {
				// 1st pass to tie actual parameters to back-referenced formal parameters
				ast.semanticAnalysis();

				// 2nd pass to tie actual parameters to forward-referenced formal parameters
				ast.semanticAnalysis();
				if (ast.populateTuples(tuples) != 0) {
					throw new ParseException("Syntax problem with the Awk script", 0);
				}
				tuples.postProcess();
				parser.populateGlobalVariableNameToOffsetMappings(tuples);
			}
		} catch (IOException e) {
			throw new ParseException(e.getMessage(), 0);
		}

		return tuples;
	}
}
