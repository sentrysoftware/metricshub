package com.sentrysoftware.matrix.common.helpers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;

import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * Use this class only when strictly necessary.
 */
public class ReflectionHelper {

	private ReflectionHelper() {

	}

	/**
	 * Invoke the given methodName of the given object
	 * 
	 * @param <T>            The return type of the underlying method
	 * 
	 * @param object         The {@link Object} the underlying method belongs to
	 * @param methodName     The name of the underlying method
	 * @param parameterTypes The types of the parameters with which the underlying
	 *                       method should be invoked
	 * @param parameters     The parameters with which the underlying method should
	 *                       be invoked
	 *
	 * @return {@link Object} instance The value returned by the invocation of the
	 *         underlying method
	 *
	 * @throws UndeclaredThrowableException when {@link ReflectionUtils} is unable
	 *                                      to handle the exception
	 */
	@SuppressWarnings("unchecked")
	public static <T> T invokeMethod(final Object object, final String methodName, final List<Class<?>> parameterTypes,
			final List<Object> parameters) {

		Assert.notNull(methodName, "methodName cannot be null.");
		Assert.notNull(parameterTypes, "parameterTypes cannot be null.");
		Assert.notNull(parameters, "parameters cannot be null.");
		Assert.isTrue(parameterTypes.size() == parameters.size(), "parameterTypes.size() != parameters.size().");

		final Method method = ReflectionUtils.findMethod(object.getClass(), methodName,
				parameterTypes.toArray(new Class<?>[0]));

		if (method == null) {
			throw new IllegalArgumentException("Method not found");
		}

		ReflectionUtils.makeAccessible(method);

		return (T) ReflectionUtils.invokeMethod(method, object, parameters.toArray(new Object[0]));

	}

	/**
	 * Generic setter for an object's parameter. Use this setter for private fields.
	 * 
	 * @param <T>    The type of the object whose field we wish to set.
	 * @param object The object whose field we wish to set.
	 * @param value  The value we wish to assign to the specified field.
	 * 
	 * @throws UndeclaredThrowableException when {@link ReflectionUtils} is unable
	 *                                      to handle the exception
	 **/
	public static <T> void setField(final T object, final String parameter, final Object value) {

		Assert.notNull(object, "object cannot be null.");
		Assert.notNull(parameter, "parameter cannot be null.");
		Assert.notNull(value, "value cannot be null.");

		Field field = ReflectionUtils.findField(object.getClass(), parameter);

		Assert.state(field != null, "field cannot be null.");

		ReflectionUtils.makeAccessible(field);
		ReflectionUtils.setField(field, object, value);

	}

}
