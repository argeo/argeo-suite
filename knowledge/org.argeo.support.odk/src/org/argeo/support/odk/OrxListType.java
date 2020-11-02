package org.argeo.support.odk;

import org.argeo.jcr.QualifiedName;

/** Types related to the http://openrosa.org/xforms/xformsList namespace. */
public enum OrxListType implements QualifiedName {
	xform;

	@Override
	public String getPrefix() {
		return prefix();
	}

	public static String prefix() {
		return "orxList";
	}

	@Override
	public String getNamespace() {
		return namespace();
	}

	public static String namespace() {
		return "http://openrosa.org/xforms/xformsList";
	}

}
