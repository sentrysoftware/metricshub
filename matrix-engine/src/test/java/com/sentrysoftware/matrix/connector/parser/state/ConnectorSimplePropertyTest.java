package com.sentrysoftware.matrix.connector.parser.state;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.connector.parser.state.ConnectorSimpleProperty.NoAutoDetectionProcessor;
import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.OSType;
import com.sentrysoftware.matrix.connector.parser.state.ConnectorSimpleProperty.AppliesToOSProcessor;
import com.sentrysoftware.matrix.connector.parser.state.ConnectorSimpleProperty.CommentsProcessor;
import com.sentrysoftware.matrix.connector.parser.state.ConnectorSimpleProperty.DisplayNameProcessor;
import com.sentrysoftware.matrix.connector.parser.state.ConnectorSimpleProperty.LocalSupportProcessor;
import com.sentrysoftware.matrix.connector.parser.state.ConnectorSimpleProperty.ReliesOnProcessor;
import com.sentrysoftware.matrix.connector.parser.state.ConnectorSimpleProperty.RemoteSupportProcessor;
import com.sentrysoftware.matrix.connector.parser.state.ConnectorSimpleProperty.SupersedesProcessor;
import com.sentrysoftware.matrix.connector.parser.state.ConnectorSimpleProperty.TypicalPlatformProcessor;
import com.sentrysoftware.matrix.connector.parser.state.ConnectorSimpleProperty.VersionProcessor;

class ConnectorSimplePropertyTest {
	
	private static final String DISPLAY_NAME_KEY = "hdf.DisplayName";
	private static final String SUPERSEDES_KEY = "hdf.Supersedes";
	private static final String TYPICAL_PLATFORM_KEY = "hdf.TypicalPlatform";
	private static final String RELIES_ON_KEY = "hdf.ReliesOn";
	private static final String VERSION_KEY = "hdf.Version";
	private static final String REMOTE_SUPPORT_KEY = "hdf.RemoteSupport";
	private static final String LOCAL_SUPPORT_KEY = "hdf.LocalSupport";
	private static final String APPLIES_TO_OS_KEY = "hdf.AppliesToOS";
	private static final String COMMENTS_KEY = "hdf.comments";
	private static final String NO_AUTODETECTION_KEY = "hdf.NoAutoDetection";

	private static final String DISPLAY_NAME_VALUE = "Dell OpenManage Server Administrator"; 
	private static final String SUPERSEDES_VALUE = "MS_HW_IpmiTool.hdf,MS_HW_VMwareESX4i.hdf";
	private static final String SUPERSEDES_VALUE_SPACED = " MS_HW_IpmiTool.hdf , MS_HW_VMwareESX4i.hdf ";
	private static final String TYPICAL_PLATFORM_VALUE = "Dell PowerEdge";
	private static final String RELIES_ON_VALUE = "Dell OpenManage Server Administrator";
	private static final String VERSION_VALUE = "1.0";
	private static final String REMOTE_SUPPORT_VALUE_TRUE = "true";
	private static final String REMOTE_SUPPORT_VALUE_FALSE = "false";
	private static final String LOCAL_SUPPORT_VALUE_TRUE = "true";
	private static final String LOCAL_SUPPORT_VALUE_FALSE = "false";
	private static final String APPLIES_TO_OS_VALUE = "Linux,NT";
	private static final String APPLIES_TO_OS_VALUE_SPACED = " Linux , NT ";
	private static final String COMMENTS_VALUE = "This the description of the Connector";
	private static final String FOO = "FOO";

	private static final Set<String> SUPERSEDES_VALUE_RESULT = new HashSet<>(
			Arrays.asList("MS_HW_IpmiTool.hdf","MS_HW_VMwareESX4i.hdf")
		);
	private static final Set<OSType> APPLIES_TO_OS_VALUE_RESULT = new HashSet<>(
			Arrays.asList(OSType.LINUX,OSType.NT)
		);
	
	private static final String SPACE = " ";
	
	// DisplayName tests
	
	@Test
	void testDisplayNameProcessorDetectKeyNull() {
		DisplayNameProcessor displayNameProcessor = new DisplayNameProcessor();
		String key = null;
		String value = DISPLAY_NAME_VALUE;
		Connector connector = new Connector();
		assertFalse(displayNameProcessor.detect(key, value, connector));
	}

	@Test
	void testDisplayNameProcessorDetectKeyEmpty() {
		DisplayNameProcessor displayNameProcessor = new DisplayNameProcessor();
		String key = "";
		String value = DISPLAY_NAME_VALUE;
		Connector connector = new Connector();
		assertFalse(displayNameProcessor.detect(key, value, connector));
	}

	@Test
	void testDisplayNameProcessorDetectValueNull() {
		DisplayNameProcessor displayNameProcessor = new DisplayNameProcessor();
		String key = DISPLAY_NAME_KEY;
		String value = null;
		Connector connector = new Connector();
		assertFalse(displayNameProcessor.detect(key, value, connector));
	}

	@Test
	void testDisplayNameProcessorDetectValueEmpty() {
		DisplayNameProcessor displayNameProcessor = new DisplayNameProcessor();
		String key = DISPLAY_NAME_KEY;
		String value = "";
		Connector connector = new Connector();
		assertTrue(displayNameProcessor.detect(key, value, connector));
	}

	@Test
	void testDisplayNameProcessorDetectConnectorNull() {
		DisplayNameProcessor displayNameProcessor = new DisplayNameProcessor();
		String key = DISPLAY_NAME_KEY;
		String value = DISPLAY_NAME_VALUE;
		Connector connector = null;
		assertTrue(displayNameProcessor.detect(key, value, connector));
	}

