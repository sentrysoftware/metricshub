package com.sentrysoftware.metricshub.converter.state.computes;

import com.sentrysoftware.metricshub.converter.state.IConnectorStateConverter;
import com.sentrysoftware.metricshub.converter.state.computes.add.ConnectorAddProperty;
import com.sentrysoftware.metricshub.converter.state.computes.and.ConnectorAndProperty;
import com.sentrysoftware.metricshub.converter.state.computes.arraytranslate.ConnectorArrayTranslateProperty;
import com.sentrysoftware.metricshub.converter.state.computes.awk.ConnectorAwkProperty;
import com.sentrysoftware.metricshub.converter.state.computes.convert.ConnectorConvertProperty;
import com.sentrysoftware.metricshub.converter.state.computes.divide.ConnectorDivideProperty;
import com.sentrysoftware.metricshub.converter.state.computes.duplicatecolumn.ConnectorDuplicateColumnProperty;
import com.sentrysoftware.metricshub.converter.state.computes.excludematchinglines.ConnectorExcludeMatchingLinesProperty;
import com.sentrysoftware.metricshub.converter.state.computes.extract.ConnectorExtractProperty;
import com.sentrysoftware.metricshub.converter.state.computes.extractpropertyfromwbempath.ConnectorExtractPropertyFromWbemPathProperty;
import com.sentrysoftware.metricshub.converter.state.computes.json2csv.ConnectorJson2CsvProperty;
import com.sentrysoftware.metricshub.converter.state.computes.keepcolumns.ConnectorKeepColumnsProperty;
import com.sentrysoftware.metricshub.converter.state.computes.keeponlymatchinglines.ConnectorKeepOnlyMatchingLinesProperty;
import com.sentrysoftware.metricshub.converter.state.computes.leftconcat.ConnectorLeftConcatProperty;
import com.sentrysoftware.metricshub.converter.state.computes.multiply.ConnectorMultiplyProperty;
import com.sentrysoftware.metricshub.converter.state.computes.perbittranslation.ConnectorPerBitTranslationProperty;
import com.sentrysoftware.metricshub.converter.state.computes.replace.ConnectorReplaceProperty;
import com.sentrysoftware.metricshub.converter.state.computes.rightconcat.ConnectorRightConcatProperty;
import com.sentrysoftware.metricshub.converter.state.computes.substring.ConnectorSubstringProperty;
import com.sentrysoftware.metricshub.converter.state.computes.subtract.ConnectorSubtractProperty;
import com.sentrysoftware.metricshub.converter.state.computes.translate.ConnectorTranslateProperty;
import com.sentrysoftware.metricshub.converter.state.computes.xml2csv.ConnectorXml2CsvProperty;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorComputeProperty {

	public static Set<IConnectorStateConverter> getConnectorProperties() {
		return Stream
			.of(
				ConnectorAddProperty.getConnectorProperties(),
				ConnectorAndProperty.getConnectorProperties(),
				ConnectorArrayTranslateProperty.getConnectorProperties(),
				ConnectorAwkProperty.getConnectorProperties(),
				ConnectorConvertProperty.getConnectorProperties(),
				ConnectorDivideProperty.getConnectorProperties(),
				ConnectorDuplicateColumnProperty.getConnectorProperties(),
				ConnectorExcludeMatchingLinesProperty.getConnectorProperties(),
				ConnectorExtractProperty.getConnectorProperties(),
				ConnectorExtractPropertyFromWbemPathProperty.getConnectorProperties(),
				ConnectorJson2CsvProperty.getConnectorProperties(),
				ConnectorKeepColumnsProperty.getConnectorProperties(),
				ConnectorKeepOnlyMatchingLinesProperty.getConnectorProperties(),
				ConnectorLeftConcatProperty.getConnectorProperties(),
				ConnectorMultiplyProperty.getConnectorProperties(),
				ConnectorPerBitTranslationProperty.getConnectorProperties(),
				ConnectorReplaceProperty.getConnectorProperties(),
				ConnectorRightConcatProperty.getConnectorProperties(),
				ConnectorSubtractProperty.getConnectorProperties(),
				ConnectorSubstringProperty.getConnectorProperties(),
				ConnectorTranslateProperty.getConnectorProperties(),
				ConnectorXml2CsvProperty.getConnectorProperties()
			)
			.flatMap(Set::stream)
			.collect(Collectors.toSet());
	}
}
