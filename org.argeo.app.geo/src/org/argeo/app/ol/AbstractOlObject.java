package org.argeo.app.ol;

import java.util.HashMap;
import java.util.Map;

import org.argeo.app.ux.js.AbstractJsObject;

public abstract class AbstractOlObject extends AbstractJsObject {

	public AbstractOlObject(Object... args) {
		super(args.length > 0 ? args : new Object[] { new HashMap<String, Object>() });
	}

	public AbstractOlObject(Map<String, Object> options) {
		super(options);
	}

	public String getJsPackage() {
		return "argeo.tp.ol";
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
}
