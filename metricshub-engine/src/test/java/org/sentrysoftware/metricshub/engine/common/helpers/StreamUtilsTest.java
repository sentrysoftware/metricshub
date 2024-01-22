package org.sentrysoftware.metricshub.engine.common.helpers;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class StreamUtilsTest {

	@Test
	void testReverse() {
		assertEquals("val3", StreamUtils.reverse(Stream.of("val1", "val2", "val3")).findFirst().get());
	}
}
