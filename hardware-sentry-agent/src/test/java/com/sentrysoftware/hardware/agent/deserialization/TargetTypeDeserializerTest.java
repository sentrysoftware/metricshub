package com.sentrysoftware.hardware.agent.deserialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import com.sentrysoftware.matrix.engine.target.TargetType;

@ExtendWith(MockitoExtension.class)
class TargetTypeDeserializerTest {

	@Mock
	private YAMLParser yamlParser;

	@Test
	void testNull() throws IOException {

		{
			assertNull(new TargetTypeDeserializer().deserialize(null, null));
		}

		{
			doReturn(null).when(yamlParser).getValueAsString();
			assertNull(new TargetTypeDeserializer().deserialize(yamlParser, null));
		}

	}

	@Test
	void testBadValue() throws IOException {
		{
			doReturn("unknown").when(yamlParser).getValueAsString();
			assertThrows(IOException.class, () -> new TargetTypeDeserializer().deserialize(yamlParser, null));
		}

		{
			doReturn("").when(yamlParser).getValueAsString();
			assertThrows(IOException.class, () -> new TargetTypeDeserializer().deserialize(yamlParser, null));
		}

	}

	@Test
	void testLinux() throws IOException {
		doReturn("linux").when(yamlParser).getValueAsString();
		assertEquals(TargetType.LINUX, new TargetTypeDeserializer().deserialize(yamlParser, null));

		doReturn("lin").when(yamlParser).getValueAsString();
		assertEquals(TargetType.LINUX, new TargetTypeDeserializer().deserialize(yamlParser, null));

		doReturn("linx").when(yamlParser).getValueAsString();
		assertEquals(TargetType.LINUX, new TargetTypeDeserializer().deserialize(yamlParser, null));

	}

	@Test
	void testWindows() throws IOException {
		doReturn("win").when(yamlParser).getValueAsString();
		assertEquals(TargetType.MS_WINDOWS, new TargetTypeDeserializer().deserialize(yamlParser, null));

		doReturn("microsoft win").when(yamlParser).getValueAsString();
		assertEquals(TargetType.MS_WINDOWS, new TargetTypeDeserializer().deserialize(yamlParser, null));

		doReturn("ms win").when(yamlParser).getValueAsString();
		assertEquals(TargetType.MS_WINDOWS, new TargetTypeDeserializer().deserialize(yamlParser, null));

		doReturn("ms_windows").when(yamlParser).getValueAsString();
		assertEquals(TargetType.MS_WINDOWS, new TargetTypeDeserializer().deserialize(yamlParser, null));
	}

	@Test
	void testOob() throws IOException {
		doReturn("oob").when(yamlParser).getValueAsString();
		assertEquals(TargetType.MGMT_CARD_BLADE_ESXI, new TargetTypeDeserializer().deserialize(yamlParser, null));

		doReturn("esx").when(yamlParser).getValueAsString();
		assertEquals(TargetType.MGMT_CARD_BLADE_ESXI, new TargetTypeDeserializer().deserialize(yamlParser, null));

		doReturn("blade").when(yamlParser).getValueAsString();
		assertEquals(TargetType.MGMT_CARD_BLADE_ESXI, new TargetTypeDeserializer().deserialize(yamlParser, null));

		doReturn("vmware").when(yamlParser).getValueAsString();
		assertEquals(TargetType.MGMT_CARD_BLADE_ESXI, new TargetTypeDeserializer().deserialize(yamlParser, null));

		doReturn("mgmt").when(yamlParser).getValueAsString();
		assertEquals(TargetType.MGMT_CARD_BLADE_ESXI, new TargetTypeDeserializer().deserialize(yamlParser, null));

		doReturn("management").when(yamlParser).getValueAsString();
		assertEquals(TargetType.MGMT_CARD_BLADE_ESXI, new TargetTypeDeserializer().deserialize(yamlParser, null));

		doReturn("MGMT_CARD_BLADE_ESXI").when(yamlParser).getValueAsString();
		assertEquals(TargetType.MGMT_CARD_BLADE_ESXI, new TargetTypeDeserializer().deserialize(yamlParser, null));
	}

	@Test
	void testNetwork() throws IOException {

		doReturn("network").when(yamlParser).getValueAsString();
		assertEquals(TargetType.NETWORK_SWITCH, new TargetTypeDeserializer().deserialize(yamlParser, null));

		doReturn("net").when(yamlParser).getValueAsString();
		assertEquals(TargetType.NETWORK_SWITCH, new TargetTypeDeserializer().deserialize(yamlParser, null));

		doReturn("switch").when(yamlParser).getValueAsString();
		assertEquals(TargetType.NETWORK_SWITCH, new TargetTypeDeserializer().deserialize(yamlParser, null));

		doReturn("NETWORK_SWITCH").when(yamlParser).getValueAsString();
		assertEquals(TargetType.NETWORK_SWITCH, new TargetTypeDeserializer().deserialize(yamlParser, null));

	}

