package com.sentrysoftware.matrix.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.sentrysoftware.matrix.common.helpers.LocalOSEnum;

class LocalOSEnumTest {

	@Test
	void testGetOSNotFoundlSystemOSName() {
		try (final MockedStatic<LocalOSEnum> mockedLocalOSEnum = mockStatic(LocalOSEnum.class)) {
			mockedLocalOSEnum.when(LocalOSEnum::getSystemOSName).thenReturn(Optional.empty());
			mockedLocalOSEnum.when(LocalOSEnum::getOS).thenCallRealMethod();

			assertEquals(Optional.empty(), LocalOSEnum.getOS());
		}
	}

	@Test
	void testGetOSUnknown() {
		try (final MockedStatic<LocalOSEnum> mockedLocalOSEnum = mockStatic(LocalOSEnum.class)) {
			mockedLocalOSEnum.when(LocalOSEnum::getSystemOSName).thenReturn(Optional.of("OS/400"));
			mockedLocalOSEnum.when(LocalOSEnum::values).thenCallRealMethod();
			mockedLocalOSEnum.when(LocalOSEnum::getOS).thenCallRealMethod();

			assertEquals(Optional.empty(), LocalOSEnum.getOS());
		}
	}

	@Test
	void testWindows() {
		try (final MockedStatic<LocalOSEnum> mockedLocalOSEnum = mockStatic(LocalOSEnum.class)) {
			mockedLocalOSEnum.when(LocalOSEnum::getSystemOSName).thenReturn(Optional.of("Windows 8.1"));
			mockedLocalOSEnum.when(LocalOSEnum::values).thenCallRealMethod();
			mockedLocalOSEnum.when(LocalOSEnum::getOS).thenCallRealMethod();

			final Optional<LocalOSEnum> maybeOS = LocalOSEnum.getOS();
			assertNotNull(maybeOS);
			assertTrue(maybeOS.isPresent());

			final LocalOSEnum os = maybeOS.get();
			assertEquals(LocalOSEnum.WINDOWS, os);
			assertFalse(os.isUnix());
		}
	}

	@Test
	void testLinux() {
		try (final MockedStatic<LocalOSEnum> mockedLocalOSEnum = mockStatic(LocalOSEnum.class)) {
			mockedLocalOSEnum.when(LocalOSEnum::getSystemOSName).thenReturn(Optional.of("Linux"));
			mockedLocalOSEnum.when(LocalOSEnum::values).thenCallRealMethod();
			mockedLocalOSEnum.when(LocalOSEnum::getOS).thenCallRealMethod();

			final Optional<LocalOSEnum> maybeOS = LocalOSEnum.getOS();
			assertNotNull(maybeOS);
			assertTrue(maybeOS.isPresent());

			final LocalOSEnum os = maybeOS.get();
			assertEquals(LocalOSEnum.LINUX, os);
			assertTrue(os.isUnix());
		}
	}

	@Test
	void testAix() {
		try (final MockedStatic<LocalOSEnum> mockedLocalOSEnum = mockStatic(LocalOSEnum.class)) {
			mockedLocalOSEnum.when(LocalOSEnum::getSystemOSName).thenReturn(Optional.of("AIX"));
			mockedLocalOSEnum.when(LocalOSEnum::values).thenCallRealMethod();
			mockedLocalOSEnum.when(LocalOSEnum::getOS).thenCallRealMethod();

			final Optional<LocalOSEnum> maybeOS = LocalOSEnum.getOS();
			assertNotNull(maybeOS);
			assertTrue(maybeOS.isPresent());

			final LocalOSEnum os = maybeOS.get();
			assertEquals(LocalOSEnum.AIX, os);
			assertTrue(os.isUnix());
		}
	}

	@Test
	void testSolaris() {
		try (final MockedStatic<LocalOSEnum> mockedLocalOSEnum = mockStatic(LocalOSEnum.class)) {
			mockedLocalOSEnum.when(LocalOSEnum::getSystemOSName).thenReturn(Optional.of("Solaris"));
			mockedLocalOSEnum.when(LocalOSEnum::values).thenCallRealMethod();
			mockedLocalOSEnum.when(LocalOSEnum::getOS).thenCallRealMethod();

			final Optional<LocalOSEnum> maybeOS = LocalOSEnum.getOS();
			assertNotNull(maybeOS);
			assertTrue(maybeOS.isPresent());

			final LocalOSEnum os = maybeOS.get();
			assertEquals(LocalOSEnum.SOLARIS, os);
			assertTrue(os.isUnix());
		}
	}

	@Test
	void testSunOS() {
		try (final MockedStatic<LocalOSEnum> mockedLocalOSEnum = mockStatic(LocalOSEnum.class)) {
			mockedLocalOSEnum.when(LocalOSEnum::getSystemOSName).thenReturn(Optional.of("sunos"));
			mockedLocalOSEnum.when(LocalOSEnum::values).thenCallRealMethod();
			mockedLocalOSEnum.when(LocalOSEnum::getOS).thenCallRealMethod();

			final Optional<LocalOSEnum> maybeOS = LocalOSEnum.getOS();
			assertNotNull(maybeOS);
			assertTrue(maybeOS.isPresent());

			final LocalOSEnum os = maybeOS.get();
			assertEquals(LocalOSEnum.SUN_OS, os);
			assertTrue(os.isUnix());
		}
	}

