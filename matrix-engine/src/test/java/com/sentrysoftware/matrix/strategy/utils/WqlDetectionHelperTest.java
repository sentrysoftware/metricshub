package com.sentrysoftware.matrix.strategy.utils;

import static com.sentrysoftware.matrix.constants.Constants.CRITERION_WMI_NAMESPACE;
import static com.sentrysoftware.matrix.constants.Constants.EXCUTE_WBEM_RESULT;
import static com.sentrysoftware.matrix.constants.Constants.EXCUTE_WBEM_RESULT_ELEMENT;
import static com.sentrysoftware.matrix.constants.Constants.FIRST_NAMESPACE;
import static com.sentrysoftware.matrix.constants.Constants.FORCED_NAMESPACE;
import static com.sentrysoftware.matrix.constants.Constants.INTEROP_NAMESPACE;
import static com.sentrysoftware.matrix.constants.Constants.LOCALHOST;
import static com.sentrysoftware.matrix.constants.Constants.MATSYA_NO_RESPONSE_EXCEPTION_MESSAGE;
import static com.sentrysoftware.matrix.constants.Constants.PASSWORD;
import static com.sentrysoftware.matrix.constants.Constants.RESULT_MESSAGE_SHOULD_CONTAIN_RESULT;
import static com.sentrysoftware.matrix.constants.Constants.ROOT;
import static com.sentrysoftware.matrix.constants.Constants.SECOND_NAMESPACE;
import static com.sentrysoftware.matrix.constants.Constants.TIMEOUT_EXCEPTION;
import static com.sentrysoftware.matrix.constants.Constants.USERNAME;
import static com.sentrysoftware.matrix.constants.Constants.WBEM_NAMESPACE_TIMEOUT_ERROR_MESSAGE;
import static com.sentrysoftware.matrix.constants.Constants.WBEM_NAMESPACE_TIMEOUT_MESSAGE;
import static com.sentrysoftware.matrix.constants.Constants.WBEM_QUERY;
import static com.sentrysoftware.matrix.constants.Constants.WEBM_CRITERION_NOT_MATCHING_EXPECTED_RESULT;
import static com.sentrysoftware.matrix.constants.Constants.WEBM_CRITERION_SUCCESS_EXPECTED_RESULT;
import static com.sentrysoftware.matrix.constants.Constants.WMI_COM_EXCEPTION_MESSAGE;
import static com.sentrysoftware.matrix.constants.Constants.WMI_EXCEPTION_OTHER_MESSAGE;
import static com.sentrysoftware.matrix.constants.Constants.WMI_EXCEPTION_WBEM_E_INVALID_CLASS_MESSAGE;
import static com.sentrysoftware.matrix.constants.Constants.WMI_EXCEPTION_WBEM_E_INVALID_NAMESPACE_MESSAGE;
import static com.sentrysoftware.matrix.constants.Constants.WMI_EXCEPTION_WBEM_E_NOT_FOUND_MESSAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sentrysoftware.javax.wbem.WBEMException;
import com.sentrysoftware.matrix.common.exception.MatsyaException;
import com.sentrysoftware.matrix.configuration.WbemConfiguration;
import com.sentrysoftware.matrix.configuration.WmiConfiguration;
import com.sentrysoftware.matrix.connector.model.identity.criterion.WmiCriterion;
import com.sentrysoftware.matrix.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.strategy.detection.CriterionTestResult;
import com.sentrysoftware.matsya.wmi.exceptions.WmiComException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WqlDetectionHelperTest {

	@Mock
	private MatsyaClientsExecutor matsyaClientsExecutorMock;

	@InjectMocks
	private WqlDetectionHelper wqlDetectionHelperMock;

	@Test
	void testWbemFindPossibleNamespacesForcedProtocol() {
		// Namespace is forced
		final WbemConfiguration wbemConfiguration = WbemConfiguration.builder().namespace(FORCED_NAMESPACE).build();

		final WqlDetectionHelper.PossibleNamespacesResult result = wqlDetectionHelperMock.findPossibleNamespaces(
			null,
			wbemConfiguration
		);
		assertTrue(result.isSuccess());
		assertEquals(FORCED_NAMESPACE, result.getPossibleNamespaces().stream().findFirst().orElseThrow());
	}

	@Test
	void testWbemFindPossibleNamespacesNoResponse() throws Exception {
		final WbemConfiguration wbemConfiguration = WbemConfiguration
			.builder()
			.username(USERNAME)
			.password(PASSWORD.toCharArray())
			.build();

		// No response from the host
		doThrow(
			new MatsyaException(MATSYA_NO_RESPONSE_EXCEPTION_MESSAGE, new TimeoutException(WBEM_NAMESPACE_TIMEOUT_MESSAGE))
		)
			.when(matsyaClientsExecutorMock)
			.executeWbem(any(), eq(wbemConfiguration), any(), any());

		final WqlDetectionHelper.PossibleNamespacesResult result = wqlDetectionHelperMock.findPossibleNamespaces(
			LOCALHOST,
			wbemConfiguration
		);
		assertFalse(result.isSuccess());
		assertTrue(result.getErrorMessage().contains(WBEM_NAMESPACE_TIMEOUT_MESSAGE), WBEM_NAMESPACE_TIMEOUT_ERROR_MESSAGE);
		verify(matsyaClientsExecutorMock).executeWbem(any(), eq(wbemConfiguration), any(), any());
	}

	@Test
	void testWbemFindPossibleNamespacesEmpty() throws Exception {
		final WbemConfiguration wbemConfiguration = WbemConfiguration
			.builder()
			.username(USERNAME)
			.password(PASSWORD.toCharArray())
			.build();

		// We will always return "not found"
		doThrow(new MatsyaException(new WBEMException(WBEMException.CIM_ERR_NOT_FOUND)))
			.when(matsyaClientsExecutorMock)
			.executeWbem(any(), eq(wbemConfiguration), any(), any());

		final WqlDetectionHelper.PossibleNamespacesResult result = wqlDetectionHelperMock.findPossibleNamespaces(
			LOCALHOST,
			wbemConfiguration
		);
		assertFalse(result.isSuccess());
	}

	@Test
	void testWbemFindPossibleNamespaces() throws Exception {
		final WbemConfiguration wbemConfiguration = WbemConfiguration
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
			.when(matsyaClientsExecutorMock)
			.executeWbem(any(), eq(wbemConfiguration), any(), any());

		doReturn(List.of(List.of(FIRST_NAMESPACE)))
			.when(matsyaClientsExecutorMock)
			.executeWbem(any(), eq(wbemConfiguration), any(), eq(ROOT));

		doReturn(List.of(List.of(SECOND_NAMESPACE)))
			.when(matsyaClientsExecutorMock)
			.executeWbem(any(), eq(wbemConfiguration), any(), eq(INTEROP_NAMESPACE));

		final WqlDetectionHelper.PossibleNamespacesResult result = wqlDetectionHelperMock.findPossibleNamespaces(
			LOCALHOST,
			wbemConfiguration
		);
		assertTrue(result.isSuccess());
		assertEquals(2, result.getPossibleNamespaces().size());
		assertTrue(result.getPossibleNamespaces().contains(ROOT + "/" + FIRST_NAMESPACE));
		assertTrue(result.getPossibleNamespaces().contains(ROOT + "/" + SECOND_NAMESPACE));
	}

	@Test
	void testWmiFindPossibleNamespacesForcedProtocol() {
		// Namespace is forced
		final WmiConfiguration wmiConfiguration = WmiConfiguration.builder().namespace(FORCED_NAMESPACE).build();

		final WqlDetectionHelper.PossibleNamespacesResult result = wqlDetectionHelperMock.findPossibleNamespaces(
			null,
			wmiConfiguration
		);
		assertTrue(result.isSuccess());
		assertEquals(FORCED_NAMESPACE, result.getPossibleNamespaces().stream().findFirst().orElseThrow());
	}

	@Test
	void testWmiFindPossibleNamespacesNoResponse() throws Exception {
		final WmiConfiguration wmiConfiguration = WmiConfiguration
			.builder()
			.username(USERNAME)
			.password(PASSWORD.toCharArray())
			.build();

		// No response from the host
		doThrow(
			new MatsyaException(MATSYA_NO_RESPONSE_EXCEPTION_MESSAGE, new TimeoutException(WBEM_NAMESPACE_TIMEOUT_MESSAGE))
		)
			.when(matsyaClientsExecutorMock)
			.executeWql(any(), eq(wmiConfiguration), any(), any());

		final WqlDetectionHelper.PossibleNamespacesResult result = wqlDetectionHelperMock.findPossibleNamespaces(
			LOCALHOST,
			wmiConfiguration
		);
		assertFalse(result.isSuccess());
		assertTrue(result.getErrorMessage().contains(WBEM_NAMESPACE_TIMEOUT_MESSAGE), WBEM_NAMESPACE_TIMEOUT_ERROR_MESSAGE);
	}

	@Test
	void testWmiFindPossibleNamespacesEmpty() throws Exception {
		final WmiConfiguration wmiConfiguration = WmiConfiguration
			.builder()
			.username(USERNAME)
			.password(PASSWORD.toCharArray())
			.build();

		// We return an empty list
		doReturn(Collections.emptyList())
			.when(matsyaClientsExecutorMock)
			.executeWql(any(), eq(wmiConfiguration), any(), eq(ROOT));

		final WqlDetectionHelper.PossibleNamespacesResult result = wqlDetectionHelperMock.findPossibleNamespaces(
			LOCALHOST,
			wmiConfiguration
		);
		assertFalse(result.isSuccess());
	}

	@Test
	void testWmiFindPossibleNamespaces() throws Exception {
		final WmiConfiguration wmiConfiguration = WmiConfiguration
			.builder()
			.username(USERNAME)
			.password(PASSWORD.toCharArray())
			.build();

		doReturn(List.of(List.of(FIRST_NAMESPACE), List.of(SECOND_NAMESPACE)))
			.when(matsyaClientsExecutorMock)
			.executeWql(any(), eq(wmiConfiguration), any(), any());

		final WqlDetectionHelper.PossibleNamespacesResult result = wqlDetectionHelperMock.findPossibleNamespaces(
			LOCALHOST,
			wmiConfiguration
		);
		assertTrue(result.isSuccess());
		assertEquals(2, result.getPossibleNamespaces().size());
		assertTrue(result.getPossibleNamespaces().contains(ROOT + "/" + FIRST_NAMESPACE));
		assertTrue(result.getPossibleNamespaces().contains(ROOT + "/" + SECOND_NAMESPACE));
	}

	@Test
	void testPerformDetectionTest() throws Exception {
		// Invalid parameters

		assertThrows(
			IllegalArgumentException.class,
			() -> wqlDetectionHelperMock.performDetectionTest(LOCALHOST, null, null)
		);

		// MatsyaException

		final WmiConfiguration wmiConfiguration = WmiConfiguration.builder().build();
		doThrow(new MatsyaException(MATSYA_NO_RESPONSE_EXCEPTION_MESSAGE, new TimeoutException()))
			.when(matsyaClientsExecutorMock)
			.executeWql(any(), eq(wmiConfiguration), any(), any());
		WmiCriterion wmiCriterion = WmiCriterion.builder().query(WBEM_QUERY).build();
		CriterionTestResult result = wqlDetectionHelperMock.performDetectionTest(LOCALHOST, wmiConfiguration, wmiCriterion);
		assertFalse(result.isSuccess());
		assertNotNull(result.getException());
		assertTrue(result.getException() instanceof TimeoutException);

		// Empty result
		// MatsyaException
		doReturn(Collections.emptyList())
			.when(matsyaClientsExecutorMock)
			.executeWql(any(), eq(wmiConfiguration), any(), any());
		result = wqlDetectionHelperMock.performDetectionTest(LOCALHOST, wmiConfiguration, wmiCriterion);
		assertFalse(result.isSuccess());
		assertNull(result.getException());

		// Non-empty result, and no expected result => success

		doReturn(EXCUTE_WBEM_RESULT).when(matsyaClientsExecutorMock).executeWql(any(), eq(wmiConfiguration), any(), any());
		result = wqlDetectionHelperMock.performDetectionTest(LOCALHOST, wmiConfiguration, wmiCriterion);
		assertTrue(result.isSuccess());
		assertTrue(result.getMessage().contains(EXCUTE_WBEM_RESULT_ELEMENT), RESULT_MESSAGE_SHOULD_CONTAIN_RESULT);

		// Non-empty result, and matching expected result => success
		wmiCriterion =
			WmiCriterion.builder().query(WBEM_QUERY).expectedResult(WEBM_CRITERION_SUCCESS_EXPECTED_RESULT).build();
		result = wqlDetectionHelperMock.performDetectionTest(LOCALHOST, wmiConfiguration, wmiCriterion);
		assertTrue(result.isSuccess());
		assertTrue(
			result.getMessage().contains(WEBM_CRITERION_SUCCESS_EXPECTED_RESULT),
			RESULT_MESSAGE_SHOULD_CONTAIN_RESULT
		);

		// Non-empty result, and non-matching expected result => failure
		wmiCriterion =
			WmiCriterion.builder().query(WBEM_QUERY).expectedResult(WEBM_CRITERION_NOT_MATCHING_EXPECTED_RESULT).build();
		result = wqlDetectionHelperMock.performDetectionTest(LOCALHOST, wmiConfiguration, wmiCriterion);
		assertFalse(result.isSuccess());
		assertNull(result.getException());
		assertTrue(result.getMessage().contains(EXCUTE_WBEM_RESULT_ELEMENT), RESULT_MESSAGE_SHOULD_CONTAIN_RESULT);
	}

	@Test
	void testDetectNamespaceNoResponse() throws Exception {
		// No response at all => we fail early (we don't try every single namespace)
		final WmiConfiguration wmiConfiguration = WmiConfiguration.builder().build();
		final WmiCriterion wmiCriterion = WmiCriterion
			.builder()
			.query(WBEM_QUERY)
			.expectedResult(WEBM_CRITERION_NOT_MATCHING_EXPECTED_RESULT)
			.build();
		doThrow(new MatsyaException(MATSYA_NO_RESPONSE_EXCEPTION_MESSAGE, new TimeoutException()))
			.when(matsyaClientsExecutorMock)
			.executeWql(any(), eq(wmiConfiguration), any(), any());

		final WqlDetectionHelper.NamespaceResult result = wqlDetectionHelperMock.detectNamespace(
			LOCALHOST,
			wmiConfiguration,
			wmiCriterion,
			Set.of(FIRST_NAMESPACE, SECOND_NAMESPACE)
		);
		assertFalse(result.getResult().isSuccess());
		assertTrue(result.getResult().getMessage().contains(TIMEOUT_EXCEPTION));
		verify(matsyaClientsExecutorMock).executeWql(any(), eq(wmiConfiguration), any(), any());
	}

	@Test
	void testDetectNamespaceEmpty() throws Exception {
		// Non-matching result AND empty result (with an error that doesn't stop the loop)
		final WmiConfiguration wmiConfiguration = WmiConfiguration.builder().build();
		final WmiCriterion wmiCriterion = WmiCriterion
			.builder()
			.query(WBEM_QUERY)
			.expectedResult(WEBM_CRITERION_NOT_MATCHING_EXPECTED_RESULT)
			.build();
		doThrow(new MatsyaException(MATSYA_NO_RESPONSE_EXCEPTION_MESSAGE, new WmiComException(WMI_COM_EXCEPTION_MESSAGE)))
			.when(matsyaClientsExecutorMock)
			.executeWql(any(), eq(wmiConfiguration), any(), eq(FIRST_NAMESPACE));
		doReturn(EXCUTE_WBEM_RESULT)
			.when(matsyaClientsExecutorMock)
			.executeWql(any(), eq(wmiConfiguration), any(), eq(SECOND_NAMESPACE));

		final WqlDetectionHelper.NamespaceResult result = wqlDetectionHelperMock.detectNamespace(
			LOCALHOST,
			wmiConfiguration,
			wmiCriterion,
			Set.of(FIRST_NAMESPACE, SECOND_NAMESPACE)
		);
		assertFalse(result.getResult().isSuccess());
		assertNull(result.getResult().getException());
		verify(matsyaClientsExecutorMock, times(2)).executeWql(any(), eq(wmiConfiguration), any(), any());
	}

	@Test
	void testDetectNamespace() throws Exception {
		// 3 matching result, and root\\cimv2 must be removed
		final WmiConfiguration wmiConfiguration = WmiConfiguration.builder().build();
		final WmiCriterion wmiCriterion = WmiCriterion.builder().query(WBEM_QUERY).build();
		doReturn(EXCUTE_WBEM_RESULT).when(matsyaClientsExecutorMock).executeWql(any(), eq(wmiConfiguration), any(), any());

		final WqlDetectionHelper.NamespaceResult result = wqlDetectionHelperMock.detectNamespace(
			LOCALHOST,
			wmiConfiguration,
			wmiCriterion,
			Set.of(CRITERION_WMI_NAMESPACE, FIRST_NAMESPACE)
		);
		assertTrue(result.getResult().isSuccess());
		assertNull(result.getResult().getException());
		assertEquals(FIRST_NAMESPACE, result.getNamespace());
		verify(matsyaClientsExecutorMock, times(2)).executeWql(any(), eq(wmiConfiguration), any(), any());
	}

	@Test
	void testDetectNamespaceCimv2() throws Exception {
		// 1 single matching result: root\\cimv2 which must not be removed
		final WmiConfiguration wmiConfiguration = WmiConfiguration.builder().build();
		final WmiCriterion wmiCriterion = WmiCriterion.builder().query(WBEM_QUERY).build();
		doReturn(EXCUTE_WBEM_RESULT).when(matsyaClientsExecutorMock).executeWql(any(), eq(wmiConfiguration), any(), any());

		final WqlDetectionHelper.NamespaceResult result = wqlDetectionHelperMock.detectNamespace(
			LOCALHOST,
			wmiConfiguration,
			wmiCriterion,
			Set.of(CRITERION_WMI_NAMESPACE)
		);
		assertTrue(result.getResult().isSuccess());
		assertEquals(CRITERION_WMI_NAMESPACE, result.getNamespace());
	}

	@Test
	void testIsAcceptableException() {
		assertFalse(WqlDetectionHelper.isAcceptableException(null));
		assertFalse(WqlDetectionHelper.isAcceptableException(new Exception()));
		assertFalse(WqlDetectionHelper.isAcceptableException(new Exception(new Exception())));

		assertFalse(WqlDetectionHelper.isAcceptableException(new WmiComException(WMI_EXCEPTION_OTHER_MESSAGE)));
		assertFalse(WqlDetectionHelper.isAcceptableException(new WmiComException(new Exception())));
		assertTrue(WqlDetectionHelper.isAcceptableException(new WmiComException(WMI_EXCEPTION_WBEM_E_NOT_FOUND_MESSAGE)));
		assertTrue(
			WqlDetectionHelper.isAcceptableException(new WmiComException(WMI_EXCEPTION_WBEM_E_INVALID_NAMESPACE_MESSAGE))
		);
		assertTrue(
			WqlDetectionHelper.isAcceptableException(new WmiComException(WMI_EXCEPTION_WBEM_E_INVALID_CLASS_MESSAGE))
		);

		assertFalse(WqlDetectionHelper.isAcceptableException(new WBEMException(WMI_EXCEPTION_OTHER_MESSAGE)));
		assertFalse(WqlDetectionHelper.isAcceptableException(new WBEMException(0)));
		assertTrue(WqlDetectionHelper.isAcceptableException(new WBEMException(WBEMException.CIM_ERR_NOT_FOUND)));
		assertTrue(WqlDetectionHelper.isAcceptableException(new WBEMException(WBEMException.CIM_ERR_INVALID_NAMESPACE)));
		assertTrue(WqlDetectionHelper.isAcceptableException(new WBEMException(WBEMException.CIM_ERR_INVALID_CLASS)));

		assertTrue(
			WqlDetectionHelper.isAcceptableException(
				new Exception(new WmiComException(WMI_EXCEPTION_WBEM_E_NOT_FOUND_MESSAGE))
			)
		);
		assertTrue(
			WqlDetectionHelper.isAcceptableException(new Exception(new WBEMException(WBEMException.CIM_ERR_NOT_FOUND)))
		);
	}
}
