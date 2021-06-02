package com.sentrysoftware.matrix.connector.parser.state.compute;

import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.compute.add.ConnectorAddProperty;
import com.sentrysoftware.matrix.connector.parser.state.compute.awk.ConnectorAwkProperty;
import com.sentrysoftware.matrix.connector.parser.state.compute.divide.ConnectorDivideProperty;
import com.sentrysoftware.matrix.connector.parser.state.compute.duplicatecolumn.ConnectorDuplicateColumnProperty;
import com.sentrysoftware.matrix.connector.parser.state.compute.excludematchinglines.ConnectorExcludeMatchingLinesProperty;
import com.sentrysoftware.matrix.connector.parser.state.compute.keeponlymatchinglines.ConnectorKeepOnlyMatchingLinesProperty;
import com.sentrysoftware.matrix.connector.parser.state.compute.leftconcat.ConnectorLeftConcatProperty;
import com.sentrysoftware.matrix.connector.parser.state.compute.multiply.ConnectorMultiplyProperty;
import com.sentrysoftware.matrix.connector.parser.state.compute.perbittranslation.ConnectorPerBitTranslationProperty;
import com.sentrysoftware.matrix.connector.parser.state.compute.replace.ConnectorReplaceProperty;
import com.sentrysoftware.matrix.connector.parser.state.compute.rightconcat.ConnectorRightConcatProperty;
import com.sentrysoftware.matrix.connector.parser.state.compute.translate.ConnectorTranslateProperty;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConnectorComputeProperty {

	private ConnectorComputeProperty() {}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
			.of(ConnectorAddProperty.getConnectorProperties(),
				ConnectorDivideProperty.getConnectorProperties(),
				ConnectorDuplicateColumnProperty.getConnectorProperties(),
				ConnectorExcludeMatchingLinesProperty.getConnectorProperties(),
				ConnectorKeepOnlyMatchingLinesProperty.getConnectorProperties(),
				ConnectorLeftConcatProperty.getConnectorProperties(),
				ConnectorMultiplyProperty.getConnectorProperties(),
				ConnectorPerBitTranslationProperty.getConnectorProperties(),
				ConnectorReplaceProperty.getConnectorProperties(),
				ConnectorRightConcatProperty.getConnectorProperties(),
				ConnectorTranslateProperty.getConnectorProperties(),
				ConnectorAwkProperty.getConnectorProperties())
			.flatMap(Set::stream)
			.collect(Collectors.toSet());
	}
}