	@Test
	void testStorage() throws IOException {

		doReturn("storage").when(yamlParser).getValueAsString();
		assertEquals(TargetType.STORAGE, new TargetTypeDeserializer().deserialize(yamlParser, null));

		doReturn("sto").when(yamlParser).getValueAsString();
		assertEquals(TargetType.STORAGE, new TargetTypeDeserializer().deserialize(yamlParser, null));

		doReturn("san").when(yamlParser).getValueAsString();
		assertEquals(TargetType.STORAGE, new TargetTypeDeserializer().deserialize(yamlParser, null));

		doReturn("STORAGE").when(yamlParser).getValueAsString();
		assertEquals(TargetType.STORAGE, new TargetTypeDeserializer().deserialize(yamlParser, null));

	}

	@Test
	void testTru64() throws IOException {

		doReturn("tru64").when(yamlParser).getValueAsString();
		assertEquals(TargetType.HP_TRU64_UNIX, new TargetTypeDeserializer().deserialize(yamlParser, null));

		doReturn("osf").when(yamlParser).getValueAsString();
		assertEquals(TargetType.HP_TRU64_UNIX, new TargetTypeDeserializer().deserialize(yamlParser, null));

		doReturn("HP_TRU64_UNIX").when(yamlParser).getValueAsString();
		assertEquals(TargetType.HP_TRU64_UNIX, new TargetTypeDeserializer().deserialize(yamlParser, null));

	}

	@Test
	void testHpUx() throws IOException {

		doReturn("hpux").when(yamlParser).getValueAsString();
		assertEquals(TargetType.HP_UX, new TargetTypeDeserializer().deserialize(yamlParser, null));

		doReturn("hp ux").when(yamlParser).getValueAsString();
		assertEquals(TargetType.HP_UX, new TargetTypeDeserializer().deserialize(yamlParser, null));

		doReturn("HP_UX").when(yamlParser).getValueAsString();
		assertEquals(TargetType.HP_UX, new TargetTypeDeserializer().deserialize(yamlParser, null));

	}

	@Test
	void testAix() throws IOException {

		doReturn("aix").when(yamlParser).getValueAsString();
		assertEquals(TargetType.IBM_AIX, new TargetTypeDeserializer().deserialize(yamlParser, null));

		doReturn("rs6000").when(yamlParser).getValueAsString();
		assertEquals(TargetType.IBM_AIX, new TargetTypeDeserializer().deserialize(yamlParser, null));

		doReturn("IBM_AIX").when(yamlParser).getValueAsString();
		assertEquals(TargetType.IBM_AIX, new TargetTypeDeserializer().deserialize(yamlParser, null));

	}

	@Test
	void testSun() throws IOException {

		doReturn("sun").when(yamlParser).getValueAsString();
		assertEquals(TargetType.SUN_SOLARIS, new TargetTypeDeserializer().deserialize(yamlParser, null));

		doReturn("oracle").when(yamlParser).getValueAsString();
		assertEquals(TargetType.SUN_SOLARIS, new TargetTypeDeserializer().deserialize(yamlParser, null));

		doReturn("ora").when(yamlParser).getValueAsString();
		assertEquals(TargetType.SUN_SOLARIS, new TargetTypeDeserializer().deserialize(yamlParser, null));

		doReturn("solaris").when(yamlParser).getValueAsString();
		assertEquals(TargetType.SUN_SOLARIS, new TargetTypeDeserializer().deserialize(yamlParser, null));

		doReturn("SUN_SOLARIS").when(yamlParser).getValueAsString();
		assertEquals(TargetType.SUN_SOLARIS, new TargetTypeDeserializer().deserialize(yamlParser, null));

	}

	@Test
	void testVms() throws IOException {
		doReturn("vms").when(yamlParser).getValueAsString();
		assertEquals(TargetType.HP_OPEN_VMS, new TargetTypeDeserializer().deserialize(yamlParser, null));

		doReturn("HP_OPEN_VMS").when(yamlParser).getValueAsString();
		assertEquals(TargetType.HP_OPEN_VMS, new TargetTypeDeserializer().deserialize(yamlParser, null));

		doReturn("open vms").when(yamlParser).getValueAsString();
		assertEquals(TargetType.HP_OPEN_VMS, new TargetTypeDeserializer().deserialize(yamlParser, null));
	}
}
