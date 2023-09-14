package com.sentrysoftware.matrix.strategy.source.compute;

import static com.sentrysoftware.matrix.constants.Constants.EMPTY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Add;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.And;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Divide;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Multiply;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Subtract;

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
}