	@Test
	void testDisplayNameProcessorDetectUppercaseOK() {
		DisplayNameProcessor displayNameProcessor = new DisplayNameProcessor();
		String key = DISPLAY_NAME_KEY.toUpperCase();
		String value = DISPLAY_NAME_VALUE;
		Connector connector = new Connector();
		assertTrue(displayNameProcessor.detect(key, value, connector));
	}

	@Test
	void testDisplayNameProcessorDetectLowercaseOK() {
		DisplayNameProcessor displayNameProcessor = new DisplayNameProcessor();
		String key = DISPLAY_NAME_KEY.toLowerCase();
		String value = DISPLAY_NAME_VALUE;
		Connector connector = new Connector();
		assertTrue(displayNameProcessor.detect(key, value, connector));
	}

	@Test
	void testDisplayNameProcessorDetectOK() {
		DisplayNameProcessor displayNameProcessor = new DisplayNameProcessor();
		String key = DISPLAY_NAME_KEY;
		String value = DISPLAY_NAME_VALUE;
		Connector connector = new Connector();
		assertTrue(displayNameProcessor.detect(key, value, connector));
	}
	
	@Test
	void testDisplayNameProcessorParseConnectorNull() {
		DisplayNameProcessor displayNameProcessor = new DisplayNameProcessor();
		String key = DISPLAY_NAME_KEY;
		String value = DISPLAY_NAME_VALUE;
		Connector connector = null;
		displayNameProcessor.parse(key, value, connector);
		assertNull(connector);
	}
	
	@Test
	void testDisplayNameProcessorParseValueNull() {
		DisplayNameProcessor displayNameProcessor = new DisplayNameProcessor();
		String key = DISPLAY_NAME_KEY;
		String value = null;
		Connector connector = new Connector();
		displayNameProcessor.parse(key, value, connector);
		assertNull(connector.getDisplayName());
	}
	
	@Test
	void testDisplayNameProcessorParseTrimOK() {
		DisplayNameProcessor displayNameProcessor = new DisplayNameProcessor();
		String key = DISPLAY_NAME_KEY;
		String value = SPACE.concat(DISPLAY_NAME_VALUE).concat(SPACE);
		Connector connector = new Connector();
		displayNameProcessor.parse(key, value, connector);
		assertEquals(DISPLAY_NAME_VALUE, connector.getDisplayName());
	}
	
	@Test
	void testDisplayNameProcessorParseOK() {
		DisplayNameProcessor displayNameProcessor = new DisplayNameProcessor();
		String key = DISPLAY_NAME_KEY;
		String value = DISPLAY_NAME_VALUE;
		Connector connector = new Connector();
		displayNameProcessor.parse(key, value, connector);
		assertEquals(DISPLAY_NAME_VALUE, connector.getDisplayName());
	}
	
	// Supersedes tests
	
	@Test
	void testSupersedesProcessorDetectKeyNull() {
		SupersedesProcessor supersedesProcessor = new SupersedesProcessor();
		String key = null;
		String value = SUPERSEDES_VALUE;
		Connector connector = new Connector();
		assertFalse(supersedesProcessor.detect(key, value, connector));
	}

	@Test
	void testSupersedesProcessorDetectKeyEmpty() {
		SupersedesProcessor supersedesProcessor = new SupersedesProcessor();
		String key = "";
		String value = SUPERSEDES_VALUE;
		Connector connector = new Connector();
		assertFalse(supersedesProcessor.detect(key, value, connector));
	}

	@Test
	void testSupersedesProcessorDetectValueNull() {
		SupersedesProcessor supersedesProcessor = new SupersedesProcessor();
		String key = SUPERSEDES_KEY;
		String value = null;
		Connector connector = new Connector();
		assertFalse(supersedesProcessor.detect(key, value, connector));
	}

	@Test
	void testSupersedesProcessorDetectValueEmpty() {
		SupersedesProcessor supersedesProcessor = new SupersedesProcessor();
		String key = SUPERSEDES_KEY;
		String value = "";
		Connector connector = new Connector();
		assertTrue(supersedesProcessor.detect(key, value, connector));
	}

	@Test
	void testSupersedesProcessorDetectConnectorNull() {
		SupersedesProcessor supersedesProcessor = new SupersedesProcessor();
		String key = SUPERSEDES_KEY;
		String value = SUPERSEDES_VALUE;
		Connector connector = null;
		assertTrue(supersedesProcessor.detect(key, value, connector));
	}

	@Test
	void testSupersedesProcessorDetectUppercaseOK() {
		SupersedesProcessor supersedesProcessor = new SupersedesProcessor();
		String key = SUPERSEDES_KEY.toUpperCase();
		String value = SUPERSEDES_VALUE;
		Connector connector = new Connector();
		assertTrue(supersedesProcessor.detect(key, value, connector));
	}

	@Test
	void testSupersedesProcessorDetectLowercaseOK() {
		SupersedesProcessor supersedesProcessor = new SupersedesProcessor();
		String key = SUPERSEDES_KEY.toLowerCase();
		String value = SUPERSEDES_VALUE;
		Connector connector = new Connector();
		assertTrue(supersedesProcessor.detect(key, value, connector));
	}

	@Test
	void testSupersedesProcessorDetectOK() {
		SupersedesProcessor supersedesProcessor = new SupersedesProcessor();
		String key = SUPERSEDES_KEY;
		String value = SUPERSEDES_VALUE;
		Connector connector = new Connector();
		assertTrue(supersedesProcessor.detect(key, value, connector));
	}
	
