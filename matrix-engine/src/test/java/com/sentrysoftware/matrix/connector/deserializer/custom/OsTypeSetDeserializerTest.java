package com.sentrysoftware.matrix.connector.deserializer.custom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import com.sentrysoftware.matrix.connector.model.common.OsType;

@ExtendWith(MockitoExtension.class)
class OsTypeSetDeserializerTest {

	private static final OsTypeSetDeserializer OS_TYPE_DESERIALIZER = new OsTypeSetDeserializer();

	@Mock
	private YAMLParser yamlParser;

	@Test
	void testNull() throws IOException {

		{
			assertEquals(Collections.emptySet(), OS_TYPE_DESERIALIZER.deserialize(null, null));
		}

		{
			doReturn(false).when(yamlParser).isExpectedStartArrayToken();
			doReturn(null).when(yamlParser).getValueAsString();
			assertEquals(Collections.emptySet(), OS_TYPE_DESERIALIZER.deserialize(yamlParser, null));
		}
		{
			doReturn(true).when(yamlParser).isExpectedStartArrayToken();
			doReturn(null).when(yamlParser).readValueAs(any(TypeReference.class));
			assertEquals(Collections.emptySet(), OS_TYPE_DESERIALIZER.deserialize(yamlParser, null));
		}
		{
			doReturn(true).when(yamlParser).isExpectedStartArrayToken();
			doReturn(Collections.emptySet()).when(yamlParser).readValueAs(any(TypeReference.class));
			assertEquals(Collections.emptySet(), OS_TYPE_DESERIALIZER.deserialize(yamlParser, null));
		}
	}

	@Test
	void testBadValue() throws IOException {
		{
			doReturn(false).when(yamlParser).isExpectedStartArrayToken();
			doReturn("unknown").when(yamlParser).getValueAsString();
			assertThrows(IOException.class, () -> OS_TYPE_DESERIALIZER.deserialize(yamlParser, null));
		}

		{
			doReturn(false).when(yamlParser).isExpectedStartArrayToken();
			doReturn("").when(yamlParser).getValueAsString();
			assertThrows(IOException.class, () -> OS_TYPE_DESERIALIZER.deserialize(yamlParser, null));
		}

		{
			doReturn(true).when(yamlParser).isExpectedStartArrayToken();
			doReturn(Set.of("unknown")).when(yamlParser).readValueAs(any(TypeReference.class));
			assertThrows(IOException.class, () -> OS_TYPE_DESERIALIZER.deserialize(yamlParser, null));
		}
		{
			doReturn(true).when(yamlParser).isExpectedStartArrayToken();
			doReturn(Set.of("")).when(yamlParser).readValueAs(any(TypeReference.class));
			assertThrows(IOException.class, () -> OS_TYPE_DESERIALIZER.deserialize(yamlParser, null));
		}

	}

	@Test
	void testDeserializeArray() throws IOException {
		{
			final Set<String> osTypes = Set.of("linux", "windows", "oob", "network", "storage", "vms", "tru64", "hpux", "aix", "solaris");
			doReturn(true).when(yamlParser).isExpectedStartArrayToken();
			doReturn(osTypes).when(yamlParser).readValueAs(any(TypeReference.class));
			assertEquals(OsType.OS_TYPES, OS_TYPE_DESERIALIZER.deserialize(yamlParser, null));
		}

		{
			doReturn(true).when(yamlParser).isExpectedStartArrayToken();
			doReturn(
				Set.of(
					"linux", "LINUX", "Linux",
					"windows", "Windows", "NT", "Nt", "nt", "WIN", "win",
					"Microsoft Windows", "microsoft	 windows", "MicrosoftWindows",
					"oob", "management card", "out of band", "out-of-band",
					"network", "NETWORK", "Network", "Switch", "SWITCH", "switch",
					"storage", "Storage", "san", "SAN", "library", "array",
					"vms", "OpenVms", "HP  OpenVms", "openvms", "HpOpenVms", "HP Open Vms",
					"tru64", "Tru64", "osf1", "OSF1", "hp tru64 unix", "HPTru64Unix",
					"hpux", "hp-ux", "hp", "HPUX", "HP-UX", "HP",
					"aix", "ibm aix", "rs6000", "ibm-aix", "IBM AIX", "RS6000", "IBM-AIX",
					"solaris", "Solaris", "sunos", "SunOS"
				)
			)
			.when(yamlParser).readValueAs(any(TypeReference.class));
			assertEquals(OsType.OS_TYPES, OS_TYPE_DESERIALIZER.deserialize(yamlParser, null));
		}

		{
			doReturn(false).when(yamlParser).isExpectedStartArrayToken();
			doReturn("linux").when(yamlParser).getValueAsString();
			assertEquals(Set.of(OsType.LINUX), OS_TYPE_DESERIALIZER.deserialize(yamlParser, null));
			doReturn("windows").when(yamlParser).getValueAsString();
			assertEquals(Set.of(OsType.WINDOWS), OS_TYPE_DESERIALIZER.deserialize(yamlParser, null));
			doReturn("oob").when(yamlParser).getValueAsString();
			assertEquals(Set.of(OsType.OOB), OS_TYPE_DESERIALIZER.deserialize(yamlParser, null));
			doReturn("network").when(yamlParser).getValueAsString();
			assertEquals(Set.of(OsType.NETWORK), OS_TYPE_DESERIALIZER.deserialize(yamlParser, null));
			doReturn("storage").when(yamlParser).getValueAsString();
			assertEquals(Set.of(OsType.STORAGE), OS_TYPE_DESERIALIZER.deserialize(yamlParser, null));
			doReturn("vms").when(yamlParser).getValueAsString();
			assertEquals(Set.of(OsType.VMS), OS_TYPE_DESERIALIZER.deserialize(yamlParser, null));
			doReturn("tru64").when(yamlParser).getValueAsString();
			assertEquals(Set.of(OsType.TRU64), OS_TYPE_DESERIALIZER.deserialize(yamlParser, null));
			doReturn("hpux").when(yamlParser).getValueAsString();
			assertEquals(Set.of(OsType.HPUX), OS_TYPE_DESERIALIZER.deserialize(yamlParser, null));
			doReturn("aix").when(yamlParser).getValueAsString();
			assertEquals(Set.of(OsType.AIX), OS_TYPE_DESERIALIZER.deserialize(yamlParser, null));
			doReturn("solaris").when(yamlParser).getValueAsString();
			assertEquals(Set.of(OsType.SOLARIS), OS_TYPE_DESERIALIZER.deserialize(yamlParser, null));
		}

	}

}
