package com.sentrysoftware.matrix.connector.parser.state.source.sshinteractive;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.sshinteractive.SshInteractiveSource;
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
import com.sentrysoftware.matrix.connector.parser.state.source.sshinteractive.step.ConnectorSourceSshInteractiveStepProperty;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorSshInteractiveProperty {

	private static final Class<SshInteractiveSource> TYPE = SshInteractiveSource.class;

	private static final String TYPE_VALUE = "TelnetInteractive";

	public static Set<IConnectorStateParser> getConnectorProperties() {
		final Set<IConnectorStateParser> sshInteractive = Set.of(
				new TypeProcessor(TYPE, TYPE_VALUE),
				new ForceSerializationProcessor(TYPE, TYPE_VALUE),
				new ExcludeRegExpProcessor(TYPE, TYPE_VALUE),
				new KeepOnlyRegExpProcessor(TYPE, TYPE_VALUE),
				new RemoveFooterProcessor(TYPE, TYPE_VALUE),
				new RemoveHeaderProcessor(TYPE, TYPE_VALUE),
				new SelectColumnsProcessor(TYPE, TYPE_VALUE),
				new SeparatorsProcessor(TYPE, TYPE_VALUE),
				new ExecuteForEachEntryOfProcessor(TYPE, TYPE_VALUE),
				new EntryConcatMethodProcessor(TYPE, TYPE_VALUE),
				new EntryConcatStartProcessor(TYPE, TYPE_VALUE),
				new EntryConcatEndProcessor(TYPE, TYPE_VALUE));

		return Stream.concat(sshInteractive.stream(), ConnectorSourceSshInteractiveStepProperty.getConnectorProperties())
				.collect(Collectors.toSet());
	}
}
