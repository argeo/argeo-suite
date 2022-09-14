package org.argeo.app.odk;

import org.argeo.api.acr.QNamed;

/** Types related to the http://openrosa.org/xforms/xformsList namespace. */
public enum OrxType implements QNamed {
	submission, xml_submission_file;

	@Override
	public String getDefaultPrefix() {
		return "orx";
	}

	@Override
	public String getNamespace() {
		return "http://openrosa.org/xforms";
	}

}
