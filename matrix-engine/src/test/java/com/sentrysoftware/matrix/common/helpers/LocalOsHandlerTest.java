package com.sentrysoftware.matrix.common.helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.condition.OS.LINUX;
import static org.junit.jupiter.api.condition.OS.WINDOWS;
import static org.mockito.Mockito.mockStatic;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.mockito.MockedStatic;

import com.sentrysoftware.matrix.common.helpers.LocalOsHandler.FreeBSD;
import com.sentrysoftware.matrix.common.helpers.LocalOsHandler.ILocalOS;
import com.sentrysoftware.matrix.common.helpers.LocalOsHandler.NetBSD;
import com.sentrysoftware.matrix.common.helpers.LocalOsHandler.OpenBSD;

class LocalOsHandlerTest {

	@Test
	void testDetectOSNotFoundlSystemOSName() {
		try (final MockedStatic<LocalOsHandler> mockedLocalOSHandler = mockStatic(LocalOsHandler.class)) {
			mockedLocalOSHandler.when(LocalOsHandler::getSystemOSName).thenReturn(Optional.empty());
			mockedLocalOSHandler.when(LocalOsHandler::detectOS).thenCallRealMethod();

			assertEquals(Optional.empty(), LocalOsHandler.detectOS());
		}
	}

	@Test
	void testDetectOSUnknown() {
		try (final MockedStatic<LocalOsHandler> mockedLocalOSHandler = mockStatic(LocalOsHandler.class)) {
			mockedLocalOSHandler.when(LocalOsHandler::getSystemOSName).thenReturn(Optional.of("OS/400"));
			mockedLocalOSHandler.when(LocalOsHandler::detectOS).thenCallRealMethod();

			assertEquals(Optional.empty(), LocalOsHandler.detectOS());
		}
	}

	@Test
	void testDetectOSWindows() {
		try (final MockedStatic<LocalOsHandler> mockedLocalOSHandler = mockStatic(LocalOsHandler.class)) {
			mockedLocalOSHandler.when(LocalOsHandler::getSystemOSName).thenReturn(Optional.of("Windows 8.1"));
			mockedLocalOSHandler.when(LocalOsHandler::detectOS).thenCallRealMethod();

			final Optional<ILocalOS> maybeOS = LocalOsHandler.detectOS();
			assertNotNull(maybeOS);
			assertTrue(maybeOS.isPresent());

			final ILocalOS os = maybeOS.get();
			assertEquals(LocalOsHandler.WINDOWS, os);
			assertFalse(os.isUnix());
		}
	}

	@Test
	void testDetectOSLinux() {
		try (final MockedStatic<LocalOsHandler> mockedLocalOSHandler = mockStatic(LocalOsHandler.class)) {
			mockedLocalOSHandler.when(LocalOsHandler::getSystemOSName).thenReturn(Optional.of("Linux"));
			mockedLocalOSHandler.when(LocalOsHandler::detectOS).thenCallRealMethod();

			final Optional<ILocalOS> maybeOS = LocalOsHandler.detectOS();
			assertNotNull(maybeOS);
			assertTrue(maybeOS.isPresent());

			final ILocalOS os = maybeOS.get();
			assertEquals(LocalOsHandler.LINUX, os);
			assertTrue(os.isUnix());
		}
	}

	@Test
	void testDetectOSAix() {
		try (final MockedStatic<LocalOsHandler> mockedLocalOSHandler = mockStatic(LocalOsHandler.class)) {
			mockedLocalOSHandler.when(LocalOsHandler::getSystemOSName).thenReturn(Optional.of("AIX"));
			mockedLocalOSHandler.when(LocalOsHandler::detectOS).thenCallRealMethod();

			final Optional<ILocalOS> maybeOS = LocalOsHandler.detectOS();
			assertNotNull(maybeOS);
			assertTrue(maybeOS.isPresent());

			final ILocalOS os = maybeOS.get();
			assertEquals(LocalOsHandler.AIX, os);
			assertTrue(os.isUnix());
		}
	}

	@Test
	void testDetectOSSolaris() {
		try (final MockedStatic<LocalOsHandler> mockedLocalOSHandler = mockStatic(LocalOsHandler.class)) {
			mockedLocalOSHandler.when(LocalOsHandler::getSystemOSName).thenReturn(Optional.of("Solaris"));
			mockedLocalOSHandler.when(LocalOsHandler::detectOS).thenCallRealMethod();

			final Optional<ILocalOS> maybeOS = LocalOsHandler.detectOS();
			assertNotNull(maybeOS);
			assertTrue(maybeOS.isPresent());

			final ILocalOS os = maybeOS.get();
			assertEquals(LocalOsHandler.SOLARIS, os);
			assertTrue(os.isUnix());
		}
	}

