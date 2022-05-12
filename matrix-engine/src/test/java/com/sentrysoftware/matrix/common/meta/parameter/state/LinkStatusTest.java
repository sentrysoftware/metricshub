package com.sentrysoftware.matrix.common.meta.parameter.state;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class LinkStatusTest {

	@Test
	void testInterpretNull() {
		assertEquals(Optional.empty(), LinkStatus.interpret(null));
		assertEquals(Optional.empty(), LinkStatus.interpret(""));
		assertEquals(Optional.empty(), LinkStatus.interpret("unknown"));
	}

	@Test
	void testInterpretPlugged() {
		assertEquals(Optional.of(LinkStatus.PLUGGED), LinkStatus.interpret("0"));
		assertEquals(Optional.of(LinkStatus.PLUGGED), LinkStatus.interpret("0.0"));
		assertEquals(Optional.of(LinkStatus.PLUGGED), LinkStatus.interpret("ok"));
		assertEquals(Optional.of(LinkStatus.PLUGGED), LinkStatus.interpret(" ok "));
		assertEquals(Optional.of(LinkStatus.PLUGGED), LinkStatus.interpret(" Ok "));
		assertEquals(Optional.of(LinkStatus.PLUGGED), LinkStatus.interpret(" OK "));
		assertEquals(Optional.of(LinkStatus.PLUGGED), LinkStatus.interpret("OK"));
	}

	@Test
	void testInterpretUnpluggedFromWarn() {
		assertEquals(Optional.of(LinkStatus.UNPLUGGED), LinkStatus.interpret("1"));
		assertEquals(Optional.of(LinkStatus.UNPLUGGED), LinkStatus.interpret("1.0"));
		assertEquals(Optional.of(LinkStatus.UNPLUGGED), LinkStatus.interpret("warn"));
		assertEquals(Optional.of(LinkStatus.UNPLUGGED), LinkStatus.interpret(" warn "));
		assertEquals(Optional.of(LinkStatus.UNPLUGGED), LinkStatus.interpret(" WARN "));
		assertEquals(Optional.of(LinkStatus.UNPLUGGED), LinkStatus.interpret("WARN"));
		assertEquals(Optional.of(LinkStatus.UNPLUGGED), LinkStatus.interpret("warning"));
		assertEquals(Optional.of(LinkStatus.UNPLUGGED), LinkStatus.interpret(" warning "));
		assertEquals(Optional.of(LinkStatus.UNPLUGGED), LinkStatus.interpret(" WARNING "));
		assertEquals(Optional.of(LinkStatus.UNPLUGGED), LinkStatus.interpret("WARNING"));
	}

	@Test
	void testInterpretUnpluggedFromAlarm() {
		assertEquals(Optional.of(LinkStatus.UNPLUGGED), LinkStatus.interpret("2.0"));
		assertEquals(Optional.of(LinkStatus.UNPLUGGED), LinkStatus.interpret("2"));
		assertEquals(Optional.of(LinkStatus.UNPLUGGED), LinkStatus.interpret("2 "));
		assertEquals(Optional.of(LinkStatus.UNPLUGGED), LinkStatus.interpret("alarm"));
		assertEquals(Optional.of(LinkStatus.UNPLUGGED), LinkStatus.interpret(" alarm "));
		assertEquals(Optional.of(LinkStatus.UNPLUGGED), LinkStatus.interpret(" Alarm "));
		assertEquals(Optional.of(LinkStatus.UNPLUGGED), LinkStatus.interpret(" ALARM "));
		assertEquals(Optional.of(LinkStatus.UNPLUGGED), LinkStatus.interpret("ALARM"));
	}

}
