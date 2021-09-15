package com.sentrysoftware.matrix.common.helpers;

import java.util.List;
import java.util.Optional;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor (access=AccessLevel.PRIVATE)
public class LocalOSHandler {

	public static final ILocalOS WINDOWS = new Windows();
	public static final ILocalOS LINUX = new Linux();
	public static final ILocalOS SUN = new Sun();
	public static final ILocalOS HP = new Hp();
	public static final ILocalOS SOLARIS = new Solaris();
	public static final ILocalOS AIX = new Aix();
	public static final ILocalOS FREE_BSD = new FreeBSD();
	public static final ILocalOS OPEN_BSD = new OpenBSD();
	public static final ILocalOS NET_BSD = new NetBSD();
	public static final ILocalOS MAC_OS_X = new MacOSX();

	private static final List<ILocalOS> OS_LIST = List.of(WINDOWS, LINUX, AIX, SUN, HP, MAC_OS_X, SOLARIS, FREE_BSD, OPEN_BSD, NET_BSD);

	@Getter
	private static final Optional<ILocalOS> OS = detectOS();
	private static final boolean IS_WINDOWS = OS.isPresent() && OS.get().equals(WINDOWS);

	/**
	 * Detect the current Local OS.
	 * @return An optional with the current local OS. Empty if not determined.
	 */
	static Optional<ILocalOS> detectOS() {
		return getSystemOSName()
				.map(String::toLowerCase)
				.map(name -> OS_LIST.stream().filter(os -> name.startsWith(os.getOsTag())).findFirst().orElse(null));
	}
	
	/**
	 * Check if the Local OS is a Windows.
	 * 
	 * @return true if Windows false otherwise.
	 */
	public static boolean isWindows() {
		return IS_WINDOWS;
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

	public static interface ILocalOSVisitor {
		void visit(final Windows os);
		void visit(final Linux os);
		void visit(final Sun os);
		void visit(final Hp os);
		void visit(final Solaris os);
		void visit(final Aix os);
		void visit(final FreeBSD os);
		void visit(final OpenBSD os);
		void visit(final NetBSD os);
		void visit(final MacOSX os);
	}

	@Getter
	public abstract static class ILocalOS {

		protected String osTag;
		protected boolean unix;

		public abstract void accept(final ILocalOSVisitor visitor);
	}

	public static class Windows extends ILocalOS {
		Windows() {
			osTag = "windows";
			unix = false;
		}

		@Override
		public void accept(final ILocalOSVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class Linux extends ILocalOS {
		Linux() {
			osTag = "linux";
			unix = true;
		}

		@Override
		public void accept(final ILocalOSVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class Sun extends ILocalOS {
		Sun() {
			osTag = "sunos";
			unix = true;
		}

		@Override
		public void accept(final ILocalOSVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class Hp extends ILocalOS {
		Hp() {
			osTag = "hp-ux";
			unix = true;
		}

		@Override
		public void accept(final ILocalOSVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class Solaris extends ILocalOS {
		Solaris() {
			osTag = "solaris";
			unix = true;
		}

		@Override
		public void accept(final ILocalOSVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class Aix extends ILocalOS {
		Aix() {
			osTag = "aix";
			unix = true;
		}

		@Override
		public void accept(final ILocalOSVisitor visitor) {
			visitor.visit(this);
		}
	}

	@Getter
	private abstract static class BsdOS extends ILocalOS {

		private final boolean bsd;

		BsdOS() {
			bsd = true;
			unix = true;
		}
	}

	public static class FreeBSD extends BsdOS {

		FreeBSD() {
			super();
			osTag = "freebsd";
		}

		@Override
		public void accept(final ILocalOSVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class OpenBSD extends BsdOS {
		OpenBSD() {
			super();
			osTag = "openbsd";
		}

		@Override
		public void accept(final ILocalOSVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class NetBSD extends BsdOS {
		NetBSD() {
			super();
			osTag = "netbsd";
		}

		@Override
		public void accept(final ILocalOSVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class MacOSX extends ILocalOS {
		MacOSX() {
			osTag = "mac os x";
			unix = true;
		}

		@Override
		public void accept(final ILocalOSVisitor visitor) {
			visitor.visit(this);
		}
	}
}
