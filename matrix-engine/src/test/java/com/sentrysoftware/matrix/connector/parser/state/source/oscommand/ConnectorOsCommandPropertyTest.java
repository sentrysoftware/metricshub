package com.sentrysoftware.matrix.connector.parser.state.source.oscommand;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.source.common.EntryConcatEndProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.EntryConcatMethodProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.EntryConcatStartProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.ExcludeRegExpProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.ExecuteForEachEntryOfProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.KeepOnlyRegExpProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.RemoveFooterProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.RemoveHeaderProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.SelectColumnsProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.SeparatorsProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.TypeProcessor;

class ConnectorOsCommandPropertyTest {

	@Test
	void testGetConnectorProperties() {
		assertEquals(
				Set.of(
						TypeProcessor.class,
						ForceSerializationProcessor.class,
						CommandLineProcessor.class,
						ExecuteLocallyProcessor.class,
						ExcludeRegExpProcessor.class,
						KeepOnlyRegExpProcessor.class,
						RemoveFooterProcessor.class,
						RemoveHeaderProcessor.class,
						SelectColumnsProcessor.class,
						SeparatorsProcessor.class,
						TimeoutProcessor.class,
						ExecuteForEachEntryOfProcessor.class,
						EntryConcatMethodProcessor.class,
						EntryConcatStartProcessor.class,
						EntryConcatEndProcessor.class),
				ConnectorOsCommandProperty
					.getConnectorProperties()
					.stream()
					.map(IConnectorStateParser::getClass)
					.collect(Collectors.toSet()));
	}
}
