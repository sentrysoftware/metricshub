package org.sentrysoftware.metricshub.engine.strategy.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.sentrysoftware.metricshub.engine.constants.Constants.WMI_EXCEPTION_OTHER_MESSAGE;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sentrysoftware.metricshub.engine.client.ClientsExecutor;
import org.sentrysoftware.wbem.javax.wbem.WBEMException;

@ExtendWith(MockitoExtension.class)
class WqlDetectionHelperTest {

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
