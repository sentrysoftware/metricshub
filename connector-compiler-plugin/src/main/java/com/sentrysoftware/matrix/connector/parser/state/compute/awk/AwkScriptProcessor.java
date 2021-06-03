package com.sentrysoftware.matrix.connector.parser.state.compute.awk;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.Assert;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.EmbeddedFile;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Awk;

public class AwkScriptProcessor extends AwkProcessor {

	private static final Pattern AWK_SCRIPT_KEY_PATTERN = Pattern.compile(
			"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.compute\\(([1-9]\\d*)\\)\\.awkscript\\s*$",
			Pattern.CASE_INSENSITIVE);

	private static final Pattern EMBEDDED_PATTERN = Pattern.compile(
			"^\\s*embeddedfile\\((\\d+)\\)\\s*$",
			Pattern.CASE_INSENSITIVE);

	@Override
	public Matcher getMatcher(String key) {
		return AWK_SCRIPT_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		Matcher embeddedFileMatcher = EMBEDDED_PATTERN.matcher(value);
		embeddedFileMatcher.find();

		int embeddedFileIndex;

		try {
			embeddedFileIndex = Integer.parseInt(embeddedFileMatcher.group(1));
		} catch(NumberFormatException e) {
			throw new IllegalStateException(
					"AwkScriptProcessor parse: Could not instantiate EmbeddedFile from Source ("
							+ value
							+ "): "
							+ e.getMessage());
		}

		EmbeddedFile embeddedFile = connector.getEmbeddedFiles().get(embeddedFileIndex);

		Assert.state(embeddedFile != null, () -> "AwkScriptProcessor parse: Could not find EmbeddedFile in Source (" + value + ")");

		((Awk) getCompute(key, connector)).setAwkScript(embeddedFile);
	}

}
