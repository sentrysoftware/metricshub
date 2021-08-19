package com.sentrysoftware.matrix.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.sentrysoftware.matrix.common.helpers.LocalOSHandler;
import com.sentrysoftware.matrix.common.helpers.LocalOSHandler.ILocalOS;

class LocalOSHandlerTest {

	@Test
	void testGetOSNotFoundlSystemOSName() {
		try (final MockedStatic<LocalOSHandler> mockedLocalOSEnum = mockStatic(LocalOSHandler.class)) {
			mockedLocalOSEnum.when(LocalOSHandler::getSystemOSName).thenReturn(Optional.empty());
			mockedLocalOSEnum.when(LocalOSHandler::getOS).thenCallRealMethod();

			assertEquals(Optional.empty(), LocalOSHandler.getOS());
		}
	}

	@Test
	void testGetOSUnknown() {
		try (final MockedStatic<LocalOSHandler> mockedLocalOSEnum = mockStatic(LocalOSHandler.class)) {
			mockedLocalOSEnum.when(LocalOSHandler::getSystemOSName).thenReturn(Optional.of("OS/400"));
			mockedLocalOSEnum.when(LocalOSHandler::getOS).thenCallRealMethod();

			assertEquals(Optional.empty(), LocalOSHandler.getOS());
		}
	}

	@Test
	void testWindows() {
		try (final MockedStatic<LocalOSHandler> mockedLocalOSEnum = mockStatic(LocalOSHandler.class)) {
			mockedLocalOSEnum.when(LocalOSHandler::getSystemOSName).thenReturn(Optional.of("Windows 8.1"));
			mockedLocalOSEnum.when(LocalOSHandler::getOS).thenCallRealMethod();

			final Optional<ILocalOS> maybeOS = LocalOSHandler.getOS();
			assertNotNull(maybeOS);
			assertTrue(maybeOS.isPresent());

			final ILocalOS os = maybeOS.get();
			assertEquals(LocalOSHandler.WINDOWS, os);
			assertFalse(os.isUnix());
		}
	}

	@Test
	void testLinux() {
		try (final MockedStatic<LocalOSHandler> mockedLocalOSEnum = mockStatic(LocalOSHandler.class)) {
			mockedLocalOSEnum.when(LocalOSHandler::getSystemOSName).thenReturn(Optional.of("Linux"));
			mockedLocalOSEnum.when(LocalOSHandler::getOS).thenCallRealMethod();

			final Optional<ILocalOS> maybeOS = LocalOSHandler.getOS();
			assertNotNull(maybeOS);
			assertTrue(maybeOS.isPresent());

			final ILocalOS os = maybeOS.get();
			assertEquals(LocalOSHandler.LINUX, os);
			assertTrue(os.isUnix());
		}
	}

	@Test
	void testAix() {
		try (final MockedStatic<LocalOSHandler> mockedLocalOSEnum = mockStatic(LocalOSHandler.class)) {
			mockedLocalOSEnum.when(LocalOSHandler::getSystemOSName).thenReturn(Optional.of("AIX"));
			mockedLocalOSEnum.when(LocalOSHandler::getOS).thenCallRealMethod();

			final Optional<ILocalOS> maybeOS = LocalOSHandler.getOS();
			assertNotNull(maybeOS);
			assertTrue(maybeOS.isPresent());

			final ILocalOS os = maybeOS.get();
			assertEquals(LocalOSHandler.AIX, os);
			assertTrue(os.isUnix());
		}
	}

	@Test
	void testSolaris() {
		try (final MockedStatic<LocalOSHandler> mockedLocalOSEnum = mockStatic(LocalOSHandler.class)) {
			mockedLocalOSEnum.when(LocalOSHandler::getSystemOSName).thenReturn(Optional.of("Solaris"));
			mockedLocalOSEnum.when(LocalOSHandler::getOS).thenCallRealMethod();

			final Optional<ILocalOS> maybeOS = LocalOSHandler.getOS();
			assertNotNull(maybeOS);
			assertTrue(maybeOS.isPresent());

			final ILocalOS os = maybeOS.get();
			assertEquals(LocalOSHandler.SOLARIS, os);
			assertTrue(os.isUnix());
		}
	}

	@Test
	void testSunOS() {
		try (final MockedStatic<LocalOSHandler> mockedLocalOSEnum = mockStatic(LocalOSHandler.class)) {
			mockedLocalOSEnum.when(LocalOSHandler::getSystemOSName).thenReturn(Optional.of("sunos"));
			mockedLocalOSEnum.when(LocalOSHandler::getOS).thenCallRealMethod();

			final Optional<ILocalOS> maybeOS = LocalOSHandler.getOS();
			assertNotNull(maybeOS);
			assertTrue(maybeOS.isPresent());

			final ILocalOS os = maybeOS.get();
			assertEquals(LocalOSHandler.SUN, os);
			assertTrue(os.isUnix());
		}
	}

