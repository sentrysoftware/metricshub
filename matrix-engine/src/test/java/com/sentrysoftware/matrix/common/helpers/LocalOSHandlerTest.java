package com.sentrysoftware.matrix.common.helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.sentrysoftware.matrix.common.helpers.LocalOSHandler.FreeBSD;
import com.sentrysoftware.matrix.common.helpers.LocalOSHandler.ILocalOS;
import com.sentrysoftware.matrix.common.helpers.LocalOSHandler.NetBSD;
import com.sentrysoftware.matrix.common.helpers.LocalOSHandler.OpenBSD;

class LocalOSHandlerTest {

	@Test
	void testDetectOSNotFoundlSystemOSName() {
		try (final MockedStatic<LocalOSHandler> mockedLocalOSHandler = mockStatic(LocalOSHandler.class)) {
			mockedLocalOSHandler.when(LocalOSHandler::getSystemOSName).thenReturn(Optional.empty());
			mockedLocalOSHandler.when(LocalOSHandler::detectOS).thenCallRealMethod();

			assertEquals(Optional.empty(), LocalOSHandler.detectOS());
		}
	}

	@Test
	void testDetectOSUnknown() {
		try (final MockedStatic<LocalOSHandler> mockedLocalOSHandler = mockStatic(LocalOSHandler.class)) {
			mockedLocalOSHandler.when(LocalOSHandler::getSystemOSName).thenReturn(Optional.of("OS/400"));
			mockedLocalOSHandler.when(LocalOSHandler::detectOS).thenCallRealMethod();

			assertEquals(Optional.empty(), LocalOSHandler.detectOS());
		}
	}

	@Test
	void testDetectOSWindows() {
		try (final MockedStatic<LocalOSHandler> mockedLocalOSHandler = mockStatic(LocalOSHandler.class)) {
			mockedLocalOSHandler.when(LocalOSHandler::getSystemOSName).thenReturn(Optional.of("Windows 8.1"));
			mockedLocalOSHandler.when(LocalOSHandler::detectOS).thenCallRealMethod();

			final Optional<ILocalOS> maybeOS = LocalOSHandler.detectOS();
			assertNotNull(maybeOS);
			assertTrue(maybeOS.isPresent());

			final ILocalOS os = maybeOS.get();
			assertEquals(LocalOSHandler.WINDOWS, os);
			assertFalse(os.isUnix());
		}
	}

	@Test
	void testDetectOSLinux() {
		try (final MockedStatic<LocalOSHandler> mockedLocalOSHandler = mockStatic(LocalOSHandler.class)) {
			mockedLocalOSHandler.when(LocalOSHandler::getSystemOSName).thenReturn(Optional.of("Linux"));
			mockedLocalOSHandler.when(LocalOSHandler::detectOS).thenCallRealMethod();

			final Optional<ILocalOS> maybeOS = LocalOSHandler.detectOS();
			assertNotNull(maybeOS);
			assertTrue(maybeOS.isPresent());

			final ILocalOS os = maybeOS.get();
			assertEquals(LocalOSHandler.LINUX, os);
			assertTrue(os.isUnix());
		}
	}

	@Test
	void testDetectOSAix() {
		try (final MockedStatic<LocalOSHandler> mockedLocalOSHandler = mockStatic(LocalOSHandler.class)) {
			mockedLocalOSHandler.when(LocalOSHandler::getSystemOSName).thenReturn(Optional.of("AIX"));
			mockedLocalOSHandler.when(LocalOSHandler::detectOS).thenCallRealMethod();

			final Optional<ILocalOS> maybeOS = LocalOSHandler.detectOS();
			assertNotNull(maybeOS);
			assertTrue(maybeOS.isPresent());

			final ILocalOS os = maybeOS.get();
			assertEquals(LocalOSHandler.AIX, os);
			assertTrue(os.isUnix());
		}
	}

	@Test
	void testDetectOSSolaris() {
		try (final MockedStatic<LocalOSHandler> mockedLocalOSHandler = mockStatic(LocalOSHandler.class)) {
			mockedLocalOSHandler.when(LocalOSHandler::getSystemOSName).thenReturn(Optional.of("Solaris"));
			mockedLocalOSHandler.when(LocalOSHandler::detectOS).thenCallRealMethod();

			final Optional<ILocalOS> maybeOS = LocalOSHandler.detectOS();
			assertNotNull(maybeOS);
			assertTrue(maybeOS.isPresent());

			final ILocalOS os = maybeOS.get();
			assertEquals(LocalOSHandler.SOLARIS, os);
			assertTrue(os.isUnix());
		}
	}

