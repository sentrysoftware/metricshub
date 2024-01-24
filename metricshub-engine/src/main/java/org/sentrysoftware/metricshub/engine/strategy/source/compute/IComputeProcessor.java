package org.sentrysoftware.metricshub.engine.strategy.source.compute;

import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Add;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.And;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.ArrayTranslate;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Awk;
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

/**
 * Interface for a ComputeProcessor responsible for processing various compute operations on source data.
 * Implementations of this interface should handle specific types of compute operations, such as Add, And,
 * ArrayTranslate, Awk, Convert, Divide, DuplicateColumn, ExcludeMatchingLines, Extract, ExtractPropertyFromWbemPath,
 * Json2Csv, KeepColumns, KeepOnlyMatchingLines, LeftConcat, Multiply, PerBitTranslation, Replace, RightConcat,
 * Substring, Subtract, Translate, and Xml2Csv.
 */
public interface IComputeProcessor {
	/**
	 * Processes the Add compute operation on the source data.
	 *
	 * @param add The Add compute operation to be processed.
	 */
	void process(Add add);

	/**
	 * Processes the And compute operation on the source data.
	 *
	 * @param and The And compute operation to be processed.
	 */
	void process(And and);

	/**
	 * Processes the ArrayTranslate compute operation on the source data.
	 *
	 * @param arrayTranslate The ArrayTranslate compute operation to be processed.
	 */
	void process(ArrayTranslate arrayTranslate);

	/**
	 * Processes the Awk compute operation on the source data.
	 *
	 * @param awk The Awk compute operation to be processed.
	 */
	void process(Awk awk);

	/**
	 * Processes the Convert compute operation on the source data.
	 *
	 * @param convert The Convert compute operation to be processed.
	 */
	void process(Convert convert);

	/**
	 * Processes the Divide compute operation on the source data.
	 *
	 * @param divide The Divide compute operation to be processed.
	 */
	void process(Divide divide);

	/**
	 * Processes the DuplicateColumn compute operation on the source data.
	 *
	 * @param duplicateColumn The DuplicateColumn compute operation to be processed.
	 */
	void process(DuplicateColumn duplicateColumn);

	/**
	 * Processes the ExcludeMatchingLines compute operation on the source data.
	 *
	 * @param excludeMatchingLines The ExcludeMatchingLines compute operation to be processed.
	 */
	void process(ExcludeMatchingLines excludeMatchingLines);

	/**
	 * Processes the Extract compute operation on the source data.
	 *
	 * @param extract The Extract compute operation to be processed.
	 */
	void process(Extract extract);

	/**
	 * Processes the ExtractPropertyFromWbemPath compute operation on the source data.
	 *
	 * @param extractPropertyFromWbemPath The ExtractPropertyFromWbemPath compute operation to be processed.
	 */
	void process(ExtractPropertyFromWbemPath extractPropertyFromWbemPath);

	/**
	 * Processes the Json2Csv compute operation on the source data.
	 *
	 * @param json2Csv The Json2Csv compute operation to be processed.
	 */
	void process(Json2Csv json2Csv);

	/**
	 * Processes the KeepColumns compute operation on the source data.
	 *
	 * @param keepColumns The KeepColumns compute operation to be processed.
	 */
	void process(KeepColumns keepColumns);

	/**
	 * Processes the KeepOnlyMatchingLines compute operation on the source data.
	 *
	 * @param keepOnlyMatchingLines The KeepOnlyMatchingLines compute operation to be processed.
	 */
	void process(KeepOnlyMatchingLines keepOnlyMatchingLines);

	/**
	 * Processes the LeftConcat compute operation on the source data.
	 *
	 * @param leftConcat The LeftConcat compute operation to be processed.
	 */
	void process(LeftConcat leftConcat);

	/**
	 * Processes the Multiply compute operation on the source data.
	 *
	 * @param multiply The Multiply compute operation to be processed.
	 */
	void process(Multiply multiply);

	/**
	 * Processes the PerBitTranslation compute operation on the source data.
	 *
	 * @param perBitTranslation The PerBitTranslation compute operation to be processed.
	 */
	void process(PerBitTranslation perBitTranslation);

	/**
	 * Processes the Replace compute operation on the source data.
	 *
	 * @param replace The Replace compute operation to be processed.
	 */
	void process(Replace replace);

	/**
	 * Processes the RightConcat compute operation on the source data.
	 *
	 * @param rightConcat The RightConcat compute operation to be processed.
	 */
	void process(RightConcat rightConcat);

	/**
	 * Processes the Substring compute operation on the source data.
	 *
	 * @param substring The Substring compute operation to be processed.
	 */
	void process(Substring substring);

	/**
	 * Processes the Subtract compute operation on the source data.
	 *
	 * @param subtract The Subtract compute operation to be processed.
	 */
	void process(Subtract subtract);

	/**
	 * Processes the Translate compute operation on the source data.
	 *
	 * @param translate The Translate compute operation to be processed.
	 */
	void process(Translate translate);

	/**
	 * Processes the Xml2Csv compute operation on the source data.
	 *
	 * @param xml2Csv The Xml2Csv compute operation to be processed.
	 */
	void process(Xml2Csv xml2Csv);
}
