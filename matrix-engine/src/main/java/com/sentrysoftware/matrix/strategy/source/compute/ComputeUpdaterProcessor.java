package com.sentrysoftware.matrix.strategy.source.compute;

import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Add;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.And;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.ArrayTranslate;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Awk;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Compute;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Convert;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Divide;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.DuplicateColumn;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.ExcludeMatchingLines;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Extract;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.ExtractPropertyFromWbemPath;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Json2Csv;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.KeepColumns;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.KeepOnlyMatchingLines;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.LeftConcat;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Multiply;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.PerBitTranslation;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Replace;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.RightConcat;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Substring;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Subtract;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Translate;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Xml2Csv;
import com.sentrysoftware.matrix.strategy.source.SourceUpdaterProcessor;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ComputeUpdaterProcessor implements IComputeProcessor {

	private IComputeProcessor computeProcessor;
	private TelemetryManager telemetryManager;
	private String connectorName;
	private String monitorId;

	@Override
	public void process(final ArrayTranslate arrayTranslate) {
		arrayTranslate.accept(computeProcessor);
	}

	@Override
	public void process(final And and) {
		processCompute(and);
	}

	@Override
	public void process(final Add add) {
		processCompute(add);
	}

	@Override
	public void process(final Awk awk) {
		// TODO Auto-generated method stub
	}

	@Override
	public void process(final Convert convert) {
		// TODO Auto-generated method stub
	}

	@Override
	public void process(final Divide divide) {
		processCompute(divide);
	}

	@Override
	public void process(final DuplicateColumn duplicateColumn) {
		// TODO Auto-generated method stub
	}

	@Override
	public void process(final ExcludeMatchingLines excludeMatchingLines) {
		// TODO Auto-generated method stub
	}

	@Override
	public void process(final Extract extract) {
		// TODO Auto-generated method stub
	}

	@Override
	public void process(final ExtractPropertyFromWbemPath extractPropertyFromWbemPath) {
		// TODO Auto-generated method stub
	}

	@Override
	public void process(final Json2Csv json2csv) {
		// TODO Auto-generated method stub
	}

	@Override
	public void process(final KeepColumns keepColumns) {
		// TODO Auto-generated method stub
	}

	@Override
	public void process(final KeepOnlyMatchingLines keepOnlyMatchingLines) {
		// TODO Auto-generated method stub
	}

	@Override
	public void process(final LeftConcat leftConcat) {
		processCompute(leftConcat);
	}

	@Override
	public void process(final Multiply multiply) {
		processCompute(multiply);
	}

	@Override
	public void process(final PerBitTranslation perBitTranslation) {
		// TODO Auto-generated method stub
	}

	@Override
	public void process(final Replace replace) {
		// TODO Auto-generated method stub
	}

	@Override
	public void process(final RightConcat rightConcat) {
		processCompute(rightConcat);
	}

	@Override
	public void process(final Subtract subtract) {
		processCompute(subtract);
	}

	@Override
	public void process(final Substring substring) {
		// TODO Auto-generated method stub
	}

	@Override
	public void process(final Translate translate) {
		// TODO Auto-generated method stub
	}

	@Override
	public void process(final Xml2Csv xml2csv) {
		// TODO Auto-generated method stub
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
		copy.update(value -> SourceUpdaterProcessor.replaceDeviceId(value, monitorId));

		// Replace source reference
		copy.update(value -> replaceSourceReference(value, copy));

		// Call the next compute visitor
		copy.accept(computeProcessor);
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

		return SourceUpdaterProcessor.replaceSourceReferenceContent(
			value,
			telemetryManager,
			connectorName,
			compute.getClass().getSimpleName(),
			compute.getType()
		);
	}
}
