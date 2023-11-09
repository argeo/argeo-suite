package org.argeo.app.ol;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.argeo.app.ux.js.AbstractJsObject;

public abstract class AbstractOlObject extends AbstractJsObject {
	public final static String JS_PACKAGE = "argeo.tp.ol";

	public AbstractOlObject(Object... args) {
		super(args.length > 0 ? args : new Object[] { new HashMap<String, Object>() });
	}

//	public AbstractOlObject(Map<String, Object> options) {
//		super(new Object[] { options });
//	}

	public String getJsPackage() {
		return JS_PACKAGE;
	}

	@SuppressWarnings("unchecked")
	protected Map<String, Object> getNewOptions() {
		if (!isNew())
			throw new IllegalStateException("Object " + getJsClassName() + " is not new");
		Object[] args = getJsConstructorArgs();
		if (args.length != 1 || !(args[0] instanceof Map))
			throw new IllegalStateException("Object " + getJsClassName() + " has no available options");
		return (Map<String, Object>) args[0];
	}

	protected void doSetValue(String methodName, String newOption, Object value) {
		if (isNew()) {
			Objects.requireNonNull(newOption, "Value cannot be set as an option for " + getJsClassName() + ", use "
					+ methodName + "() after the object has been created");
			getNewOptions().put(newOption, value);
		} else {
			Objects.requireNonNull(methodName, "Value cannot be set via a method for " + getJsClassName() + ", use "
					+ newOption + " before the object is created");
			executeMethod(methodName, value);
		}
	}

	public void set(String key, Object value) {
		set(key, value, false);
	}

	public void set(String key, Object value, boolean silent) {
		if (isNew()) {
			getNewOptions().put(key, value);
		} else {
			executeMethod(getMethodName(), new Object[] { key, value, silent });
		}
	}

	public Object get(String key) {
		if (isNew()) {
			return getNewOptions().get(key);
		} else {
			// TDO deal with reference if we are trying to get an object
			return callMethod(getMethodName(), key);
		}

	}

	public static String getJsClassName(Class<?> clss) {
		if (AbstractOlObject.class.isAssignableFrom(clss)) {
			// NB: would failed for renamed classes
			return JS_PACKAGE + "." + clss.getSimpleName();
		}
		throw new IllegalArgumentException(clss + " is not an OpenLayers object");
	}

}
