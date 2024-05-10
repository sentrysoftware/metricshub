package org.sentrysoftware;

import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.engine.strategy.utils.WqlDetectionHelper;
import org.sentrysoftware.metricshub.extension.wbem.WbemRequestExecutor;
import org.sentrysoftware.wbem.javax.wbem.WBEMException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WbemRequestExecutorTest {
	@Test
	void testIsAcceptableException() {
		assertFalse(WqlDetectionHelper.isAcceptableException(null));
		assertFalse(WqlDetectionHelper.isAcceptableException(new Exception()));
		assertFalse(WqlDetectionHelper.isAcceptableException(new Exception(new Exception())));

		assertFalse(WqlDetectionHelper.isAcceptableException(new WBEMException("other")));
		assertFalse(WqlDetectionHelper.isAcceptableException(new WBEMException(0)));
		assertTrue(WqlDetectionHelper.isAcceptableException(new WBEMException(WBEMException.CIM_ERR_NOT_FOUND)));
		assertTrue(WqlDetectionHelper.isAcceptableException(new WBEMException(WBEMException.CIM_ERR_INVALID_NAMESPACE)));
		assertTrue(WqlDetectionHelper.isAcceptableException(new WBEMException(WBEMException.CIM_ERR_INVALID_CLASS)));

		assertTrue(
				WqlDetectionHelper.isAcceptableException(new Exception(new WBEMException(WBEMException.CIM_ERR_NOT_FOUND)))
		);
	}
}