	@Test
	void testSupersedesProcessorParseConnectorNull() {
		SupersedesProcessor supersedesProcessor = new SupersedesProcessor();
		String key = SUPERSEDES_KEY;
		String value = SUPERSEDES_VALUE;
		Connector connector = null;
		supersedesProcessor.parse(key, value, connector);
		assertNull(connector);
	}
	
	@Test
	void testSupersedesProcessorParseValueNull() {
		SupersedesProcessor supersedesProcessor = new SupersedesProcessor();
		String key = SUPERSEDES_KEY;
		String value = null;
		Connector connector = new Connector();
		supersedesProcessor.parse(key, value, connector);
		assertEquals(connector.getSupersedes(), new HashSet<>());
	}
	
	@Test
	void testSupersedesProcessorParseTrimOK() {
		SupersedesProcessor supersedesProcessor = new SupersedesProcessor();
		String key = SUPERSEDES_KEY;
		String value = SUPERSEDES_VALUE_SPACED;
		Connector connector = new Connector();
		supersedesProcessor.parse(key, value, connector);
		assertEquals(SUPERSEDES_VALUE_RESULT, connector.getSupersedes());
	}
	
	@Test
	void testSupersedesProcessorParseOK() {
		SupersedesProcessor supersedesProcessor = new SupersedesProcessor();
		String key = SUPERSEDES_KEY;
		String value = SUPERSEDES_VALUE;
		Connector connector = new Connector();
		supersedesProcessor.parse(key, value, connector);
		assertEquals(SUPERSEDES_VALUE_RESULT, connector.getSupersedes());
	}
	
	// AppliesToOS tests
	
	@Test
	void testAppliesToOSProcessorDetectKeyNull() {
		AppliesToOSProcessor appliesToOSProcessor = new AppliesToOSProcessor();
		String key = null;
		String value = APPLIES_TO_OS_VALUE;
		Connector connector = new Connector();
		assertFalse(appliesToOSProcessor.detect(key, value, connector));
	}

	@Test
	void testAppliesToOSProcessorDetectKeyEmpty() {
		AppliesToOSProcessor appliesToOSProcessor = new AppliesToOSProcessor();
		String key = "";
		String value =APPLIES_TO_OS_VALUE;
		Connector connector = new Connector();
		assertFalse(appliesToOSProcessor.detect(key, value, connector));
	}

	@Test
	void testAppliesToOSProcessorDetectValueNull() {
		AppliesToOSProcessor appliesToOSProcessor = new AppliesToOSProcessor();
		String key = APPLIES_TO_OS_KEY;
		String value = null;
		Connector connector = new Connector();
		assertFalse(appliesToOSProcessor.detect(key, value, connector));
	}

	@Test
	void testAppliesToOSProcessorDetectValueEmpty() {
		AppliesToOSProcessor appliesToOSProcessor = new AppliesToOSProcessor();
		String key = APPLIES_TO_OS_KEY;
		String value = "";
		Connector connector = new Connector();
		assertTrue(appliesToOSProcessor.detect(key, value, connector));
	}

	@Test
	void testAppliesToOSProcessorDetectConnectorNull() {
		AppliesToOSProcessor appliesToOSProcessor = new AppliesToOSProcessor();
		String key = APPLIES_TO_OS_KEY;
		String value = APPLIES_TO_OS_VALUE;
		Connector connector = null;
		assertTrue(appliesToOSProcessor.detect(key, value, connector));
	}

	@Test
	void testAppliesToOSProcessorDetectUppercaseOK() {
		AppliesToOSProcessor appliesToOSProcessor = new AppliesToOSProcessor();
		String key = APPLIES_TO_OS_KEY.toUpperCase();
		String value = APPLIES_TO_OS_VALUE;
		Connector connector = new Connector();
		assertTrue(appliesToOSProcessor.detect(key, value, connector));
	}

	@Test
	void testAppliesToOSProcessorDetectLowercaseOK() {
		AppliesToOSProcessor appliesToOSProcessor = new AppliesToOSProcessor();
		String key = APPLIES_TO_OS_KEY.toLowerCase();
		String value = APPLIES_TO_OS_VALUE;
		Connector connector = new Connector();
		assertTrue(appliesToOSProcessor.detect(key, value, connector));
	}

	@Test
	void testAppliesToOSProcessorDetectOK() {
		AppliesToOSProcessor appliesToOSProcessor = new AppliesToOSProcessor();
		String key = APPLIES_TO_OS_KEY;
		String value = APPLIES_TO_OS_VALUE;
		Connector connector = new Connector();
		assertTrue(appliesToOSProcessor.detect(key, value, connector));
	}
	
	@Test
	void testAppliesToOSProcessorParseConnectorNull() {
		AppliesToOSProcessor appliesToOSProcessor = new AppliesToOSProcessor();
		String key = APPLIES_TO_OS_KEY;
		String value = APPLIES_TO_OS_VALUE;
		Connector connector = null;
		appliesToOSProcessor.parse(key, value, connector);
		assertNull(connector);
	}
	
	@Test
	void testAppliesToOSProcessorParseValueNull() {
		AppliesToOSProcessor appliesToOSProcessor = new AppliesToOSProcessor();
		String key = APPLIES_TO_OS_KEY;
		String value = null;
		Connector connector = new Connector();
		appliesToOSProcessor.parse(key, value, connector);
		assertEquals(connector.getAppliesToOS(), new HashSet<>());
	}
	
