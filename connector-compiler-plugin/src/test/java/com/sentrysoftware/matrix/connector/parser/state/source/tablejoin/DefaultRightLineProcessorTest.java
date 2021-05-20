package com.sentrysoftware.matrix.connector.parser.state.source.tablejoin;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tablejoin.TableJoinSource;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultRightLineProcessorTest {

	private final DefaultRightLineProcessor defaultRightLineProcessor = new DefaultRightLineProcessor();

	private final Connector connector = new Connector();

	private static final String DEFAULT_RIGHT_LINE_KEY = "enclosure.discovery.source(1).defaultrightline";
	private static final String FOO = "FOO";
	private static final String VALUE_ENDING_WITH_SEMI_COLON = ";;;;;;;;";
	private static final List<String> RESULT1 = Arrays.asList("", "", "", "", "", "", "", "");
	private static final List<String> RESULT2 = Collections.singletonList("FOO");

	@Test
	void testParse() {

		// Key does not match
		assertThrows(IllegalArgumentException.class, () -> defaultRightLineProcessor.parse(FOO, FOO, connector));

		// Key matches, source not found
		Discovery discovery = Discovery.builder().build();
		HardwareMonitor hardwareMonitor = HardwareMonitor
			.builder()
			.type(MonitorType.ENCLOSURE)
			.discovery(discovery)
			.build();
		connector.setHardwareMonitors(Collections.singletonList(hardwareMonitor));
		assertThrows(
			IllegalArgumentException.class,
			() -> defaultRightLineProcessor.parse(DEFAULT_RIGHT_LINE_KEY, FOO, connector));

		// Key matches, source found, value ends with <semicolon>
		TableJoinSource tableJoinSource = TableJoinSource.builder().index(1).build();
		discovery.setSources(Collections.singletonList(tableJoinSource));
		defaultRightLineProcessor.parse(DEFAULT_RIGHT_LINE_KEY, VALUE_ENDING_WITH_SEMI_COLON, connector);
		assertNotNull(connector.getHardwareMonitors());
		assertEquals(1, connector.getHardwareMonitors().size());
		assertEquals(hardwareMonitor, connector.getHardwareMonitors().get(0));
		assertEquals(discovery, connector.getHardwareMonitors().get(0).getDiscovery());
		assertNotNull(connector.getHardwareMonitors().get(0).getDiscovery().getSources());
		assertEquals(1, connector.getHardwareMonitors().get(0).getDiscovery().getSources().size());
		assertEquals(tableJoinSource, connector.getHardwareMonitors().get(0).getDiscovery().getSources().get(0));
		assertEquals(RESULT1, tableJoinSource.getDefaultRightLine());

		// Key matches, HardwareMonitor found, MonitorJob found, source found, value does not end with <semicolon>
		defaultRightLineProcessor.parse(DEFAULT_RIGHT_LINE_KEY, FOO, connector);
		assertEquals(RESULT2, tableJoinSource.getDefaultRightLine());
	}
}