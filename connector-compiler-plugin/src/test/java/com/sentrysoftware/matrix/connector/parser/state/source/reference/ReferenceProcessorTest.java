package com.sentrysoftware.matrix.connector.parser.state.source.reference;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.reference.ReferenceSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.reference.StaticSource;

public class ReferenceProcessorTest {

	private static final String KEY = "enclosure.discovery.source(2)";
	private static final String REFERENCE_VALUE = "%enclosure.discovery.source(1)%";
	private static final String REFERENCE_RESULT = "enclosure.discovery.source(1)";

	private static final String STATIC_VALUE = "val1;val2;val3";

	@Test
	void testReferenceParse() {
		Connector connector = new Connector();

		new ReferenceProcessor().parse(KEY, REFERENCE_VALUE, connector);

		Source source = connector.getHardwareMonitors().get(0).getDiscovery().getSources().get(0);
		assertEquals(ReferenceSource.class, source.getClass());
		assertEquals(REFERENCE_RESULT, ((ReferenceSource) source).getReference());
	}

	@Test
	void testStaticParse() {
		Connector connector = new Connector();

		new ReferenceProcessor().parse(KEY, STATIC_VALUE, connector);

		Source source = connector.getHardwareMonitors().get(0).getDiscovery().getSources().get(0);
		assertEquals(StaticSource.class, source.getClass());
		assertEquals(STATIC_VALUE, ((StaticSource) source).getReference());
	}
}
