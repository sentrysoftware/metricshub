package com.sentrysoftware.matrix.connector.parser.state.source.tableunion;

import static com.sentrysoftware.matrix.connector.parser.state.source.tableunion.TableUnionProcessor.TABLE_UNION_TYPE_VALUE;

import java.util.Set;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tableunion.TableUnionSource;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.source.common.EntryConcatEndProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.EntryConcatMethodProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.EntryConcatStartProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.ExecuteForEachEntryOfProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.TypeProcessor;

public class ConnectorTableUnionProperty {

	private ConnectorTableUnionProperty() {
	}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Set.of(
				new TypeProcessor(TableUnionSource.class, TABLE_UNION_TYPE_VALUE),
				new ForceSerializationProcessor(TableUnionSource.class, TABLE_UNION_TYPE_VALUE),
				new TableProcessor(),
				new ExecuteForEachEntryOfProcessor(TableUnionSource.class, TABLE_UNION_TYPE_VALUE),
				new EntryConcatMethodProcessor(TableUnionSource.class, TABLE_UNION_TYPE_VALUE),
				new EntryConcatStartProcessor(TableUnionSource.class, TABLE_UNION_TYPE_VALUE),
				new EntryConcatEndProcessor(TableUnionSource.class, TABLE_UNION_TYPE_VALUE));
	}
}