	@Test
	void testAppliesToOSProcessorParseTrimOK() {
		AppliesToOSProcessor appliesToOSProcessor = new AppliesToOSProcessor();
		String key = APPLIES_TO_OS_KEY;
		String value = APPLIES_TO_OS_VALUE_SPACED;
		Connector connector = new Connector();
		appliesToOSProcessor.parse(key, value, connector);
		assertEquals(APPLIES_TO_OS_VALUE_RESULT, connector.getAppliesToOS());
	}
	
	@Test
	void testAppliesToOSProcessorParseOK() {
		AppliesToOSProcessor appliesToOSProcessor = new AppliesToOSProcessor();
		String key = APPLIES_TO_OS_KEY;
		String value = APPLIES_TO_OS_VALUE;
		Connector connector = new Connector();
		appliesToOSProcessor.parse(key, value, connector);
		assertEquals(APPLIES_TO_OS_VALUE_RESULT, connector.getAppliesToOS());
	}
	
	// LocalSupport tests
	
	@Test
	void testLocalSupportProcessorDetectKeyNull() {
		LocalSupportProcessor localSupportProcessor = new LocalSupportProcessor();
		String key = null;
		String value = LOCAL_SUPPORT_VALUE_TRUE;
		Connector connector = new Connector();
		assertFalse(localSupportProcessor.detect(key, value, connector));
	}

	@Test
	void testLocalSupportProcessorDetectKeyEmpty() {
		LocalSupportProcessor localSupportProcessor = new LocalSupportProcessor();
		String key = "";
		String value = LOCAL_SUPPORT_VALUE_TRUE;
		Connector connector = new Connector();
		assertFalse(localSupportProcessor.detect(key, value, connector));
	}

	@Test
	void testLocalSupportProcessorDetectValueNull() {
		LocalSupportProcessor localSupportProcessor = new LocalSupportProcessor();
		String key = LOCAL_SUPPORT_KEY;
		String value = null;
		Connector connector = new Connector();
		assertFalse(localSupportProcessor.detect(key, value, connector));
	}

	@Test
	void testLocalSupportProcessorDetectValueEmpty() {
		LocalSupportProcessor localSupportProcessor = new LocalSupportProcessor();
		String key = LOCAL_SUPPORT_KEY;
		String value = "";
		Connector connector = new Connector();
		assertTrue(localSupportProcessor.detect(key, value, connector));
	}

	@Test
	void testLocalSupportProcessorDetectConnectorNull() {
		LocalSupportProcessor localSupportProcessor = new LocalSupportProcessor();
		String key = LOCAL_SUPPORT_KEY;
		String value = LOCAL_SUPPORT_VALUE_TRUE;
		Connector connector = null;
		assertTrue(localSupportProcessor.detect(key, value, connector));
	}

	@Test
	void testLocalSupportProcessorDetectUppercaseOK() {
		LocalSupportProcessor localSupportProcessor = new LocalSupportProcessor();
		String key = LOCAL_SUPPORT_KEY.toUpperCase();
		String value = LOCAL_SUPPORT_VALUE_TRUE;
		Connector connector = new Connector();
		assertTrue(localSupportProcessor.detect(key, value, connector));
	}

	@Test
	void testLocalSupportProcessorDetectLowercaseOK() {
		LocalSupportProcessor localSupportProcessor = new LocalSupportProcessor();
		String key = LOCAL_SUPPORT_KEY.toLowerCase();
		String value = LOCAL_SUPPORT_VALUE_TRUE;
		Connector connector = new Connector();
		assertTrue(localSupportProcessor.detect(key, value, connector));
	}

	@Test
	void testLocalSupportProcessorDetectOK() {
		LocalSupportProcessor localSupportProcessor = new LocalSupportProcessor();
		String key = LOCAL_SUPPORT_KEY;
		String value = LOCAL_SUPPORT_VALUE_TRUE;
		Connector connector = new Connector();
		assertTrue(localSupportProcessor.detect(key, value, connector));
	}
	
	@Test
	void testLocalSupportProcessorParseConnectorNull() {
		LocalSupportProcessor localSupportProcessor = new LocalSupportProcessor();
		String key = LOCAL_SUPPORT_KEY;
		String value = LOCAL_SUPPORT_VALUE_TRUE;
		Connector connector = null;
		localSupportProcessor.parse(key, value, connector);
		assertNull(connector);
	}
	
	@Test
	void testLocalSupportProcessorParseValueNull() {
		LocalSupportProcessor localSupportProcessor = new LocalSupportProcessor();
		String key = LOCAL_SUPPORT_KEY;
		String value = null;
		Connector connector = new Connector();
		localSupportProcessor.parse(key, value, connector);
		assertNull(connector.getLocalSupport());
	}
	
	@Test
	void testLocalSupportProcessorParseTrimOK() {
		LocalSupportProcessor localSupportProcessor = new LocalSupportProcessor();
		String key = LOCAL_SUPPORT_KEY;
		String value = SPACE.concat(LOCAL_SUPPORT_VALUE_TRUE).concat(SPACE);
		Connector connector = new Connector();
		localSupportProcessor.parse(key, value, connector);
		assertTrue(connector.getLocalSupport());
	}
	
	@Test
	void testLocalSupportProcessorParseTrue() {
		LocalSupportProcessor localSupportProcessor = new LocalSupportProcessor();
		String key = LOCAL_SUPPORT_KEY;
		String value = LOCAL_SUPPORT_VALUE_TRUE;
		Connector connector = new Connector();
		localSupportProcessor.parse(key, value, connector);
		assertTrue(connector.getLocalSupport());
	}
	