	@Test
	void testDetectOSSunOS() {
		try (final MockedStatic<LocalOsHandler> mockedLocalOSHandler = mockStatic(LocalOsHandler.class)) {
			mockedLocalOSHandler.when(LocalOsHandler::getSystemOSName).thenReturn(Optional.of("sunos"));
			mockedLocalOSHandler.when(LocalOsHandler::detectOS).thenCallRealMethod();

			final Optional<ILocalOS> maybeOS = LocalOsHandler.detectOS();
			assertNotNull(maybeOS);
			assertTrue(maybeOS.isPresent());

			final ILocalOS os = maybeOS.get();
			assertEquals(LocalOsHandler.SUN, os);
			assertTrue(os.isUnix());
		}
	}

	@Test
	void testDetectOSHp() {
		try (final MockedStatic<LocalOsHandler> mockedLocalOSHandler = mockStatic(LocalOsHandler.class)) {
			mockedLocalOSHandler.when(LocalOsHandler::getSystemOSName).thenReturn(Optional.of("HP-UX"));
			mockedLocalOSHandler.when(LocalOsHandler::detectOS).thenCallRealMethod();

			final Optional<ILocalOS> maybeOS = LocalOsHandler.detectOS();
			assertNotNull(maybeOS);
			assertTrue(maybeOS.isPresent());

			final ILocalOS os = maybeOS.get();
			assertEquals(LocalOsHandler.HP, os);
			assertTrue(os.isUnix());
		}
	}

	@Test
	void testDetectOSFreeBSD() {
		try (final MockedStatic<LocalOsHandler> mockedLocalOSHandler = mockStatic(LocalOsHandler.class)) {
			mockedLocalOSHandler.when(LocalOsHandler::getSystemOSName).thenReturn(Optional.of("FreeBSD"));
			mockedLocalOSHandler.when(LocalOsHandler::detectOS).thenCallRealMethod();

			final Optional<ILocalOS> maybeOS = LocalOsHandler.detectOS();
			assertNotNull(maybeOS);
			assertTrue(maybeOS.isPresent());

			final ILocalOS os = maybeOS.get();
			assertEquals(LocalOsHandler.FREE_BSD, os);
			assertTrue(os.isUnix());

			final FreeBSD bsdOS = (FreeBSD) os;
			assertTrue(bsdOS.isBsd());
		}
	}

	@Test
	void testDetectOSOpenBSD() {
		try (final MockedStatic<LocalOsHandler> mockedLocalOSHandler = mockStatic(LocalOsHandler.class)) {
			mockedLocalOSHandler.when(LocalOsHandler::getSystemOSName).thenReturn(Optional.of("OpenBSD"));
			mockedLocalOSHandler.when(LocalOsHandler::detectOS).thenCallRealMethod();

			final Optional<ILocalOS> maybeOS = LocalOsHandler.detectOS();
			assertNotNull(maybeOS);
			assertTrue(maybeOS.isPresent());

			final ILocalOS os = maybeOS.get();
			assertEquals(LocalOsHandler.OPEN_BSD, os);
			assertTrue(os.isUnix());

			final OpenBSD bsdOS = (OpenBSD) os;
			assertTrue(bsdOS.isBsd());
		}
	}

	@Test
	void testDetectOSNetBSD() {
		try (final MockedStatic<LocalOsHandler> mockedLocalOSHandler = mockStatic(LocalOsHandler.class)) {
			mockedLocalOSHandler.when(LocalOsHandler::getSystemOSName).thenReturn(Optional.of("NetBSD"));
			mockedLocalOSHandler.when(LocalOsHandler::detectOS).thenCallRealMethod();

			final Optional<ILocalOS> maybeOS = LocalOsHandler.detectOS();
			assertNotNull(maybeOS);
			assertTrue(maybeOS.isPresent());

			final ILocalOS os = maybeOS.get();
			assertEquals(LocalOsHandler.NET_BSD, os);
			assertTrue(os.isUnix());

			final NetBSD bsdOS = (NetBSD) os;
			assertTrue(bsdOS.isBsd());
		}
	}

	@Test
	void testDetectOSMacOSX() {
		try (final MockedStatic<LocalOsHandler> mockedLocalOSHandler = mockStatic(LocalOsHandler.class)) {
			mockedLocalOSHandler.when(LocalOsHandler::getSystemOSName).thenReturn(Optional.of("Mac OS X"));
			mockedLocalOSHandler.when(LocalOsHandler::detectOS).thenCallRealMethod();

			final Optional<ILocalOS> maybeOS = LocalOsHandler.detectOS();
			assertNotNull(maybeOS);
			assertTrue(maybeOS.isPresent());

			final ILocalOS os = maybeOS.get();
			assertEquals(LocalOsHandler.MAC_OS_X, os);
			assertTrue(os.isUnix());
		}
	}

	@Test
	@EnabledOnOs(WINDOWS)
	void testIsWindows() {
		assertTrue(LocalOsHandler.isWindows());
	}

	@Test
	@EnabledOnOs(LINUX)
	void testIsNotWindows() {
		assertFalse(LocalOsHandler.isWindows());
	}
}
