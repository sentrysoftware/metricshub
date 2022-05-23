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

import com.sentrysoftware.matrix.engine.host.HostType;

@ExtendWith(MockitoExtension.class)
class HostTypeDeserializerTest {

	@Mock
	private YAMLParser yamlParser;

	@Test
	void testNull() throws IOException {

		{
			assertNull(new HostTypeDeserializer().deserialize(null, null));
		}

		{
			doReturn(null).when(yamlParser).getValueAsString();
			assertNull(new HostTypeDeserializer().deserialize(yamlParser, null));
		}

	}

	@Test
	void testBadValue() throws IOException {
		{
			doReturn("unknown").when(yamlParser).getValueAsString();
			assertThrows(IOException.class, () -> new HostTypeDeserializer().deserialize(yamlParser, null));
		}

		{
			doReturn("").when(yamlParser).getValueAsString();
			assertThrows(IOException.class, () -> new HostTypeDeserializer().deserialize(yamlParser, null));
		}

	}

	@Test
	void testLinux() throws IOException {
		doReturn("linux").when(yamlParser).getValueAsString();
		assertEquals(HostType.LINUX, new HostTypeDeserializer().deserialize(yamlParser, null));

		doReturn("lin").when(yamlParser).getValueAsString();
		assertEquals(HostType.LINUX, new HostTypeDeserializer().deserialize(yamlParser, null));

		doReturn("linx").when(yamlParser).getValueAsString();
		assertEquals(HostType.LINUX, new HostTypeDeserializer().deserialize(yamlParser, null));

	}

	@Test
	void testWindows() throws IOException {
		doReturn("win").when(yamlParser).getValueAsString();
		assertEquals(HostType.MS_WINDOWS, new HostTypeDeserializer().deserialize(yamlParser, null));

		doReturn("microsoft win").when(yamlParser).getValueAsString();
		assertEquals(HostType.MS_WINDOWS, new HostTypeDeserializer().deserialize(yamlParser, null));

		doReturn("ms win").when(yamlParser).getValueAsString();
		assertEquals(HostType.MS_WINDOWS, new HostTypeDeserializer().deserialize(yamlParser, null));

		doReturn("ms_windows").when(yamlParser).getValueAsString();
		assertEquals(HostType.MS_WINDOWS, new HostTypeDeserializer().deserialize(yamlParser, null));
	}

	@Test
	void testOob() throws IOException {
		doReturn("oob").when(yamlParser).getValueAsString();
		assertEquals(HostType.MGMT_CARD_BLADE_ESXI, new HostTypeDeserializer().deserialize(yamlParser, null));

		doReturn("esx").when(yamlParser).getValueAsString();
		assertEquals(HostType.MGMT_CARD_BLADE_ESXI, new HostTypeDeserializer().deserialize(yamlParser, null));

		doReturn("blade").when(yamlParser).getValueAsString();
		assertEquals(HostType.MGMT_CARD_BLADE_ESXI, new HostTypeDeserializer().deserialize(yamlParser, null));

		doReturn("vmware").when(yamlParser).getValueAsString();
		assertEquals(HostType.MGMT_CARD_BLADE_ESXI, new HostTypeDeserializer().deserialize(yamlParser, null));

		doReturn("mgmt").when(yamlParser).getValueAsString();
		assertEquals(HostType.MGMT_CARD_BLADE_ESXI, new HostTypeDeserializer().deserialize(yamlParser, null));

		doReturn("management").when(yamlParser).getValueAsString();
		assertEquals(HostType.MGMT_CARD_BLADE_ESXI, new HostTypeDeserializer().deserialize(yamlParser, null));

		doReturn("MGMT_CARD_BLADE_ESXI").when(yamlParser).getValueAsString();
		assertEquals(HostType.MGMT_CARD_BLADE_ESXI, new HostTypeDeserializer().deserialize(yamlParser, null));
	}

	@Test
	void testNetwork() throws IOException {

		doReturn("network").when(yamlParser).getValueAsString();
		assertEquals(HostType.NETWORK_SWITCH, new HostTypeDeserializer().deserialize(yamlParser, null));

		doReturn("net").when(yamlParser).getValueAsString();
		assertEquals(HostType.NETWORK_SWITCH, new HostTypeDeserializer().deserialize(yamlParser, null));

		doReturn("switch").when(yamlParser).getValueAsString();
		assertEquals(HostType.NETWORK_SWITCH, new HostTypeDeserializer().deserialize(yamlParser, null));

		doReturn("NETWORK_SWITCH").when(yamlParser).getValueAsString();
		assertEquals(HostType.NETWORK_SWITCH, new HostTypeDeserializer().deserialize(yamlParser, null));

	}

