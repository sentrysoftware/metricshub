package com.sentrysoftware.matrix.utils;


import java.util.function.Supplier;

/**
 * Assertion helper class that assists in checking arguments.
 *
 * <p>
 * Useful for identifying errors early and clearly at runtime.
 * 
 * @author Nassim BOUTEKEDJIRET
 * @since 1.0.00
 *
 */
public class Assert {

	private Assert() {

	}

	/**
	 * Assert a boolean expression, throwing an {@code IllegalStateException}
	 * if the expression evaluates to {@code false}.
	 * 
	 * <pre class="code">
	 * Assert.state(state != null, "The state is not initialized");
	 * </pre>
	 * 
	 * @param expression a boolean expression
	 * @param message    the exception message to use if the assertion fails
	 * @throws IllegalStateException if {@code expression} is {@code false}
	 */
	public static void state(boolean expression, String message) {

		if (!expression) {
			throw new IllegalStateException(message);
		}
	}

	/**
	 * Assert a boolean expression, throwing an {@code IllegalStateException}
	 * if the expression evaluates to {@code false}.
	 * <p>
	 * 
	 * <pre class="code">
	 * Assert.state(state.getId() != null,
	 * 		() -> "ID for state " + state.getName() + " is not initialized");
	 * </pre>
	 * 
	 * @param expression      a boolean expression
	 * @param messageSupplier a supplier for the exception message to use if the
	 *                        assertion fails
	 * @throws IllegalStateException if {@code expression} is {@code false}
	 */
	public static void state(boolean expression, Supplier<String> messageSupplier) {

		if (!expression) {
			throw new IllegalStateException(messageSupplier.get());
		}
	}

	/**
	 * Assert a boolean expression, throwing an {@code IllegalArgumentException}
	 * if the expression evaluates to {@code false}.
	 * 
	 * <pre class="code">
	 * Assert.isTrue(i > -1, "i must be greater than -1");
	 * </pre>
	 * 
	 * @param expression a boolean expression
	 * @param message    the exception message to use if the assertion fails
	 * @throws IllegalArgumentException if {@code expression} is {@code false}
	 */
	public static void isTrue(boolean expression, String message) {

		if (!expression) {
			throw new IllegalArgumentException(message);
		}
	}

	/**
	 * Assert a boolean expression, throwing an {@code IllegalArgumentException}
	 * if the expression evaluates to {@code false}.
	 * 
	 * <pre class="code">
	 * Assert.isTrue(i > -1, () -> "The value '" + i + "' must be greater than -1");
	 * </pre>
	 * 
	 * @param expression      a boolean expression
	 * @param messageSupplier a supplier for the exception message to use if the
	 *                        assertion fails
	 * @throws IllegalArgumentException if {@code expression} is {@code false}
	 */
	public static void isTrue(boolean expression, Supplier<String> messageSupplier) {

		if (!expression) {
			throw new IllegalArgumentException(messageSupplier.get());
		}
	}

	/**
	 * Assert that an object is not {@code null}.
	 * 
	 * <pre class="code">
	 * Assert.notNull(argument, "The argument must not be null");
	 * </pre>
	 * 
	 * @param object  the object to check
	 * @param message the exception message to use if the assertion fails
	 * @throws IllegalArgumentException if the object is {@code null}
	 */
	public static void notNull(Object object, String message) {

		if (object == null) {
			throw new IllegalArgumentException(message);
		}
	}

	/**
	 * Assert that an object is not {@code null}.
	 * 
	 * <pre class="code">
	 * Assert.notNull(entity.getId(),
	 * 		() -> "ID for entity " + entity.getName() + " must not be null");
	 * </pre>
	 * 
	 * @param object          the object to check
	 * @param messageSupplier a supplier for the exception message to use if the
	 *                        assertion fails
	 * @throws IllegalArgumentException if the object is {@code null}
	 */
	public static void notNull(Object object, Supplier<String> messageSupplier) {

		if (object == null) {
			throw new IllegalArgumentException(messageSupplier.get());
		}
	}

}