package com.sentrysoftware.matrix.engine.target;

import static org.junit.jupiter.api.Assertions.*;
import static com.sentrysoftware.matrix.engine.target.TargetType.*;
import org.junit.jupiter.api.Test;

class TargetTypeTest {

	@Test
	void testInterpretValueOf() {
		assertNull(interpretValueOf(null));

		assertEquals(LINUX, interpretValueOf("Linux"));
		assertEquals(LINUX, interpretValueOf("LNX"));
		assertEquals(MS_WINDOWS, interpretValueOf("ms-win"));
		assertEquals(MS_WINDOWS, interpretValueOf("Win64"));
		assertEquals(MGMT_CARD_BLADE_ESXI, interpretValueOf("VMware"));
		assertEquals(MGMT_CARD_BLADE_ESXI, interpretValueOf("ESXi"));
		assertEquals(MGMT_CARD_BLADE_ESXI, interpretValueOf("Out-of-band"));
		assertEquals(MGMT_CARD_BLADE_ESXI, interpretValueOf("Management Card"));
		assertEquals(MGMT_CARD_BLADE_ESXI, interpretValueOf("mgmt0"));
		assertEquals(MGMT_CARD_BLADE_ESXI, interpretValueOf("BLADE"));
		assertEquals(NETWORK_SWITCH, interpretValueOf("Network"));
		assertEquals(NETWORK_SWITCH, interpretValueOf("Switch"));
		assertEquals(STORAGE, interpretValueOf("SAN-Switch"));
		assertEquals(STORAGE, interpretValueOf("Storage"));
		assertEquals(STORAGE, interpretValueOf("SAN"));
		assertEquals(HP_OPEN_VMS, interpretValueOf("HP_OPEN_VMS"));
		assertEquals(HP_TRU64_UNIX, interpretValueOf("HP-Tru64"));
		assertEquals(HP_TRU64_UNIX, interpretValueOf("Digital-OSF1"));
		assertEquals(HP_UX, interpretValueOf("HP-UX"));
		assertEquals(HP_UX, interpretValueOf("HPUX"));
		assertEquals(IBM_AIX, interpretValueOf("IBM-AIX"));
		assertEquals(IBM_AIX, interpretValueOf("AIX"));
		assertEquals(SUN_SOLARIS, interpretValueOf("sol32"));
		assertEquals(SUN_SOLARIS, interpretValueOf("Oracle-Solaris"));
		assertEquals(SUN_SOLARIS, interpretValueOf("OpenSolaris"));
		assertEquals(SUN_SOLARIS, interpretValueOf("SunOS"));

		assertThrows(IllegalArgumentException.class, () -> interpretValueOf("invalid"));
	}

}
