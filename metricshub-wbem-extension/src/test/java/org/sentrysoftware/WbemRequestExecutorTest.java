package org.sentrysoftware;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.extension.wbem.WbemRequestExecutor;
import org.sentrysoftware.wbem.javax.wbem.WBEMException;

public class WbemRequestExecutorTest {

	WbemRequestExecutor wbemRequestExecutor = new WbemRequestExecutor();

	@Test
	void testIsAcceptableException() {
		assertFalse(wbemRequestExecutor.isAcceptableException(null));
		assertFalse(wbemRequestExecutor.isAcceptableException(new Exception()));
		assertFalse(wbemRequestExecutor.isAcceptableException(new Exception(new Exception())));

		assertFalse(wbemRequestExecutor.isAcceptableException(new WBEMException("other")));
		assertFalse(wbemRequestExecutor.isAcceptableException(new WBEMException(0)));
		assertTrue(wbemRequestExecutor.isAcceptableException(new WBEMException(WBEMException.CIM_ERR_NOT_FOUND)));
		assertTrue(wbemRequestExecutor.isAcceptableException(new WBEMException(WBEMException.CIM_ERR_INVALID_NAMESPACE)));
		assertTrue(wbemRequestExecutor.isAcceptableException(new WBEMException(WBEMException.CIM_ERR_INVALID_CLASS)));

		assertTrue(
			wbemRequestExecutor.isAcceptableException(new Exception(new WBEMException(WBEMException.CIM_ERR_NOT_FOUND)))
		);
	}
}
