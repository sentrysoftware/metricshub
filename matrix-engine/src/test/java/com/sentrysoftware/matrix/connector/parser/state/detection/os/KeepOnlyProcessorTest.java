package com.sentrysoftware.matrix.connector.parser.state.detection.os;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.OsType;
import com.sentrysoftware.matrix.connector.model.detection.Detection;
import com.sentrysoftware.matrix.connector.model.detection.criteria.Criterion;
import com.sentrysoftware.matrix.connector.model.detection.criteria.os.Os;

class KeepOnlyProcessorTest {

	private final KeepOnlyProcessor keepOnlyProcessor = new KeepOnlyProcessor();

	private final Connector connector = new Connector();

	private static final String CRITERION_KEEP_ONLY_KEY = "detection.criteria(1).keeponly";
	private static final String VALUE = "Solaris,OOB,Linux";
	private static final Set<OsType> RESULT = new HashSet<>(Arrays.asList(OsType.SOLARIS, OsType.OOB, OsType.LINUX));
	private static final String FOO = "FOO";

	@Test
	void testParse() {

		// Key does not match
		assertThrows(IllegalArgumentException.class, () -> keepOnlyProcessor.parse(FOO, FOO, connector));

		// Key matches, type is OS, detection is initially null
		Os os = Os.builder().index(1).build();
		Detection detection = Detection.builder().criteria(Collections.singletonList(os)).build();
		connector.setDetection(detection);
		keepOnlyProcessor.parse(CRITERION_KEEP_ONLY_KEY, VALUE, connector);
		List<Criterion> criteria = connector.getDetection().getCriteria();
		assertNotNull(criteria);
		assertEquals(1, criteria.size());
		Criterion criterion = criteria.get(0);
		assertTrue(criterion instanceof Os);
		assertEquals(1, criterion.getIndex());
		assertEquals(RESULT, ((Os) criterion).getKeepOnly());
	}
}
