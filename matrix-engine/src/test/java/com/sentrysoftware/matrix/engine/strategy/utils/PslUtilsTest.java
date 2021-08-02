package com.sentrysoftware.matrix.engine.strategy.utils;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.EMPTY;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.NEW_LINE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.WHITE_SPACE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.engine.strategy.source.SourceTable;

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
		assertEquals(EMPTY, PslUtils.nthArgf(null, null, null, null));

		// text is not null, selectColumns is null
		assertEquals(EMPTY, PslUtils.nthArgf(EMPTY, null, null, null));

		// text is not null, selectColumns is not null, separators is null
		assertEquals(EMPTY, PslUtils.nthArgf(EMPTY, EMPTY, null, null));

		// selectColumns is not null, separators is not null, text is empty
		assertEquals(EMPTY, PslUtils.nthArgf(EMPTY, EMPTY, EMPTY, null));

		// text is not null and not empty, separators is not null, selectColumns is empty
		String text = "|OK|1";
		assertEquals(EMPTY, PslUtils.nthArgf(text, EMPTY, EMPTY, null));

		// text is not null and not empty, selectColumns is not null and not empty, separators is empty
		String selectColumns = "3";
		assertEquals(EMPTY, PslUtils.nthArgf(text, selectColumns, EMPTY, null));

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
		assertEquals(EMPTY, PslUtils.nthArgf(text, selectColumns, separators, WHITE_SPACE));
		selectColumns = "3-4";
		assertEquals("1", PslUtils.nthArgf(text, selectColumns, separators, WHITE_SPACE));
		selectColumns = "3-1";
		assertEquals(EMPTY, PslUtils.nthArgf(text, selectColumns, separators, WHITE_SPACE));

		// text contains "\n" in columns
		text = "OK|OK|\n|WARN|\nALARM";
		selectColumns = "1-";
		assertEquals("OK\nOK\n\n\nWARN\n\nALARM", PslUtils.nthArgf(text, selectColumns, separators, NEW_LINE));

		assertEquals("a,b,d", PslUtils.nthArgf("a,b,c,d,e,", "1,2,4", ",", ","));
		assertEquals("a,b,c,d,e,", PslUtils.nthArgf("a,b,c,d,e,", "1-", ",", ","));
		assertEquals("a,b,c", PslUtils.nthArgf("a,b,c,d,e,", "1-3", ",", ","));
		assertEquals("b,c,d", PslUtils.nthArgf("a,b,c,d,e,", "2-4", ",", ","));
		assertEquals("b", PslUtils.nthArgf("a,b,c,d,e,", "2", ",", ","));
		assertEquals("a,b,c,d,e,", PslUtils.nthArgf("a,b,c,d,e,", "1-50", ",", ","));
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
		assertEquals("OK\nWARN", PslUtils.nthArg(text, selectColumns, separators, NEW_LINE));

		assertEquals("OK\nWARN\nALARM", PslUtils.nthArg("OK|OK|\nOK|WARN|\nOK|ALARM", selectColumns, separators, NEW_LINE));

		assertEquals("a,b,c,d", PslUtils.nthArg("a,b,\nc,d,e,", "1-2", ",", ","));

		assertEquals("a,b,e,f", PslUtils.nthArg("a,b,c,d,\ne,f,g,h,", "-2", ",", ","));
		assertEquals("a,b,d", PslUtils.nthArg("a,b,c,d,e,", "1,2,4", ",", ","));
		assertEquals("a,b,c,d,e", PslUtils.nthArg("a,b,c,d,e,", "1-", ",", ","));
		assertEquals("a,b,c", PslUtils.nthArg("a,b,c,d,e,", "1-3", ",", ","));
		assertEquals("b,c,d", PslUtils.nthArg("a,b,c,d,e,", "2-4", ",", ","));
		assertEquals("b", PslUtils.nthArg("a,b,c,d,e,", "2", ",", ","));
		assertEquals("a,b,c,d,e", PslUtils.nthArg("a,b,c,d,e,", "1-50", ",", ","));
	}

	@Test
	void testFormatExtendedJSON() {
		assertThrows(IllegalArgumentException.class, () -> PslUtils.formatExtendedJSON(null, null));

		SourceTable sourceTable = SourceTable.builder().build();
		assertThrows(IllegalArgumentException.class, () -> PslUtils.formatExtendedJSON("", null));
		assertThrows(IllegalArgumentException.class, () -> PslUtils.formatExtendedJSON(null, sourceTable));
		assertThrows(IllegalArgumentException.class, () -> PslUtils.formatExtendedJSON("", sourceTable), "Empty row of values");

		String row = "val1,val2,val3";
		assertThrows(IllegalArgumentException.class, () -> PslUtils.formatExtendedJSON(row, sourceTable), "Empty SourceTable data: " + sourceTable);

		sourceTable.setRawData("source table raw data");
		assertEquals("{\n" +
				"\"Entry\":{\n" +
				"\"Full\":\"val1,val2,val3\",\n" +
				"\"Column(1)\":\"val1\",\n" +
				"\"Column(2)\":\"val2\",\n" +
				"\"Column(3)\":\"val3\",\n" +
				"\"Value\":source table raw data\n" +
				"}\n" +
				"}",
				PslUtils.formatExtendedJSON(row, sourceTable));
	}
}