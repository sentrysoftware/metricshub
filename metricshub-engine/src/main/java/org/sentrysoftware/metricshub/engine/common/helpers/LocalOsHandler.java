package org.sentrysoftware.metricshub.engine.common.helpers;

import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LocalOsHandler {

	public static final ILocalOs WINDOWS = new Windows();
	public static final ILocalOs LINUX = new Linux();
	public static final ILocalOs SUN = new Sun();
	public static final ILocalOs HP = new Hp();
	public static final ILocalOs SOLARIS = new Solaris();
	public static final ILocalOs AIX = new Aix();
	public static final ILocalOs FREE_BSD = new FreeBsd();
	public static final ILocalOs OPEN_BSD = new OpenBsd();
	public static final ILocalOs NET_BSD = new NetBsd();
	public static final ILocalOs MAC_OS_X = new MacOsx();

	private static final List<ILocalOs> OS_LIST = List.of(
		WINDOWS,
		LINUX,
		AIX,
		SUN,
		HP,
		MAC_OS_X,
		SOLARIS,
		FREE_BSD,
		OPEN_BSD,
		NET_BSD
	);

	@Getter
	private static final Optional<ILocalOs> OS = detectOs();

	private static final boolean IS_WINDOWS = OS.isPresent() && OS.get().equals(WINDOWS);

	/**
	 * Detect the current Local OS.
	 * @return An optional with the current local OS. Empty if not determined.
	 */
	static Optional<ILocalOs> detectOs() {
		return getSystemOsName()
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
	public static Optional<String> getSystemOsName() {
		return Optional.ofNullable(System.getProperty("os.name"));
	}

	/**
	 * Get the OS Version from the System.
	 * @return An Optional of the OS version. Empty if not defined.
	 */
	public static Optional<String> getSystemOsVersion() {
		return Optional.ofNullable(System.getProperty("os.version"));
	}

	public interface ILocalOsVisitor {
		void visit(Windows os);
		void visit(Linux os);
		void visit(Sun os);
		void visit(Hp os);
		void visit(Solaris os);
		void visit(Aix os);
		void visit(FreeBsd os);
		void visit(OpenBsd os);
		void visit(NetBsd os);
		void visit(MacOsx os);
	}

	@Getter
	public abstract static class ILocalOs {

		protected String osTag;
		protected boolean unix;

		public abstract void accept(ILocalOsVisitor visitor);
	}

	public static class Windows extends ILocalOs {

		Windows() {
			osTag = "windows";
			unix = false;
		}

		@Override
		public void accept(final ILocalOsVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class Linux extends ILocalOs {

		Linux() {
			osTag = "linux";
			unix = true;
		}

		@Override
		public void accept(final ILocalOsVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class Sun extends ILocalOs {

		Sun() {
			osTag = "sunos";
			unix = true;
		}

		@Override
		public void accept(final ILocalOsVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class Hp extends ILocalOs {

		Hp() {
			osTag = "hp-ux";
			unix = true;
		}

		@Override
		public void accept(final ILocalOsVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class Solaris extends ILocalOs {

		Solaris() {
			osTag = "solaris";
			unix = true;
		}

		@Override
		public void accept(final ILocalOsVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class Aix extends ILocalOs {

		Aix() {
			osTag = "aix";
			unix = true;
		}

		@Override
		public void accept(final ILocalOsVisitor visitor) {
			visitor.visit(this);
		}
	}

	@Getter
	private abstract static class BsdOs extends ILocalOs {

		private final boolean bsd;

		BsdOs() {
			bsd = true;
			unix = true;
		}
	}

	public static class FreeBsd extends BsdOs {

		FreeBsd() {
			super();
			osTag = "freebsd";
		}

		@Override
		public void accept(final ILocalOsVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class OpenBsd extends BsdOs {

		OpenBsd() {
			super();
			osTag = "openbsd";
		}

		@Override
		public void accept(final ILocalOsVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class NetBsd extends BsdOs {

		NetBsd() {
			super();
			osTag = "netbsd";
		}

		@Override
		public void accept(final ILocalOsVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class MacOsx extends ILocalOs {

		MacOsx() {
			osTag = "mac os x";
			unix = true;
		}

		@Override
		public void accept(final ILocalOsVisitor visitor) {
			visitor.visit(this);
		}
	}
}
