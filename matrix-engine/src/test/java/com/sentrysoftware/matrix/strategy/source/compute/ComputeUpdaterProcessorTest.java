package com.sentrysoftware.matrix.strategy.source.compute;

import static com.sentrysoftware.matrix.constants.Constants.EMPTY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sentrysoftware.matrix.connector.model.common.ReferenceTranslationTable;
import com.sentrysoftware.matrix.connector.model.common.TranslationTable;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Add;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.And;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.ArrayTranslate;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Divide;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.ExcludeMatchingLines;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Extract;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Json2Csv;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.LeftConcat;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Multiply;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Replace;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.RightConcat;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Substring;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Subtract;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Translate;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Xml2Csv;
import com.sentrysoftware.matrix.telemetry.Monitor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ComputeUpdaterProcessorTest {

	@Mock
	private ComputeProcessor computeProcessor;

	@Mock
	private Monitor monitor;

	@InjectMocks
	private ComputeUpdaterProcessor computeUpdaterProcessor;

	private static final String DEVICE_ID = "deviceId";

	@Test
	void testProcessAdd() {
		doNothing().when(computeProcessor).process(any(Add.class));
		computeUpdaterProcessor.process(Add.builder().column(1).value(EMPTY).build());
		verify(computeProcessor, times(1)).process(any(Add.class));
	}

	@Test
	void testProcessDivide() {
		doNothing().when(computeProcessor).process(any(Divide.class));
		computeUpdaterProcessor.process(Divide.builder().column(1).value(EMPTY).build());
		verify(computeProcessor, times(1)).process(any(Divide.class));
	}

	@Test
	void testProcessMultiply() {
		doNothing().when(computeProcessor).process(any(Multiply.class));
		computeUpdaterProcessor.process(Multiply.builder().column(1).value(EMPTY).build());
		verify(computeProcessor, times(1)).process(any(Multiply.class));
	}

	@Test
	void testProcessSubtract() {
		doNothing().when(computeProcessor).process(any(Subtract.class));
		computeUpdaterProcessor.process(Subtract.builder().column(1).value(EMPTY).build());
		verify(computeProcessor, times(1)).process(any(Subtract.class));
	}

	@Test
	void testProcessAnd() {
		doNothing().when(computeProcessor).process(any(And.class));
		computeUpdaterProcessor.process(And.builder().column(1).value(EMPTY).build());
		verify(computeProcessor, times(1)).process(any(And.class));
	}

	@Test
	void testProcessLeftConcat() {
		doNothing().when(computeProcessor).process(any(LeftConcat.class));
		computeUpdaterProcessor.process(LeftConcat.builder().column(1).value(EMPTY).build());
		verify(computeProcessor, times(1)).process(any(LeftConcat.class));
	}

	@Test
	void testProcessRightConcat() {
		doNothing().when(computeProcessor).process(any(RightConcat.class));
		computeUpdaterProcessor.process(RightConcat.builder().column(1).value(EMPTY).build());
		verify(computeProcessor, times(1)).process(any(RightConcat.class));
	}

	@Test
	void testProcessArrayTranslate() {
		doNothing().when(computeProcessor).process(any(ArrayTranslate.class));
		computeUpdaterProcessor.process(
			ArrayTranslate.builder().column(1).translationTable(new TranslationTable()).build()
		);

		computeUpdaterProcessor.process(
			ArrayTranslate.builder().column(1).translationTable(new ReferenceTranslationTable()).build()
		);

		verify(computeProcessor, times(2)).process(any(ArrayTranslate.class));
	}

	@Test
	void testProcessJson2CSV() {
		final Json2Csv json2Csv = Json2Csv.builder().build();
		doNothing().when(computeProcessor).process(any(Json2Csv.class));
		computeUpdaterProcessor.process(json2Csv);
		verify(computeProcessor, times(1)).process(any(Json2Csv.class));
	}

	@Test
	void testProcessExcludeMatchingLines() {
		final ExcludeMatchingLines excludeMatchingLines = ExcludeMatchingLines.builder().column(-1).build();
		doNothing().when(computeProcessor).process(any(ExcludeMatchingLines.class));
		computeUpdaterProcessor.process(excludeMatchingLines);
		verify(computeProcessor, times(1)).process(any(ExcludeMatchingLines.class));
	}

	@Test
	void testProcessExtract() {
		final Extract extract = Extract.builder().column(-1).subColumn(-1).build();
		doNothing().when(computeProcessor).process(any(Extract.class));
		computeUpdaterProcessor.process(extract);
		verify(computeProcessor, times(1)).process(any(Extract.class));
	}

	@Test
	void testProcessReplace() {
		final Replace replace = Replace.builder().column(-1).build();
		doNothing().when(computeProcessor).process(any(Replace.class));
		computeUpdaterProcessor.process(replace);
		verify(computeProcessor, times(1)).process(any(Replace.class));
	}

	@Test
	void testProcessSubstring() {
		final Substring substring = Substring.builder().column(-1).start("-1").length("-1").build();
		doNothing().when(computeProcessor).process(any(Substring.class));
		computeUpdaterProcessor.process(substring);
		verify(computeProcessor, times(1)).process(any(Substring.class));
	}

	@Test
	void testProcessXML2CSV() {
		final Xml2Csv xml2Csv = Xml2Csv.builder().build();
		doNothing().when(computeProcessor).process(any(Xml2Csv.class));
		computeUpdaterProcessor.process(xml2Csv);
		verify(computeProcessor, times(1)).process(any(Xml2Csv.class));
	}

	@Test
	void testProcessTranslate() {
		final Translate translate = Translate
			.builder()
			.column(-1)
			.translationTable(TranslationTable.builder().build())
			.build();
		doNothing().when(computeProcessor).process(any(Translate.class));
		computeUpdaterProcessor.process(translate);
		verify(computeProcessor, times(1)).process(any(Translate.class));
	}
}