	@Test
	void testHp() {
		try (final MockedStatic<LocalOSEnum> mockedLocalOSEnum = mockStatic(LocalOSEnum.class)) {
			mockedLocalOSEnum.when(LocalOSEnum::getSystemOSName).thenReturn(Optional.of("HP-UX"));
			mockedLocalOSEnum.when(LocalOSEnum::values).thenCallRealMethod();
			mockedLocalOSEnum.when(LocalOSEnum::getOS).thenCallRealMethod();

			final Optional<LocalOSEnum> maybeOS = LocalOSEnum.getOS();
			assertNotNull(maybeOS);
			assertTrue(maybeOS.isPresent());

			final LocalOSEnum os = maybeOS.get();
			assertEquals(LocalOSEnum.HP, os);
			assertTrue(os.isUnix());
		}
	}

	@Test
	void testFreeBSD() {
		try (final MockedStatic<LocalOSEnum> mockedLocalOSEnum = mockStatic(LocalOSEnum.class)) {
			mockedLocalOSEnum.when(LocalOSEnum::getSystemOSName).thenReturn(Optional.of("FreeBSD"));
			mockedLocalOSEnum.when(LocalOSEnum::values).thenCallRealMethod();
			mockedLocalOSEnum.when(LocalOSEnum::getOS).thenCallRealMethod();

			final Optional<LocalOSEnum> maybeOS = LocalOSEnum.getOS();
			assertNotNull(maybeOS);
			assertTrue(maybeOS.isPresent());

			final LocalOSEnum os = maybeOS.get();
			assertEquals(LocalOSEnum.FREE_BSD, os);
			assertTrue(os.isUnix());
		}
	}

	@Test
	void testOpenBSD() {
		try (final MockedStatic<LocalOSEnum> mockedLocalOSEnum = mockStatic(LocalOSEnum.class)) {
			mockedLocalOSEnum.when(LocalOSEnum::getSystemOSName).thenReturn(Optional.of("OpenBSD"));
			mockedLocalOSEnum.when(LocalOSEnum::values).thenCallRealMethod();
			mockedLocalOSEnum.when(LocalOSEnum::getOS).thenCallRealMethod();

			final Optional<LocalOSEnum> maybeOS = LocalOSEnum.getOS();
			assertNotNull(maybeOS);
			assertTrue(maybeOS.isPresent());

			final LocalOSEnum os = maybeOS.get();
			assertEquals(LocalOSEnum.OPEN_BSD, os);
			assertTrue(os.isUnix());
		}
	}

	@Test
	void testNetBSD() {
		try (final MockedStatic<LocalOSEnum> mockedLocalOSEnum = mockStatic(LocalOSEnum.class)) {
			mockedLocalOSEnum.when(LocalOSEnum::getSystemOSName).thenReturn(Optional.of("NetBSD"));
			mockedLocalOSEnum.when(LocalOSEnum::values).thenCallRealMethod();
			mockedLocalOSEnum.when(LocalOSEnum::getOS).thenCallRealMethod();

			final Optional<LocalOSEnum> maybeOS = LocalOSEnum.getOS();
			assertNotNull(maybeOS);
			assertTrue(maybeOS.isPresent());

			final LocalOSEnum os = maybeOS.get();
			assertEquals(LocalOSEnum.NET_BSD, os);
			assertTrue(os.isUnix());
		}
	}

	@Test
	void testOs2() {
		try (final MockedStatic<LocalOSEnum> mockedLocalOSEnum = mockStatic(LocalOSEnum.class)) {
			mockedLocalOSEnum.when(LocalOSEnum::getSystemOSName).thenReturn(Optional.of("OS/2"));
			mockedLocalOSEnum.when(LocalOSEnum::values).thenCallRealMethod();
			mockedLocalOSEnum.when(LocalOSEnum::getOS).thenCallRealMethod();

			final Optional<LocalOSEnum> maybeOS = LocalOSEnum.getOS();
			assertNotNull(maybeOS);
			assertTrue(maybeOS.isPresent());

			final LocalOSEnum os = maybeOS.get();
			assertEquals(LocalOSEnum.OS2, os);
			assertFalse(os.isUnix());
		}
	}

	@Test
	void testMacOSX() {
		try (final MockedStatic<LocalOSEnum> mockedLocalOSEnum = mockStatic(LocalOSEnum.class)) {
			mockedLocalOSEnum.when(LocalOSEnum::getSystemOSName).thenReturn(Optional.of("Mac OS X"));
			mockedLocalOSEnum.when(LocalOSEnum::values).thenCallRealMethod();
			mockedLocalOSEnum.when(LocalOSEnum::getOS).thenCallRealMethod();

			final Optional<LocalOSEnum> maybeOS = LocalOSEnum.getOS();
			assertNotNull(maybeOS);
			assertTrue(maybeOS.isPresent());

			final LocalOSEnum os = maybeOS.get();
			assertEquals(LocalOSEnum.MAC_OS_X, os);
			assertTrue(os.isUnix());
		}
	}

	@Test
	void testIrix() {
		try (final MockedStatic<LocalOSEnum> mockedLocalOSEnum = mockStatic(LocalOSEnum.class)) {
			mockedLocalOSEnum.when(LocalOSEnum::getSystemOSName).thenReturn(Optional.of("IRIX"));
			mockedLocalOSEnum.when(LocalOSEnum::values).thenCallRealMethod();
			mockedLocalOSEnum.when(LocalOSEnum::getOS).thenCallRealMethod();

			final Optional<LocalOSEnum> maybeOS = LocalOSEnum.getOS();
			assertNotNull(maybeOS);
			assertTrue(maybeOS.isPresent());

			final LocalOSEnum os = maybeOS.get();
			assertEquals(LocalOSEnum.IRIX, os);
			assertTrue(os.isUnix());
		}
	}
}
