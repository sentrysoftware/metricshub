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
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Awk;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Divide;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.LeftConcat;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Multiply;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.RightConcat;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Subtract;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ComputeUpdaterProcessorTest {

	@Mock
	private ComputeProcessor computeProcessor;

	@InjectMocks
	private ComputeUpdaterProcessor computeUpdaterProcessor;

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
	void testProcessAwk() {
		doNothing().when(computeProcessor).process(any(Awk.class));
		computeUpdaterProcessor.process(Awk.builder().script("script").build());
		verify(computeProcessor, times(1)).process(any(Awk.class));
	}
}