	@Test
	void testLocalSupportProcessorParseFalse() {
		LocalSupportProcessor localSupportProcessor = new LocalSupportProcessor();
		String key = LOCAL_SUPPORT_KEY;
		String value = LOCAL_SUPPORT_VALUE_FALSE;
		Connector connector = new Connector();
		localSupportProcessor.parse(key, value, connector);
		assertFalse(connector.getLocalSupport());
	}
	
	// RemoteSupport tests
	
	@Test
	void testRemoteSupportProcessorDetectKeyNull() {
		RemoteSupportProcessor remoteSupportProcessor = new RemoteSupportProcessor();
		String key = null;
		String value = REMOTE_SUPPORT_VALUE_TRUE;
		Connector connector = new Connector();
		assertFalse(remoteSupportProcessor.detect(key, value, connector));
	}

	@Test
	void testRemoteSupportProcessorDetectKeyEmpty() {
		RemoteSupportProcessor remoteSupportProcessor = new RemoteSupportProcessor();
		String key = "";
		String value = REMOTE_SUPPORT_VALUE_TRUE;
		Connector connector = new Connector();
		assertFalse(remoteSupportProcessor.detect(key, value, connector));
	}

	@Test
	void testRemoteSupportProcessorDetectValueNull() {
		RemoteSupportProcessor remoteSupportProcessor = new RemoteSupportProcessor();
		String key = REMOTE_SUPPORT_KEY;
		String value = null;
		Connector connector = new Connector();
		assertFalse(remoteSupportProcessor.detect(key, value, connector));
	}

	@Test
	void testRemoteSupportProcessorDetectValueEmpty() {
		RemoteSupportProcessor remoteSupportProcessor = new RemoteSupportProcessor();
		String key = REMOTE_SUPPORT_KEY;
		String value = "";
		Connector connector = new Connector();
		assertTrue(remoteSupportProcessor.detect(key, value, connector));
	}

	@Test
	void testRemoteSupportProcessorDetectConnectorNull() {
		RemoteSupportProcessor remoteSupportProcessor = new RemoteSupportProcessor();
		String key = REMOTE_SUPPORT_KEY;
		String value = REMOTE_SUPPORT_VALUE_TRUE;
		Connector connector = null;
		assertTrue(remoteSupportProcessor.detect(key, value, connector));
	}

	@Test
	void testRemoteSupportProcessorDetectUppercaseOK() {
		RemoteSupportProcessor remoteSupportProcessor = new RemoteSupportProcessor();
		String key = REMOTE_SUPPORT_KEY.toUpperCase();
		String value = REMOTE_SUPPORT_VALUE_TRUE;
		Connector connector = new Connector();
		assertTrue(remoteSupportProcessor.detect(key, value, connector));
	}

	@Test
	void testRemoteSupportProcessorDetectLowercaseOK() {
		RemoteSupportProcessor remoteSupportProcessor = new RemoteSupportProcessor();
		String key = REMOTE_SUPPORT_KEY.toLowerCase();
		String value = REMOTE_SUPPORT_VALUE_TRUE;
		Connector connector = new Connector();
		assertTrue(remoteSupportProcessor.detect(key, value, connector));
	}

	@Test
	void testRemoteSupportProcessorDetectOK() {
		RemoteSupportProcessor remoteSupportProcessor = new RemoteSupportProcessor();
		String key = REMOTE_SUPPORT_KEY;
		String value = REMOTE_SUPPORT_VALUE_TRUE;
		Connector connector = new Connector();
		assertTrue(remoteSupportProcessor.detect(key, value, connector));
	}
	
	@Test
	void testRemoteSupportProcessorParseConnectorNull() {
		RemoteSupportProcessor remoteSupportProcessor = new RemoteSupportProcessor();
		String key = REMOTE_SUPPORT_KEY;
		String value = REMOTE_SUPPORT_VALUE_TRUE;
		Connector connector = null;
		remoteSupportProcessor.parse(key, value, connector);
		assertNull(connector);
	}
	
	@Test
	void testRemoteSupportProcessorParseValueNull() {
		RemoteSupportProcessor remoteSupportProcessor = new RemoteSupportProcessor();
		String key = REMOTE_SUPPORT_KEY;
		String value = null;
		Connector connector = new Connector();
		remoteSupportProcessor.parse(key, value, connector);
		assertNull(connector.getRemoteSupport());
	}
	
	@Test
	void testRemoteSupportProcessorParseTrimOK() {
		RemoteSupportProcessor remoteSupportProcessor = new RemoteSupportProcessor();
		String key = REMOTE_SUPPORT_KEY;
		String value = SPACE.concat(REMOTE_SUPPORT_VALUE_TRUE).concat(SPACE);
		Connector connector = new Connector();
		remoteSupportProcessor.parse(key, value, connector);
		assertTrue(connector.getRemoteSupport());
	}
	
	@Test
	void testRemoteSupportProcessorParseTrue() {
		RemoteSupportProcessor remoteSupportProcessor = new RemoteSupportProcessor();
		String key = REMOTE_SUPPORT_KEY;
		String value = REMOTE_SUPPORT_VALUE_TRUE;
		Connector connector = new Connector();
		remoteSupportProcessor.parse(key, value, connector);
		assertTrue(connector.getRemoteSupport());
	}
	
	@Test
	void testRemoteSupportProcessorParseFalse() {
		RemoteSupportProcessor remoteSupportProcessor = new RemoteSupportProcessor();
		String key = REMOTE_SUPPORT_KEY;
		String value = REMOTE_SUPPORT_VALUE_FALSE;
		Connector connector = new Connector();
		remoteSupportProcessor.parse(key, value, connector);
		assertFalse(connector.getRemoteSupport());
	}
	
