package com.sentrysoftware.matrix.connector.parser;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.Detection;
import com.sentrysoftware.matrix.connector.model.detection.criteria.Criterion;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMP;
import com.sentrysoftware.matrix.connector.model.detection.criteria.wbem.WBEM;
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
//                .parse("../matrix/connector-compiler-plugin/src/it/success/hardware-connectors/src/main/hdf/MS_HW_DellOpenManage.hdfs")
//                .parse("../hardware-connectors/src/main/hdf/MS_HW_AdptStorManUnix.hdfs")
//                .parse("../hardware-connectors/src/main/hdf/MS_HW_CpMgServTru64.hdfs")
//                .parse("../hardware-connectors/src/main/hdf/MS_HW_CiscoUCSCIMC.hdfs")
                .parse("../hardware-connectors/src/main/hdf/MS_HW_SunILOMSNMP.hdfs")
//                .parse("../hardware-connectors/src/main/hdf/MS_HW_Director4Linux.hdfs")
                .orElseThrow(() -> new IllegalStateException("Connector should not be null"));

        Detection detection = connector.getDetection();
        if (detection == null) {
            throw new IllegalStateException("Detection should not be null.");
        }

        for (Criterion criterion : detection.getCriteria()) {

			System.out.println("\nType:\t\t\t\t" + criterion.getClass().getSimpleName());
			System.out.println("index:\t\t\t\t" + criterion.getIndex());
			System.out.println("ForceSerialization:\t" + criterion.isForceSerialization());

			if (criterion instanceof SNMP) {

				SNMP snmpCriterion = (SNMP) criterion;

				System.out.println("OID:\t\t\t\t" + snmpCriterion.getOid());
				System.out.println("ExpectedResult:\t\t" + snmpCriterion.getExpectedResult());

            } else if (criterion instanceof WBEM) {

				WBEM wbemCriterion = (WBEM) criterion;

				System.out.println("WbemNamespace:\t\t\t\t" + wbemCriterion.getWbemNamespace());
				System.out.println("WbemQuery:\t\t\t\t" + wbemCriterion.getWbemQuery());
				System.out.println("ExpectedResult:\t\t" + wbemCriterion.getExpectedResult());
				System.out.println("ErrorMessage:\t" + wbemCriterion.getErrorMessage());

			} else {
				System.out.println("\n" + criterion);
			}
        }
    }
}
