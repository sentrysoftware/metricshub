package com.sentrysoftware.matrix.engine.strategy.utils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sentrysoftware.javax.wbem.WBEMException;
import com.sentrysoftware.matrix.common.exception.MatsyaException;
import com.sentrysoftware.matrix.connector.model.detection.criteria.wmi.WMI;
import com.sentrysoftware.matrix.engine.protocol.WBEMProtocol;
import com.sentrysoftware.matrix.engine.protocol.WMIProtocol;
import com.sentrysoftware.matrix.engine.strategy.detection.CriterionTestResult;
import com.sentrysoftware.matrix.engine.strategy.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.engine.strategy.utils.WqlDetectionHelper.NamespaceResult;
import com.sentrysoftware.matrix.engine.strategy.utils.WqlDetectionHelper.PossibleNamespacesResult;
import com.sentrysoftware.matsya.wmi.exceptions.WmiComException;

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
			.executeWql(any(), eq(wmiConfig), any(), any());

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
		.executeWql(any(), eq(wmiConfig), any(), eq("root"));

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
				.executeWql(any(), eq(wmiConfig), any(), any());

		PossibleNamespacesResult result = wqlDetectionHelper.findPossibleNamespaces(HOSTNAME, wmiConfig);
		assertTrue(result.isSuccess());
		assertEquals(2, result.getPossibleNamespaces().size());
		assertTrue(result.getPossibleNamespaces().contains("root/namespace1"));
		assertTrue(result.getPossibleNamespaces().contains("root/namespace2"));

	}


	@Test
	void testPerformDetectionTest() throws Exception {

		// Invalid parameters
		{
			assertThrows(IllegalArgumentException.class, () -> wqlDetectionHelper.performDetectionTest(HOSTNAME, null, null));
		}

		// MatsyaException
		{
			WMIProtocol wmiConfig = WMIProtocol.builder().build();
			doThrow(new MatsyaException("problem", new TimeoutException()))
					.when(matsyaClientsExecutor)
					.executeWql(any(), eq(wmiConfig), any(), any());
			WMI criterion = WMI.builder().wbemQuery("query").build();

			CriterionTestResult result = wqlDetectionHelper.performDetectionTest(HOSTNAME, wmiConfig, criterion);
			assertFalse(result.isSuccess());
			assertNotNull(result.getException());
			assertTrue(result.getException() instanceof TimeoutException);
		}

		// Empty result
		// MatsyaException
		{
			WMIProtocol wmiConfig = WMIProtocol.builder().build();
			doReturn(Collections.emptyList())
					.when(matsyaClientsExecutor)
					.executeWql(any(), eq(wmiConfig), any(), any());
			WMI criterion = WMI.builder().wbemQuery("query").build();

			CriterionTestResult result = wqlDetectionHelper.performDetectionTest(HOSTNAME, wmiConfig, criterion);
			assertFalse(result.isSuccess());
			assertNull(result.getException());
		}

		// Non-empty result, and no expected result => success
		{
			WMIProtocol wmiConfig = WMIProtocol.builder().build();
			doReturn(List.of(List.of("some result")))
					.when(matsyaClientsExecutor)
					.executeWql(any(), eq(wmiConfig), any(), any());
			WMI criterion = WMI.builder().wbemQuery("query").build();

			CriterionTestResult result = wqlDetectionHelper.performDetectionTest(HOSTNAME, wmiConfig, criterion);
			assertTrue(result.isSuccess());
			assertTrue(result.getMessage().contains("some result"), "Result message must contain the query result");
		}

		// Non-empty result, and matching expected result => success
		{
			WMIProtocol wmiConfig = WMIProtocol.builder().build();
			doReturn(List.of(List.of("some result")))
					.when(matsyaClientsExecutor)
					.executeWql(any(), eq(wmiConfig), any(), any());
			WMI criterion = WMI.builder().wbemQuery("query").expectedResult("^Some Res[aeiouy]lt").build();

			CriterionTestResult result = wqlDetectionHelper.performDetectionTest(HOSTNAME, wmiConfig, criterion);
			assertTrue(result.isSuccess());
			assertTrue(result.getMessage().contains("some result"), "Result message must contain the query result");
		}

		// Non-empty result, and non-matching expected result => failure
		{
			WMIProtocol wmiConfig = WMIProtocol.builder().build();
			doReturn(List.of(List.of("some result")))
					.when(matsyaClientsExecutor)
					.executeWql(any(), eq(wmiConfig), any(), any());
			WMI criterion = WMI.builder().wbemQuery("query").expectedResult("^Some Res[^aeiouy]lt").build();

			CriterionTestResult result = wqlDetectionHelper.performDetectionTest(HOSTNAME, wmiConfig, criterion);
			assertFalse(result.isSuccess());
			assertNull(result.getException());
			assertTrue(result.getMessage().contains("some result"), "Result message must contain the query result");
		}

	}


	@Test
	void testDetectNamespaceNoResponse() throws Exception {
		// No response at all => we fail early (we don't try every single namespace)
		WMIProtocol wmiConfig = WMIProtocol.builder().build();
		WMI criterion = WMI.builder().wbemQuery("query").expectedResult("^Some Res[^aeiouy]lt").build();
		doThrow(new MatsyaException("problem", new TimeoutException()))
				.when(matsyaClientsExecutor)
				.executeWql(any(), eq(wmiConfig), any(), any());

		NamespaceResult result = wqlDetectionHelper.detectNamespace(HOSTNAME, wmiConfig, criterion, Set.of("ns1", "ns2"));
		assertFalse(result.getResult().isSuccess());
		assertTrue(result.getResult().getMessage().contains("TimeoutException"));
		verify(matsyaClientsExecutor).executeWql(any(), eq(wmiConfig), any(), any());
	}


	@Test
	void testDetectNamespaceEmpty() throws Exception {
		// Non-matching result AND empty result (with an error that doesn't stop the loop)
		WMIProtocol wmiConfig = WMIProtocol.builder().build();
		WMI criterion = WMI.builder().wbemQuery("query").expectedResult("^Some Res[^aeiouy]lt").build();
		doThrow(new MatsyaException("problem", new WmiComException("WBEM_E_INVALID_NAMESPACE")))
				.when(matsyaClientsExecutor)
				.executeWql(any(), eq(wmiConfig), any(), eq("ns1"));
		doReturn(List.of(List.of("non-matching")))
				.when(matsyaClientsExecutor)
				.executeWql(any(), eq(wmiConfig), any(), eq("ns2"));

		NamespaceResult result = wqlDetectionHelper.detectNamespace(HOSTNAME, wmiConfig, criterion, Set.of("ns1", "ns2"));
		assertFalse(result.getResult().isSuccess());
		assertNull(result.getResult().getException());
		verify(matsyaClientsExecutor, times(2)).executeWql(any(), eq(wmiConfig), any(), any());
	}


	@Test
	void testDetectNamespace() throws Exception {
		// 3 matching result, and root\\cimv2 must be removed
		WMIProtocol wmiConfig = WMIProtocol.builder().build();
		WMI criterion = WMI.builder().wbemQuery("query").build();
		doReturn(List.of(List.of("some result")))
				.when(matsyaClientsExecutor)
				.executeWql(any(), eq(wmiConfig), any(), any());

		NamespaceResult result = wqlDetectionHelper.detectNamespace(HOSTNAME, wmiConfig, criterion, Set.of("root\\cimv2", "ns1"));
		assertTrue(result.getResult().isSuccess());
		assertNull(result.getResult().getException());
		assertEquals("ns1", result.getNamespace());
		verify(matsyaClientsExecutor, times(2)).executeWql(any(), eq(wmiConfig), any(), any());
	}


	@Test
	void testDetectNamespaceCimv2() throws Exception {
		// 1 single matching result: root\\cimv2 which must not be removed
		WMIProtocol wmiConfig = WMIProtocol.builder().build();
		WMI criterion = WMI.builder().wbemQuery("query").build();
		doReturn(List.of(List.of("some result")))
				.when(matsyaClientsExecutor)
				.executeWql(any(), eq(wmiConfig), any(), any());

		NamespaceResult result = wqlDetectionHelper.detectNamespace(HOSTNAME, wmiConfig, criterion, Set.of("root\\cimv2"));
		assertTrue(result.getResult().isSuccess());
		assertEquals("root\\cimv2", result.getNamespace());
	}


	@Test
    void testIsAcceptableException() {

        assertFalse(WqlDetectionHelper.isAcceptableException(null));
        assertFalse(WqlDetectionHelper.isAcceptableException(new Exception()));
        assertFalse(WqlDetectionHelper.isAcceptableException(new Exception(new Exception())));

        assertFalse(WqlDetectionHelper.isAcceptableException(new WmiComException("other")));
        assertFalse(WqlDetectionHelper.isAcceptableException(new WmiComException(new Exception())));
        assertTrue(WqlDetectionHelper.isAcceptableException(new WmiComException("WBEM_E_NOT_FOUND")));
        assertTrue(WqlDetectionHelper.isAcceptableException(new WmiComException("WBEM_E_INVALID_NAMESPACE")));
        assertTrue(WqlDetectionHelper.isAcceptableException(new WmiComException("WBEM_E_INVALID_CLASS")));

        assertFalse(WqlDetectionHelper.isAcceptableException(new WBEMException("other")));
        assertFalse(WqlDetectionHelper.isAcceptableException(new WBEMException(0)));
        assertTrue(WqlDetectionHelper.isAcceptableException(new WBEMException(WBEMException.CIM_ERR_NOT_FOUND)));
        assertTrue(WqlDetectionHelper.isAcceptableException(new WBEMException(WBEMException.CIM_ERR_INVALID_NAMESPACE)));
        assertTrue(WqlDetectionHelper.isAcceptableException(new WBEMException(WBEMException.CIM_ERR_INVALID_CLASS)));

        assertTrue(WqlDetectionHelper.isAcceptableException(new Exception(new WmiComException("WBEM_E_NOT_FOUND"))));
        assertTrue(WqlDetectionHelper.isAcceptableException(new Exception(new WBEMException(WBEMException.CIM_ERR_NOT_FOUND))));

    }


}
