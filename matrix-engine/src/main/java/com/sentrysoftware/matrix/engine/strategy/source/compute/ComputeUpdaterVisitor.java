package com.sentrysoftware.matrix.engine.strategy.source.compute;

import static com.sentrysoftware.matrix.engine.strategy.source.SourceUpdaterVisitor.replaceSourceReferenceContent;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Add;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.And;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.ArrayTranslate;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Awk;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Compute;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Convert;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Divide;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.DuplicateColumn;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.ExcludeMatchingLines;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Extract;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.ExtractPropertyFromWbemPath;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Json2Csv;
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
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Xml2Csv;
import com.sentrysoftware.matrix.engine.strategy.StrategyConfig;
import com.sentrysoftware.matrix.engine.strategy.source.SourceTable;
import com.sentrysoftware.matrix.engine.strategy.source.SourceUpdaterVisitor;
import com.sentrysoftware.matrix.model.monitor.Monitor;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ComputeUpdaterVisitor implements IComputeVisitor {

	private IComputeVisitor computeVisitor;
	private Monitor monitor;
	private Connector connector;
	private StrategyConfig strategyConfig;

	@Override
	public void visit(final ArrayTranslate arrayTranslate) {
		arrayTranslate.accept(computeVisitor);
	}

	@Override
	public void visit(final And and) {
		processCompute(and);
	}

	@Override
	public void visit(final Add add) {
		processCompute(add);
	}

	@Override
	public void visit(final Awk awk) {
		processCompute(awk);
	}

	@Override
	public void visit(final Convert convert) {
		processCompute(convert);
	}

	@Override
	public void visit(final Divide divide) {
		processCompute(divide);
	}

	@Override
	public void visit(final DuplicateColumn duplicateColumn) {
		processCompute(duplicateColumn);
	}

	@Override
	public void visit(final ExcludeMatchingLines excludeMatchingLines) {
		processCompute(excludeMatchingLines);
	}

	@Override
	public void visit(final Extract extract) {
		processCompute(extract);
	}

	@Override
	public void visit(final ExtractPropertyFromWbemPath extractPropertyFromWbemPath) {
		processCompute(extractPropertyFromWbemPath);
	}

	@Override
	public void visit(final Json2Csv json2csv) {
		processCompute(json2csv);
	}

	@Override
	public void visit(final KeepColumns keepColumns) {
		processCompute(keepColumns);
	}

	@Override
	public void visit(final KeepOnlyMatchingLines keepOnlyMatchingLines) {
		processCompute(keepOnlyMatchingLines);
	}

	@Override
	public void visit(final LeftConcat leftConcat) {
		processCompute(leftConcat);
	}

	@Override
	public void visit(final Multiply multiply) {
		processCompute(multiply);
	}

	@Override
	public void visit(final PerBitTranslation perBitTranslation) {
		processCompute(perBitTranslation);
	}

	@Override
	public void visit(final Replace replace) {
		processCompute(replace);
	}

	@Override
	public void visit(final RightConcat rightConcat) {
		processCompute(rightConcat);
	}

	@Override
	public void visit(final Substract substract) {
		processCompute(substract);
	}

	@Override
	public void visit(final Substring substring) {
		processCompute(substring);
	}

	@Override
	public void visit(final Translate translate) {
		processCompute(translate);
	}

	@Override
	public void visit(final Xml2Csv xml2csv) {
		processCompute(xml2csv);
	}

	@Override
	public void setSourceTable(SourceTable sourceTable) {
		computeVisitor.setSourceTable(sourceTable);
	}

	@Override
	public SourceTable getSourceTable() {
		return computeVisitor.getSourceTable();
	}


	/**
	 * Copy the given compute, replace device id when running mono-instance collects, replace
	 * source reference and finally call the compute visitor
	 * 
	 * @param origin original compute instance
	 */
	private void processCompute(final Compute origin) {

		// Deep copy
		final Compute copy = origin.copy();

		// Replace device id (mono instance)
		copy.update(value -> SourceUpdaterVisitor.replaceDeviceId(value, monitor));

		// Replace source reference
		copy.update(value -> replaceSourceReference(value, copy));

		// Call the next compute visitor
		copy.accept(computeVisitor);
	}

	/**
	 * Replace referenced source in the given compute attributes
	 * 
	 * @param value The value containing a source reference such as %Enclosure.Discovery.Source(1)%.
	 * @param compute {@link Compute} instance we wish to update with the content of the referenced source
	 * @return String value
	 */
	private String replaceSourceReference(final String value, final Compute compute) {

		// Null check
		if (value == null) {
			return value;
		}

		return replaceSourceReferenceContent(
			value,
			strategyConfig,
			connector,
			compute.getClass().getSimpleName(),
			compute.getIndex()
		);

	}

}