	// TypicalPlatform tests
	
	@Test
	void testTypicalPlatformProcessorDetectKeyNull() {
		TypicalPlatformProcessor typicalPlatformProcessor = new TypicalPlatformProcessor();
		String key = null;
		String value = TYPICAL_PLATFORM_VALUE;
		Connector connector = new Connector();
		assertFalse(typicalPlatformProcessor.detect(key, value, connector));
	}

	@Test
	void testTypicalPlatformProcessorDetectKeyEmpty() {
		TypicalPlatformProcessor typicalPlatformProcessor = new TypicalPlatformProcessor();
		String key = "";
		String value = TYPICAL_PLATFORM_VALUE;
		Connector connector = new Connector();
		assertFalse(typicalPlatformProcessor.detect(key, value, connector));
	}

	@Test
	void testTypicalPlatformProcessorDetectValueNull() {
		TypicalPlatformProcessor typicalPlatformProcessor = new TypicalPlatformProcessor();
		String key = TYPICAL_PLATFORM_KEY;
		String value = null;
		Connector connector = new Connector();
		assertFalse(typicalPlatformProcessor.detect(key, value, connector));
	}

	@Test
	void testTypicalPlatformProcessorDetectValueEmpty() {
		TypicalPlatformProcessor typicalPlatformProcessor = new TypicalPlatformProcessor();
		String key = TYPICAL_PLATFORM_KEY;
		String value = "";
		Connector connector = new Connector();
		assertTrue(typicalPlatformProcessor.detect(key, value, connector));
	}

	@Test
	void testTypicalPlatformProcessorDetectConnectorNull() {
		TypicalPlatformProcessor typicalPlatformProcessor = new TypicalPlatformProcessor();
		String key = TYPICAL_PLATFORM_KEY;
		String value = TYPICAL_PLATFORM_VALUE;
		Connector connector = null;
		assertTrue(typicalPlatformProcessor.detect(key, value, connector));
	}

	@Test
	void testTypicalPlatformProcessorDetectUppercaseOK() {
		TypicalPlatformProcessor typicalPlatformProcessor = new TypicalPlatformProcessor();
		String key = TYPICAL_PLATFORM_KEY.toUpperCase();
		String value = TYPICAL_PLATFORM_VALUE;
		Connector connector = new Connector();
		assertTrue(typicalPlatformProcessor.detect(key, value, connector));
	}

	@Test
	void testTypicalPlatformProcessorDetectLowercaseOK() {
		TypicalPlatformProcessor typicalPlatformProcessor = new TypicalPlatformProcessor();
		String key = TYPICAL_PLATFORM_KEY.toLowerCase();
		String value = TYPICAL_PLATFORM_VALUE;
		Connector connector = new Connector();
		assertTrue(typicalPlatformProcessor.detect(key, value, connector));
	}

	@Test
	void testTypicalPlatformProcessorDetectOK() {
		TypicalPlatformProcessor typicalPlatformProcessor = new TypicalPlatformProcessor();
		String key = TYPICAL_PLATFORM_KEY;
		String value = TYPICAL_PLATFORM_VALUE;
		Connector connector = new Connector();
		assertTrue(typicalPlatformProcessor.detect(key, value, connector));
	}
	
	@Test
	void testTypicalPlatformProcessorParseConnectorNull() {
		TypicalPlatformProcessor typicalPlatformProcessor = new TypicalPlatformProcessor();
		String key = TYPICAL_PLATFORM_KEY;
		String value = TYPICAL_PLATFORM_VALUE;
		Connector connector = null;
		typicalPlatformProcessor.parse(key, value, connector);
		assertNull(connector);
	}
	
	@Test
	void testTypicalPlatformProcessorParseValueNull() {
		TypicalPlatformProcessor typicalPlatformProcessor = new TypicalPlatformProcessor();
		String key = TYPICAL_PLATFORM_KEY;
		String value = null;
		Connector connector = new Connector();
		typicalPlatformProcessor.parse(key, value, connector);
		assertNull(connector.getTypicalPlatform());
	}
	
	@Test
	void testTypicalPlatformProcessorParseTrimOK() {
		TypicalPlatformProcessor typicalPlatformProcessor = new TypicalPlatformProcessor();
		String key = TYPICAL_PLATFORM_KEY;
		String value = SPACE.concat(TYPICAL_PLATFORM_VALUE).concat(SPACE);
		Connector connector = new Connector();
		typicalPlatformProcessor.parse(key, value, connector);
		assertEquals(TYPICAL_PLATFORM_VALUE, connector.getTypicalPlatform());
	}
	
	@Test
	void testTypicalPlatformProcessorParseOK() {
		TypicalPlatformProcessor typicalPlatformProcessor = new TypicalPlatformProcessor();
		String key = TYPICAL_PLATFORM_KEY;
		String value = TYPICAL_PLATFORM_VALUE;
		Connector connector = new Connector();
		typicalPlatformProcessor.parse(key, value, connector);
		assertEquals(TYPICAL_PLATFORM_VALUE, connector.getTypicalPlatform());
	}
	
	// ReliesOn tests
	
	@Test
	void testReliesOnProcessorDetectKeyNull() {
		ReliesOnProcessor reliesOnProcessor = new ReliesOnProcessor();
		String key = null;
		String value = RELIES_ON_VALUE;
		Connector connector = new Connector();
		assertFalse(reliesOnProcessor.detect(key, value, connector));
	}

