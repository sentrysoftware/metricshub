package com.sentrysoftware.matrix.connector.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;

import org.junit.platform.commons.util.ReflectionUtils;

public class TestHelper {

	/**
	 * Invoke the given methodName located in clazz using the given object
	 * @param <T>
	 * 
	 * @param clazz
	 * @param methodName
	 * @param parameterTypes
	 * @param parameters
	 * @param object
	 * @return {@link Object} instance
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T invokeMethod(final Class<?> clazz, final String methodName, final List<Class<?>> parameterTypes,
			final List<Object> parameters, final Object object)
			throws IllegalAccessException, InvocationTargetException {

		assertNotNull(clazz);
		assertNotNull(methodName);
		assertNotNull(parameterTypes);
		assertNotNull(parameters);
		assertEquals(parameterTypes.size(), parameters.size());

		final Optional<Method> serialize = ReflectionUtils.findMethod(clazz, methodName,
				parameterTypes.toArray(new Class<?>[parameterTypes.size()]));
		assertTrue(serialize.isPresent());

		final Method method = serialize.get();
		method.setAccessible(true);

		return (T) method.invoke(object, parameters.toArray(new Object[parameters.size()]));

	}

	/**
	 * Generic setter for an object's parameter. Use this setter for private fields.
	 * 
	 * @param <T>						The type of the object whose field we wish to set.
	 * @param object					The object whose field we wish to set.
	 * @param value						The value we wish to assign to the specified field.
	 *
	 * @throws NoSuchFieldException		If a field with the specified name is not found.
	 * @throws IllegalAccessException	If this Field object is enforcing Java language access control
	 * 									and the underlying field is either inaccessible or final.
	 */
	public static <T> void setField(final T object, final String parameter,
			final Object value) throws NoSuchFieldException, IllegalAccessException {

		Field field = object.getClass().getDeclaredField(parameter);
		field.setAccessible(true);
		field.set(object, value);
	}

	/**
	 * Set a static final field of a java class
	 * @param field {@link Field} instance
	 * @param newValue new value to set
	 * @throws Exception
	 */
	public static void setFinalStatic(Field field, Object newValue) throws Exception {

		// Why ? Java final fields can only be assigned once.
		// Turn off final modifier to inject the new value
		// Invert the Modifier.FINAL (16) bits to get -17 with the bitwise-complement ~
		// Use the bitwise-and & for current modifiers [ 26= PRIVATE(2) & STATIC(8) &
		// FINAL(16) ] and the inverted final to get 10 which corresponds to [ 10 =
		// PRIVATE(2) and STATIC(8) ]
		// https://stackoverflow.com/questions/3301635/change-private-static-final-field-using-java-reflection
		field.setAccessible(true);

		Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

		field.set(null, newValue);
	}
}