	@Test
	void testDetectOSSunOS() {
		try (final MockedStatic<LocalOSHandler> mockedLocalOSHandler = mockStatic(LocalOSHandler.class)) {
			mockedLocalOSHandler.when(LocalOSHandler::getSystemOSName).thenReturn(Optional.of("sunos"));
			mockedLocalOSHandler.when(LocalOSHandler::detectOS).thenCallRealMethod();

			final Optional<ILocalOS> maybeOS = LocalOSHandler.detectOS();
			assertNotNull(maybeOS);
			assertTrue(maybeOS.isPresent());

			final ILocalOS os = maybeOS.get();
			assertEquals(LocalOSHandler.SUN, os);
			assertTrue(os.isUnix());
		}
	}

	@Test
	void testDetectOSHp() {
		try (final MockedStatic<LocalOSHandler> mockedLocalOSHandler = mockStatic(LocalOSHandler.class)) {
			mockedLocalOSHandler.when(LocalOSHandler::getSystemOSName).thenReturn(Optional.of("HP-UX"));
			mockedLocalOSHandler.when(LocalOSHandler::detectOS).thenCallRealMethod();

			final Optional<ILocalOS> maybeOS = LocalOSHandler.detectOS();
			assertNotNull(maybeOS);
			assertTrue(maybeOS.isPresent());

			final ILocalOS os = maybeOS.get();
			assertEquals(LocalOSHandler.HP, os);
			assertTrue(os.isUnix());
		}
	}

	@Test
	void testDetectOSFreeBSD() {
		try (final MockedStatic<LocalOSHandler> mockedLocalOSHandler = mockStatic(LocalOSHandler.class)) {
			mockedLocalOSHandler.when(LocalOSHandler::getSystemOSName).thenReturn(Optional.of("FreeBSD"));
			mockedLocalOSHandler.when(LocalOSHandler::detectOS).thenCallRealMethod();

			final Optional<ILocalOS> maybeOS = LocalOSHandler.detectOS();
			assertNotNull(maybeOS);
			assertTrue(maybeOS.isPresent());

			final ILocalOS os = maybeOS.get();
			assertEquals(LocalOSHandler.FREE_BSD, os);
			assertTrue(os.isUnix());

			final FreeBSD bsdOS = (FreeBSD) os;
			assertTrue(bsdOS.isBsd());
		}
	}

	@Test
	void testDetectOSOpenBSD() {
		try (final MockedStatic<LocalOSHandler> mockedLocalOSHandler = mockStatic(LocalOSHandler.class)) {
			mockedLocalOSHandler.when(LocalOSHandler::getSystemOSName).thenReturn(Optional.of("OpenBSD"));
			mockedLocalOSHandler.when(LocalOSHandler::detectOS).thenCallRealMethod();

			final Optional<ILocalOS> maybeOS = LocalOSHandler.detectOS();
			assertNotNull(maybeOS);
			assertTrue(maybeOS.isPresent());

			final ILocalOS os = maybeOS.get();
			assertEquals(LocalOSHandler.OPEN_BSD, os);
			assertTrue(os.isUnix());

			final OpenBSD bsdOS = (OpenBSD) os;
			assertTrue(bsdOS.isBsd());
		}
	}

	@Test
	void testDetectOSNetBSD() {
		try (final MockedStatic<LocalOSHandler> mockedLocalOSHandler = mockStatic(LocalOSHandler.class)) {
			mockedLocalOSHandler.when(LocalOSHandler::getSystemOSName).thenReturn(Optional.of("NetBSD"));
			mockedLocalOSHandler.when(LocalOSHandler::detectOS).thenCallRealMethod();

			final Optional<ILocalOS> maybeOS = LocalOSHandler.detectOS();
			assertNotNull(maybeOS);
			assertTrue(maybeOS.isPresent());

			final ILocalOS os = maybeOS.get();
			assertEquals(LocalOSHandler.NET_BSD, os);
			assertTrue(os.isUnix());

			final NetBSD bsdOS = (NetBSD) os;
			assertTrue(bsdOS.isBsd());
		}
	}

	@Test
	void testDetectOSMacOSX() {
		try (final MockedStatic<LocalOSHandler> mockedLocalOSHandler = mockStatic(LocalOSHandler.class)) {
			mockedLocalOSHandler.when(LocalOSHandler::getSystemOSName).thenReturn(Optional.of("Mac OS X"));
			mockedLocalOSHandler.when(LocalOSHandler::detectOS).thenCallRealMethod();

			final Optional<ILocalOS> maybeOS = LocalOSHandler.detectOS();
			assertNotNull(maybeOS);
			assertTrue(maybeOS.isPresent());

			final ILocalOS os = maybeOS.get();
			assertEquals(LocalOSHandler.MAC_OS_X, os);
			assertTrue(os.isUnix());
		}
	}
}
