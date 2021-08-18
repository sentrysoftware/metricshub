package com.sentrysoftware.matrix.common.helpers;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor (access=AccessLevel.PRIVATE)
public enum LocalOSEnum {

	WINDOWS ("windows", new Windows()),
	LINUX ("linux", new Linux()),
	SUN_OS ("sunos", new Sun()),
	HP ("hp-ux", new Hp()),
	SOLARIS ("solaris", new Solaris()),
	OS2 ("os/2", new Os2()),
	AIX ("aix", new Aix()),
	FREE_BSD ("freebsd", new FreeBSD()),
	OPEN_BSD ("openbsd", new OpenBSD()),
	NET_BSD ("netbsd", new NetBSD()),
	IRIX ("irix", new Irix()),
	MAC_OS_X ("mac os x", new MacOSX());

	private static final Set<LocalOSEnum> UNIX_SET = Set.of(AIX, HP, IRIX, LINUX, MAC_OS_X, SOLARIS, SUN_OS, FREE_BSD, OPEN_BSD, NET_BSD);

	private final String startTag;
	private final ILocalOS localOS;

	/**
	 * Get the current Local OS.
	 * @return An optional with the current local OS. Empty if not determined.
	 */
	public static Optional<LocalOSEnum> getOS() {
		return getSystemOSName()
				.map(String::toLowerCase)
				.map(name -> Stream.of(values()).filter(os -> name.startsWith(os.startTag)).findFirst().orElse(null));
	}

	/**
	 * Get the OS Name from the System.
	 * @return An Optional of the OS name. Empty if not defined.
	 */
	public static Optional<String> getSystemOSName() {
		return Optional.ofNullable(System.getProperty("os.name"));
	}

	/**
	 * Get the OS Version from the System.
	 * @return An Optional of the OS version. Empty if not defined.
	 */
	public static Optional<String> getSystemOSVersion() {
		return Optional.ofNullable(System.getProperty("os.version"));
	}

	/**
	 * Indicate if the Local OS is an UNIX OS.
	 * @return true if the OS is an UNIX OS, false Otherwise.
	 */
	public boolean isUnix() {
		return UNIX_SET.contains(this);
	}

	public void accept(final ILocalOSVisitor visitor) {
		if (visitor != null) {
			localOS.accept(visitor);
		}
	}

	public static interface ILocalOSVisitor {
		void visit(final Windows os);
		void visit(final Linux os);
		void visit(final Sun os);
		void visit(final Hp os);
		void visit(final Solaris os);
		void visit(final Os2 os);
		void visit(final Aix os);
		void visit(final FreeBSD os);
		void visit(final OpenBSD os);
		void visit(final NetBSD os);
		void visit(final Irix os);
		void visit(final MacOSX os);
	}

	private static interface ILocalOS {
		public abstract void accept(final ILocalOSVisitor visitor);
	}

	public static class Windows implements ILocalOS {
		@Override
		public void accept(final ILocalOSVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class Linux implements ILocalOS {
		@Override
		public void accept(final ILocalOSVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class Sun implements ILocalOS {
		@Override
		public void accept(final ILocalOSVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class Hp implements ILocalOS {
		@Override
		public void accept(final ILocalOSVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class Solaris implements ILocalOS {
		@Override
		public void accept(final ILocalOSVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class Os2 implements ILocalOS {
		@Override
		public void accept(final ILocalOSVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class Aix implements ILocalOS {
		@Override
		public void accept(final ILocalOSVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class FreeBSD implements ILocalOS {
		@Override
		public void accept(final ILocalOSVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class OpenBSD implements ILocalOS {
		@Override
		public void accept(final ILocalOSVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class NetBSD implements ILocalOS {
		@Override
		public void accept(final ILocalOSVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class Irix implements ILocalOS {
		@Override
		public void accept(final ILocalOSVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class MacOSX implements ILocalOS {
		@Override
		public void accept(final ILocalOSVisitor visitor) {
			visitor.visit(this);
		}
	}
}