	@Test
	void testReliesOnProcessorDetectKeyEmpty() {
		ReliesOnProcessor reliesOnProcessor = new ReliesOnProcessor();
		String key = "";
		String value = RELIES_ON_VALUE;
		Connector connector = new Connector();
		assertFalse(reliesOnProcessor.detect(key, value, connector));
	}

	@Test
	void testReliesOnProcessorDetectValueNull() {
		ReliesOnProcessor reliesOnProcessor = new ReliesOnProcessor();
		String key = RELIES_ON_KEY;
		String value = null;
		Connector connector = new Connector();
		assertFalse(reliesOnProcessor.detect(key, value, connector));
	}

	@Test
	void testReliesOnProcessorDetectValueEmpty() {
		ReliesOnProcessor reliesOnProcessor = new ReliesOnProcessor();
		String key = RELIES_ON_KEY;
		String value = "";
		Connector connector = new Connector();
		assertTrue(reliesOnProcessor.detect(key, value, connector));
	}

	@Test
	void testReliesOnProcessorDetectConnectorNull() {
		ReliesOnProcessor reliesOnProcessor = new ReliesOnProcessor();
		String key = RELIES_ON_KEY;
		String value = RELIES_ON_VALUE;
		Connector connector = null;
		assertTrue(reliesOnProcessor.detect(key, value, connector));
	}

	@Test
	void testReliesOnProcessorDetectUppercaseOK() {
		ReliesOnProcessor reliesOnProcessor = new ReliesOnProcessor();
		String key = RELIES_ON_KEY.toUpperCase();
		String value = RELIES_ON_VALUE;
		Connector connector = new Connector();
		assertTrue(reliesOnProcessor.detect(key, value, connector));
	}

	@Test
	void testReliesOnProcessorDetectLowercaseOK() {
		ReliesOnProcessor reliesOnProcessor = new ReliesOnProcessor();
		String key = RELIES_ON_KEY.toLowerCase();
		String value = RELIES_ON_VALUE;
		Connector connector = new Connector();
		assertTrue(reliesOnProcessor.detect(key, value, connector));
	}

	@Test
	void testReliesOnProcessorDetectOK() {
		ReliesOnProcessor reliesOnProcessor = new ReliesOnProcessor();
		String key = RELIES_ON_KEY;
		String value = RELIES_ON_VALUE;
		Connector connector = new Connector();
		assertTrue(reliesOnProcessor.detect(key, value, connector));
	}
	
	@Test
	void testReliesOnProcessorParseConnectorNull() {
		ReliesOnProcessor reliesOnProcessor = new ReliesOnProcessor();
		String key = RELIES_ON_KEY;
		String value = RELIES_ON_VALUE;
		Connector connector = null;
		reliesOnProcessor.parse(key, value, connector);
		assertNull(connector);
	}
	
	@Test
	void testReliesOnProcessorParseValueNull() {
		ReliesOnProcessor reliesOnProcessor = new ReliesOnProcessor();
		String key = RELIES_ON_KEY;
		String value = null;
		Connector connector = new Connector();
		reliesOnProcessor.parse(key, value, connector);
		assertNull(connector.getReliesOn());
	}
	
	@Test
	void testReliesOnProcessorParseTrimOK() {
		ReliesOnProcessor reliesOnProcessor = new ReliesOnProcessor();
		String key = RELIES_ON_KEY;
		String value = SPACE.concat(RELIES_ON_VALUE).concat(SPACE);
		Connector connector = new Connector();
		reliesOnProcessor.parse(key, value, connector);
		assertEquals(RELIES_ON_VALUE, connector.getReliesOn());
	}
	
	@Test
	void testReliesOnProcessorParseOK() {
		ReliesOnProcessor reliesOnProcessor = new ReliesOnProcessor();
		String key = RELIES_ON_KEY;
		String value = RELIES_ON_VALUE;
		Connector connector = new Connector();
		reliesOnProcessor.parse(key, value, connector);
		assertEquals(RELIES_ON_VALUE, connector.getReliesOn());
	}
	
	// Version tests
	
	@Test
	void testVersionProcessorDetectKeyNull() {
		VersionProcessor versionProcessor = new VersionProcessor();
		String key = null;
		String value = VERSION_VALUE;
		Connector connector = new Connector();
		assertFalse(versionProcessor.detect(key, value, connector));
	}

	@Test
	void testVersionProcessorDetectKeyEmpty() {
		VersionProcessor versionProcessor = new VersionProcessor();
		String key = "";
		String value = VERSION_VALUE;
		Connector connector = new Connector();
		assertFalse(versionProcessor.detect(key, value, connector));
	}

	@Test
	void testVersionProcessorDetectValueNull() {
		VersionProcessor versionProcessor = new VersionProcessor();
		String key = VERSION_KEY;
		String value = null;
		Connector connector = new Connector();
		assertFalse(versionProcessor.detect(key, value, connector));
	}

	@Test
	void testVersionProcessorDetectValueEmpty() {
		VersionProcessor versionProcessor = new VersionProcessor();
		String key = VERSION_KEY;
		String value = "";
		Connector connector = new Connector();
		assertTrue(versionProcessor.detect(key, value, connector));
	}

	@Test
	void testVersionProcessorDetectConnectorNull() {
		VersionProcessor versionProcessor = new VersionProcessor();
		String key = VERSION_KEY;
		String value = VERSION_VALUE;
		Connector connector = null;
		assertTrue(versionProcessor.detect(key, value, connector));
	}

