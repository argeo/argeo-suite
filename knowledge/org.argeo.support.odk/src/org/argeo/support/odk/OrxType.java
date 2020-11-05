package org.argeo.support.odk;

import org.argeo.entity.JcrName;

/** Types related to the http://openrosa.org/xforms/xformsList namespace. */
public enum OrxType implements JcrName {
	submission, xml_submission_file;

	@Override
	public String getPrefix() {
		return prefix();
	}

	public static String prefix() {
		return "orx";
	}

	@Override
	public String getNamespace() {
		return namespace();
	}

	public static String namespace() {
		return "http://openrosa.org/xforms";
	}

}
