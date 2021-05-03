package com.sentrysoftware.matrix.engine.strategy.collect;

import static com.sentrysoftware.matrix.connector.model.monitor.MonitorType.ENCLOSURE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.model.parameter.ParameterState;

class CollectHelperTest {

	private static final List<String> ROW = Arrays.asList("0", "100", "400");
	private static final ParameterState UNKNOWN_STATUS_WARN = ParameterState.WARN;
	private static final ParameterState UNKNOWN_STATUS_OK = ParameterState.OK;
	private static final ParameterState UNKNOWN_STATUS_ALARM = ParameterState.ALARM;
	private static final String HOST_NAME = "host";
	private static final String ID = "enclosure_1";
	private static final String VALUE_TABLE = "Enclosure.Collect.Source(1)";

	@Test
	void testTranslateStatus() {
		assertNull(CollectHelper.translateStatus(null,
				UNKNOWN_STATUS_WARN,
				ID,
				HOST_NAME,
				HardwareConstants.STATUS_PARAMETER));

		assertEquals(ParameterState.OK, CollectHelper.translateStatus("0",
				UNKNOWN_STATUS_WARN,
				ID,
				HOST_NAME,
				HardwareConstants.STATUS_PARAMETER));

		assertEquals(ParameterState.OK, CollectHelper.translateStatus("OK",
				UNKNOWN_STATUS_WARN,
				ID,
				HOST_NAME,
				HardwareConstants.STATUS_PARAMETER));

		assertEquals(UNKNOWN_STATUS_WARN, CollectHelper.translateStatus("1",
				UNKNOWN_STATUS_WARN,
				ID,
				HOST_NAME,
				HardwareConstants.STATUS_PARAMETER));

		assertEquals(UNKNOWN_STATUS_WARN, CollectHelper.translateStatus("WARN",
				UNKNOWN_STATUS_WARN,
				ID,
				HOST_NAME,
				HardwareConstants.STATUS_PARAMETER));

		assertEquals(ParameterState.ALARM, CollectHelper.translateStatus("2",
				UNKNOWN_STATUS_WARN,
				ID,
				HOST_NAME,
				HardwareConstants.STATUS_PARAMETER));

		assertEquals(ParameterState.ALARM, CollectHelper.translateStatus("ALARM",
				UNKNOWN_STATUS_WARN,
				ID,
				HOST_NAME,
				HardwareConstants.STATUS_PARAMETER));

		assertEquals(ParameterState.ALARM, CollectHelper.translateStatus("ON",
				UNKNOWN_STATUS_WARN,
				ID,
				HOST_NAME,
				HardwareConstants.STATUS_PARAMETER));

		assertEquals(ParameterState.WARN, CollectHelper.translateStatus("BLINKING",
				UNKNOWN_STATUS_WARN,
				ID,
				HOST_NAME,
				HardwareConstants.STATUS_PARAMETER));

		assertEquals(ParameterState.OK, CollectHelper.translateStatus("OFF",
				UNKNOWN_STATUS_WARN,
				ID,
				HOST_NAME,
				HardwareConstants.STATUS_PARAMETER));

		assertEquals(UNKNOWN_STATUS_WARN, CollectHelper.translateStatus("SOMETHING_UNKNOWN",
				UNKNOWN_STATUS_WARN,
				ID,
				HOST_NAME,
				HardwareConstants.STATUS_PARAMETER));

		assertEquals(UNKNOWN_STATUS_OK, CollectHelper.translateStatus("SOMETHING_UNKNOWN",
				UNKNOWN_STATUS_OK,
				ID,
				HOST_NAME,
				HardwareConstants.STATUS_PARAMETER));

		assertEquals(UNKNOWN_STATUS_ALARM, CollectHelper.translateStatus("SOMETHING_UNKNOWN",
				UNKNOWN_STATUS_ALARM,
				ID,
				HOST_NAME,
				HardwareConstants.STATUS_PARAMETER));
	}


	@Test
	void testGetValueTableColumnValue() {

		assertNull(CollectHelper.getValueTableColumnValue(VALUE_TABLE,
				HardwareConstants.STATUS_PARAMETER,
				ENCLOSURE,
				Collections.emptyList(),
				null));

		assertEquals("100", CollectHelper.getValueTableColumnValue(VALUE_TABLE,
				HardwareConstants.STATUS_PARAMETER,
				ENCLOSURE,
				ROW,
				"Valuetable.Column(2)"));

		assertEquals("400", CollectHelper.getValueTableColumnValue(VALUE_TABLE,
				HardwareConstants.STATUS_PARAMETER,
				ENCLOSURE,
				ROW,
				"Valuetable.Column(3)"));

		assertNull(CollectHelper.getValueTableColumnValue(VALUE_TABLE,
				HardwareConstants.STATUS_PARAMETER,
				ENCLOSURE,
				ROW,
				"Valuetable.Column(4)"));

		assertNull(CollectHelper.getValueTableColumnValue(VALUE_TABLE,
				HardwareConstants.STATUS_PARAMETER,
				ENCLOSURE,
				ROW,
				"Valuetable.Column(string)"));

		assertNull(CollectHelper.getValueTableColumnValue(VALUE_TABLE,
				HardwareConstants.STATUS_PARAMETER,
				ENCLOSURE,
				ROW,
				"Valuetable.Column(-1)"));

		assertNull(CollectHelper.getValueTableColumnValue(VALUE_TABLE,
				HardwareConstants.STATUS_PARAMETER,
				ENCLOSURE,
				ROW,
				"Valuetable.Column(0)"));
	}

}
