package com.sentrysoftware.matrix.engine.strategy.utils;

import org.junit.jupiter.api.Test;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.EMPTY;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.NEW_LINE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.WHITE_SPACE;
import static org.junit.jupiter.api.Assertions.*;

class PslUtilsTest {

	@Test
	void testPsl2JavaRegex() {

		// pslRegex is null
		assertEquals(EMPTY, PslUtils.psl2JavaRegex(null));

		// pslRegex is empty
		assertEquals(EMPTY, PslUtils.psl2JavaRegex(EMPTY));

		// pslRegex is not null and not empty, contains a '\' in a range
		assertEquals("[ab\\\\cd]", PslUtils.psl2JavaRegex("[ab\\cd]"));

		// pslRegex is not null and not empty, contains a '\' not in a range, next character is '<' or '>'
		assertEquals("ab\\bd\\be", PslUtils.psl2JavaRegex("ab\\<d\\>e"));

		// pslRegex is not null and not empty, contains a '\' not in a range, next character is special
		assertEquals("ab\\+", PslUtils.psl2JavaRegex("ab\\+"));

		// pslRegex is not null and not empty, contains a '\' not in a range, next character is regular
		assertEquals("abcd", PslUtils.psl2JavaRegex("ab\\cd"));

		// pslRegex is not null and not empty, ends with '\'
		assertEquals("ab\\", PslUtils.psl2JavaRegex("ab\\"));

		// pslRegex is not null and not empty, contains '(', ')', '|', '{' or '}'
		assertEquals("\\(\\)\\|\\{\\}", PslUtils.psl2JavaRegex("()|{}"));
	}

	@Test
	void testNthArgf() {

		// text is null
		assertNull(PslUtils.nthArgf(null, null, null, null));

		// text is not null, selectColumns is null
		assertNull(PslUtils.nthArgf(EMPTY, null, null, null));

		// text is not null, selectColumns is not null, separators is null
		assertNull(PslUtils.nthArgf(EMPTY, EMPTY, null, null));

		// selectColumns is not null, separators is not null, text is empty
		assertNull(PslUtils.nthArgf(EMPTY, EMPTY, EMPTY, null));

		// text is not null and not empty, separators is not null, selectColumns is empty
		String text = "|OK|1";
		assertNull(PslUtils.nthArgf(text, EMPTY, EMPTY, null));

		// text is not null and not empty, selectColumns is not null and not empty, separators is empty
		String selectColumns = "3";
		assertNull(PslUtils.nthArgf(text, selectColumns, EMPTY, null));

		// text is not null and not empty, separators is not null and not empty, resultSeparator is not null,
		// selectColumns starts with '-'
		String separators = "|";
		selectColumns = "-2";
		assertEquals(" OK", PslUtils.nthArgf(text, selectColumns, separators, WHITE_SPACE));

		// text is not null and not empty, separators is not null and not empty, resultSeparator is not null,
		// selectColumns ends with '-'
		selectColumns = "2-";
		assertEquals("OK 1", PslUtils.nthArgf(text, selectColumns, separators, WHITE_SPACE));

		// text is not null and not empty, separators is not null and not empty, resultSeparator is not null,
		// selectColumns is '1-3'
		selectColumns = "1-3";
		assertEquals(" OK 1", PslUtils.nthArgf(text, selectColumns, separators, WHITE_SPACE));

		// text is not null and not empty, separators is not null and not empty, resultSeparator is not null,
		// selectColumns is invalid
		selectColumns = "foo-bar";
		assertNull(PslUtils.nthArgf(text, selectColumns, separators, WHITE_SPACE));
		selectColumns = "3-4";
		assertNull(PslUtils.nthArgf(text, selectColumns, separators, WHITE_SPACE));
		selectColumns = "3-1";
		assertNull(PslUtils.nthArgf(text, selectColumns, separators, WHITE_SPACE));

		// text contains "\n" in columns
		text = "OK|OK|\n|WARN|\nALARM";
		selectColumns = "1-";
		assertEquals("OK\nOK\n\n\nWARN\n\nALARM", PslUtils.nthArgf(text, selectColumns, separators, NEW_LINE));
	}

	@Test
	void testNthArg() {

		// text contains "\n" in columns
		String text = "OK|OK|\n|WARN|\nALARM";
		String selectColumns = "1-";
		String separators = "|";
		assertEquals("OK\nOK\nWARN\nALARM", PslUtils.nthArg(text, selectColumns, separators, NEW_LINE));

		// selectColumns is "2-"
		selectColumns = "2-";
		assertEquals("OK\nWARN\nALARM", PslUtils.nthArg(text, selectColumns, separators, NEW_LINE));
	}
}