package com.sentrysoftware.matrix.connector.parser;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Add;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.ArrayTranslate;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Compute;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Divide;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.DuplicateColumn;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Extract;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.KeepColumns;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.KeepOnlyMatchingLines;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Multiply;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.PerBitTranslation;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Replace;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Substract;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Translate;
import com.sentrysoftware.matrix.connector.parser.state.ConnectorState;
import lombok.Data;
import org.springframework.util.Assert;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class ConnectorParser {

	/**
	 * Process Connector file
	 *
	 * @param connectorFilePath	The path of the {@link Connector} file
	 * @return {@link Optional} of {@link Connector} instance
	 */
	public Optional<Connector> parse(final String connectorFilePath) {

		Assert.isTrue(
				connectorFilePath != null && !connectorFilePath.trim().isEmpty(),
				"connectorFilePath cannot be null or empty"
		);

		try {

			final ConnectorRefined connectorRefined = new ConnectorRefined();

			connectorRefined.load(connectorFilePath);

			return Optional.of(parseContent(connectorRefined));

		} catch (Exception e) {

			throw new IllegalStateException(

					String.format(
							"Cannot load Connector file %s. Message: %s",
							connectorFilePath,
							e.getMessage()
					)
			);
		}
	}

	/**
	 * From the given {@link ConnectorRefined} object parse the whole Connector content
	 * @param connectorRefined	The refined connector
	 * @return {@link Connector} instance.
	 */
	private static Connector parseContent(final ConnectorRefined connectorRefined) {

		final Connector connector = new Connector();

		connector.setEmbeddedFiles(connectorRefined.getEmbeddedFiles());
		connector.setTranslationTables(connectorRefined.getTranslationTables());
		connector.setCompiledFilename(connectorRefined.getCompiledFilename());

		connectorRefined.getCodeMap().forEach((key, value) -> parseKeyValue(key, value, connector));

		return connector;
	}

	/**
	 * Detect and parse the given line
	 * @param key the Connector key we wish to extract its value
	 * @param value the corresponding value we wish to process
	 * @param connector {@link Connector} instance to update
	 */
	private static void parseKeyValue(final String key, final String value, final Connector connector) {

		// Get the detected state
		final Set<ConnectorState> connectorStates = ConnectorState
				.getConnectorStates()
				.stream()
				.filter(state -> state.detect(key, value, connector))
				.collect(Collectors.toSet());

		Optional<ConnectorState> firstConnectorState = connectorStates.stream().findFirst();
		firstConnectorState.ifPresent(connectorState -> connectorState.parse(key, value, connector));

		// TODO if the Connector defines an IPMI source then add "ipmitool" to the list of sudoCommands in the Connector bean.
	}

		public static void main(String[] args) {

        Connector connector = new ConnectorParser()
                .parse("../matrix/connector-compiler-plugin/src/it/success/hardware-connectors/src/main/hdf/MS_HW_DellOpenManage.hdfs")
//                .parse("../hardware-connectors/src/main/hdf/MS_HW_AdptStorManUnix.hdfs")
//                .parse("../hardware-connectors/src/main/hdf/MS_HW_CpMgServTru64.hdfs")
//                .parse("../hardware-connectors/src/main/hdf/MS_HW_CiscoUCSCIMC.hdfs")
//                .parse("../hardware-connectors/src/main/hdf/MS_HW_SunILOMSNMP.hdfs")
//                .parse("../hardware-connectors/src/main/hdf/MS_HW_SunXsigoSwitch.hdfs")
//                .parse("../hardware-connectors/src/main/hdf/MS_HW_ServerviewNT.hdfs")
//                .parse("../hardware-connectors/src/main/hdf/MS_HW_NetApp.hdfs")
                .orElseThrow(() -> new IllegalStateException("Connector should not be null"));

		for (HardwareMonitor hardwareMonitor : connector.getHardwareMonitors()) {

			System.out.println("\n\n\nMonitor: " + hardwareMonitor.getType());

			for (Source source : hardwareMonitor.getCollect().getSources()) {

				System.out.println("\n\nSource index: " + source.getIndex());

				if (source.getComputes() != null) {

					for (Compute compute : source.getComputes()) {

						System.out.println("\nCompute index: " + compute.getIndex());
						System.out.println("Type: " + compute.getClass().getSimpleName());

						if (compute instanceof KeepOnlyMatchingLines) {

							System.out.println("Column: " + ((KeepOnlyMatchingLines) compute).getColumn());
							System.out.println("Value list: " + ((KeepOnlyMatchingLines) compute).getValueList());
							System.out.println("Regexp: " + ((KeepOnlyMatchingLines) compute).getRegExp());

						} else if (compute instanceof DuplicateColumn) {

							System.out.println("Column: " + ((DuplicateColumn) compute).getColumn());

						} else if (compute instanceof Translate) {

							System.out.println("Column: " + ((Translate) compute).getColumn());
							System.out.println("Translation table name: " + ((Translate) compute).getTranslationTable().getName());
							System.out.println("Translation table translations: " + ((Translate) compute).getTranslationTable().getTranslations());

						} else if (compute instanceof Add) {

							System.out.println("Column: " + ((Add) compute).getColumn());
							System.out.println("Add: " + ((Add) compute).getAdd());

						} else if (compute instanceof Substract) {

							System.out.println("Column: " + ((Substract) compute).getColumn());
							System.out.println("Substract: " + ((Substract) compute).getSubstract());

						} else if (compute instanceof Multiply) {

							System.out.println("Column: " + ((Multiply) compute).getColumn());
							System.out.println("Multiply by: " + ((Multiply) compute).getMultiplyBy());

						} else if (compute instanceof Divide) {

							System.out.println("Column: " + ((Divide) compute).getColumn());
							System.out.println("Divide by: " + ((Divide) compute).getDivideBy());

						} else if (compute instanceof PerBitTranslation) {

							System.out.println("Column: " + ((PerBitTranslation) compute).getColumn());
							System.out.println("Bit translation table name: " + ((PerBitTranslation) compute).getBitTranslationTable().getName());
							System.out.println("Bit translation table translations: " + ((PerBitTranslation) compute).getBitTranslationTable().getTranslations());
							System.out.println("Bit list: " + ((PerBitTranslation) compute).getBitList());

						} else if (compute instanceof Replace) {

							System.out.println("Column: " + ((Replace) compute).getColumn());
							System.out.println("Replace: " + ((Replace) compute).getReplace());
							System.out.println("Replace by: " + ((Replace) compute).getReplaceBy());

						} else if (compute instanceof KeepColumns) {

							System.out.println("ColumnNumbers: " + ((KeepColumns) compute).getColumnNumbers());

						} else if (compute instanceof Extract) {

							System.out.println("Column: " + ((Extract) compute).getColumn());
							System.out.println("SubColumn: " + ((Extract) compute).getSubColumn());
							System.out.println("SubSeparators: " + ((Extract) compute).getSubSeparators());

						} else if (compute instanceof ArrayTranslate) {

							System.out.println("Column: " + ((ArrayTranslate) compute).getColumn());
							System.out.println("Translation table: " + ((ArrayTranslate) compute).getTranslationTable());
							System.out.println("Array separator: " + ((ArrayTranslate) compute).getArraySeparator());
							System.out.println("Result separator: " + ((ArrayTranslate) compute).getResultSeparator());
						}
					}
				}
			}
		}
	}
}
