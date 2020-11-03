package org.argeo.entity;

/** Can be applied to {@link Enum}s in order to generate prefixed names. */
public interface JcrName {
	String name();

	default String getPrefix() {
		return null;
	}

	default String getNamespace() {
		return null;
	}

	default String get() {
		String prefix = getPrefix();
		return prefix != null ? prefix + ":" + name() : name();
	}

	default String withNamespace() {
		String namespace = getNamespace();
		if (namespace == null)
			throw new UnsupportedOperationException("No namespace is specified for " + getClass());
		return "{" + namespace + "}" + name();
	}
}
