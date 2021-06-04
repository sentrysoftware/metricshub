package com.sentrysoftware.matrix.engine.strategy.source.compute;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.Assert;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Add;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.And;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.ArrayTranslate;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Awk;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Convert;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Divide;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.DuplicateColumn;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.ExcludeMatchingLines;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Extract;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.ExtractPropertyFromWbemPath;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Json2CSV;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.KeepColumns;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.KeepOnlyMatchingLines;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.LeftConcat;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Multiply;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.PerBitTranslation;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Replace;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.RightConcat;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Substract;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Substring;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Translate;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.XML2CSV;
import com.sentrysoftware.matrix.engine.strategy.source.SourceTable;
import com.sentrysoftware.matrix.model.monitor.Monitor;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ComputeUpdaterVisitor implements IComputeVisitor {

	private static final Pattern MONO_INSTANCE_REPLACEMENT_PATTERN = Pattern.compile("%\\w+\\.collect\\.deviceid%", Pattern.CASE_INSENSITIVE);

	private IComputeVisitor computeVisitor;
	private Monitor monitor;

	@Override
	public void visit(final ArrayTranslate arrayTranslate) {
		arrayTranslate.accept(computeVisitor);
	}

	@Override
	public void visit(final And and) {
		and.accept(computeVisitor);
	}

	@Override
	public void visit(final Add add) {
		add.accept(computeVisitor);
	}

	@Override
	public void visit(final Awk awk) {
		awk.accept(computeVisitor);
	}

	@Override
	public void visit(final Convert convert) {
		convert.accept(computeVisitor);
	}

	@Override
	public void visit(final Divide divide) {
		divide.accept(computeVisitor);
	}

	@Override
	public void visit(final DuplicateColumn duplicateColumn) {
		duplicateColumn.accept(computeVisitor);
	}

	@Override
	public void visit(final ExcludeMatchingLines excludeMatchingLines) {
		excludeMatchingLines.accept(computeVisitor);
	}

	@Override
	public void visit(final Extract extract) {
		extract.accept(computeVisitor);
	}

	@Override
	public void visit(final ExtractPropertyFromWbemPath extractPropertyFromWbemPath) {
		extractPropertyFromWbemPath.accept(computeVisitor);
	}

	@Override
	public void visit(final Json2CSV json2csv) {
		json2csv.accept(computeVisitor);
	}

	@Override
	public void visit(final KeepColumns keepColumns) {
		keepColumns.accept(computeVisitor);
	}

	@Override
	public void visit(final KeepOnlyMatchingLines keepOnlyMatchingLines) {
		keepOnlyMatchingLines.accept(computeVisitor);
	}

	@Override
	public void visit(final LeftConcat leftConcat) {
		leftConcat.accept(computeVisitor);
	}

	@Override
	public void visit(final Multiply multiply) {
		multiply.accept(computeVisitor);
	}

	@Override
	public void visit(final PerBitTranslation perBitTranslation) {
		perBitTranslation.accept(computeVisitor);
	}

	@Override
	public void visit(final Replace replace) {
		replace.accept(computeVisitor);
	}

	@Override
	public void visit(final RightConcat rightConcat) {
		rightConcat.accept(computeVisitor);
	}

	@Override
	public void visit(final Substract substract) {
		substract.accept(computeVisitor);
	}

	@Override
	public void visit(final Substring substring) {
		if (monitor != null) {
			final Substring copy = substring.copy();
			doSubstringReplacements(copy, monitor);
			copy.accept(computeVisitor);
			return;
		}
		substring.accept(computeVisitor);
	}

	/**
	 * Replace the Substring Start/Length fields by the monitor id (mono-instance handling)
	 * 
	 * @param substring The substring instance we want to update
	 * @param monitor   The monitor we currently collect
	 */
	static void doSubstringReplacements(final Substring substring, final Monitor monitor) {
		if (!ComputeVisitor.checkSubstring(substring) || monitor == null) {
			return;
		}

		Assert.notNull(monitor.getMetadata(), "monitor metadata cannot be null.");
		Assert.notNull(monitor.getMetadata().get(HardwareConstants.DEVICE_ID), "monitor deviceId cannot be null.");

		final String deviceId = monitor.getMetadata().get(HardwareConstants.DEVICE_ID);

		substring.setStart(monoInstanceReplace(substring.getStart(), deviceId));
		substring.setLength(monoInstanceReplace(substring.getLength(),deviceId));
	}

	/**
	 * Perform the mono instance replacement on the given value
	 * 
	 * @param value       The value we wish to perform the replacement
	 * @param replacement The new string to use
	 * @return {@link String} value
	 */
	static String monoInstanceReplace(final String value, final String replacement) {

		Assert.notNull(value, "value cannot be null.");
		Assert.notNull(replacement, "replacement cannot be null.");

		final Matcher matcher = MONO_INSTANCE_REPLACEMENT_PATTERN.matcher(value);

		final StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			matcher.appendReplacement(sb, replacement);
		}
		matcher.appendTail(sb);

		return sb.toString();
	}

	@Override
	public void visit(final Translate translate) {
		translate.accept(computeVisitor);
	}

	@Override
	public void visit(final XML2CSV xml2csv) {
		xml2csv.accept(computeVisitor);
	}

	@Override
	public void setSourceTable(SourceTable sourceTable) {
		computeVisitor.setSourceTable(sourceTable);
	}

	@Override
	public SourceTable getSourceTable() {
		return computeVisitor.getSourceTable();
	}
}
