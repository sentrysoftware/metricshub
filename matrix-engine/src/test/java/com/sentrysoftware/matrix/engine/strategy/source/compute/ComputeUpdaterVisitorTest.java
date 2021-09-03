package com.sentrysoftware.matrix.engine.strategy.source.compute;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sentrysoftware.matrix.engine.strategy.source.SourceTable;
import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.*;
import com.sentrysoftware.matrix.model.monitor.Monitor;

@ExtendWith(MockitoExtension.class)
class ComputeUpdaterVisitorTest {

	@Mock
	private ComputeVisitor computeVisitor;

	@Mock
	private Monitor monitor;

	@InjectMocks
	private ComputeUpdaterVisitor computeUpdaterVisitor;

	@Test
	void testVisitArrayTranslate() {
		final ArrayTranslate arrayTranslate = ArrayTranslate.builder().build();
		doNothing().when(computeVisitor).visit(any(ArrayTranslate.class));
		computeUpdaterVisitor.visit(arrayTranslate);
		verify(computeVisitor, times(1)).visit(any(ArrayTranslate.class));
	}

	@Test
	void testVisitAnd() {
		final And and = And.builder().build();
		doNothing().when(computeVisitor).visit(any(And.class));
		computeUpdaterVisitor.visit(and);
		verify(computeVisitor, times(1)).visit(any(And.class));
	}

	@Test
	void testVisitAdd() {
		final Add add = Add.builder().build();
		doNothing().when(computeVisitor).visit(any(Add.class));
		computeUpdaterVisitor.visit(add);
		verify(computeVisitor, times(1)).visit(any(Add.class));
	}

	@Test
	void testVisitAwk() {
		final Awk awk = Awk.builder().build();
		doNothing().when(computeVisitor).visit(any(Awk.class));
		computeUpdaterVisitor.visit(awk);
		verify(computeVisitor, times(1)).visit(any(Awk.class));
	}

	@Test
	void testVisitConvert() {
		final Convert convert = Convert.builder().build();
		doNothing().when(computeVisitor).visit(any(Convert.class));
		computeUpdaterVisitor.visit(convert);
		verify(computeVisitor, times(1)).visit(any(Convert.class));
	}

	@Test
	void testVisitDivide() {
		final Divide divide = Divide.builder().build();
		doNothing().when(computeVisitor).visit(any(Divide.class));
		computeUpdaterVisitor.visit(divide);
		verify(computeVisitor, times(1)).visit(any(Divide.class));
	}

	@Test
	void testVisitDuplicateColumn() {
		final DuplicateColumn duplicateColumn = DuplicateColumn.builder().build();
		doNothing().when(computeVisitor).visit(any(DuplicateColumn.class));
		computeUpdaterVisitor.visit(duplicateColumn);
		verify(computeVisitor, times(1)).visit(any(DuplicateColumn.class));
	}

	@Test
	void testVisitExcludeMatchingLines() {
		final ExcludeMatchingLines excludeMatchingLines = ExcludeMatchingLines.builder().build();
		doNothing().when(computeVisitor).visit(any(ExcludeMatchingLines.class));
		computeUpdaterVisitor.visit(excludeMatchingLines);
		verify(computeVisitor, times(1)).visit(any(ExcludeMatchingLines.class));
	}

	@Test
	void testVisitExtract() {
		final Extract extract = Extract.builder().build();
		doNothing().when(computeVisitor).visit(any(Extract.class));
		computeUpdaterVisitor.visit(extract);
		verify(computeVisitor, times(1)).visit(any(Extract.class));
	}

	@Test
	void testVisitExtractPropertyFromWbemPath() {
		final ExtractPropertyFromWbemPath extractPropertyFromWbemPath = ExtractPropertyFromWbemPath.builder().build();
		doNothing().when(computeVisitor).visit(any(ExtractPropertyFromWbemPath.class));
		computeUpdaterVisitor.visit(extractPropertyFromWbemPath);
		verify(computeVisitor, times(1)).visit(any(ExtractPropertyFromWbemPath.class));
	}

	@Test
	void testVisitJson2CSV() {
		final Json2CSV json2CSV = Json2CSV.builder().build();
		doNothing().when(computeVisitor).visit(any(Json2CSV.class));
		computeUpdaterVisitor.visit(json2CSV);
		verify(computeVisitor, times(1)).visit(any(Json2CSV.class));
	}

	@Test
	void testVisitKeepColumns() {
		final KeepColumns keepColumns = KeepColumns.builder().build();
		doNothing().when(computeVisitor).visit(any(KeepColumns.class));
		computeUpdaterVisitor.visit(keepColumns);
		verify(computeVisitor, times(1)).visit(any(KeepColumns.class));
	}

	@Test
	void testVisitKeepOnlyMatchingLines() {
		final KeepOnlyMatchingLines keepOnlyMatchingLines = KeepOnlyMatchingLines.builder().build();
		doNothing().when(computeVisitor).visit(any(KeepOnlyMatchingLines.class));
		computeUpdaterVisitor.visit(keepOnlyMatchingLines);
		verify(computeVisitor, times(1)).visit(any(KeepOnlyMatchingLines.class));
	}

