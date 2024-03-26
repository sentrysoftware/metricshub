package org.sentrysoftware.metricshub.classloader.agent;

import java.lang.instrument.Instrumentation;
import java.util.Optional;
import java.util.jar.JarFile;

/**
 * Class Loader Agent for dynamic class loading. (Needed for module-winrm and
 * java 11)
 *
 * @link https://cgjennings.ca/articles/java-9-dynamic-jar-loading/
 */
public class ClassLoaderAgent {

	private ClassLoaderAgent() {}

	private static Instrumentation inst;

	/**
	 * Called by the JRE. <em>Do not call this method from user code.</em>
	 * <p>
	 *
	 * @param unusedArgs      currently ignored
	 * @param instrumentation provided by the JRE
	 */
	public static void premain(final String unusedArgs, final Instrumentation instrumentation) {
		agentmain(unusedArgs, instrumentation);
	}

	/**
	 * Called by the JRE. <em>Do not call this method from user code.</em>
	 * <p>
	 *
	 * @param unusedArgs      currently ignored
	 * @param instrumentation provided by the JRE
	 */
	public static void agentmain(final String unusedArgs, final Instrumentation instrumentation) {
		inst = instrumentation;
	}

	/**
	 * Adds a JAR file to the system class loader's classpath.
	 *
	 * @param jarFile the JAR file to add to the classpath
	 */
	public static synchronized void addToClassPath(final JarFile jarFile) {
		getInstrumentation().ifPresent(instrumentation -> instrumentation.appendToSystemClassLoaderSearch(jarFile));
	}

	/**
	 * Retrieves the current instrumentation instance, if available.
	 *
	 * @return an {@code Optional} containing the current instrumentation instance, or an empty {@code Optional} if not available
	 */
	static Optional<Instrumentation> getInstrumentation() {
		return Optional.ofNullable(inst);
	}
}