	@Test
	void testHp() {
		try (final MockedStatic<LocalOSHandler> mockedLocalOSEnum = mockStatic(LocalOSHandler.class)) {
			mockedLocalOSEnum.when(LocalOSHandler::getSystemOSName).thenReturn(Optional.of("HP-UX"));
			mockedLocalOSEnum.when(LocalOSHandler::getOS).thenCallRealMethod();

			final Optional<ILocalOS> maybeOS = LocalOSHandler.getOS();
			assertNotNull(maybeOS);
			assertTrue(maybeOS.isPresent());

			final ILocalOS os = maybeOS.get();
			assertEquals(LocalOSHandler.HP, os);
			assertTrue(os.isUnix());
		}
	}

	@Test
	void testFreeBSD() {
		try (final MockedStatic<LocalOSHandler> mockedLocalOSEnum = mockStatic(LocalOSHandler.class)) {
			mockedLocalOSEnum.when(LocalOSHandler::getSystemOSName).thenReturn(Optional.of("FreeBSD"));
			mockedLocalOSEnum.when(LocalOSHandler::getOS).thenCallRealMethod();

			final Optional<ILocalOS> maybeOS = LocalOSHandler.getOS();
			assertNotNull(maybeOS);
			assertTrue(maybeOS.isPresent());

			final ILocalOS os = maybeOS.get();
			assertEquals(LocalOSHandler.FREE_BSD, os);
			assertTrue(os.isUnix());
		}
	}

	@Test
	void testOpenBSD() {
		try (final MockedStatic<LocalOSHandler> mockedLocalOSEnum = mockStatic(LocalOSHandler.class)) {
			mockedLocalOSEnum.when(LocalOSHandler::getSystemOSName).thenReturn(Optional.of("OpenBSD"));
			mockedLocalOSEnum.when(LocalOSHandler::getOS).thenCallRealMethod();

			final Optional<ILocalOS> maybeOS = LocalOSHandler.getOS();
			assertNotNull(maybeOS);
			assertTrue(maybeOS.isPresent());

			final ILocalOS os = maybeOS.get();
			assertEquals(LocalOSHandler.OPEN_BSD, os);
			assertTrue(os.isUnix());
		}
	}

	@Test
	void testNetBSD() {
		try (final MockedStatic<LocalOSHandler> mockedLocalOSEnum = mockStatic(LocalOSHandler.class)) {
			mockedLocalOSEnum.when(LocalOSHandler::getSystemOSName).thenReturn(Optional.of("NetBSD"));
			mockedLocalOSEnum.when(LocalOSHandler::getOS).thenCallRealMethod();

			final Optional<ILocalOS> maybeOS = LocalOSHandler.getOS();
			assertNotNull(maybeOS);
			assertTrue(maybeOS.isPresent());

			final ILocalOS os = maybeOS.get();
			assertEquals(LocalOSHandler.NET_BSD, os);
			assertTrue(os.isUnix());
		}
	}

	@Test
	void testOs2() {
		try (final MockedStatic<LocalOSHandler> mockedLocalOSEnum = mockStatic(LocalOSHandler.class)) {
			mockedLocalOSEnum.when(LocalOSHandler::getSystemOSName).thenReturn(Optional.of("OS/2"));
			mockedLocalOSEnum.when(LocalOSHandler::getOS).thenCallRealMethod();

			final Optional<ILocalOS> maybeOS = LocalOSHandler.getOS();
			assertNotNull(maybeOS);
			assertTrue(maybeOS.isPresent());

			final ILocalOS os = maybeOS.get();
			assertEquals(LocalOSHandler.OS2, os);
			assertFalse(os.isUnix());
		}
	}

	@Test
	void testMacOSX() {
		try (final MockedStatic<LocalOSHandler> mockedLocalOSEnum = mockStatic(LocalOSHandler.class)) {
			mockedLocalOSEnum.when(LocalOSHandler::getSystemOSName).thenReturn(Optional.of("Mac OS X"));
			mockedLocalOSEnum.when(LocalOSHandler::getOS).thenCallRealMethod();

			final Optional<ILocalOS> maybeOS = LocalOSHandler.getOS();
			assertNotNull(maybeOS);
			assertTrue(maybeOS.isPresent());

			final ILocalOS os = maybeOS.get();
			assertEquals(LocalOSHandler.MAC_OS_X, os);
			assertTrue(os.isUnix());
		}
	}

	@Test
	void testIrix() {
		try (final MockedStatic<LocalOSHandler> mockedLocalOSEnum = mockStatic(LocalOSHandler.class)) {
			mockedLocalOSEnum.when(LocalOSHandler::getSystemOSName).thenReturn(Optional.of("IRIX"));
			mockedLocalOSEnum.when(LocalOSHandler::getOS).thenCallRealMethod();

			final Optional<ILocalOS> maybeOS = LocalOSHandler.getOS();
			assertNotNull(maybeOS);
			assertTrue(maybeOS.isPresent());

			final ILocalOS os = maybeOS.get();
			assertEquals(LocalOSHandler.IRIX, os);
			assertTrue(os.isUnix());
		}
	}
}