	@Test
	void testStorage() throws IOException {

		doReturn("storage").when(yamlParser).getValueAsString();
		assertEquals(HostType.STORAGE, new HostTypeDeserializer().deserialize(yamlParser, null));

		doReturn("sto").when(yamlParser).getValueAsString();
		assertEquals(HostType.STORAGE, new HostTypeDeserializer().deserialize(yamlParser, null));

		doReturn("san").when(yamlParser).getValueAsString();
		assertEquals(HostType.STORAGE, new HostTypeDeserializer().deserialize(yamlParser, null));

		doReturn("STORAGE").when(yamlParser).getValueAsString();
		assertEquals(HostType.STORAGE, new HostTypeDeserializer().deserialize(yamlParser, null));

	}

	@Test
	void testTru64() throws IOException {

		doReturn("tru64").when(yamlParser).getValueAsString();
		assertEquals(HostType.HP_TRU64_UNIX, new HostTypeDeserializer().deserialize(yamlParser, null));

		doReturn("osf").when(yamlParser).getValueAsString();
		assertEquals(HostType.HP_TRU64_UNIX, new HostTypeDeserializer().deserialize(yamlParser, null));

		doReturn("HP_TRU64_UNIX").when(yamlParser).getValueAsString();
		assertEquals(HostType.HP_TRU64_UNIX, new HostTypeDeserializer().deserialize(yamlParser, null));

	}

	@Test
	void testHpUx() throws IOException {

		doReturn("hpux").when(yamlParser).getValueAsString();
		assertEquals(HostType.HP_UX, new HostTypeDeserializer().deserialize(yamlParser, null));

		doReturn("hp ux").when(yamlParser).getValueAsString();
		assertEquals(HostType.HP_UX, new HostTypeDeserializer().deserialize(yamlParser, null));

		doReturn("HP_UX").when(yamlParser).getValueAsString();
		assertEquals(HostType.HP_UX, new HostTypeDeserializer().deserialize(yamlParser, null));

	}

	@Test
	void testAix() throws IOException {

		doReturn("aix").when(yamlParser).getValueAsString();
		assertEquals(HostType.IBM_AIX, new HostTypeDeserializer().deserialize(yamlParser, null));

		doReturn("rs6000").when(yamlParser).getValueAsString();
		assertEquals(HostType.IBM_AIX, new HostTypeDeserializer().deserialize(yamlParser, null));

		doReturn("IBM_AIX").when(yamlParser).getValueAsString();
		assertEquals(HostType.IBM_AIX, new HostTypeDeserializer().deserialize(yamlParser, null));

	}

	@Test
	void testSun() throws IOException {

		doReturn("sun").when(yamlParser).getValueAsString();
		assertEquals(HostType.SUN_SOLARIS, new HostTypeDeserializer().deserialize(yamlParser, null));

		doReturn("oracle").when(yamlParser).getValueAsString();
		assertEquals(HostType.SUN_SOLARIS, new HostTypeDeserializer().deserialize(yamlParser, null));

		doReturn("ora").when(yamlParser).getValueAsString();
		assertEquals(HostType.SUN_SOLARIS, new HostTypeDeserializer().deserialize(yamlParser, null));

		doReturn("solaris").when(yamlParser).getValueAsString();
		assertEquals(HostType.SUN_SOLARIS, new HostTypeDeserializer().deserialize(yamlParser, null));

		doReturn("SUN_SOLARIS").when(yamlParser).getValueAsString();
		assertEquals(HostType.SUN_SOLARIS, new HostTypeDeserializer().deserialize(yamlParser, null));

	}

	@Test
	void testVms() throws IOException {
		doReturn("vms").when(yamlParser).getValueAsString();
		assertEquals(HostType.HP_OPEN_VMS, new HostTypeDeserializer().deserialize(yamlParser, null));

		doReturn("HP_OPEN_VMS").when(yamlParser).getValueAsString();
		assertEquals(HostType.HP_OPEN_VMS, new HostTypeDeserializer().deserialize(yamlParser, null));

		doReturn("open vms").when(yamlParser).getValueAsString();
		assertEquals(HostType.HP_OPEN_VMS, new HostTypeDeserializer().deserialize(yamlParser, null));
	}
}
