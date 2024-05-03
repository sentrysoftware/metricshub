package org.sentrysoftware.metricshub.engine.strategy.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.sentrysoftware.metricshub.engine.constants.Constants.FIRST_NAMESPACE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.FORCED_NAMESPACE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.INTEROP_NAMESPACE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.LOCALHOST;
import static org.sentrysoftware.metricshub.engine.constants.Constants.MATSYA_NO_RESPONSE_EXCEPTION_MESSAGE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.PASSWORD;
import static org.sentrysoftware.metricshub.engine.constants.Constants.ROOT;
import static org.sentrysoftware.metricshub.engine.constants.Constants.SECOND_NAMESPACE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.USERNAME;
import static org.sentrysoftware.metricshub.engine.constants.Constants.WBEM_NAMESPACE_TIMEOUT_ERROR_MESSAGE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.WBEM_NAMESPACE_TIMEOUT_MESSAGE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.WMI_EXCEPTION_OTHER_MESSAGE;

import java.util.List;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sentrysoftware.metricshub.engine.client.ClientsExecutor;
import org.sentrysoftware.metricshub.engine.common.exception.ClientException;
import org.sentrysoftware.metricshub.engine.configuration.WbemConfiguration;
import org.sentrysoftware.wbem.javax.wbem.WBEMException;

@ExtendWith(MockitoExtension.class)
class WqlDetectionHelperTest {

	@Mock
	private ClientsExecutor clientsExecutorMock;

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
			new ClientException(MATSYA_NO_RESPONSE_EXCEPTION_MESSAGE, new TimeoutException(WBEM_NAMESPACE_TIMEOUT_MESSAGE))
		)
			.when(clientsExecutorMock)
			.executeWbem(any(), eq(wbemConfiguration), any(), any());

		final WqlDetectionHelper.PossibleNamespacesResult result = wqlDetectionHelperMock.findPossibleNamespaces(
			LOCALHOST,
			wbemConfiguration
		);
		assertFalse(result.isSuccess());
		assertTrue(result.getErrorMessage().contains(WBEM_NAMESPACE_TIMEOUT_MESSAGE), WBEM_NAMESPACE_TIMEOUT_ERROR_MESSAGE);
		verify(clientsExecutorMock).executeWbem(any(), eq(wbemConfiguration), any(), any());
	}

	@Test
	void testWbemFindPossibleNamespacesEmpty() throws Exception {
		final WbemConfiguration wbemConfiguration = WbemConfiguration
			.builder()
			.username(USERNAME)
			.password(PASSWORD.toCharArray())
			.build();

		// We will always return "not found"
		doThrow(new ClientException(new WBEMException(WBEMException.CIM_ERR_NOT_FOUND)))
			.when(clientsExecutorMock)
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
			new ClientException(new WBEMException(WBEMException.CIM_ERR_INVALID_NAMESPACE)),
			new ClientException(new WBEMException(WBEMException.CIM_ERR_INVALID_CLASS)),
			new ClientException(new WBEMException(WBEMException.CIM_ERR_NOT_FOUND))
		)
			.when(clientsExecutorMock)
			.executeWbem(any(), eq(wbemConfiguration), any(), any());

		doReturn(List.of(List.of(FIRST_NAMESPACE)))
			.when(clientsExecutorMock)
			.executeWbem(any(), eq(wbemConfiguration), any(), eq(ROOT));

		doReturn(List.of(List.of(SECOND_NAMESPACE)))
			.when(clientsExecutorMock)
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
	void testIsAcceptableException() {
		assertFalse(WqlDetectionHelper.isAcceptableException(null));
		assertFalse(WqlDetectionHelper.isAcceptableException(new Exception()));
		assertFalse(WqlDetectionHelper.isAcceptableException(new Exception(new Exception())));

		assertFalse(WqlDetectionHelper.isAcceptableException(new WBEMException(WMI_EXCEPTION_OTHER_MESSAGE)));
		assertFalse(WqlDetectionHelper.isAcceptableException(new WBEMException(0)));
		assertTrue(WqlDetectionHelper.isAcceptableException(new WBEMException(WBEMException.CIM_ERR_NOT_FOUND)));
		assertTrue(WqlDetectionHelper.isAcceptableException(new WBEMException(WBEMException.CIM_ERR_INVALID_NAMESPACE)));
		assertTrue(WqlDetectionHelper.isAcceptableException(new WBEMException(WBEMException.CIM_ERR_INVALID_CLASS)));

		assertTrue(
			WqlDetectionHelper.isAcceptableException(new Exception(new WBEMException(WBEMException.CIM_ERR_NOT_FOUND)))
		);
	}
}
