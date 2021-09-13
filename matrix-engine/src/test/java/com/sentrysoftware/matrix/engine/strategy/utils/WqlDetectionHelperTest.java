package com.sentrysoftware.matrix.engine.strategy.utils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sentrysoftware.javax.wbem.WBEMException;
import com.sentrysoftware.matrix.common.exception.MatsyaException;
import com.sentrysoftware.matrix.engine.protocol.WBEMProtocol;
import com.sentrysoftware.matrix.engine.protocol.WMIProtocol;
import com.sentrysoftware.matrix.engine.strategy.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.engine.strategy.utils.WqlDetectionHelper.PossibleNamespacesResult;

@ExtendWith(MockitoExtension.class)
class WqlDetectionHelperTest {

	private static final String HOSTNAME = "hostname";
	private static final String PASSWORD = "password";
	private static final String USERNAME = "user";

	@Mock
	private MatsyaClientsExecutor matsyaClientsExecutor;

	@InjectMocks
	private WqlDetectionHelper wqlDetectionHelper = new WqlDetectionHelper();

	@Test
	void testWbemFindPossibleNamespacesForcedProtocol() {

		// Namespace is forced
		WBEMProtocol wbemConfig = WBEMProtocol.builder().namespace("forced").build();

		PossibleNamespacesResult result = wqlDetectionHelper.findPossibleNamespaces(null, wbemConfig);
		assertTrue(result.isSuccess());
		assertEquals("forced", result.getPossibleNamespaces().stream().findFirst().orElseThrow());
	}

	@Test
	void testWbemFindPossibleNamespacesNoResponse() throws Exception {

		WBEMProtocol wbemConfig = WBEMProtocol
				.builder()
				.username(USERNAME)
				.password(PASSWORD.toCharArray())
				.build();

		// No response from the target
		doThrow(new MatsyaException("no response", new TimeoutException("very long")))
			.when(matsyaClientsExecutor)
			.executeWbem(any(), eq(wbemConfig), any(), any());

		PossibleNamespacesResult result = wqlDetectionHelper.findPossibleNamespaces(HOSTNAME, wbemConfig);
		assertFalse(result.isSuccess());
		assertTrue(result.getErrorMessage().contains("very long"), "Error message must contain the cause of the problem");
		verify(matsyaClientsExecutor).executeWbem(any(), eq(wbemConfig), any(), any());
	}

	@Test
	void testWbemFindPossibleNamespacesEmpty() throws Exception {

		WBEMProtocol wbemConfig = WBEMProtocol
				.builder()
				.username(USERNAME)
				.password(PASSWORD.toCharArray())
				.build();

		// We will always return "not found"
		doThrow(new MatsyaException(new WBEMException(WBEMException.CIM_ERR_NOT_FOUND)))
				.when(matsyaClientsExecutor)
				.executeWbem(any(), eq(wbemConfig), any(), any());

		PossibleNamespacesResult result = wqlDetectionHelper.findPossibleNamespaces(HOSTNAME, wbemConfig);
		assertFalse(result.isSuccess());

	}

	@Test
	void testWbemFindPossibleNamespaces() throws Exception {

		WBEMProtocol wbemConfig = WBEMProtocol
				.builder()
				.username(USERNAME)
				.password(PASSWORD.toCharArray())
				.build();

		// By default, WBEM will throw an "invalid namespace" exception
		// except for 2 namespaces where we will return a result
		doThrow(
				new MatsyaException(new WBEMException(WBEMException.CIM_ERR_INVALID_NAMESPACE)),
				new MatsyaException(new WBEMException(WBEMException.CIM_ERR_INVALID_CLASS)),
				new MatsyaException(new WBEMException(WBEMException.CIM_ERR_NOT_FOUND))
			)
				.when(matsyaClientsExecutor)
				.executeWbem(any(), eq(wbemConfig), any(), any());

		doReturn(List.of(List.of("namespace1")))
				.when(matsyaClientsExecutor)
				.executeWbem(any(), eq(wbemConfig), any(), eq("root"));

		doReturn(List.of(List.of("namespace2")))
				.when(matsyaClientsExecutor)
				.executeWbem(any(), eq(wbemConfig), any(), eq("interop"));

		PossibleNamespacesResult result = wqlDetectionHelper.findPossibleNamespaces(HOSTNAME, wbemConfig);
		assertTrue(result.isSuccess());
		assertEquals(2, result.getPossibleNamespaces().size());
		assertTrue(result.getPossibleNamespaces().contains("root/namespace1"));
		assertTrue(result.getPossibleNamespaces().contains("root/namespace2"));

	}


	@Test
	void testWmiFindPossibleNamespacesForcedProtocol() {

		// Namespace is forced
		WMIProtocol wmiConfig = WMIProtocol.builder().namespace("forced").build();

		PossibleNamespacesResult result = wqlDetectionHelper.findPossibleNamespaces(null, wmiConfig);
		assertTrue(result.isSuccess());
		assertEquals("forced", result.getPossibleNamespaces().stream().findFirst().orElseThrow());
	}

	@Test
	void testWmiFindPossibleNamespacesNoResponse() throws Exception {

		WMIProtocol wmiConfig = WMIProtocol
				.builder()
				.username(USERNAME)
				.password(PASSWORD.toCharArray())
				.build();

		// No response from the target
		doThrow(new MatsyaException("no response", new TimeoutException("very long")))
			.when(matsyaClientsExecutor)
			.executeWmi(any(), eq(wmiConfig), any(), any());

		PossibleNamespacesResult result = wqlDetectionHelper.findPossibleNamespaces(HOSTNAME, wmiConfig);
		assertFalse(result.isSuccess());
		assertTrue(result.getErrorMessage().contains("very long"), "Error message must contain the cause of the problem");
	}

	@Test
	void testWmiFindPossibleNamespacesEmpty() throws Exception {

		WMIProtocol wmiConfig = WMIProtocol
				.builder()
				.username(USERNAME)
				.password(PASSWORD.toCharArray())
				.build();

		// We return an empty list
		doReturn(Collections.emptyList())
		.when(matsyaClientsExecutor)
		.executeWmi(any(), eq(wmiConfig), any(), eq("root"));

		PossibleNamespacesResult result = wqlDetectionHelper.findPossibleNamespaces(HOSTNAME, wmiConfig);
		assertFalse(result.isSuccess());

	}

	@Test
	void testWmiFindPossibleNamespaces() throws Exception {

		WMIProtocol wmiConfig = WMIProtocol
				.builder()
				.username(USERNAME)
				.password(PASSWORD.toCharArray())
				.build();

		doReturn(List.of(List.of("namespace1"), List.of("namespace2")))
				.when(matsyaClientsExecutor)
				.executeWmi(any(), eq(wmiConfig), any(), any());

		PossibleNamespacesResult result = wqlDetectionHelper.findPossibleNamespaces(HOSTNAME, wmiConfig);
		assertTrue(result.isSuccess());
		assertEquals(2, result.getPossibleNamespaces().size());
		assertTrue(result.getPossibleNamespaces().contains("root/namespace1"));
		assertTrue(result.getPossibleNamespaces().contains("root/namespace2"));

	}


	@Test
	void testDetectNamespace() {
	}

	@Test
	void testPerformDetectionTest() {
	}

}
