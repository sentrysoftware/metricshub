package com.sentrysoftware.matrix.connector.parser.state.compute.convert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.ConversionType;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Convert;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SnmpGetTableSource;

class ConversionTypeProcessorTest {

	private static final String CONVERSION_TYPE_KEY = "enclosure.collect.source(1).compute(1).ConversionType";

	private static ConversionTypeProcessor conversionTypeProcessor = new ConversionTypeProcessor();

	@Test
	void testGetMatcher() {
		assertNotNull(conversionTypeProcessor.getMatcher(CONVERSION_TYPE_KEY));
	}

	@Test
	void testParse() {
		{
			final Connector connector = new Connector();

			final Convert convert = Convert.builder().index(1).build();

			final SnmpGetTableSource snmpGetTableSource = SnmpGetTableSource
					.builder()
					.index(1)
					.computes(Collections.singletonList(convert))
					.build();

			final Collect collect = Collect.builder()
					.sources(Collections.singletonList(snmpGetTableSource))
					.build();

			final HardwareMonitor hardwareMonitor = HardwareMonitor.builder().type(MonitorType.ENCLOSURE).collect(collect).build();

			connector.getHardwareMonitors().add(hardwareMonitor);

			conversionTypeProcessor.parse(CONVERSION_TYPE_KEY, "hex2dec", connector);

			assertEquals(ConversionType.HEX_2_DEC, convert.getConversionType());
		}

		{
			final Connector connector = new Connector();

			final Convert convert = Convert.builder().index(1).build();

			final SnmpGetTableSource snmpGetTableSource = SnmpGetTableSource
					.builder()
					.index(1)
					.computes(Collections.singletonList(convert))
					.build();

			final Collect collect = Collect.builder()
					.sources(Collections.singletonList(snmpGetTableSource))
					.build();

			final HardwareMonitor hardwareMonitor = HardwareMonitor.builder().type(MonitorType.ENCLOSURE).collect(collect).build();

			connector.getHardwareMonitors().add(hardwareMonitor);

			conversionTypeProcessor.parse(CONVERSION_TYPE_KEY, "Array2SimpleStatus", connector);

			assertEquals(ConversionType.ARRAY_2_SIMPLE_STATUS, convert.getConversionType());
		}
	}

}