	@Test
	void testVersionProcessorDetectUppercaseOK() {
		VersionProcessor versionProcessor = new VersionProcessor();
		String key = VERSION_KEY.toUpperCase();
		String value = VERSION_VALUE;
		Connector connector = new Connector();
		assertTrue(versionProcessor.detect(key, value, connector));
	}

	@Test
	void testVersionProcessorDetectLowercaseOK() {
		VersionProcessor versionProcessor = new VersionProcessor();
		String key = VERSION_KEY.toLowerCase();
		String value = VERSION_VALUE;
		Connector connector = new Connector();
		assertTrue(versionProcessor.detect(key, value, connector));
	}

	@Test
	void testVersionProcessorDetectOK() {
		VersionProcessor versionProcessor = new VersionProcessor();
		String key = VERSION_KEY;
		String value = VERSION_VALUE;
		Connector connector = new Connector();
		assertTrue(versionProcessor.detect(key, value, connector));
	}
	
	@Test
	void testVersionProcessorParseConnectorNull() {
		VersionProcessor versionProcessor = new VersionProcessor();
		String key = VERSION_KEY;
		String value = VERSION_VALUE;
		Connector connector = null;
		versionProcessor.parse(key, value, connector);
		assertNull(connector);
	}
	
	@Test
	void testVersionProcessorParseValueNull() {
		VersionProcessor versionProcessor = new VersionProcessor();
		String key = VERSION_KEY;
		String value = null;
		Connector connector = new Connector();
		versionProcessor.parse(key, value, connector);
		assertNull(connector.getVersion());
	}
	
	@Test
	void testVersionProcessorParseTrimOK() {
		VersionProcessor versionProcessor = new VersionProcessor();
		String key = VERSION_KEY;
		String value = SPACE.concat(VERSION_VALUE).concat(SPACE);
		Connector connector = new Connector();
		versionProcessor.parse(key, value, connector);
		assertEquals(VERSION_VALUE, connector.getVersion());
	}
	
	@Test
	void testVersionProcessorParseOK() {
		VersionProcessor versionProcessor = new VersionProcessor();
		String key = VERSION_KEY;
		String value = VERSION_VALUE;
		Connector connector = new Connector();
		versionProcessor.parse(key, value, connector);
		assertEquals(VERSION_VALUE, connector.getVersion());
	}

	@Test
	void testGetConnectorProperties() {

		assertEquals(
				Stream.of(DisplayNameProcessor.class, TypicalPlatformProcessor.class, ReliesOnProcessor.class, VersionProcessor.class,
						RemoteSupportProcessor.class, LocalSupportProcessor.class, AppliesToOSProcessor.class, SupersedesProcessor.class,
						CommentsProcessor.class, NoAutoDetectionProcessor.class)
						.collect(Collectors.toSet()),
				ConnectorSimpleProperty.getConnectorProperties().stream().map(IConnectorStateParser::getClass).collect(Collectors.toSet()));
	}

	void testCommentsProcessorDetectOK() {
		CommentsProcessor commentsProcessor = new CommentsProcessor();
		Connector connector = new Connector();
		assertTrue(commentsProcessor.detect(COMMENTS_KEY, COMMENTS_VALUE, connector));
	}

	@Test
	void testCommentsProcessorParseConnectorNull() {
		CommentsProcessor commentsProcessor = new CommentsProcessor();
		assertDoesNotThrow(() -> commentsProcessor.parse(COMMENTS_KEY, COMMENTS_VALUE, null));
	}

	@Test
	void testCommentsProcessorParseValueNull() {
		CommentsProcessor commentsProcessor = new CommentsProcessor();
		Connector connector = new Connector();
		commentsProcessor.parse(COMMENTS_KEY, null, connector);
		assertNull(connector.getComments());
	}

	@Test
	void testCommentsProcessorParseTrimOK() {
		CommentsProcessor commentsProcessor = new CommentsProcessor();
		Connector connector = new Connector();
		commentsProcessor.parse(COMMENTS_KEY, SPACE.concat(COMMENTS_VALUE).concat(SPACE), connector);
		assertEquals(COMMENTS_VALUE, connector.getComments());
	}

	@Test
	void testCommentsProcessorParseOK() {
		CommentsProcessor commentsProcessor = new CommentsProcessor();
		Connector connector = new Connector();
		commentsProcessor.parse(COMMENTS_KEY, COMMENTS_VALUE, connector);
		assertEquals(COMMENTS_VALUE, connector.getComments());
	}

	@Test
	void testNoAutoDetectionParse() {

		NoAutoDetectionProcessor noAutoDetectionProcessor = new NoAutoDetectionProcessor();

		// connector is null
		assertDoesNotThrow(() -> noAutoDetectionProcessor.parse(NO_AUTODETECTION_KEY, FOO, null));

		// connector is not null, value is null
		Connector connector = new Connector();
		assertNull(connector.getNoAutoDetection());
		noAutoDetectionProcessor.parse(NO_AUTODETECTION_KEY, null, connector);
		assertFalse(connector.getNoAutoDetection());

		// connector is not null, value is not null, value is neither true nor false
		noAutoDetectionProcessor.parse(NO_AUTODETECTION_KEY, FOO, connector);
		assertFalse(connector.getNoAutoDetection());

		// connector is not null, value is not null, value is false
		noAutoDetectionProcessor.parse(NO_AUTODETECTION_KEY, Boolean.FALSE.toString(), connector);
		assertFalse(connector.getNoAutoDetection());

		// connector is not null, value is not null, value is true
		noAutoDetectionProcessor.parse(NO_AUTODETECTION_KEY, Boolean.TRUE.toString(), connector);
		assertTrue(connector.getNoAutoDetection());
	}
}