	@Test
	void testVisitLeftConcat() {
		final LeftConcat leftConcat = LeftConcat.builder().build();
		doNothing().when(computeVisitor).visit(any(LeftConcat.class));
		computeUpdaterVisitor.visit(leftConcat);
		verify(computeVisitor, times(1)).visit(any(LeftConcat.class));
	}

	@Test
	void testVisitMultiply() {
		final Multiply multiply = Multiply.builder().build();
		doNothing().when(computeVisitor).visit(any(Multiply.class));
		computeUpdaterVisitor.visit(multiply);
		verify(computeVisitor, times(1)).visit(any(Multiply.class));
	}

	@Test
	void testVisitPerBitTranslation() {
		final PerBitTranslation perBitTranslation = PerBitTranslation.builder().build();
		doNothing().when(computeVisitor).visit(any(PerBitTranslation.class));
		computeUpdaterVisitor.visit(perBitTranslation);
		verify(computeVisitor, times(1)).visit(any(PerBitTranslation.class));
	}

	@Test
	void testVisitReplace() {
		final Replace replace = Replace.builder().build();
		doNothing().when(computeVisitor).visit(any(Replace.class));
		computeUpdaterVisitor.visit(replace);
		verify(computeVisitor, times(1)).visit(any(Replace.class));
	}

	@Test
	void testVisitRightConcat() {
		final RightConcat rightConcat = RightConcat.builder().build();
		doNothing().when(computeVisitor).visit(any(RightConcat.class));
		computeUpdaterVisitor.visit(rightConcat);
		verify(computeVisitor, times(1)).visit(any(RightConcat.class));
	}

	@Test
	void testVisitSubstract() {
		final Substract substract = Substract.builder().build();
		doNothing().when(computeVisitor).visit(any(Substract.class));
		computeUpdaterVisitor.visit(substract);
		verify(computeVisitor, times(1)).visit(any(Substract.class));
	}

	@Test
	void testVisitSubstring() {
		final Substring substring = Substring.builder().build();
		doNothing().when(computeVisitor).visit(any(Substring.class));
		computeUpdaterVisitor.visit(substring);
		verify(computeVisitor, times(1)).visit(any(Substring.class));
	}

	@Test
	void testVisitTranslate() {
		final Translate translate = Translate.builder().build();
		doNothing().when(computeVisitor).visit(any(Translate.class));
		computeUpdaterVisitor.visit(translate);
		verify(computeVisitor, times(1)).visit(any(Translate.class));
	}

	@Test
	void testVisitXML2CSV() {
		final Xml2Csv xml2Csv = Xml2Csv.builder().build();
		doNothing().when(computeVisitor).visit(any(Xml2Csv.class));
		computeUpdaterVisitor.visit(xml2Csv);
		verify(computeVisitor, times(1)).visit(any(Xml2Csv.class));
	}

	@Test
	void testSetSourceTable() {
		doNothing().when(computeVisitor).setSourceTable(any());
		assertDoesNotThrow(() -> computeUpdaterVisitor.setSourceTable(SourceTable.empty())); 
	}

	@Test
	void testGetSourceTable() {
		doReturn(SourceTable.empty()).when(computeVisitor).getSourceTable();
		assertDoesNotThrow(() -> computeUpdaterVisitor.getSourceTable()); 
	}

	@Test
	void testDoSubstringReplacements() {
		{
			final Substring substring = Substring.builder()
					.column(1)
					.start("1%PowerSupply.Collect.DeviceID%0")
					.length("5")
					.build();

			doReturn(Map.of(HardwareConstants.DEVICE_ID, "1")).when(monitor).getMetadata();
			ComputeUpdaterVisitor.doSubstringReplacements(substring, monitor);

			final Substring expected = Substring.builder()
					.column(1)
					.start("110")
					.length("5")
					.build();

			assertEquals(expected, substring);
		}

		{
			// Bad substring, no start value
			final Substring substring = Substring.builder()
					.column(1)
					.length("5")
					.build();


			ComputeUpdaterVisitor.doSubstringReplacements(substring, monitor);

			final Substring expected = Substring.builder()
					.column(1)
					.length("5")
					.build();

			assertEquals(expected, substring);
		}

		{

			final Substring substring = Substring.builder()
					.column(1)
					.start("1")
					.length("5")
					.build();

			// no monitor (multi instance collect)
			ComputeUpdaterVisitor.doSubstringReplacements(substring, null);

			final Substring expected = Substring.builder()
					.column(1)
					.start("1")
					.length("5")
					.build();

			assertEquals(expected, substring);
		}
	}

	@Test
	void testMonoInstanceReplace() {
		assertEquals("110", ComputeUpdaterVisitor.monoInstanceReplace("1%PowerSupply.Collect.DeviceID%0", "1"));
		assertEquals("88", ComputeUpdaterVisitor.monoInstanceReplace("88", "1"));
	}
}
