package com.sentrysoftware.matrix.connector.parser.state.source.tablejoin;

import static com.sentrysoftware.matrix.connector.parser.state.source.tablejoin.TableJoinProcessor.TABLE_JOIN_TYPE_VALUE;

import java.util.Set;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tablejoin.TableJoinSource;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.source.common.EntryConcatEndProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.EntryConcatMethodProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.EntryConcatStartProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.ExecuteForEachEntryOfProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.TypeProcessor;

public class ConnectorTableJoinProperty {

	private ConnectorTableJoinProperty() {
	}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Set.of(
				new TypeProcessor(TableJoinSource.class, TABLE_JOIN_TYPE_VALUE),
				new ForceSerializationProcessor(TableJoinSource.class, TABLE_JOIN_TYPE_VALUE),
				new LeftTableProcessor(),
				new RightTableProcessor(),
				new LeftKeyColumnProcessor(),
				new RightKeyColumnProcessor(),
				new KeyTypeProcessor(),
				new DefaultRightLineProcessor(),
				new ExecuteForEachEntryOfProcessor(TableJoinSource.class, TABLE_JOIN_TYPE_VALUE),
				new EntryConcatMethodProcessor(TableJoinSource.class, TABLE_JOIN_TYPE_VALUE),
				new EntryConcatStartProcessor(TableJoinSource.class, TABLE_JOIN_TYPE_VALUE),
				new EntryConcatEndProcessor(TableJoinSource.class, TABLE_JOIN_TYPE_VALUE));
	}
}
