package org.sentrysoftware.metricshub.engine.strategy.source.compute;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Add;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.And;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.ArrayTranslate;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Awk;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Compute;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Convert;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Divide;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.DuplicateColumn;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.ExcludeMatchingLines;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Extract;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.ExtractPropertyFromWbemPath;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Json2Csv;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.KeepColumns;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.KeepOnlyMatchingLines;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.LeftConcat;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Multiply;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.PerBitTranslation;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Replace;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.RightConcat;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Substring;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Subtract;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Translate;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Xml2Csv;
import org.sentrysoftware.metricshub.engine.strategy.source.SourceUpdaterProcessor;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ComputeUpdaterProcessor implements IComputeProcessor {

	private IComputeProcessor computeProcessor;
	private TelemetryManager telemetryManager;
	private String connectorId;
	private Map<String, String> attributes;

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
		processCompute(awk);
	}

	@Override
	public void process(final Convert convert) {
		processCompute(convert);
	}

	@Override
	public void process(final Divide divide) {
		processCompute(divide);
	}

	@Override
	public void process(final DuplicateColumn duplicateColumn) {
		processCompute(duplicateColumn);
	}

	@Override
	public void process(final ExcludeMatchingLines excludeMatchingLines) {
		processCompute(excludeMatchingLines);
	}

	@Override
	public void process(final Extract extract) {
		processCompute(extract);
	}

	@Override
	public void process(final ExtractPropertyFromWbemPath extractPropertyFromWbemPath) {
		processCompute(extractPropertyFromWbemPath);
	}

	@Override
	public void process(final Json2Csv json2csv) {
		processCompute(json2csv);
	}

	@Override
	public void process(final KeepColumns keepColumns) {
		processCompute(keepColumns);
	}

	@Override
	public void process(final KeepOnlyMatchingLines keepOnlyMatchingLines) {
		processCompute(keepOnlyMatchingLines);
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
		processCompute(perBitTranslation);
	}

	@Override
	public void process(final Replace replace) {
		processCompute(replace);
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
		processCompute(substring);
	}

	@Override
	public void process(final Translate translate) {
		processCompute(translate);
	}

	@Override
	public void process(final Xml2Csv xml2csv) {
		processCompute(xml2csv);
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
		copy.update(value -> SourceUpdaterProcessor.replaceAttributeReferences(value, attributes));

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
			connectorId,
			compute.getClass().getSimpleName(),
			compute.getType()
		);
	}
}
